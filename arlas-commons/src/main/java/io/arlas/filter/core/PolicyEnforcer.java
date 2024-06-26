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

package io.arlas.filter.core;

import io.arlas.commons.cache.BaseCacheManager;
import io.arlas.commons.config.ArlasAuthConfiguration;
import jakarta.ws.rs.container.ContainerRequestFilter;

import java.util.ServiceLoader;

public interface PolicyEnforcer extends ContainerRequestFilter {

    PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception;

    PolicyEnforcer setCacheTimeout(long timeout) throws Exception;

    PolicyEnforcer setCacheManager(BaseCacheManager cacheManager);

    default boolean isEnabled() { return true; }

    static PolicyEnforcer newInstance(String defaultClass) {

        ServiceLoader<PolicyEnforcer> loader = ServiceLoader.load(PolicyEnforcer.class);
        return loader.stream()
                .filter(f -> defaultClass != null && f.get().getClass().getCanonicalName().equals(defaultClass))
                .toList().get(0).get();
    }
}
