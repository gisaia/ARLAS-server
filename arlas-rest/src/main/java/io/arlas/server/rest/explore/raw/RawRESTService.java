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

package io.arlas.server.rest.explore.raw;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.ArlasHit;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

public class RawRESTService extends ExploreRESTServices {

    public RawRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/{identifier}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Get an Arlas document",
            description = "Returns a raw indexed document."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = ArlasHit.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Not Found Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getArlasHit(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "identifier",
                    description = "identifier",
                    required = true)
            @PathParam(value = "identifier") String identifier,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = "Pretty print",
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            @Parameter(name = "flat",
                    description = Documentation.FORM_FLAT,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException("Collection " + collection + " not found.");
        }

        ColumnFilterUtil.assertCollectionsAllowed(Optional.ofNullable(columnFilter), Collections.singletonList(collectionReference));

        String[] includes = ColumnFilterUtil.cleanColumnFilter(Optional.ofNullable(columnFilter))
                .map(cf -> cf + "," + String.join(",", ColumnFilterUtil.getCollectionMandatoryPaths(collectionReference)))
                .map(i -> i.split(","))
                .orElse(new String[]{});

        Map<String, Object> source = exploreService.getRawDoc(collectionReference, identifier, includes);

        if (source == null || source.isEmpty()) {
            throw new NotFoundException("Document " + identifier + " not found.");
        }

        ArlasHit hit = new ArlasHit(collectionReference, source, Boolean.TRUE.equals(flat), false);
        return cache(Response.ok(hit), maxagecache);
    }
}
