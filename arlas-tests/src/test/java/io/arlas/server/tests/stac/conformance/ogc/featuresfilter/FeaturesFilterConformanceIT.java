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

package io.arlas.server.tests.stac.conformance.ogc.featuresfilter;

import io.arlas.server.tests.stac.conformance.ogc.commons.AbstractOgcApiTest;
import io.arlas.server.tests.stac.conformance.ogc.commons.CollectionDiscoverySupport;
import io.arlas.server.tests.stac.conformance.ogc.commons.CollectionFilteringMetadata;
import io.arlas.server.tests.stac.conformance.ogc.commons.CqlFilterBuilder;
import io.arlas.server.tests.stac.conformance.ogc.commons.FilteringAssertions;
import io.arlas.server.tests.stac.conformance.ogc.commons.QueryablesDiscoverySupport;
import io.arlas.server.tests.stac.conformance.ogc.filter.FilterConformanceIT;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FeaturesFilterConformanceIT extends AbstractOgcApiTest {

    private static final String CONF_FEATURES_FILTER =
            "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/features-filter";

    private List<CollectionFilteringMetadata> metadataList;

    @BeforeAll
    void setUp() {
        metadataList = QueryablesDiscoverySupport.loadQueryablesMetadata(this, apiUri);
        CollectionDiscoverySupport.enrichCollectionsMetadata(this, metadataList);
    }

    @Test
    @DisplayName("Scenario: A.4.1 Conformance Test 13 - /conf/features-filter/get-conformance")
    void getConformance() {
        Response response = getJson("/conformance");
        response.then().statusCode(200);
        assertTrue(response.contentType().contains("application/json"));
        assertTrue(response.jsonPath().getList("conformsTo", String.class).contains(CONF_FEATURES_FILTER));
    }

    @Test
    @DisplayName("Scenario: A.4.2 Conformance Test 14 - /conf/features-filter/get-collections")
    void getCollections() {
        Response response = getJson("/collections");
        response.then().statusCode(200);
        assertTrue(response.contentType().contains("application/json"));
        assertTrue(response.jsonPath().getList("collections") != null);
    }

    @Test
    @DisplayName("Scenario: A.4.3 Conformance Test 15 - /conf/features-filter/get-collection")
    void getCollection() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            Response response = getJson("/collections/" + metadata.getCollectionId());
            response.then().statusCode(200);
            assertTrue(response.contentType().contains("application/json"));

            boolean hasQueryablesLink = response.jsonPath().getList("links").stream().anyMatch(linkObj -> {
                if (!(linkObj instanceof Map<?, ?> link)) {
                    return false;
                }
                Object rel = link.get("rel");
                Object href = link.get("href");
                return "http://www.opengis.net/def/rel/ogc/1.0/queryables".equals(String.valueOf(rel))
                        && (RestAssured.baseURI + ":" + RestAssured.port + apiUri + "/collections/" + metadata.getCollectionId() + "/queryables").equals(String.valueOf(href));
            });
            assertTrue(hasQueryablesLink);
        }
    }

    @Test
    @DisplayName("Scenario: A.4.4 Conformance Test 16 - /conf/features-filter/filter-on-items")
    void filterOnItems() {
        FilterConformanceIT filterConformance = new FilterConformanceIT();
        for (CollectionFilteringMetadata metadata : metadataList) {
            Response response = filterConformance.get(metadata.getItemsPath(), "application/geo+json");
            assertTrue(response.statusCode() == 200 || response.statusCode() == 204);
        }
    }

    @Test
    @DisplayName("Scenario: A.4.5 Conformance Test 17 - /conf/features-filter/mixing-expression")
    void mixingExpression() {
        for (CollectionFilteringMetadata metadata : metadataList) {
            if (metadata.getSpatialQueryable() == null || metadata.getWgs84Bbox().size() < 4) {
                continue;
            }

            String path = metadata.getItemsPath();

            Response unfiltered = get(path, "application/geo+json");
            assertTrue(unfiltered.statusCode() == 200 || unfiltered.statusCode() == 204);
            if (unfiltered.statusCode() == 200) {
                metadata.setUnfilteredFeatures(unfiltered.jsonPath().getList("features"));
            }

            List<Double> bbox = metadata.getWgs84Bbox();
            String filter = CqlFilterBuilder.sIntersectsBbox(metadata.getSpatialQueryable(), bbox);

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("filter-lang", "cql2-text");
            params.put("filter", filter);
            params.put("bbox", bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + "," + bbox.get(3));

            Response response = get(metadata.getItemsPath(), params, "application/geo+json");
            assertTrue(response.statusCode() == 200 || response.statusCode() == 204);
            if (response.statusCode() == 200) {
                FilteringAssertions.assertSameFeatureIds(
                        metadata.getUnfilteredFeatures(),
                        response.jsonPath().getList("features")
                );
            }
        }
    }
}
