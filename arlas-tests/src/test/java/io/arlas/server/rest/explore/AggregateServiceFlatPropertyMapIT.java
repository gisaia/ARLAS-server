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

public class AggregateServiceFlatPropertyMapIT extends AggregateServiceIT {
    public AggregateServiceFlatPropertyMapIT() {
        extraParams.add(Pair.of("flat","true"));
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_aggregate";
    }

    //----------------------------------------------------------------
    //----------------------- AGGREGATE PART -------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingGeohashAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))));
    }

    @Override
    protected void handleMatchingAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLonMin))))))
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(greaterThanOrEqualTo(centroidLatMin))))))
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLonMax))))))
                .body("elements.flattened_elements." + collectField.replace(".", "-") + "_" + collectFct + "_.features[0].geometry.coordinates", everyItem(hasItem(everyItem(hasItem(lessThanOrEqualTo(centroidLatMax))))));
    }

    @Override
    protected void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float featureCollectMin,
                                                             float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.flattened_elements." + collectField + "_" + collectFct + "_", everyItem(greaterThanOrEqualTo(featureCollectMin)))
                .body("elements.flattened_elements." + collectField + "_" + collectFct + "_", everyItem(lessThanOrEqualTo(featureCollectMax)));
    }

    @Override
    protected  void handleMatchingAggregateWithMultiCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField1, String collectField2, String collectFct1, String collectFct2,
                                                            float featureCollectMin1, float featureCollectMax1, float featureCollectMin2, float featureCollectMax2) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then
                .body("elements.flattened_elements." + collectField1 + "_" + collectFct1 + "_", everyItem(greaterThanOrEqualTo(Math.min(featureCollectMin1, featureCollectMin2))))
                .body("elements.flattened_elements." + collectField2 + "_" + collectFct2 + "_", everyItem(greaterThanOrEqualTo(Math.min(featureCollectMin1, featureCollectMin2))))
                .body("elements.flattened_elements." + collectField1 + "_" + collectFct1 + "_", everyItem(lessThanOrEqualTo(Math.max(featureCollectMax1, featureCollectMax2))))
                .body("elements.flattened_elements." + collectField2 + "_" + collectFct2 + "_", everyItem(lessThanOrEqualTo(Math.max(featureCollectMax1, featureCollectMax2))));
    }


    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(featuresSize))
                .body("elements.count", everyItem(greaterThanOrEqualTo(featureCountMin)))
                .body("elements.flattened_elements.count", everyItem(greaterThanOrEqualTo(featureCountMin)))
                .body("elements.count", everyItem(lessThanOrEqualTo(featureCountMax)))
                .body("elements.flattened_elements.count", everyItem(lessThanOrEqualTo(featureCountMax)));
    }


    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String keyAsString) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then.body("elements.key_as_string", everyItem(equalTo(keyAsString)));
        then.body("elements.flattened_elements.keyAsString", everyItem(equalTo(keyAsString)));
    }

    @Override
    protected void handleMatchingAggregateWithOrder(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String firstKey) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then.body("elements[0].key_as_string", equalTo(firstKey));
        then.body("elements[0].flattened_elements.keyAsString", equalTo(firstKey));
    }

    @Override
    protected void handleMultiMatchingAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        handleMultiMatchingGeohashAggregate(then, featuresSize);
    }

    @Override
    protected void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        then.statusCode(200)
                .body("elements.size()", equalTo(featuresSize))
                .body("elements.flattened_elements", hasSize(featuresSize));
    }

    //----------------------------------------------------------------
    //---------------------- Validatable Response ------------------
    //----------------------------------------------------------------

    @Override
    protected RequestSpecification handleGetRequest(RequestSpecification req){
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.queryParam(extraParam.getKey(), extraParam.getValue());
        }
        return req;
    }

    protected ValidatableResponse post(Request request) {
        if(request.form == null) {
            request.form = new Form();
        }
        request.form.flat = true;
        return given().contentType("application/json;charset=utf-8").body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

}
