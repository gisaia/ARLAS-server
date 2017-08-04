package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class GeoSearchServiceIT extends AbstractSizedTest {
    
    @Override
    public String getUrlPath(String collection) {
        return arlasPrefix + "explore/"+collection+"/_geosearch";
    }
    
    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
        .body("type", equalTo("FeatureCollection"))

        .body("$", not(hasKey("features")));
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
        .body("features[0].properties.params.job", equalTo("Architect"))
        .body("features[0].properties.params.startdate", equalTo(1009800))
        .body("features[0].properties.geo_params.centroid", equalTo("20,-10"));
    }
    
    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(equalTo("Actor")));
    }
    
    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(isOneOf("Actor","Announcers")));
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(equalTo("Actor")));
    }

    //TODO : fix the case where the field is full text
    /*@Override
    protected void handleKnownFullTextFieldLikeFilter(ValidatableResponse then) throws Exception {
         then.statusCode(200)
        .body("features.properties.job", everyItem(isOneOf("Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }*/

    @Override
    protected void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.params.job", everyItem(isOneOf("Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter")));
    }
    
    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(10));//get only default sized result array
    }
    
    @Override
    protected void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.params.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.params.startdate", everyItem(greaterThan(1250000)));
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(2))
        .body("features.properties.params.startdate", everyItem(greaterThan(770000)))
        .body("features.properties.params.startdate", everyItem(lessThan(775000)));
    }
    
    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("0,0")));
    }
    
    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(10))//get only default sized result array
        .body("features.properties.geo_params.centroid", everyItem(endsWith("170")));
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8))
        .body("features.properties.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }
    
    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(4))
        .body("features.properties.geo_params.centroid", hasItems("-70,170","-80,170","-70,160","-80,160"));
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8))
        .body("features.properties.geo_params.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
    }
    
    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.geo_params.centroid", everyItem(equalTo("-80,170")));
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.geo_params.centroid", hasItems("10,-10","0,-10","-10,-10"));
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
    protected void handleSizeParameter(ValidatableResponse then, int size) {
        if(size > 0) {
            then.statusCode(200)
                .body("features.size()", equalTo(size));
        } else {
            then.statusCode(200)
            .body("$", not(hasKey("features")));
        }
    }
}
