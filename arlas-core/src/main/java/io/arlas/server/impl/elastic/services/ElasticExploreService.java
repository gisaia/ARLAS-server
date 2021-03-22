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

package io.arlas.server.impl.elastic.services;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.impl.elastic.core.ElasticDocument;
import io.arlas.server.impl.elastic.core.ElasticFluidSearch;
import io.arlas.server.impl.elastic.utils.ElasticClient;
import io.arlas.server.impl.elastic.utils.GeoTypeMapper;
import io.arlas.server.managers.CollectionReferenceManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.Link;
import io.arlas.server.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.model.enumerations.CollectionFunction;
import io.arlas.server.model.enumerations.ComputationEnum;
import io.arlas.server.model.enumerations.GeoTypeEnum;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.*;
import io.arlas.server.services.CollectionReferenceService;
import io.arlas.server.services.ExploreService;
import io.arlas.server.services.FluidSearchService;
import io.arlas.server.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.*;
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

import static io.arlas.server.services.FluidSearchService.*;

public class ElasticExploreService extends ExploreService {

    private static final String FEATURE_TYPE_KEY = "feature_type";
    private static final String FEATURE_TYPE_VALUE = "hit";
    private static final String FEATURE_GEOMETRY_PATH = "geometry_path";

    protected ElasticClient client;

    public ElasticExploreService(ElasticClient client, CollectionReferenceService collectionReferenceService, String baseUri, int arlasRestCacheTimeout) {
        super(baseUri, arlasRestCacheTimeout, collectionReferenceService);
        this.client = client;
    }

    public ElasticClient getClient() {
        return client;
    }

    @Override
    public FluidSearchService getFluidSearch() {
        return new ElasticFluidSearch().setClient(client);
    }

    @Override
    public Hits count(CollectionReference collectionReference,
                      FluidSearchService fluidSearch) throws ArlasException {
        SearchHits searchHits = ((ElasticFluidSearch) fluidSearch).exec().getHits();

        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = searchHits.getTotalHits().value;
        hits.nbhits = searchHits.getHits().length;
        return hits;
    }

