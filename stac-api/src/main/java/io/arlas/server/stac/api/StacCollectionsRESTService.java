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
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.stac.model.*;
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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class StacCollectionsRESTService extends StacRESTService {

    public StacCollectionsRESTService(STACConfiguration configuration,
                                      int arlasRestCacheTimeout,
                                      CollectionReferenceService collectionReferenceService,
                                      ExploreService exploreService, String baseUri) {
        super(configuration, arlasRestCacheTimeout, collectionReferenceService, exploreService, baseUri);
    }

    @Timed
    @Path("/collections")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "The feature collections in the dataset",
            description = """
                    A body of Feature Collections that belong or are used together with additional links.
                    Request may not return the full set of metadata per Feature Collection.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                    The feature collections shared by this API.
                    The dataset is organized as one or more feature collections.
                    This resource provides information about and access to the collections.
                    The response contains the list of collections.
                    For each collection, a link to the items in the collection (path `/collections/{collectionId}/items`, link relation `items`) as well as key information about the collection.
                    This information includes:
                      * A local identifier for the collection that is unique for the dataset;
                      * A list of coordinate reference systems (CRS) in which geometries may be returned by the server. The first CRS is the default coordinate reference system (the default is always WGS 84 with axis order longitude/latitude);
                      * An optional title and description for the collection;
                      * An optional extent that can be used to provide an indication of the spatial and temporal extent of the collection - typically derived from the data;
                      * An optional indicator about the type of the items in the collection (the default value, if the indicator is not provided, is 'feature').""",
                    content = @Content(schema = @Schema(implementation = CollectionList.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getCollections(@Context UriInfo uriInfo,
                                   @Parameter(hidden = true)
                                   @HeaderParam(value = COLUMN_FILTER) String columnFilter,

                                   @Parameter(hidden = true)
                                   @HeaderParam(value = ARLAS_ORGANISATION) String organisations

                                   ) throws ArlasException {

        List<StacLink> links = new ArrayList<>(); // TODO what do we put in there?
        links.add(getSelfLink(uriInfo));
        links.add(getRootLink(uriInfo));

        List<Collection> collectionList = new ArrayList<>();
        for (CollectionReference c :
                collectionReferenceService.getAllCollectionReferences(Optional.ofNullable(columnFilter), Optional.ofNullable(organisations))
                        .stream()
                        .filter(c -> !c.collectionName.equals("metacollection"))
                        .toList()) {
            try {
                collectionList.add(getCollection(c, uriInfo));
            } catch (InvalidParameterException e) {
                // skipping collection with no geometry
            }
        }

        return cache(Response.ok(new CollectionList().collections(collectionList).links(links)), 0);
    }

    @Timed
    @Path("/collections/{collectionId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Describe the feature collection with id `collectionId`",
            description = "A single Feature Collection for the given id `collectionId`. Request this endpoint to get a full list of metadata for the Feature Collection.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                    Information about the feature collection with id `collectionId`.
                    The response contains a link to the items in the collection (path `/collections/{collectionId}/items`, link relation `items`) as well as key information about the collection.
                    This information includes:
                      * A local identifier for the collection that is unique for the dataset;
                      * A list of coordinate reference systems (CRS) in which geometries may be returned by the server. The first CRS is the default coordinate reference system (the default is always WGS 84 with axis order longitude/latitude);
                      * An optional title and description for the collection;
                      * An optional extent that can be used to provide an indication of the spatial and temporal extent of the collection - typically derived from the data;
                      * An optional indicator about the type of the items in the collection (the default value, if the indicator is not provided, is 'feature').""",
                    content = @Content(schema = @Schema(implementation = Collection.class))),
            @ApiResponse(responseCode = "404", description = "The requested URI was not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response describeCollection(@Context UriInfo uriInfo,
                                       @Parameter(name = "collectionId", description = "Local identifier of a collection", required = true)
                                       @PathParam(value = "collectionId") String collectionId,

                                       @Parameter(hidden = true)
                                       @HeaderParam(value = ARLAS_ORGANISATION) String organisations
    ) throws ArlasException {

        return cache(Response.ok(getCollection(collectionReferenceService.getCollectionReference(collectionId, Optional.ofNullable(organisations)), uriInfo)), 0);
    }

    @Timed
    @Path("/collections/{collectionId}/items")
    @GET
    @Produces({ "application/geo+json", MediaType.APPLICATION_JSON })
    @Operation(
            summary = "Fetch features",
            description = """
                    Fetch features of the feature collection with id `collectionId`.
                    Every feature in a dataset belongs to a collection. A dataset may consist of multiple feature collections.
                    A feature collection is often a collection of features of a similar type, based on a common schema.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                    The response is a document consisting of features in the collection.
                    The features included in the response are determined by the server based on the query parameters of the request.
                    To support access to larger collections without overloading the client, the API supports paged access with links to the next page, if more features are selected than the page size.
                    The `bbox` and `datetime` parameter can be used to select only a subset of the features in the collection (the features that are in the bounding box or time interval).
                    The `bbox` parameter matches all features in the collection that are not associated with a location, too.
                    The `datetime` parameter matches all features in the collection that are not associated with a time stamp or interval, too.
                    The `limit` parameter may be used to control the subset of the selected features that should be returned in the response, the page size.
                    Each page may include information about the number of selected and returned features (`numberMatched` and `numberReturned`) as well as links to support paging (link relation `next`).""",
                    content = @Content(schema = @Schema(implementation = StacFeatureCollection.class))),
            @ApiResponse(responseCode = "400", description = "A query parameter has an invalid value.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "The requested URI was not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getFeatures(@Context UriInfo uriInfo,
                                @Parameter(name = "collectionId", description = "Local identifier of a collection", required = true)
                                @PathParam(value = "collectionId") String collectionId,

                                @Parameter(name = "limit",
                                        description = """
                                        The optional limit parameter limits the number of items that are presented in the response document.
                                        Only items are counted that are on the first level of the collection in the response document.
                                        Nested objects contained within the explicitly requested items shall not be counted.
                                        Minimum &#x3D; 1. Maximum &#x3D; 10000. Default &#x3D; 10.""",
                                        style = ParameterStyle.FORM,
                                        schema = @Schema(type="integer", minimum = "1", maximum="10000", defaultValue = "10"))
                                @DefaultValue("10")
                                @QueryParam(value = "limit") IntParam limit,

                                @Parameter(name = "bbox",
                                        description = """
                                        Only features that have a geometry that intersects the bounding box are selected. The bounding box is provided as four or six numbers, depending on whether the coordinate reference system includes a vertical axis (height or depth):
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
                                        If a feature has multiple spatial geometry properties, it is the decision of the server whether only a single spatial geometry property is used to determine the extent or all relevant geometries.""",
                                        style = ParameterStyle.FORM,
                                        explode = Explode.FALSE,
                                        array = @ArraySchema(schema = @Schema(type="number"), minItems = 4, maxItems = 6))
                                @QueryParam(value = "bbox") String bbox,

                                @Parameter(name = "datetime",
                                        description = """
                                        Either a date-time or an interval, open or closed. Date and time expressions adhere to RFC 3339. Open intervals are expressed using double-dots.  Examples:
                                          * A date-time: "2018-02-12T23:20:50Z"
                                          * A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
                                          * Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"

                                        Only features that have a temporal property that intersects the value of &#x60;datetime&#x60; are selected.
                                        If a feature has multiple temporal properties, it is the decision of the server whether only a single temporal property is used to determine the extent or all relevant temporal properties.""",
                                        style = ParameterStyle.FORM)
                                @QueryParam(value = "datetime") String datetime,

                                // --------------------------------------------------------
                                // -----------------------  PAGE   -----------------------
                                // --------------------------------------------------------

                                @Parameter(name = "sortby", description = """
                                        **Optional Extension:** Sort  An array of property names, prefixed by either "+"
                                        for ascending or "-" for descending. If no prefix is provided, "+" is assumed.""")
                                @QueryParam(value = "sortby") String sortBy,

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
                                ) throws ArlasException {

        CollectionReference collectionReference = collectionReferenceService.getCollectionReference(collectionId, Optional.ofNullable(organisations));
        String dateFilter = getDateFilter(datetime, collectionReference);
        String geoFilter = getGeoFilter(getBboxAsList(bbox), collectionReference);

        List<String> f = new ArrayList<>();
        if (dateFilter != null) { f.add(dateFilter); }
        if (geoFilter != null) { f.add(geoFilter); }

        SearchBody searchBody = new SearchBody()
                .limit(limit.get())
                .from(from.get())
                .sortBy(sortBy)
                .after(after)
                .before(before);

        return cache(Response.ok(getStacFeatureCollection(collectionReference, partitionFilter, Optional.ofNullable(columnFilter),
                searchBody, f, uriInfo, "GET", true)), 0);
    }

    @Timed
    @Path("/collections/{collectionId}/items/{featureId}")
    @GET
    @Produces({ "application/geo+json", MediaType.APPLICATION_JSON })
    @Operation(
            summary = "Fetch a single feature",
            description = "Fetch the feature with id `featureId` in the feature collection with id `collectionId`.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch the feature with id `featureId` in the feature collection with id `collectionId`.",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "404", description = "The requested URI was not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response getFeature(@Context UriInfo uriInfo,
                               @Parameter(name = "collectionId", description = "Local identifier of a collection", required = true)
                               @PathParam(value = "collectionId") String collectionId,

                               @Parameter(name = "featureId", description = "Local identifier of a feature", required = true)
                               @PathParam(value = "featureId") String featureId,

                               @Parameter(hidden = true)
                               @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

                               @Parameter(hidden = true)
                               @HeaderParam(value = COLUMN_FILTER) String columnFilter,

                               @Parameter(hidden = true)
                               @HeaderParam(value = ARLAS_ORGANISATION) String organisations
    ) throws ArlasException {

        CollectionReference collectionReference = collectionReferenceService.getCollectionReference(collectionId, Optional.ofNullable(organisations));

        StacFeatureCollection features = getStacFeatureCollection(collectionReference, partitionFilter, Optional.ofNullable(columnFilter), null,
                java.util.Collections.singletonList(getIdFilter(featureId, collectionReference)),
                uriInfo, "GET", true);

        if (features.getFeatures().size() > 0) {
            Item response = features.getFeatures().get(0);
            return cache(Response.ok(response), 0);
        } else {
            throw new NotFoundException("Item not found");
        }
    }
}
