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

import io.arlas.server.tests.DataSetTool;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.Optional;

public class DescribeCollectionServiceIT extends AbstractDescribeTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_describe";
    }

    @Override
    public void testDescribeFeatureWithCollectionBasedColumFiltering() throws Exception {
        handleMatchingResponse(get(Optional.of("fullname,params,geo_params")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleMatchingResponse(get(Optional.of(COLLECTION_NAME + ":fullname," + COLLECTION_NAME + ":params," + COLLECTION_NAME + ":geo_params")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleNotMatchingResponse(
                get(Optional.of("notExisting:fullname,notExisting:params,notExisting:geo_params,params.startdate")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));

        handleUnavailableCollection(get(Optional.of("notExisting:fullname,notExisting:params,notExisting:geo_params")));
    }

    @Override
    protected String getDescribeResultPath() {
        return "results/" + (DataSetTool.ALIASED_COLLECTION ? "_describe_base_result_aliased.json" : "_describe_base_result.json");
    }

    @Override
    protected String getFilteredDescribeResultPath() {
        return "results/" + (DataSetTool.ALIASED_COLLECTION ? "_describe_filtered_result_aliased.json" : "_describe_filtered_result.json");
    }

    @Override
    protected void handleMatchingResponse(ValidatableResponse response, JsonPath jsonPath) {
        handleResponse(response, jsonPath, true);
    }

    @Override
    protected void handleNotMatchingResponse(ValidatableResponse response, JsonPath jsonPath) {
        handleResponse(response, jsonPath, false);
    }

    private void handleUnavailableCollection(ValidatableResponse response) {
        response.statusCode(403)
        .body(Matchers.stringContainsInOrder(Arrays.asList("collection", "available")));
    }

    private void handleResponse(ValidatableResponse response, JsonPath jsonPath, boolean areParamsEqual) {
        compare(response, jsonPath, Optional.empty(), areParamsEqual);
    }
}
