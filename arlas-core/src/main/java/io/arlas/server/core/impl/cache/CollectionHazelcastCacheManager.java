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

package io.arlas.server.core.impl.cache;

import io.arlas.commons.cache.BaseHazelcastCacheManager;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This cache holds a replicated map (named 'collections') for storing the collection references
 * and one replicated map per collection (named '<collection name>') for storing the elastic types.
 */
public class CollectionHazelcastCacheManager extends BaseHazelcastCacheManager implements CacheManager {
    Logger LOGGER = LoggerFactory.getLogger(CollectionHazelcastCacheManager.class);

    public CollectionHazelcastCacheManager(int cacheTimeout) {
        super(cacheTimeout);
    }

    @Override
    public CollectionReference getCollectionReference(String ref) {
        return (CollectionReference) getObject("collections", ref);
    }

    @Override
    public void putCollectionReference(String ref, CollectionReference col) {
        putObject("collections", ref, col);
        LOGGER.debug("Clearing field types of collection '" + ref + "' from cache");
        this.instance.getReplicatedMap(ref).clear();
    }

    @Override
    public void removeCollectionReference(String ref) {
        removeObject("collections", ref);
        LOGGER.debug("Clearing field types of collection '" + ref + "' from cache");
        this.instance.getReplicatedMap(ref).clear();
    }

    @Override
    public FieldType getFieldType(String ref, String name) {
        return (FieldType) getObject(ref, name);
    }

    @Override
    public void putFieldType(String ref, String name, FieldType type) {
        putObject(ref, name, type);
    }

    @Override
    public void putMapping(String indexName, Map<String, Map<String, Object>> mapping) {
        putObject("mappings", indexName, mapping);
    }

    @Override
    public Map<String, Map<String, Object>> getMapping(String indexName) {
        return (Map<String, Map<String, Object>>) getObject("mappings", indexName);
    }

    @Override
    public void removeMapping(String indexName) {
        removeObject("mappings", indexName);
    }
}
