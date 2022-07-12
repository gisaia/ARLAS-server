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

package io.arlas.server.core.impl.elastic.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.arlas.server.core.exceptions.CollectionUnavailableException;
import io.arlas.server.core.services.CollectionReferenceService;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InternalServerErrorException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.utils.CollectionUtil;
import io.arlas.server.core.utils.ColumnFilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.arlas.server.core.model.CollectionReference.INCLUDE_FIELDS;

public class ElasticCollectionReferenceService extends CollectionReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticCollectionReferenceService.class);

    private static final String ARLAS_MAPPING_FILE_NAME = "arlas.mapping.json";
    private static final String  ARLAS_PUT_MAPPING_FILE_NAME = "arlas.put.mapping.json";

    private final ElasticClient client;

    public ElasticCollectionReferenceService(ElasticClient client, String arlasIndex, CacheManager cacheManager) {
        super(arlasIndex, cacheManager);
        this.client = client;
    }

    @Override
    public void initCollectionDatabase() throws ArlasException {
        InputStream mapping = this.getClass().getClassLoader().getResourceAsStream(ARLAS_MAPPING_FILE_NAME);
        InputStream putMapping = this.getClass().getClassLoader().getResourceAsStream(ARLAS_PUT_MAPPING_FILE_NAME);

        if (client.indexExists(arlasIndex)) {
            client.putMapping(arlasIndex, putMapping);
        } else {
            client.createIndex(arlasIndex, mapping);
        }
    }

    @Override
    protected CollectionReference getCollectionReferenceFromDao(String ref) throws ArlasException {
        return client.getCollectionReferenceFromES(arlasIndex, ref);
    }

    @Override
    protected Map<String, Map<String, Property>> getMappingFromDao(String indexName) throws ArlasException {
        return client.getMappings(indexName);
    }

    @Override
    protected Map<String, Map<String, Property>> getAllMappingsFromDao(String arlasIndex) throws ArlasException {
        return client.getMappings();
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences(Optional<String> columnFilter, Optional<String> organisations) throws ArlasException {
        List<CollectionReference> collections = new ArrayList<>();
        Set<String> allowedCollections = ColumnFilterUtil.getAllowedCollections(columnFilter);
        List<Hit<CollectionReferenceParameters>> hits = null;
        try {
            do {
                var requestBuilder = new SearchRequest.Builder()
                        .index(arlasIndex)
                        .source(b -> b.filter(c -> c.excludes(INCLUDE_FIELDS)))
                        .sort(b -> b.field(c -> c.field("_doc").order(SortOrder.Asc)))
                        .query(b -> b.matchAll(c -> c.queryName("matchAllQuery")))
                        .size(100);
                if (hits != null && hits.size() > 0) {
                    requestBuilder.searchAfter(hits.get(hits.size() - 1).sort());
                }
                hits = client.search(requestBuilder.build(), CollectionReferenceParameters.class).hits().hits();
    // TODO ES8
/*                for (Hit<CollectionReferenceParameters> hit : hits) {
                    CollectionReference colRef = new CollectionReference(hit.getId(), reader.readValue(source));
                    checkIfAllowedForOrganisations(colRef, organisations);
                    for (String c : allowedCollections) {
                        if ((c.endsWith("*") && hit.getId().startsWith(c.substring(0, c.indexOf("*"))))
                                || hit.getId().equals(c)) {
                            collections.add(colRef);
                            break;
                        }
                    }
                }*/
            } while (hits.size() > 0);
        } catch (NotFoundException e) {
            throw new InternalServerErrorException("Unreachable collections", e);
        }

        return collections.stream().filter(c-> {
            try {
                return !getMapping(c.params.indexName).isEmpty();
            } catch (ArlasException e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteCollectionReference(String ref) throws ArlasException {
        DeleteResponse response = client.deleteDocument(arlasIndex, ref);
        if (response.result().equals(Result.NotFound)) {
            throw new NotFoundException("collection " + ref + " not found.");
        } else if (!response.result().equals(Result.Deleted)) {
            throw new InternalServerErrorException("Unable to delete collection : " + response.result());
        } else {
        //explicit clean-up cache
        cacheManager.removeCollectionReference(ref);
        }
    }

    @Override
    protected void putCollectionReferenceWithDao(CollectionReference collectionReference) throws ArlasException {
        IndexResponse response = client.index(arlasIndex, collectionReference.collectionName, collectionReference.params);
        if (!response.result().equals(Result.Created)) {
            throw new InternalServerErrorException("Unable to index collection : " + response.result());
        }
    }

    @Override
    public boolean isDateField(String field, String index) throws ArlasException {
        return client.isDateField(field, index);
    }
}
