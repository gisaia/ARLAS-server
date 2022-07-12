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

package io.arlas.server.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.h3core.H3Core;
import io.arlas.server.core.app.ElasticConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.model.RasterTileURL;
import org.apache.logging.log4j.core.util.IOUtils;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataSetTool {

    static Logger LOGGER = LoggerFactory.getLogger(DataSetTool.class);

    public final static String DATASET_COLLECTION_DISPLAY_NAME="Test Collection";
    public final static String DATASET_INDEX_NAME="dataset";
    public final static String DATASET_ID_PATH="id";
    public final static String DATASET_ID_DESC = "object identifier";
    public final static String DATASET_GEO_PARAMS="geo_params";
    public final static String DATASET_GEOMETRY_PATH="geo_params.geometry";
    public final static String DATASET_GEOMETRY_DESC = "object geometry";
    public final static String DATASET_WKT_GEOMETRY_PATH="geo_params.wktgeometry";
    public final static String DATASET_CENTROID_PATH="geo_params.centroid";
    public final static String DATASET_CENTROID_DESC = "object centroid";
    public final static String DATASET_H3_PATH="geo_params.h3";
    public final static String DATASET_TIMESTAMP_PATH="params.startdate";
    public final static String DATASET_TIMESTAMP_DESC = "record timestamp";
    public final static String DATASET_EXCLUDE_FIELDS = "params.ci*";
    public final static String DATASET_EXCLUDE_WFS_FIELDS="params.country";
    public final static String DATASET_TIMESTAMP_FORMAT = "epoch_millis";
    public static final String DATASET_INSPIRE_LINEAGE = "Dataset loaded for testing";
    public static final String DATASET_INSPIRE_TOPIC_CATEGORY = "biota";
    public static final String DATASET_DUBLIN_CORE_TITLE = "geodata";
    public static final String DATASET_DUBLIN_CORE_DESCRIPTION = "geodata set for testing";
    public static final String DATASET_DUBLIN_CORE_LANGUAGE = "eng";
    public final static RasterTileURL DATASET_TILE_URL = new RasterTileURL(Optional.ofNullable(System.getenv("ARLAS_TILE_URL")).orElse("{id}/{z}/{x}/{y}.png"),5,18,true);
    public static final String[] jobs = {"Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter"};
    public static final String[] cities = {"Paris", "London", "New York", "Tokyo", "Toulouse", "Marseille", "Lyon", "Bordeaux", "Lille", "Albi", "Calais"};
    public static final String[] countries = {"Afghanistan",
            "Albania",
            "Algeria",
            "Andorra",
            "Angola",
            "Antigua",
            "Barbuda",
            "Argentina",
            "Armenia",
            "Aruba",
            "Australia",
            "Austria"
    };

    public static ElasticClient client;
    public static boolean ALIASED_COLLECTION;
    public static boolean WKT_GEOMETRIES;

    static {
        ElasticConfiguration conf = new ElasticConfiguration();
        conf.elasticnodes = Optional.ofNullable(System.getenv("ARLAS_ELASTIC_NODES")).orElse("localhost:9200");
        conf.elasticEnableSsl = false;
        client = new ElasticClient(conf);
        ALIASED_COLLECTION = Optional.ofNullable(System.getenv("ALIASED_COLLECTION")).orElse("false").equals("true");
        WKT_GEOMETRIES = false;
        LOGGER.info("Load data in " + conf.elasticnodes + " with ALIASED_COLLECTION=" + ALIASED_COLLECTION);
    }

    public static void main(String[] args) throws IOException, ArlasException {
        DataSetTool.loadDataSet();
    }

    public static void loadDataSet() throws IOException, ArlasException {
        if(!ALIASED_COLLECTION) {
            //Create a single index with all data
            createIndex(DATASET_INDEX_NAME,"dataset.mapping.json");
            fillIndex(DATASET_INDEX_NAME,-170,170,-80,80);
            LOGGER.info("Index created : " + DATASET_INDEX_NAME);
        } else {
            //Create 2 indices, split data between them and create an alias above these 2 indices
            createIndex(DATASET_INDEX_NAME+"_original","dataset.mapping.json");
            fillIndex(DATASET_INDEX_NAME+"_original",-170,0,-80,80);
            createIndex(DATASET_INDEX_NAME+"_alt","dataset.alternate.mapping.json");
            fillIndex(DATASET_INDEX_NAME+"_alt",10,170,-80,80);
            addAlias(DATASET_INDEX_NAME+"*", DATASET_INDEX_NAME);
            LOGGER.info("Indices created : " + DATASET_INDEX_NAME + "_original," + DATASET_INDEX_NAME + "_alt");
            LOGGER.info("Alias created : " + DATASET_INDEX_NAME);
        }
    }

    private static void createIndex(String indexName, String mappingFileName) throws IOException, ArlasException {
        try {
            client.deleteIndex(indexName);
        } catch (Exception ignored) {
        }
        client.getClient().indices().create(b -> b.index(indexName).withJson(new InputStreamReader(DataSetTool.class.getClassLoader().getResourceAsStream(mappingFileName))));
    }

    private static void addAlias(String index, String alias) throws ArlasException {
        client.aliasIndex(index, alias);
    }

    private static void fillIndex(String indexName, int lonMin, int lonMax, int latMin, int latMax) throws IOException, ArlasException {
        Data data;
        H3Core h3 = H3Core.newInstance();

        for (int i = lonMin; i <= lonMax; i += 10) {
            for (int j = latMin; j <= latMax; j += 10) {
                int i2 = i + 6;
                int j2 = j + 6;
                data = new Data();
                data.id = ("ID_" + i + "_" + j + "DI").replace("-", "_");
                data.fullname = "My name is " + data.id;
                data.params.age = Math.abs(i * j);
                data.params.startdate = (long) (i + 1000) * (j + 1000);
                if (data.params.startdate >= 1013600) {
                    data.params.weight = (i + 10) * (j + 10);
                }
                data.params.stopdate = (long) (i + 1000) * (j + 1000) + 100;
                data.geo_params.centroid = j + "," + i;
                for (int res = 0; res <= 15; res++) {
                    data.geo_params.h3.put(String.valueOf(res), h3.geoToH3Address(j, i, res));
                }
                data.geo_params.other_geopoint = j2 + "," + i2;
                data.params.job = jobs[((Math.abs(i) + Math.abs(j)) / 10) % (jobs.length - 1)];
                data.params.country = countries[((Math.abs(i) + Math.abs(j)) / 10) % (countries.length - 1)];
                data.params.city = cities[((Math.abs(i) + Math.abs(j)) / 10) % (cities.length - 1)];
                List<LngLatAlt> coords = new ArrayList<>();
                List<LngLatAlt> second_coords = new ArrayList<>();
                String wktGeometry = "POLYGON ((";
                coords.add(new LngLatAlt(i - 1, j + 1));
                second_coords.add(new LngLatAlt(i2 - 1, j2 + 1));
                wktGeometry += (i - 1) + " " + (j + 1) + ",";
                coords.add(new LngLatAlt(i + 1, j + 1));
                second_coords.add(new LngLatAlt(i2 + 1, j2 + 1));
                wktGeometry += " " + (i + 1) + " " + (j + 1) + ",";
                coords.add(new LngLatAlt(i + 1, j - 1));
                second_coords.add(new LngLatAlt(i2 + 1, j2 - 1));
                wktGeometry += " " + (i + 1) + " " + (j - 1) + ",";
                coords.add(new LngLatAlt(i - 1, j - 1));
                second_coords.add(new LngLatAlt(i2 - 1, j2 - 1));
                wktGeometry += " " + (i - 1) + " " + (j - 1) + ",";
                coords.add(new LngLatAlt(i - 1, j + 1));
                second_coords.add(new LngLatAlt(i2 - 1, j2 + 1));
                wktGeometry += " " + (i - 1) + " " + (j + 1) + "))";

                data.geo_params.geometry = new Polygon(coords);
                data.geo_params.second_geometry = new Polygon(second_coords);
                data.geo_params.wktgeometry = wktGeometry;
                client.index(indexName, "ES_ID_TEST" + data.id, data);
            }
        }
    }

    public static void clearDataSet() throws ArlasException {
        if (!ALIASED_COLLECTION) {
            client.deleteIndex(DATASET_INDEX_NAME);
        } else {
            client.deleteIndex(DATASET_INDEX_NAME + "_original");
            client.deleteIndex(DATASET_INDEX_NAME + "_alt");
        }
    }

}