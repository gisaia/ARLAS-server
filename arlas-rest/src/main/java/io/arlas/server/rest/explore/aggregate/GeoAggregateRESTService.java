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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.AggregationTypeEnum;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.Error;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.utils.*;
import io.arlas.server.services.ExploreServices;
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

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,


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
    ) throws IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geoaggregate(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat),
                partitionFilter, columnFilter, flat, agg, maxagecache, Optional.empty());

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

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

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
    ) throws IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        if (geohash.startsWith("#")) {
            geohash = geohash.substring(1, geohash.length());
        }
        BoundingBox bbox = GeoTileUtil.getBoundingBox(geohash);
        Expression pwithinBbox = new Expression(collectionReference.params.centroidPath, OperatorEnum.within,
                bbox.getWest() + "," + bbox.getSouth() + "," + bbox.getEast() + "," + bbox.getNorth());

        if (agg == null || agg.size() == 0) {
            agg = Collections.singletonList("geohash:" + collectionReference.params.centroidPath + ":interval-" + geohash.length());
        }

        return geoaggregate(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, bbox, pwithinBbox),
                partitionFilter, columnFilter, flat, agg, maxagecache, Optional.of(geohash));
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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreServices.setValidGeoFilters(collectionReference, aggregationRequest);
        exploreServices.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationRequest);

        request.basicRequest = aggregationRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        FeatureCollection fc = getFeatureCollection(request, collectionReference, (aggregationRequest.form != null && aggregationRequest.form.flat), Optional.empty());

        return cache(Response.ok(fc), maxagecache);
    }

    private Response geoaggregate(CollectionReference collectionReference, Filter filter, String partitionFilter, Optional<String> columnFilter,
                                  Boolean flat, List<String> agg, Integer maxagecache, Optional<String> geohash) throws ArlasException, IOException {
        AggregationsRequest aggregationsRequest = new AggregationsRequest();
        aggregationsRequest.filter = filter;
        aggregationsRequest.aggregations = ParamsParser.getAggregations(agg);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationsRequest);

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        exploreServices.setValidGeoFilters(collectionReference, aggregationsRequestHeader);
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);
        FeatureCollection fc = getFeatureCollection(request, collectionReference, Boolean.TRUE.equals(flat), geohash);
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
