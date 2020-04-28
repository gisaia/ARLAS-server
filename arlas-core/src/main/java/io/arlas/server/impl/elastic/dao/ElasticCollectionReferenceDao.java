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

package io.arlas.server.impl.elastic.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.impl.elastic.utils.ElasticClient;
import io.arlas.server.impl.elastic.utils.ElasticTool;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.ColumnFilterUtil;
import io.arlas.server.utils.FilterMatcherUtil;
import io.arlas.server.utils.StringUtil;
import org.apache.logging.log4j.util.Strings;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ElasticCollectionReferenceDao implements CollectionReferenceDao {
    private static Logger LOGGER = LoggerFactory.getLogger(ElasticCollectionReferenceDao.class);

    private static LoadingCache<String, CollectionReference> collections = null;
    private static ObjectMapper mapper;
    private static ObjectReader reader;
    private static final String ARLAS_MAPPING_FILE_NAME = "arlas.mapping.json";

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    private ElasticClient client;
    private String arlasIndex;

    public ElasticCollectionReferenceDao(ElasticClient client, String arlasIndex, int arlasCacheSize, int arlasCacheTimeout) {
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
        ElasticTool.checkAliasMappingFields(client, collectionReference.params.indexName, fields.toArray(new String[0]));
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setTimestampFormatOfCollectionReference(String index, CollectionReferenceParameters collectionRefParams) throws ArlasException {
        String timestampField = collectionRefParams.timestampPath;

        GetFieldMappingsResponse response = client.getFieldMapping(index, timestampField);

        GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(index, timestampField);
        if (data != null) {
            String[] fields = timestampField.split("\\.");
            String field = fields[fields.length - 1];
            LinkedHashMap<String, Object> timestampMD = (LinkedHashMap) data.sourceAsMap().get(field);
            collectionRefParams.customParams = new HashMap<>();
            if (timestampMD.containsKey("format")) {
                collectionRefParams.customParams.put(CollectionReference.TIMESTAMP_FORMAT, timestampMD.get("format").toString());
            } else {
                collectionRefParams.customParams.put(CollectionReference.TIMESTAMP_FORMAT, CollectionReference.DEFAULT_TIMESTAMP_FORMAT);
            }
        }
    }

    // The methods below have been moved from ElasticAdmin (class removed)

    @Override
    public CollectionReferenceDescription describeCollection(CollectionReference collectionReference) throws ArlasException {
        return this.describeCollection(collectionReference, Optional.empty());
    }

    @Override
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

        Map<String, LinkedHashMap> response = client.getMappings(collectionReferenceDescription.params.indexName);

        Iterator<String> indices = response.keySet().iterator();

        Map<String, CollectionReferenceDescriptionProperty> properties = new HashMap<>();
        Optional<Set<String>> columnFilterPredicates = ColumnFilterUtil.getColumnFilterPredicates(columnFilter, collectionReference);

        while (indices.hasNext()) {
            String index = indices.next();
            LinkedHashMap fields = response.get(index);
            properties = union(properties, getFromSource(collectionReference, fields, new Stack<>(), excludeFields, columnFilterPredicates));
        }

        collectionReferenceDescription.properties = properties;
        return collectionReferenceDescription;
    }

    private Map<String, CollectionReferenceDescriptionProperty> union(Map<String, CollectionReferenceDescriptionProperty> source,
                                                                      Map<String, CollectionReferenceDescriptionProperty> update) {
        Map<String, CollectionReferenceDescriptionProperty> ret = new HashMap<>(source);
        for (String key : update.keySet()) {
            if(!ret.containsKey(key)) {
                ret.put(key,update.get(key));
            } else if(ret.get(key).type != update.get(key).type) {
                LOGGER.error("Cannot union field [key=" + key + "] because type mismatch between indices' mappings");
            } else if(ret.get(key).properties != null && update.get(key).properties != null) {
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
                            collectionProperty.taggable = Arrays.asList(collectionReference.params.taggableFields.split(",")).contains(path);
                        }
                        ret.put(key.toString(), collectionProperty);
                    }
                }
            }
            namespace.pop();
        }
        return ret;
    }

    @Override
    public List<CollectionReferenceDescription> describeAllCollections(List<CollectionReference> collectionReferenceList,
                                                                       Optional<String> columnFilter) throws ArlasException {

        // Can't use lambdas because of the need to throw the exception of describeCollection()
        List<CollectionReferenceDescription> res  = new ArrayList<>();
        for (CollectionReference collection : collectionReferenceList) {
            if (!ColumnFilterUtil.cleanColumnFilter(columnFilter).isPresent()
                    || ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter,collection).isPresent()) {
                res.add(describeCollection(collection, columnFilter));
            }
        }
        return res;
    }

    @Override
    public List<CollectionReferenceDescription> getAllIndicesAsCollections() throws ArlasException {
        List<CollectionReferenceDescription> collections = new ArrayList<>();
        Map<String, LinkedHashMap> indices = client.getMappings();

        for (String indexName : indices.keySet()) {
            CollectionReference collection = new CollectionReference();
            collection.collectionName = indexName;
            collection.params = new CollectionReferenceParameters();
            collection.params.indexName = indexName;
            collections.add(describeCollection(collection));
        }
        return collections;
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
        return properties.entrySet().stream().map(es -> {
            if (es.getValue().type == ElasticType.OBJECT) {
                return getPropertiesFields(es.getValue().properties, parentPath + es.getKey() + ".");
            } else {
                return Stream.of(parentPath + es.getKey());
            }
        })
                .flatMap(x -> x);
    }

}