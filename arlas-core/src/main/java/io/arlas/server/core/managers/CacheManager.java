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

package io.arlas.server.core.managers;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import io.arlas.commons.cache.BaseCacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.FieldType;

import java.util.Map;

public interface CacheManager extends BaseCacheManager {

    CollectionReference getCollectionReference(String ref);
    void putCollectionReference(String ref, CollectionReference col);
    void removeCollectionReference(String ref);

    FieldType getFieldType(String ref, String name);
    void putFieldType(String ref, String name, FieldType type);

    void putMapping(String indexName, Map<String, Map<String, Object>> exists);
    Map<String, Map<String, Object>> getMapping(String indexName);
    void removeMapping(String indexName);
}
