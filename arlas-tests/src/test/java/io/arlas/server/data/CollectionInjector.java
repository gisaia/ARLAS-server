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

package io.arlas.server.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.AbstractTestContext;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.DublinCoreElementName;
import io.arlas.server.model.Inspire;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.MultiValueFilter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class CollectionInjector extends AbstractTestContext {

    //TODO refactor Elasticsearch implementation as a regular CollectionInjector sink
    public static final String POSTGIS_SINK = "postgis";

    public static String COLLECTION_NAME = "geodata";
    public static String COLLECTION_NAME_ACTOR = "geodata_actor";

    public static AbstractDataInjector getDataInjector(String sink) {
        switch(sink) {
            case POSTGIS_SINK:
                return new PostgisDataInjector();
            default:
                return null;
        }
    }

    public static void main(String[] args) throws IOException, ArlasException {
        // RUN this class to load POSTGIS dataset with following arguments
        switch (args[0]) {
            case "load":
                new CollectionInjector().load();
                break;
            case "loadcsw":
                new CollectionInjector().loadCsw(0);
                break;
            case "delete":
                new CollectionInjector().delete();
                break;
            case "deletecsw":
                new CollectionInjector().deleteCsw();
                break;
        }
    }

    @Test
    public  void load() throws ArlasException {
        this.load(0);
    }

    public  void load(long sleepAfter) throws ArlasException {

        try {
            getDataInjector(POSTGIS_SINK).loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        CollectionReferenceParameters params = new CollectionReferenceParameters();
        params.indexName = AbstractDataInjector.DATA_SINK_NAME;
        params.idPath = AbstractDataInjector.DATASET_ID_PATH;
        params.geometryPath = AbstractDataInjector.WKT_GEOMETRIES ? AbstractDataInjector.DATASET_WKT_GEOMETRY_PATH:AbstractDataInjector.DATASET_GEOMETRY_PATH;
        params.centroidPath = AbstractDataInjector.DATASET_CENTROID_PATH;
        params.timestampPath = AbstractDataInjector.DATASET_TIMESTAMP_PATH;
        params.excludeFields = AbstractDataInjector.DATASET_EXCLUDE_FIELDS;
        params.excludeWfsFields = AbstractDataInjector.DATASET_EXCLUDE_WFS_FIELDS;
        params.rasterTileURL = AbstractDataInjector.DATASET_TILE_URL;
        params.rasterTileWidth=256;
        params.rasterTileHeight=256;
        params.inspire = new Inspire();
        params.inspire.lineage = AbstractDataInjector.DATASET_INSPIRE_LINEAGE;
        params.inspire.topicCategories = Arrays.asList(AbstractDataInjector.DATASET_INSPIRE_TOPIC_CATEGORY);
        params.dublinCoreElementName = new DublinCoreElementName();
        params.dublinCoreElementName.title = AbstractDataInjector.DATASET_DUBLIN_CORE_TITLE;
        params.dublinCoreElementName.description = AbstractDataInjector.DATASET_DUBLIN_CORE_DESCRIPTION;
        params.dublinCoreElementName.language = AbstractDataInjector.DATASET_DUBLIN_CORE_LANGUAGE;


        // PUT new collection
        given().contentType("application/json").body(params).when().put(getUrlPath()).then().statusCode(200);
        Filter filter = new Filter();
        filter.f =Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, AbstractDataInjector.jobs[0])));
        params.filter =filter;
        given().contentType("application/json").body(params).when().put(arlasPath + "collections/" + COLLECTION_NAME_ACTOR).then().statusCode(200);

        try {
            Thread.sleep(sleepAfter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void loadCsw() throws IOException {
        this.loadCsw(0);
    }

    public  void loadCsw(long sleepAfter) throws IOException {
        try {
            getDataInjector(POSTGIS_SINK).loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArlasException e) {
            e.printStackTrace();
        }

        InputStreamReader dcelementForCollection = new InputStreamReader(CollectionInjector.class.getClassLoader().getResourceAsStream("csw.collection.dcelements.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        DublinCoreElementName[] dcelements = objectMapper.readValue(dcelementForCollection, DublinCoreElementName[].class);
        Arrays.asList(dcelements).forEach(dublinCoreElementName -> {
                    CollectionReferenceParameters params = new CollectionReferenceParameters();
                    params.indexName = AbstractDataInjector.DATA_SINK_NAME;
                    params.idPath = AbstractDataInjector.DATASET_ID_PATH;
                    params.geometryPath = AbstractDataInjector.DATASET_GEOMETRY_PATH;
                    params.centroidPath = AbstractDataInjector.DATASET_CENTROID_PATH;
                    params.timestampPath = AbstractDataInjector.DATASET_TIMESTAMP_PATH;
                    params.excludeFields = AbstractDataInjector.DATASET_EXCLUDE_FIELDS;
                    params.excludeWfsFields = AbstractDataInjector.DATASET_EXCLUDE_WFS_FIELDS;
                    params.rasterTileURL = AbstractDataInjector.DATASET_TILE_URL;
                    params.dublinCoreElementName=dublinCoreElementName;
                    params.inspire = new Inspire();
                    params.inspire.lineage = AbstractDataInjector.DATASET_INSPIRE_LINEAGE;
                    params.inspire.topicCategories = Arrays.asList(AbstractDataInjector.DATASET_INSPIRE_TOPIC_CATEGORY);
                    String url = arlasPath + "collections/" + dublinCoreElementName.title.split(" ")[0].toLowerCase();
                    // PUT new collection
                    given().contentType("application/json").body(params).when().put(url).then().statusCode(200);
                }
        );
        try {
            Thread.sleep(sleepAfter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  void delete() throws IOException, ArlasException {
        getDataInjector(POSTGIS_SINK).clearDataSet();
        //DELETE collection
        when().delete(getUrlPath()).then().statusCode(200);
        when().delete(arlasPath + "collections/" + COLLECTION_NAME_ACTOR).then().statusCode(200);
    }

    public  void deleteCsw() throws IOException, ArlasException {
        getDataInjector(POSTGIS_SINK).clearDataSet();
        InputStreamReader dcelementForCollection = new InputStreamReader(CollectionInjector.class.getClassLoader().getResourceAsStream("csw.collection.dcelements.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        DublinCoreElementName[] dcelements = objectMapper.readValue(dcelementForCollection, DublinCoreElementName[].class);
        Arrays.asList(dcelements).forEach(dublinCoreElementName -> {
                    String url = arlasPath + "collections/" + dublinCoreElementName.title.split(" ")[0].toLowerCase();
                    //DELETE collection
                    when().delete(url).then().statusCode(200);
                }
        );
    }

    @Override
    protected String getUrlPath(String collection) {
        return getUrlPath();
    }

    protected static String getUrlPath() {
        return arlasPath + "collections/" + COLLECTION_NAME;
    }
}
