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

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.exceptions.CollectionUnavailableException;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.core.model.response.FieldType;
import io.arlas.server.core.utils.CheckParams;
import io.arlas.server.core.utils.CollectionUtil;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.core.utils.FilterMatcherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DAO for collection references
 */
@SuppressWarnings({"rawtypes"})
public abstract class CollectionReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionReferenceService.class);
    protected final String arlasIndex;
    protected final CacheManager cacheManager;

    public CollectionReferenceService(String arlasIndex, CacheManager cacheManager) {
        this.arlasIndex = arlasIndex;
        this.cacheManager = cacheManager;
    }

    abstract protected CollectionReference getCollectionReferenceFromDao(String ref) throws ArlasException;

    abstract protected Map<String, Map<String, Object>> getMappingFromDao(String indexName) throws ArlasException;

    abstract protected Map<String, Map<String, Object>> getAllMappingsFromDao(String arlasIndex) throws ArlasException;

    abstract protected void putCollectionReferenceWithDao(CollectionReference collectionReference) throws ArlasException;

    abstract public void initCollectionDatabase() throws ArlasException;

    abstract public List<CollectionReference> getAllCollectionReferences(Optional<String> columnFilter, Optional<String> organisations) throws ArlasException;

    abstract public void deleteCollectionReference(String ref) throws ArlasException;

    // -------

    public CollectionReference getCollectionReference(String ref, Optional<String> organisations) throws ArlasException {
        CollectionReference collectionReference = cacheManager.getCollectionReference(ref);
        if (collectionReference == null) {
            collectionReference = getCollectionReferenceFromDao(ref);
            cacheManager.putCollectionReference(ref, collectionReference);
        }
        checkIfAllowedForOrganisations(collectionReference, organisations);
        if (!getMapping(collectionReference.params.indexName).isEmpty()){
            return collectionReference;
        } else {
            throw new ArlasException("Collection " + ref + " exists but can not be described. Check if index or template ".concat(collectionReference.params.indexName).concat(" exists"));
        }
    }

    protected Map<String, Map<String, Object>> getMapping(String indexName) throws ArlasException {
        Map<String, Map<String, Object>> mapping = cacheManager.getMapping(indexName);
        if (mapping == null) {
            mapping = getMappingFromDao(indexName);
            cacheManager.putMapping(indexName, mapping);
        }
        return mapping;
    }

    public void putCollectionReference(CollectionReference collectionReference) throws ArlasException {
        putCollectionReference(collectionReference, true);
    }

    public CollectionReference putCollectionReference(CollectionReference collectionReference, boolean checkFields) throws ArlasException {
        checkCollectionReferenceParameters(collectionReference, checkFields);
        putCollectionReferenceWithDao(collectionReference);
        //explicit clean-up cache
        cacheManager.removeCollectionReference(collectionReference.collectionName);
        cacheManager.removeMapping(collectionReference.params.indexName);
        return collectionReference;
    }

    public List<CollectionReferenceDescription> describeAllCollections(List<CollectionReference> collectionReferenceList,
                                                                       Optional<String> columnFilter) throws CollectionUnavailableException {

        // Can't use lambdas because of the need to throw the exception of describeCollection()
        List<CollectionReferenceDescription> res  = new ArrayList<>();
        for (CollectionReference collection : collectionReferenceList) {
            if (ColumnFilterUtil.cleanColumnFilter(columnFilter).isEmpty()
                    || ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter,collection).isPresent()) {
                try {
                    CollectionReferenceDescription describe = describeCollection(collection, columnFilter);
                    res.add(describe);
                } catch (ArlasException e) { }

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

        Map<String, Map<String, Object>> mappings = getMapping(collectionReferenceDescription.params.indexName);
        Iterator<String> indices = mappings.keySet().iterator();
        Map<String, CollectionReferenceDescriptionProperty> properties = new HashMap<>();
        Optional<Set<String>> columnFilterPredicates = ColumnFilterUtil.getColumnFilterPredicates(columnFilter, collectionReference);

        while (indices.hasNext()) {
            String index = indices.next();
            Map fields = mappings.get(index);
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

    protected Map<String, CollectionReferenceDescriptionProperty> getFromSource(CollectionReference collectionReference,
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
                if (source.get(key) instanceof Map property) {
                    CollectionReferenceDescriptionProperty collectionProperty = new CollectionReferenceDescriptionProperty();
                    if (property.containsKey("type")) {
                        collectionProperty.type = FieldType.getType(property.get("type"));
                    } else {
                        collectionProperty.type = FieldType.OBJECT;
                    }
                    if (FilterMatcherUtil.matchesOrWithin(columnFilterPredicates, path, collectionProperty.type == FieldType.OBJECT)) {
                        if (property.containsKey("fields")) {
                            if(property.get("fields") instanceof Map){
                                for (Object keyFields : ((Map)property.get("fields")).keySet()) {
                                    if ( ((Map)property.get("fields")).get(keyFields) instanceof Map propertyFields) {
                                        if (propertyFields.containsKey("type")) {
                                            if(propertyFields.get("type").equals(FieldType.MAPPER_MURMUR3.toString())){
                                                collectionProperty.hashField = keyFields.toString();
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                            collectionProperty.taggable = Arrays.stream(collectionReference.params.taggableFields.split(",")).map(String::trim).toList().contains(path);
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
        Map<String, Map<String, Object>> indices = getAllMappingsFromDao(this.arlasIndex);

        for (String indexName : indices.keySet()) {
            CollectionReference collection = new CollectionReference();
            collection.collectionName = indexName;
            collection.params = new CollectionReferenceParameters();
            collection.params.indexName = indexName;
            collections.add(describeCollection(collection));
        }
        return collections;
    }

    @SuppressWarnings({"unchecked"})
    protected void checkCollectionReferenceParameters(CollectionReference collectionReference, boolean checkFields) throws ArlasException {
        //get fields
        List<String> fields = new ArrayList<>();
        if (checkFields) {
            if (collectionReference.params.idPath != null)
                fields.add(collectionReference.params.idPath);
            if (collectionReference.params.geometryPath != null)
                fields.add(collectionReference.params.geometryPath);
            if (collectionReference.params.centroidPath != null)
                fields.add(collectionReference.params.centroidPath);
            if (collectionReference.params.timestampPath != null)
                fields.add(collectionReference.params.timestampPath);
            if (!StringUtil.isNullOrEmpty(collectionReference.params.excludeFields)) {
                List<String> excludeField = Arrays.asList(collectionReference.params.excludeFields.split(","));
                CheckParams.checkExcludeField(excludeField, fields);
            }
        }
        Map<String, Map<String, Object>> mappings = CollectionUtil.checkAliasMappingFields(getMapping(collectionReference.params.indexName), fields.toArray(new String[0]));
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
            fieldType = esField.type;
            cacheManager.putFieldType(collectionReference.collectionName, field, fieldType);
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

    public boolean isDateField(String field, String index) throws ArlasException {
        return getFieldType(field, index).isDateField();
    }

    public boolean isGeoField(String field, String index) throws ArlasException {
        return getFieldType(field, index).isGeoField();
    }

    private FieldType getFieldType(String field, String index) throws ArlasException  {
        AtomicReference<FieldType> ret = new AtomicReference<>(FieldType.UNKNOWN);
        Optional.ofNullable(getMapping(index).get(index))
                .flatMap(e -> Optional.ofNullable(e.get(field)))
                .flatMap(f -> Optional.ofNullable(((Map) f).get("type")))
                .ifPresent(t -> ret.set(FieldType.getType(t)));
        return ret.get();
    }

    protected void checkIfAllowedForOrganisations(CollectionReference collection,
                                               Optional<String> organisations)
            throws CollectionUnavailableException {
        checkIfAllowedForOrganisations(collection, organisations, false);
    }

    public void checkIfAllowedForOrganisations(CollectionReference collection,
                                               Optional<String> organisations,
                                               boolean ownerOnly)
            throws CollectionUnavailableException {
        if (organisations.isEmpty()) {
            // no header, we'll trust the column filter if any
            LOGGER.debug("No organisation header");
            return;
        }

        if (!ownerOnly &&
                (collection.params.collectionOrganisations == null ||
                        collection.params.collectionOrganisations.isPublic ||
                        collection.params.collectionOrganisations.owner == null)) {
            // do we consider a collection with no organisation attribute open to all?
            LOGGER.debug(String.format("Collection %s organisation is public or null: %s",
                    collection.collectionName, collection.params.collectionOrganisations));
            return;
        }

        List<String> o = new ArrayList<>();
        o.add(collection.params.collectionOrganisations.owner);
        if (!ownerOnly && collection.params.collectionOrganisations.sharedWith != null) {
            o.addAll(collection.params.collectionOrganisations.sharedWith);
        }
        LOGGER.debug("collection's organisations=" + o);
        LOGGER.debug("header=" + organisations.get());
        o.retainAll(Arrays.stream(organisations.get().split(",")).toList());
        LOGGER.debug("allowed org=" + o);
        if (o.isEmpty()) {
            throw new CollectionUnavailableException("The collection not available with organisation header: " + organisations.get());
        }
    }
}
