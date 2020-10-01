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

package io.arlas.server.impl.cache;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.managers.CacheManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.ElasticType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This cache holds a replicated map (named 'collections') for storing the collection references
 * and one replicated map per collection (named '<collection name>') for storing the elastic types.
 */
public class HazelcastCacheManager implements CacheManager {
    Logger LOGGER = LoggerFactory.getLogger(HazelcastCacheManager.class);
    final private Config hzConfig;
    final private ArlasServerConfiguration arlasConfig;
    private HazelcastInstance instance;

    public HazelcastCacheManager(ArlasServerConfiguration configuration) {
        this.arlasConfig = configuration;
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
            this.instance.getReplicatedMap("collections").put(ref, col, arlasConfig.arlasCacheTimeout, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap("collections").put(ref, col, arlasConfig.arlasCacheTimeout, TimeUnit.SECONDS);
        }
        LOGGER.debug("Clearing elastic types of collection '" + ref + "' from cache");
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
        LOGGER.debug("Clearing elastic types of collection '" + ref + "' from cache");
        this.instance.getReplicatedMap(ref).clear();
    }

    @Override
    public ElasticType getElasticType(String ref, String name) {
        ElasticType t;
        try {
            t = (ElasticType) this.instance.getReplicatedMap(ref).get(name);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            t = (ElasticType) this.instance.getReplicatedMap(ref).get(name);
        }
        LOGGER.debug("Returning elastic type '" + name + "' for collection '" + ref + "' from cache with value " + (t == null ? "null" : t.elasticType));
        return t;
    }

    @Override
    public void putElasticType(String ref, String name, ElasticType type) {
        LOGGER.debug("Inserting elastic type '" + name + "' for collection '" + ref + "' in cache with value " + (type == null ? "null" : type.elasticType));
        try {
            this.instance.getReplicatedMap(ref).put(name, type, arlasConfig.arlasCacheTimeout, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap(ref).put(name, type, arlasConfig.arlasCacheTimeout, TimeUnit.SECONDS);
        }
    }
}
