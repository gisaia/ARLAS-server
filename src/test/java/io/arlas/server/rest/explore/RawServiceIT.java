package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.arlas.server.rest.AbstractTestWithDataSet;

public class RawServiceIT extends AbstractTestWithDataSet {

    @Test
    public void testGetArlasHit() throws Exception {

        // GET existing document
        when().get(getUrlPath(COLLECTION_NAME)+"/0-0")
        .then().statusCode(200)
            .body("md.id", equalTo("0-0"))
            .body("data.centroid", equalTo("0,0"))
            .body("data.id", equalTo("0-0"))
            .body("data.fullname", equalTo("My name is 0-0"))
            .body("data.startdate", equalTo(1000000));

        // GET invalid collection
        when().get(getUrlPath("foo")+"/0-0")
        .then().statusCode(404);
        
        // GET invalid identifier
        when().get(getUrlPath(COLLECTION_NAME)+"/foo")
        .then().statusCode(404);
    }

    @Override
    protected String getUrlPath(String collection) {
        return "/explore/"+collection;
    }
}
