package io.arlas.server.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import io.arlas.server.model.CollectionReference;

public abstract class AbstractTestWithDataSet extends AbstractTest {
    
    @BeforeClass
    static public void beforeClass() {
        try {
            dataset = DataSetTool.init();
            dataset.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void before() {
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put(CollectionReference.CENTROID_PATH, DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put(CollectionReference.TIMESTAMP_PATH, DataSetTool.DATASET_TIMESTAMP_PATH);

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap).when().put("/collections/foo").then().statusCode(200);
    }

    @After
    public void after() {
        // DELETE collection
        when().delete("/collections/foo").then().statusCode(200);
    }

    @AfterClass
    public static void afterClass() {
        dataset.clearDataSet();
    }
}
