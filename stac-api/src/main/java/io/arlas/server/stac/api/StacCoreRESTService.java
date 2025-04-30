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
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.stac.model.LandingPage;
import io.arlas.server.stac.model.StacLink;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

public class StacCoreRESTService extends StacRESTService {

    private final String openAPIjson;
    public StacCoreRESTService(STACConfiguration configuration,
                               int arlasRestCacheTimeout,
                               CollectionReferenceService collectionReferenceService,
                               ExploreService exploreService, String baseUri, String openAPIjson) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService, baseUri);
        this.openAPIjson = openAPIjson;
    }

    @Timed
    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Landing page",
            description = """
                    Returns the root STAC Catalog or STAC Collection that is the entry point for users to browse with STAC Browser or for search engines to crawl.
                    This can either return a single STAC Collection or more commonly a STAC catalog.
                    The landing page provides links to the API definition (link relations `service-desc` and `service-doc`) and the STAC records such as collections/catalogs (link relation `child`) or items (link relation `item`).
                    Extensions may add additional links with new relation types.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                    The landing page provides links to the API definition (link relations `service-desc` and `service-doc`) and the Feature Collection (path `/collections`, link relation `data`).""",
                    content = @Content(schema = @Schema(implementation = LandingPage.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getLandingPage(@Context UriInfo uriInfo,
                                   @Parameter(hidden = true)
                                   @HeaderParam(value = COLUMN_FILTER) String columnFilter,

                                   @Parameter(hidden = true)
                                   @HeaderParam(value = ARLAS_ORGANISATION) String organisations

    ) throws ArlasException {

        List<StacLink> links = new ArrayList<>();
        links.add(getSelfLink(uriInfo));
        links.add(getRootLink(uriInfo));
        links.add(getLink(uriInfo, "conformance", "conformance", MediaType.APPLICATION_JSON));
        links.add(getApiLink(uriInfo));
//        links.add(getLink(uriInfo, "api.html", "service-doc", MediaType.TEXT_HTML)); // TODO (recommended)
        links.add(getLink(uriInfo, "collections", "data", MediaType.APPLICATION_JSON));
        links.add(getLink(uriInfo, "search","GET" ,"search", "application/geo+json"));
        links.add(getLink(uriInfo, "search", "POST", "search", "application/geo+json"));
        collectionReferenceService.getAllCollectionReferences(Optional.ofNullable(columnFilter), Optional.ofNullable(organisations)).forEach(c -> {
            if (!c.collectionName.equals("metacollection")) {
                links.add(getLink(uriInfo, "collections/" + c.collectionName, "child", MediaType.APPLICATION_JSON));
            }
        });

        LandingPage lp = new LandingPage();
        lp.setStacVersion(configuration.stacVersion);
        lp.setType(configuration.type);
        lp.setId(configuration.id);
        lp.setTitle(configuration.title);
        lp.setDescription(configuration.description);
        lp.setConformsTo(configuration.conformsTo);
        lp.setLinks(links);
        return cache(Response.ok(lp), 0);
    }

    @Timed
    @Path("/api")
    @GET
    @Produces("application/vnd.oai.openapi+json;version=3.0")
    @Operation(summary = "OpenAPI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OpenAPI specification",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getApi(@Context UriInfo uriInfo) {
        return Response.ok(openAPIjson).type("application/vnd.oai.openapi+json;version=3.0").build();
    }
}
