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

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.OperatorEnum;
import org.junit.Before;
import org.junit.Test;

import io.arlas.server.AbstractTestWithCollection;
import io.arlas.server.DataSetTool;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Request;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractFilteredTest extends AbstractTestWithCollection {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUpFilter(){
        request = new Request();
        request.filter = new Filter();
    }
    
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testFieldFilter() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0]));//("job:eq:" + DataSetTool.jobs[0]);
        handleKnownFieldFilter(post(request));
        handleKnownFieldFilter(get("f", request.filter.f.get(0).toString()));
        handleKnownFieldFilter(header(request.filter));

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]));//"job:eq:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleKnownFieldFilterWithOr(post(request));
        handleKnownFieldFilterWithOr(get("f", request.filter.f.get(0).toString()));
        handleKnownFieldFilterWithOr(header(request.filter));

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.like, "cto"));//"job:like:" + "cto");
        handleKnownFieldLikeFilter(post(request));
        handleKnownFieldLikeFilter(get("f", request.filter.f.get(0).toString()));
        handleKnownFieldLikeFilter(header(request.filter));

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.ne, DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]));//"job:ne:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleKnownFieldFilterNotEqual(post(request));
        handleKnownFieldFilterNotEqual(get("f", request.filter.f.get(0).toString()));
        handleKnownFieldFilterNotEqual(header(request.filter));
        //TODO : fix the case where the field is full text
        /*handleKnownFullTextFieldLikeFilter(
                givenFilterableRequestParams().param("f", "fullname:like:" + "name is")
                        .when().get(getUrlPath("geodata"))
                        .then());*/
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "UnknownJob"));//"job:eq:UnknownJob");
        handleUnknownFieldFilter(post(request));
        handleUnknownFieldFilter(get("f", request.filter.f.get(0).toString()));
        handleUnknownFieldFilter(header(request.filter));
        request.filter.f = null;

    }
    
    @Test
    public void testQueryFilter() throws Exception {

        request.filter.q = "My name is";
        handleMatchingQueryFilter(post(request));
        handleMatchingQueryFilter(get("q", request.filter.q));
        handleMatchingQueryFilter(header(request.filter));

        request.filter.q = "fullname:My name is";
        handleMatchingQueryFilter(post(request));
        handleMatchingQueryFilter(get("q", request.filter.q));
        handleMatchingQueryFilter(header(request.filter));

        request.filter.q = "UnknownQuery";
        handleNotMatchingQueryFilter(post(request));
        handleNotMatchingQueryFilter(get("q", request.filter.q));
        handleNotMatchingQueryFilter(header(request.filter));

        request.filter.q = null;
    }

    @Test
    public void testRangeFilter() throws Exception {

        // TIMESTAMP RANGE

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[0;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        //ALIAS
        request.filter.f = Arrays.asList(new Expression("$timestamp", OperatorEnum.range, "[0;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        request.filter.f = Arrays.asList(new Expression("$timestamp", OperatorEnum.range, "[770000;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 770000, 775000, 2);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 770000, 775000, 2);
        handleMatchingTimestampRangeFilter(header(request.filter), 770000, 775000, 2);

        //MULTIRANGE
        request.filter.f = Arrays.asList(new Expression("$timestamp", OperatorEnum.range, "[0;765000],[770000;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[1270000;1283600]"));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[765000;770000],[1270000;1283600]"));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        // TEXT RANGE
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[Ac;An]"));
        handleMatchingStringRangeFilter(post(request), "Ac", "An", 59);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).toString()), "Ac", "An", 59);
        handleMatchingStringRangeFilter(header(request.filter), "Ac", "An", 59);

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[Coa;Cod]"));
        handleMatchingStringRangeFilter(post(request), "Coa", "Cod", 58);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).toString()), "Coa", "Cod", 58);
        handleMatchingStringRangeFilter(header(request.filter), "Coa", "Cod", 58);

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[Coa;Cod],[Ac;An]"));
        handleMatchingStringRangeFilter(post(request), "Ac", "Cod", 117);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).toString()), "Ac", "Cod", 117);
        handleMatchingStringRangeFilter(header(request.filter), "Ac", "Cod", 117);

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[X;Z]"));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = null;
    }
    
    @Test
    public void testPwithinFilter() throws Exception {
        request.filter.pwithin = "5,-5,-5,5";
        handleMatchingPwithinFilter(post(request), "0,0");
        handleMatchingPwithinFilter(get("pwithin",request.filter.pwithin), "0,0");
        handleMatchingPwithinFilter(header(request.filter), "0,0");

        request.filter.pwithin = "5,180,0,-165";
        handleMatchingPwithinFilter(post(request), "0,-170");
        handleMatchingPwithinFilter(get("pwithin",request.filter.pwithin), "0,-170");
        handleMatchingPwithinFilter(header(request.filter), "0,-170");

        request.filter.pwithin = "90,175,85,180";
        handleNotMatchingPwithinFilter(post(request));
        handleNotMatchingPwithinFilter(get("pwithin",request.filter.pwithin));
        handleNotMatchingPwithinFilter(header(request.filter));

        request.filter.pwithin = null;
        request.filter.notpwithin = "85,-170,-85,175";
        handleMatchingNotPwithinFilter(post(request));
        handleMatchingNotPwithinFilter(get("notpwithin",request.filter.notpwithin));
        handleMatchingNotPwithinFilter(header(request.filter));

        request.filter.notpwithin = "85,-175,-85,175";
        handleNotMatchingNotPwithinFilter(post(request));
        handleNotMatchingNotPwithinFilter(get("notpwithin",request.filter.notpwithin));
        handleNotMatchingNotPwithinFilter(header(request.filter));

        //TODO support correct 10,-10,-10,10 bounding box
        request.filter.pwithin = "11,-11,-11,11";
        request.filter.notpwithin = "5,-5,-5,5";
        handleMatchingPwithinComboFilter(post(request));
        handleMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin)
                    .param("notpwithin", request.filter.notpwithin)
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingPwithinComboFilter(header(request.filter));

        request.filter.pwithin = "6,-6,-6,6";
        request.filter.notpwithin = "5,-5,-5,5";
        handleNotMatchingPwithinComboFilter(post(request));
        handleNotMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin)
                    .param("notpwithin", request.filter.notpwithin)
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingPwithinComboFilter(header(request.filter));

        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }
    
    @Test
    public void testGwithinFilter() throws Exception {
        request.filter.gwithin = "POLYGON((2 2,2 -2,-2 -2,-2 2,2 2))";
        handleMatchingGwithinFilter(post(request));
        handleMatchingGwithinFilter(get("gwithin",request.filter.gwithin));
        handleMatchingGwithinFilter(header(request.filter));

        request.filter.gwithin = "POLYGON((1 1,2 1,2 2,1 2,1 1))";
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("gwithin",request.filter.gwithin));
        handleNotMatchingGwithinFilter(header(request.filter));
        request.filter.gwithin = null;

        request.filter.notgwithin = "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))";
        handleMatchingNotGwithinFilter(post(request));
        handleMatchingNotGwithinFilter(get("notgwithin",request.filter.notgwithin));
        handleMatchingNotGwithinFilter(header(request.filter));

        request.filter.notgwithin = "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))";
        handleNotMatchingNotGwithinFilter(post(request));
        handleNotMatchingNotGwithinFilter(get("notgwithin",request.filter.notgwithin));
        handleNotMatchingNotGwithinFilter(header(request.filter));

        request.filter.gwithin = "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))";
        request.filter.notgwithin = "POLYGON((8 8,8 -8,-8 -8,-8 8,8 8))";
        handleMatchingGwithinComboFilter(post(request));
        handleMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin)
                    .param("notgwithin", request.filter.notgwithin)
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingGwithinComboFilter(header(request.filter));

        request.filter.gwithin = "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))";
        request.filter.notgwithin = "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))";
        handleNotMatchingGwithinComboFilter(post(request));
        handleNotMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin)
                .param("notgwithin", request.filter.notgwithin)
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGwithinComboFilter(header(request.filter));
        request.filter.gwithin = null;
        request.filter.notgwithin = null;
    }
    
    @Test
    public void testGintersectFilter() throws Exception {
        request.filter.gintersect = "POLYGON((0 1,1 1,1 -1,0 -1,0 1))";
        handleMatchingGintersectFilter(post(request));
        handleMatchingGintersectFilter(get("gintersect",request.filter.gintersect));
        handleMatchingGintersectFilter(header(request.filter));

        request.filter.gintersect = "POLYGON((2 2,3 2,3 3,2 3,2 2))";
        handleNotMatchingGintersectFilter(post(request));
        handleNotMatchingGintersectFilter(get("gintersect", request.filter.gintersect));
        handleNotMatchingGintersectFilter(header(request.filter));
        request.filter.gintersect = null;

        request.filter.notgintersect = "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))";
        handleMatchingNotGintersectFilter(post(request));
        handleMatchingNotGintersectFilter(get("notgintersect", request.filter.notgintersect));
        handleMatchingNotGintersectFilter(header(request.filter));

        request.filter.notgintersect = "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))";
        handleNotMatchingNotGintersectFilter(post(request));
        handleNotMatchingNotGintersectFilter(get("notgintersect", request.filter.notgintersect));
        handleNotMatchingNotGintersectFilter(header(request.filter));

        request.filter.gintersect = "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))";
        request.filter.notgintersect = "POLYGON((10 10,10 -10,0 -10,0 10,10 10))";
        handleMatchingGintersectComboFilter(post(request));
        handleMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect)
                    .param("notgintersect", request.filter.notgintersect)
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingGintersectComboFilter(header(request.filter));

        request.filter.gintersect = "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))";
        request.filter.notgintersect = "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))";
        handleNotMatchingGintersectComboFilter(post(request));
        handleNotMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect)
                .param("notgintersect", request.filter.notgintersect)
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGintersectComboFilter(header(request.filter));
        request.filter.gintersect = null;
        request.filter.notgintersect = null;

    }
    
    @Test
    public void testComplexFilter() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.like, "Architect"),//"job:eq:Architect"
                new Expression("params.startdate", OperatorEnum.range, "[1009799;1009801]"));
        request.filter.pwithin = "50,-50,-50,50";
        request.filter.notpwithin = "50,20,-50,60";
        request.filter.gwithin = "POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))";
        request.filter.notgwithin = "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))";
        request.filter.gintersect = "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))";
        request.filter.notgintersect = "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))";
        handleComplexFilter(post(request));
        handleComplexFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).toString())
                    .param("f","params.startdate:range:[1009799;1009801]")
                    .param("pwithin", request.filter.pwithin)
                    .param("notpwithin", request.filter.notpwithin)
                    .param("gwithin", request.filter.gwithin)
                    .param("notgwithin", request.filter.notgwithin)
                    .param("gintersect", request.filter.gintersect)
                    .param("notgintersect", request.filter.notgintersect)
                .when().get(getUrlPath("geodata"))
                .then());
        handleComplexFilter(header(request.filter));
        request.filter = new Filter();
    }

    @Test
    public void testMixedFilter() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "Architect"),//"job:eq:Architect"
                new Expression("params.startdate", OperatorEnum.range, "[1009799;2000000]"));
        request.filter.pwithin = "50,-50,-50,50";
        request.filter.gwithin = "POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))";
        request.filter.gintersect = "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))";

        Filter filterHeader = new Filter();
        filterHeader.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[0;1009801]"));
        filterHeader.notpwithin = "50,20,-50,60";
        filterHeader.notgwithin = "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))";
        filterHeader.notgintersect = "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))";
        handleComplexFilter(
                givenFilterableRequestParams()
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .param("f", new Expression("params.job", OperatorEnum.eq, "Architect").toString())
                        .param("f", new Expression("params.startdate", OperatorEnum.range, "[1009799;2000000]").toString())
                        .param("pwithin", request.filter.pwithin)
                        .param("gwithin", request.filter.gwithin)
                        .param("gintersect", request.filter.gintersect)
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleComplexFilter(
                givenFilterableRequestBody().body(request)
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .when().post(getUrlPath("geodata"))
                        .then());

        filterHeader.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "Actor"));
        handleNotMatchingRequest(
                givenFilterableRequestParams().header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .param("f", (new Expression("params.job", OperatorEnum.eq, "Architect")).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingRequest(
                givenFilterableRequestBody().body(request)
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .when().post(getUrlPath("geodata"))
                        .then());
        request.filter = new Filter();
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNotFoundCollection() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0]),//"job:eq:" + DataSetTool.jobs[0]
                new Expression("params.startdate", OperatorEnum.range, "[1000000;2000000]"));
        request.filter.pwithin = "10,10,-10,-10";
        request.filter.notpwithin = "5,5,-5,-5";
        handleNotFoundCollection(
                givenFilterableRequestBody().body(request)
                .when().post(getUrlPath("unknowncollection"))
                .then());
        handleNotFoundCollection(
                givenFilterableRequestParams().param("f", request.filter.f)
                    .param("pwithin",  request.filter.pwithin)
                    .param("notpwithin",  request.filter.notpwithin)
                .when().get(getUrlPath("unknowncollection"))
                .then());
        request.filter.f = null;
        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }
    
    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        request.filter.f = Arrays.asList(new Expression("foobar", null, null));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[0;775000],"));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        //Q
        request.filter.q = "fullname:My:name";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("q", request.filter.q));
        handleInvalidParameters(header(request.filter));
        request.filter.q = null;
        
        //PWITHIN
        request.filter.pwithin = "-5,-5,5,5";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin",request.filter.pwithin));
        handleInvalidParameters(header(request.filter));

        request.filter.pwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin",request.filter.pwithin));
        handleInvalidParameters(header(request.filter));
        request.filter.pwithin = null;

        request.filter.notpwithin = "-5,-5,5,5";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin",request.filter.notpwithin));
        handleInvalidParameters(header(request.filter));

        request.filter.notpwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin",request.filter.notpwithin));
        handleInvalidParameters(header(request.filter));
        request.filter.notpwithin = null;
        
        //GWITHIN
        request.filter.gwithin = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin",request.filter.gwithin));
        handleInvalidParameters(header(request.filter));

        request.filter.gwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin",request.filter.gwithin));
        handleInvalidParameters(header(request.filter));
        request.filter.gwithin = null;

        request.filter.notgwithin = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin",request.filter.notgwithin));
        handleInvalidParameters(header(request.filter));

        request.filter.notgwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin",request.filter.notgwithin));
        handleInvalidParameters(header(request.filter));
        request.filter.notgwithin = null;

        //GINTERSECT
        request.filter.gintersect = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect",request.filter.gintersect));
        handleInvalidParameters(header(request.filter));

        request.filter.gintersect = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect",request.filter.gintersect));
        handleInvalidParameters(header(request.filter));
        request.filter.gintersect = null;

        request.filter.notgintersect = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect",request.filter.notgintersect));
        handleInvalidParameters(header(request.filter));

        request.filter.notgintersect = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect",request.filter.notgintersect));
        handleInvalidParameters(header(request.filter));
        request.filter.notgintersect = null;

    }
    
    
    //----------------------------------------------------------------
    //----------------------- COMMON BEHAVIORS -----------------------
    //----------------------------------------------------------------
    protected void handleNotFoundCollection(ValidatableResponse then) throws Exception {
        then.statusCode(404);
    }
    
    protected void handleInvalidParameters(ValidatableResponse then) throws Exception {
        then.statusCode(400);
    }

    protected void handleNotImplementedParameters(ValidatableResponse then) throws Exception {
        then.statusCode(501);
    }


    protected abstract void handleNotMatchingRequest(ValidatableResponse then);
    
    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------
    
    protected abstract RequestSpecification givenFilterableRequestParams();
    protected abstract RequestSpecification givenFilterableRequestBody();

    protected abstract void handleKnownFieldFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception;
    protected abstract void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception;

    
    protected abstract void handleMatchingQueryFilter(ValidatableResponse then) throws Exception;


    protected abstract void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end,
                                                               int size) throws Exception;
    protected abstract void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end,
                                                            int size) throws Exception;

    protected abstract void handleMatchingPwithinFilter(ValidatableResponse then, String centroid) throws Exception;
    protected abstract void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleComplexFilter(ValidatableResponse then) throws Exception;
    

    //----------------------------------------------------------------
    //---------------------- NOT MATCHING RESPONSES ------------------
    //----------------------------------------------------------------
    protected void handleUnknownFieldFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingQueryFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingRange(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }
    
    protected void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    //----------------------------------------------------------------
    //---------------------- ValidatableResponse ------------------
    //----------------------------------------------------------------

    private ValidatableResponse post(Request request){
        return givenFilterableRequestBody().body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String param,Object paramValue){
        return givenFilterableRequestParams().param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse header(Filter filter) throws JsonProcessingException {
        return givenFilterableRequestParams().header("Partition-Filter", objectMapper.writeValueAsString(filter))
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
