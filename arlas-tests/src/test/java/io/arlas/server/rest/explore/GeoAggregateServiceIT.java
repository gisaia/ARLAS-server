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

import io.arlas.server.model.enumerations.AggregationTypeEnum;
import io.arlas.server.model.request.Interval;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GeoAggregateServiceIT extends AbstractGeohashTiledTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_geoaggregate";
    }

    @Override
    protected String getGeohashUrlPath(String collection, String geohash) {
        return getUrlPath(collection) + "/" + geohash;
    }

    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
                .body("type", equalTo("FeatureCollection"))
                .body("$", not(hasKey("features")));
    }

    //----------------------------------------------------------------
    //----------------------- AGGREGATE PART -------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(featuresSize))
                .body("features.properties.count", everyItem(greaterThanOrEqualTo(featureCountMin)))
                .body("features.properties.count", everyItem(lessThanOrEqualTo(featureCountMax)))
                .body("features.properties.feature_type", everyItem(equalTo("aggregation")))
                .body("sumotherdoccounts", nullValue());
    }

    @Override
    protected void handleMatchingGeohashAggregateCenter(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))
                .body("features.geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))
                .body("features.geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))
                .body("features.geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))));
    }

    protected void handleMatchingGeohashAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))
                .body("features.properties.elements[0].name", everyItem(startsWith(collectFct)))
                .body("features.properties.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeocentroidBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))
                .body("features.geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))
                .body("features.geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))
                .body("features.geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))
                .body("features[0].properties.elements.size()", equalTo(elementsSize));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))))
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))))
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))))
                .body("features.properties.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))))
                .body("features.properties.elements[0].name", everyItem(startsWith(collectFct)))
                .body("features.properties.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeoBboxBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))))
                .body("features.geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))))
                .body("features.geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))))
                .body("features.geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))))
                .body("features[0].properties.elements.size()", equalTo(elementsSize));
    }

    @Override
    protected void handleMatchingAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
                                                             float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties.elements[0].metric.value", everyItem(greaterThanOrEqualTo(featureCollectMin)))
                .body("features.properties.elements[0].metric.value", everyItem(lessThanOrEqualTo(featureCollectMax)))
                .body("features.properties.elements[0].name", everyItem(startsWith(collectFct)))
                .body("features.properties.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    @Override
    protected  void handleMatchingGeohashAggregateWithMultiCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct1, String collectFct2,
                                                            float featureCollectMin1, float featureCollectMax1, float featureCollectMin2, float featureCollectMax2) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties.elements[0].metric.value", everyItem(greaterThanOrEqualTo(Math.min(featureCollectMin1, featureCollectMin2))))
                .body("features.properties.elements[1].metric.value", everyItem(greaterThanOrEqualTo(Math.min(featureCollectMin1, featureCollectMin2))))
                .body("features.properties.elements[0].metric.value", everyItem(lessThanOrEqualTo(Math.max(featureCollectMax1, featureCollectMax2))))
                .body("features.properties.elements[1].metric.value", everyItem(lessThanOrEqualTo(Math.max(featureCollectMax1, featureCollectMax2))))
                .body("features.properties.elements[0].name", hasItem(startsWith(collectFct1)))
                .body("features.properties.elements[1].name", hasItem(startsWith(collectFct2)))
                .body("features.properties.elements[0].metric.type", hasItem(equalTo(collectFct1)))
                .body("features.properties.elements[1].metric.type", hasItem(equalTo(collectFct2)));
    }

    @Override
    protected void handleSumOtherCountsExistence(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleSumOtherCountsExistence(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int sumOtherDocCounts) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMatchingAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
                                                      float featureCollectMax) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected  void handleMatchingAggregateWithMultiCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct1, String collectFct2,
                                                            float featureCollectMin1, float featureCollectMax1, float featureCollectMin2, float featureCollectMax2) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMatchingAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        then.statusCode(400);
    }


    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String keyAsString) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMatchingAggregateWithOrder(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String firstKey) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMultiMatchingAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        then.statusCode(400);
    }

    @Override
    protected void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(featuresSize))
                .body("features.properties.elements.elements", hasSize(featuresSize))
                .body("features.properties.elements.elements", hasSize(featuresSize));
    }

    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given().param("agg", "geohash:geo_params.centroid:interval-3");
    }

    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohash;
        aggregationRequest.aggregations.get(0).field = "geo_params.centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(3, null);
        request = aggregationRequest;
        return given().contentType("application/json;charset=utf-8");
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(1));
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception {
        handleFieldFilter(then, nbResults);
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(nbResults));
    }

    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then, int nbResults) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(nbResults));
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int size) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(size));
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int size) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(size));
    }

    @Override
    protected void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(nbResults));
    }

    //----------------------------------------------------------------
    //----------------------- GEOHASH TILES PART -------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleGeohashTileGreaterThanPrecision(ValidatableResponse then, int count, String geohash) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(1))
                .body("features[0].properties.count", equalTo(count))
                .body("features[0].properties.geohash", lessThanOrEqualTo(geohash));
    }

    @Override
    protected void handleGeohashTileLessThanPrecision(ValidatableResponse then, int featuresSize, String geohash) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(featuresSize))
                .body("features.properties.geohash", everyItem(greaterThanOrEqualTo(geohash)));
    }

    @Override
    protected void handleGeohashTileDisjointFromPwithin(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("features", equalTo(null));
    }

    @Override
    protected void handleInvalidGeohashTile(ValidatableResponse then) {
        then.statusCode(400);
    }


}
