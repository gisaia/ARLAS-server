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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Aggregation;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.Request;
import io.arlas.server.model.response.*;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ParamsParser;
import io.arlas.server.utils.ResponseCacheManager;
import org.geojson.FeatureCollection;
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

    private String baseUri;
    protected CollectionReferenceDao daoCollectionReference;
    protected ResponseCacheManager responseCacheManager;

    public ExploreService() {
    }

    public ExploreService(String baseUri, int arlasRestCacheTimeout) {
        this.baseUri = baseUri;
        this.responseCacheManager = new ResponseCacheManager(arlasRestCacheTimeout);
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

    // -----------------

    public abstract CollectionReferenceDao getDaoCollectionReference();

    public abstract AggregationResponse aggregate(MixedRequest request,
                                                  CollectionReference collectionReference,
                                                  Boolean isGeoAggregation,
                                                  List<Aggregation> aggregationsRequests,
                                                  int aggTreeDepth,
                                                  Long startQuery) throws ArlasException;

    public abstract ComputationResponse compute(MixedRequest request,
                                                CollectionReference collectionReference) throws ArlasException;

    public abstract Hits count(MixedRequest request,
                               CollectionReference collectionReference) throws ArlasException;

    public abstract List<CollectionReferenceDescription> describeAllCollections(List<CollectionReference> collectionReferenceList,
                                                                                Optional<String> columnFilter) throws ArlasException;

    public abstract CollectionReferenceDescription describeCollection(CollectionReference collectionReference,
                                                             Optional<String> columnFilter) throws ArlasException;

    public abstract FeatureCollection getFeatures(MixedRequest request,
                                                  CollectionReference collectionReference,
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
