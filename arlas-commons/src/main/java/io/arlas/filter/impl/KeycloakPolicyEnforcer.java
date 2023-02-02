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

import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.filter.core.PolicyEnforcer;
import org.keycloak.TokenVerifier;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;
import java.util.*;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class KeycloakPolicyEnforcer extends AbstractPolicyEnforcer {
    private final Logger LOGGER = LoggerFactory.getLogger(KeycloakPolicyEnforcer.class);
    private AuthzClient authzClient;

    @Override
    public PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception {
        super.setAuthConf(conf);
        this.authzClient = AuthzClient.create(new Configuration(this.authConf.keycloakConfiguration.getAuthServerUrl(),
                this.authConf.keycloakConfiguration.getRealm(), this.authConf.keycloakConfiguration.getResource(),
                this.authConf.keycloakConfiguration.getCredentials(), null)
        );
        return this;
    }

    @Override
    protected Object getObjectToken(String accessToken, String orgFilter) throws Exception {
        LOGGER.debug("accessToken=" + decodeToken(accessToken));
        String token = cacheManager.getPermission(accessToken);
        if (token == null) {
            token = authzClient.authorization(accessToken)
                    .authorize(new AuthorizationRequest())
                    .getToken();
            cacheManager.putPermission(accessToken, token);
        }
        LOGGER.debug("RPT=" + decodeToken(token));
        return TokenVerifier.create(token, AccessToken.class).getToken();
    }

    @Override
    protected String getSubject(Object token) {
        return ((AccessToken)token).getSubject();
    }

    @Override
    protected Map<String, Object> getRolesClaim(Object token, Optional<String> org) {
        return Collections.singletonMap(org.orElse(""),
                ((AccessToken)token).getResourceAccess(authConf.keycloakConfiguration.getResource()).getRoles().stream().toList());
    }

    @Override
    protected Set<String> getPermissionsClaim(Object token){
        return new HashSet(((AccessToken) token).getAuthorization().getPermissions().stream()
                .map(Permission::getResourceName).toList());
    }
}
