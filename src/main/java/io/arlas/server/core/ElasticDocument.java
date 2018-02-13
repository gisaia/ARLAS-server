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

package io.arlas.server.core;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.client.Client;

import io.arlas.server.model.CollectionReference;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class ElasticDocument {

    public Client client;

    public ElasticDocument(Client client){
        this.client = client;
    }
    
    public Map<String,Object> getSource (CollectionReference collectionReference, String identifier) throws  ExecutionException, InterruptedException {
        
        SearchHits hits = client
                .prepareSearch(collectionReference.params.indexName)
                .setQuery(QueryBuilders.matchQuery(collectionReference.params.idPath,identifier))
                .execute()
                .get()
                .getHits();
        Map<String,Object> response = null;
        if(hits.getHits().length>0){
            response = hits.getAt(0).getSource();
        }
        return response;
    }
}
