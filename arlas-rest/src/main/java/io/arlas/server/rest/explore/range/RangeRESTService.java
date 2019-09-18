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

package io.arlas.server.rest.explore.range;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.RangeRequest;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.RangeResponse;
import io.arlas.server.app.Documentation;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RangeRESTService extends ExploreRESTServices {
    public RangeRESTService(ExploreServices exploreServices) { super(exploreServices);}

    @Timed
    @Path("{collection}/_range")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "RangeRequest", produces = UTF8JSON, notes = Documentation.RANGE_OPERATION, consumes = UTF8JSON, response = RangeResponse.class

    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = RangeResponse.class, responseContainer = "ArlasRange"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response range(
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
            @ApiParam(name = "field",
                    value = Documentation.RANGE_FIELD,
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "field") String field,

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
        RangeRequest rangeRequest = new RangeRequest();
        rangeRequest.filter = ParamsParser.getFilter(f, q, pwithin, gwithin, gintersect, notpwithin, notgwithin, notgintersect, dateformat);
        rangeRequest.field = field;
        RangeRequest rangeRequestHeader = new RangeRequest();
        rangeRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = rangeRequest;
        exploreServices.setValidGeoFilters(rangeRequestHeader);
        request.headerRequest = rangeRequestHeader;

        RangeResponse rangeResponse = getFieldRange(request, collectionReference);
        rangeResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return cache(Response.ok(rangeResponse), maxagecache);
    }

    @Timed
    @Path("{collection}/_range")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Aggregate", produces = UTF8JSON, notes = Documentation.RANGE_OPERATION, consumes = UTF8JSON, response = RangeResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = RangeResponse.class, responseContainer = "ArlasRange"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response rangePost(
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
            RangeRequest rangeRequest,

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
        RangeRequest rangeRequestHeader = new RangeRequest();
        rangeRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreServices.setValidGeoFilters(rangeRequest);
        exploreServices.setValidGeoFilters(rangeRequestHeader);

        request.basicRequest = rangeRequest;
        request.headerRequest = rangeRequestHeader;

        RangeResponse rangeResponse = getFieldRange(request, collectionReference);
        rangeResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return cache(Response.ok(rangeResponse), maxagecache);
    }

    public RangeResponse getFieldRange(MixedRequest request, CollectionReference collectionReference) throws ArlasException, IOException{
        RangeResponse rangeResponse = new RangeResponse();
        Long startQuery = System.nanoTime();
        SearchResponse response = this.getExploreServices().getFieldRange(request, collectionReference);
        Aggregation firstAggregation = response.getAggregations().asList().get(0);
        Aggregation secondAggregation = response.getAggregations().asList().get(1);

        rangeResponse.totalnb = response.getHits().getTotalHits().value;
        if (rangeResponse.totalnb > 0) {
            if (firstAggregation.getName().equals(FluidSearch.FIELD_MIN_VALUE)) {
                rangeResponse.min = ((Min)firstAggregation).getValue();
                rangeResponse.max = ((Max)secondAggregation).getValue();
            } else {
                rangeResponse.min = ((Min)secondAggregation).getValue();
                rangeResponse.max = ((Max)firstAggregation).getValue();
            }
            CheckParams.checkRangeFieldExists(rangeResponse);
        } else {
            rangeResponse.min = rangeResponse.max = null;
        }
        
        rangeResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);
        return rangeResponse;
    }
}
