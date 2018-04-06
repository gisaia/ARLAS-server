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
import io.arlas.server.core.ElasticDocument;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.Hit;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RawRESTService extends ExploreRESTServices {
    public RawRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/{identifier}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Get an Arlas document", produces = UTF8JSON, notes = "Returns a raw indexed document.", consumes = UTF8JSON, response = Hit.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hit.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class)})
    public Response getArlasHit(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(
                    name = "identifier",
                    value = "identifier",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "identifier") String identifier,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException("Collection " + collection + " not found.");
        }

        ElasticDocument elasticDoc = new ElasticDocument(this.getExploreServices().getClient());
        Map<String, Object> source = elasticDoc.getSource(collectionReference, identifier);
        if (source == null || source.isEmpty()) {
            throw new NotFoundException("Document " + identifier + " not found.");
        }

        Hit hit = new Hit(collectionReference, source);
        return cache(Response.ok(hit), maxagecache);
    }
}
