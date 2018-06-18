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

import io.arlas.server.model.request.Form;
import io.arlas.server.model.request.Request;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.Pair;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

public class GeoAggregateServiceFlatPropertyMapIT extends GeoAggregateServiceIT{
    public GeoAggregateServiceFlatPropertyMapIT() {
        extraParams.add(Pair.of("flat","true"));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties", everyItem(hasKey(collectField.replace(".", "-") + "_" + collectFct + "_")));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeocentroidBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features[0].properties.size()", equalTo(5));// 5 = default value when nothing fetched
    }


    @Override
    protected void handleMatchingGeohashAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties", everyItem(hasKey(collectField.replace(".", "-") + "_" + collectFct + "_")));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithGeoBboxBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features[0].properties.size()", equalTo(5 + elementsSize));// 5 = default value when nothing fetched
    }
    @Override
    protected void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float featureCollectMin,
                                                             float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties."+collectField+"_"+collectFct + "_", everyItem(greaterThanOrEqualTo(featureCollectMin)))
                .body("features.properties."+collectField+"_"+collectFct + "_", everyItem(lessThanOrEqualTo(featureCollectMax)));
    }

    @Override
    protected  void handleMatchingGeohashAggregateWithMultiCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField1, String collectField2, String collectFct1, String collectFct2,
                                                                   float featureCollectMin1, float featureCollectMax1, float featureCollectMin2, float featureCollectMax2) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("features.properties."+collectField1+"_"+collectFct1 + "_", everyItem(greaterThanOrEqualTo(Math.min(featureCollectMin1, featureCollectMin2))))
                .body("features.properties."+collectField2+"_"+collectFct2 + "_", everyItem(greaterThanOrEqualTo(Math.min(featureCollectMin1, featureCollectMin2))))
                .body("features.properties."+collectField1+"_"+collectFct1 + "_", everyItem(lessThanOrEqualTo(Math.max(featureCollectMax1, featureCollectMax2))))
                .body("features.properties."+collectField2+"_"+collectFct2 + "_", everyItem(lessThanOrEqualTo(Math.max(featureCollectMax1, featureCollectMax2))));
    }

    @Override
    protected void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(featuresSize))
                .body("features.properties", hasSize(featuresSize));
    }

    @Override
    protected RequestSpecification handleGetRequest(RequestSpecification req){
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.queryParam(extraParam.getKey(), extraParam.getValue());
        }
        return req;
    }

    protected ValidatableResponse post(Request request) {
        if(request.form==null)request.form=new Form();
        request.form.flat=true;
        return given().contentType("application/json;charset=utf-8").body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }
}
