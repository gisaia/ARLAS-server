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

package io.arlas.commons.cache;

import io.arlas.commons.utils.SelfExpiringHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a local cache implementation (no network replication)
 */
public class BaseLocalCacheManager implements BaseCacheManager {
    Logger LOGGER = LoggerFactory.getLogger(BaseLocalCacheManager.class);
    final protected long cacheTimeout;
    final protected Map<String, SelfExpiringHashMap<String, Object>> cache;

    public BaseLocalCacheManager(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Object getObject(String key, String ref) {
        Object c = this.cache.computeIfAbsent(key, k -> new SelfExpiringHashMap<>()).get(ref);
        LOGGER.debug(String.format("Returning {'%s':{'%s': '%s'}}", key, ref, (c == null ? "null" : c.toString())));
        return c;
    }

    @Override
    public void putObject(String key, String ref, Object o, long timeout) {
        LOGGER.debug(String.format("Inserting {'%s':{'%s': '%s'}}", key, ref, o));
        this.cache.computeIfAbsent(key, k -> new SelfExpiringHashMap<>()).put(ref, o, timeout * 1000L);
    }

    @Override
    public void putObject(String key, String ref, Object o) {
        putObject(key, ref, o, this.cacheTimeout);
    }

    @Override
    public void removeObject(String key, String ref) {
        LOGGER.debug(String.format("Clearing {'%s':{'%s': ''}}", key, ref));
        this.cache.computeIfAbsent(key, k -> new SelfExpiringHashMap<>()).remove(ref);
    }
}
