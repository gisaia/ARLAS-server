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
package io.arlas.server.core.services;

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.exceptions.BadRequestException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.*;
import io.arlas.server.core.utils.*;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.*;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.locationtech.spatial4j.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class ExploreService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ExploreService.class);

    public static final Integer SEARCH_DEFAULT_PAGE_SIZE = 10;
    public static final Integer SEARCH_DEFAULT_PAGE_FROM = 0;
    private static final String FEATURE_TYPE_KEY = "feature_type";
    private static final String FEATURE_TYPE_VALUE = "hit";
    private static final String FEATURE_GEOMETRY_PATH = "geometry_path";


    private String baseUri;
    protected CollectionReferenceService collectionReferenceService;
    protected ResponseCacheManager responseCacheManager;

    public ExploreService() {
    }

    public ExploreService(String baseUri, int arlasRestCacheTimeout, CollectionReferenceService collectionReferenceService) {
        this.baseUri = baseUri;
        this.responseCacheManager = new ResponseCacheManager(arlasRestCacheTimeout);
        this.collectionReferenceService = collectionReferenceService;
    }

    public ResponseCacheManager getResponseCacheManager() {
        return this.responseCacheManager;
    }

    public String getBaseUri() {
        return this.baseUri;
    }

    public void setValidGeoFilters(CollectionReference collectionReference, Request request) throws ArlasException {
        if (request != null && request.filter != null) {
            request.filter = ParamsParser.getFilterWithValidGeos(collectionReference, request.filter);
        }
    }

    public Map<String, Object> flat(AggregationResponse element,
                                    Function<Map<List<String>, Object>, Map<String, Object>> keyStringifier,
                                    Predicate<String> keyPartFiler) {
        Map<List<String>, Object> flatted = new HashMap<>();
        flat(flatted, element, new ArrayList<>());
        return keyStringifier.apply(flatted.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().stream().filter(keyPartFiler).collect(Collectors.toList()), Map.Entry::getValue)));
    }

    private void flat(Map<List<String>, Object> flat, AggregationResponse element, List<String> keyParts) {
        addToFlat(flat, keyParts, "count", element.count);
        if (element.key !=  null) {
            addToFlat(flat, keyParts, "key", element.key.toString());
        }
        addToFlat(flat, keyParts, "key_as_string", element.keyAsString);
        addToFlat(flat, keyParts, "name", element.name);
        addToFlat(flat, keyParts, "query_time", element.queryTime);
        addToFlat(flat, keyParts, "sumotherdoccounts", element.sumotherdoccounts);
        addToFlat(flat, keyParts, "totalnb", element.totalnb);
        addToFlat(flat, keyParts, "totalTime", element.totalTime);
        if (element.metrics != null) {
            element.metrics.forEach(metric -> addToFlat(flat, newKeyParts(newKeyParts(keyParts, metric.field), metric.type), "", metric.value));
        }
        if (element.hits != null) {
            int i = 0;
            for (Object hit : element.hits) {
                Map flatHit = MapExplorer.flat(hit,new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), new HashSet<>());
                for (Object k: flatHit.keySet()) {
                    addToFlat(flat, newKeyParts(newKeyParts(keyParts, "hits"), i + "" ), k.toString(), flatHit.get(k).toString());
                }
                i++;
            }
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

    public void applyFilter(Filter filter, FluidSearchService fluidSearch) throws ArlasException {
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

    /**
     * This method checks whether in all the expressions of the filter `f`, a date field has been queried using `lte`, `gte`, `lt`, `gt` or `range` operations
     * **/
    private boolean filterFHasDateQuery(Filter filter, CollectionReference collectionReference) {
        return filter.f.stream()
                .anyMatch(expressions -> expressions
                        .stream()
                        .filter(expression -> expression.op == OperatorEnum.gt
                                || expression.op == OperatorEnum.lt
                                || expression.op == OperatorEnum.gte
                                || expression.op == OperatorEnum.lte
                                || expression.op == OperatorEnum.range)
                        .anyMatch(expression -> {
                            try {
                                return collectionReferenceService.isDateField(
                                        ParamsParser.getFieldFromFieldAliases(expression.field, collectionReference),
                                        collectionReference.params.indexName);
                            } catch (ArlasException e) {
                                throw new RuntimeException(e);
                            }
                        })
                );
    }

    protected Feature getFeatureFromHit(Hit arlasHit, String path, GeoJsonObject geometry) {
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

    protected void sortPage(Page page, FluidSearchService fluidSearch) throws ArlasException {
        if (page != null && page.sort != null) {
            fluidSearch.sort(page.sort);
        }
    }

    protected void applyProjection(Projection projection, FluidSearchService fluidSearch, Optional<String> columnFilter, CollectionReference collectionReference) throws ArlasException {
        if (ColumnFilterUtil.isValidColumnFilterPresent(columnFilter)) {
            String filteredIncludes = ColumnFilterUtil.getFilteredIncludes(columnFilter, projection, collectionReferenceService.getCollectionFields(collectionReference, columnFilter))
                    .orElse(
                            // if filteredIncludes were to be null or an empty string, FluidSearch would then build a bad request
                            String.join(",", ColumnFilterUtil.getCollectionMandatoryPaths(collectionReference)));
            fluidSearch = fluidSearch.include(filteredIncludes);

        } else if (projection != null && !StringUtil.isNullOrEmpty(projection.includes)) {
            fluidSearch = fluidSearch.include(projection.includes);
        }

        if (projection != null && !StringUtil.isNullOrEmpty(projection.excludes)) {
            fluidSearch = fluidSearch.exclude(projection.excludes);
        }
    }

    protected void paginate(Page page, CollectionReference collectionReference, FluidSearchService fluidSearch) throws ArlasException {
        setPageSizeAndFrom(page, fluidSearch);
        if (page != null) {
            if (page.before != null) {
                Page newPage = page;
                newPage.sort = Arrays.stream(page.sort.split(","))
                        .map(field -> field.startsWith("-") ? field.substring(1) : "-".concat(field)).collect(Collectors.joining(","));
                sortPage(newPage, fluidSearch);
                searchAfterPage(newPage, collectionReference.params.idPath, fluidSearch);
            } else {
                sortPage(page, fluidSearch);
                searchAfterPage(page, collectionReference.params.idPath, fluidSearch);
            }
        }
    }

    protected void setPageSizeAndFrom(Page page, FluidSearchService fluidSearch) throws ArlasException {
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

    protected void searchAfterPage(Page page, String idCollectionField, FluidSearchService fluidSearch) throws ArlasException {
        if (page != null && page.after != null) {
            CheckParams.checkPageAfter(page, idCollectionField);
            fluidSearch = fluidSearch.searchAfter(page, page.after);
        }
        if (page != null && page.before != null) {
            CheckParams.checkPageAfter(page, idCollectionField);
            fluidSearch.searchAfter(page, page.before);
        }
    }

    protected Polygon createPolygonFromH3(String h3) {
        List<Pair<Double, Double>> latLonList = H3Util.getInstance().getCellBoundaryAsLatLonList(h3);
        if (latLonList.size() > 7) {
            LOGGER.debug(h3);
            LOGGER.debug(latLonList.toString());
        }
        Polygon box = new Polygon();
        List<LngLatAlt> bounds = new ArrayList<>();
        latLonList.stream().forEach(g -> bounds.add(new LngLatAlt(g.getRight(), g.getLeft())));
        // add first point in order to close the polygon
        bounds.add(new LngLatAlt(latLonList.get(0).getRight(), latLonList.get(0).getLeft()));
        box.add(bounds);

        return box;
    }
    protected Polygon createPolygonFromGeohash(String geohash) {
        Rectangle rectangle = GeohashUtils.decodeBoundary(geohash, SpatialContext.GEO);
        Polygon polygon = new Polygon();
        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(rectangle.getMinX(), rectangle.getMaxY()));
        bounds.add(new LngLatAlt(rectangle.getMaxX(), rectangle.getMaxY()));
        bounds.add(new LngLatAlt(rectangle.getMaxX(), rectangle.getMinY()));
        bounds.add(new LngLatAlt(rectangle.getMinX(), rectangle.getMinY()));
        polygon.add(bounds);
        return polygon;
    }

    protected Polygon createBox(BoundingBox bbox) {
        Polygon box = new Polygon();
        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(bbox.getWest(), bbox.getNorth()));
        bounds.add(new LngLatAlt(bbox.getEast(), bbox.getNorth()));
        bounds.add(new LngLatAlt(bbox.getEast(), bbox.getSouth()));
        bounds.add(new LngLatAlt(bbox.getWest(), bbox.getSouth()));
        box.add(bounds);

        return box;
    }


    public CollectionReferenceService getCollectionReferenceService() { return collectionReferenceService; }

    public List<CollectionReferenceDescription> describeAllCollections(List<CollectionReference> collectionReferenceList,
                                                                       Optional<String> columnFilter) throws ArlasException {
        return collectionReferenceService.describeAllCollections(collectionReferenceList, columnFilter);
    }

    public CollectionReferenceDescription describeCollection(CollectionReference collectionReference,
                                                             Optional<String> columnFilter) throws ArlasException {
        return collectionReferenceService.describeCollection(collectionReference, columnFilter);
    }

    public AggregationResponse aggregate(MixedRequest request,
                                         CollectionReference collectionReference,
                                         Boolean isGeoAggregation,
                                         List<Aggregation> aggregationsRequests,
                                         int aggTreeDepth,
                                         Long startQuery) throws ArlasException {
        CheckParams.checkAggregationRequest(request.basicRequest, collectionReference);
        FluidSearchService fluidSearch = getFluidSearch(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        List<Aggregation> aggregations = ((AggregationsRequest) request.basicRequest).aggregations;
        if (aggregations != null && !aggregations.isEmpty()) {
            fluidSearch.aggregate(aggregations, isGeoAggregation);
        }
        return aggregate(collectionReference, aggregationsRequests, aggTreeDepth, startQuery, fluidSearch);
    }

    public ComputationResponse compute(MixedRequest request,
                                       CollectionReference collectionReference) throws ArlasException {
        CheckParams.checkComputationRequest(request.basicRequest, collectionReference);
        FluidSearchService fluidSearch = getFluidSearch(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        String field = ((ComputationRequest)request.basicRequest).field;
        ComputationEnum metric = ((ComputationRequest)request.basicRequest).metric;
        fluidSearch = fluidSearch.compute(field, metric);
        return compute(collectionReference, fluidSearch, field, metric);
    }

    public Hits count(MixedRequest request,
                      CollectionReference collectionReference) throws ArlasException {
        FluidSearchService fluidSearch = getFluidSearch(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        return count(collectionReference, fluidSearch);
    }

    public FeatureCollection getFeatures(MixedRequest request,
                                         CollectionReference collectionReference,
                                         boolean flat) throws ArlasException {
        FluidSearchService fluidSearch = getSearchRequest(request, collectionReference);
        return getFeatures(request, collectionReference, fluidSearch, flat);
    }


    private FluidSearchService getSearchRequest(MixedRequest request, CollectionReference collectionReference) throws ArlasException {
        FluidSearchService fluidSearch = getFluidSearch(collectionReference);
        applyFilter(collectionReference.params.filter, fluidSearch);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        paginate(((Search) request.basicRequest).page, collectionReference, fluidSearch);
        applyProjection(((Search) request.basicRequest).projection, fluidSearch, request.columnFilter, collectionReference);
        return fluidSearch;
    }

    // -----------------

    protected abstract AggregationResponse aggregate(CollectionReference collectionReference,
                                                     List<Aggregation> aggregationsRequests,
                                                     int aggTreeDepth,
                                                     Long startQuery,
                                                     FluidSearchService fluidSearch) throws ArlasException;

    public abstract Hits count(CollectionReference collectionReference,
                               FluidSearchService fluidSearch) throws ArlasException;

    public abstract FluidSearchService getFluidSearch(CollectionReference collectionReference) throws ArlasException;

    public abstract ComputationResponse compute(CollectionReference collectionReference,
                                                FluidSearchService fluidSearch,
                                                String field, ComputationEnum metric) throws ArlasException;

    public abstract FeatureCollection getFeatures(MixedRequest request,
                                                  CollectionReference collectionReference,
                                                  FluidSearchService fluidSearch,
                                                  boolean flat) throws ArlasException;

    public abstract Hits search(MixedRequest request,
                                CollectionReference collectionReference,
                                Boolean flat,
                                UriInfo uriInfo,
                                String method) throws ArlasException;

    public abstract List<Map<String, Object>> searchAsRaw(MixedRequest request,
                                                          CollectionReference collectionReference) throws ArlasException;

    public abstract Map<String, Object> getRawDoc(CollectionReference collectionReference,
                                                  String identifier,
                                                  String[] includes) throws ArlasException;

}
