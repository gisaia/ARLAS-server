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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.arlas.server.core.exceptions.CollectionUnavailableException;
import io.arlas.server.core.services.CollectionReferenceService;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InternalServerErrorException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.utils.CollectionUtil;
import io.arlas.server.core.utils.ColumnFilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.arlas.server.core.model.CollectionReference.INCLUDE_FIELDS;

public class ElasticCollectionReferenceService extends CollectionReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticCollectionReferenceService.class);

    private static final String ARLAS_MAPPING_FILE_NAME = "arlas.mapping.json";

    private final ElasticClient client;

    public ElasticCollectionReferenceService(ElasticClient client, String arlasIndex, CacheManager cacheManager) {
        super(arlasIndex, cacheManager);
        this.client = client;
    }

    @Override
    public void initCollectionDatabase() throws ArlasException {
        if (client.indexExists(arlasIndex)) {
            client.putMapping(arlasIndex, ARLAS_MAPPING_FILE_NAME);
        } else {
            client.createIndex(arlasIndex, ARLAS_MAPPING_FILE_NAME);
        }
    }

    @Override
    protected CollectionReference getCollectionReferenceFromDao(String ref) throws ArlasException {
        return client.getCollectionReferenceFromES(arlasIndex, ref);
    }

    @Override
    protected Map<String, Map<String, Property>> getMappingFromDao(String indexName) throws ArlasException {
        return client.getMappings(indexName);
    }

    @Override
    protected Map<String, Map<String, Property>> getAllMappingsFromDao(String arlasIndex) throws ArlasException {
        return client.getMappings();
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences(Optional<String> columnFilter, Optional<String> organisations) throws ArlasException {
        List<CollectionReference> collections = new ArrayList<>();

        try {
            SearchRequest request = SearchRequest.of(r -> r
                    .index(arlasIndex)
                    .source(b -> b.filter(c -> c.excludes(INCLUDE_FIELDS)))
                    .sort(b -> b.field(c -> c.order(SortOrder.Asc)))
                    .query(b -> b.matchAll(c -> c.queryName("matchAllQuery")))
                    .size(100)
            );
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            //Exclude old include_fields for support old collection
//            searchSourceBuilder.fetchSource(null, "include_fields")
//                    .sort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
//                    .query(QueryBuilders.matchAllQuery())
//                    .size(100);// max of 100 hits will be returned for each scroll
//            SearchRequest request = new SearchRequest(arlasIndex);
//            request.source(searchSourceBuilder);
            SearchResponse<CollectionReferenceParameters> response = client.search(request, CollectionReferenceParameters.class);
            Set<String> allowedCollections = ColumnFilterUtil.getAllowedCollections(columnFilter);
/*<<<<<<< HEAD
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    String source = hit.getSourceAsString();
                    try {
                        CollectionReference colRef = new CollectionReference(hit.getId(), reader.readValue(source));
                        checkIfAllowedForOrganisations(colRef, organisations);
                        for (String c : allowedCollections) {
                            if ((c.endsWith("*") && hit.getId().startsWith(c.substring(0, c.indexOf("*"))))
                                    || hit.getId().equals(c)) {
                                collections.add(colRef);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new InternalServerErrorException("Can not fetch collection", e);
                    } catch (CollectionUnavailableException e) {
                        LOGGER.warn(String.format("Collection %s not available for this organisation %s",
                                hit.getId(), organisations));
=======*/

            // TODO es8 add search after
            for (Hit<CollectionReferenceParameters> hit : response.hits().hits()) {
                for (String c : allowedCollections) {
                    if (CollectionUtil.matches(c, hit.id())){
                        collections.add(new CollectionReference(hit.id(), hit.source()));
                        break;
                    }
                }
            }

        } catch (NotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collections.stream().filter(c-> {
            try {
                return !getMapping(c.params.indexName).isEmpty();
            } catch (ArlasException e) {
                return false;
            }
        }).toList();
    }

    @Override
    public void deleteCollectionReference(String ref) throws ArlasException {
        DeleteResponse response = client.deleteDocument(arlasIndex, ref);
        // TODO es8
//        if (response.status().equals(RestStatus.NOT_FOUND)) {
//            throw new NotFoundException("collection " + ref + " not found.");
//        } else if (!response.status().equals(RestStatus.OK)) {
//            throw new InternalServerErrorException("Unable to delete collection : " + response.status().toString());
//        } else {
            //explicit clean-up cache
            cacheManager.removeCollectionReference(ref);
//        }
    }

    @Override
    protected void putCollectionReferenceWithDao(CollectionReference collectionReference) throws ArlasException {
        IndexResponse response = client.index(arlasIndex, collectionReference.collectionName, collectionReference.params);
        // TODO es8
//        if (response.status().getStatus() != RestStatus.OK.getStatus()
//                && response.status().getStatus() != RestStatus.CREATED.getStatus()) {
//            throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
//        }
    }

    @Override
    public boolean isDateField(String field, String index) throws ArlasException {
        return client.isDateField(field, index);
    }
}
