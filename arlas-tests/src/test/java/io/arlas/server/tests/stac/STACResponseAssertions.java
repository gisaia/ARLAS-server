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

package io.arlas.server.tests.stac;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static io.arlas.server.tests.stac.STACFilterModels.FilterClause;
import static io.arlas.server.tests.stac.STACFilterModels.RequestTarget;
import static io.arlas.server.tests.stac.STACFilterModels.TypeOfGet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

public class STACResponseAssertions {
    private static final List<String> ROOT_STAC_FIELD = List.of("collection", "catalog", "id", "geometry", "bbox", "centroid", "type");
    private static final List<String> ROOT_STAC_KEY = List.of("properties.", "assets.");
    public void assertCommonResponse(
            ValidatableResponse response,
            int expectedMatched,
            RequestTarget target
    ) {
        String propertyPath = target.typeOfGet() == TypeOfGet.OGC_FEATURE
                ? "numberMatched"
                : "context.matched";

        response
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(propertyPath, equalTo(expectedMatched));
    }

    public void assertClauses(
            ValidatableResponse response,
            List<FilterClause> clauses,
            Boolean isStacModel
    ) {
        for (FilterClause clause : clauses) {
            assertClause(response, clause, isStacModel);
        }
    }

    private void assertClause(ValidatableResponse response, FilterClause clause, Boolean isStacModel) {
        if (Boolean.TRUE.equals(isStacModel)) {
            List<Object> values = extractSTACValues(response, clause.property());
            assertThat(values, everyItem(matcherFor(clause)));
            return;
        }

        String path = "features.properties." + clause.property();
        response.body(path, everyItem(matcherFor(clause)));
    }

    private List<Object> extractSTACValues(ValidatableResponse response, String property) {
        String normalizedProperty = normalizeSTACProperty(property);
        String quotedPath = quotePath(normalizedProperty);
        List<Object> rawList = response.extract().jsonPath().getList(quotedPath);
        return flattenNonNull(rawList);
    }

    private String normalizeSTACProperty(String property) {
        if (!ROOT_STAC_FIELD.contains(property) &&
                ROOT_STAC_KEY.stream().noneMatch(property::startsWith)) {
            return "properties." + property;
        }
        return property;
    }

    private List<Object> flattenNonNull(List<Object> rawList) {
        List<Object> cleanList = new ArrayList<>();
        if (rawList == null) {
            return cleanList;
        }
        for (Object obj : rawList) {
            if (obj instanceof List<?> list) {
                for (Object subObj : list) {
                    if (subObj != null) {
                        cleanList.add(subObj);
                    }
                }
            } else if (obj != null) {
                cleanList.add(obj);
            }
        }
        return cleanList;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Matcher matcherFor(FilterClause clause) {
        Object value = clause.value();
        return switch (clause.operator()) {
            case EQ -> equalTo(value);
            case NE -> not(equalTo(value));
            case GT -> greaterThanValue(value);
            case GTE -> greaterThanOrEqualToValue(value);
            case LT -> lessThanValue(value);
            case LTE -> lessThanOrEqualToValue(value);
            case LIKE -> containsString(String.valueOf(value));
            case ST_INTERSECTS ->  equalTo(true);
            case ST_WITHIN -> equalTo(true);
            case BBOX ->  equalTo(true);
            case BETWEEN -> {
                if (value instanceof STACFilterModels.BetweenValue v) {
                    yield allOf(greaterThanOrEqualToValue(v.lower()),lessThanOrEqualToValue(v.upper())) ;
                } else {
                    yield equalTo(true);
                }
            }
        };
    }

    private static String quotePath(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        if (path.contains("\"")) {
            return path;
        }
        return "\"" + path.replace(".", "\".\"") + "\"";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Matcher greaterThanValue(Object value) {
        return (Matcher) greaterThan((Comparable) value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Matcher greaterThanOrEqualToValue(Object value) {
        return (Matcher) greaterThanOrEqualTo((Comparable) value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Matcher lessThanValue(Object value) {
        return (Matcher) lessThan((Comparable) value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Matcher lessThanOrEqualToValue(Object value) {
        return (Matcher) lessThanOrEqualTo((Comparable) value);
    }
}