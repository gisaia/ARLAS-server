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

package io.arlas.server.tests.auth;

import io.arlas.server.tests.AbstractTestContext;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class AuthServiceIT extends AbstractTestContext {

//      "http://arlas.io/permissions": [
//              "rule:collections:GET:100",
//              "rule:explore/_list:GET:200",
//              "variable:organisation:foo",
//              "header:partition-filter:${organisation}",
//              "rule:explore/${organisation}/_search:GET:300"
//              ],
    private String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1FWXhNVEF3TTBORk9URXlSVVpDTlRRek0wVXhOVU0xUlVWQlF6TTNNVFpFT0VZNFJEWTBPUSJ9.eyJodHRwOi8vYXJsYXMuaW8vcGVybWlzc2lvbnMiOlsicnVsZTpjb2xsZWN0aW9uczpHRVQ6MTAwIiwicnVsZTpleHBsb3JlL19saXN0OkdFVDoyMDAiLCJ2YXJpYWJsZTpvcmdhbmlzYXRpb246YXh4ZXMiLCJoZWFkZXI6UGFydGl0aW9uLUZpbHRlcjoke29yZ2FuaXNhdGlvbn0iLCJydWxlOmV4cGxvcmUvJHtvcmdhbmlzYXRpb259L19zZWFyY2g6R0VUOjMwMCJdLCJodHRwOi8vYXJsYXMuaW8vZ3JvdXBzIjpbIlRlc3QgR3JvdXAiXSwiaHR0cDovL2FybGFzLmlvL3JvbGVzIjpbInJvbGU6YXh4ZXNFeHBsb3JlciJdLCJodHRwOi8vYXJsYXMuaW8vZmlsdGVyIjoiY291bnRyeTplcTpGUiIsIm5pY2tuYW1lIjoiYWxhaW4uYm9kaWd1ZWwiLCJuYW1lIjoiYWxhaW4uYm9kaWd1ZWxAZ21haWwuY29tIiwicGljdHVyZSI6Imh0dHBzOi8vcy5ncmF2YXRhci5jb20vYXZhdGFyLzk2YTkzMDZjOTg4NjkyYzNjNDJkZmUxNzkyNTU1MGU4P3M9NDgwJnI9cGcmZD1odHRwcyUzQSUyRiUyRmNkbi5hdXRoMC5jb20lMkZhdmF0YXJzJTJGYWwucG5nIiwidXBkYXRlZF9hdCI6IjIwMTktMDktMDNUMDk6Mjc6NDcuMjY1WiIsImlzcyI6Imh0dHBzOi8vdGVzdGFybGFzLmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1ZDNlZGY5NGJiNDhiNDBlYzcwMTZmNGEiLCJhdWQiOiJBek5nZ2t4SGhTWlRFcEI0VWhIcDVuUGN0UWJWNGNVSCIsImlhdCI6MTU2NzUwMjg2OSwiZXhwIjoxODgyODYyODY5fQ.kVabMa-OzwBy9M9PMCKx3U9WMajKYTVYWlWsck17SoZje90lkEObhGHY_XgPcLl7dhOO2DHc1sc1y_e7VnAFielz5jr50stLm04ublI6LKk_n2AoagpLnc-NPoEjPAVIxvODdxSx6TpVIOhe63zO7e6KcQOrR2MN_1vm5f7kdaSBIynpmR7E-gyr92cbZkLcQD5wGATFau17zmczudDEjcxfhLLTpJh_5-rvYHwESDZmnrfXtGPoK0R_G3DWwwLVUB15mQAMt17m4mkUwQ3qD0E7zmXoGzXhOAHq4XY3X7CVJjlsqVnjQvfvme-1vPJP4Fu7hOb8WF_0klc77OBiUA";

    @Test
    public void testAccessPublicResourceWithoutAuthHeader() throws Exception {
        when().get(arlasPath + "swagger")
                .then().statusCode(200);
    }

    @Test
    public void testAccessPublicResourceWithInvalidAuthHeader() throws Exception {
        given().header(HttpHeaders.AUTHORIZATION, "foo")
                .when().get(arlasPath + "swagger")
                .then().statusCode(200);
    }

    @Test
    public void testAccessPublicResourceWithValidAuthHeader() throws Exception {
        given().header(HttpHeaders.AUTHORIZATION, token)
                .when().get(arlasPath + "swagger")
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
        given().header(HttpHeaders.AUTHORIZATION, token)
                .when().get(getUrlPath(""))
                .then().statusCode(200);
    }

    @Test
    public void testAccessForbiddenResourceWithValidAuthHeader() throws Exception {
        given().header(HttpHeaders.AUTHORIZATION, token)
                .when().get(getUrlPath("/_export"))
                .then().statusCode(403);
    }

    @Override
    protected String getUrlPath(String p) {
        return arlasPath + "collections" + p;
    }
}
