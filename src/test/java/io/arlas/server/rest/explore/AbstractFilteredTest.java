package io.arlas.server.rest.explore;

import com.fasterxml.jackson.databind.util.JSONPObject;
import groovy.json.JsonBuilder;
import io.arlas.server.model.request.*;
import org.junit.Before;
import org.junit.Test;

import io.arlas.server.rest.AbstractTestWithDataSet;
import io.arlas.server.rest.DataSetTool;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.Arrays;

public abstract class AbstractFilteredTest extends AbstractTestWithDataSet {

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
        request.filter.f = Arrays.asList("job:" + DataSetTool.jobs[0]);
        handleKnownFieldFilter(post(request));
        handleKnownFieldFilter(get("f", request.filter.f.get(0)));

        request.filter.f = Arrays.asList("job:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleKnownFieldFilterWithOr(post(request));
        handleKnownFieldFilterWithOr(get("f", request.filter.f.get(0)));

        request.filter.f = Arrays.asList("job:like:" + "cto");
        handleKnownFieldLikeFilter(post(request));
        handleKnownFieldLikeFilter(get("f", request.filter.f.get(0)));

        request.filter.f = Arrays.asList("job:ne:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleKnownFieldFilterNotEqual(post(request));
        handleKnownFieldFilterNotEqual(get("f", request.filter.f.get(0)));
        //TODO : fix the case where the field is full text
        /*handleKnownFullTextFieldLikeFilter(
                givenFilterableRequestParams().param("f", "fullname:like:" + "name is")
                        .when().get(getUrlPath("geodata"))
                        .then());*/
        request.filter.f = Arrays.asList("job:UnknownJob");
        handleUnknownFieldFilter(post(request));
        handleUnknownFieldFilter(get("f", request.filter.f.get(0)));
        request.filter.f = null;

    }
    
    @Test
    public void testQueryFilter() throws Exception {

        request.filter.q = "My name is";
        handleMatchingQueryFilter(post(request));
        handleMatchingQueryFilter(get("q", request.filter.q));

        request.filter.q = "UnknownQuery";
        handleNotMatchingQueryFilter(post(request));
        handleNotMatchingQueryFilter(get("q", request.filter.q));

        request.filter.q = null;
    }
    
    @Test
    public void testAfterBeforeFilter() throws Exception {

        //max 1 263 600
        //min 763600
        request.filter.before = 775000L;
        handleMatchingBeforeFilter(post(request));
        handleMatchingBeforeFilter(get("before",request.filter.before));

        request.filter.before = 760000L;
        handleNotMatchingBeforeFilter(post(request));
        handleNotMatchingBeforeFilter(get("before",request.filter.before));

        request.filter.before = null;
        request.filter.after = 1250000L;
        handleMatchingAfterFilter(post(request));
        handleMatchingAfterFilter(get("after",request.filter.after));

        request.filter.after = 1270000L;
        handleNotMatchingAfterFilter(post(request));
        handleNotMatchingAfterFilter(get("after",request.filter.after));

        request.filter.after = 770000L;
        request.filter.before = 775000L;
        handleMatchingBeforeAfterFilter(post(request));
        handleMatchingBeforeAfterFilter(
                givenFilterableRequestParams().param("after", request.filter.after)
                    .param("before", request.filter.before)
                .when().get(getUrlPath("geodata"))
                .then());

        request.filter.after = 765000L;
        request.filter.before = 770000L;
        handleNotMatchingBeforeAfterFilter(post(request));
        handleNotMatchingBeforeAfterFilter(
                givenFilterableRequestParams().param("after", request.filter.after)
                    .param("before", request.filter.before)
                .when().get(getUrlPath("geodata"))
                .then());

        request.filter.after = null;
        request.filter.before = null;
    }
    
    @Test
    public void testPwithinFilter() throws Exception {
        request.filter.pwithin = "5,-5,-5,5";
        handleMatchingPwithinFilter(post(request));
        handleMatchingPwithinFilter(get("pwithin",request.filter.pwithin));

        request.filter.pwithin = "90,175,85,180";
        handleNotMatchingPwithinFilter(post(request));
        handleNotMatchingPwithinFilter(get("pwithin",request.filter.pwithin));

        request.filter.pwithin = null;
        request.filter.notpwithin = "85,-170,-85,175";
        handleMatchingNotPwithinFilter(post(request));
        handleMatchingNotPwithinFilter(get("notpwithin",request.filter.notpwithin));

        request.filter.notpwithin = "85,-175,-85,175";
        handleNotMatchingNotPwithinFilter(post(request));
        handleNotMatchingNotPwithinFilter(get("notpwithin",request.filter.notpwithin));

        //TODO support correct 10,-10,-10,10 bounding box
        request.filter.pwithin = "11,-11,-11,11";
        request.filter.notpwithin = "5,-5,-5,5";
        handleMatchingPwithinComboFilter(post(request));
        handleMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin)
                    .param("notpwithin", request.filter.notpwithin)
                .when().get(getUrlPath("geodata"))
                .then());

