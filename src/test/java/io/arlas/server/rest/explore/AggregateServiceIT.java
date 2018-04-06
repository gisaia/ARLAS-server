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

import io.arlas.server.model.request.AggregationTypeEnum;
import io.arlas.server.model.request.Interval;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AggregateServiceIT extends AbstractAggregatedTest {

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_aggregate";
    }

    //----------------------------------------------------------------
    //----------------------- AGGREGATE PART -------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
    }

    @Override
    protected void handleMatchingAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregateWithGeocentroidCollect(then, featuresSize, featureCountMin, featureCountMax, collectFct, centroidLonMin, centroidLatMin, centroidLonMax, centroidLatMax);
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))
                .body("elements.elements[0].name", everyItem(equalTo(collectFct)))
                .body("elements.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeocentroidBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.centroid.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))
                .body("elements.centroid.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))
                .body("elements.centroid.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))
                .body("elements.centroid.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeoBboxBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.bbox.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))))
                .body("elements.bbox.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))))
                .body("elements.bbox.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))))
                .body("elements.bbox.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))));
    }

    @Override
    protected void handleMatchingAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))))
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))))
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))))
                .body("elements.elements[0].metric.value.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))))
                .body("elements.elements[0].name", everyItem(equalTo(collectFct)))
                .body("elements.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingAggregateWithGeoBboxCollect(then, featuresSize, featureCountMin, featureCountMax, collectFct, centroidLonMin, centroidLatMin, centroidLonMax, centroidLatMax);
    }

    @Override
    protected void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
                                                             float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.elements[0].metric.value", everyItem(greaterThanOrEqualTo(featureCollectMin)))
                .body("elements.elements[0].metric.value", everyItem(lessThanOrEqualTo(featureCollectMax)))
                .body("elements.elements[0].name", everyItem(equalTo(collectFct)))
                .body("elements.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    @Override
    protected void handleMatchingGeohashAggregateCenter(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.key.lon", everyItem(greaterThanOrEqualTo(centroidLonMin)))
                .body("elements.key.lat", everyItem(greaterThanOrEqualTo(centroidLatMin)))
                .body("elements.key.lon", everyItem(lessThanOrEqualTo(centroidLonMax)))
                .body("elements.key.lat", everyItem(lessThanOrEqualTo(centroidLatMax)));
    }

    @Override
    protected void handleSumOtherCountsExistence(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        handleSumOtherCountsExistence(then, featuresSize, featureCountMin, featureCountMax, -1);

    }

    @Override
    protected void handleSumOtherCountsExistence(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int sumOtherDocCounts) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        if (sumOtherDocCounts == -1) {
            then
                    .body("sumotherdoccounts", nullValue());
        } else {
            then
                    .body("sumotherdoccounts", equalTo(sumOtherDocCounts));
        }
    }

    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(featuresSize))
                .body("elements.count", everyItem(greaterThanOrEqualTo(featureCountMin)))
                .body("elements.count", everyItem(lessThanOrEqualTo(featureCountMax)));
    }

    @Override
    protected void handleMatchingAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
                                                      float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregateWithCollect(then, featuresSize, featureCountMin, featureCountMax, collectFct, featureCollectMin, featureCollectMax);
    }

    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String keyAsString) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then.body("elements.key_as_string", everyItem(equalTo(keyAsString)));
    }

    @Override
    protected void handleMatchingAggregateWithOrder(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String firstKey) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then.body("elements[0].key_as_string", equalTo(firstKey));
    }

    @Override
    protected void handleMultiMatchingAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        handleMultiMatchingGeohashAggregate(then, featuresSize);
    }

    @Override
    protected void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(featuresSize))
                .body("elements.elements.elements", hasSize(featuresSize))
                .body("elements.elements.elements", hasSize(featuresSize));
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
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
                .body("totalnb", equalTo(0));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(1));
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(nbResults));
    }

    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then, int nbResults) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(nbResults));
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int size) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(size));
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int size) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(size));
    }

    @Override
    protected void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(nbResults));
    }
}
