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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;

public abstract class AbstractFieldRangeTest  extends AbstractFilteredTest {
    protected static RangeRequest rangeRequest;

    @Before
    public void setUpAggregationRequest() {
        rangeRequest = new RangeRequest();
        rangeRequest.filter = new Filter();
        rangeRequest.field = "params.startdate";
        request = rangeRequest;
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNumericFieldRangeRequest() throws Exception {
        rangeRequest.field = "params.startdate";
        handleFieldRangeRequest(post(rangeRequest), 595, 763600, 1263600);
        handleFieldRangeRequest(get(rangeRequest.field), 595, 763600, 1263600);

        rangeRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<775000]")));
        handleFieldRangeRequest(post(rangeRequest), 3, 763600, 772800);
        handleFieldRangeRequest(get(rangeRequest.field,"f", rangeRequest.filter.f.get(0).get(0).toString()), 3, 763600, 772800);

        rangeRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lt, "0")));
        handleFieldRangeEmptyResponse(post(rangeRequest));
        handleFieldRangeEmptyResponse(get(rangeRequest.field,"f", rangeRequest.filter.f.get(0).get(0).toString()));

        rangeRequest.field = "params.weight";
        rangeRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[763600<1013700]")));
        handleFieldRangeRequest(post(rangeRequest), 1, -6000, -6000);
        handleFieldRangeRequest(get(rangeRequest.field,"f", rangeRequest.filter.f.get(0).get(0).toString()), 1, -6000, -6000);

        rangeRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[763600<1013599]")));
        handleFieldRangeEmptyResponse(post(rangeRequest));
        handleFieldRangeEmptyResponse(get(rangeRequest.field,"f", rangeRequest.filter.f.get(0).get(0).toString()));
        rangeRequest.filter =  new Filter();

        rangeRequest.field = "params.foo";
        handleFieldRangeEmptyResponse(post(rangeRequest));
        handleFieldRangeEmptyResponse(get(rangeRequest.field));
    }

    //----------------------------------------------------------------
    //----------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testInvalidFieldRangeRequest() throws Exception {
        rangeRequest.field = "params.job";
        handleInvalidFieldRangeRequest(post(rangeRequest));
        handleInvalidFieldRangeRequest(get(rangeRequest.field));

        rangeRequest.field = "geo_params.centroid";
        handleInvalidFieldRangeRequest(post(rangeRequest));
        handleInvalidFieldRangeRequest(get(rangeRequest.field));
    }

    protected abstract void handleFieldRangeRequest(ValidatableResponse then, int count, float minValue, float maxValue) throws Exception;
    protected abstract void handleFieldRangeEmptyResponse(ValidatableResponse then) throws Exception;

    protected abstract void handleInvalidFieldRangeRequest(ValidatableResponse then) throws Exception;

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
