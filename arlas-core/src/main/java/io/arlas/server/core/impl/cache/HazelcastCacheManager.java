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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This cache holds a replicated map (named 'collections') for storing the collection references
 * and one replicated map per collection (named '<collection name>') for storing the elastic types.
 */
public class HazelcastCacheManager implements CacheManager {
    Logger LOGGER = LoggerFactory.getLogger(HazelcastCacheManager.class);
    final private Config hzConfig;
    final private int cacheTimeout;
    private HazelcastInstance instance;

    public HazelcastCacheManager(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
        this.hzConfig = new Config();
        hzConfig.setProperty( "hazelcast.phone.home.enabled", "false" );
        // no need to expose the following env variable as a server configuration as it is set by Arlas Cloud if needed
        String dns = System.getenv("ARLAS_CLOUD_SERVER_DNS");
        if (dns != null) {
            LOGGER.info("Setting up Hazelcast to use Kubernetes service DNS " + dns);
            this.hzConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            this.hzConfig.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", dns)
                    .setProperty("service-dns-timeout", "60");
        }
        init();
    }

    private void init() {
        LOGGER.info("Starting Hazelcast member");
        this.instance = Hazelcast.newHazelcastInstance(this.hzConfig);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOGGER.info("Stopping Hazelcast member");
        instance.shutdown();
    }

    @Override
    public CollectionReference getCollectionReference(String ref) {
        CollectionReference c;
        try {
            c = (CollectionReference) this.instance.getReplicatedMap("collections").get(ref);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            c = (CollectionReference) this.instance.getReplicatedMap("collections").get(ref);
        }
        LOGGER.debug("Returning collection reference '" + ref + "' from cache with value " + (c == null ? "null" : c.collectionName));
        return c;
    }

    @Override
    public void putCollectionReference(String ref, CollectionReference col) {
        LOGGER.debug("Inserting collection reference '" + ref + "' in cache");
        try {
            this.instance.getReplicatedMap("collections").put(ref, col, cacheTimeout, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap("collections").put(ref, col, cacheTimeout, TimeUnit.SECONDS);
        }
        LOGGER.debug("Clearing field types of collection '" + ref + "' from cache");
        this.instance.getReplicatedMap(ref).clear();
    }

    @Override
    public void removeCollectionReference(String ref) {
        LOGGER.debug("Clearing collection '" + ref + "' from cache");
        try {
            this.instance.getReplicatedMap("collections").remove(ref);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap("collections").remove(ref);
        }
        LOGGER.debug("Clearing field types of collection '" + ref + "' from cache");
        this.instance.getReplicatedMap(ref).clear();
    }

    @Override
    public FieldType getFieldType(String ref, String name) {
        FieldType t;
        try {
            t = (FieldType) this.instance.getReplicatedMap(ref).get(name);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            t = (FieldType) this.instance.getReplicatedMap(ref).get(name);
        }
        LOGGER.debug("Returning field type '" + name + "' for collection '" + ref + "' from cache with value " + (t == null ? "null" : t.fieldType));
        return t;
    }

    @Override
    public void putFieldType(String ref, String name, FieldType type) {
        LOGGER.debug("Inserting field type '" + name + "' for collection '" + ref + "' in cache with value " + (type == null ? "null" : type.fieldType));
        try {
            this.instance.getReplicatedMap(ref).put(name, type, cacheTimeout, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap(ref).put(name, type, cacheTimeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void putMapping(String indexName, Map<String, LinkedHashMap> mapping) {
        LOGGER.debug("Inserting mapping for index '" + indexName + "' in cache");
        try {
            this.instance.getReplicatedMap("mappings").put(indexName, mapping);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap("mappings").put(indexName, mapping);
        }
    }

    @Override
    public Map<String, LinkedHashMap> getMapping(String indexName) {
        Map<String, LinkedHashMap> mapping;
        try {
            mapping = (Map<String, LinkedHashMap>) this.instance.getReplicatedMap("mappings").get(indexName);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            mapping = (Map<String, LinkedHashMap>) this.instance.getReplicatedMap("mappings").get(indexName);
        }
        LOGGER.debug("Returning mapping for '" + indexName + "' from cache");
        return mapping;
    }

    @Override
    public void removeMapping(String indexName) {
        LOGGER.debug("Clearing mapping '" + indexName + "' from cache");
        try {
            this.instance.getReplicatedMap("mappings").remove(indexName);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap("mappings").remove(indexName);
        }
    }
}
