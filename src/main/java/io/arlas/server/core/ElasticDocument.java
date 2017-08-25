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

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.model.CollectionReference;

public class ElasticDocument {

    public TransportClient client;
    
    public ElasticDocument(TransportClient client){
        this.client = client;
    }
    
    public Map<String,Object> getSource (CollectionReference collectionReference, String identifier) throws IOException {
        GetResponse response = client.prepareGet(collectionReference.params.indexName, collectionReference.params.typeName, identifier).get();
        return response.getSource();
    }
}
