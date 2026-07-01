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

package io.arlas.server.stac.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Builds a /queryables JSON Schema document from an ARLAS _describe collection response.
 *
 * <p>The generated schema only exposes indexed leaf fields. Nested objects are traversed
 * recursively and flattened using dot notation.</p>
 *
 * <p>For STAC collections, field names can be normalized to STAC-style names and
 * aliases without the {@code properties.} prefix can also be exposed.</p>
 */
public class QueryablesBuilder {

    /** JSON Schema version used by the generated queryables document. */
    private static final String JSON_SCHEMA_2020_12 = "https://json-schema.org/draft/2020-12/schema";

    /** Prefix used by STAC item properties. */
    private static final String STAC_PROPERTIES_PREFIX = "properties.";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Builds the queryables schema from a describe response.
     *
     * @param baseUrl Base API URL.
     * @param describeResponse ARLAS collection describe response.
     * @return JSON Schema document for /queryables.
     */
    public ObjectNode build(String baseUrl, JsonNode describeResponse) {
        String collectionName = text(describeResponse.path("collection_name"));
        Map<String, String> displayNames = extractDisplayNames(describeResponse);
        boolean isStacModel = describeResponse.path("params").path("is_stac_model").asBoolean(false);
        ObjectNode root = mapper.createObjectNode();
        root.put("$schema", JSON_SCHEMA_2020_12);
        root.put("$id", baseUrl + "stac/collections/" + collectionName + "/queryables");
        root.put("type", "object");
        root.put("title", "Queryables for " + collectionName);
        root.put("description", "Queryable names for collection " + collectionName + ".");
        root.set("properties", mapper.createObjectNode());
        root.put("additionalProperties", false);
        JsonNode sourceProperties = describeResponse.path("properties");
        ObjectNode targetProperties = (ObjectNode) root.get("properties");
        flattenProperties("", sourceProperties, targetProperties, displayNames, isStacModel);
        return root;
    }

    // Retrieve all the queryable field in a set from queryable response
    public static Set<String> getAllowedQueryables(JsonNode queryablesSchema) {
        if (queryablesSchema == null || queryablesSchema.isMissingNode() || queryablesSchema.isNull()) {
            return Set.of();
        }
        JsonNode properties = queryablesSchema.path("properties");
        if (!properties.isObject()) {
            return Set.of();
        }
        Set<String> allowedQueryables = new LinkedHashSet<>();
        Iterator<String> fieldNames = properties.fieldNames();
        while (fieldNames.hasNext()) {
            allowedQueryables.add(fieldNames.next());
        }
        return allowedQueryables;
    }

