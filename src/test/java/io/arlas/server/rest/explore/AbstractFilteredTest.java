package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.arlas.server.rest.AbstractTestWithDataSet;
import io.arlas.server.rest.DataSetTool;
import io.restassured.response.ValidatableResponse;

public abstract class AbstractFilteredTest extends AbstractTestWithDataSet {
    
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testFieldFilter() throws Exception {
        handleKnownFieldFilter(
                given().param("f", "job:" + DataSetTool.jobs[0])
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleUnknownFieldFilter(
                given().param("f", "job:UnknownJob")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    @Test
    public void testQueryFilter() throws Exception {
        handleMatchingQueryFilter(
                given().param("q", "My name is")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingQueryFilter(
                given().param("q", "UnknownQuery")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    @Test
    public void testAfterBeforeFilter() throws Exception {
        //max 1 263 600
        //min 763600
        handleMatchingBeforeFilter(
                given().param("before", 775000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingBeforeFilter(
                given().param("before", 760000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingAfterFilter(
                given().param("after", 1250000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingAfterFilter(
                given().param("after", 1270000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingBeforeAfterFilter(
                given().param("after", 770000)
                    .param("before", 775000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingBeforeAfterFilter(
                given().param("after", 765000)
                    .param("before", 770000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    @Test
    public void testPwithinFilter() throws Exception {
        handleMatchingPwithinFilter(
                given().param("pwithin", "5,-5,-5,5")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingPwithinFilter(
                given().param("pwithin", "90,175,85,180")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingNotPwithinFilter(
                given().param("notpwithin", "85,-170,-85,175")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingNotPwithinFilter(
                given().param("notpwithin", "85,-175,-85,175")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        //TODO support correct 10,-10,-10,10 bounding box
        handleMatchingPwithinComboFilter(
                given().param("pwithin", "11,-11,-11,11")
                    .param("notpwithin", "5,-5,-5,5")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingPwithinComboFilter(
                given().param("pwithin", "6,-6,-6,6")
                    .param("notpwithin", "5,-5,-5,5")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    @Test
    public void testGwithinFilter() throws Exception {
        handleMatchingGwithinFilter(
                given().param("gwithin", "POLYGON((2 2,2 -2,-2 -2,-2 2,2 2))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingGwithinFilter(
                given().param("gwithin", "POLYGON((1 1,2 1,2 2,1 2,1 1))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingNotGwithinFilter(
                given().param("notgwithin", "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingNotGwithinFilter(
                given().param("notgwithin", "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingGwithinComboFilter(
                given().param("gwithin", "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))")
                    .param("notgwithin", "POLYGON((8 8,8 -8,-8 -8,-8 8,8 8))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingGwithinComboFilter(
                given().param("gwithin", "POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))")
                .param("notgwithin", "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    @Test
    public void testGintersectFilter() throws Exception {
        handleMatchingGintersectFilter(
                given().param("gintersect", "POLYGON((0 1,1 1,1 -1,0 -1,0 1))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingGintersectFilter(
                given().param("gintersect", "POLYGON((2 2,3 2,3 3,2 3,2 2))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingNotGintersectFilter(
                given().param("notgintersect", "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingNotGintersectFilter(
                given().param("notgintersect", "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleMatchingGintersectComboFilter(
                given().param("gintersect", "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))")
                    .param("notgintersect", "POLYGON((10 10,10 -10,0 -10,0 10,10 10))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleNotMatchingGintersectComboFilter(
                given().param("gintersect", "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))")
                .param("notgintersect", "POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    @Test
    public void testComplexFilter() throws Exception {
        handleComplexFilter(
                given().param("f", "job:Architect")
                    .param("after", 1009799)
                    .param("before", 1009801)
                    .param("pwithin", "50,-50,-50,50")
                    .param("notpwithin", "50,20,-50,60")
                    .param("gwithin", "POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))")
                    .param("notgwithin", "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))")
                    .param("gintersect", "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))")
                    .param("notgintersect", "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNotFoundCollection() throws Exception {
        handleNotFoundCollection(
                given().param("f", "job:" + DataSetTool.jobs[0])
                    .param("after", 1000000)
                    .param("before", 2000000)
                    .param("pwithin", "10,10,-10,-10")
                    .param("notpwithin", "5,5,-5,-5")
                .when().get(getUrlFilterPath("unknowncollection"))
                .then());
    }
    
    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        handleInvalidParameters(
                given().param("f", "foobar")
                .when().get(getUrlFilterPath("geodata"))
                .then());
                
        // BEFORE/AFTER
        handleInvalidParameters(
                given().param("before", 1000000)
                .param("after", 1200000)
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("before", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("after", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        
        //PWITHIN
        handleInvalidParameters(
                given().param("pwithin", "-5,-5,5,5")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("notpwithin", "-5,-5,5,5")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("pwithin", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("notpwithin", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        
        //GWITHIN
        handleInvalidParameters(
                given().param("gwithin", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("notgwithin", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("gwithin", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("notgwithin", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        
        //GINTERSECT
        handleInvalidParameters(
                given().param("gintersect", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("notgintersect", "POLYGON((10 10,10 -10,0 -10))")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("gintersect", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("notgintersect", "foo")
                .when().get(getUrlFilterPath("geodata"))
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
    
    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------
    protected abstract String getUrlFilterPath(String collection);
    
    protected abstract void handleKnownFieldFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleUnknownFieldFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingQueryFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingQueryFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingBeforeFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingAfterFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingAfterFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingPwithinComboFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingGwithinComboFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception;
    protected abstract void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception;
    
    protected abstract void handleComplexFilter(ValidatableResponse then) throws Exception;
}
