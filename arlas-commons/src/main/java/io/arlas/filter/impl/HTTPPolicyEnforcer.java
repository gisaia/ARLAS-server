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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.filter.core.PolicyEnforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

@Provider
@Priority(Priorities.AUTHORIZATION)
/*
  This is the policy enforcer to be used with Arlas Auth in Arlas Server.
  Set ARLAS_AUTH_POLICY_CLASS=io.arlas.filter.impl.HTTPPolicyEnforcer
 */
public class HTTPPolicyEnforcer extends AbstractPolicyEnforcer {
    private final Logger LOGGER = LoggerFactory.getLogger(HTTPPolicyEnforcer.class);
    private final Client client = ClientBuilder.newClient();
    private WebTarget resource;

    public HTTPPolicyEnforcer() {}

    @Override
    public PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception {
        super.setAuthConf(conf);
        this.resource = client.target(authConf.permissionUrl);
        return this;
    }

    @Override
    protected Map<String, Object> getRolesClaim(Object token, Optional<String> org) {
        Claim jwtClaimRoles = ((DecodedJWT) token).getClaim(authConf.claimRoles);
        if (!jwtClaimRoles.isNull()) {
            return jwtClaimRoles.asMap();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    protected Object getObjectToken(String accessToken, String orgFilter) throws Exception {
        boolean isApiKey = accessToken.startsWith(ARLAS_API_KEY);
        LOGGER.debug("accessToken=" + (isApiKey ? accessToken.split(":")[1] : decodeToken(accessToken)));
        String token = getPermission(accessToken + orgFilter);
        if (token == null) {
            Invocation.Builder request = orgFilter == null ? resource.request() : resource.queryParam(ARLAS_ORG_FILTER, orgFilter).request();
            if (isApiKey) {
                request.header(ARLAS_API_KEY_ID, accessToken.split(":")[1]);
                request.header(ARLAS_API_KEY_SECRET, accessToken.split(":")[2]);
            } else {
                request.header(HttpHeaders.AUTHORIZATION, "bearer " + accessToken);
            }
            request.accept(MediaType.APPLICATION_JSON);
            Response response = request.get();

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                token = response.readEntity(String.class);
                LOGGER.debug("Got permission token=" + token);
            } else {
                throw new ArlasException("Impossible to get permissions with given access token:" + accessToken);
            }
            putPermission(accessToken + orgFilter, token);
        }
        LOGGER.debug("RPT=" + decodeToken(token));
        return JWT.decode(token);
    }
}
