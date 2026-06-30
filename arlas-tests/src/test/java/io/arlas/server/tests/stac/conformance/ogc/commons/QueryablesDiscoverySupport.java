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

package io.arlas.server.tests.stac.conformance.ogc.commons;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public final class QueryablesDiscoverySupport {
    private static final String REL = "http://www.opengis.net/def/rel/ogc/1.0/queryables";
    private static final Pattern LINK_PATTERN = Pattern.compile("<([^>]+)>;\\s*rel=\"([^\"]+)\"");

    private QueryablesDiscoverySupport() {
    }

    public static List<CollectionFilteringMetadata> loadQueryablesMetadata(AbstractOgcApiTest test, String baseUri) {
        List<String> collectionIds = loadCollectionIds(test);
        List<CollectionFilteringMetadata> result = new ArrayList<>();

        for (String collectionId : collectionIds) {
            CollectionFilteringMetadata metadata = new CollectionFilteringMetadata();
            metadata.setCollectionId(collectionId);
            metadata.setItemsPath("/collections/" + collectionId + "/items");

            Response itemsResponse = test.get(metadata.getItemsPath(), Map.of(), "application/json");
            assertEquals(200, itemsResponse.statusCode());

            String queryablesUri = extractQueryablesLink(itemsResponse);
            assertNotNull(queryablesUri, "Missing queryables link for " + metadata.getItemsPath());
            metadata.setQueryablesUri(queryablesUri);
            String stacBasePath = RestAssured.baseURI + ":" + RestAssured.port + baseUri;
            String relativeQueryableUri = queryablesUri.replace(stacBasePath, "");
            Response queryablesResponse = test.get(relativeQueryableUri, Map.of(), "application/schema+json");
            assertEquals(200, queryablesResponse.statusCode());
            assertEquals("https://json-schema.org/draft/2020-12/schema", queryablesResponse.jsonPath().getString("$schema"));
            assertEquals(queryablesUri, queryablesResponse.jsonPath().getString("$id"));
            assertEquals("object", queryablesResponse.jsonPath().getString("type"));

            Map<String, Object> properties = queryablesResponse.jsonPath().getMap("properties");
            assertNotNull(properties, "Queryables properties must exist");
            assertFalse(properties.isEmpty(), "Queryables properties must not be empty");

            String sampleQueryable = null;
            String spatialQueryable = null;

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (!(entry.getValue() instanceof Map<?, ?> valueMap)) {
                    continue;
                }

                if (sampleQueryable == null && entry.getKey().equals("params.age")) {
                    sampleQueryable = entry.getKey();
                }
                if (spatialQueryable == null  && entry.getKey().equals("geo_params.geometry")) {
                    spatialQueryable = entry.getKey();
                }
            }

            assertNotNull(sampleQueryable, "No sample queryable found for " + collectionId);

            metadata.setSampleQueryable(sampleQueryable);
            metadata.setSpatialQueryable(spatialQueryable);

            Boolean additionalProperties = queryablesResponse.jsonPath().getBoolean("additionalProperties");
            metadata.setAdditionalProperties(additionalProperties == null || additionalProperties);

            result.add(metadata);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<String> loadCollectionIds(AbstractOgcApiTest test) {
        Response response = test.get("/collections", Map.of(), "application/json");
        assertEquals(200, response.statusCode());
        List<Map<String, Object>> collections = response.jsonPath().getList("collections");
        List<String> ids = new ArrayList<>();
        if (collections == null) {
            return ids;
        }
        for (Map<String, Object> collection : collections) {
            Object id = collection.get("id");
            if (id != null) {
                ids.add(String.valueOf(id));
            }
        }
        return ids;
    }

    private static  String extractQueryablesLink(Response response) {
        Headers headers = response.getHeaders();
        for (Header h : headers) {
            if ("Link".equalsIgnoreCase(h.getName())) {
                Matcher matcher = LINK_PATTERN.matcher(h.getValue());
                while (matcher.find()) {
                    String href = matcher.group(1);
                    String rel = matcher.group(2);
                    if (REL.equals(rel)) {
                        return href;
                    }
                }
            }
        }
        return null;
    }
}
