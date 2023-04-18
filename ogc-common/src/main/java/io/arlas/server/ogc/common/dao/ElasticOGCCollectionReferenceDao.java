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

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InternalServerErrorException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.model.CollectionReferences;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.utils.BoundingBox;
import io.arlas.server.core.utils.CollectionUtil;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.requestfilter.ElasticFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ElasticOGCCollectionReferenceDao implements OGCCollectionReferenceDao {

    private final ElasticClient client;
    private final String arlasIndex;
    private final Service service;
    private final CollectionReferenceService collectionReferenceService;

    public ElasticOGCCollectionReferenceDao(ElasticClient client, CollectionReferenceService collectionReferenceService, String index, Service service) {
        this.client = client;
        this.collectionReferenceService = collectionReferenceService;
        this.arlasIndex = index;
        this.service = service;
    }

    @Override
    public CollectionReferences getCollectionReferences(String[] includes, String[] excludes, int size, int from,
                                                        String[] ids, String q, String constraint, BoundingBox boundingBox) throws ArlasException, IOException {

        BoolQuery.Builder ogcBoolQuery =  new BoolQuery.Builder();
        BoolQuery.Builder ogcIdsQBoundingBoxBoolQuery = ElasticFilter.filter(ids, "dublin_core_element_name.identifier", q, "internal.fulltext", boundingBox, "dublin_core_element_name.coverage");
        BoolQuery.Builder ogcConstraintBoolQuery = ElasticFilter.filter(constraint, getMetacollectionDescription(), service);
        ogcBoolQuery.filter(ogcIdsQBoundingBoxBoolQuery.build()._toQuery()).filter(ogcConstraintBoolQuery.build()._toQuery());
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    @Override
    public CollectionReferences getCollectionReferencesExceptOne(String[] includes, String[] excludes, int size, int from,
                                                                 String[] ids, String q, String constraint, BoundingBox boundingBox, CollectionReference collectionReferenceToRemove) throws ArlasException, IOException {

        BoolQuery.Builder ogcBoolQuery = new BoolQuery.Builder();
        BoolQuery.Builder ogcIdsQBoundingBoxBoolQuery = ElasticFilter.filter(ids, "dublin_core_element_name.identifier", q, "internal.fulltext", boundingBox, "dublin_core_element_name.coverage");
        BoolQuery.Builder ogcConstraintBoolQuery = ElasticFilter.filter(constraint, getMetacollectionDescription(), service);
        ogcBoolQuery.filter(ogcIdsQBoundingBoxBoolQuery.build()._toQuery()).filter(ogcConstraintBoolQuery.build()._toQuery());
        ogcBoolQuery.filter(new BoolQuery.Builder().mustNot(MatchQuery.of(builder -> builder.field("dublin_core_element_name.identifier")
                .query(FieldValue.of(collectionReferenceToRemove.params.dublinCoreElementName.identifier)))._toQuery()).build()._toQuery());
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    @Override
    public CollectionReferences getAllCollectionReferencesExceptOne(String[] includes, String[] excludes, int size, int from,
                                                                  CollectionReference collectionReferenceToRemove) throws ArlasException {

        BoolQuery.Builder ogcBoolQuery = new BoolQuery.Builder();
        ogcBoolQuery.filter(new BoolQuery.Builder().mustNot(MatchQuery.of(builder -> builder.field("dublin_core_element_name.identifier")
                .query(FieldValue.of(collectionReferenceToRemove.params.dublinCoreElementName.identifier)))._toQuery()).build()._toQuery());
        return getCollectionReferences(ogcBoolQuery, includes, excludes, size, from);
    }

    private CollectionReferences getCollectionReferences(BoolQuery.Builder boolQueryBuilder, String[] includes, String[] excludes, int size, int from) throws ArlasException {
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
        SourceFilter sourceFilter = new SourceFilter.Builder().excludes(Arrays.asList(excludes)).includes(Arrays.asList(includes)).build();
        SourceConfig sourceConfig = new SourceConfig.Builder().filter(sourceFilter).build();
        try {
            SearchRequest request = SearchRequest.of(r -> r
                            .index(arlasIndex)
                            .source(sourceConfig)
                            .from(from).size(size)
                            .query(boolQueryBuilder.build()._toQuery()));
            SearchResponse<CollectionReferenceParameters> response = client.search(request, CollectionReferenceParameters.class);
            List<Hit<CollectionReferenceParameters>> hits = client.search(request, CollectionReferenceParameters.class).hits().hits();
            collectionReferences.totalCollectionReferences = response.hits().total().value();
            for (Hit<CollectionReferenceParameters> hit : hits) {
                    collectionReferences.collectionReferences.add(new CollectionReference(hit.id(), hit.source()));
            }
            collectionReferences.nbCollectionReferences = collectionReferences.collectionReferences.size();
        } catch (NotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }
        return collectionReferences;
    }

    private CollectionReferenceDescription getMetacollectionDescription() throws ArlasException {
        CollectionReference metaCollection = client.getCollectionReferenceFromES(arlasIndex, "metacollection");
        return collectionReferenceService.describeCollection(metaCollection);
    }
}
