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

package io.arlas.server.stac.api;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.stac.model.ConformanceClasses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class StacConformanceRESTService extends StacRESTService {

    public StacConformanceRESTService(STACConfiguration configuration,
                                      int arlasRestCacheTimeout,
                                      CollectionReferenceService collectionReferenceService,
                                      ExploreService exploreService, String baseUri) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService, baseUri);
    }

    @Timed
    @Path("/conformance")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Conformance",
            description = """
                    The URIs of all conformance classes supported by the server.
                    To support "generic" clients that want to access multiple OGC API Features implementations -
                    and not "just" a specific API / server, the server declares the conformance classes it implements and conforms to.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConformanceClasses.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getConformanceDeclaration() {
        return cache(Response.ok(new ConformanceClasses().conformsTo(configuration.conformsTo)), 0);
    }
}
