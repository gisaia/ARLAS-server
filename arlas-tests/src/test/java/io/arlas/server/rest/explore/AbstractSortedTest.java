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

import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public abstract class AbstractSortedTest extends AbstractProjectedTest {
    @Before
    public void setUpSearch() {
        search.size = new Size();
        search.filter = new Filter();
        search.sort = new Sort();
        search.projection = new Projection();
        search.filter = new Filter();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testSortParameter() throws Exception {
        search.sort.sort = "-params.job";
        handleSortParameter(post(search), "Dancer");
        handleSortParameter(get("sort", search.sort.sort), "Dancer");

        search.sort.sort = "geodistance:-50 -110";
        handleGeoSortParameter(post(search), "-50,-110");
        handleGeoSortParameter(get("sort", search.sort.sort), "-50,-110");

    }


    @Test
    public void testInvalidSizeParameters() throws Exception {
        search.sort.sort = "-50 -110";
        handleInvalidGeoSortParameter(post(search));
        handleInvalidGeoSortParameter(get("sort", search.sort.sort));
    }

    protected abstract void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception;

    protected abstract void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception;

    protected abstract void handleInvalidGeoSortParameter(ValidatableResponse then) throws Exception;

    private ValidatableResponse post(Request request) {
        RequestSpecification req = givenFilterableRequestBody();
        return req.body(handlePostRequest(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }
}
