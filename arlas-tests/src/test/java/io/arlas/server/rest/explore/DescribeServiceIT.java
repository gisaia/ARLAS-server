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

import io.arlas.server.DataSetTool;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;

import java.util.Optional;

public class DescribeServiceIT extends AbstractDescribeTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/_list";
    }

    @Override
    protected String getDescribeResultPath() {
        return "results/" + (DataSetTool.ALIASED_COLLECTION ? "_list_base_result_aliased.json" : "_list_base_result.json");
    }

    @Override
    protected void handleResponse(ValidatableResponse response, JsonPath jsonPath) {
        compare(response, jsonPath, Optional.of(0));
        compare(response, jsonPath, Optional.of(1));
    }

}
