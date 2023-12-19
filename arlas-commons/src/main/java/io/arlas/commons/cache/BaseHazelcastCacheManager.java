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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * This cache holds a replicated map (named 'collections') for storing the collection references
 * and one replicated map per collection (named '<collection name>') for storing the elastic types.
 */
public class BaseHazelcastCacheManager implements BaseCacheManager {
    Logger LOGGER = LoggerFactory.getLogger(BaseHazelcastCacheManager.class);
    final private Config hzConfig;
    final protected int cacheTimeout;
    protected HazelcastInstance instance;

    public BaseHazelcastCacheManager(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
        this.hzConfig = new Config();
        hzConfig.setProperty( "hazelcast.phone.home.enabled", "false" );
        // no need to expose the following env variable as a server configuration as it is set by Arlas Cloud if needed
        String dns = System.getenv("ARLAS_CLOUD_SERVER_DNS");
        if (dns != null) {
            // https://github.com/hazelcast/hazelcast-kubernetes
            LOGGER.info("Setting up Hazelcast to use Kubernetes service DNS " + dns);
            this.hzConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            this.hzConfig.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", dns)
                    .setProperty("service-dns-timeout", "60");
        }
        init();
    }

    protected void init() {
        LOGGER.info("Starting Hazelcast member");
        this.instance = Hazelcast.newHazelcastInstance(this.hzConfig);
    }

    @Override
    public Object getObject(String key, String ref) {
        Object c;
        try {
            c = this.instance.getReplicatedMap(key).get(ref);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            c = this.instance.getReplicatedMap(key).get(ref);
        }
        LOGGER.debug(String.format("Returning {'%s':{'%s': '%s'}}", key, ref, (c == null ? "null" : c.toString())));
        return c;
    }

    @Override
    public void putObject(String key, String ref, Object o, long timeout) {
        LOGGER.debug(String.format("Inserting {'%s':{'%s': '%s'}}", key, ref, o));
        try {
            this.instance.getReplicatedMap(key).put(ref, o, timeout, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap(key).put(ref, o, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void putObject(String key, String ref, Object o) {
        putObject(key, ref, o, cacheTimeout);
    }

    @Override
    public void removeObject(String key, String ref) {
        LOGGER.debug(String.format("Clearing {'%s':{'%s': ''}}", key, ref));
        try {
            this.instance.getReplicatedMap(key).remove(ref);
        } catch (HazelcastInstanceNotActiveException e) { // recover from unexpected shutdown
            init();
            this.instance.getReplicatedMap(key).remove(ref);
        }
    }
}
