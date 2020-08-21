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

package io.arlas.server.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.server.exceptions.ArlasConfigurationException;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.List;

public class ArlasBaseConfiguration extends Configuration {

    @JsonProperty("zipkin")
    public ZipkinFactory zipkinConfiguration;

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @JsonProperty("elastic")
    public ElasticConfiguration elasticConfiguration;

    @JsonProperty("arlas_auth")
    public ArlasAuthConfiguration arlasAuthConfiguration;

    @JsonProperty("arlas_cors")
    public ArlasCorsConfiguration arlarsCorsConfiguration;

    @JsonProperty("arlas_database_factory_class")
    public String arlasDatabaseFactoryClass;

    public static final String FLATTEN_CHAR = "_";

    public void check() throws ArlasConfigurationException {
        elasticConfiguration.check();

        if (zipkinConfiguration == null) {
            throw new ArlasConfigurationException("Zipkin configuration missing in config file.");
        }
        if (swaggerBundleConfiguration == null) {
            throw new ArlasConfigurationException("Swagger configuration missing in config file.");
        }
        if (arlasAuthConfiguration == null) {
            arlasAuthConfiguration = new ArlasAuthConfiguration();
            arlasAuthConfiguration.enabled = false;
        } else {
            arlasAuthConfiguration.check();
        }
        if(arlarsCorsConfiguration == null) {
            arlarsCorsConfiguration = new ArlasCorsConfiguration();
            arlarsCorsConfiguration.enabled = false;
        }
        if (arlasDatabaseFactoryClass == null) {
            throw new ArlasConfigurationException("arlas_database_factory_class is missing");
        }
    }
}
