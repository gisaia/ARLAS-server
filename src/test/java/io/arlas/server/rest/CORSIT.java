package io.arlas.server.rest;

import io.arlas.server.AbstractTest;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;

import static io.restassured.RestAssured.given;

/**
 * Created by sfalquier on 07/07/2017.
 */
public class CORSIT extends AbstractTest {

    @Test
    public void testCORS() throws Exception {

        // CHECK CORS
        given()
                .header("Origin","http://example.com")
                .header("Access-Control-Request-Method","GET")
                .header("Access-Control-Request-Headers","X-Requested-With")
        .when().get(arlasPrefix+"collections/")
                .then()
                .header(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://example.com")
                .header(CrossOriginFilter.ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location")
                .header(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    }

    @Override
    protected String getUrlPath(String collection) {
        return null;
    }
}
