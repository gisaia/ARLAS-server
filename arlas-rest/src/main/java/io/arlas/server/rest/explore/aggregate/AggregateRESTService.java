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
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.AggregationsRequest;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.Error;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreService;
import io.arlas.server.utils.ColumnFilterUtil;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.BooleanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AggregateRESTService extends ExploreRESTServices {

    public AggregateRESTService(ExploreService exploreService) {
        super(exploreService);
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
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.AGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "dateformat",
                    value = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,
            @ApiParam(name = "flat", value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {
        long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreService.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        AggregationsRequest aggregationsRequest = new AggregationsRequest();
        aggregationsRequest.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat);
        aggregationsRequest.aggregations = ParamsParser.getAggregations(collectionReference, agg);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequest);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationsRequest);

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

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
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,


            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreService.getDaoCollectionReference()
                .getCollectionReference(collection);

        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequest);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationsRequest);

        request.basicRequest = aggregationsRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        AggregationResponse aggregationResponse = getArlasAggregation(request, collectionReference, (aggregationsRequest.form != null && BooleanUtils.isTrue(aggregationsRequest.form.flat)));
        aggregationResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);

        return cache(Response.ok(aggregationResponse), maxagecache);
    }

    public AggregationResponse getArlasAggregation(MixedRequest request, CollectionReference collectionReference, boolean flat) throws ArlasException {
        AggregationResponse aggregationResponse = exploreService.aggregate(request,
                collectionReference,
                false,
                ((AggregationsRequest) request.basicRequest).aggregations,
                0,
                System.nanoTime());
        return  flat ? flatten(aggregationResponse) : aggregationResponse;
    }

    private AggregationResponse flatten(AggregationResponse aggregationResponse) {
        List<AggregationResponse> elements = aggregationResponse.elements;
        if (elements != null && elements.size() > 0) {
            for (AggregationResponse element : elements) {
                element.flattenedElements = new HashMap<>();
                exploreService.flat(
                                element,
                                new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR),
                                s -> (!"elements".equals(s))
                        )
                        .forEach((key, value) -> element.flattenedElements.put(key,value));
                element.elements = null;
                element.metrics = null;
                if (element.hits != null) {
                    element.hits = element.hits.stream().map(hit -> MapExplorer.flat(hit,new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), new HashSet<>())).collect(Collectors.toList());
                }
            }
        }
        return aggregationResponse;
    }
}
