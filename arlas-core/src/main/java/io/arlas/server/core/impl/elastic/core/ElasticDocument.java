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

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.model.CollectionReference;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class ElasticDocument {

    public ElasticClient client;

    public ElasticDocument(ElasticClient client) {
        this.client = client;
    }

    public Map<String, Object> getSource(CollectionReference collectionReference, String identifier, String[] includes) throws ArlasException {
        String[] excludes = collectionReference.params.excludeFields.split(",");
        SearchRequest request = SearchRequest.of(r -> r
                        .index(collectionReference.params.indexName)
                        .source(b -> b.filter(c -> c.excludes(Arrays.asList(excludes)).includes(Arrays.asList(includes))))
                        .query(b -> b.term(c -> c.field(collectionReference.params.idPath).value(identifier)))
        );

        Optional<Hit<Map>> hits = client.search(request).hits().hits().stream().findFirst();
        return hits.<Map<String, Object>>map(Hit::source).orElse(null);
    }
}
