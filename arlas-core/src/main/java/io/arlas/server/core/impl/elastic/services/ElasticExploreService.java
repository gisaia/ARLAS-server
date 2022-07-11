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

package io.arlas.server.core.impl.elastic.services;

import co.elastic.clients.elasticsearch._types.GeoBounds;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.mapping.GeoPointProperty;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.core.ElasticDocument;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.impl.elastic.utils.GeoTypeMapper;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Link;
import io.arlas.server.core.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.core.model.enumerations.CollectionFunction;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.enumerations.GeoTypeEnum;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.request.Aggregation;
import io.arlas.server.core.model.response.*;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.services.FluidSearchService;
import io.arlas.server.core.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.*;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.locationtech.spatial4j.shape.Rectangle;

import javax.ws.rs.core.UriInfo;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.arlas.server.core.services.FluidSearchService.*;

public class ElasticExploreService extends ExploreService {

    protected ElasticClient client;

    public ElasticExploreService(ElasticClient client, CollectionReferenceService collectionReferenceService, String baseUri, int arlasRestCacheTimeout) {
        super(baseUri, arlasRestCacheTimeout, collectionReferenceService);
        this.client = client;
    }

    public ElasticClient getClient() {
        return client;
    }

    @Override
    public FluidSearchService getFluidSearch(CollectionReference collectionReference) {
        return new ElasticFluidSearch(collectionReference).setClient(client);
    }

    @Override
    public Hits count(CollectionReference collectionReference,
                      FluidSearchService fluidSearch) throws ArlasException {
        SearchResponse<Map> searchHits = ((ElasticFluidSearch) fluidSearch).exec();

        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = searchHits.hits().total().value();
        hits.nbhits = searchHits.hits().hits().size();
        return hits;
    }

