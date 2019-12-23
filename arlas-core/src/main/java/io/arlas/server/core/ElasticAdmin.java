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

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;
import io.arlas.server.utils.ColumnFilterUtil;
import io.arlas.server.utils.FilterMatcherUtil;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElasticAdmin {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticAdmin.class);

    public Client client;

    public ElasticAdmin(Client client) {
        this.client = client;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CollectionReferenceDescription describeCollection(CollectionReference collectionReference) {
        return this.describeCollection(collectionReference, Optional.empty());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CollectionReferenceDescription describeCollection(CollectionReference collectionReference, Optional<String> columnFilter) {
        ArrayList<Pattern> excludeFields = new ArrayList<>();
        if (collectionReference.params.excludeFields != null) {
            Arrays.asList(collectionReference.params.excludeFields.split(",")).forEach(field -> {
                excludeFields.add(Pattern.compile("^" + field.replace(".", "\\.").replace("*", ".*") + "$"));
            });
        }
        CollectionReferenceDescription collectionReferenceDescription = new CollectionReferenceDescription();
        collectionReferenceDescription.params = collectionReference.params;
        collectionReferenceDescription.collectionName = collectionReference.collectionName;
        GetMappingsResponse response;
        response = client.admin().indices()
                .prepareGetMappings(collectionReferenceDescription.params.indexName).setTypes(collectionReferenceDescription.params.typeName).get();
        Iterator<String> indeces = response.getMappings().keysIt();

        Map<String, CollectionReferenceDescriptionProperty> properties = new HashMap<>();
        Optional<Set<String>> columnFilterPredicates = ColumnFilterUtil.getColumnFilterPredicates(columnFilter, collectionReference);

        while(indeces.hasNext()) {
            String index = indeces.next();
            LinkedHashMap fields = (LinkedHashMap) response.getMappings()
                    .get(index).get(collectionReferenceDescription.params.typeName).sourceAsMap().get("properties");
            properties = union(properties, getFromSource(collectionReference, fields, new Stack<>(), excludeFields, columnFilterPredicates));
        }

        collectionReferenceDescription.properties = properties;
        return collectionReferenceDescription;
    }

    private Map<String, CollectionReferenceDescriptionProperty> union(Map<String, CollectionReferenceDescriptionProperty> source, Map<String, CollectionReferenceDescriptionProperty> update) {
        Map<String, CollectionReferenceDescriptionProperty> ret = new HashMap<>(source);
        for (String key : update.keySet()) {
            if(!ret.containsKey(key)) {
                ret.put(key,update.get(key));
            } else if(ret.get(key).type != update.get(key).type) {
                LOGGER.error("Cannot union field [key=" + key + "] because type mismatch between indices' mappings");
            } else if(ret.get(key).properties instanceof Map && update.get(key).properties instanceof Map) {
                ret.get(key).properties = union(ret.get(key).properties, update.get(key).properties);
            }
        }
        return ret;
    }

    private Map<String, CollectionReferenceDescriptionProperty> getFromSource(
            CollectionReference collectionReference,
            Map source, Stack<String> namespace,
            ArrayList<Pattern> excludeFields,
            Optional<Set<String>> columnFilterPredicates) {

        Map<String, CollectionReferenceDescriptionProperty> ret = new HashMap<>();

        for (Object key : source.keySet()) {
            namespace.push(key.toString());
            String path = Strings.join(namespace,'.');
            boolean excludePath = excludeFields.stream().anyMatch(pattern -> pattern.matcher(path).matches());
            if (!excludePath) {
                if (source.get(key) instanceof Map) {
                    Map property = (Map) source.get(key);

                    CollectionReferenceDescriptionProperty collectionProperty = new CollectionReferenceDescriptionProperty();
                    if (property.containsKey("type")) {
                        collectionProperty.type = ElasticType.getType(property.get("type"));
                    } else {
                        collectionProperty.type = ElasticType.OBJECT;
                    }
                    if (FilterMatcherUtil.matchesOrWithin(columnFilterPredicates, path, collectionProperty.type == ElasticType.OBJECT)) {

                        if (property.containsKey("format")) {
                            String format = property.get("format").toString();
                            if (format == null && collectionProperty.type.equals(ElasticType.DATE)) {
                                format = CollectionReference.DEFAULT_TIMESTAMP_FORMAT;
                            }
                            collectionProperty.format = format;
                        }
                        if (property.containsKey("properties") && property.get("properties") instanceof Map) {
                            collectionProperty.properties = getFromSource(collectionReference, (Map) property.get("properties"), namespace, excludeFields, columnFilterPredicates);
                        }
                        if (collectionReference.params.taggableFields != null) {
                            collectionProperty.taggable = Arrays.stream(collectionReference.params.taggableFields.split(",")).anyMatch(taggable -> taggable.equals(path));
                        }
                        ret.put(key.toString(), collectionProperty);
                    }
                }
            }
            namespace.pop();
        }
        return ret;
    }

    public List<CollectionReferenceDescription> describeAllCollections(List<CollectionReference> collectionReferenceList, Optional<String> columnFilter) throws IOException, ArlasException {
        List<CollectionReferenceDescription> collectionReferenceDescriptionList = new ArrayList<>();
        for (CollectionReference collectionReference : collectionReferenceList) {
            collectionReferenceDescriptionList.add(describeCollection(collectionReference, columnFilter));
        }
        return collectionReferenceDescriptionList;
    }

    public List<CollectionReferenceDescription> getAllIndecesAsCollections() throws IOException {
        List<CollectionReferenceDescription> collections = new ArrayList<CollectionReferenceDescription>();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indices = client.admin().indices().getMappings(new GetMappingsRequest()).actionGet().getMappings();
        for (Iterator<String> indexNames = indices.keysIt(); indexNames.hasNext(); ) {
            String indexName = indexNames.next();
            ImmutableOpenMap<String, MappingMetaData> mappings = indices.get(indexName);
            for (Iterator<String> mappingNames = mappings.keysIt(); mappingNames.hasNext(); ) {
                String mappingName = mappingNames.next();
                CollectionReference collection = new CollectionReference();
                collection.collectionName = indexName + "-" + mappingName;
                collection.params = new CollectionReferenceParameters();
                collection.params.indexName = indexName;
                collection.params.typeName = mappingName;
                collections.add(describeCollection(collection));
            }
        }
        return collections;
    }

}
