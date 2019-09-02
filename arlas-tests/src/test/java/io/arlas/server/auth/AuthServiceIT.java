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

package io.arlas.server.auth;

import io.arlas.server.AbstractTestContext;
import io.arlas.server.AbstractTestWithCollection;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class AuthServiceIT extends AbstractTestContext {

    @Test
    public void testAccessPublicResource() throws Exception {
        when().get(arlasPath + "swagger")
                .then().statusCode(200);
    }

    @Test
    public void testAccessProtectedResourceWithoutAuthHeader() throws Exception {
        when().get(getUrlPath(""))
                .then().statusCode(401);
    }

    @Test
    public void testAccessProtectedResourceWithInvalidAuthHeader() throws Exception {
        given().header(HttpHeaders.AUTHORIZATION, "foo")
                .when().get(getUrlPath(""))
                .then().statusCode(401);
    }

    @Test
    public void testAccessProtectedResourceWithValidAuthHeader() throws Exception {
        given().header(HttpHeaders.AUTHORIZATION, "Bearer foo")
                .when().get(getUrlPath(""))
                .then().statusCode(200);
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "collections";
    }
}
