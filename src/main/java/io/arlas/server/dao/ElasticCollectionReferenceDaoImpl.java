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
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ElasticCollectionReferenceDaoImpl implements CollectionReferenceDao {


    TransportClient client = null;
    String arlasIndex = null;
    private static LoadingCache<String, CollectionReference> collections = null;

    public ElasticCollectionReferenceDaoImpl(TransportClient client, String arlasIndex, int arlasCacheSize, int arlasCacheTimeout) {
        super();
        this.client = client;
        this.arlasIndex = arlasIndex;
        collections = CacheBuilder.newBuilder()
        .maximumSize(arlasCacheSize)
        .expireAfterWrite(arlasCacheTimeout, TimeUnit.SECONDS)
        .build(
                new CacheLoader<String, CollectionReference>() {
                    public CollectionReference load(String ref) throws NotFoundException {
                        return getCollectionReferenceFromES(ref);
                    }
                });
    }

    @Override
    public void initCollectionDatabase() throws IOException {
        try {
            client.admin().indices().prepareGetIndex().setIndices(arlasIndex).get();
        } catch (IndexNotFoundException e) {
            createArlasIndex();
        }
    }

    private void createArlasIndex() throws IOException {
        String arlasMapping = IOUtils.toString(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("arlas.mapping.json")));
        client.admin().indices().prepareCreate(arlasIndex).addMapping("collection", arlasMapping).get();
    }

    private CollectionReferenceParameters getCollectionReferenceParameters(Map<String, Object> source) {
        CollectionReferenceParameters params = new CollectionReferenceParameters();
        for (String field : source.keySet()) {
            switch (field) {
            case CollectionReference.INDEX_NAME:
                params.indexName = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.TYPE_NAME:
                params.typeName = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.ID_PATH:
                params.idPath = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.GEOMETRY_PATH:
                params.geometryPath = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.CENTROID_PATH:
                params.centroidPath = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.TIMESTAMP_PATH:
                params.timestampPath = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.INCLUDE_FIELDS:
                params.includeFields = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.EXCLUDE_FIELDS:
                params.excludeFields = source.get(field) != null ? source.get(field).toString() : null;
                break;
            case CollectionReference.CUSTOM_PARAMS:
                params.custom_params = source.get(field) != null ? (Map<String,String>)source.get(field) : null;
                break;
            }
        }
        return params;
    }

    private CollectionReference getCollectionReferenceFromES(String ref) throws NotFoundException {
        CollectionReference collection = null;
        GetResponse response = client.prepareGet(arlasIndex, "collection", ref).get();
        Map<String, Object> source = response.getSource();
        if (source != null) {
            collection = new CollectionReference(ref);
            collection.params = getCollectionReferenceParameters(source);
        } else {
            throw new NotFoundException("Collection " + ref + " not found.");
        }
        return collection;
    }
    
    @Override
    public CollectionReference getCollectionReference(String ref) throws NotFoundException {
        try {
            return collections.get(ref);
        } catch (ExecutionException e) {
            throw new NotFoundException("Collection " + ref + " not found.");
        }
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences() throws InternalServerErrorException {
        List<CollectionReference> collections = new ArrayList<CollectionReference>();

        try {
            QueryBuilder qb = QueryBuilders.matchAllQuery();
            SearchResponse scrollResp = client.prepareSearch(arlasIndex)
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC).setScroll(new TimeValue(60000))
                    .setQuery(qb).setSize(100).get(); // max of 100 hits will be returned for each scroll

            // Scroll until no hits are returned
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    CollectionReference collection = new CollectionReference(hit.getId());
                    collection.params = getCollectionReferenceParameters(hit.getSource());
                    collections.add(collection);
                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
                        .execute().actionGet();
            } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Collections not found.");
        }

        return collections;
    }

    @Override
    public CollectionReference putCollectionReference(String ref, CollectionReferenceParameters desc)
            throws InternalServerErrorException, JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        om.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        IndexResponse response = client.prepareIndex(arlasIndex, "collection", ref)
                .setSource(om.writeValueAsString(desc)).get();
        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.CREATED.getStatus()) {
            throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
        } else {
            //explicit clean-up cache
            collections.invalidate(ref);
            collections.cleanUp();
            
            return new CollectionReference(ref, desc);
        }
    }

    @Override
    public void deleteCollectionReference(String ref) throws NotFoundException, InternalServerErrorException {
        DeleteResponse response = client.prepareDelete(arlasIndex, "collection", ref).get();
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

    @Override
    public void checkCollectionReferenceParameters(CollectionReferenceParameters collectionRefParams) throws ArlasException {
        GetMappingsResponse response;
        try {
            //check index
            response = client.admin().indices().prepareGetMappings(collectionRefParams.indexName).setTypes(collectionRefParams.typeName).get();
            if(response.getMappings().isEmpty()
                    || !response.getMappings().get(collectionRefParams.indexName).containsKey(collectionRefParams.typeName)) {
                throw new NotFoundException("Type "+collectionRefParams.typeName+" does not exist in "+collectionRefParams.indexName+".");
            }
            
            //check type
            Object properties = response.getMappings().get(collectionRefParams.indexName).get(collectionRefParams.typeName).sourceAsMap().get("properties");
            if(properties == null) {
                throw new NotFoundException("Unable to find properties from "+collectionRefParams.typeName+" in "+collectionRefParams.indexName+".");
            }
            
            //check fields
            List<String> fields = new ArrayList<>();
            if(collectionRefParams.idPath != null)
                fields.add(collectionRefParams.idPath);
            if(collectionRefParams.geometryPath != null)
                fields.add(collectionRefParams.geometryPath);
            if(collectionRefParams.centroidPath != null)
                fields.add(collectionRefParams.centroidPath);
            if(collectionRefParams.timestampPath != null)
                fields.add(collectionRefParams.timestampPath);
            if(!fields.isEmpty())
                checkIndexMappingFields(collectionRefParams, fields.toArray(new String[fields.size()]));
        } catch (IndexNotFoundException e) {
            throw new NotFoundException("Index "+collectionRefParams.indexName+" does not exist.");
        } catch (Exception e) {
            throw new NotFoundException("Unable to access "+collectionRefParams.typeName+" in "+collectionRefParams.indexName+".");
        }
    }
    
    private void checkIndexMappingFields(CollectionReferenceParameters collectionRefParams , String... fields) throws ArlasException {
        GetFieldMappingsResponse response = client.admin().indices().prepareGetFieldMappings(collectionRefParams.indexName).setTypes(collectionRefParams.typeName).setFields(fields).get();
        for(String field : fields) {
            FieldMappingMetaData data = response.fieldMappings(collectionRefParams.indexName, collectionRefParams.typeName, field);
            if(data == null || data.isNull()) {
                throw new NotFoundException("Unable to find "+field+" from "+collectionRefParams.typeName+" in "+collectionRefParams.indexName+".");
            }
            else {
                if (field.equals(collectionRefParams.timestampPath)){
                    setTimestampFormat(collectionRefParams, data, field);
                }
            }
        }
    }

    private void setTimestampFormat(CollectionReferenceParameters collectionRefParams, FieldMappingMetaData data, String fieldPath) {
        String[] fields =  fieldPath.split("\\.");
        String field = fields[fields.length-1];
        LinkedHashMap<String, Object> timestampMD = (LinkedHashMap)data.sourceAsMap().get(field);
        collectionRefParams.custom_params = new HashMap<>();
        if (timestampMD.keySet().contains("format")) {
            collectionRefParams.custom_params.put(CollectionReference.TIMESTAMP_FORMAT, timestampMD.get("format").toString());
        } else {
            collectionRefParams.custom_params.put(CollectionReference.TIMESTAMP_FORMAT, CollectionReference.DEFAULT_TIMESTAMP_FORMAT);
        }
    }
}
