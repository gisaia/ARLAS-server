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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;

public class CountServiceIT extends AbstractFilteredTest {
    
    @Override
    public String getUrlPath(String collection) {
        return arlasPrefix + "explore/"+collection+"/_count";
    }    
    
    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        handleMatchingFilter(then,0);        
    }
    
    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    private void handleMatchingFilter(ValidatableResponse then, int nbResults) {
        then.statusCode(200)
                .body("totalnb", equalTo(nbResults));
    }
    
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given();
    }

    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        return given().contentType("application/json;charset=utf-8");
    }
    
    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,1);
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception {
        handleMatchingFilter(then,nbResults);
    }
    
    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,595); 
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int size) throws Exception {
        handleMatchingFilter(then,size);
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int size) throws Exception {
        handleMatchingFilter(then,size);
    }

    @Override
    protected void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception {
        handleMatchingFilter(then,nbResults);
    }
}
