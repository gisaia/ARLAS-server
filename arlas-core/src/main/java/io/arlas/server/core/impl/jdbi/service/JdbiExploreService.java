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

package io.arlas.server.core.impl.jdbi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.exceptions.NotImplementedException;
import io.arlas.server.core.impl.jdbi.dao.DataDao;
import io.arlas.server.core.impl.jdbi.model.ClauseParam;
import io.arlas.server.core.impl.jdbi.model.JdbiAggregationResult;
import io.arlas.server.core.impl.jdbi.model.RequestFactory;
import io.arlas.server.core.impl.jdbi.model.SelectRequest;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Link;
import io.arlas.server.core.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.request.Aggregation;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.request.RawGeometry;
import io.arlas.server.core.model.request.Search;
import io.arlas.server.core.model.response.*;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.services.FluidSearchService;
import io.arlas.server.core.utils.BoundingBox;
import io.arlas.server.core.utils.GeoTileUtil;
import io.arlas.server.core.utils.GeoUtil;
import io.arlas.server.core.utils.ParamsParser;
import org.apache.commons.collections4.CollectionUtils;
import org.geojson.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.result.ResultIterator;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.locationtech.spatial4j.shape.Rectangle;

import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.arlas.server.core.impl.jdbi.model.ColumnQualifier.*;
import static io.arlas.server.core.impl.jdbi.model.SelectRequest.*;
import static io.arlas.server.core.model.enumerations.ComputationEnum.GEOBBOX;
import static io.arlas.server.core.model.enumerations.ComputationEnum.GEOCENTROID;
import static io.arlas.server.core.services.FluidSearchService.*;

public class JdbiExploreService extends ExploreService {
    private static ObjectReader reader = new ObjectMapper().readerFor(GeoJsonObject.class);
    private static GeoJsonWriter geoJsonWriter = new GeoJsonWriter();

    final private DataDao dao;
    final private RequestFactory requestFactory;

    public JdbiExploreService(Jdbi jdbi, CollectionReferenceService collectionReferenceService,
                              String baseUri, int arlasRestCacheTimeout, RequestFactory requestFactory) {
        super(baseUri, arlasRestCacheTimeout, collectionReferenceService);
        this.dao = jdbi.onDemand(DataDao.class);
        this.requestFactory = requestFactory;
    }

    @Override
    public FluidSearchService getFluidSearch(CollectionReference collectionReference) throws ArlasException {
        return new JdbiFluidSearch(collectionReference, requestFactory);
    }

    @Override
    public Hits count(CollectionReference collectionReference,
                      FluidSearchService fluidSearch) throws ArlasException {
        SelectRequest req = ((JdbiFluidSearch) fluidSearch).getRequest();
        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = dao.count(collectionReference.params.indexName, req.getWhereClause(),
                getParamMap(collectionReference, req.getWhereParams()));
        hits.nbhits = hits.totalnb;
        return hits;
    }

