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

import io.arlas.server.core.model.enumerations.AggregationTypeEnum;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.GaussianResponse;
import io.arlas.server.tests.AbstractTestWithCollection;
import io.arlas.server.tests.CollectionTool;
import io.arlas.server.tests.GMMTool;
import io.restassured.response.ValidatableResponse;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GMMAggregateServiceIT extends AbstractTestWithCollection {

    protected static List<Aggregation> aggregations;
    protected static GMMRequest gmmRequest;

    @BeforeClass
    public static void beforeClass() throws IOException {
        new CollectionTool().loadGMM(10000);
    }

    @Before
    public void setUpGMMRequest() {
        aggregations = new ArrayList<>();
        request = gmmRequest;
    }

    @AfterClass
    public static void afterClass() {
        new CollectionTool().deleteGMM();
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_geoaggregate/_gmm/";
    }

    protected String getTileUrlPathGMM(String collection, String tile) {
        return getUrlPath(collection) + tile;
    }

    protected void handleGMMGeotileAggregate(ValidatableResponse then, int featuresSize, List<GaussianResponse> gmmResponse) {
        then.statusCode(200)
                .body("features.size()", equalTo(featuresSize))
                .body("features.get(0).properties.gmm.size()", equalTo(gmmResponse.size()));

        FeatureCollection fc = then.extract().response().getBody().as(FeatureCollection.class);
        for (Feature feature : fc.getFeatures()) {
            for (LinkedHashMap<String, Object> map : (List<LinkedHashMap<String, Object>>) feature.getProperties().get("gmm")) {
                assertThat(gmmResponse, hasItem(new GaussianResponse(map)));
            }
        }
    }

    @Test
    public void testGMMAggregate() throws Exception {
        Aggregation aggregationModelSub = new Aggregation();
        aggregations.add(aggregationModelSub);
        aggregations.add(aggregationModelSub);
        aggregations.add(aggregationModelSub);

        aggregations.get(0).type = AggregationTypeEnum.geotile;
        aggregations.get(0).field = GMMTool.DATASET_CENTROID_PATH;
        aggregations.get(0).interval =  new Interval(4, null);

        aggregations.get(1).type = AggregationTypeEnum.histogram;
        aggregations.get(1).field = "current_dir_angle_arlas";
        aggregations.get(1).interval = new Interval(18, null);

        aggregations.get(2).type = AggregationTypeEnum.histogram;
        aggregations.get(2).field = "current_speed";
        aggregations.get(2).interval = new Interval(0.04375, null);

        gmmRequest = new GMMRequest(aggregations, "degree", null, null);

        handleGMMGeotileAggregate(
                given().param("agg", "geotile:point_geom:interval-4")
                        .param("agg", "histogram:current_dir_angle_arlas:interval-18")
                        .param("agg", "histogram:current_speed:interval-0.04375")
                        .param("abscissaunit", "degree")
                        .when().get(getTileUrlPathGMM(GMMTool.COLLECTION_NAME, "4/8/5"))
                        .then(), 1, GMMTool.loadGMM(GMMTool.RESULTS_FILE).get(0));
    }
}
