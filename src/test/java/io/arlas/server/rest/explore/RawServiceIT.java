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

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import org.junit.Test;

import io.arlas.server.AbstractTestWithCollection;

public class RawServiceIT extends AbstractTestWithCollection {

    @Test
    public void testGetArlasHit() throws Exception {

        // GET existing document
        when().get(getUrlPath(COLLECTION_NAME)+"/ID__170__20DI")
        .then().statusCode(200)
            .body("md.id", equalTo("ID__170__20DI"))
            .body("data.geo_params.centroid", equalTo("-20,-170"))
            .body("data.id", equalTo("ID__170__20DI"))
            .body("data.fullname", equalTo("My name is ID__170__20DI"))
            .body("data.params.startdate", equalTo(813400))
            .body("data.params.city", isEmptyOrNullString());


        // GET invalid collection
        when().get(getUrlPath("foo")+"/0-0")
        .then().statusCode(404);
        
        // GET invalid identifier
        when().get(getUrlPath(COLLECTION_NAME)+"/foo")
        .then().statusCode(404);
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/"+collection;
    }
}
