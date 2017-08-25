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

import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractSizedTest extends AbstractFilteredTest {
    protected static Search search = new Search();

    @Before
    public void setUpSearch(){
        search.size = new Size();
        search.filter = new Filter();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testSizeFilter() throws Exception {
        handleSizeParameter(post(search), 10);
        handleSizeParameter(
                givenBigSizedRequestParams()
                        .when().get(getUrlPath("geodata"))
                        .then(), 10);

        search.size.size = 40;
        handleSizeParameter(post(search), 40);
        handleSizeParameter(get("size",search.size.size), 40);

        search.size.size = Integer.valueOf(getBigSizedResponseSize());
        handleSizeParameter(post(search), getBigSizedResponseSize());
        handleSizeParameter(get("size",search.size.size), getBigSizedResponseSize());

        search.size.size = Integer.valueOf(getBigSizedResponseSize())+5;
        handleSizeParameter(post(search), getBigSizedResponseSize());
        handleSizeParameter(get("size",search.size.size), getBigSizedResponseSize());
        search.size.size = null;
    }
    
    @Test
    public void testFromFilter() throws Exception {
        handleSizeParameter(post(search), 10);
        handleSizeParameter(
                givenBigSizedRequestParams()
                        .when().get(getUrlPath("geodata"))
                        .then(), 10);

        search.size.from = Integer.valueOf(getBigSizedResponseSize()-5);
        handleSizeParameter(post(search), 5);
        handleSizeParameter(get("from",search.size.from), 5);

        search.size.from = Integer.valueOf(getBigSizedResponseSize()+5);
        handleSizeParameter(post(search), 0);
        handleSizeParameter(get("from",search.size.from), 0);
        search.size.from = null;

    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    
    @Test
    public void testInvalidSizeParameters() throws Exception {
        //SIZE
        search.size.size = Integer.valueOf(0);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("size",search.size.size));

        search.size.size = Integer.valueOf(-10);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("size",search.size.size));
        search.size.size = null;

        handleInvalidParameters(
                givenBigSizedRequestParams().param("size", "foo")
                        .when().get(getUrlPath("geodata"))
                        .then());

        //FROM
        search.size.from = Integer.valueOf(-10);
        handleInvalidParameters(post(search));
        handleInvalidParameters(get("from",search.size.from));
        search.size.from = null;

        handleInvalidParameters(
                givenBigSizedRequestParams().param("from", "foo")
                        .when().get(getUrlPath("geodata"))
                        .then());
    }

    protected abstract RequestSpecification givenBigSizedRequestParams();
    protected abstract RequestSpecification givenBigSizedRequestParamsPost();
    protected abstract int getBigSizedResponseSize();
    protected abstract void handleSizeParameter(ValidatableResponse then, int size) throws Exception;

    //----------------------------------------------------------------
    //---------------------- ValidatableResponse ------------------
    //----------------------------------------------------------------

    protected ValidatableResponse post(Request request){
        return givenBigSizedRequestParamsPost().body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    protected ValidatableResponse get(String param,Object paramValue){
        return givenBigSizedRequestParams().param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
