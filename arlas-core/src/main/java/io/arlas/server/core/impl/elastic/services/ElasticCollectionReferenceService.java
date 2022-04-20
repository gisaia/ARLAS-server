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
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InternalServerErrorException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.impl.elastic.utils.ElasticTool;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.utils.ColumnFilterUtil;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ElasticCollectionReferenceService extends CollectionReferenceService {
    private static Logger LOGGER = LoggerFactory.getLogger(ElasticCollectionReferenceService.class);

    private static ObjectMapper mapper;
    private static ObjectReader reader;
    private static final String ARLAS_MAPPING_FILE_NAME = "arlas.mapping.json";

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    private final ElasticClient client;

    public ElasticCollectionReferenceService(ElasticClient client, String arlasIndex, CacheManager cacheManager) {
        super(arlasIndex, cacheManager);
        this.client = client;
    }

    @Override
    public void initCollectionDatabase() throws ArlasException {
        if (client.indexExists(arlasIndex)) {
            ElasticTool.putExtendedMapping(client, arlasIndex, this.getClass().getClassLoader().getResourceAsStream(ARLAS_MAPPING_FILE_NAME));
        } else {
            ElasticTool.createArlasIndex(client, arlasIndex, ARLAS_MAPPING_FILE_NAME);
        }
    }

    @Override
    protected CollectionReference getCollectionReferenceFromDao(String ref) throws ArlasException {
        return ElasticTool.getCollectionReferenceFromES(client, arlasIndex, reader, ref);
    }

    @Override
    protected Map<String, LinkedHashMap> getMappingFromDao(String indexName) throws ArlasException {
        return client.getMappings(indexName);
    }

    @Override
    protected Map<String, LinkedHashMap> getAllMappingsFromDao(String arlasIndex) throws ArlasException {
        return client.getMappings();
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences(Optional<String> columnFilter) throws ArlasException {
        List<CollectionReference> collections = new ArrayList<>();

        try {
            QueryBuilder qb = QueryBuilders.matchAllQuery();
            SearchRequest request = new SearchRequest(arlasIndex);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //Exclude old include_fields for support old collection
            searchSourceBuilder.fetchSource(null, "include_fields")
                    .sort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                    .query(qb).size(100);// max of 100 hits will be returned for each scroll
            request.source(searchSourceBuilder);
            request.scroll(new TimeValue(60000));
            SearchResponse scrollResp = client.search(request);
            Set<String> allowedCollections = ColumnFilterUtil.getAllowedCollections(columnFilter);
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    String source = hit.getSourceAsString();
                    try {
                        for (String c : allowedCollections) {
                            if ((c.endsWith("*") && hit.getId().startsWith(c.substring(0, c.indexOf("*"))))
                                    || hit.getId().equals(c)){
                                collections.add(new CollectionReference(hit.getId(), reader.readValue(source)));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new InternalServerErrorException("Can not fetch collection", e);
                    }
                }
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollResp.getScrollId());
                scrollRequest.scroll(new TimeValue(60000));
                scrollResp = client.searchScroll(scrollRequest);
            }
            while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollResp.getScrollId());
            client.clearScroll(clearScrollRequest);
        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collections.stream().filter(c-> {
            try {
                return !getMapping(c.params.indexName).isEmpty();
            } catch (ArlasException e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteCollectionReference(String ref) throws ArlasException {
        DeleteResponse response = client.delete(arlasIndex, ref);
        if (response.status().equals(RestStatus.NOT_FOUND)) {
            throw new NotFoundException("collection " + ref + " not found.");
        } else if (!response.status().equals(RestStatus.OK)) {
            throw new InternalServerErrorException("Unable to delete collection : " + response.status().toString());
        } else {
            //explicit clean-up cache
            cacheManager.removeCollectionReference(ref);
        }
    }

    @Override
    protected void putCollectionReferenceWithDao(CollectionReference collectionReference) throws ArlasException {
        IndexResponse response;
        try {
            response = client.index(arlasIndex, collectionReference.collectionName, mapper.writeValueAsString(collectionReference.params));
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Can not put collection " + collectionReference.collectionName, e);
        }

        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.CREATED.getStatus()) {
            throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
        }
    }

    @Override
    public boolean isDateField(String field, String index) throws ArlasException {
        return ElasticTool.isDateField(field, client, index);
    }
}
