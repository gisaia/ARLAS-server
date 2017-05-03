package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class GeoAggregateServiceIT extends AbstractAggregatedTest {
    
    @Override
    protected String getUrlPath(String collection) {
        return "/explore/"+collection+"/_geoaggregate";
    }
    
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given().param("agg", "geohash:centroid:interval-3");
    }
    
    @Override
    protected void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCount) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCount, featureCount);
    }
    
    @Override
    protected void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(featuresSize))
        .body("features.properties.count", everyItem(greaterThanOrEqualTo(featureCountMin)))
        .body("features.properties.count", everyItem(lessThanOrEqualTo(featureCountMax)));
    }
    
    @Override
    protected void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
            float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregate(then,featuresSize,featureCountMin,featureCountMax);
        then
        .body("features.properties.elements[0].metric.value", everyItem(greaterThanOrEqualTo(featureCollectMin)))
        .body("features.properties.elements[0].metric.value", everyItem(lessThanOrEqualTo(featureCollectMax)))
        .body("features.properties.elements[0].name", everyItem(equalTo(collectFct)))
        .body("features.properties.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }

    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    
    private void handleNotMatchingFilter(ValidatableResponse then) {
        then.statusCode(200)
        .body("type", equalTo("FeatureCollection"))
        .body("$", not(hasKey("features")));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1));     
    }
    
    //----------------------------------------------------------------
    //----------------------- FIELD ----------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(59));
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
        .body("features.size()", equalTo(595));
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
        .body("features.size()", equalTo(3));
    }

    @Override
    protected void handleNotMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3));
    }

    @Override
    protected void handleNotMatchingAfterFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(2));
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
        .body("features.size()", equalTo(1));
    }

    @Override
    protected void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(17));
    }

    @Override
    protected void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8));
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
        .body("features.size()", equalTo(1));
    }

    @Override
    protected void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(4));
    }

    @Override
    protected void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(8));
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
        .body("features.size()", equalTo(1));
    }

    @Override
    protected void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(1));
    }

    @Override
    protected void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("features.size()", equalTo(3));
    }

    @Override
    protected void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingFilter(then);
    }
}