        request.filter.pwithin = "6,-6,-6,6";
        request.filter.notpwithin = "5,-5,-5,5";
        handleNotMatchingPwithinComboFilter(post(request));
        handleNotMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin)
                    .param("notpwithin", request.filter.notpwithin)
                .when().get(getUrlPath("geodata"))
                .then());

        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }
    
    @Test
    public void testGwithinFilter() throws Exception {
        request.filter.gwithin = "POLYGON((2 2,2 -2,-2 -2,-2 2,2 2))";
        handleMatchingGwithinFilter(post(request));
        handleMatchingGwithinFilter(get("gwithin",request.filter.gwithin));

        request.filter.gwithin = "POLYGON((1 1,2 1,2 2,1 2,1 1))";
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("gwithin",request.filter.gwithin));
        request.filter.gwithin = null;

        request.filter.notgwithin = "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))";
        handleMatchingNotGwithinFilter(post(request));
        handleMatchingNotGwithinFilter(get("notgwithin",request.filter.notgwithin));

        request.filter.notgwithin = "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))";
        handleNotMatchingNotGwithinFilter(post(request));
        handleNotMatchingNotGwithinFilter(get("notgwithin",request.filter.notgwithin));

        request.filter.gwithin = "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))";
        request.filter.notgwithin = "POLYGON((8 8,8 -8,-8 -8,-8 8,8 8))";
        handleMatchingGwithinComboFilter(post(request));
        handleMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin)
                    .param("notgwithin", request.filter.notgwithin)
                .when().get(getUrlPath("geodata"))
                .then());

        request.filter.gwithin = "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))";
        request.filter.notgwithin = "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))";
        handleNotMatchingGwithinComboFilter(post(request));
        handleNotMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin)
                .param("notgwithin", request.filter.notgwithin)
                .when().get(getUrlPath("geodata"))
                .then());
        request.filter.gwithin = null;
        request.filter.notgwithin = null;
    }
    
    @Test
    public void testGintersectFilter() throws Exception {
        request.filter.gintersect = "POLYGON((0 1,1 1,1 -1,0 -1,0 1))";
        handleMatchingGintersectFilter(post(request));
        handleMatchingGintersectFilter(get("gintersect",request.filter.gintersect));

        request.filter.gintersect = "POLYGON((2 2,3 2,3 3,2 3,2 2))";
        handleNotMatchingGintersectFilter(post(request));
        handleNotMatchingGintersectFilter(get("gintersect", request.filter.gintersect));
        request.filter.gintersect = null;

        request.filter.notgintersect = "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))";
        handleMatchingNotGintersectFilter(post(request));
        handleMatchingNotGintersectFilter(get("notgintersect", request.filter.notgintersect));

        request.filter.notgintersect = "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))";
        handleNotMatchingNotGintersectFilter(post(request));
        handleNotMatchingNotGintersectFilter(get("notgintersect", request.filter.notgintersect));

        request.filter.gintersect = "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))";
        request.filter.notgintersect = "POLYGON((10 10,10 -10,0 -10,0 10,10 10))";
        handleMatchingGintersectComboFilter(post(request));
        handleMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect)
                    .param("notgintersect", request.filter.notgintersect)
                .when().get(getUrlPath("geodata"))
                .then());

        request.filter.gintersect = "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))";
        request.filter.notgintersect = "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))";
        handleNotMatchingGintersectComboFilter(post(request));
        handleNotMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect)
                .param("notgintersect", request.filter.notgintersect)
                .when().get(getUrlPath("geodata"))
                .then());
        request.filter.gintersect = null;
        request.filter.notgintersect = null;

    }
    
    @Test
    public void testComplexFilter() throws Exception {
        request.filter.f = Arrays.asList("job:Architect");
        request.filter.after = 1009799L;
        request.filter.before = 1009801L;
        request.filter.pwithin = "50,-50,-50,50";
        request.filter.notpwithin = "50,20,-50,60";
        request.filter.gwithin = "POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))";
        request.filter.notgwithin = "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))";
        request.filter.gintersect = "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))";
        request.filter.notgintersect = "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))";
        handleComplexFilter(post(request));
        handleComplexFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0))
                    .param("after", request.filter.after)
                    .param("before", request.filter.before)
                    .param("pwithin", request.filter.pwithin)
                    .param("notpwithin", request.filter.notpwithin)
                    .param("gwithin", request.filter.gwithin)
                    .param("notgwithin", request.filter.notgwithin)
                    .param("gintersect", request.filter.gintersect)
                    .param("notgintersect", request.filter.notgintersect)
                .when().get(getUrlPath("geodata"))
                .then());
        request.filter = new Filter();
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNotFoundCollection() throws Exception {
        request.filter.f = Arrays.asList("job:" + DataSetTool.jobs[0]);
        request.filter.after = 1000000L;
        request.filter.before = 2000000L;
        request.filter.pwithin = "10,10,-10,-10";
        request.filter.notpwithin = "5,5,-5,-5";
        handleNotFoundCollection(
                givenFilterableRequestBody().body(request)
                .when().post(getUrlPath("unknowncollection"))
                .then());
        handleNotFoundCollection(
                givenFilterableRequestParams().param("f", request.filter.f)
                    .param("after", request.filter.after)
                    .param("before",  request.filter.before)
                    .param("pwithin",  request.filter.pwithin)
                    .param("notpwithin",  request.filter.notpwithin)
                .when().get(getUrlPath("unknowncollection"))
                .then());
        request.filter.f = null;
        request.filter.after = null;
        request.filter.before = null;
        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }
    
    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        request.filter.f = Arrays.asList("foobar");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0)));
        request.filter.f = null;
                
        // BEFORE/AFTER
        request.filter.after = 1200000l;
        request.filter.before = 1000000L;
        handleInvalidParameters(post(request));
        handleInvalidParameters(
                givenFilterableRequestParams().param("before", request.filter.before)
                .param("after", request.filter.after)
                .when().get(getUrlPath("geodata"))
                .then());
        request.filter.after = null;
        request.filter.before = null;

        handleInvalidParameters(
                givenFilterableRequestParams().param("before", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("after", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        
        //PWITHIN
        request.filter.pwithin = "-5,-5,5,5";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin",request.filter.pwithin));

        request.filter.pwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin",request.filter.pwithin));
        request.filter.pwithin = null;

        request.filter.notpwithin = "-5,-5,5,5";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin",request.filter.notpwithin));

        request.filter.notpwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin",request.filter.notpwithin));
        request.filter.notpwithin = null;
        
        //GWITHIN
        request.filter.gwithin = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin",request.filter.gwithin));

        request.filter.gwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin",request.filter.gwithin));
        request.filter.gwithin = null;

        request.filter.notgwithin = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin",request.filter.notgwithin));

        request.filter.notgwithin = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin",request.filter.notgwithin));
        request.filter.notgwithin = null;

        //GINTERSECT
        request.filter.gintersect = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect",request.filter.gintersect));

        request.filter.gintersect = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect",request.filter.gintersect));
        request.filter.gintersect = null;

        request.filter.notgintersect = "POLYGON((10 10,10 -10,0 -10))";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect",request.filter.notgintersect));

        request.filter.notgintersect = "foo";
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect",request.filter.notgintersect));
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
    
    protected abstract void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingAfterFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception;
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
    
    protected void handleNotMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingAfterFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
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
}
