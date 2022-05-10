/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.rest.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.IOUtils;
import io.swagger.annotations.*;
import org.apache.commons.io.FileUtils;
import org.geojson.FeatureCollection;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipOutputStream;

@Path("/explore")
@Api(value = "/explore")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS Exploration API",
                description = "Explore the content of ARLAS collections",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "22.0.0-beta.3"),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS })

public abstract class ExploreRESTServices {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExploreRESTServices.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";
    public static final String ZIPFILE = "application/zip";
    private CoordinateReferenceSystem CRS_WGS84;
    private static ObjectMapper mapper = new ObjectMapper();

    protected ExploreService exploreService;

    public ExploreRESTServices(ExploreService exploreService) {
        this.exploreService = exploreService;
        try {
            CRS_WGS84 = CRS.parseWKT("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]");
        } catch (FactoryException e) {
            CRS_WGS84 = DefaultGeographicCRS.WGS84;
        }

    }

    public Response cache(Response.ResponseBuilder response, Integer maxagecache) {
        return exploreService.getResponseCacheManager().cache(response, maxagecache);
    }

    public String getExplorePathUri() {
        return "explore/";
    }

    public File toShapefile(FeatureCollection geojson, Map<String, String> shapeColumnNames) throws ArlasException {
        try {
            java.nio.file.Path tempDir = Files.createTempDirectory("shpdir");
            try (InputStream in = new ByteArrayInputStream(mapper.writeValueAsBytes(geojson))) {
                // get GeoJson source
                org.geotools.feature.FeatureCollection fc = new FeatureJSON(new GeometryJSON(15)).readFeatureCollection(in);

                if (!fc.isEmpty()) {
                    // Create schema
                    List<String> originalColNames = new ArrayList<>();
                    AtomicInteger colNum = new AtomicInteger(0);
                    SimpleFeatureType geoJsonSchema = (SimpleFeatureType) fc.getSchema();
                    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                    builder.setName(geoJsonSchema.getTypeName());
                    geoJsonSchema.getAttributeDescriptors().stream().forEach(adesc -> {
                        originalColNames.add(adesc.getLocalName());
                        builder.nillable(adesc.isNillable());
                        builder.minOccurs(adesc.getMinOccurs());
                        builder.maxOccurs(adesc.getMaxOccurs());
                        if (adesc instanceof GeometryDescriptorImpl) {
                            // the geometry column name of schema for saving into a shapefile must be "the_geom"
                            builder.add("the_geom", adesc.getType().getBinding(), CRS_WGS84);
                        } else {
                            String columnName = shapeColumnNames!=null && shapeColumnNames.containsKey(adesc.getLocalName()) ?
                                    shapeColumnNames.get(adesc.getLocalName()) : adesc.getLocalName();
                            builder.add(getShortColumnName(columnName, colNum.get()),
                                    adesc.getType().getBinding() == Object.class ? String.class : adesc.getType().getBinding());
                        }
                        colNum.getAndIncrement();
                    });
                    // write column mapping
                    Files.write(Paths.get(tempDir.toString(), "column_mapping.txt"),
                            String.join(",", originalColNames).getBytes());
                    final SimpleFeatureType SCHEMA = builder.buildFeatureType();

                    // Convert GeoJson features to features with short column names
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(SCHEMA);
                    List<SimpleFeature> newGeoJsonSource = new ArrayList<>();
                    try (FeatureIterator iter = fc.features()) {
                        while (iter.hasNext()) {
                            SimpleFeatureImpl originalFeature = (SimpleFeatureImpl) iter.next();
                            featureBuilder.addAll(originalFeature.getAttributes());
                            newGeoJsonSource.add(featureBuilder.buildFeature(originalFeature.getID()));
                        }
                    }
                    if (!new ShapefileDumper(tempDir.toFile()).dump(new ListFeatureCollection(SCHEMA, newGeoJsonSource))) {
                        throw new ArlasException("Shapefile could not be created.");
                    }
                } else {
                    Files.write(Paths.get(tempDir.toString(), "README.txt"),
                            "No shapefile could be generated because the request returned no data.".getBytes());
                }

                // Create Shapefile
                File zipFile = Files.createTempFile("shapefile", ".zip").toFile();
                FileOutputStream output = new FileOutputStream(zipFile);
                // zip all the files produced
                final FilenameFilter filter = (dir, name) -> {
                    name = name.toLowerCase();
                    return name.endsWith(".shp") || name.endsWith(".shx") || name.endsWith(".dbf")
                            || name.endsWith(".prj") || name.endsWith(".cst") || name.endsWith(".txt");
                };
                ZipOutputStream zipOut = new ZipOutputStream(output);
                IOUtils.zipDirectory(tempDir.toFile(), "", zipOut, filter);
                zipOut.finish();
                return zipFile;
            } finally {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    LOGGER.warn("Could not delete temp directory: " + tempDir.toString() + " due to: " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new ArlasException("IOException while generating shapefile", e);
        }
    }

    private static String getShortColumnName(String s, int colNum) {
        return s.length() <= 10 ? s : "column" + colNum;
    }
}
