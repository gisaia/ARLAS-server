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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class GeoSearchServiceIT extends AbstractXYZTiledTest {
    
    @Override
    public String getUrlPath(String collection) {
        return arlasPrefix + "explore/"+collection+"/_geosearch";
    }

    @Override
    public String getXYZUrlPath(String collection, int z, int x, int y) { return getUrlPath(collection)+ "/" + z + "/" + x + "/" + y; }
    
    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
        .body("type", equalTo("FeatureCollection"))

        .body("$", not(hasKey("features")));
    }

    
    
    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given();
    }
    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        return given().contentType("application/json");
    }
    
    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features[0].properties.params.job", equalTo("Architect"))
        .body("features[0].properties.params.startdate", equalTo(1009800))
        .body("features[0].properties.geo_params.centroid", equalTo("20,-10"));
    }
    
    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(equalTo("Actor")));
    }
    
    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(isOneOf("Actor","Announcers")));
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(equalTo("Actor")));
    }

    //TODO : fix the case where the field is full text
    /*@Override
    protected void handleKnownFullTextFieldLikeFilter(ValidatableResponse then) throws Exception {
         then.statusCode(200)
        .body("features.properties.job", everyItem(isOneOf("Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }*/

    @Override
    protected void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(isOneOf("Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }
    
    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(10));//get only default sized result array
    }
    
    @Override
    protected void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.params.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.params.startdate", everyItem(greaterThan(1250000)));
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(2))
        .body("features.properties.params.startdate", everyItem(greaterThan(770000)))
        .body("features.properties.params.startdate", everyItem(lessThan(775000)));
    }
    
    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then, String centroid) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo(centroid)));
    }
    
    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(10))//get only default sized result array
        .body("features.properties.geo_params.centroid", everyItem(endsWith("170")));
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8))
        .body("features.properties.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }
    
    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(4))
        .body("features.properties.geo_params.centroid", hasItems("-70,170","-80,170","-70,160","-80,160"));
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8))
        .body("features.properties.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }
    
    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("-80,170")));
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.geo_params.centroid", hasItems("10,-10","0,-10","-10,-10"));
    }

    //----------------------------------------------------------------
    //----------------------- SIZE PART ------------------------------
    //----------------------------------------------------------------
    @Override
    protected RequestSpecification givenBigSizedRequestParams() {
        return given().param("q", "My name is");
    }

    @Override
    protected RequestSpecification givenBigSizedRequestParamsPost() {
        search.filter.q = "My name is";
        return given().contentType("application/json");
    }
    
    @Override
    protected int getBigSizedResponseSize() {
        return 595;
    }
    
    @Override
    protected void handleSizeParameter(ValidatableResponse then, int size) {
        if(size > 0) {
            then.statusCode(200)
                .body("features.size()", equalTo(size));
        } else {
            then.statusCode(200)
            .body("$", not(hasKey("features")));
        }
    }

    //----------------------------------------------------------------
    //----------------------- PROJECTION PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleHiddenParameter(ValidatableResponse then, List<String> hidden) throws Exception {
        then.statusCode(200);
        for(String key : hidden) {
            String path = "features.properties";
            String lastKey = key;
            if(key.contains(".")) {
                path += ("."+key.substring(0, key.lastIndexOf(".")));
                lastKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            }
            then.body(path, everyItem(not(hasKey(lastKey))));
        }
    }

    @Override
    protected void handleDisplayedParameter(ValidatableResponse then, List<String> displayed) throws Exception {
        then.statusCode(200);
        for(String key : displayed) {
            String path = "features.properties";
            String lastKey = key;
            if(key.contains(".")) {
                path += ("."+key.substring(0, key.lastIndexOf(".")));
                lastKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            }
            then.body(path, everyItem(hasKey(lastKey)));
        }
    }

    //----------------------------------------------------------------
    //----------------------- SORT PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        then.statusCode(200)
                .body("features[0].properties.params.job", equalTo(firstElement));
    }

    @Override
    protected void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        then.statusCode(200)
                .body("features[0].properties.geo_params.centroid", equalTo(firstElement));
    }

    @Override
    protected void handleInvalidGeoSortParameter(ValidatableResponse then) {
        then.statusCode(400);
    }

    //----------------------- XYZ TILES PART -------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleXYZWithoutFilters(ValidatableResponse then, String bottomLeft, String topRight) throws Exception {
        then.statusCode(200)
        .body("features.properties.geo_params.centroid", everyItem(greaterThanOrEqualTo(bottomLeft)))
        .body("features.properties.geo_params.centroid", everyItem(lessThanOrEqualTo(topRight)));
    }

    @Override
    protected void handleXYZWithPwithin(ValidatableResponse then, String bottomLeft, String topRight) throws Exception{
        handleXYZWithoutFilters(then, bottomLeft, topRight);
    }

    @Override
    protected void handleXYZDisjointFromPwithin(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features", equalTo(null));
    }

    @Override
    protected void handleInvalidXYZ(ValidatableResponse then) {
        then.statusCode(400);
    }
}
