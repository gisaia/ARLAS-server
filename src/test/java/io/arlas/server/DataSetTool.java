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

package io.arlas.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSetTool {
    static Logger LOGGER = LoggerFactory.getLogger(DataSetTool.class);

    public final static String DATASET_INDEX_NAME = "dataset";
    public final static String DATASET_TYPE_NAME = "mytype";
    public final static String DATASET_ID_PATH = "id";
    public final static String DATASET_GEOMETRY_PATH = "geo_params.geometry";
    public final static String DATASET_CENTROID_PATH = "geo_params.centroid";
    public final static String DATASET_TIMESTAMP_PATH = "params.startdate";
    public final static String DATASET_INCLUDE_FIELDS = null;
    public final static String DATASET_EXCLUDE_FIELDS = "params.city";
    public final static String DATASET_EXCLUDE_WFS_FIELDS = "params.country";;
    public final static String DATASET_TIMESTAMP_FORMAT = "epoch_millis";
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

    public static Object jsonSchema;
    public static AdminClient adminClient;
    public static Client client;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonSchema = mapper.readValue(DataSetTool.class.getClassLoader().getResourceAsStream("dataset.schema.json"), ObjectNode.class);
            Settings settings = null;
            String host = Optional.ofNullable(System.getenv("ARLAS_ELASTIC_HOST")).orElse("localhost");
            Integer port = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_ELASTIC_PORT")).orElse("9300"));
            if ("localhost".equals(host)) {
                settings = Settings.EMPTY;
            } else {
                settings = Settings.builder().put("cluster.name", "docker-cluster").build();
            }
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
            adminClient = client.admin();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws IOException {
        DataSetTool.loadDataSet();
    }

    public static void loadDataSet() throws IOException {
        String mapping = IOUtils.toString(new InputStreamReader(DataSetTool.class.getClassLoader().getResourceAsStream("dataset.mapping.json")));
        try {
            adminClient.indices().prepareDelete(DATASET_INDEX_NAME).get();
        } catch (Exception e) {
        }
        adminClient.indices().prepareCreate(DATASET_INDEX_NAME).addMapping(DATASET_TYPE_NAME, mapping).get();
        Data data;
        ObjectMapper mapper = new ObjectMapper();
        for (int i = -170; i <= 170; i += 10) {
            for (int j = -80; j <= 80; j += 10) {
                data = new Data();
                data.id = String.valueOf("ID_" + i + "_" + j + "DI").replace("-", "_");
                data.fullname = "My name is " + data.id;
                data.params.age = Math.abs(i * j);
                data.params.startdate = 1l * (i + 1000) * (j + 1000);
                data.geo_params.centroid = j + "," + i;
                data.params.job = jobs[((Math.abs(i) + Math.abs(j)) / 10) % (jobs.length - 1)];
                data.params.country = countries[((Math.abs(i) + Math.abs(j)) / 10) % (countries.length - 1)];
                data.params.city = cities[((Math.abs(i) + Math.abs(j)) / 10) % (cities.length - 1)];
                List<LngLatAlt> coords = new ArrayList<>();
                coords.add(new LngLatAlt(i - 1, j + 1));
                coords.add(new LngLatAlt(i + 1, j + 1));
                coords.add(new LngLatAlt(i + 1, j - 1));
                coords.add(new LngLatAlt(i - 1, j - 1));
                coords.add(new LngLatAlt(i - 1, j + 1));
                data.geo_params.geometry = new Polygon(coords);
                IndexResponse response = client.prepareIndex(DATASET_INDEX_NAME, DATASET_TYPE_NAME, "ES_ID_TEST" + data.id)
                        .setSource(mapper.writer().writeValueAsString(data))
                        .get();
            }
        }
    }

    public static void clearDataSet() {
        adminClient.indices().prepareDelete(DATASET_INDEX_NAME).get();
    }
}
