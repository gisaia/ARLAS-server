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

package io.arlas.server.rest.explore;

import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CountDistinctServiceIT extends AbstractFilteredTest {
    protected static CountDistinct countDistinctRequest;

    @Before
    public void setUpAggregationRequest() {
        countDistinctRequest = new CountDistinct();
        countDistinctRequest.filter = new Filter();
        countDistinctRequest.field = "params.job";
        request = countDistinctRequest;
    }

    @Override
    public String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_countDistinct";
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testCountDistinctRequest() throws Exception {
        handleCountDistinctRequest(post(countDistinctRequest), 10, 595);
        handleCountDistinctRequest(get(countDistinctRequest.field), 10, 595);

        countDistinctRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<775000]")));
        handleCountDistinctRequest(post(countDistinctRequest), 2, 3);
        handleCountDistinctRequest(get(countDistinctRequest.field,"f", countDistinctRequest.filter.f.get(0).get(0).toString()), 2, 3);

        countDistinctRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Architect")));
        handleCountDistinctRequest(post(countDistinctRequest), 1, 58);
        handleCountDistinctRequest(get(countDistinctRequest.field,"f", countDistinctRequest.filter.f.get(0).get(0).toString()), 1, 58);

        // numeric field
        countDistinctRequest.filter.f = null;
        countDistinctRequest.field = "params.age";
        handleCountDistinctRequest(post(countDistinctRequest), 73, 595);
        handleCountDistinctRequest(get(countDistinctRequest.field), 73, 595);

        // geo_point field
        countDistinctRequest.field = "geo_params.centroid";
        handleCountDistinctRequest(post(countDistinctRequest), 595, 595);
        handleCountDistinctRequest(get(countDistinctRequest.field), 595, 595);

        // non-existing field
        countDistinctRequest.field = "params.foo";
        handleCountDistinctRequest(post(countDistinctRequest), 0, 595);
        handleCountDistinctRequest(get(countDistinctRequest.field), 0, 595);
    }


    //----------------------------------------------------------------
    //----------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testInvalidCountDistinctRequest() throws Exception {
        countDistinctRequest.field = "";
        handleInvalidCountDistinctRequest(post(countDistinctRequest));
        handleInvalidCountDistinctRequest(get(countDistinctRequest.field));

        countDistinctRequest.field = null;
        handleInvalidCountDistinctRequest(post(countDistinctRequest));
        handleInvalidCountDistinctRequest(get(countDistinctRequest.field));
    }

    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        handleCountDistinctRequest(then, 0,0);
    }


    private void handleCountDistinctRequest(ValidatableResponse then, int nbDistinctValues, int nbResults) {
        then.statusCode(200)
                .body("totalnb", equalTo(nbResults))
                .body("value", equalTo(nbDistinctValues));
    }

    private void handleInvalidCountDistinctRequest(ValidatableResponse then) {
        then.statusCode(400);
    }


    private void handleMatchingFilter(ValidatableResponse then, int nbResults) {
        then.statusCode(200)
                .body("totalnb", equalTo(nbResults));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        handleCountDistinctRequest(then, 1, 1);
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception {
        handleFieldFilter(then, nbResults);
    }
    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults) throws Exception {
        handleMatchingFilter(then, nbResults);
    }


    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then, int nbResults) throws Exception {
        handleMatchingFilter(then, nbResults);
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int size) throws Exception {
        handleMatchingFilter(then, size);
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int size) throws Exception {
        handleMatchingFilter(then, size);
    }

    @Override
    protected void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception {
        handleMatchingFilter(then, nbResults);
    }

    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given().param("field", "params.job");
    }

    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        countDistinctRequest.field = "params.job";
        request = countDistinctRequest;
        return given().contentType("application/json;charset=utf-8");
    }

    private ValidatableResponse post(Request request) {
        return given().contentType("application/json;charset=utf-8").body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(Object paramValue) {
        return given().param("field", paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String field, String param, Object paramValue) {
        return given().param("field", field).param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
