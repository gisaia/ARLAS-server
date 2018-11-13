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
import io.arlas.server.app.INSPIREConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.exceptions.*;
import io.arlas.server.model.*;
import io.arlas.server.model.OgcInspireConfigurationParameters;
import io.arlas.server.model.enumerations.AccessConstraintEnum;
import io.arlas.server.model.enumerations.InspireAccessClassificationEnum;
import io.arlas.server.utils.BoundingBox;
import io.arlas.server.utils.CheckParams;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
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


    Client client = null;
    String arlasIndex = null;
    OGCConfiguration ogcConfiguration;
    private static LoadingCache<String, CollectionReference> collections = null;
    private static ObjectMapper mapper;
    private static ObjectReader reader;
    private static final String META_COLLECTION_NAME = "metacollection";
    private static final String ARLAS_INDEX_MAPPING_NAME = "collection";
    private static final String META_COLLECTION_ID_PATH = "dublin_core_element_name.identifier";
    private static final String META_COLLECTION_GEOMETRY_PATH = "dublin_core_element_name.coverage";
    private static final String META_COLLECTION_CENTROID_PATH = "dublin_core_element_name.coverage_centroid";
    private static final String META_COLLECTION_TIMESTAMP_PATH = "dublin_core_element_name.date";


    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    public ElasticCollectionReferenceDaoImpl(Client client, String arlasIndex, int arlasCacheSize, int arlasCacheTimeout) {
        super();
        this.client = client;
        this.arlasIndex = arlasIndex;
        collections = CacheBuilder.newBuilder()
                .maximumSize(arlasCacheSize)
                .expireAfterWrite(arlasCacheTimeout, TimeUnit.SECONDS)
                .build(
                        new CacheLoader<String, CollectionReference>() {
                            public CollectionReference load(String ref) throws ArlasException {
                                return getCollectionReferenceFromES(ref);
                            }
                        });
    }

    @Override
    public void initCollectionDatabase() {
        try {
            client.admin().indices().prepareGetIndex().setIndices(arlasIndex).get();
        } catch (IndexNotFoundException e) {
            try {
                createArlasIndex();
            } catch (IOException e1) {
                new InternalServerErrorException("Can not initialize the collection database", e);
            }
        }
    }

    @Override
    public void initMetaCollection(OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws ArlasException {
        try {
            collections.get(META_COLLECTION_NAME);
        } catch (ExecutionException e) {
            createMetaCollection(ogcConfiguration, inspireConfiguration);
        }
    }

    private void createArlasIndex() throws IOException {
        String arlasMapping = IOUtils.toString(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("arlas.mapping.json")));
        client.admin().indices().prepareCreate(arlasIndex).addMapping(ARLAS_INDEX_MAPPING_NAME, arlasMapping, XContentType.JSON).get();
    }

    private CollectionReference getCollectionReferenceFromES(String ref) throws ArlasException {
        CollectionReference collection = new CollectionReference(ref);
        //Exclude old include_fields for support old collection
        GetResponse hit = client.prepareGet(arlasIndex, "collection", ref).setFetchSource(null, "include_fields").get();
        String source = hit.getSourceAsString();
        if (source != null) {
            try {
                collection.params = reader.readValue(source);
            } catch (IOException e) {
                throw new InternalServerErrorException("Can not fetch collection " + ref, e);
            }
        } else {
            throw new NotFoundException("Collection " + ref + " not found.");
        }
        return collection;
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
    public  CollectionReference getMetaCollectionReference(OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws ArlasException {
        try {
            return collections.get(META_COLLECTION_NAME);
        } catch (ExecutionException e) {
            throw new InternalServerErrorException("Cannot fetch metacollection");
        }
    }

    @Override
    public CollectionReferences getCollectionReferencesForCSW(QueryBuilder queryBuilder,  Boolean isConfigurationQuery, String[] includes, String[] excludes, int size,
                                                        int from) throws ArlasException, IOException {
        CollectionReferences collectionReferences = new CollectionReferences();
        collectionReferences.collectionReferences = new ArrayList<>();

        //Exclude old include_fields for support old collection
        if (excludes != null) {
            excludes[excludes.length + 1] = "include_fields";
        } else {
            excludes = new String[]{"include_fields"};
        }
        try {
            SearchResponse response = client.prepareSearch(arlasIndex)
                    .setFetchSource(includes, excludes)
                    .setFrom(from)
                    .setSize(size)
                    .setQuery(queryBuilder).get();

            if (isConfigurationQuery) {
                // The queried parameter is set in the configuration file ==> The only returned collection should be metacollection
                // ==> this means that all the collections would match the query if the right value is given
                if (response.getHits().getTotalHits() == 1 && response.getHits().getHits()[0].getId().equals(META_COLLECTION_NAME) ) {
                    collectionReferences = getCollectionReferences(includes, null, size, from);
                } else if (response.getHits().getTotalHits() == 0){
                    collectionReferences.collectionReferences = new ArrayList<>();
                    collectionReferences.totalCollectionReferences = 0;
                    collectionReferences.nbCollectionReferences = 0;
                } else {
                    throw new InternalServerErrorException("Cannot fetch metacollection");
                }
            } else {
                // The queried parameter is set in each declared collection
                collectionReferences.totalCollectionReferences = response.getHits().getTotalHits();

                for (SearchHit hit : response.getHits().getHits()) {
                    String source = hit.getSourceAsString();
                    try {
                        if (!hit.getId().equals(META_COLLECTION_NAME)){
                            collectionReferences.collectionReferences.add(new CollectionReference(hit.getId(), mapper.readerFor(CollectionReferenceParameters.class).readValue(source)));
                        } else {
                            collectionReferences.totalCollectionReferences -= 1;
                        }
                    } catch (IOException e) {
                        throw new InternalServerErrorException("Can not fetch collection", e);
                    }
                }
                collectionReferences.nbCollectionReferences = collectionReferences.collectionReferences.size();
            }

        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collectionReferences;
    }

    @Override
    public CollectionReferences getCollectionReferences(String[] includes, String[] excludes, int size,
                                                              int from) throws ArlasException, IOException {
        CollectionReferences collectionReferences = new CollectionReferences();
        collectionReferences.collectionReferences = new ArrayList<>();

        //Exclude old include_fields for support old collection
        if (excludes != null) {
            excludes[excludes.length + 1] = "include_fields";
        } else {
            excludes = new String[]{"include_fields"};
        }
        try {
            SearchResponse response = client.prepareSearch(arlasIndex)
                    .setFetchSource(includes, excludes)
                    .setFrom(from)
                    .setSize(size)
                    .get();
            collectionReferences.totalCollectionReferences = response.getHits().getTotalHits();
            for (SearchHit hit : response.getHits().getHits()) {
                String source = hit.getSourceAsString();
                try {
                    if (!hit.getId().equals(META_COLLECTION_NAME)){
                        collectionReferences.collectionReferences.add(new CollectionReference(hit.getId(), mapper.readerFor(CollectionReferenceParameters.class).readValue(source)));
                    } else {
                        collectionReferences.totalCollectionReferences -= 1;
                    }
                } catch (IOException e) {
                    throw new InternalServerErrorException("Can not fetch collection", e);
                }
            }
            collectionReferences.nbCollectionReferences = collectionReferences.collectionReferences.size();

        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collectionReferences;
    }


    @Override
    public List<CollectionReference> getCollectionReferences(String[] includes, String[] excludes, int size,
                                                             int from, String[] ids, String q, BoundingBox boundingBox) throws ArlasException, IOException {

        List<CollectionReference> collections = new ArrayList<>();
        //Exclude old include_fields for support old collection
        if (excludes != null) {
            excludes[excludes.length + 1] = "include_fields";
        } else {
            excludes = new String[]{"include_fields"};
        }
        try {
            BoolQueryBuilder boolQuery = buildCollectionQuery(ids, q, boundingBox);
            SearchResponse response = client.prepareSearch(arlasIndex)
                    .setFetchSource(includes, excludes)
                    .setFrom(from)
                    .setSize(size)
                    .setQuery(boolQuery).get();
            for (SearchHit hit : response.getHits().getHits()) {
                String source = hit.getSourceAsString();
                try {
                    if (!hit.getId().equals(META_COLLECTION_NAME)){
                        collections.add(new CollectionReference(hit.getId(), reader.readValue(source)));
                    }
                } catch (IOException e) {
                    throw new InternalServerErrorException("Can not fetch collection", e);
                }
            }
        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collections;
    }

    @Override
    public long countCollectionReferences(String[] ids, String q, BoundingBox boundingBox) throws ArlasException, IOException {
        long count = 0;
        try {
            BoolQueryBuilder boolQuery = buildCollectionQuery(ids, q, boundingBox);
            SearchResponse response = client.prepareSearch(arlasIndex)
                    .setQuery(boolQuery).get();
            count = response.getHits().getTotalHits();
        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return count;
    }

    public static BoolQueryBuilder buildCollectionQuery(String[] ids, String q, BoundingBox boundingBox) throws IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        int minimumShouldMatch = 0;
        if (ids != null && ids.length > 0) {
            for (String resourceIdValue : Arrays.asList(ids)) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery("dublin_core_element_name.identifier", resourceIdValue));
            }
            minimumShouldMatch = minimumShouldMatch + 1;
        } else if (q != null && q.length() > 0) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should((QueryBuilders.simpleQueryStringQuery(q).defaultOperator(Operator.AND).field("internal.fulltext")));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        if (boundingBox != null) {
            CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
            coordinatesBuilder.coordinate(boundingBox.getEast(), boundingBox.getSouth());
            coordinatesBuilder.coordinate(boundingBox.getEast(), boundingBox.getNorth());
            coordinatesBuilder.coordinate(boundingBox.getWest(), boundingBox.getNorth());
            coordinatesBuilder.coordinate(boundingBox.getWest(), boundingBox.getSouth());
            coordinatesBuilder.coordinate(boundingBox.getEast(), boundingBox.getSouth());
            PolygonBuilder polygonBuilder = new PolygonBuilder(coordinatesBuilder, ShapeBuilder.Orientation.LEFT);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoIntersectionQuery("dublin_core_element_name.coverage", polygonBuilder));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        orBoolQueryBuilder.minimumShouldMatch(minimumShouldMatch);
        boolQuery = boolQuery.filter(orBoolQueryBuilder);
        return boolQuery;
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences() throws ArlasException {
        List<CollectionReference> collections = new ArrayList<>();

        try {
            QueryBuilder qb = QueryBuilders.matchAllQuery();
            //Exclude old include_fields for support old collection
            SearchResponse scrollResp = client.prepareSearch(arlasIndex).setFetchSource(null, "include_fields")
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC).setScroll(new TimeValue(60000))
                    .setQuery(qb).setSize(100).get(); // max of 100 hits will be returned for each scroll
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    String source = hit.getSourceAsString();
                    if (!hit.getId().equals(META_COLLECTION_NAME)) {
                        try {
                            collections.add(new CollectionReference(hit.getId(), reader.readValue(source)));
                        } catch (IOException e) {
                            throw new InternalServerErrorException("Can not fetch collection", e);
                        }
                    }

                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
                        .execute().actionGet();
            }
            while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collections;
    }

    @Override
    public CollectionReference putCollectionReference(CollectionReference collectionReference)
            throws ArlasException {
        if (!collectionReference.collectionName.equals(META_COLLECTION_NAME)) {
            return putCollectionReferenceInternal(collectionReference);
        } else {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' is not allowed as a name for collections");
        }
    }

    @Override
    public void deleteCollectionReference(String ref) throws NotFoundException, InternalServerErrorException, NotAllowedException {
        if (ref != null && ref.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("Forbidden operation on '" + META_COLLECTION_NAME + "'");
        }
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

    private void checkCollectionReferenceParameters(CollectionReference collectionReference) throws ArlasException {
        GetMappingsResponse response;
        try {
            //check index
            response = client.admin().indices().prepareGetMappings(collectionReference.params.indexName)/*.setTypes(collectionReference.params.typeName)*/.get();
            if (response.getMappings().isEmpty()) {
                throw new NotFoundException("No types in " + collectionReference.params.indexName + ".");
            }

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
            if(collectionReference.params.excludeFields!=null && collectionReference.params.excludeFields!=""){
                List<String> excludeField = Arrays.asList(collectionReference.params.excludeFields.split(","));
                CheckParams.checkExcludeField(excludeField, fields);
            }

            try {
                AccessConstraintEnum.valueOf(collectionReference.params.inspire.inspireLimitationAccess.accessConstraints);
            } catch (IllegalArgumentException e) {
                String listOfAccessConstraintEnum = "";
                for (AccessConstraintEnum ace : AccessConstraintEnum.values()) {
                    listOfAccessConstraintEnum += "'" + ace.name() + "', ";
                }
                throw new InvalidParameterException("accessConstraints is invalid. Please choose one of : " + listOfAccessConstraintEnum);
            }
            try {
                InspireAccessClassificationEnum.valueOf(collectionReference.params.inspire.inspireLimitationAccess.classification);
            } catch (IllegalArgumentException e) {
                String listOfClassificationEnum = "";
                for (InspireAccessClassificationEnum iace : InspireAccessClassificationEnum.values()) {
                    listOfClassificationEnum += "'" + iace.name() + "', ";
                }
                throw new InvalidParameterException("Inspire Access Classification is invalid. Please choose one of : " + listOfClassificationEnum);
            }


            Iterator<String> indeces = response.getMappings().keysIt();
            while (indeces.hasNext()) {
                String index = indeces.next();
                //check type
                try {
                    if (!response.getMappings().get(index).containsKey(collectionReference.params.typeName)) {
                        throw new NotFoundException("Type " + collectionReference.params.typeName + " does not exist in " + collectionReference.params.indexName + ".");
                    }
                    Object properties = response.getMappings().get(index).get(collectionReference.params.typeName).sourceAsMap().get("properties");
                    if (properties == null) {
                        throw new NotFoundException("Unable to find properties from " + collectionReference.params.typeName + " in " + index + ".");
                    }
                } catch (Exception e) {
                    throw new NotFoundException("Unable to get " + collectionReference.params.typeName + " in " + index + ".");
                }

                //check fields
                if (!fields.isEmpty())
                    checkIndexMappingFields(index, collectionReference.params, fields.toArray(new String[fields.size()]));

            }
        } catch (ArlasException e) {
            throw e;
        } catch (IndexNotFoundException e) {
            throw new NotFoundException("Index " + collectionReference.params.indexName + " does not exist.");
        } catch (Exception e) {
            throw new NotFoundException("Unable to access " + collectionReference.params.typeName + " in " + collectionReference.params.indexName + ".");
        }
    }

    private CollectionReference putCollectionReferenceInternal(CollectionReference collectionReference) throws ArlasException {
        checkCollectionReferenceParameters(collectionReference);
        setDefaultInspireParameters(collectionReference);
        setDefaultDublinCoreParameters(collectionReference);
        IndexResponse response = null;
        try {
            response = client.prepareIndex(arlasIndex, "collection", collectionReference.collectionName)
                    .setSource(mapper.writeValueAsString(collectionReference.params), XContentType.JSON).get();
        } catch (JsonProcessingException e) {
            new InternalServerErrorException("Can not put collection " + collectionReference.collectionName, e);
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

    private void createMetaCollection(OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws ArlasException {
        CollectionReference collectionReference =  new CollectionReference();
        collectionReference.collectionName = META_COLLECTION_NAME;
        MetaCollectionReferenceParameters collectionReferenceParameters = new MetaCollectionReferenceParameters();
        collectionReferenceParameters.indexName = arlasIndex;
        collectionReferenceParameters.typeName = ARLAS_INDEX_MAPPING_NAME;
        collectionReferenceParameters.idPath = META_COLLECTION_ID_PATH;
        collectionReferenceParameters.geometryPath = META_COLLECTION_GEOMETRY_PATH;
        collectionReferenceParameters.centroidPath = META_COLLECTION_CENTROID_PATH;
        collectionReferenceParameters.timestampPath = META_COLLECTION_TIMESTAMP_PATH;
        collectionReferenceParameters.inspireConfigurationParameters = new OgcInspireConfigurationParameters();
        collectionReferenceParameters.inspireConfigurationParameters.reponsibleParty = ogcConfiguration.serviceProviderName;
        collectionReferenceParameters.inspireConfigurationParameters.reponsiblePartyRole = ogcConfiguration.serviceProviderRole;
        collectionReferenceParameters.inspireConfigurationParameters.creationDate = inspireConfiguration.servicesDateOfCreation;
        collectionReferenceParameters.inspireConfigurationParameters.setConformityParameter();
        collectionReferenceParameters.inspireConfigurationParameters.publicAccessLimitations = inspireConfiguration.publicAccessLimitations;
        collectionReferenceParameters.inspireConfigurationParameters.accessAndUseConditions = inspireConfiguration.accessAndUseConditions;
        collectionReference.params = collectionReferenceParameters;
        putCollectionReferenceInternal(collectionReference);
    }

    private void setDefaultInspireParameters(CollectionReference collectionReference) {
        if (collectionReference.params.inspire == null) {
            collectionReference.params.inspire = new Inspire();
        }
        if (collectionReference.params.inspire.inspireURI == null) {
            collectionReference.params.inspire.inspireURI = new InspireURI();
        }
        if (Strings.isNullOrEmpty(collectionReference.params.inspire.inspireURI.code)) {
            collectionReference.params.inspire.inspireURI.code = "ARLAS.INSPIRE." + collectionReference.collectionName.toUpperCase();
        }
        if (Strings.isNullOrEmpty(collectionReference.params.inspire.inspireURI.namespace)) {
            collectionReference.params.inspire.inspireURI.namespace = "ARLAS.INSPIRE." + collectionReference.collectionName.toUpperCase();
        }
    }

    private void setDefaultDublinCoreParameters(CollectionReference collectionReference) {
        if (Strings.isNullOrEmpty(collectionReference.params.dublinCoreElementName.title)) {
            collectionReference.params.dublinCoreElementName.title = DublinCoreElementName.DEFAULT_TITLE + " for " + collectionReference.collectionName;
        }
        if (Strings.isNullOrEmpty(collectionReference.params.dublinCoreElementName.description)) {
            collectionReference.params.dublinCoreElementName.description = DublinCoreElementName.DEFAULT_DESCRIPTION + " for " + collectionReference.collectionName;
        }
    }

    private void checkIndexMappingFields(String index, CollectionReferenceParameters collectionRefParams, String... fields) throws ArlasException {
        GetFieldMappingsResponse response = client.admin().indices().prepareGetFieldMappings(index).setTypes(collectionRefParams.typeName).setFields(fields).get();
        for (String field : fields) {
            GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(index, collectionRefParams.typeName, field);
            if (data == null || data.isNull()) {
                throw new NotFoundException("Unable to find " + field + " from " + collectionRefParams.typeName + " in " + index + ".");
            } else {
                if (field.equals(collectionRefParams.timestampPath)) {
                    setTimestampFormat(collectionRefParams, data, field);
                }
            }
        }
    }

    private void setTimestampFormat(CollectionReferenceParameters collectionRefParams, GetFieldMappingsResponse.FieldMappingMetaData data, String fieldPath) {
        String[] fields = fieldPath.split("\\.");
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
