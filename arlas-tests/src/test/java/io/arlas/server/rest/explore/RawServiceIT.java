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

import io.arlas.server.AbstractTestWithCollection;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;
import java.util.Optional;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class RawServiceIT extends AbstractTestWithCollection {

    @Test
    public void testGetArlasHit() throws Exception {

        // GET existing document (flat == false)
        handleRawQuery(
                givenRawQuery("/ID__170__20DI", Optional.empty(), Optional.empty()));

        // GET existing document (flat == true)
        handleRawQuery(
                givenRawQuery("/ID__170__20DI", Optional.of(Boolean.TRUE), Optional.empty()),
                FLATTEN_CHAR);
    }

    @Test
    public void testGetArlasHitWithInvalidCollection() throws Exception {
        when().get(getUrlPath("foo") + "/0-0")
                .then().statusCode(404);
    }

    @Test
    public void testGetArlasHitWithInvalidIdentifier() throws Exception {
        when().get(getUrlPath(COLLECTION_NAME) + "/foo")
                .then().statusCode(404);
    }

    @Test
    public void testGetArlasHitWithEmptyColumnFilter() throws Exception {
        handleRawQuery(
                givenRawQuery("/ID__170__20DI", Optional.empty(), Optional.of("")));

        handleRawQuery(
                givenRawQuery("/ID__170__20DI", Optional.of(Boolean.TRUE), Optional.of("")),
                FLATTEN_CHAR);
    }


    @Test
    public void testGetArlasHitWithColumnFilter() throws Exception {
        handleRawQueryWithColumnsFiltered(
                givenRawQuery("/ID__170__20DI", Optional.empty(), Optional.of("id")));

        handleRawQueryWithColumnsFiltered(
                givenRawQuery("/ID__170__20DI", Optional.of(Boolean.TRUE), Optional.of("id")),
                FLATTEN_CHAR);
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection;
    }

    private void handleRawQuery(ValidatableResponse response) {
        handleRawQuery(response, ".");
    }

    private void handleRawQuery(ValidatableResponse response, String separator) {
        response.statusCode(200)
                .body("md.id", equalTo("ID__170__20DI"))
                .body("data.geo_params" + separator + "centroid", equalTo("-20,-170"))
                .body("data.id", equalTo("ID__170__20DI"))
                .body("data.fullname", equalTo("My name is ID__170__20DI"))
                .body("data.params" + separator + "startdate", equalTo(813400))
                .body("data.params" + separator + "city", isEmptyOrNullString());
    }

    private void handleRawQueryWithColumnsFiltered(ValidatableResponse response) {
        handleRawQueryWithColumnsFiltered(response, ".");
    }

    private void handleRawQueryWithColumnsFiltered(ValidatableResponse response, String separator) {
        response.statusCode(200)
                .body("md.id", equalTo("ID__170__20DI"))
                .body("data.geo_params" + separator + "centroid", equalTo("-20,-170"))
                .body("data.id", equalTo("ID__170__20DI"))
                .body("data.fullname", isEmptyOrNullString())
                .body("data.params" + separator + "startdate", equalTo(813400))
                .body("data.params" + separator + "city", isEmptyOrNullString());
    }

    private ValidatableResponse givenRawQuery(String identifier, Optional<Boolean> flatParam, Optional<String> columnFilter) {
        RequestSpecification given = given();
        flatParam.ifPresent(fp -> given.param("flat", fp));
        columnFilter.ifPresent(cf -> given.header("column-filter", cf));

        return given.when().get(getUrlPath(COLLECTION_NAME) + identifier).then();
    }

}
