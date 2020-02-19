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

package io.arlas.server.services;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.managers.CollectionReferenceManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.Link;
import io.arlas.server.model.enumerations.CollectionFunction;
import io.arlas.server.model.enumerations.ComputationEnum;
import io.arlas.server.model.enumerations.GeoTypeEnum;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.*;
import io.arlas.server.model.enumerations.*;
import io.arlas.server.model.response.AggregationMetric;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.ComputationResponse;

import io.arlas.server.model.response.ReturnedGeometry;
import io.arlas.server.utils.GeoTypeMapper;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ResponseCacheManager;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExploreServices {

    public static final Integer SEARCH_DEFAULT_PAGE_SIZE = 10;
    public static final Integer SEARCH_DEFAULT_PAGE_FROM = 0;

    private static final String FEATURE_TYPE_KEY = "feature_type";
    private static final String FEATURE_TYPE_VALUE = "hit";
    private static final String FEATURE_GEOMETRY_PATH = "geometry_path";

    protected ElasticClient client;
    protected CollectionReferenceDao daoCollectionReference;
    private ResponseCacheManager responseCacheManager = null;
    private ArlasServerConfiguration configuration;
    private ElasticAdmin elasticAdmin;

    public ExploreServices() {}

    public ExploreServices(ElasticClient client, ArlasServerConfiguration configuration) {
        this.client = client;
        this.elasticAdmin = new ElasticAdmin(client);
        this.configuration = configuration;
        this.daoCollectionReference = new ElasticCollectionReferenceDaoImpl(client, configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
        this.responseCacheManager = new ResponseCacheManager(configuration.arlasrestcachetimeout);
    }

    public String getBaseUri() {
        String baseUri = null;
        if (configuration.arlasBaseUri != null) {
            baseUri =  configuration.arlasBaseUri;
        }
        return baseUri;
    }

    public ElasticClient getClient() {
        return client;
    }

    public void setClient(ElasticClient client) {
        this.client = client;
    }

    public ElasticAdmin getElasticAdmin() {
        return elasticAdmin;
    }

    public CollectionReferenceDao getDaoCollectionReference() {
        return daoCollectionReference;
    }

    public ResponseCacheManager getResponseCacheManager() {
        return responseCacheManager;
    }

    public Hits count(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        SearchHits searchHits = fluidSearch.exec().getHits();

        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = searchHits.getTotalHits().value;
        hits.nbhits = searchHits.getHits().length;
        return hits;

    }

    public SearchHits search(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        paginate(((Search) request.basicRequest).page, collectionReference, fluidSearch);
        applyProjection(((Search) request.basicRequest).projection, fluidSearch, request.columnFilter, collectionReference);
        return fluidSearch.exec().getHits();
    }


    public SearchResponse aggregate(MixedRequest request, CollectionReference collectionReference, Boolean isGeoAggregation) throws ArlasException {
        CheckParams.checkAggregationRequest(request.basicRequest, collectionReference);
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        applyAggregation(((AggregationsRequest) request.basicRequest).aggregations, fluidSearch, isGeoAggregation);
        return fluidSearch.exec();
    }

    public ComputationResponse compute(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        CheckParams.checkComputationRequest(request.basicRequest, collectionReference);
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        String field = ((ComputationRequest)request.basicRequest).field;
        ComputationEnum metric = ((ComputationRequest)request.basicRequest).metric;
        fluidSearch = fluidSearch.compute(field, metric);
        SearchResponse response = fluidSearch.exec();
        ComputationResponse computationResponse = new ComputationResponse();
        Long startQueryTimestamp = System.nanoTime();
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
                    computationResponse.value = new Double(((Cardinality)aggregations.get(0)).getValue());
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
                    if (aggregations.get(0).getName().equals(FluidSearch.FIELD_MIN_VALUE)) {
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

    public Hits getSearchHits(MixedRequest request, CollectionReference collectionReference, Boolean flat, UriInfo uriInfo, String method) throws ArlasException {
        UriInfoWrapper uriInfoUtil = new UriInfoWrapper(uriInfo, getBaseUri());
        SearchHits searchHits = search(request, collectionReference);
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
        Integer sizeParam = searchRequest.page != null ? searchRequest.page.size : ExploreServices.SEARCH_DEFAULT_PAGE_SIZE;
        String lastHitAfter = "";
        String firstHitAfter = "";
        if (lastIndex >= 0 && sizeParam == hits.nbhits && sortParam != null && (afterParam != null || sortParam.contains(collectionReference.params.idPath))) {
            next = new Link();
            next.method = method;
            // Use sorted value of last element return by ES to build after param of next & previous link
            lastHitAfter = Arrays.stream(searchHitList.get(lastIndex).getSortValues()).map(value->value.toString()).collect(Collectors.joining(","));
        }
        if (searchHitList.size()>0 && sortParam != null && (beforeParam != null || sortParam.contains(collectionReference.params.idPath))) {
            previous = new Link();
            previous.method = method;
            firstHitAfter = Arrays.stream(searchHitList.get(0).getSortValues()).map(value->value.toString()).collect(Collectors.joining(","));
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

    public FeatureCollection getFeatures(CollectionReference collectionReference, MixedRequest request, boolean flat) throws ArlasException {
        SearchHits searchHits = search(request, collectionReference);
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

        /** Setting geometry of geojson */
        feature.setGeometry(geometry);

        /** setting the properties of the geojson */
        feature.setProperties(new HashMap<>(arlasHit.getDataAsMap()));

        /** Setting the Metadata (md) in properties of geojson.
         * Only id, timestamp and centroid are set in the MD. The geometry is already returned in the geojson.*/
        MD md = new MD();
        md.id = arlasHit.md.id;
        md.timestamp = arlasHit.md.timestamp;
        md.centroid = arlasHit.md.centroid;
        feature.setProperty(MD.class.getSimpleName().toLowerCase(), md);

        /** Setting the feature type of the geojson */
        feature.setProperty(FEATURE_TYPE_KEY, FEATURE_TYPE_VALUE);
        feature.setProperty(FEATURE_GEOMETRY_PATH, path);
        return feature;
    }

    public RangeResponse getFieldRange(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        Long startQuery = System.nanoTime();
        CheckParams.checkRangeRequestField(request.basicRequest);
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        applyRangeRequest(((RangeRequest) request.basicRequest).field, fluidSearch);
        SearchResponse response;
        try {
            response = fluidSearch.exec();
        } catch (SearchPhaseExecutionException e) {
            throw new InvalidParameterException("The field's type must be numeric");
        }

        org.elasticsearch.search.aggregations.Aggregation firstAggregation = response.getAggregations().asList().get(0);
        org.elasticsearch.search.aggregations.Aggregation secondAggregation = response.getAggregations().asList().get(1);

        RangeResponse rangeResponse = new RangeResponse();
        rangeResponse.totalnb = response.getHits().getTotalHits().value;
        if (rangeResponse.totalnb > 0) {
            if (firstAggregation.getName().equals(FluidSearch.FIELD_MIN_VALUE)) {
                rangeResponse.min = ((Min)firstAggregation).getValue();
                rangeResponse.max = ((Max)secondAggregation).getValue();
            } else {
                rangeResponse.min = ((Min)secondAggregation).getValue();
                rangeResponse.max = ((Max)firstAggregation).getValue();
            }
            CheckParams.checkRangeFieldExists(rangeResponse);
        } else {
            rangeResponse.min = rangeResponse.max = null;
        }

        rangeResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);

        return rangeResponse;
    }

    protected void applyAggregation(List<Aggregation> aggregations, FluidSearch fluidSearch, Boolean isGeoAggregation) throws ArlasException {
        if (aggregations != null && aggregations != null && !aggregations.isEmpty()) {
            fluidSearch = fluidSearch.aggregate(aggregations, isGeoAggregation);
        }
    }

    protected void applyRangeRequest(String field, FluidSearch fluidSearch) throws ArlasException {
        fluidSearch = fluidSearch.getFieldRange(field);
    }

    public void applyFilter(Filter filter, FluidSearch fluidSearch) throws ArlasException {
        if (filter != null) {
            CheckParams.checkFilter(filter);
            if (filter.f != null && !filter.f.isEmpty()) {
                CollectionReference collectionReference = fluidSearch.getCollectionReference();
                if (!filterFHasDateQuery(filter, collectionReference) && !StringUtil.isNullOrEmpty(filter.dateformat)) {
                    throw new BadRequestException("dateformat is specified but no date field is queried in f filter (gt, lt, gte, lte or range operations)");
                }
                for (MultiValueFilter<Expression> f : filter.f) {
                    fluidSearch = fluidSearch.filter(f, filter.dateformat);
                }
            }
            if (filter.q != null && !filter.q.isEmpty()) {
                for (MultiValueFilter<String> q : filter.q) {
                    fluidSearch = fluidSearch.filterQ(q);
                }
            }
        }
    }

    public void setValidGeoFilters(CollectionReference collectionReference, Request request) throws ArlasException {
        if (request != null && request.filter != null) {
            request.filter = ParamsParser.getFilterWithValidGeos(collectionReference, request.filter);
        }
    }

    /**
     * This method checks whether in all the expressions of the filter `f`, a date field has been queried using `lte`, `gte`, `lt`, `gt` or `range` operations
     * **/
    protected boolean filterFHasDateQuery(Filter filter, CollectionReference collectionReference) {
        return filter.f.stream()
                .anyMatch(expressions -> expressions
                        .stream()
                        .filter(expression -> expression.op == OperatorEnum.gt || expression.op == OperatorEnum.lt || expression.op == OperatorEnum.gte || expression.op == OperatorEnum.lte || expression.op == OperatorEnum.range)
                        .anyMatch(expression -> {
                            try {
                                return ElasticTool.isDateField(ParamsParser.getFieldFromFieldAliases(expression.field, collectionReference), client, collectionReference.params.indexName);
                            } catch (ArlasException e) {
                                throw new RuntimeException(e);
                            }
                        })
                );
    }

    protected void paginate(Page page, CollectionReference collectionReference, FluidSearch fluidSearch) throws ArlasException {
        setPageSizeAndFrom(page, fluidSearch);
        searchAfterPage(page, collectionReference.params.idPath, fluidSearch);
        if(page!=null){
            if(page.before != null){
                Page newPage = page;
                newPage.sort = Arrays.stream(page.sort.split(","))
                        .map(field -> field.startsWith("-") ? field.substring(1) : "-".concat(field)).collect(Collectors.joining(","));
                sortPage(newPage, fluidSearch);
            }else{
                sortPage(page, fluidSearch);
            }
        }
    }

    protected void setPageSizeAndFrom(Page page, FluidSearch fluidSearch) throws ArlasException {
        if (page != null) {
            if (page.size == null) {
                page.size = SEARCH_DEFAULT_PAGE_SIZE;
            }
            if (page.from == null) {
                page.from = SEARCH_DEFAULT_PAGE_FROM;
            }
            CheckParams.checkPageSize(page);
            CheckParams.checkPageFrom(page);
            fluidSearch = fluidSearch.filterSize(page.size, page.from);
        }
    }

    protected void searchAfterPage(Page page, String idCollectionField, FluidSearch fluidSearch) throws ArlasException {
        if (page != null && page.after != null) {
            CheckParams.checkPageAfter(page, idCollectionField);
            fluidSearch = fluidSearch.searchAfter(page.after);
        }
        if (page != null && page.before != null) {
            CheckParams.checkPageAfter(page, idCollectionField);
            fluidSearch = fluidSearch.searchAfter(page.before);
        }
    }

    protected void sortPage(Page page, FluidSearch fluidSearch) throws ArlasException {
        if (page != null && page.sort != null) {
            fluidSearch = fluidSearch.sort(page.sort);
        }
    }

    protected void applyProjection(Projection projection, FluidSearch fluidSearch, Optional<String> columnFilter, CollectionReference collectionReference) throws ArlasException {
        if (ColumnFilterUtil.isValidColumnFilterPresent(columnFilter)) {
            String filteredIncludes = ColumnFilterUtil.getFilteredIncludes(columnFilter, projection, elasticAdmin.getCollectionFields(collectionReference, columnFilter))
                    .orElse(
                            // if filteredIncludes were to be null or an empty string, FluidSearch would then build a bad request
                            String.join(",", ColumnFilterUtil.getCollectionMandatoryPaths(collectionReference)));
            fluidSearch = fluidSearch.include(filteredIncludes);

        } else if (projection != null && !Strings.isNullOrEmpty(projection.includes)) {
            fluidSearch = fluidSearch.include(projection.includes);
        }

        if (projection != null && !Strings.isNullOrEmpty(projection.excludes)) {
            fluidSearch = fluidSearch.exclude(projection.excludes);
        }
    }

    public AggregationResponse formatAggregationResult(SearchResponse response, String collection, Long startQuery) {
        AggregationResponse aggregationResponse = new AggregationResponse();
        aggregationResponse.totalnb = response.getHits().getTotalHits().value;
        aggregationResponse.queryTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startQuery);
        return formatAggregationResult((MultiBucketsAggregation) response.getAggregations().asList().get(0), aggregationResponse, collection);
    }

    public AggregationResponse formatAggregationResult(MultiBucketsAggregation aggregation, AggregationResponse aggregationResponse, CollectionReference collection, List<Aggregation> aggregationsRequest, int i) {
        aggregationResponse.name = aggregation.getName();
        if (aggregationResponse.name.equals(FluidSearch.TERM_AGG)) {
            aggregationResponse.sumotherdoccounts = ((Terms) aggregation).getSumOfOtherDocCounts();
        }
        RawGeometries rawGeometries = aggregationsRequest.size() > i ? aggregationsRequest.get(i).rawGeometries : null;
        List<AggregatedGeometryEnum> aggregatedGeometries = aggregationsRequest.size() > i ? aggregationsRequest.get(i).aggregatedGeometries : null;
        aggregationResponse.elements = new ArrayList<AggregationResponse>();
        List<MultiBucketsAggregation.Bucket> buckets = (List<MultiBucketsAggregation.Bucket>) aggregation.getBuckets();
        buckets.forEach(bucket -> {
            AggregationResponse element = new AggregationResponse();
            element.keyAsString = bucket.getKeyAsString();
            /** if it is a `geohash` aggregation type, we set the GEOHASHCENTER and GEOHASH aggregated geometries if they're requested **/
            if (aggregationResponse.name.equals(FluidSearch.GEOHASH_AGG)) {
                GeoPoint geoPoint = getGeohashCentre(element.keyAsString.toString());
                element.key = geoPoint;
                if (!CollectionUtils.isEmpty(aggregatedGeometries)) {
                    aggregatedGeometries.stream().filter(g -> g == AggregatedGeometryEnum.GEOHASHCENTER || g == AggregatedGeometryEnum.GEOHASH).forEach(g -> {
                        ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                        returnedGeometry.reference = g.value();
                        returnedGeometry.isRaw = false;
                        if (g == AggregatedGeometryEnum.GEOHASH) {
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
            } else if(aggregationResponse.name.startsWith(FluidSearch.DATEHISTOGRAM_AGG)){
                element.key = ((ZonedDateTime)bucket.getKey()).withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli();
            } else {
                element.key = bucket.getKey();
            }
            element.count = bucket.getDocCount();
            element.elements = new ArrayList<AggregationResponse>();
            if (bucket.getAggregations().asList().size() == 0) {
                element.elements = null;
                aggregationResponse.elements.add(element);
            } else {
                bucket.getAggregations().forEach(subAggregation -> {
                    AggregationResponse subAggregationResponse = new AggregationResponse();
                    subAggregationResponse.name = subAggregation.getName();
                    if (subAggregationResponse.name.equals(FluidSearch.TERM_AGG)) {
                        subAggregationResponse.sumotherdoccounts = ((Terms) subAggregation).getSumOfOtherDocCounts();
                    }
                    if (subAggregation.getName().equals(FluidSearch.FETCH_HITS_AGG)) {
                        subAggregationResponse = null;
                        element.hits = Optional.ofNullable(((TopHits)subAggregation).getHits().getHits())
                                .map(hitsArray -> Arrays.asList(hitsArray))
                                .map(hitsList -> hitsList.stream().map(hit -> hit.getSourceAsMap()).collect(Collectors.toList()))
                                .orElse(new ArrayList());
                    } else if (Arrays.asList(FluidSearch.DATEHISTOGRAM_AGG, FluidSearch.HISTOGRAM_AGG, FluidSearch.TERM_AGG, FluidSearch.GEOHASH_AGG).contains(subAggregation.getName())) {
                        subAggregationResponse = formatAggregationResult(((MultiBucketsAggregation) subAggregation), subAggregationResponse, collection, aggregationsRequest, i+1);
                    } else if (isAggregatedGeometry(subAggregation.getName(), aggregatedGeometries)) {
                        subAggregationResponse = null;
                        ReturnedGeometry returnedGeometry = new ReturnedGeometry();
                        returnedGeometry.isRaw = false;
                        if (subAggregation.getName().equals(AggregatedGeometryEnum.BBOX.value() + FluidSearch.AGGREGATED_GEOMETRY_SUFFIX)) {
                            Polygon box = createBox((GeoBounds) subAggregation);
                            returnedGeometry.reference = AggregatedGeometryEnum.BBOX.value();
                            returnedGeometry.geometry = box;
                        } else if (subAggregation.getName().equals(AggregatedGeometryEnum.CENTROID.value() + FluidSearch.AGGREGATED_GEOMETRY_SUFFIX)) {
                            GeoPoint centroid = ((GeoCentroid) subAggregation).centroid();
                            GeoJsonObject g = new Point(centroid.getLon(), centroid.getLat());
                            returnedGeometry.reference = AggregatedGeometryEnum.CENTROID.value();
                            returnedGeometry.geometry = g;
                        }
                        if (element.geometries == null) {
                            element.geometries = new ArrayList<>();
                        }
                        element.geometries.add(returnedGeometry);
                    } else if (subAggregation.getName().equals(FluidSearch.RAW_GEOMETRY_SUFFIX)) {
                        subAggregationResponse = null;
                        long nbHits = ((TopHits) subAggregation).getHits().getTotalHits().value;
                        if (nbHits > 0) {
                            List<SearchHit> hits = Arrays.asList(((TopHits) subAggregation).getHits().getHits());
                            for (SearchHit hit: hits) {
                                Map source = hit.getSourceAsMap();
                                if (rawGeometries != null && !CollectionUtils.isEmpty(rawGeometries.geometries)) {
                                    rawGeometries.geometries.forEach(g -> {
                                        GeoJsonObject geometryGeoJson = null;
                                        try {
                                            CollectionReferenceManager.setCollectionGeometriesType(source, collection, rawGeometries.geometries.stream().collect(Collectors.joining(",")));
                                            GeoTypeEnum geometryType = null;
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
                        if (!aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase()) && !aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                            aggregationMetric.field = subAggregation.getName().split(":")[1];
                            aggregationMetric.value = (((NumericMetricsAggregation.SingleValue) subAggregation).value());
                        } else {
                            FeatureCollection fc = new FeatureCollection();
                            Feature feature = new Feature();
                            if (aggregationMetric.type.equals(CollectionFunction.GEOBBOX.name().toLowerCase())) {
                                Polygon box = createBox((GeoBounds) subAggregation);
                                GeoJsonObject g = box;
                                feature.setGeometry(g);
                                fc.add(feature);
                            } else if (aggregationMetric.type.equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                                GeoPoint centroid = ((GeoCentroid) subAggregation).centroid();
                                GeoJsonObject g = new Point(centroid.getLon(), centroid.getLat());
                                feature.setGeometry(g);
                                fc.add(feature);
                            }
                            aggregationMetric.field = collection.params.centroidPath;
                            aggregationMetric.value = fc;
                        }
                        element.metrics.add(aggregationMetric);
                    }
                    if (subAggregationResponse != null) {
                        element.elements.add(subAggregationResponse);
                    }
                });
                aggregationResponse.elements.add(element);
            }
        });
        return aggregationResponse;
    }


    public Map<String, Object> flat(AggregationResponse element, Function<Map<List<String>, Object>, Map<String, Object>> keyStringifier, Predicate<String> keyPartFiler) {
        Map<List<String>, Object> flatted = new HashMap<>();
        flat(flatted, element, new ArrayList<>());
        return keyStringifier.apply(flatted.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().stream().filter(keyPartFiler).collect(Collectors.toList()), e -> e.getValue())));
    }

    public Map<String, Object> flat(AggregationResponse element, Function<Map<List<String>, Object>, Map<String, Object>> keyStringifier) {
        Map<List<String>, Object> flatted = new HashMap<>();
        flat(flatted, element, new ArrayList<>());
        return keyStringifier.apply(flatted);
    }

    private boolean isAggregatedGeometry(String subName, List<AggregatedGeometryEnum> geometries) {
        if(!CollectionUtils.isEmpty(geometries)) {
            long n = geometries.stream().map(g -> g.value() + FluidSearch.AGGREGATED_GEOMETRY_SUFFIX).filter(g -> g.equals(subName)).count();
            return n > 0;
        }
        return false;
    }

    private void flat(Map<List<String>, Object> flat, AggregationResponse element, List<String> keyParts) {
        addToFlat(flat, keyParts, "count", element.count);
        addToFlat(flat, keyParts, "key", element.key);
        addToFlat(flat, keyParts, "key_as_string", element.keyAsString);
        addToFlat(flat, keyParts, "name", element.name);
        addToFlat(flat, keyParts, "query_time", element.queryTime);
        addToFlat(flat, keyParts, "sumotherdoccounts", element.sumotherdoccounts);
        addToFlat(flat, keyParts, "totalnb", element.totalnb);
        addToFlat(flat, keyParts, "totalTime", element.totalTime);
        if (element.metrics != null) {
            element.metrics.forEach(metric -> addToFlat(flat, newKeyParts(newKeyParts(keyParts, metric.field), metric.type), "", metric.value));
        }
        int idx = 0;
        if (element.elements != null) {
            for (AggregationResponse subElement : element.elements) {
                flat(flat, subElement, newKeyParts(newKeyParts(keyParts, "elements"), "" + (idx++)));
            }
        }
    }

    private void addToFlat(Map<List<String>, Object> flat, List<String> keyParts, String key, Object value) {
        if (value != null) {
            flat.put(newKeyParts(keyParts, key), value);
        }
    }

    private List<String> newKeyParts(List<String> keyParts, String key) {
        List<String> newOne = new ArrayList<>(keyParts);
        newOne.add(key);
        return newOne;
    }

    private GeoPoint getGeohashCentre(String geohash) {
        Rectangle bbox = GeohashUtils.decodeBoundary(geohash, SpatialContext.GEO);

        Double maxLon = bbox.getMaxX();
        Double minLon = bbox.getMinX();
        Double lon = (maxLon + minLon) / 2;

        Double maxLat = bbox.getMaxY();
        Double minLat = bbox.getMinY();
        Double lat = (maxLat + minLat) / 2;

        return new GeoPoint(lat, lon);
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
