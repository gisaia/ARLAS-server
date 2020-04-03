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

package io.arlas.server.ogc.common.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.impl.elastic.utils.ElasticClient;
import io.arlas.server.impl.elastic.utils.ElasticTool;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.CollectionReferences;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.requestfilter.ElasticFilter;
import io.arlas.server.utils.BoundingBox;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ElasticOGCCollectionReferenceDao implements OGCCollectionReferenceDao {

    private final ElasticClient client;
    private final String arlasIndex;
    private final Service service;
    private final CollectionReferenceDao collectionReferenceDao;

    private static ObjectMapper mapper;
    private static ObjectReader reader;

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    public ElasticOGCCollectionReferenceDao(ElasticClient client, CollectionReferenceDao collectionReferenceDao, String index, Service service) {
        this.client = client;
        this.collectionReferenceDao = collectionReferenceDao;
        this.arlasIndex = index;
        this.service = service;
    }

    @Override
    public CollectionReferences getCollectionReferences(String[] includes, String[] excludes, int size, int from,
                                                        String[] ids, String q, String constraint, BoundingBox boundingBox) throws ArlasException, IOException {

        BoolQueryBuilder ogcBoolQuery = QueryBuilders.boolQuery();
        BoolQueryBuilder ogcIdsQBoundingBoxBoolQuery = ElasticFilter.filter(ids, "dublin_core_element_name.identifier", q, "internal.fulltext", boundingBox, "dublin_core_element_name.coverage");
        BoolQueryBuilder ogcConstraintBoolQuery = ElasticFilter.filter(constraint, getMetacollectionDescription(), service);
        ogcBoolQuery.filter(ogcIdsQBoundingBoxBoolQuery).filter(ogcConstraintBoolQuery);
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    @Override
    public CollectionReferences getCollectionReferencesExceptOne(String[] includes, String[] excludes, int size, int from,
                                                                 String[] ids, String q, String constraint, BoundingBox boundingBox, CollectionReference collectionReferenceToRemove) throws ArlasException, IOException {

        BoolQueryBuilder ogcBoolQuery = QueryBuilders.boolQuery();
        BoolQueryBuilder ogcIdsQBoundingBoxBoolQuery = ElasticFilter.filter(ids, "dublin_core_element_name.identifier", q, "internal.fulltext", boundingBox, "dublin_core_element_name.coverage");
        BoolQueryBuilder ogcConstraintBoolQuery = ElasticFilter.filter(constraint, getMetacollectionDescription(), service);
        ogcBoolQuery.filter(ogcIdsQBoundingBoxBoolQuery).filter(ogcConstraintBoolQuery);
        ogcBoolQuery.filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery("dublin_core_element_name.identifier", collectionReferenceToRemove.params.dublinCoreElementName.identifier)));
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    @Override
    public CollectionReferences getAllCollectionReferencesExceptOne(String[] includes, String[] excludes, int size, int from,
                                                                  CollectionReference collectionReferenceToRemove) throws ArlasException {

        BoolQueryBuilder ogcBoolQuery = QueryBuilders.boolQuery();
        ogcBoolQuery.filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery("dublin_core_element_name.identifier", collectionReferenceToRemove.params.dublinCoreElementName.identifier)));
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    private CollectionReferences getCollectionReferences(BoolQueryBuilder boolQueryBuilder, String[] includes, String[] excludes, int size, int from) throws ArlasException {
        CollectionReferences collectionReferences = new CollectionReferences();
        collectionReferences.collectionReferences = new ArrayList<>();

        //Exclude old include_fields for support old collection
        if (excludes != null) {
            String[] copy = Arrays.copyOf(excludes, excludes.length + 1);
            copy[excludes.length] = "include_fields";
            excludes = copy;
        } else {
            excludes = new String[]{"include_fields"};
        }
        try {
            SearchRequest request = new SearchRequest(arlasIndex);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQueryBuilder).from(from).size(size)
                .fetchSource(includes, excludes);
            request.source(searchSourceBuilder);
            SearchResponse response = client.search(request);

            collectionReferences.totalCollectionReferences = response.getHits().getTotalHits().value;
            for (SearchHit hit : response.getHits().getHits()) {
                String source = hit.getSourceAsString();
                try {
                    collectionReferences.collectionReferences.add(new CollectionReference(hit.getId(), mapper.readerFor(CollectionReferenceParameters.class).readValue(source)));

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

    private CollectionReferenceDescription getMetacollectionDescription() throws ArlasException, IOException {
        CollectionReference metaCollection = ElasticTool.getCollectionReferenceFromES(client, arlasIndex, reader, "metacollection");
        return collectionReferenceDao.describeCollection(metaCollection);
    }
}
