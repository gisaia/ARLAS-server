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

package io.arlas.server.rest.explore.search;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.core.app.Documentation;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.request.Search;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.model.response.Hits;
import io.arlas.server.core.ns.ATOM;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.CheckParams;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.core.utils.ParamsParser;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

public class SearchRESTService extends ExploreRESTServices {

    public SearchRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_search")
    @GET
    @Produces({UTF8JSON, ATOM.APPLICATION_ATOM_XML})
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Search", produces = UTF8JSON + "," + ATOM.APPLICATION_ATOM_XML, notes = Documentation.SEARCH_OPERATION, consumes = UTF8JSON, response = Hits.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hits.class, responseContainer = "ArlasHits"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response search(
            @Context UriInfo uriInfo,
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q",
                    value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "dateformat",
                    value = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(name = "righthand",
                    defaultValue = "true",
                    value = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @DefaultValue("false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat",
                    value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "include",
                    value = Documentation.PROJECTION_PARAM_INCLUDE,
                    allowMultiple = true,
                    defaultValue = "*")
            @QueryParam(value = "include") String include,

            @ApiParam(name = "exclude",
                    value = Documentation.PROJECTION_PARAM_EXCLUDE,
                    allowMultiple = true,
                    defaultValue = "")
            @QueryParam(value = "exclude") String exclude,

            @ApiParam(name = "returned_geometries",
                    value = Documentation.PROJECTION_PARAM_RETURNED_GEOMETRIES,
                    defaultValue = "")
            @QueryParam(value = "returned_geometries") String returned_geometries,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size", value = Documentation.PAGE_PARAM_SIZE,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer")
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @ApiParam(name = "from", value = Documentation.PAGE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    type = "integer")
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @ApiParam(name = "sort",
                    value = Documentation.PAGE_PARAM_SORT)
            @QueryParam(value = "sort") String sort,


            @ApiParam(name = "after",
                    value = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @ApiParam(name = "before",
                    value = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,


            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        CheckParams.checkReturnedGeometries(collectionReference, include, exclude, returned_geometries);

        Search search = new Search();
        search.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand);
        search.page = ParamsParser.getPage(size, from, sort, after, before);
        search.projection = ParamsParser.getProjection(include, exclude);
        search.returned_geometries = returned_geometries;
        exploreService.setValidGeoFilters(collectionReference, search);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        search.projection = ParamsParser.enrichIncludes(search.projection, returned_geometries);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        exploreService.setValidGeoFilters(collectionReference, searchHeader);
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        Hits hits = exploreService.search(request, collectionReference, Boolean.TRUE.equals(flat), uriInfo,"GET");
        return cache(Response.ok(hits), maxagecache);
    }

    @Timed
    @Path("{collection}/_search")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Search", produces = UTF8JSON, notes = Documentation.SEARCH_OPERATION, consumes = UTF8JSON, response = Hits.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hits.class, responseContainer = "ArlasHits"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response searchPost(
            @Context UriInfo uriInfo,
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- SEARCH -----------------------
            // --------------------------------------------------------
            Search search,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        String includes = search.projection != null ? search.projection.includes : null;
        String excludes = search.projection != null ? search.projection.excludes : null;

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);

        exploreService.setValidGeoFilters(collectionReference, search);
        exploreService.setValidGeoFilters(collectionReference, searchHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);
        CheckParams.checkReturnedGeometries(collectionReference, includes, excludes, search.returned_geometries);

        search.projection = ParamsParser.enrichIncludes(search.projection, search.returned_geometries);

        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        Hits hits = exploreService.search(request, collectionReference, (search.form != null && Boolean.TRUE.equals(search.form.flat)),uriInfo,"POST");
        return cache(Response.ok(hits), maxagecache);
    }
}
