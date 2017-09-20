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

package io.arlas.server.rest.collections;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.Success;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.rest.ResponseFormatter;
import io.arlas.server.rest.explore.Documentation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public abstract class CollectionService extends CollectionRESTServices {

    protected CollectionReferenceDao dao = null;
    
    @Timed
    @Path("/")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Get all collection references",
            produces=UTF8JSON,
            notes = "Get all collection references in ARLAS",
            consumes=UTF8JSON
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class, responseContainer = "List" ),
	    @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response getAll(
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        List<CollectionReference> collections = dao.getAllCollectionReferences();
        return ResponseFormatter.getResultResponse(collections);
    }

    @Timed
    @Path("{collection}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Get a collection reference",
            produces=UTF8JSON,
            notes = "Get a collection reference in ARLAS",
            consumes=UTF8JSON,
            response = CollectionReference.class

    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class),
	    @ApiResponse(code = 404, message = "Collection not found.", response = Error.class),
	    @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response get(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value= Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        CollectionReference cr = dao.getCollectionReference(collection);
        return ResponseFormatter.getResultResponse(cr);
    }

    @Timed
    @Path("{collection}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Add a collection reference",
            produces=UTF8JSON,
            notes = "Add a collection reference in ARLAS",
            consumes=UTF8JSON,
            response = CollectionReference.class
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class),
	    @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
	    @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
	    @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response put(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,
            @ApiParam(name = "collectionParams",
                        value="collectionParams",
                        required=true)
            @NotNull @Valid CollectionReferenceParameters collectionReferenceParameters,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty

    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        dao.checkCollectionReferenceParameters(collectionReferenceParameters);
        CollectionReference cr = dao.putCollectionReference(collection, collectionReferenceParameters);
        return ResponseFormatter.getResultResponse(cr);
    }

    @Timed
    @Path("{collection}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Delete a collection reference",
            produces=UTF8JSON,
            notes = "Delete a collection reference in ARLAS",
            consumes=UTF8JSON
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Success.class),
	    @ApiResponse(code = 404, message = "Collection not found.", response = Error.class),
	    @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response delete(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        dao.deleteCollectionReference(collection);
        return ResponseFormatter.getSuccessResponse("Collection " + collection + " deleted.");
    }
}
