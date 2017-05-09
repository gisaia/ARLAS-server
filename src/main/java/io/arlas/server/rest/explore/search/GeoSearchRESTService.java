package io.arlas.server.rest.explore.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.arlas.server.rest.explore.Documentation;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.Point;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.CheckParams;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class GeoSearchRESTService extends ExploreRESTServices {

    public GeoSearchRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_geosearch")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Geoearch", produces = UTF8JSON, notes = Documentation.GEOSEARCH_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation") })
    public Response geosearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value="collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="f",
                    value=Documentation.FILTER_PARAM_F,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="f") List<String> f,

            @ApiParam(name ="q", value=Documentation.FILTER_PARAM_Q,
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="q") String q,

            @ApiParam(name ="before", value=Documentation.FILTER_PARAM_BEFORE,
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="before") LongParam before,

            @ApiParam(name ="after", value=Documentation.FILTER_PARAM_AFTER,
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="after") LongParam after,

            @ApiParam(name ="pwithin", value=Documentation.FILTER_PARAM_PWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="pwithin") String pwithin,

            @ApiParam(name ="gwithin", value=Documentation.FILTER_PARAM_GWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="gwithin") String gwithin,

            @ApiParam(name ="gintersect", value=Documentation.FILTER_PARAM_GINTERSECT,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="gintersect") String gintersect,

            @ApiParam(name ="notpwithin", value=Documentation.FILTER_PARAM_NOTPWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notpwithin") String notpwithin,

            @ApiParam(name ="notgwithin", value=Documentation.FILTER_PARAM_NOTGWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notgwithin") String notgwithin,

            @ApiParam(name ="notgintersect", value=Documentation.FILTER_PARAM_NOTGINTERSECT,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notgintersect") String notgintersect,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty,

            @ApiParam(name ="human", value=Documentation.FORM_HUMAN,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="human") Boolean human,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="include", value = Documentation.PROJECTION_PARAM_INCLUDE,
                    allowMultiple = true,
                    defaultValue = "*",
                    example = "*",
                    required=false)
            @QueryParam(value="include") String include,

            @ApiParam(name ="exclude", value = Documentation.PROJECTION_PARAM_EXCLUDE,
                    allowMultiple = true,
                    defaultValue = "*",
                    example = "city,state",
                    required=false)
            @QueryParam(value="exclude") String exclude,

            // --------------------------------------------------------
            // -----------------------  SIZE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="size", value = Documentation.SIZE_PARAM_SIZE,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    required=false)
            @DefaultValue("10")
            @QueryParam(value="size") IntParam size,

            @ApiParam(name ="from", value = Documentation.SIZE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    required=false)
            @DefaultValue("0")
            @QueryParam(value="from") IntParam from,

            // --------------------------------------------------------
            // -----------------------  SORT   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="sort",
                    value = Documentation.SORT_PARAM_SORT,
                    allowMultiple = true,
                    example = "-country,city",
                    required=false)
            @QueryParam(value="sort") String sort,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        SearchHits searchHits = this.search(
                collectionReference,
                f, q,
                before, after,
                pwithin, gwithin, gintersect, notpwithin, notgwithin, notgintersect,
                pretty, human,
                include, exclude,
                size, from, sort
        );
        FeatureCollection fc = new FeatureCollection();
        SearchHit[] results = searchHits.getHits();

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(GeoJsonObject.class);

        for (SearchHit hit : results) {
            Feature feature = new Feature();
            Map<String, Object> hitsSources = hit.getSource();
            if (collectionReference.params.geometryPath != null) {
                if (hitsSources.keySet().contains(collectionReference.params.geometryPath)) {
                    String geometryPath = collectionReference.params.geometryPath;
                    Object m = hitsSources.get(geometryPath);
                    GeoJsonObject g = reader.readValue(mapper.writer().writeValueAsString(m));
                    feature.setGeometry(g);
                    hitsSources.remove(geometryPath);
                    feature.setProperties(hit.getSource());
                } else {
                    feature.setProperties(hit.getSource());
                }
            } else if (collectionReference.params.centroidPath != null) {
                if (hitsSources.keySet().contains(collectionReference.params.centroidPath)) {
                    String centroidPath = collectionReference.params.centroidPath;
                    String pointString = (String) hitsSources.get(centroidPath);
                    String[] tokens = pointString.split(",");
                    Double latitude = Double.parseDouble(tokens[0]);
                    Double longitude = Double.parseDouble(tokens[1]);
                    GeoJsonObject g = new Point(latitude, longitude);
                    feature.setGeometry(g);
                    hitsSources.remove(centroidPath);
                    feature.setProperties(hit.getSource());
                } else {
                    feature.setProperties(hit.getSource());
                }
            }
            fc.add(feature);

        }
        return Response.ok(fc).build();

    }
}