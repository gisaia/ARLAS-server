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
import io.arlas.server.core.app.Documentation;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.model.response.ArlasHit;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RawRESTService extends ExploreRESTServices {

    public RawRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/{identifier}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Get an Arlas document", produces = UTF8JSON, notes = "Returns a raw indexed document.", consumes = UTF8JSON, response = ArlasHit.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = ArlasHit.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class)})
    public Response getArlasHit(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(name = "identifier",
                    value = "identifier",
                    required = true)
            @PathParam(value = "identifier") String identifier,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = "Pretty print",
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat",
                    value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException("Collection " + collection + " not found.");
        }

        ColumnFilterUtil.assertCollectionsAllowed(columnFilter, Collections.singletonList(collectionReference));

        String[] includes = ColumnFilterUtil.cleanColumnFilter(columnFilter)
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
