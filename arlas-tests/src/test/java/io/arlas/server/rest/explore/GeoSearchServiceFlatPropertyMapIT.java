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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.arlas.server.model.request.Form;
import io.arlas.server.model.request.Request;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GeoSearchServiceFlatPropertyMapIT extends GeoSearchServiceIT {
    public GeoSearchServiceFlatPropertyMapIT() {
        extraParams.add(Pair.of("flat","true"));
    }

    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given().param("flat",true);
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("features[0].properties.params_job", equalTo("Architect"))
                .body("features[0].properties.params_startdate", equalTo(1009800))
                .body("features[0].properties.geo_params_centroid", equalTo("20,-10"));
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception {
        then.statusCode(200)
                .body("features.properties.params_job", everyItem(isOneOf(values)))
                .body("features.properties.feature_type", everyItem(equalTo("hit")));
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int size) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(size))
                .body("features.properties.params_startdate", everyItem(greaterThan(start)))
                .body("features.properties.params_startdate", everyItem(lessThan(end)));
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int size) throws Exception {
        then.statusCode(200)
                .body("features.properties.params_job", everyItem(greaterThan(start)))
                .body("features.properties.params_job", everyItem(lessThan(end)));
    }

    @Override
    protected void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception {
        then.statusCode(200)
                .body("features.size()", equalTo(Math.min(nbResults, 10)))
                .body("features.properties.geo_params_centroid", centroidMatcher);
    }


    //----------------------------------------------------------------
    //----------------------- PROJECTION PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleHiddenParameter(ValidatableResponse then, List<String> hidden) throws Exception {
        then.statusCode(200);
        ObjectWriter writter = new ObjectMapper().writer();
        for (String key : hidden) {
            String path = "features.properties";
            String lastKey = lastKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            Assert.assertThat(writter.writeValueAsString(then.extract().jsonPath().get(path)), not(containsString(lastKey)));
        }
    }

    @Override
    protected void handleDisplayedParameter(ValidatableResponse then, List<String> displayed) throws Exception {
        then.statusCode(200);
        ObjectWriter writter = new ObjectMapper().writer();
        for (String key : displayed) {
            String path = "features.properties";
            String lastKey = lastKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            Assert.assertThat(writter.writeValueAsString(then.extract().jsonPath().get(path)), containsString(lastKey));
        }
    }

    //----------------------------------------------------------------
    //----------------------- SORT PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        then.statusCode(200)
                .body("features[0].properties.params_job", equalTo(firstElement));
    }

    @Override
    protected void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        then.statusCode(200)
                .body("features[0].properties.geo_params_centroid", equalTo(firstElement));
    }

    //----------------------- XYZ TILES PART -------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleXYZ(ValidatableResponse then, String bottomLeft, String topRight) throws Exception {
        then.statusCode(200)
                .body("features.properties.geo_params_centroid", everyItem(greaterThanOrEqualTo(bottomLeft)))
                .body("features.properties.geo_params_centroid", everyItem(lessThanOrEqualTo(topRight)));
    }

    @Override
    protected ValidatableResponse get(String param, Object paramValue) {
        return givenBigSizedRequestParams().param(param, paramValue).param("flat", "true")
                .when().get(getUrlPath("geodata"))
                .then();
    }

    @Override
    protected Request handlePostRequest(Request req){
        if (req.form==null)req.form=new Form();
        req.form.flat=true;
        return req;
    }


    @Override
    public void testPostSortWithSearchAfter() throws Exception {
        search.sort.sort = "params.startdate,id";
        search.size.size=3;
        RequestSpecification req = givenFilterableRequestBody();
        ExtractableResponse response = req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        String id_0 = response.path("features[0].properties.id");
        Integer date_0 = response.path("features[0].properties.params_startdate");
        String id_1 = response.path("features[1].properties.id");
        String id_2 = response.path("features[2].properties.id");
        search.size.size=2;
        search.sort.searchAfter= date_0.toString().concat(",").concat(id_0);
        req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then()
                .body("features[0].properties.id",equalTo(id_1))
                .body("features[1].properties.id",equalTo(id_2))
                .statusCode(200);
        search.sort.searchAfter=null;
    }

    @Override
    public void testGetSortWithSearchAfter() throws Exception {
        search.sort.sort = "params.startdate,id";
        search.size.size = 3;
        RequestSpecification req = givenFilterableRequestBody();

        ExtractableResponse response = req
                .param("sort", search.sort.sort)
                .param("size", search.size.size)
                .param("flat", "true")
                .when().get(getUrlPath("geodata"))
                .then().extract();

        String id_0 = response.path("features[0].properties.id");
        Integer date_0 = response.path("features[0].properties.params_startdate");
        String id_1 = response.path("features[1].properties.id");
        String id_2 = response.path("features[2].properties.id");
        search.size.size = 2;
        search.sort.searchAfter = date_0.toString().concat(",").concat(id_0);

        req.param("sort", search.sort.sort)
                .param("size", search.size.size)
                .param("search-after", search.sort.searchAfter)
                .when().get(getUrlPath("geodata"))
                .then()
                .body("features[0].properties.id", equalTo(id_1))
                .body("features[1].properties.id", equalTo(id_2))
                .statusCode(200);
        search.sort.searchAfter = null;
    }
}
