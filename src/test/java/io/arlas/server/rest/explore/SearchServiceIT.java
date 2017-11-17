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
import static org.hamcrest.Matchers.hasKey;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class SearchServiceIT extends AbstractSortedTest {
    
    @Override
    public String getUrlPath(String collection) {
        return arlasPrefix + "explore/"+collection+"/_search";
    }
    
    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
        .body("totalnb", equalTo(0));
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
        .body("totalnb", equalTo(1))
        .body("hits[0].data.params.job", equalTo("Architect"))
        .body("hits[0].data.params.startdate", equalTo(1009800))
        .body("hits[0].data.geo_params.centroid", equalTo("20,-10"))
        .body("hits[0].md.timestamp",equalTo(1009800));
    }

    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(59))
        .body("hits.data.params.job", everyItem(equalTo("Actor")));
    }

    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(117))
        .body("hits.data.params.job",  everyItem(isOneOf("Actor","Announcers")));
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(59))
        .body("hits.data.params.job",  everyItem(equalTo("Actor")));
    }

    //TODO : fix the case where the field is full text
    /*@Override
    protected void handleKnownFullTextFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(595))
        .body("hits.data.job", everyItem(isOneOf("Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }*/

    @Override
    protected void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(478))
        .body("hits.data.params.job", everyItem(isOneOf("Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }
    
    //----------------------------------------------------------------
    //----------------------- TEXT QUERY -----------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(595));
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int size) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(size))
        .body("hits.data.params.startdate", everyItem(greaterThan(start)))
        .body("hits.data.params.startdate", everyItem(lessThan(end)));
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int size) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(size))
        .body("hits.data.params.job", everyItem(greaterThan(start)))
        .body("hits.data.params.job", everyItem(lessThan(end)));
    }

    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then, String centroid) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo(centroid)));
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(17))
        .body("hits.data.geo_params.centroid", everyItem(endsWith("170")));
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(8))
        .body("hits.data.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }

    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(4))
        .body("hits.data.geo_params.centroid", hasItems("-70,170","-80,170","-70,160","-80,160"));
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(8))
        .body("hits.data.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }

    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("-80,170")));
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.geo_params.centroid", hasItems("10,-10","0,-10","-10,-10"));
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
    protected void handleSizeParameter(ValidatableResponse then, int size) throws Exception {
        if(size > 0) {
            then.statusCode(200)
                .body("nbhits", equalTo(size))
                .body("hits.size()", equalTo(size));
        } else {
            then.statusCode(200)
            .body("nbhits", equalTo(size));
        }
    }

    //----------------------------------------------------------------
    //----------------------- PROJECTION PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleHiddenParameter(ValidatableResponse then, List<String> hidden) throws Exception {
        then.statusCode(200);
        for(String key : hidden) {
            String path = "hits.data";
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
            String path = "hits.data";
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
                .body("hits[0].data.params.job", equalTo(firstElement));
    }

    @Override
    protected void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        then.statusCode(200)
                .body("hits[0].data.geo_params.centroid", equalTo(firstElement));
    }

    @Override
    protected void handleInvalidGeoSortParameter(ValidatableResponse then) {
        then.statusCode(400);
    }
}
