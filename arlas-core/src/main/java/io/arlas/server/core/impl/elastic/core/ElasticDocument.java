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

package io.arlas.server.core.impl.elastic.core;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.model.CollectionReference;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Map;

public class ElasticDocument {

    public ElasticClient client;

    public ElasticDocument(ElasticClient client) {
        this.client = client;
    }

    public Map<String, Object> getSource(CollectionReference collectionReference, String identifier, String[] includes) throws ArlasException {
        String[] excludes = collectionReference.params.excludeFields.split(",");
        SearchRequest request = new SearchRequest(collectionReference.params.indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(collectionReference.params.idPath, identifier))
                .fetchSource(includes, excludes);
        request.source(searchSourceBuilder);
        SearchHits hits = client.search(request).getHits();
        Map<String, Object> response = null;
        if (hits.getHits().length > 0) {
            response = hits.getAt(0).getSourceAsMap();
        }
        return response;
    }
}
