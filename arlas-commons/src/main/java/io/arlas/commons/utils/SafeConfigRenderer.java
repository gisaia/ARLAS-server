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

package io.arlas.commons.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Renders the configuration for logging using an ALLOWLIST.
 * The output keeps the JSON tree structure of the configuration.
 *
 * Kinds:
 *  - ALLOW    : value copied as is (JSON type preserved)
 *  - URL      : value logged after stripping userinfo / query string / fragment
 *  - URL_LIST : same, for a "host:port,host:port" list
 *  - PRESENCE : null if the value is null, "set" otherwise (the value itself is never logged)
 *  - anything else : replaced by "<ignored>" (a subtree not covered by any rule
 *    is replaced as a whole)
 */
public final class SafeConfigRenderer {

    public static final String IGNORED = "<ignored>";
    public static final String MASK = "********";

    private enum Kind { ALLOW, URL, URL_LIST, PRESENCE }

    // Rules are expressed as dotted paths, for internal use only (never in the output).
    private static final Map<String, Kind> RULES = new HashMap<>();
    private static final List<String> ALLOW_PREFIXES = List.of(
            "arlas_cors.","arlas_auth","elastic",
            "logging.", "collection-auto-discover", "metrics","admin","health",
            "swagger", "server", "keycloak","init","arlas-inspire","arlas-ogc","arlas-csw","arlas_stac"
    );
    /** All intermediate nodes that have at least one rule below them. */
    private static final Set<String> BRANCHES = new HashSet<>();

    private static void put(Kind k, String... paths) {
        for (String p : paths) {
            RULES.put(p, k);
            addBranches(p);
        }
    }

    private static void addBranches(String path) {
        for (int i = path.indexOf('.'); i >= 0; i = path.indexOf('.', i + 1)) {
            BRANCHES.add(path.substring(0, i));
        }
    }

    static {
        // ---- ArlasServerConfiguration / ArlasBaseConfiguration / ArlasConfiguration
        put(Kind.ALLOW,
                "arlas-index", "arlas-cache-size", "arlas-cache-timeout", "arlas-rest-cache-timeout",
                "arlas_cache_factory_class", "arlas_database_factory_class", "arlas_auth_policy_class",
                "arlas-check-organisations",
                "arlas-service-collections-enabled", "arlas-service-explore-enabled",
                "arlas-service-wfs-enabled", "arlas-service-opensearch-enabled",
                "arlas-service-csw-enabled", "arlas-service-raster-tiles-enabled",
                "arlas_service_stac_enabled",
                "arlas-wfs.featureNamespace");
        put(Kind.URL, "arlas-base-uri");
        // ---- ElasticConfiguration
        put(Kind.URL_LIST, "elastic.elastic-nodes","elastic.elasticNodes");
        put(Kind.PRESENCE, "elastic.elastic-credentials");
        // ---- ARLAS Auth
        put(Kind.URL,
                "arlas_auth.permission_url", "arlas_auth.certificate_url",
                "arlas_auth.keycloak.auth-server-url", "arlas_auth.keycloak.proxy-url");
        put(Kind.PRESENCE,
                "arlas_auth.keycloak.credentials.secret",
                "arlas_auth.keycloak.truststore-password",
                "arlas_auth.keycloak.client-keystore-password",
                "arlas_auth.keycloak.client-key-password",
                "arlas_auth.init.password",
                "swagger.oauth2.clientSecret");
        for (String prefix : ALLOW_PREFIXES) {
            String p = prefix.substring(0, prefix.length() - 1);
            BRANCHES.add(p);
            addBranches(p);
        }
    }

    private SafeConfigRenderer() {}

    /** @param config tree obtained with mapper.valueToTree(configuration) */
    public static String render(ObjectMapper mapper, JsonNode config) {
        ObjectNode out = mapper.createObjectNode();
        if (config.isObject()) {
            config.fields().forEachRemaining(e -> walk(e.getKey(), e.getKey(), e.getValue(), out));
        }
        try {
            return mapper.writeValueAsString(out);
        } catch (Exception e) {
            return "{\"error\":\"config rendering failed\"}";   // fail-closed, no details
        }
    }

    private static boolean hasRulesUnder(String path) {
        return BRANCHES.contains(path)
                || ALLOW_PREFIXES.stream().anyMatch((path + ".")::startsWith);
    }

    private static Kind ruleFor(String path) {
        Kind kind = RULES.get(path);
        if (kind == null && ALLOW_PREFIXES.stream().anyMatch(path::startsWith)) {
            kind = Kind.ALLOW;
        }
        return kind;
    }

    /**
     * @param path full dotted path (only used to look up the rule)
     * @param key  local JSON key, used in the output
     */
    private static void walk(String path, String key, JsonNode n, ObjectNode parent) {
        if (n.isObject()) {
            walkObject(path, key, n, parent);
        } else {
            walkLeaf(path, key, n, parent);
        }
    }

    private static void walkObject(String path, String key, JsonNode n, ObjectNode parent) {
        if (!hasRulesUnder(path)) {
            maskUncoveredObject(key, n, parent);
            return;
        }
        ObjectNode child = parent.putObject(key);
        n.fields().forEachRemaining(e ->
                walk(path + "." + e.getKey(), e.getKey(), e.getValue(), child));
    }

    /** An empty object hides nothing; any other uncovered object is masked as a whole. */
    private static void maskUncoveredObject(String key, JsonNode n, ObjectNode parent) {
        if (n.size() == 0) {
            parent.putObject(key);
        } else {
            parent.put(key, IGNORED);
        }
    }

    private static void walkLeaf(String path, String key, JsonNode n, ObjectNode parent) {
        Kind kind = ruleFor(path);
        if (kind == null) {
            parent.put(key, IGNORED);
        } else if (n.isNull()) {
            parent.putNull(key);   // null is kept as null whatever the rule
        } else {
            writeLeaf(kind, key, n, parent);
        }
    }

    private static void writeLeaf(Kind kind, String key, JsonNode n, ObjectNode parent) {
        switch (kind) {
            case PRESENCE:
                parent.put(key, MASK);
                break;
            case URL:
                parent.put(key, sanitizeUrl(n.asText()));
                break;
            case URL_LIST:
                parent.put(key, sanitizeUrlList(n.asText()));
                break;
            default:
                parent.set(key, n.deepCopy());
        }
    }

    private static String sanitizeUrlList(String raw) {
        return Arrays.stream(raw.split(","))
                .map(SafeConfigRenderer::sanitizeUrl)
                .collect(Collectors.joining(","));
    }

    /** Strips userinfo ("user:pass@"), query string and fragment. Never throws. */
    static String sanitizeUrl(String raw) {
        String s = raw.trim();
        int cut = s.indexOf('?');
        if (cut >= 0) s = s.substring(0, cut);
        cut = s.indexOf('#');
        if (cut >= 0) s = s.substring(0, cut);

        String scheme = "";
        int i = s.indexOf("://");
        if (i >= 0) {
            scheme = s.substring(0, i + 3);
            s = s.substring(i + 3);
        }
        int slash = s.indexOf('/');
        String authority = slash >= 0 ? s.substring(0, slash) : s;
        String rest = slash >= 0 ? s.substring(slash) : "";
        int at = authority.lastIndexOf('@');
        if (at >= 0) authority = authority.substring(at + 1);
        return scheme + authority + rest;
    }
}
