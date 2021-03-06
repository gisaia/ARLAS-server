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

package io.arlas.server.auth;

import io.arlas.server.app.ArlasAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final ArlasAuthConfiguration authConf;

    public AuthenticationFilter(ArlasAuthConfiguration conf) {
        this.authConf = conf;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        requestContext.setProperty("public", authConf.publicUris);
        if (header == null || !header.toLowerCase().startsWith("bearer ")) {
            //If public end point and no authorize verb
            if ( !requestContext.getUriInfo().getPath().concat(":").concat(requestContext.getMethod()).matches(authConf.getPublicRegex())  && requestContext.getMethod() != "OPTIONS") {
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .header(HttpHeaders.WWW_AUTHENTICATE,
                                        "Bearer realm=\"ARLAS Server secured access\"")
                                .header(HttpHeaders.CONTENT_LOCATION, authConf.loginUrl)
                                .build());

            }
        }
    }
}