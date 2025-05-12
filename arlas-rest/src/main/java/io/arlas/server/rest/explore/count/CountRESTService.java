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

package io.arlas.server.rest.explore.count;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.request.Count;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.response.Hits;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.core.utils.ParamsParser;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class CountRESTService extends ExploreRESTServices {

    public CountRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_count")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Count",
            description = "Count the number of elements found in the collection(s), given the filters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Hits.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response count(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collections",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
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
            @HeaderParam(value = PARTITION_FILTER) String partitionfilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService().getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        Count count = new Count();
        count.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, count);

        MixedRequest request = new MixedRequest();
        request.basicRequest = count;
        Count countHeader = new Count();
        countHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionfilter);
        exploreService.setValidGeoFilters(collectionReference, countHeader);
        request.headerRequest = countHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        Hits hits = exploreService.count(request, collectionReference);
        return cache(Response.ok(hits), maxagecache);
    }


    @Timed
    @Path("{collection}/_count")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Count",
            description = "Count the number of elements found in the collection(s), given the filters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Hits.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response countPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collections",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionfilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  SEARCH  -----------------------
            // --------------------------------------------------------
            Count count
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService().getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, count);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, count);

        request.basicRequest = count;
        Count countHeader = new Count();
        countHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionfilter);
        exploreService.setValidGeoFilters(collectionReference, countHeader);
        request.headerRequest = countHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        Hits hits = exploreService.count(request, collectionReference);
        return Response.ok(hits).build();
    }
}