    @Override
    public ComputationResponse compute(CollectionReference collectionReference,
                                       FluidSearchService fluidSearch,
                                       String field, ComputationEnum metric) throws ArlasException {
        SearchResponse response = ((ElasticFluidSearch) fluidSearch).exec();
        ComputationResponse computationResponse = new ComputationResponse();
        long startQueryTimestamp = System.nanoTime();
        computationResponse.field = field;
        computationResponse.metric = metric;
        computationResponse.totalnb = response.getHits().getTotalHits().value;
        List<org.elasticsearch.search.aggregations.Aggregation> aggregations = response.getAggregations().asList();
        computationResponse.value = null;
        computationResponse.geometry = null;

        if (computationResponse.totalnb > 0) {
            switch (metric) {
                case AVG:
                    computationResponse.value = ((Avg)aggregations.get(0)).getValue();
                    break;
                case CARDINALITY:
                    computationResponse.value = (double) ((Cardinality) aggregations.get(0)).getValue();
                    break;
                case MAX:
                    computationResponse.value = ((Max)aggregations.get(0)).getValue();
                    break;
                case MIN:
                    computationResponse.value = ((Min)aggregations.get(0)).getValue();
                    break;
                case SPANNING:
                    double min;
                    double max;
                    if (aggregations.get(0).getName().equals(FIELD_MIN_VALUE)) {
                        min = ((Min)aggregations.get(0)).getValue();
                        max = ((Max)aggregations.get(1)).getValue();
                    } else {
                        min = ((Min)aggregations.get(1)).getValue();
                        max = ((Max)aggregations.get(0)).getValue();
                    }
                    computationResponse.value = max - min;
                    break;
                case SUM:
                    computationResponse.value = ((Sum)aggregations.get(0)).getValue();
                    break;
                case GEOBBOX:
                    computationResponse.geometry = createBox(((GeoBounds)aggregations.get(0)));
                    break;
                case GEOCENTROID:
                    GeoPoint centroid = ((GeoCentroid) aggregations.get(0)).centroid();
                    computationResponse.geometry = new Point(centroid.getLon(), centroid.getLat());
                    break;
            }
        }

        computationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQueryTimestamp);
        return computationResponse;
    }

    @Override
    public Hits search(MixedRequest request, CollectionReference collectionReference, Boolean flat, UriInfo uriInfo, String method) throws ArlasException {
        UriInfoWrapper uriInfoUtil = new UriInfoWrapper(uriInfo, getBaseUri());
        SearchHits searchHits = getSearchHits(request, collectionReference);
        Search searchRequest  = (Search)request.basicRequest;
        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = searchHits.getTotalHits().value;
        hits.nbhits = searchHits.getHits().length;
        HashMap<String, Link> links = new HashMap<>();
        hits.hits = new ArrayList<>((int) hits.nbhits);
        List<SearchHit>searchHitList= Arrays.asList(searchHits.getHits());
        if(searchRequest.page != null && searchRequest.page.before != null ){
            Collections.reverse(searchHitList);
        }
        for (SearchHit hit : searchHitList) {
            hits.hits.add(new Hit(collectionReference, hit.getSourceAsMap(), searchRequest.returned_geometries, flat, false));
        }
        Link self = new Link();
        self.href = uriInfoUtil.getRequestUri();
        self.method = method;
        Link next = null;
        Link previous = null;
        int lastIndex = (int) hits.nbhits -1;
        String sortParam = searchRequest.page != null ? searchRequest.page.sort : null;
        String afterParam = searchRequest.page != null ? searchRequest.page.after : null;
        String beforeParam = searchRequest.page != null ? searchRequest.page.before : null;
        Integer sizeParam = searchRequest.page != null ? searchRequest.page.size : SEARCH_DEFAULT_PAGE_SIZE;
        String lastHitAfter = "";
        String firstHitAfter = "";
        if (lastIndex >= 0 && sizeParam == hits.nbhits && sortParam != null && (afterParam != null || sortParam.contains(collectionReference.params.idPath))) {
            next = new Link();
            next.method = method;
            // Use sorted value of last element return by ES to build after param of next & previous link
            lastHitAfter = Arrays.stream(searchHitList.get(lastIndex).getSortValues()).map(Object::toString).collect(Collectors.joining(","));
        }
        if (searchHitList.size()>0 && sortParam != null && (beforeParam != null || sortParam.contains(collectionReference.params.idPath))) {
            previous = new Link();
            previous.method = method;
            firstHitAfter = Arrays.stream(searchHitList.get(0).getSortValues()).map(Object::toString).collect(Collectors.joining(","));
        }

        switch (method){
            case"GET":
                links.put("self", self);
                if (next != null){
                    next.href = uriInfoUtil.getNextHref(lastHitAfter);
                    links.put("next", next);
                }
                if (previous != null){
                    previous.href = uriInfoUtil.getPreviousHref(firstHitAfter);
                    links.put("previous", previous);
                }
                break;
            case"POST":
                self.body = searchRequest;
                links.put("self", self);
                if (next != null){
                    Page nextPage = new Page();
                    Search search = new Search();
                    search.filter = searchRequest.filter;
                    search.form = searchRequest.form;
                    search.projection =searchRequest.projection;
                    search.returned_geometries = searchRequest.returned_geometries;
                    nextPage.sort=searchRequest.page.sort;
                    nextPage.size=searchRequest.page.size;
                    nextPage.from =searchRequest.page.from;
                    nextPage.after = lastHitAfter;
                    search.page = nextPage;
                    next.href = self.href;
                    next.body = search;
                    links.put("next", next);
                }
                if (previous != null){
                    Page previousPage = new Page();
                    Search search = new Search();
                    search.filter = searchRequest.filter;
                    search.form = searchRequest.form;
                    search.projection =searchRequest.projection;
                    search.returned_geometries = searchRequest.returned_geometries;
                    previousPage.sort=searchRequest.page.sort;
                    previousPage.size=searchRequest.page.size;
                    previousPage.from =searchRequest.page.from;
                    previousPage.before = firstHitAfter;
                    search.page = previousPage;
                    previous.href = self.href;
                    previous.body = search;
                    links.put("previous", previous);
                }
                break;
        }
        hits.links = links;
        return hits;
    }

    @Override
    public List<Map<String, Object>> searchAsRaw(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        return Arrays.stream(getSearchHits(request, collectionReference).getHits()).map(SearchHit::getSourceAsMap).collect(Collectors.toList());
    }

    private SearchHits getSearchHits(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        ElasticFluidSearch fluidSearch = (ElasticFluidSearch) getFluidSearch();
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        paginate(((Search) request.basicRequest).page, collectionReference, fluidSearch);
        applyProjection(((Search) request.basicRequest).projection, fluidSearch, request.columnFilter, collectionReference);
        return fluidSearch.exec().getHits();
    }

    @Override
    public FeatureCollection getFeatures(MixedRequest request, CollectionReference collectionReference, boolean flat) throws ArlasException {
        SearchHits searchHits = getSearchHits(request, collectionReference);
        Search searchRequest = (Search) request.basicRequest;
        FeatureCollection fc = new FeatureCollection();
        List<SearchHit> results = Arrays.asList(searchHits.getHits());
        if (searchRequest.page != null && searchRequest.page.before != null) {
            Collections.reverse(results);
        }
        for (SearchHit hit : results) {
            Map<String, Object> source = hit.getSourceAsMap();
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
        return fc;
    }

    private Feature getFeatureFromHit(Hit arlasHit, String path, GeoJsonObject geometry) {
        Feature feature = new Feature();

        // Setting geometry of geojson
        feature.setGeometry(geometry);

        // setting the properties of the geojson
        feature.setProperties(new HashMap<>(arlasHit.getDataAsMap()));

        // Setting the Metadata (md) in properties of geojson. Only id, timestamp and centroid are set in the MD. The geometry is already returned in the geojson.
        MD md = new MD();
        md.id = arlasHit.md.id;
        md.timestamp = arlasHit.md.timestamp;
        md.centroid = arlasHit.md.centroid;
        if (!arlasHit.isFlat()) {
            feature.setProperty(MD.class.getSimpleName().toLowerCase(), md);
        } else {
            feature.setProperty(MD.class.getSimpleName().toLowerCase(), md.toFlatString());
        }

        // Setting the feature type of the geojson
        feature.setProperty(FEATURE_TYPE_KEY, FEATURE_TYPE_VALUE);
        feature.setProperty(FEATURE_GEOMETRY_PATH, path);
        return feature;
    }

    @Override
    public Map<String, Object> getRawDoc(CollectionReference collectionReference, String identifier, String[] includes) throws ArlasException {
        return new ElasticDocument(client).getSource(collectionReference, identifier, includes);
    }

    private void paginate(Page page, CollectionReference collectionReference, ElasticFluidSearch fluidSearch) throws ArlasException {
        setPageSizeAndFrom(page, fluidSearch);
        searchAfterPage(page, collectionReference.params.idPath, fluidSearch);
        if (page != null) {
            if (page.before != null) {
                Page newPage = page;
                newPage.sort = Arrays.stream(page.sort.split(","))
                        .map(field -> field.startsWith("-") ? field.substring(1) : "-".concat(field)).collect(Collectors.joining(","));
                sortPage(newPage, fluidSearch);
            } else {
                sortPage(page, fluidSearch);
            }
        }
    }

    protected void setPageSizeAndFrom(Page page, ElasticFluidSearch fluidSearch) throws ArlasException {
        if (page != null) {
            if (page.size == null) {
                page.size = SEARCH_DEFAULT_PAGE_SIZE;
            }
            if (page.from == null) {
                page.from = SEARCH_DEFAULT_PAGE_FROM;
            }
            CheckParams.checkPageSize(page);
            CheckParams.checkPageFrom(page);
            fluidSearch.filterSize(page.size, page.from);
        }
    }

    protected void searchAfterPage(Page page, String idCollectionField, ElasticFluidSearch fluidSearch) throws ArlasException {
        if (page != null && page.after != null) {
            CheckParams.checkPageAfter(page, idCollectionField);
            fluidSearch = fluidSearch.searchAfter(page.after);
        }
        if (page != null && page.before != null) {
            CheckParams.checkPageAfter(page, idCollectionField);
            fluidSearch.searchAfter(page.before);
        }
    }

    @Override
    public AggregationResponse aggregate(CollectionReference collectionReference,
                                         List<Aggregation> aggregationsRequests,
                                         int aggTreeDepth,
                                         Long startQuery,
                                         FluidSearchService fluidSearch) throws ArlasException {
        SearchResponse response = ((ElasticFluidSearch) fluidSearch).exec();

        AggregationResponse aggregationResponse = new AggregationResponse();
        aggregationResponse.totalnb = response.getHits().getTotalHits().value;
        aggregationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);
        return formatAggregationResult((MultiBucketsAggregation) response.getAggregations().asList().get(0), aggregationResponse, collectionReference, aggregationsRequests, aggTreeDepth);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private AggregationResponse formatAggregationResult(MultiBucketsAggregation aggregation, AggregationResponse aggregationResponse,
                                                        CollectionReference collection, List<Aggregation> aggregationsRequest, int aggTreeDepth) {
        aggregationResponse.name = aggregation.getName();
        if (aggregationResponse.name.equals(TERM_AGG)) {
            aggregationResponse.sumotherdoccounts = ((Terms) aggregation).getSumOfOtherDocCounts();
        }
        List<RawGeometry> rawGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).rawGeometries : null;
        List<AggregatedGeometryEnum> aggregatedGeometries = aggregationsRequest.size() > aggTreeDepth ? aggregationsRequest.get(aggTreeDepth).aggregatedGeometries : null;
        aggregationResponse.elements = new ArrayList<>();
        List<MultiBucketsAggregation.Bucket> buckets = (List<MultiBucketsAggregation.Bucket>) aggregation.getBuckets();
        buckets.forEach(bucket -> {
            AggregationResponse element = new AggregationResponse();
            element.keyAsString = bucket.getKeyAsString();
            // if it is a `geohash` aggregation type, we set the GEOHASHCENTER and GEOHASH aggregated geometries if they're requested
            if (aggregationResponse.name.equals(GEOHASH_AGG)) {
                GeoPoint geoPoint = getGeohashCentre(element.keyAsString.toString());
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(g -> g == AggregatedGeometryEnum.TILECENTER || g == AggregatedGeometryEnum.TILE)
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                if (g == AggregatedGeometryEnum.TILE) {
                                    returnedGeometry.geometry = createPolygonFromRectangle(GeohashUtils.decodeBoundary(element.keyAsString.toString(), SpatialContext.GEO));
                                } else {
                                    returnedGeometry.geometry = new Point(geoPoint.getLon(), geoPoint.getLat());
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
                GeoPoint geoPoint = getTileCentre(tile);
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream()
                            .filter(g -> g == AggregatedGeometryEnum.TILECENTER || g == AggregatedGeometryEnum.TILE)
                            .forEach(g -> {
                                ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                                returnedGeometry.reference = g.value();
                                returnedGeometry.isRaw = false;
                                if (g == AggregatedGeometryEnum.TILE) {
                                    returnedGeometry.geometry = createBox(tile);
                                } else {
                                    returnedGeometry.geometry = new Point(geoPoint.getLon(), geoPoint.getLat());
                                }
                                if (element.geometries == null) {
                                    element.geometries = new ArrayList<>();
                                }
                                element.geometries.add(returnedGeometry);

                            });
                }
            } else if(aggregationResponse.name.startsWith(DATEHISTOGRAM_AGG)){
                element.key = ((ZonedDateTime)bucket.getKey()).withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli();
            } else {
                element.key = bucket.getKey();
            }
            element.count = bucket.getDocCount();
            element.elements = new ArrayList<>();
            if (bucket.getAggregations().asList().size() == 0) {
                element.elements = null;
            } else {
                bucket.getAggregations().forEach(subAggregation -> {
                    AggregationResponse subAggregationResponse = new AggregationResponse();
                    subAggregationResponse.name = subAggregation.getName();
                    if (subAggregationResponse.name.equals(TERM_AGG)) {
                        subAggregationResponse.sumotherdoccounts = ((Terms) subAggregation).getSumOfOtherDocCounts();
                    }
                    if (subAggregation.getName().equals(FETCH_HITS_AGG)) {
                        subAggregationResponse = null;
                        element.hits = Optional.ofNullable(((TopHits)subAggregation).getHits().getHits())
                                .map(Arrays::asList)
                                .map(hitsList -> hitsList.stream().map(SearchHit::getSourceAsMap).collect(Collectors.toList()))
                                .orElse(new ArrayList());
                    } else if (Arrays.asList(DATEHISTOGRAM_AGG, HISTOGRAM_AGG, TERM_AGG, GEOHASH_AGG, GEOTILE_AGG).contains(subAggregation.getName())) {
                        subAggregationResponse = formatAggregationResult(((MultiBucketsAggregation) subAggregation), subAggregationResponse, collection, aggregationsRequest, aggTreeDepth+1);
                    } else if (isAggregatedGeometry(subAggregation.getName(), aggregatedGeometries)) {
                        subAggregationResponse = null;
                        ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                        returnedGeometry.isRaw = false;
                        if (subAggregation.getName().equals(AggregatedGeometryEnum.BBOX.value() + AGGREGATED_GEOMETRY_SUFFIX)) {
                            Polygon box = createBox((GeoBounds) subAggregation);
                            returnedGeometry.reference = AggregatedGeometryEnum.BBOX.value();
                            returnedGeometry.geometry = box;
                        } else if (subAggregation.getName().equals(AggregatedGeometryEnum.CENTROID.value() + AGGREGATED_GEOMETRY_SUFFIX)) {
                            GeoPoint centroid = ((GeoCentroid) subAggregation).centroid();
                            GeoJsonObject g = new Point(centroid.getLon(), centroid.getLat());
                            returnedGeometry.reference = AggregatedGeometryEnum.CENTROID.value();
                            returnedGeometry.geometry = g;
                        }
                        if (element.geometries == null) {
                            element.geometries = new ArrayList<>();
                        }
                        element.geometries.add(returnedGeometry);
                    } else if (subAggregation.getName().startsWith(RAW_GEOMETRY_SUFFIX)) {
                        String sort = subAggregation.getName().substring(RAW_GEOMETRY_SUFFIX.length());
                        subAggregationResponse = null;
                        long nbHits = ((TopHits) subAggregation).getHits().getTotalHits().value;
                        if (nbHits > 0) {
                            SearchHit[] hits = ((TopHits) subAggregation).getHits().getHits();
                            for (SearchHit hit: hits) {
                                Map source = hit.getSourceAsMap();
                                if (rawGeometries != null) {
                                    List<String> geometries = rawGeometries.stream().filter(rg -> rg.sort.equals(sort)).map(rg -> rg.geometry).collect(Collectors.toList());
                                    geometries.forEach(g -> {
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
                        if (bucket.getAggregations().asList().size() == 1) {
                            element.metrics = null;
                            element.elements = null;
                        }
                    } else {
                        if (element.metrics == null) {
                            element.metrics = new ArrayList<>();
                        }
                        subAggregationResponse = null;
                        AggregationMetric aggregationMetric = new AggregationMetric();
                        aggregationMetric.type = subAggregation.getName().split(":")[0];
                        aggregationMetric.field = subAggregation.getName().split(":")[1];
                        if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                            aggregationMetric.value = (((NumericMetricsAggregation.SingleValue) subAggregation).value());
                        } else {
                            FeatureCollection fc = new FeatureCollection();
                            Feature feature = new Feature();
                            if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                GeoJsonObject g = createBox((GeoBounds) subAggregation);
                                feature.setGeometry(g);
                                fc.add(feature);
                            } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                GeoPoint centroid = ((GeoCentroid) subAggregation).centroid();
                                GeoJsonObject g = new Point(centroid.getLon(), centroid.getLat());
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
        return aggregationResponse;
    }


    private boolean isAggregatedGeometry(String subName, List<AggregatedGeometryEnum> geometries) {
        if(!CollectionUtils.isEmpty(geometries)) {
            return geometries.stream().map(g -> g.value() + AGGREGATED_GEOMETRY_SUFFIX).anyMatch(g -> g.equals(subName));
        }
        return false;
    }

    private GeoPoint getGeohashCentre(String geohash) {
        Rectangle bbox = GeohashUtils.decodeBoundary(geohash, SpatialContext.GEO);

        Double maxLon = bbox.getMaxX();
        Double minLon = bbox.getMinX();
        double lon = (maxLon + minLon) / 2;

        Double maxLat = bbox.getMaxY();
        Double minLat = bbox.getMinY();
        double lat = (maxLat + minLat) / 2;

        return new GeoPoint(lat, lon);
    }

    private GeoPoint getTileCentre(BoundingBox bbox) {
        double lon = (bbox.getEast() + bbox.getWest()) / 2;
        double lat = (bbox.getNorth() + bbox.getSouth()) / 2;

        return new GeoPoint(lat, lon);
    }

    private Polygon createBox(BoundingBox bbox) {
        Polygon box = new Polygon();
        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(bbox.getWest(), bbox.getNorth()));
        bounds.add(new LngLatAlt(bbox.getEast(), bbox.getNorth()));
        bounds.add(new LngLatAlt(bbox.getEast(), bbox.getSouth()));
        bounds.add(new LngLatAlt(bbox.getWest(), bbox.getSouth()));
        box.add(bounds);

        return box;
    }

    private Polygon createBox(GeoBounds subAggregation) {
        Polygon box = new Polygon();
        GeoPoint topLeft = subAggregation.topLeft();
        GeoPoint bottomRight = subAggregation.bottomRight();
        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(topLeft.getLon(), topLeft.getLat()));
        bounds.add(new LngLatAlt(bottomRight.getLon(), topLeft.getLat()));
        bounds.add(new LngLatAlt(bottomRight.getLon(), bottomRight.getLat()));
        bounds.add(new LngLatAlt(topLeft.getLon(), bottomRight.getLat()));
        box.add(bounds);

        return box;
    }

    private Polygon createPolygonFromRectangle(Rectangle rectangle) {
        Polygon polygon = new Polygon();
        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(rectangle.getMinX(), rectangle.getMaxY()));
        bounds.add(new LngLatAlt(rectangle.getMaxX(), rectangle.getMaxY()));
        bounds.add(new LngLatAlt(rectangle.getMaxX(), rectangle.getMinY()));
        bounds.add(new LngLatAlt(rectangle.getMinX(), rectangle.getMinY()));
        polygon.add(bounds);
        return polygon;
    }
}
