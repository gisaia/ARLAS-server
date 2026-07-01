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


package io.arlas.server.tests.stac.conformance.ogc.queryables;

import static org.junit.jupiter.api.Assertions.*;

import io.arlas.server.tests.stac.conformance.ogc.commons.AbstractOgcApiTest;
import io.arlas.server.tests.stac.conformance.ogc.commons.CollectionFilteringMetadata;
import io.arlas.server.tests.stac.conformance.ogc.commons.QueryablesDiscoverySupport;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.*;
// Tests from https://docs.ogc.org/is/19-079r2/19-079r2.html conformance
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryablesConformanceIT extends AbstractOgcApiTest {
    private static final String CONF_QUERYABLES = "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables";
    private List<CollectionFilteringMetadata> metadataList;

    @BeforeAll
    void setUp() {
        metadataList = QueryablesDiscoverySupport.loadQueryablesMetadata(this, apiUri);
    }
    @Test
    @DisplayName("Scenario: A.1.1 Conformance Test 1 - /conf/queryables/get-conformance")
    void getConformanceQueryables() {
        Response response = getJson("/conformance");
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getList("conformsTo", String.class)
                .contains(CONF_QUERYABLES));
    }

    @Test
    @DisplayName("Scenario: A.1.2 Conformance Test 2 - /conf/queryables/get-queryables-uris")
    void getQueryablesUris() {
        assertTrue(metadataList.stream().allMatch(m ->
                m.getQueryablesUri() != null && !m.getQueryablesUri().isBlank()));
    }

    @Test
    @DisplayName("Scenario: A.1.3 Conformance Test 3 - /conf/queryables/get-queryables")
    void getQueryables() {
        assertTrue(metadataList.stream().allMatch(m ->
                m.getSampleQueryable() != null && !m.getSampleQueryable().isBlank()));
    }
}
