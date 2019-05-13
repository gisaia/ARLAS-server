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
import cyclops.data.tuple.Tuple3;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Request;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.request.Page;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URLDecoder;
import java.util.HashMap;

public abstract class AbstractPaginatedTest extends AbstractFormattedTest{
    protected static Search search = new Search();

    @Before
    public void setUpSearch() {
        search.page = new Page();
        search.filter = new Filter();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testPageSize() throws Exception {
        handleSizeParameter(post(search), 10);
        handleSizeParameter(
                givenBigSizedRequestParams()
                        .when().get(getUrlPath("geodata"))
                        .then(), 10);

        search.page.size = 40;
        handleSizeParameter(post(search), 40);
        handleSizeParameter(get("size", search.page.size), 40);

        search.page.size = Integer.valueOf(getBigSizedResponseSize());
        handleSizeParameter(post(search), getBigSizedResponseSize());
        handleSizeParameter(get("size", search.page.size), getBigSizedResponseSize());

        search.page.size = Integer.valueOf(getBigSizedResponseSize()) + 5;
        handleSizeParameter(post(search), getBigSizedResponseSize());
        handleSizeParameter(get("size", search.page.size), getBigSizedResponseSize());
        search.page.size = null;
    }

    @Test
    public void testPageFrom() throws Exception {
        handleSizeParameter(post(search), 10);
        handleSizeParameter(
                givenBigSizedRequestParams()
                        .when().get(getUrlPath("geodata"))
                        .then(), 10);

        search.page.from = Integer.valueOf(getBigSizedResponseSize() - 5);
        handleSizeParameter(post(search), 5);
        handleSizeParameter(get("from", search.page.from), 5);

        search.page.from = Integer.valueOf(getBigSizedResponseSize() + 5);
        handleSizeParameter(post(search), 0);
        handleSizeParameter(get("from", search.page.from), 0);
        search.page.from = null;

    }

    @Test
    public void testPageSort() throws Exception {
        search.page.sort = "-params.job";
        handleSortParameter(post(search), "Dancer");
        handleSortParameter(get("sort", search.page.sort), "Dancer");

        search.page.sort = "geodistance:-50 -110";
        handleGeoSortParameter(post(search), "-50,-110");
        handleGeoSortParameter(get("sort", search.page.sort), "-50,-110");

    }

    @Test
    public void testPOSTSortWithAfterParameters() throws Exception {
        search.page.sort = "params.startdate,id";
        search.page.size = 3;
        RequestSpecification req = givenFilterableRequestBody();
        ExtractableResponse response = req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        Tuple3 ids = getIdsAfterFirstSearch(response);
        String id_0 = ids._1().toString();
        String id_1 = ids._2().toString();
        String id_2 = ids._3().toString();
        Integer date_0 = getDateAfterFirstSearch(response);
        search.page.size = 2;
        search.page.after = date_0.toString().concat(",").concat(id_0);
        handleSortAndAfterParameters(req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then(), id_1, id_2);
        search.page.after = null;
    }

    @Test
    public void testPOSTLinkSearchAfter() throws Exception {
        search.page.sort = "params.startdate,id";
        search.page.size = 3;
        RequestSpecification req0 = givenFilterableRequestBody();
        ExtractableResponse response0 = req0.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        String id_0 = response0.path("hits[0].data.id");
        Integer date_0 = response0.path("hits[0].data.params.startdate");
        String id_2 = response0.path("hits[2].data.id");
        search.page.sort = "params.startdate,id";
        search.page.size = 1;
        search.page.after = date_0.toString().concat(",").concat(id_0);
        RequestSpecification req = givenFilterableRequestBody();
        ExtractableResponse response = req.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        HashMap data = response.path("links.next.body");
        String href = response.path("links.next.href");
        ObjectMapper objectMapper = new ObjectMapper();
        Search searchFromLink = objectMapper.convertValue(data,Search.class);
        ExtractableResponse secondResponse = req.body(handlePostRequest(searchFromLink))
                .when().post(href)
                .then()
                .body("hits[0].data.id", equalTo(id_2))
                .body("links.next",notNullValue())
                .body("links.self",notNullValue())
                .statusCode(200).extract();
        /** WE FETCH NOW THE LAST PAGE. 'link.next' SHOULD BE NULL */
        HashMap lastRequestBody = response.path("links.next.body");
        String lastHref = response.path("links.next.href");
        Search lastSearchFromLink = objectMapper.convertValue(lastRequestBody,Search.class);
        lastSearchFromLink.page.size = 595;
        givenFilterableRequestBody().body(handlePostRequest(lastSearchFromLink))
                .when().post(lastHref)
                .then()
                .body("nbhits", lessThan(lastSearchFromLink.page.size))
                .body("links.next",nullValue())
                .body("links.self",notNullValue())
                .statusCode(200);
    }

    @Test
    public void testGETSortWithAfterParameters() throws Exception {
        search.page.sort = "params.startdate,id";
        search.page.size = 3;
        RequestSpecification req = givenFilterableRequestParams();
        ExtractableResponse response = req
                .param("sort", search.page.sort)
                .param("size", search.page.size)
                .when().get(getUrlPath("geodata"))
                .then().extract();
        Tuple3 ids = getIdsAfterFirstSearch(response);
        String id_0 = ids._1().toString();
        String id_1 = ids._2().toString();
        String id_2 = ids._3().toString();
        Integer date_0 = getDateAfterFirstSearch(response);
        search.page.size = 2;
        search.page.after = date_0.toString().concat(",").concat(id_0);

        handleSortAndAfterParameters(req.param("sort", search.page.sort)
                .param("size", search.page.size)
                .param("after", search.page.after)
                .when().get(getUrlPath("geodata"))
                .then(), id_1, id_2);

        search.page.after = null;
    }

    @Test
    public void testGETLinkSearchAfter() throws Exception {
        search.page.sort = "params.startdate,id";
        search.page.size = 3;
        RequestSpecification req0 = givenFilterableRequestBody();

        ExtractableResponse response0 = req0
                .param("sort", search.page.sort)
                .param("size", search.page.size)
                .when().get(getUrlPath("geodata"))
                .then().extract();

        String id_0 = response0.path("hits[0].data.id");
        Integer date_0 = response0.path("hits[0].data.params.startdate");

        String id_2 = response0.path("hits[2].data.id");
        search.page.size = 1;
        search.page.after = date_0.toString().concat(",").concat(id_0);
        ExtractableResponse response = givenFilterableRequestBody()
                .param("sort", search.page.sort)
                .param("size", search.page.size)
                .param("after", search.page.after)
                .when().get(getUrlPath("geodata"))
                .then().extract();
        String href = response.path("links.next.href");
        ExtractableResponse secondResponse  = givenFilterableRequestBody().urlEncodingEnabled(true)
                .when().get(URLDecoder.decode(href,"UTF-8"))
                .then()
                .body("hits[0].data.id", equalTo(id_2))
                .body("links.next",notNullValue())
                .statusCode(200).extract();
        /** WE FETCH NOW THE LAST PAGE. 'link.next' SHOULD BE NULL */
        String lastId = secondResponse.path("hits[0].data.id");
        String lastDate = secondResponse.path("hits[0].data.params.startdate").toString();
        search.page.size = 595;
        givenFilterableRequestBody().param("sort", search.page.sort)
                .param("size", search.page.size)
                .param("after", lastDate.concat(",").concat(lastId))
                .when().get(getUrlPath("geodata"))
                .then()
                .body("nbhits", lessThan(search.page.size))
                .body("links.next",nullValue())
                .body("links.self",notNullValue())
                .statusCode(200);
    }



    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------

    @Test
    public void testInvalidPageParameters() throws Exception {
        //SIZE
        search.page.size = Integer.valueOf(0);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("size", search.page.size));

        search.page.size = Integer.valueOf(-10);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("size", search.page.size));
        search.page.size = null;

