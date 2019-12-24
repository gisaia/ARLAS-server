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

package io.arlas.server.rest.explore.search;


import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.Hit;
import io.arlas.server.model.response.MD;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.*;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class GeoSearchRESTService extends ExploreRESTServices {

    public GeoSearchRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    private static final String FEATURE_TYPE_KEY = "feature_type";
    private static final String FEATURE_TYPE_VALUE = "hit";
    private static final String FEATURE_GEOMETRY_PATH = "geometry_path";


    @Timed
    @Path("{collection}/_geosearch")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoSearch", produces = UTF8JSON, notes = Documentation.GEOSEARCH_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response geosearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat", value = Documentation.FORM_FLAT,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "include", value = Documentation.PROJECTION_PARAM_INCLUDE,
                    allowMultiple = true,
                    defaultValue = "*",
                    required = false)
            @QueryParam(value = "include") String include,

            @ApiParam(name = "exclude", value = Documentation.PROJECTION_PARAM_EXCLUDE,
                    allowMultiple = true,
                    defaultValue = "",
                    required = false)
            @QueryParam(value = "exclude") String exclude,

            @ApiParam(name = "returned_geometries",
                    value = Documentation.PROJECTION_PARAM_RETURNED_GEOMETRIES,
                    allowMultiple = false,
                    defaultValue = "",
                    required = false)
            @QueryParam(value = "returned_geometries") String returned_geometries,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size", value = Documentation.PAGE_PARAM_SIZE,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @ApiParam(name = "from", value = Documentation.PAGE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @ApiParam(name = "sort",
                    value = Documentation.PAGE_PARAM_SORT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "sort") String sort,

            @ApiParam(name = "after",
                    value = Documentation.PAGE_PARAM_AFTER,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "after") String after,

            @ApiParam(name = "before",
                    value = Documentation.PAGE_PARAM_BEFORE,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "before") String before,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws IOException, NotFoundException, ArlasException {

        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geosearch(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat), partitionFilter, columnFilter,
                flat, include, exclude, size, from, sort, after, before, maxagecache, returned_geometries);
    }

    @Timed
    @Path("{collection}/_geosearch/{z}/{x}/{y}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Tiled GeoSearch", produces = UTF8JSON, notes = Documentation.TILED_GEOSEARCH_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response tiledgeosearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            @ApiParam(
                    name = "x",
                    value = "x",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "x") Integer x,
            @ApiParam(
                    name = "y",
                    value = "y",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "y") Integer y,
            @ApiParam(
                    name = "z",
                    value = "z",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "z") Integer z,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat", value = Documentation.FORM_FLAT,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "include", value = Documentation.PROJECTION_PARAM_INCLUDE,
                    allowMultiple = true,
                    defaultValue = "*",
                    required = false)
            @QueryParam(value = "include") String include,

            @ApiParam(name = "exclude", value = Documentation.PROJECTION_PARAM_EXCLUDE,
                    allowMultiple = true,
                    defaultValue = "",
                    required = false)
            @QueryParam(value = "exclude") String exclude,

            @ApiParam(name = "returned_geometries",
                    value = Documentation.PROJECTION_PARAM_RETURNED_GEOMETRIES,
                    allowMultiple = false,
                    defaultValue = "",
                    required = false)
            @QueryParam(value = "returned_geometries") String returned_geometries,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size", value = Documentation.PAGE_PARAM_SIZE,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @ApiParam(name = "from", value = Documentation.PAGE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @ApiParam(name = "sort",
                    value = Documentation.PAGE_PARAM_SORT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "sort") String sort,

            @ApiParam(name = "after",
                    value = Documentation.PAGE_PARAM_AFTER,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "after") String after,

            @ApiParam(name = "before",
                    value = Documentation.PAGE_PARAM_BEFORE,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "before") String before,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        BoundingBox bbox = GeoTileUtil.getBoundingBox(new Tile(x, y, z));
        // west, south, east, north

        Expression pwithinBbox = new Expression(collectionReference.params.centroidPath, OperatorEnum.within,
                bbox.getWest() + "," + bbox.getSouth() + "," + bbox.getEast() + "," + bbox.getNorth());

        return geosearch(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, bbox, pwithinBbox),
                partitionFilter, columnFilter, flat, include, exclude, size, from, sort, after, before, maxagecache, returned_geometries);
    }


    @Timed
    @Path("{collection}/_geosearch")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoSearch", produces = UTF8JSON, notes = Documentation.GEOSEARCH_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response geosearchPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  Search   -----------------------
            // --------------------------------------------------------
            Search search,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,
            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        String includes = search.projection != null ? search.projection.includes : null;
        String excludes = search.projection != null ? search.projection.excludes : null;
        CheckParams.checkReturnedGeometries(collectionReference, includes, excludes, search.returned_geometries);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(partitionFilter);

        exploreServices.setValidGeoFilters(collectionReference, search);
        exploreServices.setValidGeoFilters(collectionReference, searchHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        search.projection = ParamsParser.enrichIncludes(search.projection, search.returned_geometries);

        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = columnFilter;

        FeatureCollection fc = getFeatures(collectionReference, request, (search.form!=null && search.form.flat));
        return cache(Response.ok(fc), maxagecache);
    }

    protected FeatureCollection getFeatures(CollectionReference collectionReference, MixedRequest request, boolean flat) throws ArlasException, IOException {
        SearchHits searchHits = this.getExploreServices().search(request, collectionReference);
        Search searchRequest = (Search) request.basicRequest;
        FeatureCollection fc = new FeatureCollection();
        List<SearchHit> results = Arrays.asList(searchHits.getHits());
        if (searchRequest.page != null && searchRequest.page.before != null) {
            Collections.reverse(results);
        }
        for (SearchHit hit : results) {
            Map<String, Object> source = hit.getSourceAsMap();
            Hit arlasHit = new Hit(collectionReference, source, searchRequest.returned_geometries, flat, true);
            if (searchRequest.returned_geometries != null) {
                for (String path : searchRequest.returned_geometries.split(",")) {
                    GeoJsonObject g = arlasHit.getGeometry(path);
                    if (g != null) fc.add(getFeatureFromHit(arlasHit, path, g));
                }
            } else {
                //Apply geometry or centroid to geo json feature
                if (arlasHit.md.geometry != null) {
                    fc.add(getFeatureFromHit(arlasHit, collectionReference.params.geometryPath, arlasHit.md.geometry));
                } else if (arlasHit.md.centroid != null) {
                    fc.add(getFeatureFromHit(arlasHit, collectionReference.params.centroidPath, arlasHit.md.centroid));
                }
            }
        }
        return fc;
    }

    private Feature getFeatureFromHit(Hit arlasHit, String path, GeoJsonObject geometry) {
        Feature feature = new Feature();

        /** Setting geometry of geojson */
        feature.setGeometry(geometry);

        /** setting the properties of the geojson */
        feature.setProperties(new HashMap<>(arlasHit.getDataAsMap()));

        /** Setting the Metadata (md) in properties of geojson.
         * Only id, timestamp and centroid are set in the MD. The geometry is already returned in the geojson.*/
        MD md = new MD();
        md.id = arlasHit.md.id;
        md.timestamp = arlasHit.md.timestamp;
        md.centroid = arlasHit.md.centroid;
        feature.setProperty(MD.class.getSimpleName().toLowerCase(), md);

        /** Setting the feature type of the geojson */
        feature.setProperty(FEATURE_TYPE_KEY, FEATURE_TYPE_VALUE);
        feature.setProperty(FEATURE_GEOMETRY_PATH, path);
        return feature;
    }

    private Response geosearch(CollectionReference collectionReference, Filter filter, String partitionFilter,
                               Optional<String> columnFilter, Boolean flat, String include, String exclude, IntParam size, IntParam from,
                               String sort, String after, String before, Integer maxagecache, String returned_geometries) throws ArlasException, IOException {

        CheckParams.checkReturnedGeometries(collectionReference, include, exclude, returned_geometries);

        Search search = new Search();
        search.filter = filter;
        search.page = ParamsParser.getPage(size, from, sort, after, before);
        search.projection = ParamsParser.getProjection(include, exclude);
        search.returned_geometries = returned_geometries;

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        search.projection = ParamsParser.enrichIncludes(search.projection, returned_geometries);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        exploreServices.setValidGeoFilters(collectionReference, searchHeader);
        request.headerRequest = searchHeader;
        request.columnFilter = columnFilter;
        FeatureCollection fc = getFeatures(collectionReference, request, (flat != null && flat));
        return cache(Response.ok(fc), maxagecache);
    }

}