    @Override
    public ComputationResponse compute(CollectionReference collectionReference,
                                       FluidSearchService fluidSearch,
                                       String field, ComputationEnum metric) throws ArlasException {
        SearchResponse<Map> response = ((ElasticFluidSearch) fluidSearch).exec();
        ComputationResponse computationResponse = new ComputationResponse();
        long startQueryTimestamp = System.nanoTime();
        computationResponse.field = field;
        computationResponse.metric = metric;
        computationResponse.totalnb = response.hits().total().value();
        Map<String, Aggregate> aggregations = response.aggregations();
        computationResponse.value = null;
        computationResponse.geometry = null;

        if (computationResponse.totalnb > 0) {
            switch (metric) {
                case AVG:
                    computationResponse.value = aggregations.get(FIELD_AVG_VALUE).simpleValue().value();
                    break;
                case CARDINALITY:
                    computationResponse.value = aggregations.get(FIELD_CARDINALITY_VALUE).simpleValue().value();
                    break;
                case MAX:
                    computationResponse.value = aggregations.get(FIELD_MAX_VALUE).simpleValue().value();
                    break;
                case MIN:
                    computationResponse.value = aggregations.get(FIELD_MIN_VALUE).simpleValue().value();
                    break;
                case SPANNING:
                    double min = aggregations.get(FIELD_MIN_VALUE).simpleValue().value();
                    double max =aggregations.get(FIELD_MAX_VALUE).simpleValue().value();
                    computationResponse.value = max - min;
                    break;
                case SUM:
                    computationResponse.value = aggregations.get(FIELD_SUM_VALUE).simpleValue().value();
                    break;
                case GEOBBOX:
                    // TODO es8 : replace get(0) with proper key?
                    computationResponse.geometry = createBox(aggregations.get(0).geoBounds().bounds());
                    break;
                case GEOCENTROID:
                    // TODO es8 : replace get(0) with proper key?
                    GeoLocation centroid = aggregations.get(0).geoCentroid().location();
                    computationResponse.geometry = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                    break;
            }
        }

        computationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQueryTimestamp);
        return computationResponse;
    }

    @Override
    public Hits search(MixedRequest request, CollectionReference collectionReference, Boolean flat, UriInfo uriInfo, String method) throws ArlasException {
        HitsMetadata<Map> searchHits = getSearchHits(request, collectionReference);
        Search searchRequest  = (Search)request.basicRequest;
        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = searchHits.total().value();
        hits.nbhits = searchHits.hits().size();
        hits.hits = new ArrayList<>((int) hits.nbhits);
        List<co.elastic.clients.elasticsearch.core.search.Hit<Map>> searchHitList = searchHits.hits();
        if(searchRequest.page != null && searchRequest.page.before != null ){
            Collections.reverse(searchHitList);
        }
        for (Hit<Map> hit : searchHitList) {
            hits.hits.add(new ArlasHit(collectionReference, hit.fields(), searchRequest.returned_geometries, flat, false));
        }
        hits.links = getLinks(searchRequest, collectionReference, hits.nbhits, searchHitList, uriInfo, method);
        return hits;
    }

    private HashMap<String, Link> getLinks(Search searchRequest, CollectionReference collectionReference, long nbhits, List<co.elastic.clients.elasticsearch.core.search.Hit<Map>> searchHitList, UriInfo uriInfo, String method) {
        HashMap<String, Link> links = new HashMap<>();
        UriInfoWrapper uriInfoUtil = new UriInfoWrapper(uriInfo, getBaseUri());
        Link self = new Link();
        self.href = uriInfoUtil.getRequestUri();
        self.method = method;
        Link next = null;
        Link previous = null;
        int lastIndex = (int) nbhits -1;
        String sortParam = searchRequest.page != null ? searchRequest.page.sort : null;
        String afterParam = searchRequest.page != null ? searchRequest.page.after : null;
        String beforeParam = searchRequest.page != null ? searchRequest.page.before : null;
        Integer sizeParam = searchRequest.page != null ? searchRequest.page.size : SEARCH_DEFAULT_PAGE_SIZE;
        String lastHitAfter = "";
        String firstHitAfter = "";
        if (lastIndex >= 0 && sizeParam == nbhits && sortParam != null && (afterParam != null || sortParam.contains(collectionReference.params.idPath))) {
            next = new Link();
            next.method = method;
            // Use sorted value of last element return by ES to build after param of next & previous link
            lastHitAfter = searchHitList.get(lastIndex).sort().stream().map(Object::toString).collect(Collectors.joining(","));
            LOGGER.debug("lastHitAfter="+lastHitAfter);

        }
        if (searchHitList.size() > 0 && sortParam != null && (beforeParam != null || sortParam.contains(collectionReference.params.idPath))) {
            previous = new Link();
            previous.method = method;
            firstHitAfter = searchHitList.get(0).sort().stream().map(Object::toString).collect(Collectors.joining(","));
            LOGGER.debug("firstHitAfter="+firstHitAfter);
        }

        switch (method) {
            case "GET" -> {
                links.put("self", self);
                if (next != null) {
                    next.href = uriInfoUtil.getNextHref(lastHitAfter);
                    links.put("next", next);
                }
                if (previous != null) {
                    previous.href = uriInfoUtil.getPreviousHref(firstHitAfter);
                    links.put("previous", previous);
                }
            }
            case "POST" -> {
                self.body = searchRequest;
                links.put("self", self);
                if (next != null) {
                    Page nextPage = new Page();
                    Search search = new Search();
                    search.filter = searchRequest.filter;
                    search.form = searchRequest.form;
                    search.projection = searchRequest.projection;
                    search.returned_geometries = searchRequest.returned_geometries;
                    nextPage.sort = searchRequest.page.sort;
                    nextPage.size = searchRequest.page.size;
                    nextPage.from = searchRequest.page.from;
                    nextPage.after = lastHitAfter;
                    search.page = nextPage;
                    next.href = self.href;
                    next.body = search;
                    links.put("next", next);
                }
                if (previous != null) {
                    Page previousPage = new Page();
                    Search search = new Search();
                    search.filter = searchRequest.filter;
                    search.form = searchRequest.form;
                    search.projection = searchRequest.projection;
                    search.returned_geometries = searchRequest.returned_geometries;
                    previousPage.sort = searchRequest.page.sort;
                    previousPage.size = searchRequest.page.size;
                    previousPage.from = searchRequest.page.from;
                    previousPage.before = firstHitAfter;
                    search.page = previousPage;
                    previous.href = self.href;
                    previous.body = search;
                    links.put("previous", previous);
                }
            }
        }
        return links;
    }

    @Override
    public List<Map<String, JsonData>> searchAsRaw(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        List<co.elastic.clients.elasticsearch.core.search.Hit<Map>> searchHitList = getSearchHits(request, collectionReference).hits();
        List<Map<String, JsonData>> rawList = new ArrayList<>( searchHitList.size());
        for (Hit<Map> hit : searchHitList) {
            rawList.add(hit.fields());
        }
        return rawList;

    }

    private HitsMetadata<Map> getSearchHits(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        ElasticFluidSearch fluidSearch = (ElasticFluidSearch) getSearchRequest(request, collectionReference);
        return fluidSearch.exec().hits();
    }

    @Override
    public FeatureCollection getFeatures(MixedRequest request, CollectionReference collectionReference,
                                         boolean flat, UriInfo uriInfo, String method,
                                         HashMap<String, Object> context) throws ArlasException {
        HitsMetadata<Map> searchHits = getSearchHits(request, collectionReference);
        long totalnb = searchHits.total().value();
        Search searchRequest = (Search) request.basicRequest;
        FeatureCollection fc = new FeatureCollection();
        List<co.elastic.clients.elasticsearch.core.search.Hit<Map>> results = searchHits.hits();
        if (context != null) {
            context.putAll(getLinks(searchRequest, collectionReference, results.size(), results, uriInfo, method));
            context.put("matched", totalnb);
        }
        if (searchRequest.page != null && searchRequest.page.before != null) {
            Collections.reverse(results);
        }
        for (Hit<Map> hit : results) {
            Map<String, JsonData> source = hit.fields();
            ArlasHit arlasHit = new ArlasHit(collectionReference, source, searchRequest.returned_geometries, flat, true);
            if (searchRequest.returned_geometries != null) {
                for (String path : searchRequest.returned_geometries.split(",")) {
                    GeoJsonObject g = arlasHit.getGeometry(path);
                    if (g != null) {
                        g.setCrs(null);
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
        return fc;
    }

    @Override
    public Map<String, Object> getRawDoc(CollectionReference collectionReference, String identifier, String[] includes) throws ArlasException {
        return new ElasticDocument(client).getSource(collectionReference, identifier, includes);
    }

    @Override
    public AggregationResponse aggregate(CollectionReference collectionReference,
                                         List<Aggregation> aggregationsRequests,
                                         int aggTreeDepth,
                                         Long startQuery,
                                         FluidSearchService fluidSearch) throws ArlasException {
        SearchResponse<Map> response = ((ElasticFluidSearch) fluidSearch).exec();

        AggregationResponse aggregationResponse = new AggregationResponse();
        aggregationResponse.totalnb = response.hits().total().value();
        aggregationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);
        return formatAggregationResult(response.aggregations().get("mainAgg"), aggregationResponse, collectionReference, aggregationsRequests, aggTreeDepth);
    }



    @SuppressWarnings({"rawtypes", "unchecked"})
    // TODO DEAL WITH Fetch hit, raw geom, aggregated geom
    private AggregationResponse formatAggregationResult(Aggregate aggregate, AggregationResponse aggregationResponse,
                                                        CollectionReference collection, List<Aggregation> aggregationsRequest, int aggTreeDepth) {
        aggregationResponse.name = aggregate._kind().name();
        if (aggregate.isMultiTerms()) {
            aggregationResponse.sumotherdoccounts = aggregate.multiTerms().sumOtherDocCount();
        }
        List<RawGeometry> rawGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).rawGeometries : null;
        List<AggregatedGeometryEnum> aggregatedGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).aggregatedGeometries : null;
        aggregationResponse.elements = new ArrayList<>();
        if(aggregate.isGeohashGrid()){
            aggregate.geohashGrid().buckets().array().forEach(geoHashGridBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = geoHashGridBucket.key();
                element.count = geoHashGridBucket.docCount();
                GeoLocation geoPoint = getGeohashCentre(element.keyAsString.toString());
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(AggregatedGeometryEnum::isCellOrCellCenterAgg)
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                if (g.isCellAgg()) {
                                    returnedGeometry.geometry = createPolygonFromGeohash(element.keyAsString.toString());
                                } else {
                                    returnedGeometry.geometry = new Point(geoPoint.latlon().lon(),geoPoint.latlon().lat());
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);

                            });
                }
                element.elements = new ArrayList<>();
                if (geoHashGridBucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    geoHashGridBucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( geoHashGridBucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = geoHashGridBucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( geoHashGridBucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = geoHashGridBucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( geoHashGridBucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = geoHashGridBucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(geoHashGridBucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  geoHashGridBucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(geoHashGridBucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = geoHashGridBucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);
            });
        } else if (aggregate.isGeotileGrid()){
            aggregate.geohashGrid().buckets().array().forEach(geoTileGridBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = geoTileGridBucket.key();
                element.count = geoTileGridBucket.docCount();
                List<Integer> zxy = Stream.of(element.keyAsString.toString().split("/"))
                        .map(Integer::valueOf).collect(Collectors.toList());
                BoundingBox tile = GeoTileUtil.getBoundingBox(zxy.get(1), zxy.get(2), zxy.get(0));
                GeoLocation geoPoint = getTileCentre(tile);
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(AggregatedGeometryEnum::isCellOrCellCenterAgg)
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                if (g.isCellAgg()) {
                                    returnedGeometry.geometry = createBox(tile);
                                } else {
                                    returnedGeometry.geometry = new Point(geoPoint.latlon().lon(),geoPoint.latlon().lat());
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);
                            });
                }
                element.elements = new ArrayList<>();
                if (geoTileGridBucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    geoTileGridBucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( geoTileGridBucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = geoTileGridBucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( geoTileGridBucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = geoTileGridBucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( geoTileGridBucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = geoTileGridBucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(geoTileGridBucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  geoTileGridBucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(geoTileGridBucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = geoTileGridBucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);
            });
        } else if (aggregate.isDateHistogram()){
            aggregate.dateHistogram().buckets().array().forEach(dateHistogramBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = dateHistogramBucket.key();
                element.count = dateHistogramBucket.docCount();
                element.key = (dateHistogramBucket.key()).toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli();
                element.elements = new ArrayList<>();
                if (dateHistogramBucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    dateHistogramBucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( dateHistogramBucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = dateHistogramBucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( dateHistogramBucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = dateHistogramBucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( dateHistogramBucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = dateHistogramBucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(dateHistogramBucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  dateHistogramBucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(dateHistogramBucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = dateHistogramBucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);
            });


        } else if (aggregate.isHistogram()) {
            aggregate.histogram().buckets().array().forEach(histogramBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = histogramBucket.key();
                element.count = histogramBucket.docCount();
                element.key = histogramBucket.key();



                element.elements = new ArrayList<>();
                if (histogramBucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    histogramBucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( histogramBucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = histogramBucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( histogramBucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = histogramBucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( histogramBucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = histogramBucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(histogramBucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  histogramBucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(histogramBucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = histogramBucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);
            });

        } else if (aggregate.isDterms()) {
            aggregate.dterms().buckets().array().forEach(bucket -> {
                        AggregationResponse element = new AggregationResponse();
                        element.keyAsString = bucket.key();
                        element.count = bucket.docCount();
                        element.key = bucket.key();

                element.elements = new ArrayList<>();
                if (bucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    bucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( bucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( bucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( bucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(bucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  bucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(bucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = bucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);

        });
        } else if (aggregate.isLterms()) {
            aggregate.lterms().buckets().array().forEach(bucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = bucket.key();
                element.count = bucket.docCount();
                element.key = bucket.key();

                element.elements = new ArrayList<>();
                if (bucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    bucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( bucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( bucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( bucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(bucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  bucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(bucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = bucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);

            });
        } else if (aggregate.isSterms()) {
            aggregate.sterms().buckets().array().forEach(bucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = bucket.key();
                element.count = bucket.docCount();
                element.key = bucket.key();
                element.elements = new ArrayList<>();
                if (bucket.aggregations().size() == 0) {
                    element.elements = null;
                } else {
                    bucket.aggregations().keySet().forEach(key -> {
                        AggregationResponse subAggregationResponse = new AggregationResponse();
                        if (key.contains(TERM_AGG)) {
                            if( bucket.aggregations().get(key).isSterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).sterms().sumOtherDocCount();
                            }
                            if( bucket.aggregations().get(key).isDterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).dterms().sumOtherDocCount();
                            }
                            if( bucket.aggregations().get(key).isLterms()){
                                subAggregationResponse.sumotherdoccounts = bucket.aggregations().get(key).lterms().sumOtherDocCount();
                            }
                        }
                        if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                                || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)){
                            subAggregationResponse = formatAggregationResult(bucket.aggregations().get(key), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);

                        }else {
                            //metric
                            if (element.metrics == null) {
                                element.metrics = new ArrayList<>();
                            }
                            subAggregationResponse = null;
                            AggregationMetric aggregationMetric = new AggregationMetric();
                            aggregationMetric.type = key.split(":")[0];
                            aggregationMetric.field = key.split(":")[1];
                            if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                aggregationMetric.value =  bucket.aggregations().get(key).simpleValue().value();
                            } else {
                                FeatureCollection fc = new FeatureCollection();
                                Feature feature = new Feature();
                                if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                    GeoJsonObject g = createBox(bucket.aggregations().get(key).geoBounds().bounds());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                    GeoLocation centroid = bucket.aggregations().get(key).geoCentroid().location();
                                    GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                                    feature.setGeometry(g);
                                    fc.add(feature);
                                }
                                aggregationMetric.value = fc;
                            }
                            element.metrics.add(aggregationMetric);
                        }
                        if (subAggregationResponse != null) {
                            element.elements.add(subAggregationResponse);
                        }
                    });
                }
                aggregationResponse.elements.add(element);
            });
        }
        return aggregationResponse;
    }





    private boolean isAggregatedGeometry(String subName, List<AggregatedGeometryEnum> geometries) {
        if(!CollectionUtils.isEmpty(geometries)) {
            return geometries.stream().map(g -> g.value() + AGGREGATED_GEOMETRY_SUFFIX).anyMatch(g -> g.equals(subName));
        }
        return false;
    }

    private GeoLocation getGeohashCentre(String geohash) {
        Rectangle bbox = GeohashUtils.decodeBoundary(geohash, SpatialContext.GEO);
        Double maxLon = bbox.getMaxX();
        Double minLon = bbox.getMinX();
        double lon = (maxLon + minLon) / 2;

        Double maxLat = bbox.getMaxY();
        Double minLat = bbox.getMinY();
        double lat = (maxLat + minLat) / 2;

        return GeoLocation.of(builder -> builder.latlon(builder1 -> builder1.lat(lat).lon(lon)));
    }

    private GeoLocation getTileCentre(BoundingBox bbox) {
        double lon = (bbox.getEast() + bbox.getWest()) / 2;
        double lat = (bbox.getNorth() + bbox.getSouth()) / 2;

        return GeoLocation.of(builder -> builder.latlon(builder1 -> builder1.lat(lat).lon(lon)));
    }

    private Polygon createBox(GeoBounds subAggregation) {
        double bottom = subAggregation.coords().bottom();
        double top = subAggregation.coords().top();
        double right = subAggregation.coords().right();
        double left = subAggregation.coords().left();

        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(left, top));
        bounds.add(new LngLatAlt(right, top));
        bounds.add(new LngLatAlt(right, bottom));
        bounds.add(new LngLatAlt(left, bottom));
        bounds.add(new LngLatAlt(left, top));

        Polygon box = new Polygon();
        box.add(bounds);
        return box;
    }
}
