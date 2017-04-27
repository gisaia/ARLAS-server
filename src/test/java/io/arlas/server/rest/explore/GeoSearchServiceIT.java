package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class GeoSearchServiceIT extends AbstractSizedTest {
    
    @Override
    public String getUrlFilterPath(String collection) {
        return "/explore/"+collection+"/_geosearch";
    }
    
    @Override
    protected RequestSpecification givenBigSizedRequestParams() {
        return given().param("q", "My name is");
    }
    
    @Override
    protected int getBigSizedResponseSize() {
        return 595;
    }
    
    private void handleNotMatchingFilter(ValidatableResponse then) {
        then.statusCode(200)
        .body("type", equalTo("FeatureCollection"))
        .body("$", not(hasKey("features")));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features[0].properties.job", equalTo("Architect"))
        .body("features[0].properties.startdate", equalTo(1009800))
        .body("features[0].properties.centroid", equalTo("20,-10"));      
    }
    
    //----------------------------------------------------------------
    //----------------------- FIELD ----------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.properties.job", everyItem(equalTo("Actor")));
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
        .body("features.size()", equalTo(10));//get only default sized result array
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
        .body("features.size()", equalTo(3))
        .body("features.properties.startdate", everyItem(lessThan(775000)));
    }

    @Override
    protected void handleNotMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.startdate", everyItem(greaterThan(1250000)));
    }

    @Override
    protected void handleNotMatchingAfterFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(2))
        .body("features.properties.startdate", everyItem(greaterThan(770000)))
        .body("features.properties.startdate", everyItem(lessThan(775000)));
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
        .body("features.size()", equalTo(1))
        .body("features.properties.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(10))//get only default sized result array
        .body("features.properties.centroid", everyItem(endsWith("170")));
    }

    @Override
    protected void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8))
        .body("features.properties.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
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
        .body("features.size()", equalTo(1))
        .body("features.properties.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(4))
        .body("features.properties.centroid", hasItems("-70,170","-80,170","-70,160","-80,160"));
    }

    @Override
    protected void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8))
        .body("features.properties.centroid", hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
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
        .body("features.size()", equalTo(1))
        .body("features.properties.centroid", everyItem(equalTo("0,0")));
    }

    @Override
    protected void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1))
        .body("features.properties.centroid", everyItem(equalTo("-80,170")));
    }

    @Override
    protected void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3))
        .body("features.properties.centroid", hasItems("10,-10","0,-10","-10,-10"));
    }

    @Override
    protected void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    //----------------------------------------------------------------
    //----------------------- SIZE -----------------------------------
    //----------------------------------------------------------------
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
