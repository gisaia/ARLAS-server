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
        //an empty column filter is not considered
        search.projection.includes = "id,params,geo_params";
        handleDisplayedParameter(post(search, ""), Arrays.asList("params.country"));
        handleDisplayedParameter(get("include", search.projection.includes, ""), Arrays.asList("params.country"));
        handleHiddenParameter(post(search, ""), Arrays.asList("fullname"));
        handleHiddenParameter(get("include", search.projection.includes, ""), Arrays.asList("fullname"));

        //unexisting column should not return 404 nor 403 without column filter
        search.projection.includes = "id,params,unexisting";
        handleDisplayedParameter(post(search, ""), Arrays.asList("params.country"));
        handleDisplayedParameter(get("include", search.projection.includes, ""), Arrays.asList("params.country"));
        handleHiddenParameter(post(search, ""), Arrays.asList("fullname"));
        handleHiddenParameter(get("include", search.projection.includes, ""), Arrays.asList("fullname"));

        search.projection.includes = null;

        //an empty column filter is not considered in column filter
        search.projection.excludes = "fullname";
        handleDisplayedParameter(post(search, ""), Arrays.asList("params.country"));
        handleDisplayedParameter(get("exclude", search.projection.excludes, ""), Arrays.asList("params.country"));
        handleHiddenParameter(post(search, ""), Arrays.asList("fullname"));
        handleHiddenParameter(get("exclude", search.projection.excludes, ""), Arrays.asList("fullname"));

        search.projection.excludes = null;
    }

    @Test
    public void testIncludeExcludeFilterWithAvailableColumn() throws Exception {
        search.projection.includes = "id,params*";
        handleDisplayedParameter(post(search, "id,params.country"), Arrays.asList("params.country", "id", "params.startdate"));
        handleDisplayedParameter(get("include", search.projection.includes, "id,params.country"), Arrays.asList("params.country", "id", "params.startdate"));

        handleHiddenParameter(post(search, "id,params.country"), Arrays.asList("params.job,fullname"));
        handleHiddenParameter(get("include", search.projection.includes, "id,params.country"), Arrays.asList("params.job,fullname"));

        search.projection.includes = "id,geo_params,unexisting*";
        handleDisplayedParameter(post(search, "id, geo_params"), Arrays.asList("params.startdate"));
        handleDisplayedParameter(get("include", search.projection.includes, "id, geo_params"), Arrays.asList("params.startdate"));

        search.projection.includes = "param*";
        handleDisplayedParameter(post(search, "*params"), Arrays.asList("params.startdate", "params.age", "params.job", "params.country"));
        handleDisplayedParameter(get("include", search.projection.includes, "*params"), Arrays.asList("params.startdate", "params.age", "params.job", "params.country"));
        handleHiddenParameter(post(search, "*params"), Arrays.asList("fullname"));
        handleHiddenParameter(get("include", search.projection.includes, "*params"), Arrays.asList("fullname"));

        handleDisplayedParameter(post(search, "params"), Arrays.asList("params.startdate", "params.age", "params.job", "params.country"));
        handleDisplayedParameter(get("include", search.projection.includes, "params"), Arrays.asList("params.startdate", "params.age", "params.job", "params.country"));
        handleHiddenParameter(post(search, "params"), Arrays.asList("fullname"));
        handleHiddenParameter(get("include", search.projection.includes, "params"), Arrays.asList("fullname"));

        //unexisting column should not break if used in includes and filter, it is simply not returned
        search.projection.includes = "unexisting";
        handleHiddenParameter(post(search, "unexisting"), Arrays.asList("unexisting"));
        handleHiddenParameter(get("unexisting", search.projection.includes, "params"), Arrays.asList("unexisting"));

        //include without wildcard can be used if a filter matches
        search.projection.includes = "id,params";
        handleDisplayedParameter(post(search, "id,params"), Arrays.asList("params.country"));
        handleDisplayedParameter(get("include", search.projection.includes, "id,params"), Arrays.asList("params.country"));

        handleDisplayedParameter(post(search, "id,params*"), Arrays.asList("params.country"));
        handleDisplayedParameter(get("include", search.projection.includes, "id,params*"), Arrays.asList("params.country"));


        search.projection.excludes = "params.job,fullname";
        search.projection.includes = "geo_params.geometry";
        handleDisplayedParameter(post(search, "params.job,fullname,geo_params.geometry"), Arrays.asList("id", "params.startdate",  "geo_params.centroid"));
        handleDisplayedParameter(givenFilterableRequestParams().param("include", search.projection.includes)
                .param("exclude", search.projection.excludes)
                .header("column-filter", "params.job,fullname,geo_params.geometry")
                .when().get(getUrlPath("geodata"))
                .then(), Arrays.asList("id", "params.startdate",  "geo_params.centroid"));

        //exclude is not column filtered
        search.projection.includes = null;
        search.projection.excludes = "fullname";
        handleDisplayedParameter(post(search, "id"), Arrays.asList("id"));
        handleDisplayedParameter(get("exclude", search.projection.excludes, "id"), Arrays.asList("id"));
        handleHiddenParameter(post(search, "id"), Arrays.asList("fullname", "params.country"));
        handleHiddenParameter(get("exclude", search.projection.excludes, "id"), Arrays.asList("fullname", "params.country"));

        handleHiddenParameter(post(search, "id,fullname"), Arrays.asList("fullname", "params.country"));
        handleHiddenParameter(get("exclude", search.projection.excludes, "id,fullname"), Arrays.asList("fullname", "params.country"));

        search.projection.includes = null;
        search.projection.excludes = null;
    }

    @Test
    public void testIncludeExcludeFilterWithUnavailableColumn() throws Exception {
        search.projection.includes = "fullname";
        handleUnavailableColumn(post(search, "id"));
        handleUnavailableColumn(get("include", search.projection.includes, "id"));

        //column existence is not checked, column filter is applied as usually
        search.projection.includes = "id,geo_params,unexisting";
        handleUnavailableColumn(post(search, "id, geo_params"));
        handleUnavailableColumn(get("include", search.projection.includes, "id, geo_params"));

        //without wildcard, a root path cannnot be included if a filter doesn't match
        search.projection.includes = "id,params";
        handleUnavailableColumn(post(search, "id,params.country"));
        handleUnavailableColumn(get("include", search.projection.includes, "id,params.country"));

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

    private ValidatableResponse post(Request request, String columnFilter) {
        RequestSpecification req = givenFilterableRequestBody();
        return req.body(handlePostRequest(request))
                .header("column-filter", columnFilter)
                .when().post(getUrlPath("geodata"))
                .then();
    }
}
