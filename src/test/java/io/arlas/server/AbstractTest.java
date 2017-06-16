package io.arlas.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

public abstract class AbstractTest {
    static Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    
    protected String arlasPrefix;
    
    public AbstractTest() {
        arlasPrefix = System.getenv("ARLAS_PREFIX");
    }

    static {
        String arlasHost = System.getenv("ARLAS_HOST");
        int arlasPort = Integer.valueOf(System.getenv("ARLAS_PORT"));
        RestAssured.baseURI = "http://"+arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = "";
        LOGGER.info(arlasHost+":"+arlasPort);
    }

    protected abstract String getUrlPath(String collection);
}