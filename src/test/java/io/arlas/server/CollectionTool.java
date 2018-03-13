package io.arlas.server;

import io.arlas.server.model.CollectionReference;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class CollectionTool extends AbstractTestContext {

    public static String COLLECTION_NAME = "geodata";

    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "load":
                load();
                break;
            case "delete":
                delete();
                break;
        }
    }

    @Test
    public static void load() {
        try {
            DataSetTool.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put(CollectionReference.CENTROID_PATH, DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put(CollectionReference.TIMESTAMP_PATH, DataSetTool.DATASET_TIMESTAMP_PATH);
        jsonAsMap.put(CollectionReference.INCLUDE_FIELDS, DataSetTool.DATASET_INCLUDE_FIELDS);
        jsonAsMap.put(CollectionReference.EXCLUDE_FIELDS, DataSetTool.DATASET_EXCLUDE_FIELDS);

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap).when().put("/arlas/collections/" + COLLECTION_NAME).then().statusCode(200);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void delete() throws IOException {
        DataSetTool.clearDataSet();
        //DELETE collection
        when().delete("/arlas/collections/" + COLLECTION_NAME).then().statusCode(200);
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPrefix + "/collections/" + COLLECTION_NAME;
    }
}

