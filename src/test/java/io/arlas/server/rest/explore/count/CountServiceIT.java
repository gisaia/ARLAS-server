package io.arlas.server.rest.explore.count;

import static io.restassured.RestAssured.given;

import io.arlas.server.rest.AbstractTest;
import org.junit.Test;


public class CountServiceIT extends AbstractTest{

    @Test
    public void testBasicCount() throws Exception {
        // GET _count
        given().param("f", "test")
                .when().get("/explore/foo/_count")
                .then().statusCode(200);
    }

    @Test
    public void testBasicSearch() throws Exception {
        // GET _search
        given().param("f", "test")
                .when().get("/explore/foo/_search")
                .then().statusCode(200);
    }
}
