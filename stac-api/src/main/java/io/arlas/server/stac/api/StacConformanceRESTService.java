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
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.stac.model.ConformanceClasses;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class StacConformanceRESTService extends StacRESTService {

    public StacConformanceRESTService(STACConfiguration configuration,
                                      int arlasRestCacheTimeout,
                                      CollectionReferenceService collectionReferenceService,
                                      ExploreService exploreService) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService);
    }

    @Timed
    @Path("/conformance")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Conformance",
            notes = "The URIs of all conformance classes supported by the server.\n" +
                    "To support \"generic\" clients that want to access multiple OGC API Features implementations - " +
                    "and not \"just\" a specific API / server, the server declares the conformance classes it implements and conforms to.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ConformanceClasses.class, responseContainer = "ConformanceClasses"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response getConformanceDeclaration() {
        return cache(Response.ok(new ConformanceClasses().conformsTo(configuration.conformsTo)), 0);
    }
}
