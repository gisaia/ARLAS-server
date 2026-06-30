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

import io.arlas.server.tests.stac.AbstractSTACServiceTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Map;

public class AbstractOgcApiTest extends AbstractSTACServiceTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "stac";
    }

    public final String apiUri = getUrlPath("");

    protected Response getJson(String path) {
        return RestAssured
                .given()
                .accept("application/json")
                .when()
                .get(apiUri+path)
                .then()
                .extract()
                .response();
    }

    public Response get(String path, String accept) {
        return RestAssured
                .given()
                .accept(accept)
                .when()
                .get(apiUri+path)
                .then()
                .extract()
                .response();
    }

    protected Response get(String path, Map<String, ?> queryParams, String accept) {
        return RestAssured
                .given()
                .queryParams(queryParams)
                .accept(accept)
                .when()
                .get(apiUri+path)
                .then()
                .extract()
                .response();
    }
}
