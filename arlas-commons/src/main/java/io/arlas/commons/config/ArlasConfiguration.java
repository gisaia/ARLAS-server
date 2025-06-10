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

package io.arlas.commons.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.dropwizard.core.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class ArlasConfiguration extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @JsonProperty("arlas_auth_policy_class")
    public String arlasAuthPolicyClass;

    @JsonProperty("arlas_auth")
    public ArlasAuthConfiguration arlasAuthConfiguration;

    @JsonProperty("arlas_cors")
    public ArlasCorsConfiguration arlasCorsConfiguration;

    @JsonProperty("arlas_cache_factory_class")
    public String arlasCacheFactoryClass;

    @JsonProperty("arlas-check-organisations")
    public Boolean arlasCheckOrganisations;

    @JsonProperty("arlas-cache-timeout")
    public int arlasCacheTimeout;

    public static final String FLATTEN_CHAR = "_";

    public void check() throws ArlasConfigurationException {
        if (swaggerBundleConfiguration == null) {
            throw new ArlasConfigurationException("Swagger configuration missing in config file.");
        }
        if (arlasAuthConfiguration == null) {
            arlasAuthConfiguration = new ArlasAuthConfiguration();
        }
        arlasAuthConfiguration.check();
        /** If arlas-check-organisations is not set, we consider that organisation-related checks should be performed. */
        if (arlasCheckOrganisations == null) {
            arlasCheckOrganisations = Boolean.TRUE;
        }
        if (arlasCorsConfiguration == null) {
            arlasCorsConfiguration = new ArlasCorsConfiguration();
            arlasCorsConfiguration.enabled = false;
        }
        if (arlasCacheFactoryClass == null) {
            arlasCacheFactoryClass = "io.arlas.commons.cache.NoCacheFactory";
        }
    }
}
