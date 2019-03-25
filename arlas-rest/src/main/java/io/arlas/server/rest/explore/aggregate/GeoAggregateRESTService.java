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

package io.arlas.server.rest.explore.aggregate;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.AggregationTypeEnum;
import io.arlas.server.model.request.AggregationsRequest;
import io.arlas.server.model.request.Interval;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.Error;
import io.arlas.server.app.Documentation;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.BoundingBox;
import io.arlas.server.utils.GeoTileUtil;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GeoAggregateRESTService extends ExploreRESTServices {

    public GeoAggregateRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    private static final String FEATURE_TYPE_KEY = "feature_type";
    private static final String FEATURE_TYPE_VALUE = "aggregation";

    @Timed
    @Path("{collection}/_geoaggregate")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoAggregate", produces = UTF8JSON, notes = Documentation.GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geoaggregate(
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
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG,
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
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

            @ApiParam(name = "pwithin", value = Documentation.FILTER_PARAM_PWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "pwithin") List<String> pwithin,

            @ApiParam(name = "gwithin", value = Documentation.FILTER_PARAM_GWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gwithin") List<String> gwithin,

            @ApiParam(name = "gintersect", value = Documentation.FILTER_PARAM_GINTERSECT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gintersect") List<String> gintersect,

            @ApiParam(name = "notpwithin", value = Documentation.FILTER_PARAM_NOTPWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notpwithin") List<String> notpwithin,

            @ApiParam(name = "notgwithin", value = Documentation.FILTER_PARAM_NOTGWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgwithin") List<String> notgwithin,

            @ApiParam(name = "notgintersect", value = Documentation.FILTER_PARAM_NOTGINTERSECT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgintersect") List<String> notgintersect,

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,


            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException, JsonProcessingException {
        Long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        AggregationsRequest aggregationsRequest = new AggregationsRequest();
        aggregationsRequest.filter = ParamsParser.getFilter(f, q, pwithin, gwithin, gintersect, notpwithin, notgwithin, notgintersect, dateformat);
        aggregationsRequest.aggregations = ParamsParser.getAggregations(agg);
        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        request.headerRequest = aggregationsRequestHeader;
        FeatureCollection fc = getFeatureCollection(request, collectionReference, Boolean.TRUE.equals(flat), Optional.empty());
        return cache(Response.ok(fc), maxagecache);
    }


    @Timed
    @Path("{collection}/_geoaggregate/{geohash}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoAggregate on a geohash", produces = UTF8JSON, notes = Documentation.GEOHASH_GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geohashgeoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH ---------------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(
                    name = "geohash",
                    value = "geohash",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "geohash") String geohash,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION --------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG,
                    allowMultiple = false
            )
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -------------------------
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

            @ApiParam(name = "pwithin", value = Documentation.FILTER_PARAM_PWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "pwithin") List<String> pwithin,

            @ApiParam(name = "gwithin", value = Documentation.FILTER_PARAM_GWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gwithin") List<String> gwithin,

            @ApiParam(name = "gintersect", value = Documentation.FILTER_PARAM_GINTERSECT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gintersect") List<String> gintersect,

            @ApiParam(name = "notpwithin", value = Documentation.FILTER_PARAM_NOTPWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notpwithin") List<String> notpwithin,

            @ApiParam(name = "notgwithin", value = Documentation.FILTER_PARAM_NOTGWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgwithin") List<String> notgwithin,

            @ApiParam(name = "notgintersect", value = Documentation.FILTER_PARAM_NOTGINTERSECT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgintersect") List<String> notgintersect,

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter,

            // --------------------------------------------------------
            // ----------------------- FORM ---------------------------
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
            // ----------------------- EXTRA --------------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException, JsonProcessingException {
        if (geohash.startsWith("#")) {
            geohash = geohash.substring(1, geohash.length());
        }
        BoundingBox bbox = GeoTileUtil.getBoundingBox(geohash);
        String pwithinBbox = bbox.getWest() + "," + bbox.getSouth() + "," + bbox.getEast() + "," + bbox.getNorth();

        //check if every pwithin param has a value that intersects bbox
        List<String> simplifiedPwithin = ParamsParser.simplifyPwithinAgainstBbox(pwithin, bbox);

        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        if (agg == null || agg.size() == 0) {
            agg = Collections.singletonList("geohash:" + collectionReference.params.centroidPath + ":interval-" + geohash.length());
        }

        if (bbox != null && bbox.getNorth() > bbox.getSouth()
                // if sizes are not equals, it means one multi-value pwithin does not intersects bbox => no results
                && pwithin.size() == simplifiedPwithin.size()) {
            simplifiedPwithin.add(pwithinBbox);

            AggregationsRequest aggregationsRequest = new AggregationsRequest();
            aggregationsRequest.filter = ParamsParser.getFilter(f, q, simplifiedPwithin, gwithin, gintersect, notpwithin, notgwithin, notgintersect, dateformat);
            aggregationsRequest.aggregations = ParamsParser.getAggregations(agg);
            AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
            aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
            MixedRequest request = new MixedRequest();
            request.basicRequest = aggregationsRequest;
            request.headerRequest = aggregationsRequestHeader;
            FeatureCollection fc = getFeatureCollection(request, collectionReference, Boolean.TRUE.equals(flat), Optional.of(geohash));
            return cache(Response.ok(fc), maxagecache);
        } else {
            return Response.ok(new FeatureCollection()).build();
        }

    }

    @Timed
    @Path("{collection}/_geoaggregate")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoAggregate", produces = UTF8JSON, notes = Documentation.GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geoaggregatePost(
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
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            AggregationsRequest aggregationRequest,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationRequest;
        request.headerRequest = aggregationsRequestHeader;

        FeatureCollection fc = getFeatureCollection(request, collectionReference, (aggregationRequest.form != null && aggregationRequest.form.flat), Optional.empty());

        return cache(Response.ok(fc), maxagecache);
    }

    private FeatureCollection getFeatureCollection(MixedRequest request, CollectionReference collectionReference, boolean flat, Optional<String> geohash) throws ArlasException, IOException {
        Optional<Interval> interval = Optional.ofNullable(((AggregationsRequest) request.basicRequest).aggregations.get(0).interval);
        Optional<Number> precision = interval.map(i -> i.value);
        FeatureCollection fc;
        AggregationResponse aggregationResponse = new AggregationResponse();
        AggregationTypeEnum maintAggregationType = ((AggregationsRequest) request.basicRequest).aggregations.get(0).type;
        SearchResponse response = this.getExploreServices().aggregate(request, collectionReference, true);
        MultiBucketsAggregation aggregation;
        aggregation = (MultiBucketsAggregation) response.getAggregations().asList().get(0);
        aggregationResponse = this.getExploreServices().formatAggregationResult(aggregation, aggregationResponse, collectionReference.collectionName);
        fc = toGeoJson(aggregationResponse, maintAggregationType, flat, geohash, precision.map(p->p.intValue()));
        return fc;
    }

    private FeatureCollection toGeoJson(AggregationResponse aggregationResponse, AggregationTypeEnum mainAggregationType, boolean flat, Optional<String> geohash, Optional<Integer> precision) throws IOException {
        FeatureCollection fc = new FeatureCollection();
        ObjectMapper mapper = new ObjectMapper();
        List<AggregationResponse> elements = aggregationResponse.elements;
        if (geohash.isPresent() && precision.isPresent()) {
            if (geohash.get().length() < precision.get()) {
                elements = aggregationResponse.elements.stream()
                        .filter(element -> element.keyAsString.toString().startsWith(geohash.get())).collect(Collectors.toList());
            } else {
                elements = aggregationResponse.elements.stream()
                        .filter(element -> element.keyAsString.toString().equals(geohash.get().substring(0, precision.get()))).collect(Collectors.toList());
            }
        }
        if (elements != null && elements.size() > 0) {
            for (AggregationResponse element : elements) {
                Feature feature = new Feature();
                Map<String, Object> properties = new HashMap<>();
                properties.put("count", element.count);
                if (mainAggregationType == AggregationTypeEnum.geohash) {
                    properties.put("geohash", element.keyAsString);
                } else {
                    properties.put("key", element.keyAsString);
                }
                if (flat) {
                    this.getExploreServices().flat(element, new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), s -> (!"elements".equals(s))).forEach((key, value) -> {
                        properties.put(key, value);
                    });

                    if (element.hits != null) {
                        properties.put("hits", element.hits.stream().map(hit -> MapExplorer.flat(hit,new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), new HashSet<>())));
                    }
                }else{
                    properties.put("elements", element.elements);
                    properties.put("metrics", element.metrics);
                    if (element.hits != null) {
                        properties.put("hits", element.hits);
                    }
                }
                feature.setProperties(properties);
                feature.setProperty(FEATURE_TYPE_KEY, FEATURE_TYPE_VALUE);
                GeoJsonObject g = element.geometry;
                feature.setGeometry(g);
                fc.add(feature);
            }
        }
        return fc;
    }
}
