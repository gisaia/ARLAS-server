package io.arlas.server.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Request;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.arlas.server.model.CollectionReference;

public abstract class AbstractTestWithDataSet extends AbstractTest {
    protected static Request request = new Request();
    static{
        request.filter = new Filter();
    }

    @BeforeClass
    public static void beforeClass() {
        try {
           dataset = DataSetTool.init();
           dataset.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put(CollectionReference.CENTROID_PATH, DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put(CollectionReference.TIMESTAMP_PATH, DataSetTool.DATASET_TIMESTAMP_PATH);

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap).when().put("/collections/geodata").then().statusCode(200);
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        dataset.clearDataSet();
        
        //DELETE collection
        when().delete("/collections/geodata").then().statusCode(200);
    }
}
