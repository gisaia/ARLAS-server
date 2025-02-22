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
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.arlas.commons.rest.utils.ServerConstants.*;
import static io.arlas.filter.config.TechnicalRoles.VAR_ORG;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

@Provider
@Priority(Priorities.AUTHORIZATION)
public abstract class AbstractPolicyEnforcer implements PolicyEnforcer {
    public static final String EVENT_KIND = "event.kind";
    public static final String EVENT = "event";
    public static final String EVENT_CATEGORY = "event.category";
    public static final String IAM = "iam";
    public static final String EVENT_TYPE = "event.type";
    public static final String ALLOWED = "allowed";
    public static final String DENIED = "denied";
    public static final String REFERER = "Referer";
    public static final String HTTP_REQUEST_METHOD = "http.request.method";
    public static final String URL_PATH = "url.path";
    public static final String URL_QUERY = "url.query";
    public static final String USER_AGENT_ORIGINAL = "user_agent.original";
    public static final String HTTP_REQUEST_REFERRER = "http.request.referrer";
    public static final String CLIENT_ADDRESS = "client.address";
    public static final String CLIENT_IP = "client.ip";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String ORGANIZATION_NAME = "organization.name";
    public static final String USER_ID = "user.id";
    public static final String USER_EMAIL = "user.emaild";
    // This filter is used to protect private collection in public endpoint
    public static final String DUMMY_COLUMN_FILTER = "dummy:*";

    private final Logger LOGGER = LoggerFactory.getLogger(AbstractPolicyEnforcer.class);
    protected ArlasAuthConfiguration authConf;
    protected BaseCacheManager cacheManager;
    private long cacheTimeout;
    protected boolean injectPermissions = true;

    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    @Context
    private HttpServletRequest request;

    protected AbstractPolicyEnforcer() {
    }

    @Override
    public PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception {
        this.authConf = conf;
        return this;
    }

    @Override
    public PolicyEnforcer setCacheTimeout(long timeout) throws Exception {
        // max cache timeout is 60s for decisions
        this.cacheTimeout = Math.min(timeout, 60);
        return this;
    }

    @Override
    public PolicyEnforcer setCacheManager(BaseCacheManager baseCacheManager) {
        this.cacheManager = baseCacheManager;
        return this;
    }

    protected abstract Object getObjectToken(String accessToken, String orgFilter) throws Exception;

    protected String getSubject(Object token) {
        return ((DecodedJWT) token).getSubject();
    }

    protected Optional<String> getSubjectEmail(Object token) {
        Claim emailClaim = ((DecodedJWT) token).getClaim("email");
        return emailClaim.isMissing() || emailClaim.isNull() ? Optional.empty() : Optional.of(emailClaim.asString());
    }

    protected Map<String, Object> getRolesClaim(Object token, Optional<String> org) {
        Claim jwtClaimRoles = ((DecodedJWT) token).getClaim(authConf.claimRoles);
        if (!jwtClaimRoles.isMissing() && !jwtClaimRoles.isNull()) {
            return Collections.singletonMap(org.orElse(""), jwtClaimRoles.asList(String.class));
        } else {
            return Collections.emptyMap();
        }
    }

    protected Set<String> getPermissionsClaim(Object token) {
        Claim jwtClaimPermissions = ((DecodedJWT) token).getClaim(authConf.claimPermissions);
        if (!jwtClaimPermissions.isMissing() && !jwtClaimPermissions.isNull()) {
            return new HashSet<>(jwtClaimPermissions.asList(String.class));
        } else {
            return Collections.emptySet();
        }
    }

    private void addTechnicalRolesToPermissions(Set<String> permissions, Map<String, Object> roles) {
        if (injectPermissions) {
            LOGGER.debug("Adding permissions of org/roles " + roles.toString() + " in existing permissions "
                    + permissions);
            roles.forEach((key, value) -> TechnicalRoles.getTechnicalRolesPermissions().entrySet().stream()
                    .filter(rolesPerm -> ((List<String>) value).contains(rolesPerm.getKey()))
                    .filter(rolesPerm -> !rolesPerm.getValue().get("permissions").isEmpty())
                    .forEach(rolesPerm -> permissions.addAll(rolesPerm.getValue().get("permissions").stream()
                            .map(rp -> ArlasClaims.replaceVar(rp, VAR_ORG, key)).toList())));
            LOGGER.debug("Resulting permissions: " + permissions);
        }
    }

