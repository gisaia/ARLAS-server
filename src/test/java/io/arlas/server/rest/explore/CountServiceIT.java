package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class CountServiceIT extends AbstractFilteredTest {
    
    @Override
    public String getUrlPath(String collection) {
        return "/explore/"+collection+"/_count";
    }
    
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given();
    }
    
    private void handleMatchingFilter(ValidatableResponse then, int nbResults) {
        then.statusCode(200)
                .body("totalnb", equalTo(nbResults));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,1);
    }
    
    //----------------------------------------------------------------
    //----------------------- FIELD ----------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,59);
    }

    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,117);
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,59);
    }

    @Override
    protected void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,478);
    }

    //TODO : fix the case where the field is full text
    /*@Override
    protected void handleKnownFullTextFieldLikeFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,595);
    }*/

    @Override
    protected void handleUnknownFieldFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }
    
    //----------------------------------------------------------------
    //----------------------- TEXT QUERY -----------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,595); 
    }

    @Override
    protected void handleNotMatchingQueryFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }
    
    //----------------------------------------------------------------
    //----------------------- BEFORE/AFTER ---------------------------
    //----------------------------------------------------------------
    
    @Override
    protected void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,3);
    }

    @Override
    protected void handleNotMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,3);
    }

    @Override
    protected void handleNotMatchingAfterFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,2);
    }

    @Override
    protected void handleNotMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }
    
    //----------------------------------------------------------------
    //----------------------- GIWTHIN --------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,1);
    }

    @Override
    protected void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,17);
    }

    @Override
    protected void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,8);
    }

    @Override
    protected void handleNotMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }
    
    //----------------------------------------------------------------
    //----------------------- GIWTHIN --------------------------------
    //----------------------------------------------------------------
    
    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,1);
    }

    @Override
    protected void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,4);
    }

    @Override
    protected void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,8);
    }

    @Override
    protected void handleNotMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }
    
    //----------------------------------------------------------------
    //----------------------- GINTERSECT --------------------------------
    //----------------------------------------------------------------
    
    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,1);
    }

    @Override
    protected void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,1);
    }

    @Override
    protected void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,3);
    }

    @Override
    protected void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleMatchingFilter(then,0);
    }
}
