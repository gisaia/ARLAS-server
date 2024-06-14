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
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.RasterTileURL;
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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import static io.arlas.commons.rest.utils.ServerConstants.*;

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
    @Operation(
            summary = "Tiled GeoSearch",
            description = Documentation.TILED_GEOSEARCH_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
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

            @Parameter(name = "q", description = Documentation.FILTER_PARAM_Q,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "q") List<String> q,

            @Parameter(name = "dateformat",
                    description = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @Parameter(name = "righthand",
                    description = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // -----------------------  PAGE    -----------------------
            // --------------------------------------------------------

            @Parameter(name = "size",
                    description = Documentation.PAGE_PARAM_SIZE,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @Parameter(name = "from",
                    description = Documentation.PAGE_PARAM_FROM,
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "00"))
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
            // -----------------------  RENDERING  -----------------------
            // --------------------------------------------------------
            @Parameter(name = "sampling",
                    description = TileDocumentation.TILE_SAMPLING,
                    schema = @Schema(defaultValue = "10"))
            @QueryParam(value = "sampling") Integer sampling,
            @Parameter(name = "coverage",
                    description = TileDocumentation.TILE_COVERAGE,
                    schema = @Schema(defaultValue = "70"))
            @QueryParam(value = "coverage") Integer coverage,

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
        search.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand);
        search.page = ParamsParser.getPage(size, from, sort, after,before);
        search.projection = ParamsParser.getProjection(collectionReference.params.rasterTileURL.idPath+","+collectionReference.params.geometryPath, null);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, search);

        Search searchHeader = new Search();
        searchHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

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
        if (providers.isEmpty()){
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
