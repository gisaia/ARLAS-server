package io.arlas.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

public abstract class AbstractTest {
    static DataSetTool dataset = null;
    static Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);

    static {
        String arlasHost = System.getenv("ARLAS_HOST");
        int arlasPort = Integer.valueOf(System.getenv("ARLAS_PORT"));
        String arlasPrefix = System.getenv("ARLAS_PREFIX");
        RestAssured.baseURI = "http://"+arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = arlasPrefix;
        LOGGER.info(arlasHost+":"+arlasPort+arlasPrefix);
    }
    
    protected abstract String getUrlPath(String collection);
}