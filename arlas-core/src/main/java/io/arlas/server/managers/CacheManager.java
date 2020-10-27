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

package io.arlas.server.managers;

import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.ElasticType;

import java.util.LinkedHashMap;
import java.util.Map;

public interface CacheManager {

    CollectionReference getCollectionReference(String ref);
    void putCollectionReference(String ref, CollectionReference col);
    void removeCollectionReference(String ref);

    ElasticType getElasticType(String ref, String name);
    void putElasticType(String ref, String name, ElasticType type);

    void putMapping(String indexName, Map<String, LinkedHashMap> exists);
    Map<String, LinkedHashMap> getMapping(String indexName);
    void removeMapping(String indexName);
}
