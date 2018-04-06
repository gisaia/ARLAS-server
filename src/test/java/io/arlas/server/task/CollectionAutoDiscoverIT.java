/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.task;

import io.arlas.server.AbstractTestWithDataSet;
import io.arlas.server.DataSetTool;
import org.hamcrest.Matcher;
import org.junit.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class CollectionAutoDiscoverIT extends AbstractTestWithDataSet {

    @Override
    protected String getUrlPath(String task) {
        return "/admin/tasks/" + task;
    }

    @Test
    public void testCollectionAutoDiscover() throws Exception {
        // discover collections
        when().post(getUrlPath("collection-auto-discover"))
                .then().statusCode(200);

        // GET collection
        when().get(arlasPath + "collections/" + DataSetTool.DATASET_INDEX_NAME + "-" + DataSetTool.DATASET_TYPE_NAME)
                .then().statusCode(200)
                .body("collection_name", equalTo(DataSetTool.DATASET_INDEX_NAME + "-" + DataSetTool.DATASET_TYPE_NAME))
                .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
                .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
                .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
                .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
                .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
                .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH))
                .body("params.custom_params.timestamp_format", equalTo(DataSetTool.DATASET_TIMESTAMP_FORMAT));

        // discover collections
        when().post(getUrlPath("collection-auto-discover"))
                .then().statusCode(200);

        // GET all collections
        getAllCollections(hasSize(1));

        // DELETE collection
        when().delete(arlasPath + "collections/" + DataSetTool.DATASET_INDEX_NAME + "-" + DataSetTool.DATASET_TYPE_NAME)
                .then().statusCode(200);
    }

    private void getAllCollections(Matcher matcher) throws InterruptedException {
        int cpt = 0;
        while (cpt > 0 && cpt < 5) {
            try {
                when().get(arlasPath + "collections/")
                        .then().statusCode(200)
                        .body("collection_name", matcher);
                cpt = -1;
            } catch (Exception e) {
                cpt++;
                Thread.sleep(1000);
            }
        }
    }
}
