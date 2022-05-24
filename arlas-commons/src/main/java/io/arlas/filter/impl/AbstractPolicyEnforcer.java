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

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.arlas.commons.cache.BaseCacheManager;
import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.commons.utils.StringUtil;
import io.arlas.filter.config.TechnicalRoles;
import io.arlas.filter.core.ArlasClaims;
import io.arlas.filter.core.PolicyEnforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.*;
import java.util.stream.Collectors;

import static io.arlas.filter.config.TechnicalRoles.VAR_ORG;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Provider
@Priority(Priorities.AUTHORIZATION)
public abstract class AbstractPolicyEnforcer implements PolicyEnforcer {
    private final Logger LOGGER = LoggerFactory.getLogger(AbstractPolicyEnforcer.class);
    protected ArlasAuthConfiguration authConf;
    protected BaseCacheManager cacheManager;
    protected boolean injectPermissions = true;

    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    protected AbstractPolicyEnforcer() {
    }

    @Override
    public PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception {
        this.authConf = conf;
        return this;
    }

    @Override
    public PolicyEnforcer setCacheManager(BaseCacheManager baseCacheManager) {
        this.cacheManager = baseCacheManager;
        return this;
    }

    protected abstract Object getObjectToken(String accessToken) throws Exception;

    protected String getSubject(Object token) {
        return ((DecodedJWT) token).getSubject();
    }

    protected Map<String, Object> getRolesClaim(Object token) {
        Claim jwtClaimRoles = ((DecodedJWT) token).getClaim(authConf.claimRoles);
        if (!jwtClaimRoles.isNull()) {
            return Collections.singletonMap("", jwtClaimRoles.asList(String.class));
        } else {
            return Collections.emptyMap();
        }
    }

    protected Set<String> getPermissionsClaim(Object token) {
        Claim jwtClaimPermissions = ((DecodedJWT) token).getClaim(authConf.claimPermissions);
        if (!jwtClaimPermissions.isNull()) {
            return jwtClaimPermissions.asList(String.class).stream().collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    private void addTechnicalRolesToPermissions(Set<String> permissions, Map<String, Object> roles) {
        if (injectPermissions) {
            LOGGER.debug("Adding permissions of org/roles " + roles.toString() + " from map technical roles "
                    + TechnicalRoles.getTechnicalRolesPermissions().toString() + " in existing permissions "
                    + permissions);
            roles.entrySet().stream().forEach(orgRoles -> {
                TechnicalRoles.getTechnicalRolesPermissions().entrySet().stream()
                        .filter(rolesPerm -> ((List<String>) orgRoles.getValue()).contains(rolesPerm.getKey()))
                        .filter(rolesPerm -> rolesPerm.getValue().size() > 0)
                        .forEach(rolesPerm -> permissions.addAll(rolesPerm.getValue().stream()
                                .map(rp -> ArlasClaims.replaceVar(rp, VAR_ORG, orgRoles.getKey())).toList()));
            });
        }
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        Transaction transaction = ElasticApm.currentTransaction();
        boolean isPublic = ctx.getUriInfo().getPath().concat(":").concat(ctx.getMethod()).matches(authConf.getPublicRegex());
        String header = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.toLowerCase().startsWith("bearer ")) {
            if (!isPublic && !"OPTIONS".equals(ctx.getMethod())) {
                ctx.abortWith(Response.status(UNAUTHORIZED).build());
            }
            return;
        }

        String accessToken = header.substring(7);
        try {
            Boolean ok = cacheManager.getDecision(getDecisionCacheKey(ctx, accessToken));
            if (ok != null && !ok) {
                ctx.abortWith(Response.status(FORBIDDEN).build());
            }
            Object token = getObjectToken(accessToken);
            ctx.getHeaders().remove(authConf.headerUser); // remove it in case it's been set manually
            String userId = getSubject(token);
            if (!StringUtil.isNullOrEmpty(userId)) {
                ctx.getHeaders().putSingle(authConf.headerUser, userId);
                LOGGER.debug("Add Header [" + authConf.headerUser + ": " + userId + "]");
                transaction.setUser(userId, "", "");
            }

            ctx.getHeaders().remove(authConf.headerGroup); // remove it in case it's been set manually
            Map<String, Object> roles = getRolesClaim(token);
            if (!roles.isEmpty()) {
                Set<String> groups = roles.values().stream()
                        .map(v -> (List<String>) v)
                        .flatMap(Collection::stream)
                        .filter(r -> r.toLowerCase().startsWith("group"))
                        .collect(Collectors.toSet());
                ctx.setProperty("groups", groups.stream().toList());
                ctx.getHeaders().put(authConf.headerGroup, groups.stream().toList());
                LOGGER.debug("Add Header [" + authConf.headerGroup + ": " + groups + "]");
            }

            Set<String> permissions = getPermissionsClaim(token);
            addTechnicalRolesToPermissions(permissions, roles);
            LOGGER.debug("Permissions: " + permissions.toString());
            if (!permissions.isEmpty()) {
                ArlasClaims arlasClaims = new ArlasClaims(permissions.stream().toList());
                ctx.setProperty("claims", arlasClaims.getRules());
                if ((ok != null && ok) || arlasClaims.isAllowed(ctx.getMethod(), ctx.getUriInfo().getPath())) {
                    arlasClaims.injectHeaders(ctx.getHeaders(), transaction);
                    cacheManager.putDecision(getDecisionCacheKey(ctx, accessToken), Boolean.TRUE);
                    return;
                }
            }
            if (isPublic) {
                cacheManager.putDecision(getDecisionCacheKey(ctx, accessToken), Boolean.TRUE);
                return;
            }
        } catch (Exception e) {
            LOGGER.warn("JWT verification failed.", e);
            if (!isPublic) {
                ctx.abortWith(Response.status(UNAUTHORIZED).build());
            }
            return;
        }
        cacheManager.putDecision(getDecisionCacheKey(ctx, accessToken), Boolean.FALSE);
        ctx.abortWith(Response.status(FORBIDDEN).build());
    }

    private String getDecisionCacheKey(ContainerRequestContext ctx, String accessToken) {
        return StringUtil.concat(ctx.getMethod(), ":", ctx.getUriInfo().getPath(), ":", accessToken);
    }

    protected String decodeToken(String token) {
        String[] chunks = token.split("\\.");
        return new String(decoder.decode(chunks[0])) + new String(decoder.decode(chunks[1]));
    }
}