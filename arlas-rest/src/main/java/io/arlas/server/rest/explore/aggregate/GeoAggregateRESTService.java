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

package io.arlas.server.rest.explore.aggregate;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.AggregationGeometryEnum;
import io.arlas.server.core.model.enumerations.AggregationTypeEnum;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.AggregationResponse;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.*;
import io.arlas.server.rest.explore.ExploreRESTServices;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class GeoAggregateRESTService extends ExploreRESTServices {

    private static final double GEOHASH_EPSILON = 0.00000001;
    public GeoAggregateRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    private static final String FEATURE_TYPE_KEY = "feature_type";
    private static final String FEATURE_TYPE_VALUE = "aggregation";
    private static final String GEOMETRY_REFERENCE = "geometry_ref";
    private static final String GEOMETRY_TYPE = "geometry_type";
    private static final String GEOMETRY_SORT = "geometry_sort";

    @Timed
    @Path("{collection}/_geoaggregate")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "GeoAggregate",
            description = Documentation.GEOAGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "501", description = "Not implemented functionality.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response geoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @Parameter(name = "agg",
                    description = Documentation.GEOAGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
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
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            @Parameter(name = "flat", description = Documentation.FORM_FLAT,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geoaggregate(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand),
                partitionFilter, Optional.ofNullable(columnFilter), flat, agg, maxagecache, Optional.empty(), false);

    }

    @Timed
    @Path("{collection}/_shapeaggregate")
    @GET
    @Produces(ZIPFILE)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "ShapeAggregate",
            description = Documentation.SHAPEAGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "501", description = "Not implemented functionality.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response shapeaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @Parameter(name = "agg",
                    description = Documentation.GEOAGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
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
                    description = Documentation.FILTER_RIGHT_HAND,
                    schema = @Schema(defaultValue = "true"))
            @QueryParam(value = "righthand") Boolean righthand,


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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geoaggregate(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand),
                partitionFilter, Optional.ofNullable(columnFilter), true, agg, maxagecache, Optional.empty(), true);

    }

    @Timed
    @Path("{collection}/_geoaggregate/{geohash}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "GeoAggregate on a geohash",
            description = Documentation.GEOHASH_GEOAGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "501", description = "Not implemented functionality.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response geohashgeoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH ---------------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "geohash",
                    description = "geohash",
                    required = true)
            @PathParam(value = "geohash") String geohash,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION --------------------
            // --------------------------------------------------------
            @Parameter(name = "agg",
                    description = Documentation.GEOAGGREGATION_PARAM_AGG)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -------------------------
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
            // ----------------------- FORM ---------------------------
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
            // ----------------------- EXTRA --------------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        if (geohash.startsWith("#")) {
            geohash = geohash.substring(1);
        }
        if (agg == null || agg.isEmpty()) {
            agg = Collections.singletonList("geohash:" + collectionReference.params.centroidPath + ":interval-" + geohash.length());
        }

        List<BoundingBox> bboxes = getBoundingBoxes(geohash, agg, collectionReference);
        List<CompletableFuture<AggregationResponse>> futureList = new ArrayList<>();
        AggregationTypeEnum aggType = null;
        for (BoundingBox b : bboxes) {
            Expression pwithinBbox = new Expression(collectionReference.params.centroidPath, OperatorEnum.within,
                    b.getWest() + "," + b.getSouth() + ","
                            + String.format(Locale.ROOT, "%.8f", b.getEast() - GEOHASH_EPSILON) + ","
                            + String.format(Locale.ROOT,"%.8f", b.getNorth() - GEOHASH_EPSILON));
            MixedRequest request = getGeoaggregateRequest(collectionReference,
                    ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand, b, pwithinBbox)
                    , partitionFilter, Optional.ofNullable(columnFilter), agg);
            aggType = ((AggregationsRequest) request.basicRequest).aggregations.get(0).type;

            futureList.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return exploreService.aggregate(request,collectionReference, true,
                            ((AggregationsRequest) request.basicRequest).aggregations,0,
                            System.nanoTime());
                } catch (ArlasException e) {
                    throw new RuntimeException(e);
                }
            }));

        }

        List<AggregationResponse> aggResponses = futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        return cache(Response.ok(toGeoJson(merge(aggResponses), aggType, Boolean.TRUE.equals(flat), Optional.of(geohash))), maxagecache);

    }

    @Timed
    @Path("{collection}/_geoaggregate/{z}/{x}/{y}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "GeoAggregate on a geotile",
            description = Documentation.GEOTILE_GEOAGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "501", description = "Not implemented functionality.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response geotilegeoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH ---------------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "z",
                    description = "z",
                    required = true)
            @PathParam(value = "z") Integer z,
            @Parameter(name = "x",
                    description = "x",
                    required = true)
            @PathParam(value = "x") Integer x,
            @Parameter(name = "y",
                    description = "y",
                    required = true)
            @PathParam(value = "y") Integer y,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION --------------------
            // --------------------------------------------------------
            @Parameter(name = "agg",
                    description = Documentation.GEOAGGREGATION_PARAM_AGG)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -------------------------
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
            // ----------------------- FORM ---------------------------
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
            // ----------------------- EXTRA --------------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        if (agg == null || agg.isEmpty()) {
            agg = Collections.singletonList("geotile:" + collectionReference.params.centroidPath + ":interval-" + (z+3));
        }

        List<BoundingBox> bboxes = getBoundingBoxes(z, x, y, agg, collectionReference);
        List<CompletableFuture<AggregationResponse>> futureList = new ArrayList<>();
        AggregationTypeEnum aggType = null;
        for (BoundingBox b : bboxes) {
            Expression pwithinBbox = new Expression(collectionReference.params.centroidPath, OperatorEnum.within,
                    b.getWest() + "," + b.getSouth() + ","
                            + String.format(Locale.ROOT, "%.8f", b.getEast() - GEOHASH_EPSILON) + ","
                            + String.format(Locale.ROOT, "%.8f", b.getNorth() - GEOHASH_EPSILON));
            MixedRequest request = getGeoaggregateRequest(collectionReference,
                    ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand, b, pwithinBbox)
                    , partitionFilter, Optional.ofNullable(columnFilter), agg);
            aggType = ((AggregationsRequest) request.basicRequest).aggregations.get(0).type;

            futureList.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return exploreService.aggregate(request,collectionReference, true,
                            ((AggregationsRequest) request.basicRequest).aggregations,0,
                            System.nanoTime());
                } catch (ArlasException e) {
                    throw new RuntimeException(e);
                }
            }));

        }

        List<AggregationResponse> aggResponses = futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        return cache(Response.ok(toGeoJson(merge(aggResponses), aggType, Boolean.TRUE.equals(flat), Optional.of(z + "/" + x + "/" + y))), maxagecache);

    }

    private List<BoundingBox> getBoundingBoxes(String geohash, List<String> agg, CollectionReference collectionReference) throws ArlasException {
        // we expect a 'geohash' aggregation model with an interval specified
        int interval = 0;
        List<Interval> intervals = ParamsParser.getAggregations(collectionReference, agg).stream()
                .filter(a -> a.type.equals(AggregationTypeEnum.geohash))
                .map(a -> a.interval)
                .toList();
        if (!intervals.isEmpty()) {
            interval = intervals.get(0).value.intValue();
        }
        BoundingBox bbox = GeoTileUtil.getBoundingBox(geohash);
        if (interval - geohash.length() > 2) {
            LOGGER.debug("interval - geohash > 2");
            // Split initial bbox in 4
            double midLat = (bbox.getNorth() + bbox.getSouth()) / 2;
            double midLon = (bbox.getWest() + bbox.getEast()) / 2;
            return Arrays.asList(
                    new BoundingBox(bbox.getNorth(), midLat, bbox.getWest(), midLon),
                    new BoundingBox(bbox.getNorth(), midLat, midLon, bbox.getEast()),
                    new BoundingBox(midLat, bbox.getSouth(), bbox.getWest(), midLon),
                    new BoundingBox(midLat, bbox.getSouth(), midLon, bbox.getEast()));
        } else {
            LOGGER.debug("interval - geohash <= 2");
            return List.of(bbox);
        }
    }

    private List<BoundingBox> getBoundingBoxes(Integer z, Integer x, Integer y, List<String> agg, CollectionReference collectionReference) throws ArlasException {
        // we expect a 'geotile' aggregation model with an interval specified
        int interval = 0;
        List<Interval> intervals = ParamsParser.getAggregations(collectionReference, agg).stream()
                .filter(a -> a.type.equals(AggregationTypeEnum.geotile))
                .map(a -> a.interval)
                .toList();
        if (!intervals.isEmpty()) {
            interval = intervals.get(0).value.intValue();
            if (interval - z > 7 || interval - z < 0) {
                throw new InvalidParameterException("(interval - z) must be > 0 and <= 7");
            }
        }

        BoundingBox bbox = GeoTileUtil.getBoundingBox(new Tile(x, y, z));
        if (interval - z  == 7) {
            LOGGER.debug("interval - z == 7");
            // Split initial bbox in 4
            double midLat = (bbox.getNorth() + bbox.getSouth()) / 2;
            double midLon = (bbox.getWest() + bbox.getEast()) / 2;
            return Arrays.asList(
                    new BoundingBox(bbox.getNorth(), midLat, bbox.getWest(), midLon),
                    new BoundingBox(bbox.getNorth(), midLat, midLon, bbox.getEast()),
                    new BoundingBox(midLat, bbox.getSouth(), bbox.getWest(), midLon),
                    new BoundingBox(midLat, bbox.getSouth(), midLon, bbox.getEast()));
        } else {
            LOGGER.debug("interval - z < 7");
            return List.of(bbox);
        }
    }

    private AggregationResponse merge(List<AggregationResponse> aggResponses) {
        AggregationResponse result = new AggregationResponse();
        if (aggResponses.size() > 1) {
            result.name = aggResponses.get(0).name;
            result.totalnb = aggResponses.stream().filter(r -> r.totalnb != null).mapToLong(r -> r.totalnb).sum();
            result.elements = aggResponses.stream().map(r -> r.elements).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
            result.metrics = aggResponses.stream().map(r -> r.metrics).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
            result.hits = aggResponses.stream().map(r -> r.hits).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
            result.geometries = aggResponses.stream().map(r -> r.geometries).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
        } else {
            return aggResponses.get(0);
        }
        return result;
    }

    @Timed
    @Path("{collection}/_geoaggregate")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "GeoAggregate",
            description = Documentation.GEOAGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "501", description = "Not implemented functionality.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response geoaggregatePost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            AggregationsRequest aggregationRequest,

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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, aggregationRequest);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, aggregationRequest);

        request.basicRequest = aggregationRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        FeatureCollection fc = getFeatureCollection(request, collectionReference, (aggregationRequest.form != null && aggregationRequest.form.flat), Optional.empty());

        return cache(Response.ok(fc), maxagecache);
    }


    @Timed
    @Path("{collection}/_shapeaggregate")
    @POST
    @Produces(ZIPFILE)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "ShapeAggregate",
            description = Documentation.SHAPEAGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "501", description = "Not implemented functionality.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response shapeaggregatePost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            AggregationsRequest aggregationRequest,

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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, aggregationRequest);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, aggregationRequest);

        request.basicRequest = aggregationRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        FeatureCollection fc = getFeatureCollection(request, collectionReference, true, Optional.empty());
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

    private MixedRequest getGeoaggregateRequest(CollectionReference collectionReference, Filter filter,
                                                String partitionFilter, Optional<String> columnFilter,
                                                List<String> agg) throws ArlasException {
        AggregationsRequest aggregationsRequest = new AggregationsRequest();
        aggregationsRequest.filter = filter;
        aggregationsRequest.aggregations = ParamsParser.getAggregations(collectionReference, agg);
        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationsRequest);

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);
        return request;
    }

    private Response geoaggregate(CollectionReference collectionReference, Filter filter, String partitionFilter, Optional<String> columnFilter,
                                  Boolean flat, List<String> agg, Integer maxagecache, Optional<String> geohash, boolean asShapeFile) throws ArlasException {

        MixedRequest request = getGeoaggregateRequest(collectionReference, filter, partitionFilter, columnFilter, agg);
        FeatureCollection fc = getFeatureCollection(request, collectionReference, Boolean.TRUE.equals(flat), geohash);
        if (asShapeFile) {
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
        } else {
            return cache(Response.ok(fc), maxagecache);
        }
    }

    private FeatureCollection getFeatureCollection(MixedRequest request, CollectionReference collectionReference, boolean flat, Optional<String> geohash) throws ArlasException {
        FeatureCollection fc;
        AggregationTypeEnum mainAggregationType = ((AggregationsRequest) request.basicRequest).aggregations.get(0).type;
        AggregationResponse aggregationResponse = exploreService.aggregate(request,
                collectionReference,
                true,
                ((AggregationsRequest) request.basicRequest).aggregations,
                0,
                System.nanoTime());
        fc = toGeoJson(aggregationResponse, mainAggregationType, flat, geohash);
        return fc;
    }

    private FeatureCollection toGeoJson(AggregationResponse aggregationResponse, AggregationTypeEnum mainAggregationType, boolean flat, Optional<String> tile) {
        FeatureCollection fc = new FeatureCollection();
        List<AggregationResponse> elements = aggregationResponse.elements;
        if (!CollectionUtils.isEmpty(elements)) {
            for (AggregationResponse element : elements) {
               if (!CollectionUtils.isEmpty(element.geometries)) {
                   element.geometries.forEach(g -> {
                       Feature feature = new Feature();
                       Map<String, Object> properties = new HashMap<>();
                       properties.put("count", element.count);
                       if (mainAggregationType == AggregationTypeEnum.geohash) {
                           properties.put("geohash", element.keyAsString);
                           tile.ifPresent(s -> properties.put("parent_geohash", s));
                       } else if (mainAggregationType == AggregationTypeEnum.geohex) {
                           properties.put("geohex", element.keyAsString);
                           tile.ifPresent(s -> properties.put("parent_cell", s));
                       } else if (mainAggregationType == AggregationTypeEnum.geotile) {
                           properties.put("tile", element.keyAsString);
                           tile.ifPresent(s -> properties.put("parent_tile", s));
                       } else {
                           properties.put("key", element.keyAsString);
                       }
                       if (flat) {
                           properties.putAll(exploreService.flat(element, new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), s -> (!"elements".equals(s))));
                           // To remove the geometry from the properties when we make a raw geom with include
                           String keyToRemove = "";
                           for (String key: properties.keySet()) {
                               List<String> kElements =  Arrays.asList(key.split(ArlasServerConfiguration.FLATTEN_CHAR));
                               if(kElements.size()>1 && kElements.get(0).equals("hits") ){
                                   if(String.join(".", kElements.subList(2,kElements.size())).equals(g.reference)){
                                       keyToRemove=key;
                                   }
                               }
                           }
                           properties.remove(keyToRemove);
                       }else{
                           properties.put("elements", element.elements);
                           properties.put("metrics", element.metrics);
                           if (element.hits != null) {
                               properties.put("hits", element.hits.stream());
                           }
                       }
                       feature.setProperties(properties);
                       feature.setProperty(FEATURE_TYPE_KEY, FEATURE_TYPE_VALUE);
                       feature.setProperty(GEOMETRY_REFERENCE, g.reference);

                       String aggregationGeometryType = g.isRaw ? AggregationGeometryEnum.RAW.value() : AggregationGeometryEnum.AGGREGATED.value();
                       feature.setProperty(GEOMETRY_TYPE, aggregationGeometryType);
                       if (g.isRaw) {
                           feature.setProperty(GEOMETRY_SORT, g.sort);
                       }
                       GeoJsonObject geometry = g.geometry;
                       feature.setGeometry(geometry);
                       fc.add(feature);
                   });
               }
            }
        }
        return fc;
    }
}
