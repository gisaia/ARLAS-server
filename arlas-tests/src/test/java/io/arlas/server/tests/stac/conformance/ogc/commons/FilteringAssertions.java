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

import io.restassured.response.Response;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public final class FilteringAssertions {

    private FilteringAssertions() {}

    public static void assertEveryReturnedResourceMatchesQueryable(Response response, String queryable, Object expectedValue) {
        List<Map<String, Object>> features = response.jsonPath().getList("features");
        if (features == null) {
            return;
        }

        for (Map<String, Object> feature : features) {
            Object value = resolveQueryableValue(feature, queryable);
            assertEquals(
                    stringify(expectedValue),
                    stringify(value),
                    "Feature does not match queryable filter '" + queryable + "'"
            );
        }
    }

    private static Object resolveQueryableValue(Map<String, Object> feature, String queryable) {
        if (queryable == null || queryable.isBlank()) {
            return null;
        }

        Object directValue = feature.get(queryable);
        if (directValue != null) {
            return directValue;
        }

        Object properties = feature.get("properties");
        if (!(properties instanceof Map<?, ?> propertiesMap)) {
            return null;
        }

        return getByDotPath(propertiesMap, queryable);
    }

    private static Object getByDotPath(Map<?, ?> root, String path) {
        Object current = root;

        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = currentMap.get(segment);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private static String stringify(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public static void assertSameFeatureIds(List<Map<String, Object>> expected, List<Map<String, Object>> actual) {
        Set<String> expectedIds = extractIds(expected);
        Set<String> actualIds = extractIds(actual);
        assertEquals(expectedIds, actualIds);
    }

    public static void assertNoFeatures(Response response) {
        List<Map<String, Object>> features = response.jsonPath().getList("features");
        assertTrue(features == null || features.isEmpty());
    }

    private static Set<String> extractIds(List<Map<String, Object>> features) {
        if (features == null) return Set.of();
        return features.stream()
                .map(f -> Objects.toString(f.get("id"), null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}