        handleInvalidParameters(
                givenBigSizedRequestParams().param("size", "foo")
                        .when().get(getUrlPath("geodata"))
                        .then());

        //FROM
        search.page.from = Integer.valueOf(-10);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("from", search.page.from));
        search.page.from = null;

        handleInvalidParameters(
                givenBigSizedRequestParams().param("from", "foo")
                        .when().get(getUrlPath("geodata"))
                        .then());

        // SORT
        search.page.sort = "-50 -110";
        handleInvalidGeoSortParameter(post(search));
        handleInvalidGeoSortParameter(get("sort", search.page.sort));

        // SORT CANNOT BE GEODISTANCE IF after IS SET
        search.page.sort = "params.startdate,id";
        search.page.size = 3;
        RequestSpecification reqPost = givenFilterableRequestBody();
        ExtractableResponse responsePost = reqPost.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        Tuple3 ids = getIdsAfterFirstSearch(responsePost);
        String id = ids._1().toString();
        Integer date = getDateAfterFirstSearch(responsePost);
        search.page.after = date.toString().concat(",").concat(id);
        search.page.sort = "geodistance:-50 -110";
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("sort", search.page.sort, "after", search.page.after));
        search.page.sort = null;

        // FROM CANNOT BE > 0 IF SEARCHAFTER IS SET
        search.page.sort = "params.startdate,id";
        search.page.size = 3;
        RequestSpecification reqGet = givenFilterableRequestBody();
        ExtractableResponse responseGet = reqGet.body(handlePostRequest(search))
                .when().post(getUrlPath("geodata"))
                .then().extract();
        String id1 = ids._1().toString();
        Integer date1 = getDateAfterFirstSearch(responseGet);
        search.page.after = date1.toString().concat(",").concat(id1);
        search.page.from = Integer.valueOf(10);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("from", search.page.from, "after", search.page.after));
    }

    @Test
    public void testNoSortWithSearchAfter() throws Exception {
        search.page.sort = null;
        search.page.after = "foo,bar";
        handleInvalidSortWithAfterParameters(post(search));
        handleInvalidSortWithAfterParameters(get("after", search.page.after));
        search.page.after = null;
    }

    @Test
    public void testSortWrongSizeWithSearchAfter() throws Exception {
        search.page.sort = "-params.job";
        search.page.after = "Dancer,ID__170__20DI";
        handleInvalidSortWithAfterParameters(post(search));
        handleInvalidSortWithAfterParameters(get("sort", search.page.sort, "after", search.page.after));
        search.page.after = null;
    }

    @Test
    public void testSortWrongLastElementWithSearchAfter() throws Exception {
        search.page.sort = "-params.job,-params.age";
        search.page.after = "Dancer,3400";
        handleInvalidSortWithAfterParameters(post(search));
        handleInvalidSortWithAfterParameters(get("sort", search.page.sort, "after", search.page.after));
        search.page.after = null;
    }

    @Test
    public void testSortWithSearchAfterWrongLastValue() throws Exception {
        search.page.sort = "-params.startdate,id";
        search.page.after = "foo,ID__170__20DI";
        handleInvalidSortWithAfterParameters(post(search));
        handleInvalidSortWithAfterParameters(get("sort", search.page.sort, "after", search.page.after));
        search.page.after = null;
    }



    protected abstract RequestSpecification givenBigSizedRequestParams();

    protected abstract RequestSpecification givenBigSizedRequestParamsPost();

    protected abstract int getBigSizedResponseSize();

    protected abstract Integer getDateAfterFirstSearch(ExtractableResponse response) throws Exception;

    protected abstract Tuple3 getIdsAfterFirstSearch(ExtractableResponse response) throws Exception;

    protected abstract void handleSortAndAfterParameters(ValidatableResponse then, String id1, String id2) throws Exception;

    protected abstract void handleSizeParameter(ValidatableResponse then, int size) throws Exception;

    protected abstract void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception;

    protected abstract void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception;

    protected abstract void handleInvalidGeoSortParameter(ValidatableResponse then) throws Exception;

    protected abstract void handleInvalidSortWithAfterParameters(ValidatableResponse then) throws Exception;



    //----------------------------------------------------------------
    //---------------------- ValidatableResponse ------------------
    //----------------------------------------------------------------

    protected ValidatableResponse get(String param, Object paramValue) {
        return givenFilterableRequestParams().param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    protected ValidatableResponse get(String param1, Object paramValue1, String param2, Object paramValue2) {
        return givenFilterableRequestBody().param(param1, paramValue1).param(param2, paramValue2)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse post(Request request) {
        RequestSpecification req = givenBigSizedRequestParamsPost();
        return req.body(handlePostRequest(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }
    protected Request handlePostRequest(Request req){return req;}

}