    /**
     * Recursively traverses the source properties tree and publishes indexed leaf fields.
     *
     * <p>Object nodes are not exposed directly. Only indexed terminal fields are turned
     * into queryables entries.</p>
     */
    private void flattenProperties(String prefix,
                                   JsonNode propertiesNode,
                                   ObjectNode targetProperties,
                                   Map<String, String> displayNames,
                                   boolean isStacModel) {
        if (propertiesNode == null || !propertiesNode.isObject()) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode node = entry.getValue();
            String path = prefix.isEmpty() ? name : prefix + "." + name;
            String type = text(node.path("type"));
            boolean indexed = node.path("indexed").asBoolean(false);
            JsonNode childProperties = node.path("properties");
            // Recurse into nested objects to flatten leaf properties.
            if ("OBJECT".equals(type) && childProperties.isObject()) {
                flattenProperties(path, childProperties, targetProperties, displayNames, isStacModel);
                continue;
            }
            // Only indexed leaf fields are exposed as queryables.
            if (!indexed) {
                continue;
            }
            ObjectNode schema = toJsonSchema(path, node, displayNames, isStacModel);
            if (schema != null) {
                addQueryable(targetProperties, path, schema, displayNames, isStacModel);
            }
        }
    }

    /**
     * Adds the main queryable entry and, when possible, an alias without the
     * {@code properties.} prefix.
     *
     * <p>Example:
     * {@code properties.eo__cloud_cover -> properties.eo:cloud_cover + eo:cloud_cover}</p>
     */
    private void addQueryable(ObjectNode targetProperties,
                              String path,
                              ObjectNode schema,
                              Map<String, String> displayNames,
                              boolean isStacModel) {
        String normalizedPath = normalizeQueryableName(path, isStacModel);
        targetProperties.set(normalizedPath, schema.deepCopy());
        String aliasRaw = toAlias(path);
        if (aliasRaw == null || aliasRaw.isBlank()) {
            return;
        }
        String aliasNormalized = normalizeQueryableName(aliasRaw, isStacModel);
        if (aliasNormalized.isBlank() || aliasNormalized.equals(normalizedPath) || targetProperties.has(aliasNormalized)) {
            return;
        }
        ObjectNode aliasSchema = schema.deepCopy();
        // Resolve the alias label from the original keys first, then fallback to the alias name.
        String aliasDisplayName = displayNames.get(aliasRaw);
        if (aliasDisplayName == null || aliasDisplayName.isBlank()) {
            aliasDisplayName = displayNames.get(path);
        }
        if (aliasDisplayName == null || aliasDisplayName.isBlank()) {
            aliasDisplayName = lastSegment(aliasNormalized);
        }
        aliasSchema.put("description", aliasDisplayName);
        targetProperties.set(aliasNormalized, aliasSchema);
    }

    /**
     * Builds the alias for STAC item properties by removing the {@code properties.} prefix.
     *
     * @param path Original flattened field path.
     * @return Alias without the prefix, or null if no alias applies.
     */
    private String toAlias(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.startsWith(STAC_PROPERTIES_PREFIX)) {
            return path.substring(STAC_PROPERTIES_PREFIX.length());
        }
        return null;
    }

    /**
     * Converts an ARLAS field definition to a JSON Schema property definition.
     */
    private ObjectNode toJsonSchema(String path, JsonNode node, Map<String, String> displayNames, Boolean isStacModel) {
        String type = text(node.path("type"));
        if (type == null) {
            return null;
        }
        ObjectNode schema = mapper.createObjectNode();
        String normalizedPath = normalizeQueryableName(path, isStacModel);
        // Prefer configured display names, then fallback to the last path segment.
        String title = displayNames.get(path);
        if (title == null || title.isBlank()) {
            title = displayNames.get(normalizedPath);
        }
        if (title == null || title.isBlank()) {
            title = lastSegment(normalizedPath);
        }
        schema.put("description", title);
        switch (type) {
            case "BOOLEAN":
                schema.put("type", "boolean");
                return schema;
            case "KEYWORD":
                schema.put("type", "string");
                return schema;
            case "DATE":
                schema.put("type", "string");
                schema.put("format", "date-time");
                return schema;
            case "LONG":
            case "INTEGER":
                schema.put("type", "integer");
                return schema;
            case "FLOAT":
            case "DOUBLE":
                schema.put("type", "number");
                return schema;
            case "GEO_SHAPE":
                schema.put("format", "geometry-any");
                return schema;
            case "GEO_POINT":
                schema.put("format", "geometry-point");
                return schema;
            default:
                return null;
        }
    }

    /**
     * Normalizes queryable names for STAC collections.
     *
     * <p>In STAC mode, the current implementation replaces the first {@code __}
     * occurrence with {@code :} to expose extension-style names such as
     * {@code eo:cloud_cover}.</p>
     */
    private String normalizeQueryableName(String path, boolean isStacModel) {
        if (!isStacModel || path == null || path.isBlank()) {
            return path;
        }
        return path.replaceFirst("__", ":");
    }

    /**
     * Extracts optional display names configured for fields.
     */
    private Map<String, String> extractDisplayNames(JsonNode config) {
        Map<String, String> result = new LinkedHashMap<>();
        JsonNode fields = config.path("params").path("display_names").path("fields");
        if (!fields.isObject()) {
            return result;
        }
        Iterator<Map.Entry<String, JsonNode>> it = fields.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            result.put(e.getKey(), text(e.getValue()));
        }
        return result;
    }

    /**
     * Returns the last segment of a dotted path.
     */
    private String lastSegment(String path) {
        int i = path.lastIndexOf('.');
        return i >= 0 ? path.substring(i + 1) : path;
    }

    /**
     * Safely extracts a text value from a JSON node.
     */
    private String text(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }
}