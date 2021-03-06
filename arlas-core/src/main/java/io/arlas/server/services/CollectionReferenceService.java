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

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.managers.CacheManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.FieldType;
import io.arlas.server.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DAO for collection references
 */
public abstract class CollectionReferenceService {
    private static Logger LOGGER = LoggerFactory.getLogger(CollectionReferenceService.class);
    protected final String arlasIndex;
    protected final CacheManager cacheManager;

    public CollectionReferenceService(String arlasIndex, CacheManager cacheManager) {
        this.arlasIndex = arlasIndex;
        this.cacheManager = cacheManager;
    }

    abstract protected CollectionReference getCollectionReferenceFromDao(String ref) throws ArlasException;

    abstract protected Map<String, LinkedHashMap> getMappingFromDao(String indexName) throws ArlasException;

    abstract protected Map<String, LinkedHashMap> getAllMappingsFromDao(String arlasIndex) throws ArlasException;

    abstract protected void putCollectionReferenceWithDao(CollectionReference collectionReference) throws ArlasException;

    abstract public boolean isDateField(String field, String index) throws ArlasException;

    abstract public void initCollectionDatabase() throws ArlasException;

    abstract public List<CollectionReference> getAllCollectionReferences(Optional<String> columnFilter) throws ArlasException;

    abstract public void deleteCollectionReference(String ref) throws ArlasException;

    // -------

    public CollectionReference getCollectionReference(String ref) throws ArlasException {
        CollectionReference collectionReference = cacheManager.getCollectionReference(ref);
        if (collectionReference == null) {
            collectionReference = getCollectionReferenceFromDao(ref);
            cacheManager.putCollectionReference(ref, collectionReference);
        }
        if (!getMapping(collectionReference.params.indexName).isEmpty()){
            return collectionReference;
        } else {
            throw new ArlasException("Collection " + ref + " exists but can not be described. Check if index or template ".concat(collectionReference.params.indexName).concat(" exists"));
        }
    }

    protected Map<String, LinkedHashMap> getMapping(String indexName) throws ArlasException {
        Map<String, LinkedHashMap> mapping = cacheManager.getMapping(indexName);
        if (mapping == null) {
            mapping = getMappingFromDao(indexName);
            cacheManager.putMapping(indexName, mapping);
        }
        return mapping;
    }

    public CollectionReference putCollectionReference(CollectionReference collectionReference) throws ArlasException {
        checkCollectionReferenceParameters(collectionReference);
        putCollectionReferenceWithDao(collectionReference);
        //explicit clean-up cache
        cacheManager.removeCollectionReference(collectionReference.collectionName);
        cacheManager.removeMapping(collectionReference.params.indexName);
        return collectionReference;
    }

