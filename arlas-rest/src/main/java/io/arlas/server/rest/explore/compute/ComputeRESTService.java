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
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.request.ComputationRequest;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.response.ComputationResponse;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.core.utils.ParamsParser;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class ComputeRESTService extends ExploreRESTServices {

    public ComputeRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_compute")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Compute",
            description = Documentation.COMPUTE_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComputationResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response compute(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- COMPUTE -----------------------
            // --------------------------------------------------------
            @Parameter(name = "field",
                    description = Documentation.COMPUTE_FIELD,
                    required = true)
            @QueryParam(value = "field") String field,

            @Parameter(name = "metric",
                    description = Documentation.COMPUTE_METRIC,
                    required = true)
            @QueryParam(value = "metric") String metric,

            /*@Parameter(name = "precision",
                    description = Documentation.COMPUTE_PRECISON,
                    schema = @Schema(defaultValue = "3000"))
            @DefaultValue("3000")
            @QueryParam(value = "precision") int precision,*/
            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
            // --------------------------------------------------------
            @Parameter(name = "f",
                    description = Documentation.FILTER_PARAM_F,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "f") List<String> f,

            @Parameter(name = "q", description = Documentation.FILTER_PARAM_Q,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "q") List<String> q,

            @Parameter(name = "dateformat",
                    description = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @Parameter(name = "righthand",
                    schema = @Schema(defaultValue = "true"),
                    description = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        ComputationRequest computationRequest = new ComputationRequest();
        computationRequest.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand);
        computationRequest.field = field;
        computationRequest.metric = ComputationEnum.fromValue(metric);
        // the precision threshold is initialised with 3000 when creating the computationRequest object
        //computationRequest.precisionThreshold = precision;

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, computationRequest);

        ComputationRequest computationRequestHeader = new ComputationRequest();
        computationRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        exploreService.setValidGeoFilters(collectionReference, computationRequestHeader);
        MixedRequest request = new MixedRequest();
        request.basicRequest = computationRequest;
        request.headerRequest = computationRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);
        ComputationResponse computationResponse = exploreService.compute(request, collectionReference);
        return cache(Response.ok(computationResponse), maxagecache) ;
    }


    @Timed
    @Path("{collection}/_compute")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Compute", description = Documentation.COMPUTE_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComputationResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    public Response computePost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- COMPUTE -----------------------
            // --------------------------------------------------------
            ComputationRequest computationRequest,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        ComputationRequest computationRequestHeader = new ComputationRequest();
        computationRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, computationRequest);
        exploreService.setValidGeoFilters(collectionReference, computationRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, computationRequest);

        request.basicRequest = computationRequest;
        request.headerRequest = computationRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);
        ComputationResponse computationResponse = exploreService.compute(request, collectionReference);
        return cache(Response.ok(computationResponse), maxagecache) ;
    }

}
