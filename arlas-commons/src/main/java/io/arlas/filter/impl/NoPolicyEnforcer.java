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
package io.arlas.filter.impl;

import io.arlas.commons.cache.BaseCacheManager;
import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.filter.core.PolicyEnforcer;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class NoPolicyEnforcer implements PolicyEnforcer {

    public NoPolicyEnforcer() {}

    @Override
    public boolean isEnabled() { return false; }

    @Override
    public PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception {
        return this;
    }

    @Override
    public PolicyEnforcer setCacheTimeout(long timeout) throws Exception {
        return this;
    }

    @Override
    public PolicyEnforcer setCacheManager(BaseCacheManager cacheManager) {
        return this;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {}
}
