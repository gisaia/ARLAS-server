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
import io.arlas.filter.config.InitConfiguration;
import jakarta.ws.rs.HttpMethod;
import org.keycloak.representations.adapters.config.AdapterConfig;

import java.util.*;
import java.util.stream.Collectors;

public class ArlasAuthConfiguration {

    @JsonProperty("permission_url")
    public String permissionUrl;

    @JsonProperty("public_uris")
    public List<String> publicUris;

    @JsonProperty("header_user")
    public String headerUser;

    @JsonProperty("header_group")
    public String headerGroup;

    @JsonProperty("anonymous_value")
    public String anonymousValue;

    @JsonProperty("claim_roles")
    public String claimRoles;

    @JsonProperty("claim_permissions")
    public String claimPermissions;

    @Deprecated
    @JsonProperty("certificate_file")
    public String certificateFile;

    @JsonProperty("certificate_url")
    public String certificateUrl;

    @JsonProperty("access_token_ttl")
    public long accessTokenTTL;

    @JsonProperty("refresh_token_ttl")
    public long refreshTokenTTL;

    @JsonProperty("verify_token_ttl")
    public long verifyTokenTTL;

    @JsonProperty("keycloak")
    public final AdapterConfig keycloakConfiguration = new AdapterConfig();

    @JsonProperty("init")
    public final InitConfiguration initConfiguration = new InitConfiguration();

    private String publicRegex;

    public String getPublicRegex()  {
        // [swagger.*:*, persist.*:GET/POST/DELETE}]
        if (this.publicRegex == null) {
            final String allMethods = ":" + String.join("/", Arrays.asList(HttpMethod.DELETE, HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH));
            String pathToVerbs = Optional.ofNullable(this.publicUris)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(u -> !u.contains(":") ? u.concat(allMethods) : (u.endsWith(":*") ? u.replace(":*", allMethods) : u))
                    .flatMap(uri -> {
                        String path = uri.split(":")[0];
                        String verbs = uri.split(":")[1];
                        return Arrays.stream(verbs.split("/")).map(verb -> path.concat(":").concat(verb));
                    })
                    .collect(Collectors.joining("|"));
            this.publicRegex = "^(".concat(pathToVerbs).concat(")");
        }
        return this.publicRegex;
    }

    public void check() throws ArlasConfigurationException  {
        // collect all invalid verbs declared after 'path:'
        List<String> methods = Arrays.asList(HttpMethod.DELETE, HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);
        Set<String> invalidVerbs = Optional.ofNullable(this.publicUris)
                .orElse(Collections.emptyList())
                .stream()
                .filter(uri -> uri.contains(":") && !uri.endsWith(":*")) // no ':' or ends with ':*' then no further check is needed
                .flatMap(uri -> Arrays.stream(uri.split(":")[1].split("/")))
                .filter(verb -> !methods.contains(verb))
                .collect(Collectors.toSet());

        if (invalidVerbs.size() > 0) {
            throw new ArlasConfigurationException("Public uris and verbs list is invalid. Format is 'path' or 'path:*' " +
                    "or 'path:GET/POST/DELETE'. Invalid verbs: " + invalidVerbs);
        }
    }
}
