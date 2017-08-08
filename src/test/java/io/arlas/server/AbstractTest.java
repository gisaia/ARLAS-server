package io.arlas.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

import java.util.Optional;

public abstract class AbstractTest {
    static Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    
    protected String arlasPrefix;
    
    public AbstractTest() {
        arlasPrefix = Optional.ofNullable(System.getenv("ARLAS_PREFIX")).orElse("/arlas/");
    }

    static {
        String arlasHost = Optional.ofNullable(System.getenv("ARLAS_HOST")).orElse("localhost");
        int arlasPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_PORT")).orElse("9999"));
        RestAssured.baseURI = "http://"+arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = "";
        LOGGER.info(arlasHost+":"+arlasPort);
    }

    protected abstract String getUrlPath(String collection);
}