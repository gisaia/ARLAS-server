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

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.GeoBounds;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.MultiBucketBase;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.core.ElasticDocument;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.impl.elastic.utils.GeoTypeMapper;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Link;
import io.arlas.server.core.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.enumerations.GeoTypeEnum;
import io.arlas.server.core.model.request.*;
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

import jakarta.ws.rs.core.UriInfo;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.arlas.server.core.services.FluidSearchService.*;

public class ElasticExploreService extends ExploreService {

    protected ElasticClient client;
    protected int arlasElasticMaxPrecisionThreshold;
    private final ObjectMapper mapper = new ObjectMapper();

    public ElasticExploreService(ElasticClient client, CollectionReferenceService collectionReferenceService,
                                 String baseUri, int arlasRestCacheTimeout, int arlasElasticMaxPrecisionThreshold) {
        super(baseUri, arlasRestCacheTimeout, collectionReferenceService);
        this.client = client;
        this.arlasElasticMaxPrecisionThreshold = arlasElasticMaxPrecisionThreshold;
    }

    public ElasticClient getClient() {
        return client;
    }

    @Override
    public FluidSearchService getFluidSearch(CollectionReference collectionReference) {
        return new ElasticFluidSearch(collectionReference, arlasElasticMaxPrecisionThreshold).setClient(client);
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
                case AVG -> computationResponse.value = aggregations.get(FIELD_AVG_VALUE).avg().value();
                case CARDINALITY ->
                        computationResponse.value = (double) aggregations.get(FIELD_CARDINALITY_VALUE).cardinality().value();
                case MAX -> computationResponse.value = aggregations.get(FIELD_MAX_VALUE).max().value();
                case MIN -> computationResponse.value = aggregations.get(FIELD_MIN_VALUE).min().value();
                case SPANNING -> {
                    double min = aggregations.get(FIELD_MIN_VALUE).min().value();
                    double max = aggregations.get(FIELD_MAX_VALUE).max().value();
                    computationResponse.value = max - min;
                }
                case SUM -> computationResponse.value = aggregations.get(FIELD_SUM_VALUE).sum().value();
                case GEOBBOX ->
                        computationResponse.geometry = createBox(aggregations.get(FIELD_GEOBBOX_VALUE).geoBounds().bounds());
                case GEOCENTROID -> {
                    GeoLocation centroid = aggregations.get(FIELD_GEOCENTROID_VALUE).geoCentroid().location();
                    computationResponse.geometry = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                }
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
        // searchHitList should be a modifiable list in order to apply the Collections.reverse.
        ArrayList<co.elastic.clients.elasticsearch.core.search.Hit<Map>> searchHitList = new ArrayList<>(searchHits.hits());
        if(searchRequest.page != null && searchRequest.page.before != null ){
            Collections.reverse(searchHitList);
        }
        for (Hit<Map> hit : searchHitList) {
            hits.hits.add(new ArlasHit(collectionReference, hit.source(), searchRequest.returned_geometries, flat, false));
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
            if(!sortParam.contains("geodistance")){
                // We can't use the sort() method because it transforms all the date in timestamp and dont keep the original format
                // Use sorted value of last element return by ES to build after param of next & previous link
                lastHitAfter = Arrays.asList(sortParam.split(",")).stream()
                        .map(v -> v.replace("-","").replace("+",""))
                        .map(v -> MapExplorer.getObjectFromPath(v, searchHitList.get(lastIndex).source()))
                        .map(v -> String.valueOf(v))
                        .collect(Collectors.joining(","));
            }else{
                lastHitAfter = getSortValues(sortParam, searchHitList, lastIndex, lastIndex)
                        .stream().map(v -> String.valueOf(v)).collect(Collectors.joining(","));
             }
            LOGGER.debug("lastHitAfter="+lastHitAfter);

        }
        if (searchHitList.size() > 0 && sortParam != null && (beforeParam != null || sortParam.contains(collectionReference.params.idPath))) {
            previous = new Link();
            previous.method = method;
            if(!sortParam.contains("geodistance")){
                // We can't use the sort() method because it transforms all the date in timestamp and dont keep the original format
                firstHitAfter = Arrays.asList(sortParam.split(",")).stream()
                        .map(v -> v.replace("-","").replace("+",""))
                        .map(v -> MapExplorer.getObjectFromPath(v, searchHitList.get(0).source()))
                        .map(v -> String.valueOf(v))
                        .collect(Collectors.joining(","));
            }else{
                firstHitAfter = getSortValues(sortParam, searchHitList, 0, lastIndex)
                        .stream().map(v -> String.valueOf(v)).collect(Collectors.joining(","));
            }
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

    private List<String> getSortValues(String sortParam, List<Hit<Map>> searchHitList, int index, int lastIndex) {
        List<String> sortValues = Arrays.asList(sortParam.split(","))
                .stream()
                .map(v -> v.replace("-","").replace("+",""))
                .map(v -> MapExplorer.getObjectFromPath(v, searchHitList.get(index).source()))
                .map(v -> String.valueOf(v)).collect(Collectors.toList());
        String geodistanceSortValue = Arrays.asList(sortParam.split(",")).stream().filter(v -> v.contains("geodistance")).findFirst().get();
        int geodistanceIndex = Arrays.asList(sortParam.split(",")).indexOf(geodistanceSortValue);
        String geodistanceValue = String.valueOf(searchHitList.get(lastIndex).sort().get(geodistanceIndex).doubleValue());
        sortValues.set(geodistanceIndex,geodistanceValue);
        return sortValues;
    }

    @Override
    public List<Map<String, JsonData>> searchAsRaw(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        List<co.elastic.clients.elasticsearch.core.search.Hit<Map>> searchHitList = getSearchHits(request, collectionReference).hits();
        List<Map<String, JsonData>> rawList = new ArrayList<>( searchHitList.size());
        for (Hit<Map> hit : searchHitList) {
            rawList.add(hit.source());
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
            Map<String, JsonData> source = hit.source();
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
    private AggregationResponse formatAggregationResult(Aggregate aggregate, AggregationResponse aggregationResponse,
                                                        CollectionReference collection, List<Aggregation> aggregationsRequest, int aggTreeDepth) {
        aggregationResponse.name = aggregate._kind().name();
        if (aggregate.isSterms()) {
            aggregationResponse.sumotherdoccounts = aggregate.sterms().sumOtherDocCount();
        }
        if (aggregate.isDterms()) {
            aggregationResponse.sumotherdoccounts = aggregate.dterms().sumOtherDocCount();
        }
        if (aggregate.isLterms()) {
            aggregationResponse.sumotherdoccounts = aggregate.lterms().sumOtherDocCount();
        }
        List<RawGeometry> rawGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).rawGeometries : null;
        List<AggregatedGeometryEnum> aggregatedGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).aggregatedGeometries : null;
        aggregationResponse.elements = new ArrayList<>();
        if(aggregate.isGeohashGrid()){
            aggregate.geohashGrid().buckets().array().forEach(geoHashGridBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = geoHashGridBucket.key();
                LatLonGeoLocation geoPoint = getGeohashCentre(element.keyAsString.toString()).latlon();
                element.key = new LatLon(geoPoint.lat(),geoPoint.lon());
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
                                    returnedGeometry.geometry = new Point(geoPoint.lon(), geoPoint.lat());
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);

                            });
                }
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, geoHashGridBucket);
            });
        } else if (aggregate.isGeotileGrid()){
            aggregate.geotileGrid().buckets().array().forEach(geoTileGridBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = geoTileGridBucket.key();
                List<Integer> zxy = Stream.of(element.keyAsString.toString().split("/"))
                        .map(Integer::valueOf).toList();
                BoundingBox tile = GeoTileUtil.getBoundingBox(zxy.get(1), zxy.get(2), zxy.get(0));
                LatLonGeoLocation geoPoint = getTileCentre(tile).latlon();
                element.key = new LatLon(geoPoint.lat(),geoPoint.lon());
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
                                    returnedGeometry.geometry = new Point(geoPoint.lon(), geoPoint.lat());
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);

                            });
                }
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, geoTileGridBucket);
            });
        } else if (aggregate.isGeohexGrid()){
            aggregate.geohexGrid().buckets().array().forEach(geohexGridBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = geohexGridBucket.key();
                LatLonGeoLocation geoPoint = getH3Centre(element.keyAsString.toString()).latlon();
                element.key = new LatLon(geoPoint.lat(),geoPoint.lon());
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(AggregatedGeometryEnum::isCellOrCellCenterAgg)
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                if (g.isCellAgg()) {
                                    returnedGeometry.geometry = createPolygonFromH3(element.keyAsString.toString());
                                } else {
                                    returnedGeometry.geometry = new Point(geoPoint.lon(), geoPoint.lat());
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);

                            });
                }
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, geohexGridBucket);
            });
        } else if (aggregate.isDateHistogram()){
            aggregate.dateHistogram().buckets().array().forEach(dateHistogramBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = dateHistogramBucket.keyAsString();
                element.key = dateHistogramBucket.key();
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, dateHistogramBucket);
            });
        } else if (aggregate.isHistogram()) {
            aggregate.histogram().buckets().array().forEach(histogramBucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = histogramBucket.keyAsString();
                element.key = histogramBucket.key();
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, histogramBucket);
            });

        } else if (aggregate.isDterms()) {
            aggregate.dterms().buckets().array().forEach(bucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = bucket.keyAsString();
                element.key = bucket.key();
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, bucket);
        });
        } else if (aggregate.isLterms()) {
            aggregate.lterms().buckets().array().forEach(bucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = bucket.keyAsString();
                element.key = bucket.key();
                buildResponseFromBucket(element,aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, bucket);

            });
        } else if (aggregate.isSterms()) {
            aggregate.sterms().buckets().array().forEach(bucket -> {
                AggregationResponse element = new AggregationResponse();
                element.keyAsString = bucket.key().stringValue();
                element.key = bucket.key().stringValue();
                buildResponseFromBucket(element, aggregationResponse, collection, aggregationsRequest, aggTreeDepth, rawGeometries, aggregatedGeometries, bucket);
            });
        }
        return aggregationResponse;
    }

    private void buildResponseFromBucket(AggregationResponse element, AggregationResponse aggregationResponse, CollectionReference collection, List<Aggregation> aggregationsRequest, int aggTreeDepth, List<RawGeometry> rawGeometries, List<AggregatedGeometryEnum> aggregatedGeometries, MultiBucketBase bucket) {
        element.count = bucket.docCount();
        element.elements = new ArrayList<>();
        if (bucket.aggregations().size() == 0) {
            element.elements = null;
        } else {
            bucket.aggregations().forEach((key, subAgg) -> {
                AggregationResponse subAggregationResponse = new AggregationResponse();
                if (key.contains(TERM_AGG)) {
                    if (subAgg.isSterms()) {
                        subAggregationResponse.sumotherdoccounts = subAgg.sterms().sumOtherDocCount();
                    }
                    if (subAgg.isDterms()) {
                        subAggregationResponse.sumotherdoccounts = subAgg.dterms().sumOtherDocCount();
                    }
                    if (subAgg.isLterms()) {
                        subAggregationResponse.sumotherdoccounts = subAgg.lterms().sumOtherDocCount();
                    }
                }
                if (key.equals(FETCH_HITS_AGG)) {
                    subAggregationResponse = null;
                    element.hits = new ArrayList<>();
                    for (int i = 0; i < subAgg.topHits().hits().hits().size(); i++) {
                        element.hits.add(subAgg.topHits().hits().hits().get(i).source().to(Map.class));
                    }
                } else if (key.contains(TERM_AGG) || key.contains(DATEHISTOGRAM_AGG) || key.contains(HISTOGRAM_AGG)
                        || key.contains(GEOTILE_AGG) || key.contains(GEOHASH_AGG)) {
                    subAggregationResponse = formatAggregationResult(subAgg, subAggregationResponse, collection, aggregationsRequest, aggTreeDepth + 1);

                } else if (isAggregatedGeometry(key, aggregatedGeometries)) {
                    subAggregationResponse = null;
                    ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                    returnedGeometry.isRaw = false;
                    if (key.equals(AggregatedGeometryEnum.BBOX.value() + AGGREGATED_GEOMETRY_SUFFIX)) {
                        Polygon box = createBox(subAgg.geoBounds().bounds());
                        returnedGeometry.reference = AggregatedGeometryEnum.BBOX.value();
                        returnedGeometry.geometry = box;
                    } else if (key.equals(AggregatedGeometryEnum.CENTROID.value() + AGGREGATED_GEOMETRY_SUFFIX)) {
                        GeoLocation centroid = subAgg.geoCentroid().location();
                        GeoJsonObject g = new Point(centroid.latlon().lon(), centroid.latlon().lat());
                        returnedGeometry.reference = AggregatedGeometryEnum.CENTROID.value();
                        returnedGeometry.geometry = g;
                    }
                    if (element.geometries == null) {
                        element.geometries = new ArrayList<>();
                    }
                    element.geometries.add(returnedGeometry);
                } else if (key.startsWith(RAW_GEOMETRY_SUFFIX)) {
                    boolean includeFetchHits = key.contains(FETCH_HITS_AGG);
                    String sort = !includeFetchHits ? key.substring(RAW_GEOMETRY_SUFFIX.length()) :
                            key.substring(RAW_GEOMETRY_SUFFIX.length()+FETCH_HITS_AGG.length()) ;
                    subAggregationResponse = null;
                    long nbHits = subAgg.topHits().hits().total().value();

                    if (nbHits > 0) {
                        List<Hit<JsonData>> hits = subAgg.topHits().hits().hits();
                        if(includeFetchHits){
                            element.hits = new ArrayList<>();
                            for (int i = 0; i < hits.size(); i++) {
                                element.hits.add(hits.get(i).source().to(Map.class));
                            }
                        }
                        for (Hit<JsonData> hit: hits) {
                            JsonData source = hit.source();
                            if (rawGeometries != null) {
                                List<String> geometries;
                                if(includeFetchHits){
                                    geometries = rawGeometries.stream().filter(rg -> rg.signedSort.equals(sort)).map(rg -> rg.geometry).collect(Collectors.toList());
                                }else {
                                    geometries = rawGeometries.stream().filter(rg -> rg.sort.equals(sort)).map(rg -> rg.geometry).collect(Collectors.toList());
                                }                                geometries.forEach(g -> {
                                    GeoJsonObject geometryGeoJson;
                                    try {
                                        CollectionReferenceManager.setCollectionGeometriesType(source, collection, String.join(",", geometries));
                                        GeoTypeEnum geometryType;
                                        Object geometry = MapExplorer.getObjectFromPath(g, source);
                                        ReturnedGeometry returnedGeometry = null;
                                        if (geometry != null) {
                                            returnedGeometry = new ReturnedGeometry();
                                            geometryType = collection.params.getGeometryType(g);
                                            returnedGeometry.reference = g;
                                        } else {
                                            geometry = MapExplorer.getObjectFromPath(collection.params.centroidPath, source);
                                            geometryType = collection.params.getGeometryType(collection.params.centroidPath);
                                            if (geometry != null) {
                                                returnedGeometry = new ReturnedGeometry();
                                                returnedGeometry.reference = collection.params.centroidPath;
                                            }
                                        }
                                        geometryGeoJson = geometry != null ?
                                                GeoTypeMapper.getGeoJsonObject(geometry, geometryType) : null;
                                        if (geometryGeoJson != null) {
                                            // "old-style crs member is not recommended" so we remove it
                                            geometryGeoJson.setCrs(null);
                                            returnedGeometry.geometry = geometryGeoJson;
                                            returnedGeometry.isRaw = true;
                                            returnedGeometry.sort = sort;
                                            if (element.geometries == null) {
                                                element.geometries = new ArrayList<>();
                                            }
                                            element.geometries.add(returnedGeometry);
                                        }



                                    } catch (ArlasException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
                    if (bucket.aggregations().size() == 1) {
                        element.metrics = null;
                        element.elements = null;
                    }
                } else {
                    //metric
                    if (element.metrics == null) {
                        element.metrics = new ArrayList<>();
                    }
                    subAggregationResponse = null;
                    AggregationMetric aggregationMetric = new AggregationMetric();
                    aggregationMetric.type = key.split(":")[0];
                    if (!subAgg.isGeoBounds() && !subAgg.isGeoCentroid()) {
                        aggregationMetric.field = key.split(":")[1];
                        if (subAgg.isAvg()) {
                            aggregationMetric.value = subAgg.avg().value();
                        }
                        if (subAgg.isMax()) {
                            aggregationMetric.value = subAgg.max().value();
                        }
                        if (subAgg.isMin()) {
                            aggregationMetric.value = subAgg.min().value();
                        }
                        if (subAgg.isSum()) {
                            aggregationMetric.value = subAgg.sum().value();
                        }
                        if (subAgg.isCardinality()) {
                            aggregationMetric.value = (double) subAgg.cardinality().value();
                        }
                    } else {
                        FeatureCollection fc = new FeatureCollection();
                        Feature feature = new Feature();
                        aggregationMetric.field = key.split(":")[1];
                        if (subAgg.isGeoBounds()) {
                            GeoJsonObject g = createBox(subAgg.geoBounds().bounds());
                            feature.setGeometry(g);
                            fc.add(feature);
                        } else if (subAgg.isGeoCentroid()) {
                            GeoLocation centroid = subAgg.geoCentroid().location();
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

    private GeoLocation getH3Centre(String h3) {
        Pair<Double, Double> latLon = H3Util.getInstance().getCellCenterAsLatLon(h3);
        return GeoLocation.of(builder -> builder.latlon(builder1 -> builder1.lat(latLon.getLeft()).lon(latLon.getRight())));
    }

    private GeoLocation getTileCentre(BoundingBox bbox) {
        double lon = (bbox.getEast() + bbox.getWest()) / 2;
        double lat = (bbox.getNorth() + bbox.getSouth()) / 2;

        return GeoLocation.of(builder -> builder.latlon(builder1 -> builder1.lat(lat).lon(lon)));
    }

    private Polygon createBox(GeoBounds subAggregation) {
        double bottom = subAggregation.tlbr().bottomRight().latlon().lat();
        double top = subAggregation.tlbr().topLeft().latlon().lat();
        double right = subAggregation.tlbr().bottomRight().latlon().lon();
        double left = subAggregation.tlbr().topLeft().latlon().lon();

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
