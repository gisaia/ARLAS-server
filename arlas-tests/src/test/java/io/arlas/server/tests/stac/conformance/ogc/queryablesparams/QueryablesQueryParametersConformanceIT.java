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

package io.arlas.server.tests.stac.conformance.ogc.queryablesparams;

import io.arlas.server.tests.stac.conformance.ogc.commons.AbstractOgcApiTest;
import io.arlas.server.tests.stac.conformance.ogc.commons.CollectionFilteringMetadata;
import io.arlas.server.tests.stac.conformance.ogc.commons.FilteringAssertions;
import io.arlas.server.tests.stac.conformance.ogc.commons.QueryablesDiscoverySupport;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
// Tests from https://docs.ogc.org/is/19-079r2/19-079r2.html conformance
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryablesQueryParametersConformanceIT extends AbstractOgcApiTest {

    private static final String CONF_QUERYABLES_QUERY = "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables-query-parameters";
    private List<CollectionFilteringMetadata> metadataList;

    @BeforeAll
    void setUp() {
        metadataList = QueryablesDiscoverySupport.loadQueryablesMetadata(this, apiUri);
    }

    @Test
    @DisplayName("Scenario: A.2.1 Conformance Test 4 - /conf/queryables-query-parameters/get-conformance")
    void getConformanceQueryablesQueryParameters() {
        Response response = getJson("/conformance");
        response.then().statusCode(200);
        assertTrue(response.contentType().contains("application/json"));
        assertTrue(response.jsonPath().getList("conformsTo", String.class)
                .contains(CONF_QUERYABLES_QUERY));
    }


    @Test
    @DisplayName("Scenario: A.2.2 Conformance Test 5 - /conf/queryables-query-parameters/query-param")
    void queryParam() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            assertNotNull(metadata.getSampleQueryable());
            String queryable = metadata.getSampleQueryable();
            Object validValue = TestValueRegistry.validValue(queryable);
            Object invalidValue = TestValueRegistry.invalidValue(queryable);
            if (validValue != null) {
                Response ok = get(metadata.getItemsPath(), Map.of(queryable, validValue), "application/geo+json");
                assertEquals(200, ok.statusCode());
                assertTrue(ok.contentType().contains("application/geo+json"));
                FilteringAssertions.assertEveryReturnedResourceMatchesQueryable(ok, queryable, validValue);
            }
            if (invalidValue != null) {
                Response ko = get(metadata.getItemsPath(), Map.of(queryable, invalidValue), "application/geo+json");
                assertEquals(400, ko.statusCode());
            }
        }
    }
}