    public List<CollectionReferenceDescription> describeAllCollections(List<CollectionReference> collectionReferenceList,
                                                                       Optional<String> columnFilter) throws ArlasException {

        // Can't use lambdas because of the need to throw the exception of describeCollection()
        List<CollectionReferenceDescription> res  = new ArrayList<>();
        for (CollectionReference collection : collectionReferenceList) {
            if (!ColumnFilterUtil.cleanColumnFilter(columnFilter).isPresent()
                    || ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter,collection).isPresent()) {
                try{
                    CollectionReferenceDescription describe = describeCollection(collection, columnFilter);
                    res.add(describe);
                }catch (ArlasException e){}
            }
        }
        return res;
    }

    public CollectionReferenceDescription describeCollection(CollectionReference collectionReference) throws ArlasException {
        return this.describeCollection(collectionReference, Optional.empty());
    }

    public CollectionReferenceDescription describeCollection(CollectionReference collectionReference,
                                                             Optional<String> columnFilter) throws ArlasException {
        ArrayList<Pattern> excludeFields = new ArrayList<>();
        if (collectionReference.params.excludeFields != null) {
            Arrays.asList(collectionReference.params.excludeFields.split(","))
                    .forEach(field -> excludeFields.add(Pattern.compile("^" + field.replace(".", "\\.").replace("*", ".*") + "$")));
        }
        CollectionReferenceDescription collectionReferenceDescription = new CollectionReferenceDescription();
        collectionReferenceDescription.params = collectionReference.params;
        collectionReferenceDescription.collectionName = collectionReference.collectionName;

        Map<String, LinkedHashMap> mappings = getMapping(collectionReferenceDescription.params.indexName);
        Iterator<String> indices = mappings.keySet().iterator();
        Map<String, CollectionReferenceDescriptionProperty> properties = new HashMap<>();
        Optional<Set<String>> columnFilterPredicates = ColumnFilterUtil.getColumnFilterPredicates(columnFilter, collectionReference);

        while (indices.hasNext()) {
            String index = indices.next();
            LinkedHashMap fields = mappings.get(index);
            properties = union(properties, getFromSource(collectionReference, fields, new Stack<>(), excludeFields, columnFilterPredicates, true));
        }

        collectionReferenceDescription.properties = properties;
        if (properties.isEmpty()){
            throw new ArlasException("This collection can not be described. Check if index or template ".concat(collectionReferenceDescription.params.indexName).concat(" exist in Elasticsearch"));
        }
        return collectionReferenceDescription;
    }

    private Map<String, CollectionReferenceDescriptionProperty> union(Map<String, CollectionReferenceDescriptionProperty> source,
                                                                      Map<String, CollectionReferenceDescriptionProperty> update) {
        Map<String, CollectionReferenceDescriptionProperty> ret = new HashMap<>(source);
        for (String key : update.keySet()) {
            if (!ret.containsKey(key)) {
                ret.put(key,update.get(key));
            } else if (ret.get(key).type != update.get(key).type) {
                LOGGER.error("Cannot union field [key=" + key + "] because type mismatch between indices' mappings");
            } else if (ret.get(key).properties != null && update.get(key).properties != null) {
                ret.get(key).properties = union(ret.get(key).properties, update.get(key).properties);
            }
        }
        return ret;
    }

    private Map<String, CollectionReferenceDescriptionProperty> getFromSource(CollectionReference collectionReference,
                                                                              Map source, Stack<String> namespace,
                                                                              ArrayList<Pattern> excludeFields,
                                                                              Optional<Set<String>> columnFilterPredicates,
                                                                              boolean parentIsIndexed) {

        Map<String, CollectionReferenceDescriptionProperty> ret = new HashMap<>();

        for (Object key : source.keySet()) {
            namespace.push(key.toString());
            String path = String.join(".", namespace);
            boolean excludePath = excludeFields.stream().anyMatch(pattern -> pattern.matcher(path).matches());
            if (!excludePath) {
                if (source.get(key) instanceof Map) {
                    Map property = (Map) source.get(key);
                    CollectionReferenceDescriptionProperty collectionProperty = new CollectionReferenceDescriptionProperty();
                    if (property.containsKey("type")) {
                        collectionProperty.type = FieldType.getType(property.get("type"));
                    } else {
                        collectionProperty.type = FieldType.OBJECT;
                    }
                    if (FilterMatcherUtil.matchesOrWithin(columnFilterPredicates, path, collectionProperty.type == FieldType.OBJECT)) {
                        // check whether the field is declared in the mapping but not index
                        if (property.containsKey("enabled")) {
                            collectionProperty.indexed = (boolean)property.get("enabled") && parentIsIndexed;
                        } else if (property.containsKey("index")) {
                            collectionProperty.indexed = (boolean)property.get("index") && parentIsIndexed;
                        } else {
                            collectionProperty.indexed = parentIsIndexed;
                        }
                        if (property.containsKey("format")) {
                            String format = property.get("format").toString();
                            if (format == null && collectionProperty.type.equals(FieldType.DATE)) {
                                format = CollectionReference.DEFAULT_TIMESTAMP_FORMAT;
                            }
                            collectionProperty.format = format;
                        }
                        if (property.containsKey("properties") && property.get("properties") instanceof Map) {
                            collectionProperty.properties = getFromSource(collectionReference, (Map) property.get("properties"), namespace, excludeFields, columnFilterPredicates, collectionProperty.indexed);
                        }
                        if (collectionReference.params.taggableFields != null) {
                            collectionProperty.taggable = Arrays.asList(collectionReference.params.taggableFields.split(",")).stream().map(s -> s.trim()).collect(Collectors.toList()).contains(path);
                        }
                        ret.put(key.toString(), collectionProperty);
                    }
                }
            }
            namespace.pop();
        }
        return ret;
    }

    /**
     * Get the parameters paths of a collection, using the given filter predicates
     */
    public Set<String> getCollectionFields(CollectionReference collectionReference, Optional<String> filterPredicates) throws ArlasException {

        Map<String, CollectionReferenceDescriptionProperty> collectionFilteredProperties =
                this.describeCollection(collectionReference, filterPredicates).properties;

        return getPropertiesFields(collectionFilteredProperties, "")
                .collect(Collectors.toSet());
    }

    /**
     * Extract the fields of the given paths, recursively
     */
    private Stream<String> getPropertiesFields(Map<String, CollectionReferenceDescriptionProperty> properties, String parentPath) {
        return properties.entrySet().stream().flatMap(es -> {
            if (es.getValue().type == FieldType.OBJECT && es.getValue().properties != null) {
                return getPropertiesFields(es.getValue().properties, parentPath + es.getKey() + ".");
            } else {
                return Stream.of(parentPath + es.getKey());
            }
        });
    }

    public List<CollectionReferenceDescription> getAllIndicesAsCollections() throws ArlasException {
        List<CollectionReferenceDescription> collections = new ArrayList<>();
        Map<String, LinkedHashMap> indices = getAllMappingsFromDao(this.arlasIndex);

        for (String indexName : indices.keySet()) {
            CollectionReference collection = new CollectionReference();
            collection.collectionName = indexName;
            collection.params = new CollectionReferenceParameters();
            collection.params.indexName = indexName;
            collections.add(describeCollection(collection));
        }
        return collections;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void checkCollectionReferenceParameters(CollectionReference collectionReference) throws ArlasException {
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
        Map<String, LinkedHashMap> mappings = CollectionUtil.checkAliasMappingFields(getMapping(collectionReference.params.indexName), fields.toArray(new String[0]));
        for (String index : mappings.keySet()) {
            Map<String, Object> timestampMD = CollectionUtil.getFieldFromProperties(collectionReference.params.timestampPath, mappings.get(index));
            collectionReference.params.customParams = new HashMap<>();
            if (timestampMD.containsKey("format")) {
                collectionReference.params.customParams.put(CollectionReference.TIMESTAMP_FORMAT, timestampMD.get("format").toString());
            } else {
                collectionReference.params.customParams.put(CollectionReference.TIMESTAMP_FORMAT, CollectionReference.DEFAULT_TIMESTAMP_FORMAT);
            }
        }
    }

    public FieldType getType(CollectionReference collectionReference, String field, boolean throwException) throws ArlasException {
        FieldType fieldType = cacheManager.getFieldType(collectionReference.collectionName, field);
        if (fieldType == null) {
            String[] props = field.split("\\.");
            CollectionReferenceDescriptionProperty esField = describeCollection(collectionReference).properties.get(props[0]);
            if (esField == null) {
                return getUnknownType(field, collectionReference.collectionName, throwException);
            }
            for (int i=1; i<props.length; i++) {
                esField = esField.properties.get(props[i]);
                if (esField == null) {
                    return getUnknownType(field, collectionReference.collectionName, throwException);
                }
            }
            if (esField != null) {
                fieldType = esField.type;
                cacheManager.putFieldType(collectionReference.collectionName, field, fieldType);
            } else {
                return getUnknownType(field, collectionReference.collectionName, throwException);
            }
        }
        return fieldType;
    }

    private FieldType getUnknownType(String parentField, String collectionName, boolean throwException) throws ArlasException{
        if (throwException) {
            throw new NotFoundException("Field '" + parentField + "' not found in collection " + collectionName);
        } else {
            return FieldType.UNKNOWN;
        }
    }

}
