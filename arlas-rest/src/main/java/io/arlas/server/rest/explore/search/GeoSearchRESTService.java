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
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.Expression;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.request.Search;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.*;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.geojson.FeatureCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class GeoSearchRESTService extends ExploreRESTServices {

    public GeoSearchRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_geosearch")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "GeoSearch",
            description = Documentation.GEOSEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response geosearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @Parameter(name = "f",
                    description = Documentation.FILTER_PARAM_F,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "f") List<String> f,

            @Parameter(name = "q", description = Documentation.FILTER_PARAM_Q,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "q") List<String> q,

            @Parameter(name = "dateformat",
                    description = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @Parameter(name = "righthand",
                    schema = @Schema(defaultValue = "true"),
                    description = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            @Parameter(name = "flat",
                    description = Documentation.FORM_FLAT,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "include",
                    description = Documentation.PROJECTION_PARAM_INCLUDE,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE,
                    schema = @Schema(defaultValue = "*"))
            @QueryParam(value = "include") String include,

            @Parameter(name = "exclude",
                    description = Documentation.PROJECTION_PARAM_EXCLUDE,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE,
                    schema = @Schema(defaultValue = ""))
            @QueryParam(value = "exclude") String exclude,

            @Parameter(name = "returned_geometries",
                    description = Documentation.PROJECTION_PARAM_RETURNED_GEOMETRIES,
                    schema = @Schema(defaultValue = ""))
            @QueryParam(value = "returned_geometries") String returned_geometries,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "size",
                    description = Documentation.PAGE_PARAM_SIZE,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @Parameter(name = "from", description = Documentation.PAGE_PARAM_FROM,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "0"))
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @Parameter(name = "sort",
                    description = Documentation.PAGE_PARAM_SORT,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "sort") String sort,

            @Parameter(name = "after",
                    description = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @Parameter(name = "before",
                    description = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {

        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geosearch(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand), partitionFilter, Optional.ofNullable(columnFilter),
                flat, include, exclude, size, from, sort, after, before, maxagecache, returned_geometries, false);
    }

    @Timed
    @Path("{collection}/_shapesearch")
    @GET
    @Produces(ZIPFILE)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "ShapeSearch",
            description = Documentation.SHAPESEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response shapesearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @Parameter(name = "f",
                    description = Documentation.FILTER_PARAM_F,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "f") List<String> f,

            @Parameter(name = "q", description = Documentation.FILTER_PARAM_Q,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "q") List<String> q,

            @Parameter(name = "dateformat",
                    description = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @Parameter(name = "righthand",
                    schema = @Schema(defaultValue = "true"),
                    description = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "include",
                    description = Documentation.PROJECTION_PARAM_INCLUDE,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE,
                    schema = @Schema(defaultValue = "*"))
            @QueryParam(value = "include") String include,

            @Parameter(name = "exclude",
                    description = Documentation.PROJECTION_PARAM_EXCLUDE,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE,
                    schema = @Schema(defaultValue = ""))
            @QueryParam(value = "exclude") String exclude,

            @Parameter(name = "returned_geometries",
                    description = Documentation.PROJECTION_PARAM_RETURNED_GEOMETRIES,
                    schema = @Schema(defaultValue = ""))
            @QueryParam(value = "returned_geometries") String returned_geometries,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "size",
                    description = Documentation.PAGE_PARAM_SIZE,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @Parameter(name = "from", description = Documentation.PAGE_PARAM_FROM,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "0"))
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @Parameter(name = "sort",
                    description = Documentation.PAGE_PARAM_SORT,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "sort") String sort,

            @Parameter(name = "after",
                    description = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @Parameter(name = "before",
                    description = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {

        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geosearch(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand), partitionFilter, Optional.ofNullable(columnFilter),
                true, include, exclude, size, from, sort, after, before, maxagecache, returned_geometries, true);
    }

    @Timed
    @Path("{collection}/_geosearch/{z}/{x}/{y}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Tiled GeoSearch",
            description = Documentation.TILED_GEOSEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response tiledgeosearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            @Parameter(name = "x",
                    description = "x",
                    required = true)
            @PathParam(value = "x") Integer x,
            @Parameter(name = "y",
                    description = "y",
                    required = true)
            @PathParam(value = "y") Integer y,
            @Parameter(name = "z",
                    description = "z",
                    required = true)
            @PathParam(value = "z") Integer z,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @Parameter(name = "f",
                    description = Documentation.FILTER_PARAM_F,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "f") List<String> f,

            @Parameter(name = "q",
                    description = Documentation.FILTER_PARAM_Q,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "q") List<String> q,

            @Parameter(name = "dateformat",
                    description = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @Parameter(name = "righthand",
                    schema = @Schema(defaultValue = "true"),
                    description = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            @Parameter(name = "flat",
                    description = Documentation.FORM_FLAT,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "include",
                    description = Documentation.PROJECTION_PARAM_INCLUDE,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE,
                    schema = @Schema(defaultValue = "*"))
            @QueryParam(value = "include") String include,

            @Parameter(name = "exclude", description = Documentation.PROJECTION_PARAM_EXCLUDE,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE,
                    schema = @Schema(defaultValue = ""))
            @QueryParam(value = "exclude") String exclude,

            @Parameter(name = "returned_geometries",
                    description = Documentation.PROJECTION_PARAM_RETURNED_GEOMETRIES,
                    schema = @Schema(defaultValue = ""))
            @QueryParam(value = "returned_geometries") String returned_geometries,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "size",
                    description = Documentation.PAGE_PARAM_SIZE,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @Parameter(name = "from",
                    description = Documentation.PAGE_PARAM_FROM,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "0"))
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @Parameter(name = "sort",
                    description = Documentation.PAGE_PARAM_SORT,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "sort") String sort,

            @Parameter(name = "after",
                    description = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @Parameter(name = "before",
                    description = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        BoundingBox bbox = GeoTileUtil.getBoundingBox(new Tile(x, y, z));
        // west, south, east, north

        Expression pwithinBbox = new Expression(collectionReference.params.centroidPath, OperatorEnum.within,
                bbox.getWest() + "," + bbox.getSouth() + "," + bbox.getEast() + "," + bbox.getNorth());

        return geosearch(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand, bbox, pwithinBbox),
                partitionFilter, Optional.ofNullable(columnFilter), flat, include, exclude, size, from, sort, after, before, maxagecache, returned_geometries, false);
    }

    @Timed
    @Path("{collection}/_geosearch")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "GeoSearch",
            description = Documentation.GEOSEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response geosearchPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  Search   ----------------------
            // --------------------------------------------------------
            Search search,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,
            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        String includes = search.projection != null ? search.projection.includes : null;
        String excludes = search.projection != null ? search.projection.excludes : null;
        CheckParams.checkReturnedGeometries(collectionReference, includes, excludes, search.returned_geometries);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        exploreService.setValidGeoFilters(collectionReference, search);
        exploreService.setValidGeoFilters(collectionReference, searchHeader);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, search);

        search.projection = ParamsParser.enrichIncludes(search.projection, search.returned_geometries);

        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        FeatureCollection fc = exploreService.getFeatures(request, collectionReference, (search.form != null && search.form.flat));
        return cache(Response.ok(fc), maxagecache);
    }

    @Timed
    @Path("{collection}/_shapesearch")
    @POST
    @Produces(ZIPFILE)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "ShapeSearch",
            description = Documentation.SHAPESEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response shapesearchPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  Search   -----------------------
            // --------------------------------------------------------
            Search search,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,
            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        String includes = search.projection != null ? search.projection.includes : null;
        String excludes = search.projection != null ? search.projection.excludes : null;
        CheckParams.checkReturnedGeometries(collectionReference, includes, excludes, search.returned_geometries);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);

        exploreService.setValidGeoFilters(collectionReference, search);
        exploreService.setValidGeoFilters(collectionReference, searchHeader);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, search);

        search.projection = ParamsParser.enrichIncludes(search.projection, search.returned_geometries);

        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        FeatureCollection fc = exploreService.getFeatures(request, collectionReference, true);
        File result = toShapefile(fc, collectionReference.params.collectionDisplayNames!=null?collectionReference.params.collectionDisplayNames.shapeColumns:null);
        try {
            return Response.ok(result)
                    .header("Content-Disposition",
                            "attachment; filename=" + result.getName()).build();
        } finally {
            try {
                FileUtils.forceDeleteOnExit(result);
            } catch (IOException ignored) {
            }
        }
    }

    private Response geosearch(CollectionReference collectionReference, Filter filter, String partitionFilter,
                               Optional<String> columnFilter, Boolean flat, String include, String exclude, IntParam size, IntParam from,
                               String sort, String after, String before, Integer maxagecache, String returned_geometries, boolean asShapeFile) throws ArlasException {

        CheckParams.checkReturnedGeometries(collectionReference, include, exclude, returned_geometries);

        Search search = new Search();
        search.filter = filter;
        search.page = ParamsParser.getPage(size, from, sort, after, before);
        search.projection = ParamsParser.getProjection(include, exclude);
        search.returned_geometries = returned_geometries;

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        search.projection = ParamsParser.enrichIncludes(search.projection, returned_geometries);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        exploreService.setValidGeoFilters(collectionReference, searchHeader);
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);
        FeatureCollection fc = exploreService.getFeatures(request, collectionReference, (flat != null && flat));
        if (asShapeFile) {
            File result = toShapefile(fc, collectionReference.params.collectionDisplayNames!=null?collectionReference.params.collectionDisplayNames.shapeColumns:null);
            try {
                return Response.ok(result)
                        .header("Content-Disposition",
                                "attachment; filename=" + result.getName()).build();
            } finally {
                try {
                    FileUtils.forceDeleteOnExit(result);
                } catch (IOException e) {
                    //ignored
                }
            }
        } else {
            return cache(Response.ok(fc), maxagecache);
        }
    }

}
