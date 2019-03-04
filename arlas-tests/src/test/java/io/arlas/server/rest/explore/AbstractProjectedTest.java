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

import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Projection;
import io.arlas.server.model.request.Request;
import io.arlas.server.model.request.Page;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractProjectedTest extends AbstractPaginatedTest {

    @Before
    public void setUpSearch() {
        search.page = new Page();
        search.filter = new Filter();
        search.projection = new Projection();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testIncludeExcludeFilter() throws Exception {
        search.projection.includes = "id,params,geo_params";
        handleHiddenParameter(post(search), Arrays.asList("fullname"));
        handleHiddenParameter(get("include", search.projection.includes), Arrays.asList("fullname"));

        search.projection.includes = "id,geo_params";
        handleDisplayedParameter(post(search), Arrays.asList("params.startdate"));
        handleDisplayedParameter(get("include", search.projection.includes), Arrays.asList("params.startdate"));

        search.projection.includes = null;

        search.projection.excludes = "fullname";
        handleHiddenParameter(post(search), Arrays.asList("fullname"));
        handleHiddenParameter(get("exclude", search.projection.excludes), Arrays.asList("fullname"));

        search.projection.excludes = null;

        search.projection.excludes = "params.job,fullname";
        search.projection.includes = "geo_params.geometry";
        handleDisplayedParameter(post(search), Arrays.asList("id", "params.startdate",  "geo_params.centroid"));
        handleDisplayedParameter(givenFilterableRequestParams().param("include", search.projection.includes)
                .param("exclude", search.projection.excludes)
                .when().get(getUrlPath("geodata"))
                .then(), Arrays.asList("id", "params.startdate",  "geo_params.centroid"));

        search.projection.includes = null;
        search.projection.excludes = null;
    }

    protected abstract void handleHiddenParameter(ValidatableResponse then, List<String> hidden) throws Exception;

    protected abstract void handleDisplayedParameter(ValidatableResponse then, List<String> displayed) throws Exception;

    private ValidatableResponse post(Request request) {
        RequestSpecification req = givenFilterableRequestBody();
        return req.body(handlePostRequest(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }
}
