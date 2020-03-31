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

package io.arlas.server.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.ElasticClient;
import io.arlas.server.utils.ElasticTool;
import io.arlas.server.utils.StringUtil;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.indices.GetFieldMappingsResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ElasticCollectionReferenceDaoImpl implements CollectionReferenceDao {


    ElasticClient client;
    String arlasIndex;
    private static LoadingCache<String, CollectionReference> collections = null;
    private static ObjectMapper mapper;
    private static ObjectReader reader;
    private static final String ARLAS_MAPPING_FILE_NAME = "arlas.mapping.json";


    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    public ElasticCollectionReferenceDaoImpl(ElasticClient client, String arlasIndex, int arlasCacheSize, int arlasCacheTimeout) {
        super();
        this.client = client;
        this.arlasIndex = arlasIndex;
        collections = CacheBuilder.newBuilder()
                .maximumSize(arlasCacheSize)
                .expireAfterWrite(arlasCacheTimeout, TimeUnit.SECONDS)
                .build(
                        new CacheLoader<String, CollectionReference>() {
                            public CollectionReference load(String ref) throws ArlasException, IOException {
                                return ElasticTool.getCollectionReferenceFromES(client, arlasIndex, reader, ref);
                            }
                        });
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
    public CollectionReference getCollectionReference(String ref) throws ArlasException {
        try {
            return collections.get(ref);
        } catch (ExecutionException e) {
            throw new NotFoundException("Collection " + ref + " not found.", e);
        }
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences() throws ArlasException {
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
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    String source = hit.getSourceAsString();
                    try {
                        collections.add(new CollectionReference(hit.getId(), reader.readValue(source)));
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
        return collections;
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
            collections.invalidate(ref);
            collections.cleanUp();
        }
    }

    private void checkCollectionReferenceParameters(CollectionReference collectionReference) throws ArlasException {
        //get fields
        List<String> fields = new ArrayList<>();
        if (collectionReference.params.idPath != null)
            fields.add(collectionReference.params.idPath);
        if (collectionReference.params.geometryPath != null)
            fields.add(collectionReference.params.geometryPath);
        if (collectionReference.params.centroidPath != null)
            fields.add(collectionReference.params.centroidPath);
        if (collectionReference.params.timestampPath != null)
            fields.add(collectionReference.params.timestampPath);
        if(!StringUtil.isNullOrEmpty(collectionReference.params.excludeFields)){
            List<String> excludeField = Arrays.asList(collectionReference.params.excludeFields.split(","));
            CheckParams.checkExcludeField(excludeField, fields);
        }
        ElasticTool.checkAliasMappingFields(client, collectionReference.params.indexName, fields.toArray(new String[fields.size()]));
        List<String> indices = ElasticTool.getIndicesName(client, collectionReference.params.indexName);
        for (String index : indices) {
            setTimestampFormatOfCollectionReference(index, collectionReference.params);
        }
    }

    @Override
    public CollectionReference putCollectionReference(CollectionReference collectionReference) throws ArlasException {
        checkCollectionReferenceParameters(collectionReference);
        IndexResponse response;
        try {
            response = client.index(arlasIndex, collectionReference.collectionName, mapper.writeValueAsString(collectionReference.params));
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Can not put collection " + collectionReference.collectionName, e);
        }

        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.CREATED.getStatus()) {
            throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
        } else {
            //explicit clean-up cache
            collections.invalidate(collectionReference.collectionName);
            collections.cleanUp();

            return collectionReference;
        }
    }



    private void setTimestampFormatOfCollectionReference(String index, CollectionReferenceParameters collectionRefParams) throws ArlasException {
        String timestampField = collectionRefParams.timestampPath;

        GetFieldMappingsResponse response = client.getFieldMapping(index, timestampField);

        GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(index, timestampField);
        if (data != null) {
            String[] fields = timestampField.split("\\.");
            String field = fields[fields.length - 1];
            LinkedHashMap<String, Object> timestampMD = (LinkedHashMap) data.sourceAsMap().get(field);
            collectionRefParams.customParams = new HashMap<>();
            if (timestampMD.keySet().contains("format")) {
                collectionRefParams.customParams.put(CollectionReference.TIMESTAMP_FORMAT, timestampMD.get("format").toString());
            } else {
                collectionRefParams.customParams.put(CollectionReference.TIMESTAMP_FORMAT, CollectionReference.DEFAULT_TIMESTAMP_FORMAT);
            }
        }
    }
}
