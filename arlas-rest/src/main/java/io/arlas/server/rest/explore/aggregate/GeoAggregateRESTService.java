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
import data.DataPoint;
import data.DataSet;
import gaussian.GaussianDistribution;
import gaussian.GaussianMixtureModel;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.app.Documentation;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotAllowedException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.AggregationGeometryEnum;
import io.arlas.server.core.model.enumerations.AggregationTypeEnum;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.AggregationResponse;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.model.response.GaussianResponse;
import io.arlas.server.core.model.response.ReturnedGeometry;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.*;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.ojalgo.array.Array1D;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.arlas.server.core.services.FluidSearchService.GMM_AGG;

public class GeoAggregateRESTService extends ExploreRESTServices {

    private static final double GEOHASH_EPSILON = 0.00000001;

    // Parameters for the GMM
    private static final String GMM_MAX_COMPONENTS = "" + 6;
    private static final int MAXIMUM_ANGLE_STD = 60;
    private static final String DEGREE_UNIT = "degree";
    private static final String RADIAN_UNIT = "radian";

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
    @ApiOperation(value = "GeoAggregate", produces = UTF8JSON, notes = Documentation.GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
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
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat", value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geoaggregate(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand),
                partitionFilter, columnFilter, flat, agg, maxagecache, Optional.empty(), false);

    }

    @Timed
    @Path("{collection}/_shapeaggregate")
    @GET
    @Produces(ZIPFILE)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "ShapeAggregate", produces = ZIPFILE, notes = Documentation.SHAPEAGGREGATION_OPERATION, consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response shapeaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
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
                    value = Documentation.FILTER_RIGHT_HAND,
                    defaultValue = "true")
            @QueryParam(value = "righthand") Boolean righthand,


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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        return geoaggregate(collectionReference,
                ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand),
                partitionFilter, columnFilter, true, agg, maxagecache, Optional.empty(), true);

    }

    private List<AggregationResponse> geocellaggregate(CollectionReference collectionReference, List<String> agg,
                                                       List<String> f, List<String> q, String dateformat, Boolean righthand,
                                                       String partitionFilter, Optional<String> columnFilter, List<BoundingBox> bboxes) throws ArlasException {
        List<CompletableFuture<AggregationResponse>> futureList = new ArrayList<>();
        for (BoundingBox b : bboxes) {
            Expression pwithinBbox = new Expression(collectionReference.params.centroidPath, OperatorEnum.within,
                    b.getWest() + "," + b.getSouth() + ","
                            + String.format(Locale.ROOT, "%.8f", b.getEast() - GEOHASH_EPSILON) + ","
                            + String.format(Locale.ROOT,"%.8f", b.getNorth() - GEOHASH_EPSILON));
            MixedRequest request = getGeoaggregateRequest(collectionReference,
                    ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand, b, pwithinBbox)
                    , partitionFilter, columnFilter, agg);

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

        return futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Timed
    @Path("{collection}/_geoaggregate/{geohash}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoAggregate on a geohash", produces = UTF8JSON, notes = Documentation.GEOHASH_GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geohashgeoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH ---------------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(name = "geohash",
                    value = "geohash",
                    required = true)
            @PathParam(value = "geohash") String geohash,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION --------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -------------------------
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

            @ApiParam(name = "righthand",
                    defaultValue = "true",
                    value = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM ---------------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat",
                    value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA --------------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        if (geohash.startsWith("#")) {
            geohash = geohash.substring(1);
        }
        if (agg == null || agg.size() == 0) {
            agg = Collections.singletonList("geohash:" + collectionReference.params.centroidPath + ":interval-" + geohash.length());
        }

        List<BoundingBox> bboxes = getBoundingBoxes(geohash, agg, collectionReference);
        AggregationTypeEnum aggType = ParamsParser.getAggregations(collectionReference, agg).get(0).type;

        List<AggregationResponse> aggResponses = geocellaggregate(collectionReference, agg, f, q, dateformat, righthand,
                partitionFilter, columnFilter, bboxes);

        return cache(Response.ok(toGeoJson(merge(aggResponses), aggType, Boolean.TRUE.equals(flat), Optional.of(geohash))), maxagecache);

    }

    @Timed
    @Path("{collection}/_geoaggregate/{z}/{x}/{y}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "GeoAggregate on a geotile", produces = UTF8JSON, notes = Documentation.GEOTILE_GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geotilegeoaggregate(
            // --------------------------------------------------------
            // ----------------------- PATH ---------------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(name = "z",
                    value = "z",
                    required = true)
            @PathParam(value = "z") Integer z,
            @ApiParam(name = "x",
                    value = "x",
                    required = true)
            @PathParam(value = "x") Integer x,
            @ApiParam(name = "y",
                    value = "y",
                    required = true)
            @PathParam(value = "y") Integer y,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION --------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -------------------------
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

            @ApiParam(name = "righthand",
                    defaultValue = "true",
                    value = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM ---------------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat",
                    value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA --------------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        if (agg == null || agg.size() == 0) {
            agg = Collections.singletonList("geotile:" + collectionReference.params.centroidPath + ":interval-" + (z+3));
        }

        List<BoundingBox> bboxes = getBoundingBoxes(z, x, y, agg, collectionReference);
        AggregationTypeEnum aggType = ParamsParser.getAggregations(collectionReference, agg).get(0).type;

        List<AggregationResponse> aggResponses = geocellaggregate(collectionReference, agg, f, q, dateformat, righthand,
                partitionFilter, columnFilter, bboxes);

        return cache(Response.ok(toGeoJson(merge(aggResponses), aggType, Boolean.TRUE.equals(flat), Optional.of(z + "/" + x + "/" + y))), maxagecache);

    }

    private List<BoundingBox> getBoundingBoxes(String geohash, List<String> agg, CollectionReference collectionReference) throws ArlasException {
        // we expect a 'geohash' aggregation model with an interval specified
        int interval = 0;
        List<Interval> intervals = ParamsParser.getAggregations(collectionReference, agg).stream()
                .filter(a -> a.type.equals(AggregationTypeEnum.geohash))
                .map(a -> a.interval)
                .collect(Collectors.toList());
        if (intervals.size() > 0) {
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
                .collect(Collectors.toList());
        if (intervals.size() > 0) {
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
    @ApiOperation(value = "GeoAggregate", produces = UTF8JSON, notes = Documentation.GEOAGGREGATION_OPERATION, consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class),
            @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geoaggregatePost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            AggregationsRequest aggregationRequest,

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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, aggregationRequest);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationRequest);

        request.basicRequest = aggregationRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        FeatureCollection fc = getFeatureCollection(request, collectionReference, (aggregationRequest.form != null && aggregationRequest.form.flat), Optional.empty());

        return cache(Response.ok(fc), maxagecache);
    }


    @Timed
    @Path("{collection}/_shapeaggregate")
    @POST
    @Produces(ZIPFILE)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "ShapeAggregate", produces = ZIPFILE, notes = Documentation.SHAPEAGGREGATION_OPERATION, consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response shapeaggregatePost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            AggregationsRequest aggregationRequest,

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
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, aggregationRequest);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, aggregationRequest);

        request.basicRequest = aggregationRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

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
        aggregationsRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
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

    @Timed
    @Path("{collection}/_gmm/{z}/{x}/{y}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Multi GaussianClustering", produces = UTF8JSON, notes = "", consumes = UTF8JSON, response = FeatureCollection.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = FeatureCollection.class, responseContainer = "FeatureCollection"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class),
            @ApiResponse(code = 501, message = "Not implemented functionality.", response = Error.class)})
    public Response geotilegmm(
            // --------------------------------------------------------
            // ----------------------- PATH ---------------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(name = "z",
                    value = "z",
                    required = true)
            @PathParam(value = "z") Integer z,
            @ApiParam(name = "x",
                    value = "x",
                    required = true)
            @PathParam(value = "x") Integer x,
            @ApiParam(name = "y",
                    value = "y",
                    required = true)
            @PathParam(value = "y") Integer y,

            // --------------------------------------------------------
            // ---------------------- GMM PARAMETERS ------------------
            // --------------------------------------------------------
            @ApiParam(name = "abscissaUnit",
                    value = Documentation.GMM_ABSCISSA_UNIT)
            @QueryParam(value = "abscissaUnit") String abscissaUnit,

            @ApiParam(name = "maxGaussians",
                    defaultValue = GMM_MAX_COMPONENTS,
                    value = Documentation.GMM_MAX_COMPONENTS)
            @QueryParam(value = "maxGaussians") Integer maxGaussians,

            @ApiParam(name = "maxSpread",
                    value = Documentation.GMM_MAX_SPREAD)
            @QueryParam(value = "maxSpread") List<Double> maxSpread,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION --------------------
            // --------------------------------------------------------
            @ApiParam(name = "agg",
                    value = Documentation.GEOAGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -------------------------
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

            @ApiParam(name = "righthand",
                    defaultValue = "true",
                    value = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM ---------------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat",
                    value = Documentation.FORM_FLAT,
                    defaultValue = "false")
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA --------------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection);

        if (collectionReference == null)
            throw new NotFoundException(collection);

        // If there is no aggregations, then throw an error
        if (agg == null || agg.size() == 0)
            throw new NotAllowedException("For a GMM clustering, there needs to be something to aggregate");

        // If the aggregations don't start with a geotile aggregation, insert it
        if (!Objects.equals(agg.get(0).split(":")[0], "geotile"))
            throw new NotAllowedException("For a GMM aggregation, the first aggregation has to be a geotile one");

        // For the following aggregations, if not histogram, raise an error
        for (int i = 1; i < agg.size(); i++)
            if (!Objects.equals(agg.get(i).split(":")[0], "histogram"))
                throw new NotAllowedException("The aggregations should all but the first be histograms");

        // Should work for any dimensions, but only tested in 2D so check for that
        if (agg.size() != 3)
            throw new NotAllowedException("The GMM clustering is only performed on 2D histograms");

        // Performs geo-aggregated 2D histogram
        List<BoundingBox> bboxes = getBoundingBoxes(z, x, y, agg, collectionReference);
        AggregationTypeEnum aggType = ParamsParser.getAggregations(collectionReference, agg).get(0).type;

        List<AggregationResponse> aggResponses = geocellaggregate(collectionReference, agg, f, q, dateformat, righthand,
                partitionFilter, columnFilter, bboxes);

        FeatureCollection geoHistogramAggregation = toGeoJson(merge(aggResponses), aggType, Boolean.TRUE.equals(flat), Optional.of(z + "/" + x + "/" + y));

        AggregationResponse gmmAggregation = new AggregationResponse();
        gmmAggregation.name = GMM_AGG;
        gmmAggregation.elements = new ArrayList<>(geoHistogramAggregation.getFeatures().size());

        // Check that the parameters for the GMM are well-defined
        // The maximum number of gaussians for the clustering
        maxGaussians = maxGaussians == null ? Integer.valueOf(GMM_MAX_COMPONENTS) : maxGaussians;

        // The unit of the first coordinate of the aggregated histogram
        abscissaUnit = abscissaUnit == null ? "" : abscissaUnit;

        // The maximum and minimum spread values for the gaussians

        // If maximum spread values have been specified for no aggregation, create the list
        if (maxSpread == null || maxSpread.size() == 0) {
            maxSpread = new ArrayList<>(agg.size() - 1);
            double maxFirstValueStd = switch (abscissaUnit) {
                case DEGREE_UNIT -> MAXIMUM_ANGLE_STD;
                case RADIAN_UNIT -> MAXIMUM_ANGLE_STD * Math.PI / 180;
                default -> Double.MAX_VALUE;
            };
            maxSpread.add(maxFirstValueStd);
        }

        while (maxSpread.size() < agg.size() - 1)
            maxSpread.add(Double.MAX_VALUE);
        Array1D<Double> maxGaussianSpread = Array1D.PRIMITIVE64.copy(maxSpread);

        List<Double> minSpread = new ArrayList<>(agg.size() - 1);
        for (int i = 1; i < agg.size(); i++)
            minSpread.add(Math.pow(Double.parseDouble(agg.get(i).split("interval-")[1]), 2) / 2);
        Array1D<Double> minGaussianSpread = Array1D.PRIMITIVE64.copy(minSpread);

        // GMM on each feature
        for(Feature feature: geoHistogramAggregation.getFeatures()) {
            DataSet dataSet = new DataSet(agg.size() - 1);

            long totalCount = feature.getProperty("count");
            double valley = Double.MAX_VALUE;

            boolean isFluxClustering = Objects.equals(abscissaUnit, DEGREE_UNIT) || Objects.equals(abscissaUnit, RADIAN_UNIT);

            // Search for the lowest count on angle bucket in case the aggregation is performed on flux data
            if (isFluxClustering) {
                long valleyCount = feature.getProperty("count");

                for (AggregationResponse elem : (ArrayList<AggregationResponse>) feature.getProperty("elements")) {
                    for (AggregationResponse bucketAngle : elem.elements) {
                        if (bucketAngle.count < valleyCount) {
                            valley = (double) bucketAngle.key;
                            valleyCount = bucketAngle.count;
                        }
                    }
                }
            }

            // Fill the data set
            for (AggregationResponse elem : (ArrayList<AggregationResponse>) feature.getProperty("elements")) {
                fillDataSetRecursively(dataSet, elem, new ArrayList<>());
            }

            List<Double> observedProbabilities = new ArrayList<>(dataSet.getWeights());

            dataSet.normaliseWeights((double) totalCount/dataSet.length);
            observedProbabilities.replaceAll(aDouble -> aDouble / totalCount);

            // Shift dataset to have the valley at the start of the data
            if (isFluxClustering) {
                double angleOffset = Objects.equals(abscissaUnit, DEGREE_UNIT) ? 360 : 2 * Math.PI;

                for (DataPoint data : dataSet.getDataPoints()) {
                    if (data.values.get(0) < valley) {
                        data.values.set(0, data.values.get(0) + angleOffset);
                    }
                }
            }

            GaussianMixtureModel model = new GaussianMixtureModel(Math.min(maxGaussians, (int) Math.ceil(dataSet.length/10.)), dataSet);

            // Perform the clustering
            model.cluster(dataSet, maxGaussianSpread, minGaussianSpread);
            GaussianMixtureModel clusteredModel = model.mergeCloseGaussians(dataSet.getDataPoints(), observedProbabilities, minGaussianSpread);

            // Convert the result to an AggregationResponse
            AggregationResponse gmm = new AggregationResponse();
            gmm.count = totalCount;

            ReturnedGeometry geometry = new ReturnedGeometry();
            geometry.geometry = feature.getGeometry();
            gmm.geometries = List.of(geometry);

            gmm.gaussians = new ArrayList<>(clusteredModel.numberClusters);
            for (int i = 0; i < clusteredModel.numberClusters; i++) {
                GaussianDistribution gaussian = clusteredModel.getGaussian(i);
                gmm.gaussians.add(new GaussianResponse(gaussian.weight, gaussian.mean, gaussian.covariance));
            }

            gmmAggregation.elements.add(gmm);
        }

        // Format the output of the clustering to the right format
        return cache(Response.ok(toGeoJson(gmmAggregation, AggregationTypeEnum.gmm, Boolean.TRUE.equals(flat), Optional.of(z + "/" + x + "/" + y))), maxagecache);
    }

    /**
     * Explore recursively the aggregation tree in order to fill the dataSet with the data contained
     * @param dataSet the DataSet to fill
     * @param element the Feature element containing the aggregated data
     * @param numericalValues the list storing the information of the bucket
     */
    private void fillDataSetRecursively(DataSet dataSet, AggregationResponse element, List<Double> numericalValues) {
        if (element.elements == null || element.elements.size() == 0) {
            dataSet.addDataPoint(new DataPoint(numericalValues), (double) element.count);
            return;
        }

        for (AggregationResponse bucket : element.elements) {
            List<Double> treeExploration = new ArrayList<>(numericalValues);
            if (bucket.key != null)
                treeExploration.add((double) bucket.key);

            fillDataSetRecursively(dataSet, bucket, treeExploration);
        }
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
                       } else if (mainAggregationType == AggregationTypeEnum.h3) {
                           properties.put("h3", element.keyAsString);
                           tile.ifPresent(s -> properties.put("parent_cell", s));
                       } else if (mainAggregationType == AggregationTypeEnum.geotile) {
                           properties.put("tile", element.keyAsString);
                           tile.ifPresent(s -> properties.put("parent_tile", s));
                       } else if (mainAggregationType == AggregationTypeEnum.gmm) {
                           properties.put("gmm", element.gaussians);
                           tile.ifPresent(s -> properties.put("parent_tile", s));
                       } else {
                           properties.put("key", element.keyAsString);
                       }
                       if (flat) {
                           properties.putAll(exploreService.flat(element, new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), s -> (!"elements".equals(s))));
                       } else {
                           properties.put("elements", element.elements);
                           properties.put("metrics", element.metrics);
                           if (element.hits != null) {
                               properties.put("hits", element.hits);
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
