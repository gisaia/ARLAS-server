package io.arlas.server.rest.explore.aggregate;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.ArlasAggregation;
import io.arlas.server.model.ArlasError;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.TimedFeatureCollection;
import io.arlas.server.rest.explore.Documentation;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.CheckParams;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.geojson.*;
import org.geojson.jackson.CrsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GeoAggregateRESTService extends ExploreRESTServices {

    static Logger LOGGER = LoggerFactory.getLogger(GeoAggregateRESTService.class);

    public GeoAggregateRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_geoaggregate")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Aggregate", produces = UTF8JSON, notes = Documentation.GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class

    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection" ),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = ArlasError.class), @ApiResponse(code = 400, message = "Bad request.", response = ArlasError.class) })
    public Response geoaggregate(
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
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="agg",
                    value= Documentation.GEOAGGREGATION_PARAM_AGG,
                    allowMultiple = false,
                    required=true)
            @QueryParam(value="agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="f",
                    value= Documentation.FILTER_PARAM_F,
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
            // ----------------------- FORM -----------------------
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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException, JsonProcessingException {
        Long startArlasTime = System.nanoTime();
        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        fluidSearch.setCollectionReference(collectionReference);

        if (f != null && !f.isEmpty()) {
            fluidSearch = fluidSearch.filter(f);
        }
        if (q != null) {
            fluidSearch = fluidSearch.filterQ(q);
        }
        if(before != null || after != null) {
            if((before!=null && before.get()<0) || (after != null && after.get()<0)
                    || (before != null && after != null && before.get() < after.get()))
                throw new InvalidParameterException(FluidSearch.INVALID_BEFORE_AFTER);
        }
        if (after != null) {
            fluidSearch = fluidSearch.filterAfter(after.get());
        }
        if (before != null) {
            fluidSearch = fluidSearch.filterBefore(before.get());
        }
        if (pwithin != null && !pwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(pwithin);
            if (tlbr.length == 4 && tlbr[0]>tlbr[2] && tlbr[2]<tlbr[3]) {
                fluidSearch = fluidSearch.filterPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (gwithin != null && !gwithin.isEmpty()) {
            fluidSearch = fluidSearch.filterGWithin(gwithin);
        }
        if (gintersect != null && !gintersect.isEmpty()) {
            fluidSearch = fluidSearch.filterGIntersect(gintersect);
        }
        if (notpwithin != null && !notpwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(notpwithin);
            if (tlbr.length == 4 && tlbr[0]>tlbr[2] && tlbr[2]<tlbr[3]) {
                fluidSearch = fluidSearch.filterNotPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (notgwithin != null && !notgwithin.isEmpty()) {
            fluidSearch = fluidSearch.filterNotGWithin(notgwithin);
        }
        if (notgintersect != null && !notgintersect.isEmpty()) {
            fluidSearch = fluidSearch.filterNotGIntersect(notgintersect);
        }

        FeatureCollection fc;
        MultiBucketsAggregation aggregation = null;
        ArlasAggregation arlasAggregation = new ArlasAggregation();
        if (agg != null && agg.size()>0){
            Long startQuery = System.nanoTime();
            fluidSearch.aggregate(agg, true);
            aggregation = (MultiBucketsAggregation)fluidSearch.exec().getAggregations().asList().get(0);
            arlasAggregation.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);

        }
        arlasAggregation = fluidSearch.formatAggregationResult(aggregation,arlasAggregation);
        fc = toGeoJson(arlasAggregation);
        //fc.queryTime = arlasAggregation.queryTime;
        //fc.arlasTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return Response.ok(fc).build();
    }

    private FeatureCollection toGeoJson(ArlasAggregation arlasAggregation) throws IOException{
        FeatureCollection fc = new FeatureCollection();
        List<ArlasAggregation> elements = arlasAggregation.elements;
        if (elements != null && elements.size()>0){
            for (ArlasAggregation element : elements){
                Feature feature = new Feature();
                Map<String,Object> properties = new HashMap<>();
                GeoPoint geoPoint = (GeoPoint)element.key;
                properties.put("count", element.count);
                properties.put("geohash", geoPoint.getGeohash());
                properties.put("elements", element.elements);
                feature.setProperties(properties);
                GeoJsonObject g = new Point(geoPoint.getLon(),geoPoint.getLat());
                feature.setGeometry(g);
                fc.add(feature);
            }
        }
        return fc;
    }
}