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
import com.fasterxml.jackson.core.JsonProcessingException;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.rest.response.Error;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.GeoUtil;
import io.arlas.server.stac.model.SearchBody;
import io.arlas.server.stac.model.StacFeatureCollection;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class StacSearchRESTService extends StacRESTService {

    public StacSearchRESTService(STACConfiguration configuration,
                                 int arlasRestCacheTimeout,
                                 CollectionReferenceService collectionReferenceService,
                                 ExploreService exploreService, String baseUri) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService, baseUri);
    }

    @Timed
    @Path("/search")
    @GET
    @Produces({ "application/geo+json", MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Operation(
            summary = "Search STAC items with simple filtering.",
            description = "Retrieve Items matching filters. Intended as a shorthand API for simple queries.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A feature collection.",
                    content = @Content(schema = @Schema(implementation = StacFeatureCollection.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getItemSearch(@Context UriInfo uriInfo,

            @Parameter(name = "bbox",
                    description = """
                    Only features that have a geometry that intersects the bounding box are selected.
                    The bounding box is provided as four or six numbers, depending on whether the coordinate reference system includes a vertical axis (height or depth):
                      * Lower left corner, coordinate axis 1
                      * Lower left corner, coordinate axis 2
                      * Minimum value, coordinate axis 3 (optional)
                      * Upper right corner, coordinate axis 1
                      * Upper right corner, coordinate axis 2
                      * Maximum value, coordinate axis 3 (optional)

                    The coordinate reference system of the values is WGS 84 longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84).
                    For WGS 84 longitude/latitude the values are in most cases the sequence of minimum longitude, minimum latitude, maximum longitude and maximum latitude.
                    However, in cases where the box spans the antimeridian the first value (west-most box edge) is larger than the third value (east-most box edge).
                    If the vertical axis is included, the third and the sixth number are the bottom and the top of the 3-dimensional bounding box.
                    If a feature has multiple spatial geometry properties, it is the decision of the server whether only a single spatial geometry property is used to determine the extent or all relevant geometries.

                    Example: The bounding box of the New Zealand Exclusive Economic Zone in WGS 84 (from 160.6°E to 170°W and from 55.95°S to 25.89°S) would be represented in JSON as &#x60;[160.6, -55.95, -170, -25.89]&#x60; and in a query as &#x60;bbox&#x3D;160.6,-55.95,-170,-25.89&#x60;.""",
                    style = ParameterStyle.FORM,
                    explode = Explode.FALSE,
                    array = @ArraySchema(schema = @Schema(type="number"), minItems = 4, maxItems = 6))
            @QueryParam(value = "bbox") String bbox,

            @Parameter(name = "intersects", description = "The optional intersects parameter filters the result Items in the same way as bbox, " +
                    "only with a GeoJSON Geometry rather than a bbox.")
            @QueryParam(value = "intersects") String intersects,

            @Parameter(name = "datetime", description = """
                    Either a date-time or an interval, open or closed. Date and time expressions adhere to RFC 3339.
                    Open intervals are expressed using double-dots.  Examples:
                      * A date-time: "2018-02-12T23:20:50Z"
                      * A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
                      * Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"

                    Only features that have a temporal property that intersects the value of &#x60;datetime&#x60; are selected.
                    If a feature has multiple temporal properties, it is the decision of the server whether only a single temporal property is used to determine the extent or all relevant temporal properties.""",
                    style = ParameterStyle.FORM)
            @QueryParam(value = "datetime") String datetime,

            @Parameter(name = "limit",
                    description = """
                            The optional limit parameter limits the number of items that are presented in the response document.
                            Only items are counted that are on the first level of the collection in the response document.
                            Nested objects contained within the explicitly requested items shall not be counted.\s
                            Minimum &#x3D; 1. Maximum &#x3D; 10000. Default &#x3D; 10.""",
                    style = ParameterStyle.FORM,
                    schema = @Schema(type="integer", minimum = "1", maximum="10000", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "limit") IntParam limit,

            @Parameter(name = "ids", description = "Array of Item ids to return.")
            @QueryParam(value = "ids") List<String> ids,

            @Parameter(name = "collections", description = "Array of Collection IDs to include in the search for items. " +
                    "Only Item objects in one of the provided collections will be searched ")
            @QueryParam(value = "collections") List<String> collections,

//            @Parameter(name = "fields", description = "**Optional Extension:** Fields  Determines the shape of the features in the response")
//            @QueryParam(value = "fields") String fields,

//            @Parameter(name = "filter", required = true, description = "**Extension:** Filter  A CQL filter expression for filtering items.")
//            @QueryParam(value = "filter") Filter filter,

            @Parameter(name = "sortby", description = """
                    **Optional Extension:** Sort  An array of property names, prefixed by either "+" for ascending or "-" for descending. If no prefix is provided, "+" is assumed.""")
            @QueryParam(value = "sortby") String sortBy,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------
            @Parameter(name = "from", description = Documentation.PAGE_PARAM_FROM,
                    schema = @Schema(type="integer", minimum = "0", defaultValue = "0"))
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @Parameter(name = "after", description = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @Parameter(name = "before", description = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations

    ) throws ArlasException, JsonProcessingException {
        collections = collections.stream().flatMap(e -> Stream.of(e.split(","))).collect(Collectors.toList());

        SearchBody searchBody = new SearchBody().bbox(getBboxAsList(bbox))
                .collections(collections)
                .datetime(datetime)
                .ids(ids)
                .limit(limit.get())
                .sortBy(sortBy)
                .from(from.get())
                .after(after)
                .before(before);

        if (!StringUtil.isNullOrEmpty(intersects)) {
            searchBody.setIntersects(GeoUtil.geojsonReader.readValue(intersects));
        }

        return cache(Response.ok(getItems(partitionFilter, Optional.ofNullable(columnFilter), Optional.ofNullable(organisations), uriInfo, searchBody, "GET")), 0);

    }

    @Timed
    @Path("/search")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ "application/geo+json" })
    @Operation(
            summary = "Search STAC items with full-featured filtering.",
            description = "Retrieve items matching filters. Intended as the standard, full-featured query API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A feature collection.",
                    content = @Content(schema = @Schema(implementation = StacFeatureCollection.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response postItemSearch(@Context UriInfo uriInfo,
                                   @Valid SearchBody body,

                                   @Parameter(hidden = true)
                                   @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

                                   @Parameter(hidden = true)
                                   @HeaderParam(value = COLUMN_FILTER) String columnFilter,

                                   @Parameter(hidden = true)
                                   @HeaderParam(value = ARLAS_ORGANISATION) String organisations

    ) throws ArlasException {
        return cache(Response.ok(getItems(partitionFilter, Optional.ofNullable(columnFilter), Optional.ofNullable(organisations), uriInfo, body, "POST")), 0);
    }

    // -----------

    private StacFeatureCollection getItems(String partitionFilter, Optional<String> columnFilter, Optional<String> organisations, UriInfo uriInfo, SearchBody body, String method) throws ArlasException {
        // TODO search in more than the first collection given as parameter
        CollectionReference collectionReference = body.getCollections() == null || body.getCollections().isEmpty() ?
                collectionReferenceService.getAllCollectionReferences(columnFilter, organisations).get(0) :
                collectionReferenceService.getCollectionReference(body.getCollections().get(0), organisations);

        return getStacFeatureCollection(collectionReference, partitionFilter, columnFilter, body,
                getFilter(collectionReference, body), uriInfo, method, false);
    }

    private List<String> getFilter(CollectionReference collectionReference, SearchBody body) throws ArlasException {
        if ((body.getBbox() != null && !body.getBbox().isEmpty()) && body.getIntersects() != null) {
            throw new InvalidParameterException("Only one of either intersects or bbox should be specified.");
        }

        List<String> f = new ArrayList<>();
        String dateFilter = getDateFilter(body.getDatetime(), collectionReference);
        if (dateFilter != null) { f.add(dateFilter); }

        String geoFilter = getGeoFilter(body.getBbox(), collectionReference);
        if (geoFilter != null) { f.add(geoFilter); }

        String geojsonFilter = getGeoFilter(body.getIntersects(), collectionReference);
        if (geojsonFilter != null) { f.add(geojsonFilter); }

        String idFilter = getIdFilter(body.getIds(), collectionReference);
        if (idFilter != null) { f.add(idFilter); }

        return f;
    }
}
