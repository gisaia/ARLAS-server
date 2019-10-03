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

package io.arlas.server.tests.rest.plugins.eo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.utils.ImageUtil;
import io.arlas.server.tests.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.hamcrest.Matchers;
import org.junit.*;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class TileServiceIT extends AbstractTestContext {
    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_tile";
    }

    @BeforeClass
    public static void beforeClass() throws JsonProcessingException, InterruptedException {
        AbstractTestWithCollection.beforeClass();

        ObjectMapper mapper = new ObjectMapper();
        Data data = new Data();
        int i=0,j=10;
        data.id = String.valueOf("ID_" + i + "_" + j + "DI_bottom").replace("-", "_");
        data.fullname = "My name is " + data.id;
        data.params.age = Math.abs(i * j);
        data.params.startdate = 1l * (i + 1000) * (j + 1000);
        data.params.stopdate = 1l * (i + 1000) * (j + 1000) + 100;
        data.geo_params.centroid = j + "," + i;
        List<LngLatAlt> coords = new ArrayList<>();
        String wktGeometry = "POLYGON ((";
        coords.add(new LngLatAlt(i - 1, j + 1));
        wktGeometry += (i - 1) + " " + (j + 1) + ",";
        coords.add(new LngLatAlt(i + 1, j + 1));
        wktGeometry += " " + (i + 1) + " " + (j + 1) + ",";
        coords.add(new LngLatAlt(i + 1, j - 1));
        wktGeometry += " " + (i + 1) + " " + (j - 1) + ",";
        coords.add(new LngLatAlt(i - 1, j - 1));
        wktGeometry += " " + (i - 1) + " " + (j - 1) + ",";
        coords.add(new LngLatAlt(i - 1, j + 1));
        wktGeometry += " " + (i - 1) + " " + (j + 1) + "))";

        data.geo_params.geometry = new Polygon(coords);
        data.geo_params.wktgeometry = wktGeometry;
        String indexName= DataSetTool.ALIASED_COLLECTION?DataSetTool.DATASET_INDEX_NAME+"_original":DataSetTool.DATASET_INDEX_NAME;
        DataSetTool.client.prepareIndex(indexName, DataSetTool.DATASET_TYPE_NAME, "ES_ID_TEST" + data.id)
                .setSource(mapper.writer().writeValueAsString(data), XContentType.JSON)
                .get();

        data.id = String.valueOf("ID_" + i + "_" + j + "DI_top").replace("-", "_");
        data.fullname = "My name is " + data.id;
        DataSetTool.client.prepareIndex(indexName, DataSetTool.DATASET_TYPE_NAME, "ES_ID_TEST" + data.id)
                .setSource(mapper.writer().writeValueAsString(data), XContentType.JSON)
                .get();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        AbstractTestWithCollection.afterClass();
    }

    @Test
    public void testEmptyTopPyramid() throws InterruptedException {
        // The top of the tile pyramid is empty
        Stream.iterate(0, n -> n + 1).limit(DataSetTool.DATASET_TILE_URL.minZ)
                .map(level ->
                        given()
                                .when()
                                .get(getUrlPath(CollectionTool.COLLECTION_NAME) + "/" + level + "/1/1.png")
                                .then()
                                .statusCode(Response.Status.NO_CONTENT.getStatusCode())
                );
    }

    @Test
    public void testTile() throws InterruptedException, IOException {
        // The top of the tile pyramid is empty => Coverage is around 50%
        BufferedImage image = ImageIO.read(given()
                .when()
                .get(getUrlPath(CollectionTool.COLLECTION_NAME) + "/10/511/484.png?f=id:eq:ID_0_10DI_bottom")
                .then()
                .statusCode(Response.Status.OK.getStatusCode()).extract().asInputStream());

        Assert.assertThat("image height is 256",
                image.getHeight(),
                Matchers.equalTo(256));
        Assert.assertThat("image width is 256",
                image.getWidth(),
                Matchers.equalTo(256));

        int coverage = ImageUtil.coverage(image,10);

        Assert.assertThat("coverage",
                coverage,
                greaterThan(40));

        Assert.assertThat("coverage",
                coverage,
                lessThan(60));

        // The bottom of the tile pyramid is empty => Coverage is around 50%
        image =ImageIO.read(given()
                .when()
                .get(getUrlPath(CollectionTool.COLLECTION_NAME) + "/10/511/484.png?f=id:eq:ID_0_10DI_top")
                .then()
                .statusCode(Response.Status.OK.getStatusCode()).extract().asInputStream());
                coverage = ImageUtil.coverage(image,10);

        Assert.assertThat("image height is 256",
                image.getHeight(),
                Matchers.equalTo(256));
        Assert.assertThat("image width is 256",
                image.getWidth(),
                Matchers.equalTo(256));

        Assert.assertThat("coverage",
                coverage,
                greaterThan(40));

        Assert.assertThat("coverage",
                coverage,
                lessThan(60));

        // The top and bottom are combined => Coverage is around 100%
        image =ImageIO.read(given()
                .when()
                .get(getUrlPath(CollectionTool.COLLECTION_NAME) + "/10/511/484.png?f=id:eq:ID_0_10DI_bottom,ID_0_10DI_top&coverage=70")
                .then()
                .statusCode(Response.Status.OK.getStatusCode()).extract().asInputStream());

        Assert.assertThat("image height is 256",
                image.getHeight(),
                Matchers.equalTo(256));
        Assert.assertThat("image width is 256",
                image.getWidth(),
                Matchers.equalTo(256));

        coverage = ImageUtil.coverage(image,10);

        Assert.assertThat("coverage",
                coverage,
                greaterThan(90));
    }
}
