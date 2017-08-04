package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThan;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class SearchServiceIT extends AbstractSizedTest {
    
    @Override
    public String getUrlPath(String collection) {
        return arlasPrefix + "explore/"+collection+"/_search";
    }
    
    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
        .body("totalnb", equalTo(0));
    }
    
    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given();
    }

    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        return given().contentType("application/json");
    }
    
    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits[0].data.params.job", equalTo("Architect"))
        .body("hits[0].data.params.startdate", equalTo(1009800))
        .body("hits[0].data.geo_params.centroid", equalTo("20,-10"))
        .body("hits[0].md.timestamp",equalTo(1009800));
    }

    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(59))
        .body("hits.data.params.job", everyItem(equalTo("Actor")));
    }

    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(117))
        .body("hits.data.params.job",  everyItem(isOneOf("Actor","Announcers")));
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(59))
        .body("hits.data.params.job",  everyItem(equalTo("Actor")));
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
        .body("hits.data.params.job", everyItem(isOneOf("Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
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
    protected void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.params.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.params.startdate", everyItem(greaterThan(1250000)));
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(2))
        .body("hits.data.params.startdate", everyItem(greaterThan(770000)))
        .body("hits.data.params.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(17))
        .body("hits.data.geo_params.centroid", everyItem(endsWith("170")));
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(8))
        .body("hits.data.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }

    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(4))
        .body("hits.data.geo_params.centroid", hasItems("-70,170","-80,170","-70,160","-80,160"));
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(8))
        .body("hits.data.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }

    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(1))
        .body("hits.data.geo_params.centroid", everyItem(equalTo("-80,170")));
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("totalnb", equalTo(3))
        .body("hits.data.geo_params.centroid", hasItems("10,-10","0,-10","-10,-10"));
    }
    
    //----------------------------------------------------------------
    //----------------------- SIZE PART ------------------------------
    //----------------------------------------------------------------
    @Override
    protected RequestSpecification givenBigSizedRequestParams() {
        return given().param("q", "My name is");
    }

    @Override
    protected RequestSpecification givenBigSizedRequestParamsPost() {
        search.filter.q = "My name is";
        return given().contentType("application/json");
    }

    @Override
    protected int getBigSizedResponseSize() {
        return 595;
    }
    
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
