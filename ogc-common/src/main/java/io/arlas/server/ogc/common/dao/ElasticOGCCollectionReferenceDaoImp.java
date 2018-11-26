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
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.CollectionReferences;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.requestfilter.ElasticFilter;
import io.arlas.server.utils.BoundingBox;
import io.arlas.server.utils.ElasticTool;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;

public class ElasticOGCCollectionReferenceDaoImp implements OGCCollectionReferenceDao {

    Client client;
    String arlasIndex;
    Service service;

    private static ObjectMapper mapper;
    private static ObjectReader reader;
    private static final String ARLAS_INDEX_MAPPING_NAME = "collection";

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    public ElasticOGCCollectionReferenceDaoImp(Client client, String index, Service service) {
        this.client = client;
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
                                                                  CollectionReference collectionReferenceToRemove) throws ArlasException, IOException {

        BoolQueryBuilder ogcBoolQuery = QueryBuilders.boolQuery();
        ogcBoolQuery.filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery("dublin_core_element_name.identifier", collectionReferenceToRemove.params.dublinCoreElementName.identifier)));
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    private CollectionReferences getCollectionReferences(BoolQueryBuilder boolQueryBuilder, String[] includes, String[] excludes, int size, int from) throws ArlasException {
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
                    .setQuery(boolQueryBuilder).get();

            collectionReferences.totalCollectionReferences = response.getHits().getTotalHits();
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
        ElasticAdmin elasticAdmin = new ElasticAdmin(client);
        CollectionReference metaCollection = ElasticTool.getCollectionReferenceFromES(client, arlasIndex, ARLAS_INDEX_MAPPING_NAME, reader, "metacollection");
        return elasticAdmin.describeCollection(metaCollection);
    }
}
