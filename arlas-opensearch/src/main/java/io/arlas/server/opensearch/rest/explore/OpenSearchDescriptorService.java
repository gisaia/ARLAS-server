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

package io.arlas.server.opensearch.rest.explore;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.OpenSearch;
import io.arlas.server.core.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.core.model.response.FieldType;
import io.arlas.server.core.ns.ATOM;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.opensearch.rest.explore.model.Image;
import io.arlas.server.opensearch.rest.explore.model.OpenSearchDescription;
import io.arlas.server.opensearch.rest.explore.model.Url;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.io.IOException;
import java.util.*;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

public class OpenSearchDescriptorService extends ExploreRESTServices {

    public OpenSearchDescriptorService(ExploreService exploreService) {
        super(exploreService);
    }

    public static final String MIME_TYPE_XML = "application/xml";

    @Timed
    @Path("/ogc/opensearch/{collection}")
    @GET
    @Produces({MIME_TYPE_XML})
    @Operation(
            summary = "OpenSearch Description Document",
            description = Documentation.OPENSEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response opensearch(@Context UriInfo uri,
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(
                    name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- FILTERS- -----------------------
            // --------------------------------------------------------
            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws IOException, NotFoundException, ArlasException {
        CollectionReference cr = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (cr == null) {
            throw new NotFoundException(collection);
        }
        ColumnFilterUtil.assertCollectionsAllowed(Optional.ofNullable(columnFilter), Collections.singletonList(cr));
        OpenSearchDescription description = new OpenSearchDescription();
        String prefix = uri.getBaseUri().toURL() + uri.getPath() + "/../_search";

        //[scheme:][//authority][path][?query][#fragment]
        if (cr.params.openSearch != null) {
            OpenSearch os = cr.params.openSearch;
            prefix = exploreService.getBaseUri() + this.getExplorePathUri() + collection + "/_search";
            description.adultContent = os.adultContent;
            description.attribution = os.attribution;
            description.contact = os.contact;
            description.description = os.description;
            description.developer = os.developer;
            if (!StringUtil.isNullOrEmpty(os.imageUrl)) {
                description.image = new Image();
                description.image.content = os.imageUrl;
                description.image.height = os.imageHeight;
                description.image.width = os.imageWidth;
                description.image.type = os.imageType;
            }
            description.inputEncoding = os.inputEncoding;
            description.language = os.language;
            description.longName = os.longName;
            description.outputEncoding = os.outputEncoding;
            description.shortName = os.shortName;
            description.syndicationRight = os.syndicationRight;
            description.tags = os.tags;
        }
        addURLs(prefix, description.url, exploreService.describeCollection(cr, Optional.ofNullable(columnFilter)).properties, new Stack<>());
        List<Url> urls = new ArrayList<>();
        description.url.forEach(url -> {
            urls.add(url(url.template + "&f="+cr.params.geometryPath+":intersect:{geo:box?}"));
            urls.add(url(url.template + "&f="+cr.params.geometryPath+":intersect:{geo:geometry?}"));
        });
        description.url = urls;
        return cache(Response.ok(description), maxagecache);
    }

    private void addURLs(String templatePrefix, List<Url> urls, Map<String, CollectionReferenceDescriptionProperty> properties, Stack<String> namespace) {
        if (properties == null) {
            return;
        }
        for (String key : properties.keySet()) {
            CollectionReferenceDescriptionProperty property = properties.get(key);
            namespace.push(key);
            if (property.type == FieldType.OBJECT) {
                addURLs(templatePrefix, urls, property.properties, namespace);
            } else {
                String fieldPath = String.join(".", new ArrayList<>(namespace));
                if (property.indexed) {
                    switch (property.type) {
                        case DATE, LONG -> addNumberUrls(urls, templatePrefix, fieldPath, "long");
                        case DOUBLE -> addNumberUrls(urls, templatePrefix, fieldPath, "double");
                        case FLOAT -> addNumberUrls(urls, templatePrefix, fieldPath, "float");
                        case SHORT -> addNumberUrls(urls, templatePrefix, fieldPath, "short");
                        case INTEGER -> addNumberUrls(urls, templatePrefix, fieldPath, "integer");
                        case TEXT -> {
                            urls.add(url(templatePrefix + "?f=" + fieldPath + ":eq:{text?}"));
                            urls.add(url(templatePrefix + "?f=" + fieldPath + ":like:{text?}"));
                        }
                        case KEYWORD -> urls.add(url(templatePrefix + "?f=" + fieldPath + ":eq:{text?}"));
                    }
                }

            }
            namespace.pop();
        }
    }

    private void addNumberUrls(List<Url> urls, String templatePrefix, String fieldPath, String type) {
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":eq:{" + type + "?}"));
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":ne:{" + type + "?}"));
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":gte:{" + type + "?}"));
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":gt:{" + type + "?}"));
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":lt:{" + type + "?}"));
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":lte:{" + type + "?}"));
        urls.add(url(templatePrefix + "?f=" + fieldPath + ":range:{" + type + "?},{" + type + "?}"));
    }

    private Url url(String path) {
        Url url = new Url();
        url.type = ATOM.APPLICATION_ATOM_XML;
        url.template = path;
        return url;
    }
}