    @Override
    public ComputationResponse compute(CollectionReference collectionReference,
                                       FluidSearchService fluidSearch,
                                       String field, ComputationEnum metric) throws ArlasException {
        SelectRequest req = ((JdbiFluidSearch) fluidSearch).getRequest();
        ComputationResponse computationResponse = new ComputationResponse();
        computationResponse.field = field;
        computationResponse.metric = metric;
        try (ResultIterator<Map<String,Object>> iter = dao.select(collectionReference.params.indexName,
                req.getSelectClause(), req.getWhereClause(), req.getGroupClause(), req.getOrderClause(),
                req.getLimitClause(), getParamMap(collectionReference, req.getWhereParams()))) {

            long startQueryTimestamp = System.nanoTime();
            computationResponse.value = null;
            computationResponse.geometry = null;
            Map<String, Object> response = ResultIterable.of(iter).first();
            computationResponse.totalnb = (Long) response.get(COUNT);
            if (computationResponse.totalnb > 0) {
                switch (metric) {
                    case AVG:
                        computationResponse.value = Double.parseDouble(response.get(AVG).toString());
                        break;
                    case CARDINALITY:
                        computationResponse.value = Double.parseDouble(response.get(COUNT).toString());
                        break;
                    case MAX:
                        computationResponse.value = Double.parseDouble(response.get(MAX).toString());
                        break;
                    case MIN:
                        computationResponse.value = Double.parseDouble(response.get(MIN).toString());
                        break;
                    case SPANNING:
                        computationResponse.value = Double.parseDouble(response.get(MAX).toString()) - Double.parseDouble(response.get(MIN).toString());
                        break;
                    case SUM:
                        computationResponse.value = Double.parseDouble(response.get(SUM).toString());
                        break;
                    case GEOBBOX:
                        computationResponse.geometry = createGeoJsonObject((String)response.get(GEOBBOX.value()));
                        break;
                    case GEOCENTROID:
                        computationResponse.geometry = createGeoJsonObject((String)response.get(GEOCENTROID.value()));
                        break;
                }
            }

            computationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQueryTimestamp);
        }
        return computationResponse;
    }

    @Override
    public FeatureCollection getFeatures(MixedRequest request, CollectionReference collectionReference,
                                         boolean flat, UriInfo uriInfo,
                                         String method, HashMap<String, Object> context) throws ArlasException {
        FluidSearchService fluidSearch = getSearchRequest(request, collectionReference);
        SelectRequest req = ((JdbiFluidSearch) fluidSearch).getRequest(true);
        Search searchRequest = (Search) request.basicRequest;
        FeatureCollection fc = new FeatureCollection();
        try (ResultIterator<Map<String,Object>> iter = dao.select(collectionReference.params.indexName,
                req.getSelectClause(), req.getWhereClause(), req.getGroupClause(), req.getOrderClause(),
                req.getLimitClause(), getParamMap(collectionReference, req.getWhereParams()))) {
            while (iter.hasNext()) {
                Map<String, Object> source = iter.next();
                Hit arlasHit = new Hit(collectionReference, source, searchRequest.returned_geometries, flat, true);
                if (searchRequest.returned_geometries != null) {
                    for (String path : searchRequest.returned_geometries.split(",")) {
                        GeoJsonObject g = arlasHit.getGeometry(path);
                        if (g != null) {
                            fc.add(getFeatureFromHit(arlasHit, path, g));
                        }
                    }
                } else {
                    //Apply geometry or centroid to geo json feature
                    if (arlasHit.md.geometry != null) {
                        fc.add(getFeatureFromHit(arlasHit, collectionReference.params.geometryPath, arlasHit.md.geometry));
                    } else if (arlasHit.md.centroid != null) {
                        fc.add(getFeatureFromHit(arlasHit, collectionReference.params.centroidPath, arlasHit.md.centroid));
                    }
                }
            }
        }
        if (searchRequest.page != null && searchRequest.page.before != null) {
            Collections.reverse(fc.getFeatures());
        }

        return fc;
    }

    @Override
    public Hits search(MixedRequest request, CollectionReference collectionReference, Boolean flat, UriInfo uriInfo, String method) throws ArlasException {
        // TODO search
        throw new NotImplementedException();
    }

    @Override
    public List<Map<String, Object>> searchAsRaw(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        // TODO searchAsRaw
        throw new NotImplementedException();
    }

    @Override
    public Map<String, Object> getRawDoc(CollectionReference collectionReference, String identifier, String[] includes) throws ArlasException {
        // TODO getRawDoc
        throw new NotImplementedException();
    }

    @Override
    protected AggregationResponse aggregate(CollectionReference collectionReference,
                                            List<Aggregation> aggregationsRequests,
                                            int aggTreeDepth,
                                            Long startQuery,
                                            FluidSearchService fluidSearch) throws ArlasException {
        SelectRequest req = ((JdbiFluidSearch) fluidSearch).getRequest();
        AggregationResponse aggregationResponse = new AggregationResponse();
        try (ResultIterator<Map<String,Object>> iter = dao.select(collectionReference.params.indexName,
                req.getSelectClause(), req.getWhereClause(), req.getGroupClause(), req.getOrderClause(), req.getLimitClause(),
                getParamMap(collectionReference, req.getWhereParams()))) {

            aggregationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);
            JdbiAggregationResult aggResult = new JdbiAggregationResult(aggregationsRequests.size(), ResultIterable.of(iter));
            aggregationResponse = formatAggregationResult(aggResult.getResults(), aggregationResponse, collectionReference, aggregationsRequests, aggTreeDepth);
        }
        return aggregationResponse;
    }

    private AggregationResponse formatAggregationResult(Map<Object, Map> buckets, AggregationResponse aggregationResponse,
                                                        CollectionReference collection, List<Aggregation> aggregationsRequest,
                                                        int aggTreeDepth) {
        List<RawGeometry> rawGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).rawGeometries : null;
        List<AggregatedGeometryEnum> aggregatedGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).aggregatedGeometries : null;
        aggregationResponse.elements = new ArrayList<>();
        buckets.forEach((key, bucket) -> {
            Map<String, Object> bucketValues = (Map<String, Object>) bucket.get("values");
            if (aggregationResponse.name == null) {
                aggregationResponse.name = FluidSearchService.getAggregationName((String) bucketValues.get("aggName"));
            }
            if (aggregationResponse.totalnb == null) {
                aggregationResponse.totalnb = 0l;
            }
            AggregationResponse element = new AggregationResponse();
            element.count = (Long) bucketValues.get("count");
            element.keyAsString = bucketValues.get("key");
            element.key = element.keyAsString;
            if (aggregationResponse.name.equals(GEOHASH_AGG)) {
                Point geoPoint = getGeohashCentre(element.keyAsString.toString());
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(g -> g.isCellOrCellCenterAgg())
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                if (g.isCellAgg()) {
                                    returnedGeometry.geometry = createPolygonFromGeohash(element.keyAsString.toString());
                                } else {
                                    returnedGeometry.geometry = geoPoint;
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);
                            });
                }
            } else if (aggregationResponse.name.equals(GEOTILE_AGG)) {
                List<Integer> zxy = Stream.of(element.keyAsString.toString().split("/"))
                        .map (elem -> Integer.valueOf(elem))
                        .collect(Collectors.toList());
                BoundingBox tile = GeoTileUtil.getBoundingBox(zxy.get(1), zxy.get(2), zxy.get(0));
                Point geoPoint = getTileCentre(tile);
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(g -> g.isCellOrCellCenterAgg())
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                returnedGeometry.geometry = (g.isCellAgg()) ? createBox(tile) : geoPoint;
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);

                            });
                }
            } else if (aggregationResponse.name.startsWith(DATEHISTOGRAM_AGG)) {
                element.keyAsString = DateTimeFormatter.ofPattern(ParamsParser.getValidAggregationFormat(aggregationsRequest.get(0).format))
                        .withZone(ZoneId.from(ZoneOffset.UTC)).format(Instant.ofEpochMilli(((Double)element.key).longValue()));
            }

            bucketValues.forEach((qualifier, value) -> {
                if (isMetric(qualifier)) {
                    AggregationMetric aggregationMetric = new AggregationMetric();
                    String[] q = splitQualifier(qualifier);
                    aggregationMetric.type = q[0];
                    aggregationMetric.field = q[1];
                    if (isGeoMetric(qualifier)) {
                        FeatureCollection fc = new FeatureCollection();
                        Feature feature = new Feature();
                        feature.setGeometry(createGeoJsonObject((String)value));
                        fc.add(feature);
                        aggregationMetric.value = fc;
                    } else {
                        aggregationMetric.value = value;
                    }
                    if (element.metrics == null) {
                        element.metrics = new ArrayList<>();
                    }
                    element.metrics.add(aggregationMetric);
                } else if (isAggregatedGeometry(qualifier)) {
                    ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                    returnedGeometry.isRaw = false;
                    returnedGeometry.geometry = createGeoJsonObject((String)value);
                    if (isAggregatedGeoBbox(qualifier)) {
                        returnedGeometry.reference = AggregatedGeometryEnum.BBOX.value();
                    } else if (isAggregatedGeoCentroid(qualifier)) {
                        returnedGeometry.reference = AggregatedGeometryEnum.CENTROID.value();
                    }
                    if (element.geometries == null) {
                        element.geometries = new ArrayList<>();
                    }
                    element.geometries.add(returnedGeometry);
                } else {
                    // TODO other qualifiers
                    if (!Arrays.asList("key","count","aggName").contains(qualifier)) {
                        LOGGER.warn("column qualifier not implemented yet: " + qualifier);
                    }
                }
            });

            Map<Object, Map> subAgg = (Map) bucket.get("subAgg");
            if (!subAgg.isEmpty()) {
                element.elements = new ArrayList<>();
                element.elements.add(formatAggregationResult(subAgg, new AggregationResponse(), collection, aggregationsRequest, aggTreeDepth+1));
            }
            aggregationResponse.totalnb += element.count;
            aggregationResponse.elements.add(element);


            if (aggregationResponse.name.equals(TERM_AGG)) {
//                aggregationResponse.sumotherdoccounts = ((Terms) aggregation).getSumOfOtherDocCounts();
                aggregationResponse.sumotherdoccounts = 0l; // all buckets are fetched in JDBC (should we change this behaviour?)
            }
        });
        return aggregationResponse;
    }

    private Map<String, Object> getParamMap(CollectionReference collectionReference, Map<String, ClauseParam> params) {
        Map<String, Object> ret = new HashMap<>();
        // JDBC infers the column types from the object type, so we need to identify the field types
        params.forEach((k, p) -> {
            try {
                ret.put(k, collectionReferenceService.getType(collectionReference, p.field, false)
                        .getJavaObject(p.value));
            } catch (ArlasException arlasException) {
                // will not happen with the parameter "throwException" set to false
            }
        });
        return ret;
    }

    private Point getGeohashCentre(String geohash) {
        Rectangle bbox = GeohashUtils.decodeBoundary(geohash, SpatialContext.GEO);

        Double maxLon = bbox.getMaxX();
        Double minLon = bbox.getMinX();
        double lon = (maxLon + minLon) / 2;

        Double maxLat = bbox.getMaxY();
        Double minLat = bbox.getMinY();
        double lat = (maxLat + minLat) / 2;

        return new Point(lon, lat);
    }

    private Point getTileCentre(BoundingBox bbox) {
        double lon = (bbox.getEast() + bbox.getWest()) / 2;
        double lat = (bbox.getNorth() + bbox.getSouth()) / 2;

        return new Point(lon, lat);
    }

    private GeoJsonObject createGeoJsonObject(String geo) {
        try {
            return reader.readValue(geoJsonWriter.write(GeoUtil.readWKT(geo)));
        } catch (IOException | ArlasException e) {
            throw new RuntimeException("Unable to parse " + geo);
        }
    }

}
