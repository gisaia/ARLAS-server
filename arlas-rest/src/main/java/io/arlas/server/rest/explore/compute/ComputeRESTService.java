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

package io.arlas.server.rest.explore.compute;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.ComputationEnum;
import io.arlas.server.model.request.ComputationRequest;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.response.ComputationResponse;
import io.arlas.server.model.response.Error;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

public class ComputeRESTService extends ExploreRESTServices {

    public ComputeRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_compute")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Compute", produces = UTF8JSON, notes = Documentation.COMPUTE_OPERATION, consumes = UTF8JSON, response = ComputationResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = ComputationResponse.class, responseContainer = "ArlasComputation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response compute(
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
            // ----------------------- COMPUTE -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "field",
                    value = Documentation.COMPUTE_FIELD,
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "field") String field,

            @ApiParam(name = "metric",
                    value = Documentation.COMPUTE_METRIC,
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "metric") String metric,
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
    ) throws ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        ComputationRequest computationRequest = new ComputationRequest();
        computationRequest.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat);
        computationRequest.field = field;
        computationRequest.metric = ComputationEnum.fromValue(metric);
        ComputationRequest computationRequestHeader = new ComputationRequest();
        computationRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        exploreServices.setValidGeoFilters(collectionReference, computationRequestHeader);
        MixedRequest request = new MixedRequest();
        request.basicRequest = computationRequest;
        request.headerRequest = computationRequestHeader;
        ComputationResponse computationResponse = exploreServices.compute(request, collectionReference);
        return cache(Response.ok(computationResponse), maxagecache) ;
    }


    @Timed
    @Path("{collection}/_compute")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Compute", produces = UTF8JSON, notes = Documentation.COMPUTE_OPERATION, consumes = UTF8JSON, response = ComputationResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = ComputationResponse.class, responseContainer = "ArlasComputation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})

    public Response computePost(
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
            // ----------------------- COMPUTE -----------------------
            // --------------------------------------------------------
            ComputationRequest computationRequest,

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
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        ComputationRequest computationRequestHeader = new ComputationRequest();
        computationRequestHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreServices.setValidGeoFilters(collectionReference, computationRequest);
        exploreServices.setValidGeoFilters(collectionReference, computationRequestHeader);

        request.basicRequest = computationRequest;
        request.headerRequest = computationRequestHeader;
        ComputationResponse computationResponse = exploreServices.compute(request, collectionReference);
        return cache(Response.ok(computationResponse), maxagecache) ;
    }

}
