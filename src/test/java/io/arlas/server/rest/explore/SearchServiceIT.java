package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class SearchServiceIT extends AbstractSizedTest {
    
    @Override
    public String getUrlPath(String collection) {
        return "/explore/"+collection+"/_search";
    }
    
    @Override
    protected RequestSpecification givenBigSizedRequestParams() {
        return given().param("q", "My name is");
    }
    
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given();
    }
    
    @Override
    protected int getBigSizedResponseSize() {
        return 595;
    }
    
    private void handleNotMatchingFilter(ValidatableResponse then) {
        then.statusCode(200)
        .body("totalnb", equalTo(0));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits[0].data.job", equalTo("Architect"))
        .body("hits[0].data.startdate", equalTo(1009800))
        .body("hits[0].data.centroid", equalTo("20,-10"));      
    }
    
    //----------------------------------------------------------------
    //----------------------- FIELD ----------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(59))
        .body("hits.data.job", everyItem(equalTo("Actor")));
    }

    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(117))
        .body("hits.data.job",  everyItem(isOneOf("Actor","Announcers")));
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(59))
        .body("hits.data.job",  everyItem(equalTo("Actor")));
    }

    //TODO : fix the case where the field is full text
    /*@Override
    protected void handleKnownFullTextFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(595))
        .body("hits.data.job", everyItem(isOneOf("Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }*/

    @Override
    protected void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(478))
        .body("hits.data.job", everyItem(isOneOf("Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }


    @Override
    protected void handleUnknownFieldFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
    
    //----------------------------------------------------------------
    //----------------------- TEXT QUERY -----------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(595));
    }

    @Override
    protected void handleNotMatchingQueryFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
    
    //----------------------------------------------------------------
    //----------------------- BEFORE/AFTER ---------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleNotMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.startdate", everyItem(greaterThan(1250000)));
    }

    @Override
    protected void handleNotMatchingAfterFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(2))
        .body("hits.data.startdate", everyItem(greaterThan(770000)))
        .body("hits.data.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleNotMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
    
    //----------------------------------------------------------------
    //----------------------- PWITHIN --------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(17))
        .body("hits.data.centroid", everyItem(endsWith("170")));
    }

    @Override
    protected void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(8))
        .body("hits.data.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }

    @Override
    protected void handleNotMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
    
    //----------------------------------------------------------------
    //----------------------- GIWTHIN --------------------------------
    //----------------------------------------------------------------
    
    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(4))
        .body("hits.data.centroid", hasItems("-70,170","-80,170","-70,160","-80,160"));
    }

    @Override
    protected void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(8))
        .body("hits.data.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }

    @Override
    protected void handleNotMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
    
    //----------------------------------------------------------------
    //----------------------- GINTERSECT -----------------------------
    //----------------------------------------------------------------
    
    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.centroid", everyItem(equalTo("-80,170")));
    }

    @Override
    protected void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.centroid", hasItems("10,-10","0,-10","-10,-10"));
    }

    @Override
    protected void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
    
    //----------------------------------------------------------------
    //----------------------- SIZE -----------------------------------
    //----------------------------------------------------------------
    @Override
    protected void handleSizeParameter(ValidatableResponse then, int size) throws Exception {
        if(size > 0) {
            then.statusCode(200)
                .body("nbhits", equalTo(size))
                .body("hits.size()", equalTo(size));
        } else {
            then.statusCode(200)
            .body("nbhits", equalTo(size));
        }
    }
}
