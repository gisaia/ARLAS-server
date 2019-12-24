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
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;

public abstract class AbstractComputationTest extends AbstractFilteredTest {
    protected static ComputationRequest computationRequest;

    @Before
    public void setUpComputationRequest() {
        computationRequest = new ComputationRequest();
        computationRequest.filter = new Filter();
        request = computationRequest;
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testComputeRequest() throws Exception {
        /** AVG **/
        computationRequest.field = "params.age";
        computationRequest.metric = ComputationEnum.AVG;
        handleComputationRequest(post(computationRequest, ""), 595, 3702.8571428571427f);//empty column filter is not considered
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value(), ""), 595, 3702.8571428571427f);//empty column filter is not considered

        /** CARDINALITY **/
        computationRequest.field = "params.job";
        computationRequest.metric = ComputationEnum.CARDINALITY;
        handleComputationRequest(post(computationRequest), 595, 10f);
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value()), 595, 10f);

        /** MAX **/
        computationRequest.field = "params.startdate";
        computationRequest.metric = ComputationEnum.MAX;
        computationRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<775000]")));
        handleComputationRequest(post(computationRequest), 3, 772800);
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value(),"f", computationRequest.filter.f.get(0).get(0).toString()), 3, 772800);

        /** MIN **/
        computationRequest.field = "params.weight";
        computationRequest.metric = ComputationEnum.MIN;
        computationRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[763600<1013700]")));
        handleComputationRequest(post(computationRequest), 1, -6000);
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value(), "f", computationRequest.filter.f.get(0).get(0).toString()), 1,-6000);

        /** SPANNING **/
        computationRequest.metric = ComputationEnum.SPANNING;
        handleComputationRequest(post(computationRequest), 1, 0);
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value(), "f", computationRequest.filter.f.get(0).get(0).toString()), 1, 0);

        /** SUM **/
        computationRequest.filter =  new Filter();
        computationRequest.metric = ComputationEnum.SUM;
        handleComputationRequest(post(computationRequest), 271, 374900);
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value()), 271, 374900);

        /** GEOCENTROID **/
        computationRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-20,-20,30,30")));
        computationRequest.field = "geo_params.centroid";
        computationRequest.metric = ComputationEnum.GEOCENTROID;
        handleGeocentroidComputationRequest(post(computationRequest), 16, 4.9f, 4.9f, 5, 5);
        handleGeocentroidComputationRequest(get(computationRequest.field, computationRequest.metric.value(), "f", computationRequest.filter.f.get(0).get(0).toString()), 16, 4.9f, 4.9f, 5, 5);

        /** GEOBBOX **/
        computationRequest.field = "geo_params.centroid";
        computationRequest.metric = ComputationEnum.GEOBBOX;
        handleGeoboxComputationRequest(post(computationRequest), 16, -11, -11, 20, 20);
        handleGeoboxComputationRequest(get(computationRequest.field, computationRequest.metric.value(), "f", computationRequest.filter.f.get(0).get(0).toString()), 16, -11, -11, 20, 20);


        /** EMPTY RESPONSE : RESTRICTIVE FILTER**/
        computationRequest.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lt, "0")));
        handleComputationEmptyResponse(post(computationRequest));
        handleComputationEmptyResponse(get(computationRequest.field, computationRequest.metric.value(), "f", computationRequest.filter.f.get(0).get(0).toString()));
        computationRequest.filter =  new Filter();

        /** EMPTY RESPONSE : NON-EXISTING FIELD**/
        computationRequest.field = "params.foo";
        handleComputationEmptyResponse(post(computationRequest));
        handleComputationEmptyResponse(get(computationRequest.field, computationRequest.metric.value()));
    }

    //----------------------------------------------------------------
    //----------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testInvalidComputationRequest() throws Exception {
        computationRequest.field = "params.job";
        computationRequest.metric = ComputationEnum.AVG;
        handleInvalidComputationRequest(post(computationRequest));
        handleInvalidComputationRequest(get(computationRequest.field, computationRequest.metric.value()));

        computationRequest.field = "geo_params.centroid";
        computationRequest.metric = ComputationEnum.MAX;
        handleInvalidComputationRequest(post(computationRequest));
        handleInvalidComputationRequest(get(computationRequest.field, computationRequest.metric.value()));

        computationRequest.field = "geo_params.geometry";
        computationRequest.metric = ComputationEnum.GEOBBOX;
        handleInvalidComputationRequest(post(computationRequest));
        handleInvalidComputationRequest(get(computationRequest.field, computationRequest.metric.value()));

        computationRequest.metric = ComputationEnum.GEOCENTROID;
        handleInvalidComputationRequest(post(computationRequest));
        handleInvalidComputationRequest(get(computationRequest.field, computationRequest.metric.value()));
    }

    @Test
    public void testComputeRequestWithAvailableColumns() throws Exception {
        computationRequest.field = "params.age";
        computationRequest.metric = ComputationEnum.AVG;
        handleComputationRequest(post(computationRequest, "params"), 595, 3702.8571428571427f);
        handleComputationRequest(get(computationRequest.field, computationRequest.metric.value(), "params"), 595, 3702.8571428571427f);
    }

    @Test
    public void testComputeRequestWithUnavailableColumns() throws Exception {
        computationRequest.field = "params.age";
        computationRequest.metric = ComputationEnum.AVG;
        handleUnavailableColumn(post(computationRequest, "fullname"));
        handleUnavailableColumn(get(computationRequest.field, computationRequest.metric.value(), "fullname"));
    }

    protected abstract void handleComputationRequest(ValidatableResponse then, int count, float value) throws Exception;
    protected abstract void handleGeoboxComputationRequest(ValidatableResponse then, int count, float west, float south, float east, float north) throws Exception;
    protected abstract void handleGeocentroidComputationRequest(ValidatableResponse then, int count, float west, float south, float east, float north) throws Exception;
    protected abstract void handleComputationEmptyResponse(ValidatableResponse then) throws Exception;
    protected abstract void handleInvalidComputationRequest(ValidatableResponse then) throws Exception;

    private ValidatableResponse post(Request request) {
        return given().contentType("application/json;charset=utf-8").body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse post(Request request, String columnFilter) {
        return given()
                .contentType("application/json;charset=utf-8")
                .header("column-filter", columnFilter)
                .body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String field, String metric) {
        return given().param("field", field).param("metric", metric)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String field, String metric, String columnFilter) {
        return given()
                .header("column-filter", columnFilter)
                .param("field", field)
                .param("metric", metric)
                .when()
                .get(getUrlPath("geodata"))
                .then();
    }
    private ValidatableResponse get(String field, String metric, String param, Object paramValue) {
        return given().param("field", field).param("metric", metric).param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
