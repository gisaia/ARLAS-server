package io.arlas.server.task;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Test;

import io.arlas.server.AbstractTestWithDataSet;
import io.arlas.server.DataSetTool;

public class CollectionAutoDiscoverIT extends AbstractTestWithDataSet {

    @Override
    protected String getUrlPath(String task) {
        return "/admin/tasks/"+task;
    }
    
    @Test
    public void testCollectionAutoDiscover() throws Exception {
        // discover collections
        when().post(getUrlPath("collection-auto-discover"))
        .then().statusCode(200);

        // GET collection
        when().get(arlasPrefix+"collections/"+DataSetTool.DATASET_INDEX_NAME+"-"+DataSetTool.DATASET_TYPE_NAME)
        .then().statusCode(200)
            .body("collection_name", equalTo(DataSetTool.DATASET_INDEX_NAME+"-"+DataSetTool.DATASET_TYPE_NAME))
            .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
            .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
            .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
            .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
            .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
            .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH))
            .body("params.custom_params.timestamp_format",equalTo(DataSetTool.DATASET_TIMESTAMP_FORMAT));

        // discover collections
        when().post(getUrlPath("collection-auto-discover"))
        .then().statusCode(200);

        // GET all collections
        when().get(arlasPrefix+"collections/")
        .then().statusCode(200)
            .body("collection_name", hasSize(1));
        
        // DELETE collection
        when().delete(arlasPrefix+"collections/"+DataSetTool.DATASET_INDEX_NAME+"-"+DataSetTool.DATASET_TYPE_NAME)
        .then().statusCode(200);
    }
}
