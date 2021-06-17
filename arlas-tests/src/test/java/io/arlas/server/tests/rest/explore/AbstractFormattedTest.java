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

package io.arlas.server.tests.rest.explore;

import io.arlas.server.core.model.request.Form;
import io.arlas.server.core.model.request.Request;
import static io.restassured.RestAssured.given;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;

import java.util.List;

public abstract class AbstractFormattedTest extends AbstractFilteredTest {


    @Test
    public void testFlatFormat() throws Exception {

        handleFlatFormatRequest(post(), getFlattenedItems());
        handleFlatFormatRequest(get("flat", true), getFlattenedItems());

    }

    protected abstract void handleFlatFormatRequest(ValidatableResponse then, List<String>   flattenedItems) throws Exception;

    protected abstract RequestSpecification givenFlattenRequestParams();

    protected abstract Request flattenRequestParamsPost(Request request);

    protected abstract List<String> getFlattenedItems();

    private ValidatableResponse post() {
        request.form = new Form();
        request.form.flat = true;
        return given().contentType("application/json").body(flattenRequestParamsPost(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String param, Object paramValue) {
        return givenFlattenRequestParams().param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

}
