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
import org.junit.Test;

import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.restassured.RestAssured.given;

public class DescribeServiceIT extends AbstractDescribeTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/_list";
    }

    @Override
    public void testDescribeFeatureWithCollectionBasedColumFiltering() throws Exception {
        handleMatchingResponse(get(Optional.of("fullname,text_search,params,geo_params")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleMatchingResponse(get(Optional.of(COLLECTION_NAME + ":fullname," + COLLECTION_NAME + ":params,"  + COLLECTION_NAME + ":text_search," +  COLLECTION_NAME + ":geo_params," + COLLECTION_NAME_ACTOR + ":fullname," + COLLECTION_NAME_ACTOR +
                        ":params," + COLLECTION_NAME_ACTOR + ":text_search," + COLLECTION_NAME_ACTOR + ":geo_params")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));

        ValidatableResponse fullGeodataPartialActorResponse = get(Optional.of(COLLECTION_NAME + ":fullname,"  + COLLECTION_NAME + ":text_search," + COLLECTION_NAME + ":params," + COLLECTION_NAME + ":geo_params," +  COLLECTION_NAME_ACTOR + ":fullname," +
                  COLLECTION_NAME_ACTOR + ":text_search," + COLLECTION_NAME_ACTOR + ":params,notExisting:geo_params"));
        compare(fullGeodataPartialActorResponse, new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())), Optional.of(0), true);
        compare(fullGeodataPartialActorResponse, new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())), Optional.of(1), true);

        handleNotMatchingResponse(get(Optional.of(COLLECTION_NAME + ":nofield," + COLLECTION_NAME_ACTOR + ":nofield,notExisting:*ullname,notExisting:*arams")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));

        //collection should also be filtered if no field is allowed for it
        get(Optional.of("notExisting:geo_params"))
            .statusCode(200)
            .body(".", Matchers.iterableWithSize(0));

        get(Optional.of(COLLECTION_NAME + ":fullname," + COLLECTION_NAME + ":params," + COLLECTION_NAME + ":geo_params"))
            .statusCode(200)
            .body(".", Matchers.iterableWithSize(1))
            .body("[0].collection_name", Matchers.equalTo(COLLECTION_NAME))
            .body("[1].collection_name", Matchers.isEmptyOrNullString());

        //however a field no related to any collection makes all collections allowed
        get(Optional.of("toto"))
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(2))
                .body("[0].collection_name", Matchers.equalTo(COLLECTION_NAME))
                .body("[1].collection_name", Matchers.equalTo(COLLECTION_NAME_ACTOR));

        get(Optional.empty())
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(2))
                .body("[0].collection_name", Matchers.equalTo(COLLECTION_NAME))
                .body("[1].collection_name", Matchers.equalTo(COLLECTION_NAME_ACTOR));
    }

    @Override
    public void testDescribeFeatureWithOrganisationFiltering() throws Exception {
        get(DataSetTool.DATASET_ORG_OWNER).statusCode(200).body(".", Matchers.iterableWithSize(2));
        get(DataSetTool.DATASET_ORG_SHARED).statusCode(200).body(".", Matchers.iterableWithSize(2));
        get("foobar").statusCode(200).body(".", Matchers.iterableWithSize(0));
    }

    protected ValidatableResponse getWithCollectionFilter(Optional<String> collectionFilter) {
        if (collectionFilter.isPresent()) {
            return given()
                    .header(COLUMN_FILTER, collectionFilter.get())
                    .when()
                    .get(getUrlPath("geodata"))
                    .then();
        } else {
            return given()
                    .when()
                    .get(getUrlPath("geodata"))
                    .then();
        }
    }

    @Test
    public void testDescribeFeatureWithCollectionFilter() throws Exception {
        getWithCollectionFilter(Optional.of("notExisting:*"))
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(0));

        getWithCollectionFilter(Optional.of("geodata_actor:*"))
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(1));

        getWithCollectionFilter(Optional.empty())
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(2));

        getWithCollectionFilter(Optional.of("geodata*:*"))
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(2));
        getWithCollectionFilter(Optional.of("geo*:*"))
                .statusCode(200)
                .body(".", Matchers.iterableWithSize(2));
    }

    @Override
    protected String getDescribeResultPath() {
        return "results/" + (DataSetTool.ALIASED_COLLECTION ? "_list_base_result_aliased.json" : "_list_base_result.json");
    }

    @Override
    protected String getFilteredDescribeResultPath() {
        return "results/" + (DataSetTool.ALIASED_COLLECTION ? "_list_filtered_result_aliased.json" : "_list_filtered_result.json");
    }

    @Override
    protected void handleMatchingResponse(ValidatableResponse response, JsonPath jsonPath) {
        handleResponse(response, jsonPath, true);
    }

    @Override
    protected void handleNotMatchingResponse(ValidatableResponse response, JsonPath jsonPath) {
        handleResponse(response, jsonPath, false);
    }

    private void handleResponse(ValidatableResponse response, JsonPath jsonPath, boolean areParamsEqual) {
        compare(response, jsonPath, Optional.of(0), areParamsEqual);
        compare(response, jsonPath, Optional.of(1), areParamsEqual);
    }

}
