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

package io.arlas.server.tests.rest.explore.opensearch;

import io.arlas.server.tests.AbstractTestWithCollection;
import io.arlas.server.tests.DataSetTool;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.XmlConfig;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

public class OpenSearchServiceIT extends AbstractTestWithCollection {

    private static final Map<String, Integer> AVAILABLE_COLUMNS = new HashMap<>();
    static {
        AVAILABLE_COLUMNS.put("fullname", 4);
        AVAILABLE_COLUMNS.put("id", 2);
        AVAILABLE_COLUMNS.put("text_search", 4);

        AVAILABLE_COLUMNS.put("params.country", 2);
        AVAILABLE_COLUMNS.put("params.keywords", 2);
        AVAILABLE_COLUMNS.put("params.weight", 14);
        AVAILABLE_COLUMNS.put("params.job", 2);
        AVAILABLE_COLUMNS.put("params.startdate", 14);
        AVAILABLE_COLUMNS.put("params.stopdate", 14);
        AVAILABLE_COLUMNS.put("params.age", 14);
        AVAILABLE_COLUMNS.put("params.tags", 2);
        AVAILABLE_COLUMNS.put("params.not_indexed", 0);
        AVAILABLE_COLUMNS.put("params.not_enabled", 0);
    }
    private static final List<String> MANDATORY_COLUMNS = Arrays.asList("id", "params.startdate");
    private static final List<String> ALIASED_COLUMNS = Arrays.asList("params.keywords");

    /**
     * Return a set with all mandatory columns and columns passed as parameter.
     * ALIASED_COLUMNS are automatically excluded if necessary.
     * @param columnsPrefix prefix of the columns to include
     * @return
     */
    private Map<String, Integer> withColumns(String... columnsPrefix) {
        Set<String> columnsSet = new HashSet<>(Arrays.asList(columnsPrefix));
        return AVAILABLE_COLUMNS.entrySet().stream()
                .filter(e -> columnsSet.stream().anyMatch(c -> e.getKey().startsWith(c)) || MANDATORY_COLUMNS.contains(e.getKey()))
                .filter(e -> DataSetTool.ALIASED_COLLECTION ? true : !ALIASED_COLUMNS.contains(e.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/ogc/opensearch/" + collection;
    }

    @Test
    public void testOpenSearchFeature() throws Exception {
        handleOpenSearchFeature(get());
    }

    @Test
    public void testOpenSearchFeatureWithAvailableColumns() throws Exception {
        handleOpenSearchFeature(get());
        handleOpenSearchFeature(get("fullname,text_search,params,geo_params.h3"));

    }

    @Test
    public void testOpenSearchFeatureWithUnavailableColumns() throws Exception {
        handleOpenSearchFeatureByColumn(get("fullname"), withColumns("fullname"));
        handleOpenSearchFeatureByColumn(get("params.startdate"), withColumns());
        handleOpenSearchFeatureByColumn(get("params"), withColumns("params."));
        handleOpenSearchFeatureByColumn(get("params.*"), withColumns("params."));
    }

    @Test
    public void testOpenSearchFeatureWithCollectionBasedColumnFiltering() throws Exception {
        handleOpenSearchFeatureByColumn(get(COLLECTION_NAME + ":fullname,notExisting:params"), withColumns("fullname"));
        handleOpenSearchFeatureByColumn(get("params.startdate,notExisting:params"), withColumns());
        handleOpenSearchFeatureByColumn(get(COLLECTION_NAME + ":params,notExisting:fullname"), withColumns("params."));
        handleOpenSearchFeatureByColumn(get(COLLECTION_NAME + ":params.*,notExisting:full*"), withColumns("params."));
        handleUnavailableCollection(get("notExisting:full*"));
        handleUnavailableCollection(get(""));
    }

    private void handleOpenSearchFeature(ValidatableResponse then) throws Exception {
        handleOpenSearchFeatureByColumn(then, withColumns(""));
    }

    private void handleOpenSearchFeatureByColumn(ValidatableResponse then, Map<String, Integer> expectedFields)
            throws Exception {

        ValidatableResponse root = then.statusCode(200)
                .body("ns2:OpenSearchDescription.ns2:Url.size()", is(expectedFields.values().stream().mapToInt(Integer::intValue).sum()))
                .root("ns2:OpenSearchDescription.ns2:Url.@template.grep(~/.*%s.*/).size()");

        expectedFields.entrySet().forEach(e -> root.body(withArgs(e.getKey()), is(e.getValue())));
    }

    private void handleUnavailableCollection(ValidatableResponse then) {
        then.statusCode(403)
                .body(stringContainsInOrder(Arrays.asList("collection", "available")));
    }

    protected RequestSpecification givenXmlNamespace(){
        return given().config(RestAssuredConfig.newConfig().xmlConfig(XmlConfig.xmlConfig().declareNamespace("ns2", "http://a9.com/-/spec/opensearch/1.1/")));
    }

    private ValidatableResponse get() {
        return givenXmlNamespace().when()
                .get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String columnFilter) {
        return givenXmlNamespace().header(COLUMN_FILTER, columnFilter)
                .when().get(getUrlPath("geodata"))
                .then();
    }

}