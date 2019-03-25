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
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.AggregationTypeEnum;
import io.arlas.server.model.request.AggregationsRequest;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.Error;
import io.arlas.server.app.Documentation;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.BooleanUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AggregateRESTService extends ExploreRESTServices {

    public AggregateRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_aggregate")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Aggregate", produces = UTF8JSON, notes = Documentation.AGGREGATION_OPERATION, consumes = UTF8JSON, response = AggregationResponse.class

    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = AggregationResponse.class, responseContainer = "ArlasAggregation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response aggregate(
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
                    value = Documentation.AGGREGATION_PARAM_AGG
                    ,
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
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
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

        AggregationResponse aggregationResponse = getArlasAggregation(request, collectionReference, BooleanUtils.isTrue(flat));
        aggregationResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return cache(Response.ok(aggregationResponse), maxagecache);
    }

    @Timed
    @Path("{collection}/_aggregate")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Aggregate", produces = UTF8JSON, notes = Documentation.AGGREGATION_OPERATION, consumes = UTF8JSON, response = AggregationResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = AggregationResponse.class, responseContainer = "ArlasAggregation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response aggregatePost(
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
            AggregationsRequest aggregationsRequest,

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
        Long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        request.headerRequest = aggregationsRequestHeader;

        AggregationResponse aggregationResponse = getArlasAggregation(request, collectionReference, (aggregationsRequest.form != null && BooleanUtils.isTrue(aggregationsRequest.form.flat)));
        aggregationResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);

        return cache(Response.ok(aggregationResponse), maxagecache);
    }

    public AggregationResponse getArlasAggregation(MixedRequest request, CollectionReference collectionReference, boolean flat) throws ArlasException, IOException {
        AggregationResponse aggregationResponse = new AggregationResponse();
        Long startQuery = System.nanoTime();
        SearchResponse response = this.getExploreServices().aggregate(request, collectionReference, false);
        MultiBucketsAggregation aggregation;
        aggregation = (MultiBucketsAggregation) response.getAggregations().asList().get(0);
        aggregationResponse.totalnb = response.getHits().getTotalHits();
        aggregationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);
        aggregationResponse = this.getExploreServices().formatAggregationResult(aggregation, aggregationResponse, collectionReference.collectionName);
        return  flat ? flatten(aggregationResponse) : aggregationResponse;
    }

    private AggregationResponse flatten(AggregationResponse aggregationResponse) {
        List<AggregationResponse> elements = aggregationResponse.elements;
        if (elements != null && elements.size() > 0) {
            for (AggregationResponse element : elements) {
                element.flattenedElements = new HashMap<>();
                this.getExploreServices().flat(element, new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), s ->(!"elements".equals(s))).forEach((key, value) -> {
                    element.flattenedElements.put(key,value);
                });
                element.elements = null;
                element.metrics = null;
                if (element.hits != null) {
                    List<Object> flattenedHits = element.hits.stream().map(hit -> MapExplorer.flat(hit,new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), new HashSet<>())).collect(Collectors.toList());
                    element.hits = flattenedHits;
                }
            }
        }
        return aggregationResponse;
    }

}
