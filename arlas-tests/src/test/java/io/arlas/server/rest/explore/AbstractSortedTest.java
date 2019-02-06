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

import cyclops.data.tuple.Tuple3;
import io.arlas.server.model.request.*;

import static org.hamcrest.Matchers.*;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractSortedTest extends AbstractProjectedTest {
    @Before
    public void setUpSearch() {
        search.size = new Size();
        search.filter = new Filter();
        search.sort = new Sort();
        search.projection = new Projection();
        search.filter = new Filter();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testSortParameter() throws Exception {
        search.sort.sort = "-params.job";
        handleSortParameter(post(search), "Dancer");
        handleSortParameter(get("sort", search.sort.sort), "Dancer");

        search.sort.sort = "geodistance:-50 -110";
        handleGeoSortParameter(post(search), "-50,-110");
        handleGeoSortParameter(get("sort", search.sort.sort), "-50,-110");

    }


    @Test
    public void testInvalidSizeParameters() throws Exception {
        search.sort.sort = "-50 -110";
        handleInvalidGeoSortParameter(post(search));
        handleInvalidGeoSortParameter(get("sort", search.sort.sort));
    }


    @Test
    public void testNoSortWithSearchAfter() throws Exception {
        search.sort.sort = null;
        search.sort.searchAfter = "foo,bar";
        handleInvalidSortParameterWithSearchAfter(post(search));
        handleInvalidSortParameterWithSearchAfter(get("search-after", search.sort.searchAfter));
        search.sort.searchAfter = null;
    }

    @Test
    public void testSortWrongSizeWithSearchAfter() throws Exception {
        search.sort.sort = "-params.job";
        search.sort.searchAfter = "Dancer,ID__170__20DI";
        handleInvalidSortParameterWithSearchAfter(post(search));
        handleInvalidSortParameterWithSearchAfter(get("sort", search.sort.sort, "search-after", search.sort.searchAfter));
        search.sort.searchAfter = null;
    }

    @Test
    public void testSortWrongLastElementWithSearchAfter() throws Exception {
        search.sort.sort = "-params.job,-params.age";
        search.sort.searchAfter = "Dancer,3400";
        handleInvalidSortParameterWithSearchAfter(post(search));
        handleInvalidSortParameterWithSearchAfter(get("sort", search.sort.sort, "search-after", search.sort.searchAfter));
        search.sort.searchAfter = null;
    }

    @Test
    public void testSortWithSearchAfterWrongLastValue() throws Exception {
        search.sort.sort = "-params.startdate,id";
        search.sort.searchAfter = "foo,ID__170__20DI";
        handleInvalidSortParameterWithSearchAfter(post(search));
        handleInvalidSortParameterWithSearchAfter(get("sort", search.sort.sort, "search-after", search.sort.searchAfter));
        search.sort.searchAfter = null;
    }


    @Test
    public void testPostSortWithSearchAfter() throws Exception {
        search.sort.sort = "params.startdate,id";
        search.size.size = 3;
        RequestSpecification req = givenFilterableRequestBody();
        ExtractableResponse response = req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        Tuple3 ids = getIdsAfterFirstSearch(response);
        String id_0 = ids._1().toString();
        String id_1 = ids._2().toString();
        String id_2 = ids._3().toString();
        Integer date_0 = getDateAfterFirstSearch(response);
        search.size.size = 2;
        search.sort.searchAfter = date_0.toString().concat(",").concat(id_0);
        handleSortAndSearchAfter(req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then(), id_1, id_2);
        search.sort.searchAfter = null;
    }

    @Test
    public void testGetSortWithSearchAfter() throws Exception {
        search.sort.sort = "params.startdate,id";
        search.size.size = 3;
        RequestSpecification req = givenFilterableRequestParams();

        ExtractableResponse response = req
                .param("sort", search.sort.sort)
                .param("size", search.size.size)
                .when().get(getUrlPath("geodata"))
                .then().extract();
        Tuple3 ids = getIdsAfterFirstSearch(response);
        String id_0 = ids._1().toString();
        String id_1 = ids._2().toString();
        String id_2 = ids._3().toString();
        Integer date_0 = getDateAfterFirstSearch(response);
        search.size.size = 2;
        search.sort.searchAfter = date_0.toString().concat(",").concat(id_0);

        handleSortAndSearchAfter(req.param("sort", search.sort.sort)
                .param("size", search.size.size)
                .param("search-after", search.sort.searchAfter)
                .when().get(getUrlPath("geodata"))
                .then(), id_1, id_2);

        search.sort.searchAfter = null;
    }


    protected abstract void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception;

    protected abstract void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception;

    protected abstract Integer getDateAfterFirstSearch(ExtractableResponse response) throws Exception;

    protected abstract Tuple3 getIdsAfterFirstSearch(ExtractableResponse response) throws Exception;

    protected abstract void handleSortAndSearchAfter(ValidatableResponse then, String id1, String id2) throws Exception;

    protected abstract void handleInvalidGeoSortParameter(ValidatableResponse then) throws Exception;

    protected abstract void handleInvalidSortParameterWithSearchAfter(ValidatableResponse then) throws Exception;


    private ValidatableResponse post(Request request) {
        RequestSpecification req = givenFilterableRequestBody();
        return req.body(handlePostRequest(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }

    protected ValidatableResponse get(String param1, Object paramValue1, String param2, Object paramValue2) {
        return givenFilterableRequestBody().param(param1, paramValue1).param(param2, paramValue2)
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
