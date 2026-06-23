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

import java.util.List;

import static io.arlas.server.tests.stac.STACFilterModels.FilterClause;
import static io.arlas.server.tests.stac.STACFilterModels.RequestTarget;
import static io.arlas.server.tests.stac.STACFilterModels.TypeOfGet;
import static org.hamcrest.Matchers.*;

public class STACResponseAssertions {

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
            List<FilterClause> clauses
    ) {
        for (FilterClause clause : clauses) {
            assertClause(response, clause);
        }
    }

    private void assertClause(
            ValidatableResponse response,
            FilterClause clause
    ) {
        String path = "features.properties." + clause.property();

        switch (clause.operator()) {
            case EQ -> response.body(path, everyItem(equalTo(clause.value())));
            case NE -> response.body(path, everyItem(not(equalTo(clause.value()))));
            case GT -> response.body(path, everyItem(greaterThanValue(clause.value())));
            case GTE -> response.body(path, everyItem(greaterThanOrEqualToValue(clause.value())));
            case LT -> response.body(path, everyItem(lessThanValue(clause.value())));
            case LTE -> response.body(path, everyItem(lessThanOrEqualToValue(clause.value())));
            case LIKE -> response.body(path, everyItem(containsString(String.valueOf(clause.value()))));
        }
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