    private void logUAM(Consumer<String> level, String type, String log) {
        MDC.put(EVENT_CATEGORY, IAM);
        MDC.put(EVENT_TYPE, type);
        level.accept(log);
        MDC.remove(EVENT_TYPE);
        MDC.remove(EVENT_CATEGORY);
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException  {
        String path = ctx.getUriInfo().getPath();
        if (path.equals("auth")) {
            String targetPath = ctx.getHeaderString("X-Forwarded-Uri");
            LOGGER.debug("==============  Applying forward auth");
            LOGGER.debug(String.format("Calling filter for : %s %s",
                    ctx.getHeaderString("X-Forwarded-Method"),
                    targetPath));
            int q = targetPath.indexOf("?");
            String pathWithoutQueryParams = q != -1 ? targetPath.substring(0, q) : targetPath;
            // in forward auth context, we check the full path
            filter(ctx, ctx.getHeaderString("X-Forwarded-Method"), pathWithoutQueryParams, pathWithoutQueryParams);
        } else {
            LOGGER.debug("============== Ignoring forward auth");
            LOGGER.debug(String.format("Calling filter for : %s %s (path=%s)",
                    ctx.getMethod(),
                    request.getRequestURI(),
                    path));
            filter(ctx, ctx.getMethod(), path, request.getRequestURI());
        }
    }

    public void filter(ContainerRequestContext ctx, String method, String path, String fullPath) {
        String log = String.join(" ", method, fullPath);
        String ip = Optional.ofNullable(ctx.getHeaderString(X_FORWARDED_FOR))
                .orElseGet(() -> request.getRemoteAddr())
                .split(",")[0].trim();
        // Using Mapped Diagnostic Context to set attribute for Elasticsearch ECS format
        MDC.put(EVENT_KIND, EVENT);
        MDC.put(HTTP_REQUEST_METHOD, method);
        MDC.put(URL_PATH, fullPath);
        MDC.put(URL_QUERY, request.getQueryString());
        MDC.put(USER_AGENT_ORIGINAL, ctx.getHeaderString(HttpHeaders.USER_AGENT));
        MDC.put(HTTP_REQUEST_REFERRER, ctx.getHeaderString(REFERER));
        MDC.put(CLIENT_ADDRESS, ip);
        MDC.put(CLIENT_IP, ip);

        try {
            Transaction transaction = ElasticApm.currentTransaction();
            boolean isPublic = path.concat(":").concat(method).matches(authConf.getPublicRegex());
            boolean isApiKey = false;
            String keyIdHeader = ctx.getHeaderString(ARLAS_API_KEY_ID);
            String keySecretHeader = ctx.getHeaderString(ARLAS_API_KEY_SECRET);
            String authHeader = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (keyIdHeader != null && keySecretHeader != null) {
                isApiKey = true;
            } else {
                if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
                    if (!isPublic && !"OPTIONS".equals(method)) {
                        logUAM(LOGGER::warn, DENIED, "unauthorized (no token): " + log);
                        ctx.abortWith(Response.status(UNAUTHORIZED).build());
                    } else {
                        if (!"OPTIONS".equals(method)) {
                            logUAM(LOGGER::debug, ALLOWED, "public (no token): " + log);
                        }
                        // use a dummy CF in order to bypass the CFUtil and give access to public collections
                        ctx.getHeaders().add(COLUMN_FILTER, DUMMY_COLUMN_FILTER);
                    }
                    return;
                }
            }

            String accessToken = isApiKey ?
                    String.join(":", ARLAS_API_KEY, keyIdHeader, keySecretHeader)
                    : authHeader.substring(7);
            try {
                Boolean ok = getDecision(getDecisionCacheKey(ctx, method, fullPath, accessToken));
                if (ok != null && !ok) {
                    logUAM(LOGGER::warn, DENIED,"forbidden (from cache): " + log);
                    ctx.abortWith(Response.status(FORBIDDEN).build());
                    return;
                }
                String orgFilter = ctx.getHeaders().getFirst(ARLAS_ORG_FILTER);
                LOGGER.debug("ARLAS_ORG_FILTER=" + orgFilter);
                Object token = getObjectToken(accessToken, orgFilter);
                Set<String> permissions = getPermissionsClaim(token);
                Optional<String> org = Optional.ofNullable(new ArlasClaims(permissions.stream().toList()).getVariables().get(VAR_ORG));
                if(ctx.getHeaders().get(PARTITION_FILTER) != null){
                    ctx.getHeaders().remove(PARTITION_FILTER); // remove it in case it's been set manually
                }
                ctx.getHeaders().remove(authConf.headerUser); // remove it in case it's been set manually
                String userId = getSubject(token);
                if (!StringUtil.isNullOrEmpty(userId)) {
                    ctx.getHeaders().putSingle(authConf.headerUser, userId);
                    LOGGER.debug("Add Header [" + authConf.headerUser + ": " + userId + "]");
                    transaction.setUser(userId, "", "");
                    MDC.put(USER_ID, userId);
                    getSubjectEmail(token).ifPresent(s -> MDC.put(USER_EMAIL, s));
                    if (orgFilter != null) {
                        MDC.put(ORGANIZATION_NAME, orgFilter);
                    }
                }

                ctx.getHeaders().remove(authConf.headerGroup); // remove it in case it's been set manually
                Map<String, Object> roles = getRolesClaim(token, org);
                log = StringUtil.concat(log, String.format(" (orgs=%s)", roles.keySet()));
                Set<String> groups = Collections.emptySet();
                if (!roles.isEmpty()) {
                    groups = roles.values().stream()
                            .map(v -> (List<String>) v)
                            .flatMap(Collection::stream)
                            .filter(r -> r.toLowerCase().startsWith("group"))
                            .collect(Collectors.toSet());
                    ctx.setProperty("groups", groups.stream().toList());
                    ctx.getHeaders().put(authConf.headerGroup, groups.stream().toList());
                    LOGGER.debug("Add Header [" + authConf.headerGroup + ": " + groups + "]");
                }
                log = StringUtil.concat(log, String.format(" (groups=%s)", groups));

                addTechnicalRolesToPermissions(permissions, roles);
                log = StringUtil.concat(log, String.format(" (permissions=%s)", permissions));
                if (!permissions.isEmpty()) {
                    ArlasClaims arlasClaims = new ArlasClaims(permissions.stream().toList());
                    ctx.setProperty("claims", arlasClaims.getRules());
                    if ((ok != null && ok) || arlasClaims.isAllowed(method, path)) {
                        arlasClaims.injectHeaders(ctx.getHeaders(), transaction);
                        putDecision(getDecisionCacheKey(ctx, method, fullPath, accessToken), Boolean.TRUE);
                        logUAM(LOGGER::debug, ALLOWED, "granted: " + log);
                        return;
                    }
                }
                if (isPublic) {
                    putDecision(getDecisionCacheKey(ctx, method, fullPath, accessToken), Boolean.TRUE);
                    ctx.getHeaders().add(COLUMN_FILTER, DUMMY_COLUMN_FILTER);
                    logUAM(LOGGER::debug, ALLOWED, "public (with token): " + log);
                    return;
                }
            } catch (Exception e) {
                LOGGER.warn("JWT verification failed.", e);
                if (!isPublic) {
                    logUAM(LOGGER::warn, DENIED,"unauthorized (invalid token): " + log);
                    ctx.abortWith(Response.status(UNAUTHORIZED).build());
                } else {
                    ctx.getHeaders().add(COLUMN_FILTER, DUMMY_COLUMN_FILTER);
                    logUAM(LOGGER::debug, ALLOWED,"public (invalid token): " + log);
                }
                return;
            }
            putDecision(getDecisionCacheKey(ctx, method, fullPath, accessToken), Boolean.FALSE);
            logUAM(LOGGER::warn, DENIED,"forbidden (with token): " + log);
            ctx.abortWith(Response.status(FORBIDDEN).build());
        } finally {
            MDC.clear();
        }
    }

    protected void putPermission(String token, String rpt) {
        cacheManager.putObject("permissions", token, rpt, this.cacheTimeout);
    }

    protected String getPermission(String token) {
        return (String) cacheManager.getObject("permissions", token);
    }

    private Boolean getDecision(String path) {
        return (Boolean) cacheManager.getObject("decisions", path);
    }

    private void putDecision(String path, Boolean decision) {
        cacheManager.putObject("decisions", path, decision, this.cacheTimeout);
    }

    private String getDecisionCacheKey(ContainerRequestContext ctx, String method, String uri, String accessToken) {
        return Integer.toString(Objects.hash(
                method,
                uri,
                ctx.getHeaderString(ARLAS_ORG_FILTER),
                accessToken));
    }

    protected String decodeToken(String token) {
        String[] chunks = token.split("\\.");
        return new String(decoder.decode(chunks[0])) + new String(decoder.decode(chunks[1]));
    }
}