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

package io.arlas.server.rest.explore.describe;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
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
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

public class DescribeCollectionRESTService extends ExploreRESTServices {

    public DescribeCollectionRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_describe")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Describe", produces = UTF8JSON, notes = "Describe the structure and the content of the given collection. ", consumes = UTF8JSON, response = CollectionReferenceDescription.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CollectionReferenceDescription.class, responseContainer = "CollectionReferenceDescription"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 403, message = "Collection not authorized.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response describe(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = "Pretty print",
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {

        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));

        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        ColumnFilterUtil.assertCollectionsAllowed(Optional.ofNullable(columnFilter), Collections.singletonList(collectionReference));
        CollectionReferenceDescription collectionReferenceDescription = exploreService.describeCollection(collectionReference, Optional.ofNullable(columnFilter));

        return cache(Response.ok(collectionReferenceDescription), maxagecache);
    }
}
