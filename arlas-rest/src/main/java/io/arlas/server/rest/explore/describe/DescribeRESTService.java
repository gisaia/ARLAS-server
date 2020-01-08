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
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.Error;
import io.arlas.server.app.Documentation;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DescribeRESTService extends ExploreRESTServices {

    public DescribeRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("_list")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "List", produces = UTF8JSON, notes = "List the collections configured in ARLAS. ", consumes = UTF8JSON, response = CollectionReferenceDescription.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CollectionReferenceDescription.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response list(

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,


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
    ) throws IOException, ArlasException {
        List<CollectionReference> collectionReferences = exploreServices.getDaoCollectionReference().getAllCollectionReferences();
        List<CollectionReferenceDescription> collectionReferenceDescriptionList = exploreServices.getElasticAdmin().describeAllCollections(collectionReferences, columnFilter);

        return cache(Response.ok(collectionReferenceDescriptionList), maxagecache);
    }
}
