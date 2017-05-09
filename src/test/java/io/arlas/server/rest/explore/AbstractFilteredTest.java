package io.arlas.server.rest.explore;

import org.junit.Test;

import io.arlas.server.rest.AbstractTestWithDataSet;
import io.arlas.server.rest.DataSetTool;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractFilteredTest extends AbstractTestWithDataSet {
    
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testFieldFilter() throws Exception {
        handleKnownFieldFilter(
                givenFilterableRequestParams().param("f", "job:" + DataSetTool.jobs[0])
                .when().get(getUrlPath("geodata"))
                .then());
        handleKnownFieldFilterWithOr(
                givenFilterableRequestParams().param("f", "job:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1])
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleKnownFieldLikeFilter(
                givenFilterableRequestParams().param("f", "job:like:" + "cto")
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleKnownFieldFilterNotEqual(
                givenFilterableRequestParams().param("f", "job:ne:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1])
                        .when().get(getUrlPath("geodata"))
                        .then());
        //TODO : fix the case where the field is full text
        /*handleKnownFullTextFieldLikeFilter(
                givenFilterableRequestParams().param("f", "fullname:like:" + "name is")
                        .when().get(getUrlPath("geodata"))
                        .then());*/
        handleUnknownFieldFilter(
                givenFilterableRequestParams().param("f", "job:UnknownJob")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    @Test
    public void testQueryFilter() throws Exception {
        handleMatchingQueryFilter(
                givenFilterableRequestParams().param("q", "My name is")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingQueryFilter(
                givenFilterableRequestParams().param("q", "UnknownQuery")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    @Test
    public void testAfterBeforeFilter() throws Exception {
        //max 1 263 600
        //min 763600
        handleMatchingBeforeFilter(
                givenFilterableRequestParams().param("before", 775000)
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingBeforeFilter(
                givenFilterableRequestParams().param("before", 760000)
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingAfterFilter(
                givenFilterableRequestParams().param("after", 1250000)
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingAfterFilter(
                givenFilterableRequestParams().param("after", 1270000)
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingBeforeAfterFilter(
                givenFilterableRequestParams().param("after", 770000)
                    .param("before", 775000)
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingBeforeAfterFilter(
                givenFilterableRequestParams().param("after", 765000)
                    .param("before", 770000)
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    @Test
    public void testPwithinFilter() throws Exception {
        handleMatchingPwithinFilter(
                givenFilterableRequestParams().param("pwithin", "5,-5,-5,5")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingPwithinFilter(
                givenFilterableRequestParams().param("pwithin", "90,175,85,180")
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingNotPwithinFilter(
                givenFilterableRequestParams().param("notpwithin", "85,-170,-85,175")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingNotPwithinFilter(
                givenFilterableRequestParams().param("notpwithin", "85,-175,-85,175")
                .when().get(getUrlPath("geodata"))
                .then());
        //TODO support correct 10,-10,-10,10 bounding box
        handleMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", "11,-11,-11,11")
                    .param("notpwithin", "5,-5,-5,5")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", "6,-6,-6,6")
                    .param("notpwithin", "5,-5,-5,5")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    @Test
    public void testGwithinFilter() throws Exception {
        handleMatchingGwithinFilter(
                givenFilterableRequestParams().param("gwithin", "POLYGON((2 2,2 -2,-2 -2,-2 2,2 2))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGwithinFilter(
                givenFilterableRequestParams().param("gwithin", "POLYGON((1 1,2 1,2 2,1 2,1 1))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingNotGwithinFilter(
                givenFilterableRequestParams().param("notgwithin", "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingNotGwithinFilter(
                givenFilterableRequestParams().param("notgwithin", "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))")
                    .param("notgwithin", "POLYGON((8 8,8 -8,-8 -8,-8 8,8 8))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))")
                .param("notgwithin", "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    @Test
    public void testGintersectFilter() throws Exception {
        handleMatchingGintersectFilter(
                givenFilterableRequestParams().param("gintersect", "POLYGON((0 1,1 1,1 -1,0 -1,0 1))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGintersectFilter(
                givenFilterableRequestParams().param("gintersect", "POLYGON((2 2,3 2,3 3,2 3,2 2))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingNotGintersectFilter(
                givenFilterableRequestParams().param("notgintersect", "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingNotGintersectFilter(
                givenFilterableRequestParams().param("notgintersect", "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))")
                    .param("notgintersect", "POLYGON((10 10,10 -10,0 -10,0 10,10 10))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))")
                .param("notgintersect", "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    @Test
    public void testComplexFilter() throws Exception {
        handleComplexFilter(
                givenFilterableRequestParams().param("f", "job:Architect")
                    .param("after", 1009799)
                    .param("before", 1009801)
                    .param("pwithin", "50,-50,-50,50")
                    .param("notpwithin", "50,20,-50,60")
                    .param("gwithin", "POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))")
                    .param("notgwithin", "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))")
                    .param("gintersect", "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))")
                    .param("notgintersect", "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNotFoundCollection() throws Exception {
        handleNotFoundCollection(
                givenFilterableRequestParams().param("f", "job:" + DataSetTool.jobs[0])
                    .param("after", 1000000)
                    .param("before", 2000000)
                    .param("pwithin", "10,10,-10,-10")
                    .param("notpwithin", "5,5,-5,-5")
                .when().get(getUrlPath("unknowncollection"))
                .then());
    }
    
    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        handleInvalidParameters(
                givenFilterableRequestParams().param("f", "foobar")
                .when().get(getUrlPath("geodata"))
                .then());
                
        // BEFORE/AFTER
        handleInvalidParameters(
                givenFilterableRequestParams().param("before", 1000000)
                .param("after", 1200000)
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("before", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("after", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        
        //PWITHIN
        handleInvalidParameters(
                givenFilterableRequestParams().param("pwithin", "-5,-5,5,5")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("notpwithin", "-5,-5,5,5")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("pwithin", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("notpwithin", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        
        //GWITHIN
        handleInvalidParameters(
                givenFilterableRequestParams().param("gwithin", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("notgwithin", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("gwithin", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("notgwithin", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        
        //GINTERSECT
        handleInvalidParameters(
                givenFilterableRequestParams().param("gintersect", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("notgintersect", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("gintersect", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                givenFilterableRequestParams().param("notgintersect", "foo")
                .when().get(getUrlPath("geodata"))
                .then());
        
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
}
