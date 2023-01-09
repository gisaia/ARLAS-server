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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
    @ApiOperation(
            value = "The feature collections in the dataset",
            notes = "A body of Feature Collections that belong or are used together with additional links.\n" +
                    "Request may not return the full set of metadata per Feature Collection.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = """
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
                    response = CollectionList.class, responseContainer = "CollectionList"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response getCollections(@Context UriInfo uriInfo,
                                   @ApiParam(hidden = true)
                                   @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

                                   @ApiParam(hidden = true)
                                   @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations

                                   ) throws ArlasException {

        List<StacLink> links = new ArrayList<>(); // TODO what do we put in there?
        links.add(getSelfLink(uriInfo));

        List<Collection> collectionList = new ArrayList<>();
        for (CollectionReference c :
                collectionReferenceService.getAllCollectionReferences(columnFilter, organisations)
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
    @ApiOperation(
            value = "Describe the feature collection with id `collectionId`",
            notes = "A single Feature Collection for the given id `collectionId`. Request this endpoint to get a full list of metadata for the Feature Collection.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = """
                    Information about the feature collection with id `collectionId`.
                    The response contains a link to the items in the collection (path `/collections/{collectionId}/items`, link relation `items`) as well as key information about the collection.
                    This information includes:
                      * A local identifier for the collection that is unique for the dataset;
                      * A list of coordinate reference systems (CRS) in which geometries may be returned by the server. The first CRS is the default coordinate reference system (the default is always WGS 84 with axis order longitude/latitude);
                      * An optional title and description for the collection;
                      * An optional extent that can be used to provide an indication of the spatial and temporal extent of the collection - typically derived from the data;
                      * An optional indicator about the type of the items in the collection (the default value, if the indicator is not provided, is 'feature').""",
                    response = Collection.class, responseContainer = "Collection"),
            @ApiResponse(code = 404, message = "The requested URI was not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response describeCollection(@Context UriInfo uriInfo,
                                       @ApiParam(name = "collectionId", value = "Local identifier of a collection", required = true)
                                       @PathParam(value = "collectionId") String collectionId,

                                       @ApiParam(hidden = true)
                                       @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations
    ) throws ArlasException {

        return cache(Response.ok(getCollection(collectionReferenceService.getCollectionReference(collectionId, organisations), uriInfo)), 0);
    }

    @Timed
    @Path("/collections/{collectionId}/items")
    @GET
    @Produces({ "application/geo+json", MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Fetch features",
            notes = """
                    Fetch features of the feature collection with id `collectionId`.
                    Every feature in a dataset belongs to a collection. A dataset may consist of multiple feature collections.
                    A feature collection is often a collection of features of a similar type, based on a common schema.""")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = """
                    The response is a document consisting of features in the collection.
                    The features included in the response are determined by the server based on the query parameters of the request.
                    To support access to larger collections without overloading the client, the API supports paged access with links to the next page, if more features are selected than the page size.
                    The `bbox` and `datetime` parameter can be used to select only a subset of the features in the collection (the features that are in the bounding box or time interval).
                    The `bbox` parameter matches all features in the collection that are not associated with a location, too.
                    The `datetime` parameter matches all features in the collection that are not associated with a time stamp or interval, too.
                    The `limit` parameter may be used to control the subset of the selected features that should be returned in the response, the page size.
                    Each page may include information about the number of selected and returned features (`numberMatched` and `numberReturned`) as well as links to support paging (link relation `next`).""",
                    response = StacFeatureCollection.class, responseContainer = "StacFeatureCollection"),
            @ApiResponse(code = 400, message = "A query parameter has an invalid value.", response = Error.class),
            @ApiResponse(code = 404, message = "The requested URI was not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response getFeatures(@Context UriInfo uriInfo,
                                @ApiParam(name = "collectionId", value = "Local identifier of a collection", required = true)
                                @PathParam(value = "collectionId") String collectionId,

                                @Parameter(name = "limit", style = ParameterStyle.FORM,
                                        schema = @Schema(maximum="10000", defaultValue = "10", minimum = "1", type="integer"))
                                @ApiParam(name = "limit", defaultValue = "10", allowableValues = "range[1,10000]", value = """
                                        The optional limit parameter limits the number of items that are presented in the response document.
                                        Only items are counted that are on the first level of the collection in the response document.
                                        Nested objects contained within the explicitly requested items shall not be counted.
                                        Minimum &#x3D; 1. Maximum &#x3D; 10000. Default &#x3D; 10.""")
                                @DefaultValue("10")
                                @QueryParam(value = "limit") IntParam limit,

                                @Parameter(name = "bbox", style = ParameterStyle.FORM, explode = Explode.FALSE,
                                        array = @ArraySchema(schema = @Schema(type="number"), maxItems = 6, minItems = 4))
                                @ApiParam(name = "bbox", value = """
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
                                        If a feature has multiple spatial geometry properties, it is the decision of the server whether only a single spatial geometry property is used to determine the extent or all relevant geometries.""")
                                @QueryParam(value = "bbox") String bbox,

                                @Parameter(name = "datetime", style = ParameterStyle.FORM)
                                @ApiParam(name = "datetime", value = """
                                        Either a date-time or an interval, open or closed. Date and time expressions adhere to RFC 3339. Open intervals are expressed using double-dots.  Examples:
                                          * A date-time: "2018-02-12T23:20:50Z"
                                          * A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
                                          * Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"

                                        Only features that have a temporal property that intersects the value of &#x60;datetime&#x60; are selected.
                                        If a feature has multiple temporal properties, it is the decision of the server whether only a single temporal property is used to determine the extent or all relevant temporal properties.""")
                                @QueryParam(value = "datetime") String datetime,

                                // --------------------------------------------------------
                                // -----------------------  PAGE   -----------------------
                                // --------------------------------------------------------

                                @ApiParam(name = "sortby", value = "**Optional Extension:** Sort  An array of property names, prefixed by either \"+\" for ascending " +
                                        "or \"-\" for descending. If no prefix is provided, \"+\" is assumed.")
                                @QueryParam(value = "sortby") String sortBy,

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
                                @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

                                @ApiParam(hidden = true)
                                @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

                                @ApiParam(hidden = true)
                                @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations
                                ) throws ArlasException {

        CollectionReference collectionReference = collectionReferenceService.getCollectionReference(collectionId, organisations);
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

        return cache(Response.ok(getStacFeatureCollection(collectionReference, partitionFilter, columnFilter,
                searchBody, f, uriInfo, "GET", true)), 0);
    }

    @Timed
    @Path("/collections/{collectionId}/items/{featureId}")
    @GET
    @Produces({ "application/geo+json", MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Fetch a single feature",
            notes = "Fetch the feature with id `featureId` in the feature collection with id `collectionId`.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Fetch the feature with id `featureId` in the feature collection with id `collectionId`.",
                    response = Item.class, responseContainer = "Item"),
            @ApiResponse(code = 404, message = "The requested URI was not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response getFeature(@Context UriInfo uriInfo,
                               @ApiParam(name = "collectionId", value = "Local identifier of a collection", required = true)
                               @PathParam(value = "collectionId") String collectionId,

                               @ApiParam(name = "featureId", value = "Local identifier of a feature", required = true)
                               @PathParam(value = "featureId") String featureId,

                               @ApiParam(hidden = true)
                                   @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

                               @ApiParam(hidden = true)
                               @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

                               @ApiParam(hidden = true)
                               @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations

    ) throws ArlasException {

        CollectionReference collectionReference = collectionReferenceService.getCollectionReference(collectionId, organisations);

        StacFeatureCollection features = getStacFeatureCollection(collectionReference, partitionFilter, columnFilter, null,
                java.util.Collections.singletonList(getIdFilter(featureId, collectionReference)),
                uriInfo, "GET", true);

        if (features.getFeatures().size() == 1) {
            Item response = getItem(features.getFeatures().get(0), collectionReference, uriInfo);
            return cache(Response.ok(response), 0);
        } else {
            throw new NotFoundException("Item not found");
        }
    }
}
