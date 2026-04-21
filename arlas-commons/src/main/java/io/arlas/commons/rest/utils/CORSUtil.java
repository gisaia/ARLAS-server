package io.arlas.commons.rest.utils;

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

import io.arlas.commons.config.ArlasCorsConfiguration;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.container.ContainerResponseFilter;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.handler.CrossOriginHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CORSUtil {

    public static void configureCors(Environment environment, ArlasCorsConfiguration configuration) {
        if (configuration.enabled) {
            setCors(environment,configuration);
        } else {
            environment.jersey().register((ContainerResponseFilter) (req, res) ->
                    res.getHeaders().add("Access-Control-Expose-Headers", "WWW-Authenticate")
            );
        }
    }

    private static void setCors(Environment environment, ArlasCorsConfiguration configuration) {
        CrossOriginHandler corsHandler = new CrossOriginHandler();
        corsHandler.setAllowedOriginPatterns(Set.of(configuration.allowedOrigins.split(",")));
        corsHandler.setAllowedHeaders(Set.of(configuration.allowedHeaders.split(",")));
        corsHandler.setAllowedMethods(Set.of(configuration.allowedMethods.split(",")));
        corsHandler.setAllowCredentials(configuration.allowedCredentials);
        Set<String> exposedHeaders = new HashSet<>(Arrays.asList(configuration.exposedHeaders.split(",")));
        exposedHeaders.add(HttpHeader.WWW_AUTHENTICATE.asString());
        corsHandler.setExposedHeaders(exposedHeaders);
        ((ServletContextHandler) environment.getApplicationContext()).insertHandler(corsHandler);
    }
}
