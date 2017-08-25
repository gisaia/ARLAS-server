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

package io.arlas.server.core;

import java.io.IOException;
import java.util.*;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;

public class ElasticAdmin {

    public TransportClient client;
    public ElasticAdmin(TransportClient client){
        this.client = client;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CollectionReferenceDescription describeCollection (CollectionReference collectionReference) throws IOException {
        CollectionReferenceDescription collectionReferenceDescription = new CollectionReferenceDescription();
        collectionReferenceDescription.params = collectionReference.params;
        collectionReferenceDescription.collectionName = collectionReference.collectionName;
        GetMappingsResponse response;
        response = client.admin().indices()
                .prepareGetMappings(collectionReferenceDescription.params.indexName).setTypes(collectionReferenceDescription.params.typeName).get();
        LinkedHashMap fields = (LinkedHashMap)response.getMappings()
                .get(collectionReferenceDescription.params.indexName).get(collectionReferenceDescription.params.typeName).sourceAsMap().get("properties");
        Map<String,CollectionReferenceDescriptionProperty> properties = getFromSource(fields);
        collectionReferenceDescription.properties = properties;
        return collectionReferenceDescription;
    }

    private Map<String,CollectionReferenceDescriptionProperty> getFromSource(Map source) {
        Map<String,CollectionReferenceDescriptionProperty> ret = new HashMap<>();
        for(Object key : source.keySet()) {
            if(source.get(key) instanceof Map) {
                Map property = (Map) source.get(key);
                CollectionReferenceDescriptionProperty collectionProperty = new CollectionReferenceDescriptionProperty();
                if (property.containsKey("type")) {
                    collectionProperty.type = ElasticType.getType(property.get("type"));
                } else {
                    collectionProperty.type = ElasticType.OBJECT;
                }
                if (property.containsKey("format")) {
                    String format = property.get("format").toString();
                    if (format == null && collectionProperty.type.equals(ElasticType.DATE)) {
                        format = CollectionReference.DEFAULT_TIMESTAMP_FORMAT;
                    }
                    collectionProperty.format = format;
                }
                if (property.containsKey("properties") && property.get("properties") instanceof Map) {
                    collectionProperty.properties = getFromSource((Map) property.get("properties"));
                }
                ret.put(key.toString(), collectionProperty);
            }
        }
        return ret;
    }

    public List<CollectionReferenceDescription> describeAllCollections (List<CollectionReference> collectionReferenceList) throws IOException, ArlasException {
        List<CollectionReferenceDescription> collectionReferenceDescriptionList = new ArrayList<>();
        for (CollectionReference collectionReference : collectionReferenceList){
            collectionReferenceDescriptionList.add(describeCollection(collectionReference));
        }
        return collectionReferenceDescriptionList;
    }
    
    public List<CollectionReferenceDescription> getAllIndecesAsCollections() throws IOException {
        List<CollectionReferenceDescription> collections = new ArrayList<CollectionReferenceDescription>();
        ImmutableOpenMap<String, IndexMetaData> indices = client.admin().cluster()
                .prepareState().get().getState()
                .getMetaData().getIndices();
        for(Iterator<String> indexNames = indices.keysIt(); indexNames.hasNext();) {
            String indexName = indexNames.next();
            ImmutableOpenMap<String, MappingMetaData> mappings = indices.get(indexName).getMappings();
            for(Iterator<String> mappingNames = mappings.keysIt(); mappingNames.hasNext();) {
                String mappingName = mappingNames.next();
                CollectionReference collection = new CollectionReference();
                collection.collectionName = indexName+"-"+mappingName;
                collection.params = new CollectionReferenceParameters();
                collection.params.indexName = indexName;
                collection.params.typeName = mappingName;
                collections.add(describeCollection(collection));
            }
        }
        return collections;
    }
}
