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

import io.arlas.server.model.enumerations.ComputationEnum;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ComputeServiceIT extends AbstractComputationTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_compute";
    }

    @Override
    protected void handleComputationRequest(ValidatableResponse then, int count, float value) throws Exception {
        then.statusCode(200)
                .body("totalnb", equalTo(count))
                .body("value", equalTo(value));
    }

    @Override
    protected void handleGeoboxComputationRequest(ValidatableResponse then, int count, float west, float south, float east, float north) throws Exception {
        then.statusCode(200)
                .body("totalnb", equalTo(count))
                .body("geometry.coordinates", hasItem(everyItem(hasItem(greaterThanOrEqualTo(west)))))
                .body("geometry.coordinates", hasItem(everyItem(hasItem(greaterThanOrEqualTo(south)))))
                .body("geometry.coordinates", hasItem(everyItem(hasItem(lessThanOrEqualTo(east)))))
                .body("geometry.coordinates", hasItem(everyItem(hasItem(lessThanOrEqualTo(north)))));
    }

    @Override
    protected void handleGeocentroidComputationRequest(ValidatableResponse then, int count, float west, float south, float east, float north) throws Exception {
        then.statusCode(200)
                .body("totalnb", equalTo(count))
                .body("geometry.coordinates", hasItem((greaterThanOrEqualTo(west))))
                .body("geometry.coordinates", hasItem(greaterThanOrEqualTo(south)))
                .body("geometry.coordinates", hasItem(lessThanOrEqualTo(east)))
                .body("geometry.coordinates", hasItem(lessThanOrEqualTo(north)));
    }


    @Override
    protected void handleInvalidComputationRequest(ValidatableResponse then) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleComputationEmptyResponse(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("totalnb", equalTo(0))
                .body("value", isEmptyOrNullString());
    }

    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given().param("field", "params.startdate").param("metric", "avg");
    }

    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        computationRequest.field = "params.startdate";
        computationRequest.metric = ComputationEnum.AVG;
        request = computationRequest;
        return given().contentType("application/json;charset=utf-8");
    }

    private void handleMatchingFilter(ValidatableResponse then, int nbResults) {
        then.statusCode(200)
                .body("totalnb", equalTo(nbResults));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then, 1);
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
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        handleMatchingFilter(then, 0);
    }

}
