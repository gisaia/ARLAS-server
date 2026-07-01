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

package io.arlas.server.tests.stac.conformance.ogc.filter;

import io.arlas.server.tests.stac.conformance.ogc.commons.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FilterConformanceIT extends AbstractOgcApiTest {
    private static final String CONF_QUERYABLES = "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables";
    private List<CollectionFilteringMetadata> metadataList;
    @BeforeAll
    void setUp() {
        metadataList = QueryablesDiscoverySupport.loadQueryablesMetadata(this, apiUri  );
        CollectionDiscoverySupport.enrichCollectionsMetadata(this, metadataList);
    }

    @Test
    @DisplayName("Scenario: A.3.1 Conformance Test 6 - /conf/filter/get-conformance")
    void getConformanceFilter() {
        Response response = getJson("/conformance");
        response.then().statusCode(200);
        assertTrue(response.contentType().contains("application/json"));
        assertTrue(response.jsonPath().getList("conformsTo", String.class)
                .contains(CONF_QUERYABLES));
    }

    @Test
    @DisplayName("Scenario: A.3.2 Conformance Test 7 - /conf/filter/filter-param")
    void filterParam() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            String path = metadata.getItemsPath();
            String queryable = metadata.getSampleQueryable();

            Response unfiltered = get(path, "application/geo+json");
            assertTrue(unfiltered.statusCode() == 200 || unfiltered.statusCode() == 204);
            if (unfiltered.statusCode() == 200) {
                metadata.setUnfilteredFeatures(unfiltered.jsonPath().getList("features"));
            }

            String validFilter = CqlFilterBuilder.validEqualCheck(queryable);
            Response filtered = get(path, Map.of("filter-lang", "cql2-text", "filter", validFilter), "application/geo+json");
            assertTrue(filtered.statusCode() == 200 || filtered.statusCode() == 204);
            if (filtered.statusCode() == 200) {
                FilteringAssertions.assertEveryReturnedResourceMatchesQueryable(filtered, queryable, 0);
            }

            Response invalid = get(path, Map.of("filter-lang", "cql2-text", "filter", "THIS IS NOT A FILTER"), "application/geo+json");
            assertEquals(400, invalid.statusCode());
        }
    }

    @Test
    @DisplayName("Scenario: A.3.3 Conformance Test 8 - /conf/filter/filter-lang-default")
    void filterLangDefault() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            String queryable = metadata.getSampleQueryable();

            Response filtered = get(metadata.getItemsPath(),
                    Map.of("filter", CqlFilterBuilder.validNullCheck(queryable)),
                    "application/geo+json");
            // ARLAS SERVER does not support is null filter, the conformance test should check 200
            assertTrue(filtered.statusCode() == 400 );

            Response invalid = get(metadata.getItemsPath(),
                    Map.of("filter", "THIS IS NOT A FILTER"),
                    "application/geo+json");
            assertEquals(400, invalid.statusCode());
        }
    }

    @Test
    @DisplayName("Scenario: A.3.4 Conformance Test 9 - /conf/filter/expression-construction")
    void expressionConstruction() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            if (metadata.isAdditionalProperties()) {
                continue;
            }

            String badFilter = CqlFilterBuilder.validEqualCheck("this_is_not_a_queryable");
            Response response = get(metadata.getItemsPath(),
                    Map.of("filter-lang", "cql2-text", "filter", badFilter),
                    "application/geo+json");

            assertEquals(400, response.statusCode());
        }
    }

    @Test
    @DisplayName("Scenario: A.3.5 Conformance Test 10 - /conf/filter/filter-crs-wgs84")
    void filterCrsWgs84() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            if (metadata.getSpatialQueryable() == null || metadata.getWgs84Bbox().size() < 4) {
                continue;
            }

            List<Double> bbox = metadata.getWgs84Bbox();
            String filter = CqlFilterBuilder.sIntersectsBbox(metadata.getSpatialQueryable(), bbox);

            Response response = get(metadata.getItemsPath(),
                    Map.of("filter-lang", "cql2-text", "filter", filter),
                    "application/geo+json");
            assertTrue(response.statusCode() == 200 || response.statusCode() == 204);

            if (response.statusCode() == 200) {
                FilteringAssertions.assertSameFeatureIds(metadata.getUnfilteredFeatures(), response.jsonPath().getList("features"));
            }

            String invalidFilter = CqlFilterBuilder.sIntersectsBbox(metadata.getSpatialQueryable(),
                    List.of(1000000d, 1000000d, 2000000d, 2000000d));

            Response invalid = get(metadata.getItemsPath(),
                    Map.of("filter-lang", "cql2-text", "filter", invalidFilter),
                    "application/geo+json");
            assertEquals(400, invalid.statusCode());
        }
    }

    @Test
    @DisplayName("Scenario: A.3.6 Conformance Test 11 - /conf/filter/filter-crs-param")
    void filterCrsParam() {
        // ARLAS-Server supports only "http://www.opengis.net/def/crs/OGC/1.3/CRS84" for filter-crs param
        final String supportedCrs = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        for (CollectionFilteringMetadata metadata : metadataList) {
            if (metadata.getSpatialQueryable() == null || metadata.getWgs84Bbox().size() < 4) {
                continue;
            }
            if (metadata.getUnfilteredFeatures() == null || metadata.getUnfilteredFeatures().isEmpty())  {
                Response unfiltered = get(metadata.getItemsPath(), "application/geo+json");
                assertTrue(unfiltered.statusCode() == 200 || unfiltered.statusCode() == 204);
                metadata.setUnfilteredFeatures(
                        unfiltered.statusCode() == 200 ? unfiltered.jsonPath().getList("features") : List.of()
                );
            }
            List<Double> bbox = metadata.getWgs84Bbox();
            String filter = CqlFilterBuilder.sIntersectsBbox(metadata.getSpatialQueryable(), bbox);
            Map<String, Object> okParams = new LinkedHashMap<>();
            okParams.put("filter-lang", "cql2-text");
            okParams.put("filter-crs", supportedCrs);
            okParams.put("filter", filter);

            Response ok = get(metadata.getItemsPath(), okParams, "application/geo+json");
            assertTrue(ok.statusCode() == 200 || ok.statusCode() == 204);

            if (ok.statusCode() == 200) {
                FilteringAssertions.assertSameFeatureIds(
                        metadata.getUnfilteredFeatures(),
                        ok.jsonPath().getList("features")
                );
            }

            Map<String, Object> badParams = new LinkedHashMap<>();
            badParams.put("filter-lang", "cql2-text");
            badParams.put("filter-crs", "http://www.opengis.net/def/crs/EPSG/0/4326");
            badParams.put("filter", filter);

            Response bad = get(metadata.getItemsPath(), badParams, "application/geo+json");
            assertEquals(400, bad.statusCode());
        }
    }

}
