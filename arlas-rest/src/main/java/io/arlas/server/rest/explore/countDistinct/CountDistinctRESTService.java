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

package io.arlas.server.rest.explore.countDistinct;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.CountDistinct;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.response.CountDistinctResponse;
import io.arlas.server.model.response.Error;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CountDistinctRESTService extends ExploreRESTServices {
    public CountDistinctRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_countDistinct")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Count distinct", produces = UTF8JSON, notes = "Counts the approximate distinct values of a given field, given the filters", consumes = UTF8JSON, response = CountDistinctResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CountDistinctResponse.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response countDistinct(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collections",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FIELD  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "field",
                    value = Documentation.COUNT_DISTINCT_FIELD,
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "field") String field,
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

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionfilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
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
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        Long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        CountDistinct countDistinct = new CountDistinct();
        countDistinct.filter = ParamsParser.getFilter(f, q, pwithin, gwithin, gintersect, notpwithin, notgwithin, notgintersect);
        countDistinct.field = field;
        CountDistinctResponse countDistinctResponse = getCountDistinctResponse(countDistinct, partitionfilter, collectionReference);
        countDistinctResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return cache(Response.ok(countDistinctResponse), maxagecache);
    }


    @Timed
    @Path("{collection}/_countDistinct")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Count distinct", produces = UTF8JSON, notes = "Counts the approximate distinct values of a given field, given the filters", consumes = UTF8JSON, response = CountDistinctResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CountDistinctResponse.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response countPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collections",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionfilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  COUNT DISTINC  -----------------------
            // --------------------------------------------------------
            CountDistinct countDistinct
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        Long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        CountDistinctResponse countDistinctResponse = getCountDistinctResponse(countDistinct, partitionfilter, collectionReference);
        countDistinctResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return Response.ok(countDistinctResponse).build();
    }

    private CountDistinctResponse getCountDistinctResponse(CountDistinct countDistinct, String partitionfilter, CollectionReference collectionReference) throws ArlasException, IOException {
        CheckParams.checkCountDistinctRequest(countDistinct);
        MixedRequest request = new MixedRequest();
        request.basicRequest = countDistinct;
        CountDistinct countDistinctHeader = new CountDistinct();
        countDistinctHeader.filter = ParamsParser.getFilter(partitionfilter);
        request.headerRequest = countDistinctHeader;
        CountDistinctResponse countDistinctResponse = this.getExploreServices().countDistinct(request, collectionReference);
        return countDistinctResponse;
    }
}
