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
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.stac.model.LandingPage;
import io.arlas.server.stac.model.StacLink;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StacCoreRESTService extends StacRESTService {

    private final Client client;
    private final String baseUri;

    public StacCoreRESTService(STACConfiguration configuration,
                               int arlasRestCacheTimeout,
                               CollectionReferenceService collectionReferenceService,
                               ExploreService exploreService,
                               String baseUri) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService);
        this.client = ClientBuilder.newClient();
        this.baseUri = baseUri;
    }

    @Timed
    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Landing page",
            notes = "Returns the root STAC Catalog or STAC Collection that is the entry point for " +
                    "users to browse with STAC Browser or for search engines to crawl.\n" +
                    "This can either return a single STAC Collection or more commonly a STAC catalog.\n" +
                    "The landing page provides links to the API definition (link relations `service-desc` and `service-doc`) " +
                    "and the STAC records such as collections/catalogs (link relation `child`) or items (link relation `item`).\n" +
                    "Extensions may add additional links with new relation types.",
            response = LandingPage.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The landing page provides links to the API definition (link relations " +
                    "`service-desc` and `service-doc`) and the Feature Collection (path `/collections`, link relation " +
                    "`data`).", response = LandingPage.class, responseContainer = "LandingPage"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response getLandingPage(@Context UriInfo uriInfo) throws ArlasException {

        List<StacLink> links = new ArrayList<>();
        links.add(getSelfLink(uriInfo));
        links.add(getRootLink(uriInfo));
        links.add(getLink(uriInfo, "conformance", "conformance", MediaType.APPLICATION_JSON));
        links.add(getApiLink(uriInfo));
//        links.add(getLink(uriInfo, "api.html", "service-doc", MediaType.TEXT_HTML)); // TODO (recommended)
        links.add(getLink(uriInfo, "collections", "data", MediaType.APPLICATION_JSON));
        links.add(getLink(uriInfo, "search", "search", "application/geo+json"));
        links.add(getLink(uriInfo, "search", "POST", "search", "application/geo+json"));
        collectionReferenceService.getAllCollectionReferences(Optional.empty()).forEach(c -> {
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
    @ApiOperation(
            value = "OpenAPI",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OpenAPI specification", response = String.class, responseContainer = "OpenAPI")})
    public Response getApi(@Context UriInfo uriInfo) {
        return Response.ok(client
                .target(uriInfo.getBaseUriBuilder().uri(baseUri).path("openapi.json").build())
                .request()
                .get().getEntity()).type("application/vnd.oai.openapi+json;version=3.0").build();
    }
}
