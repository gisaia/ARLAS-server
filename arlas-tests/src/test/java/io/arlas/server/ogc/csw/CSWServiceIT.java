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

package io.arlas.server.ogc.csw;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.AbstractTestWithCollection;
import io.arlas.server.CollectionTool;
import io.arlas.server.model.DublinCoreElementName;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import static io.restassured.RestAssured.given;

public class CSWServiceIT extends AbstractTestWithCollection {

    static DublinCoreElementName[] dcelements;

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "ogc/csw";
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        InputStreamReader dcelementForCollection = new InputStreamReader(CollectionTool.class.getClassLoader().getResourceAsStream("csw.collection.dcelements.json"));
        dcelements = new ObjectMapper().readValue(dcelementForCollection, DublinCoreElementName[].class);

        new CollectionTool().loadCsw(10000l);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        new CollectionTool().deleteCsw();
    }

    @Test
    public void testGetCapabilities() throws Exception {
        handleGetCapabilities(get("GetCapabilities"));
    }

    @Test
    public void testGetRecords() throws Exception {
        handleGetRecords(get("GetRecords"));
    }

    @Test
    public void testGetRecordById() throws Exception {

        String collectionName = "Panda Collection";
        Object id = getRecordId(collectionName);

        handleGetRecordById(
                get("GetRecordById", Arrays.asList(new ImmutablePair<>("ID", (String)id))),
                collectionName);
    }

    private void handleGetCapabilities(ValidatableResponse response) {
        String[] collectionNames = Arrays.stream(dcelements).map(el -> el.title.split(" ")[0].toLowerCase()).toArray(String[]::new);

        response.statusCode(200)
                .body("Capabilities.ServiceIdentification.Keywords.Keyword.findAll()", Matchers.hasItems(collectionNames))
                .body("Capabilities.OperationsMetadata.ExtendedCapabilities.ExtendedCapabilities.Keyword.KeywordValue.findAll()", Matchers.hasItems(collectionNames));
    }

    private void handleGetRecords(ValidatableResponse response) {
        String[] collectionTitles = Arrays.stream(dcelements).map(el -> el.title).toArray(String[]::new);
        
        response.statusCode(200)
                .body("GetRecordsResponse.SearchResults.SummaryRecord.title.findAll()", Matchers.hasItems(collectionTitles));
    }

    private void handleGetRecordById(ValidatableResponse response, String collectionName) {
        response.statusCode(200)
                .body("SummaryRecord.title", Matchers.equalTo(collectionName));
    }

    private Object getRecordId(String collection) {
        return get("GetRecords")
                .statusCode(200)
                .extract()
                .path("GetRecordsResponse.SearchResults.SummaryRecord.find { it.title = '" + collection + "' }.identifier");
    }

    private RequestSpecification givenFilterableRequestParams() {
        return given().contentType("application/xml");
    }

    private ValidatableResponse get(String request) {
        return this.get(request, Arrays.asList());
    }

    private ValidatableResponse get(String request, List<Pair<String, String>> params) {
        RequestSpecification req = givenFilterableRequestParams()
                .param("VERSION", "3.0.0")
                .param("SERVICE", "CSW")
                .param("REQUEST", request);

        for (Pair<String, String> param : params) {
            req = req.param(param.getKey(), param.getValue());
        }

        return req
                .when().get(getUrlPath(null))
                .then();
    }

}
