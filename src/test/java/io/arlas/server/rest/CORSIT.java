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

package io.arlas.server.rest;

import io.arlas.server.AbstractTest;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class CORSIT extends AbstractTest {

    @Test
    public void testCORS() throws Exception {

        // CHECK CORS
        given()
                .header("Origin","http://example.com")
                .header("Access-Control-Request-Method","GET")
                .header("Access-Control-Request-Headers","X-Requested-With")
        .when().get(arlasPrefix+"collections/")
                .then()
                .header(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://example.com")
                .header(CrossOriginFilter.ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location")
                .header(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    }

    @Override
    protected String getUrlPath(String collection) {
        return null;
    }
}
