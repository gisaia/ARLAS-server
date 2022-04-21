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
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.GeoUtil;
import io.arlas.server.core.utils.StringUtil;
import io.arlas.server.stac.model.SearchBody;
import io.arlas.server.stac.model.StacFeatureCollection;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StacSearchRESTService extends StacRESTService {

    public StacSearchRESTService(STACConfiguration configuration,
                                 int arlasRestCacheTimeout,
                                 CollectionReferenceService collectionReferenceService,
                                 ExploreService exploreService) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService);
    }

    @Timed
    @Path("/search")
    @GET
    @Produces({ "application/geo+json", MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Search STAC items with simple filtering.",
            notes = "Retrieve Items matching filters. Intended as a shorthand API for simple queries.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A feature collection.", response = StacFeatureCollection.class,
                    responseContainer = "StacFeatureCollection"),
            @ApiResponse(code = 400, message = "Invalid query parameter.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response getItemSearch(@Context UriInfo uriInfo,

            @Parameter(name = "bbox", style = ParameterStyle.FORM, explode = Explode.FALSE,
                    array = @ArraySchema(schema = @Schema(type="number"), maxItems = 6, minItems = 4))
            @ApiParam(name = "bbox", value = "Only features that have a geometry that intersects the bounding box are selected.\n" +
                    "The bounding box is provided as four or six numbers, depending on whether the coordinate reference system includes a vertical axis (height or depth):\n" +
                    "  * Lower left corner, coordinate axis 1\n" +
                    "  * Lower left corner, coordinate axis 2\n" +
                    "  * Minimum value, coordinate axis 3 (optional)\n" +
                    "  * Upper right corner, coordinate axis 1\n" +
                    "  * Upper right corner, coordinate axis 2\n" +
                    "  * Maximum value, coordinate axis 3 (optional)\n\n" +
                    "The coordinate reference system of the values is WGS 84 longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84).\n" +
                    "For WGS 84 longitude/latitude the values are in most cases the sequence of minimum longitude, minimum latitude, maximum longitude and maximum latitude.\n" +
                    "However, in cases where the box spans the antimeridian the first value (west-most box edge) is larger than the third value (east-most box edge).\n" +
                    "If the vertical axis is included, the third and the sixth number are the bottom and the top of the 3-dimensional bounding box.\n" +
                    "If a feature has multiple spatial geometry properties, it is the decision of the server whether only a single " +
                    "spatial geometry property is used to determine the extent or all relevant geometries.\n\n" +
                    "Example: The bounding box of the New Zealand Exclusive Economic Zone in WGS 84 (from 160.6°E to 170°W and from 55.95°S to 25.89°S) " +
                    "would be represented in JSON as &#x60;[160.6, -55.95, -170, -25.89]&#x60; and in a query as &#x60;bbox&#x3D;160.6,-55.95,-170,-25.89&#x60;.")
            @QueryParam(value = "bbox") String bbox,

            @ApiParam(name = "intersects", value = "The optional intersects parameter filters the result Items in the same way as bbox, " +
                    "only with a GeoJSON Geometry rather than a bbox.")
            @QueryParam(value = "intersects") String intersects,

            @Parameter(name = "datetime", style = ParameterStyle.FORM)
            @ApiParam(name = "datetime", value = "Either a date-time or an interval, open or closed. Date and time expressions adhere to RFC 3339.\n" +
                    "Open intervals are expressed using double-dots.  Examples:\n" +
                    "  * A date-time: \"2018-02-12T23:20:50Z\"\n" +
                    "  * A closed interval: \"2018-02-12T00:00:00Z/2018-03-18T12:31:12Z\"\n" +
                    "  * Open intervals: \"2018-02-12T00:00:00Z/..\" or \"../2018-03-18T12:31:12Z\"\n\n" +
                    "Only features that have a temporal property that intersects the value of &#x60;datetime&#x60; are selected.\n" +
                    "If a feature has multiple temporal properties, it is the decision of the server whether only a single temporal " +
                    "property is used to determine the extent or all relevant temporal properties.")
            @QueryParam(value = "datetime") String datetime,

            @Parameter(name = "limit", style = ParameterStyle.FORM, schema = @Schema(maximum="10000", defaultValue = "10", minimum = "1"))
            @ApiParam(name = "limit", defaultValue = "10", allowableValues = "range[1,10000]",
                    value = "The optional limit parameter limits the number of items that are presented in the response document.\n" +
                            "Only items are counted that are on the first level of the collection in the response document.\n" +
                            "Nested objects contained within the explicitly requested items shall not be counted. \n" +
                            "Minimum &#x3D; 1. Maximum &#x3D; 10000. Default &#x3D; 10.")
            @DefaultValue("10")
            @QueryParam(value = "limit") IntParam limit,

            @ApiParam(name = "ids", value = "Array of Item ids to return.")
            @QueryParam(value = "ids") List<String> ids,

            @ApiParam(name = "collections", value = "Array of Collection IDs to include in the search for items. " +
                    "Only Item objects in one of the provided collections will be searched ")
            @QueryParam(value = "collections") List<String> collections,

//            @ApiParam(name = "fields", value = "**Optional Extension:** Fields  Determines the shape of the features in the response")
//            @QueryParam(value = "fields") String fields,

//            @ApiParam(name = "filter", required = true, value = "**Extension:** Filter  A CQL filter expression for filtering items.")
//            @QueryParam(value = "filter") Filter filter,

            @ApiParam(name = "sortby", value = "**Optional Extension:** Sort  An array of property names, prefixed by either \"+\" for ascending " +
                    "or \"-\" for descending. If no prefix is provided, \"+\" is assumed.")
            @QueryParam(value = "sortby") String sortBy,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "from", value = Documentation.PAGE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    type = "integer")
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @ApiParam(name = "after", value = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @ApiParam(name = "before", value = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter

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

        return cache(Response.ok(getItems(partitionFilter, columnFilter, uriInfo, searchBody, "GET")), 0);

    }

    @Timed
    @Path("/search")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ "application/geo+json", MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Search STAC items with full-featured filtering.",
            notes = "Retrieve items matching filters. Intended as the standard, full-featured query API.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A feature collection.", response = StacFeatureCollection.class,
                    responseContainer = "StacFeatureCollection"),
            @ApiResponse(code = 400, message = "Invalid query parameter.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response postItemSearch(@Context UriInfo uriInfo,
                                   @Valid SearchBody body,

                                   @ApiParam(hidden = true)
                                   @HeaderParam(value = "partition-filter") String partitionFilter,

                                   @ApiParam(hidden = true)
                                   @HeaderParam(value = "Column-Filter") Optional<String> columnFilter
                                   ) throws ArlasException {
        return cache(Response.ok(getItems(partitionFilter, columnFilter, uriInfo, body, "POST")), 0);
    }

    // -----------

    private StacFeatureCollection getItems(String partitionFilter, Optional<String> columnFilter, UriInfo uriInfo, SearchBody body, String method) throws ArlasException {
        // TODO search in more than the first collection given as parameter
        CollectionReference collectionReference = body.getCollections() == null || body.getCollections().isEmpty() ?
                collectionReferenceService.getAllCollectionReferences(columnFilter).get(0) :
                collectionReferenceService.getCollectionReference(body.getCollections().get(0));

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
