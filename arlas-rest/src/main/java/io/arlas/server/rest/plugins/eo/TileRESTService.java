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

package io.arlas.server.rest.plugins.eo;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import cyclops.control.Try;
import cyclops.data.tuple.Tuple2;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.RasterTileURL;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.request.Search;
import io.arlas.server.core.model.response.Error;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.*;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

public class TileRESTService extends ExploreRESTServices {
    public final static String PRODUCES_PNG =  "image/png";

    public TileRESTService(ExploreService exploreService) {
        super(exploreService);
    }


    @Timed
    @Path("{collection}/_tile/{z}/{x}/{y}.png")
    @GET
    @Produces({TileRESTService.PRODUCES_PNG})
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Tiled GeoSearch", produces = TileRESTService.PRODUCES_PNG, notes = Documentation.TILED_GEOSEARCH_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response tiledgeosearch(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            @ApiParam(name = "x",
                    value = "x",
                    required = true)
            @PathParam(value = "x") Integer x,
            @ApiParam(name = "y",
                    value = "y",
                    required = true)
            @PathParam(value = "y") Integer y,
            @ApiParam(name = "z",
                    value = "z",
                    required = true)
            @PathParam(value = "z") Integer z,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "dateformat",
                    value = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  PAGE    -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size",
                    value = Documentation.PAGE_PARAM_SIZE,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer")
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @ApiParam(name = "from",
                    value = Documentation.PAGE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    type = "integer")
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @ApiParam(name = "sort",
                    value = Documentation.PAGE_PARAM_SORT,
                    allowMultiple = true)
            @QueryParam(value = "sort") String sort,

            @ApiParam(name = "after",
                    value = Documentation.PAGE_PARAM_AFTER)
            @QueryParam(value = "after") String after,

            @ApiParam(name = "before",
                    value = Documentation.PAGE_PARAM_BEFORE)
            @QueryParam(value = "before") String before,

            // --------------------------------------------------------
            // -----------------------  RENDERING  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "sampling",
                    value = TileDocumentation.TILE_SAMPLING,
                    defaultValue = "10")
            @QueryParam(value = "sampling") Integer sampling,
            @ApiParam(name = "coverage",
                    value = TileDocumentation.TILE_COVERAGE,
                    defaultValue = "70")
            @QueryParam(value = "coverage") Integer coverage,

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
        if (collectionReference.params.geometryPath == null) {
            throw new BadRequestException("You cannot request a tiledgeosearch on a collection which has no geometry path defined.");
        }
        if (collectionReference.params.rasterTileURL == null) {
            throw new NotFoundException(collectionReference.collectionName+" has no URL defined for fetching the tiles.");
        }
        if (StringUtil.isNullOrEmpty(collectionReference.params.rasterTileURL.url)) {
            throw new NotFoundException(collectionReference.collectionName+" has no URL defined for fetching the tiles.");
        }
        if (z < collectionReference.params.rasterTileURL.minZ || z > collectionReference.params.rasterTileURL.maxZ) {
            LOGGER.info("Request level out of ["+collectionReference.params.rasterTileURL.minZ+"-"+collectionReference.params.rasterTileURL.maxZ+"]");
            return Response.noContent().build();
        }

        BoundingBox bbox = GeoTileUtil.getBoundingBox(new Tile(x, y, z));
        Search search = new Search();
        search.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat);
        search.page = ParamsParser.getPage(size, from, sort, after,before);
        search.projection = ParamsParser.getProjection(collectionReference.params.rasterTileURL.idPath+","+collectionReference.params.geometryPath, null);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        Queue<TileProvider<RasterTile>> providers = findCandidateTiles(collectionReference, request).stream()
                .filter(match -> match._2().map(
                        polygon -> (!collectionReference.params.rasterTileURL.checkGeometry) || polygon.intersects(GeoTileUtil.toPolygon(bbox))) // if geo is available, does it intersect the bbox?
                        .orElse(Boolean.TRUE)) // otherwise, let's keep that match, we'll see later if it paints something
                .map(match -> new URLBasedRasterTileProvider(new RasterTileURL(
                        collectionReference.params.rasterTileURL.url.replace("{id}", Optional.ofNullable(match._1()).orElse("")),
                        collectionReference.params.rasterTileURL.minZ,
                        collectionReference.params.rasterTileURL.maxZ,
                        collectionReference.params.rasterTileURL.checkGeometry),
                        collectionReference.params.rasterTileWidth,
                        collectionReference.params.rasterTileHeight)).collect(Collectors.toCollection(LinkedList::new));
        if(providers.size()==0){
            return Response.noContent().build();
        }
        Try<Optional<RasterTile>,ArlasException> stacked = new RasterTileStacker()
                .stack(providers)
                .sampling(Optional.ofNullable(sampling).orElse(10))
                .upTo(new RasterTileStacker.Percentage(Optional.ofNullable(coverage).orElse(10)))
                .on(new Tile(x, y, z));

        stacked.onFail(failure-> LOGGER.error("Failed to fetch a tile",failure));

        return stacked.map(otile->
                otile.map(tile->
                        Try.withCatch(()->{ // lets write the image to the response's output
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            ImageIO.write(tile.getImg(), "png", out);
                            return cache(Response.ok(out.toByteArray()), maxagecache);
                        },IOException.class)
                                .onFail(e -> Response.serverError().entity(e.getMessage()).build())
                                .orElse(Response.noContent().build())) // Can't write the tile => No content
                        .orElse(Response.noContent().build()))// No tile => No content
                .orElse(Response.noContent().build());// No tile => No content
    }

    protected List<Tuple2<String,Optional<Geometry>>> findCandidateTiles(CollectionReference collectionReference, MixedRequest request) throws ArlasException {
        return exploreService.searchAsRaw(request, collectionReference).stream()
                .map(hit -> Tuple2.of(
                        "" + MapExplorer.getObjectFromPath(collectionReference.params.rasterTileURL.idPath, hit), // Let's get the ID of the match
                        Try.withCatch(() -> GeoTileUtil.getGeometryFromSource(hit, collectionReference), // and its geometry: must be a polygon
                                ParseException.class, ClassCastException.class, JsonProcessingException.class) // there might be some troubles when parsing the geometry
                                .onFail(e ->LOGGER.error("Failed to fetch geometry for " + MapExplorer.getObjectFromPath(collectionReference.params.idPath, hit)))
                                .toOptional()// in case there's a problem, we don't need the geometry: the optimisation won't be applied on the hit => an empty Optional is good enough
                )).collect(Collectors.toList());
    }
}
