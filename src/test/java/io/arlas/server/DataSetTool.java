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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

public class DataSetTool {
    public final static String DATASET_INDEX_NAME="dataset";
    public final static String DATASET_TYPE_NAME="mytype";
    public final static String DATASET_ID_PATH="id";
    public final static String DATASET_GEOMETRY_PATH="geo_params.geometry";
    public final static String DATASET_CENTROID_PATH="geo_params.centroid";
    public final static String DATASET_TIMESTAMP_PATH="params.startdate";
    public final static String DATASET_INCLUDE_FIELDS=null;
    public final static String DATASET_EXCLUDE_FIELDS=null;
    public final static String DATASET_TIMESTAMP_FORMAT="epoch_millis";
    public static final String[] jobs= {"Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter"};


    AdminClient adminClient;
    Client client;

    public static void main(String [] args) throws IOException {
        DataSetTool dst = DataSetTool.init(args[0], Integer.parseInt(args[1]));
        dst.clearDataSet();
        dst.loadDataSet();
    }

    static public DataSetTool init() throws UnknownHostException {
        return new DataSetTool(Optional.ofNullable(System.getenv("ARLAS_ELASTIC_HOST")).orElse("localhost"),
                Integer.parseInt(Optional.ofNullable(System.getenv("ARLAS_ELASTIC_PORT")).orElse("9300")));
    }

    static public DataSetTool init(String host, int port) throws UnknownHostException {
        return new DataSetTool(host, port);
    }

    private DataSetTool(String host, int port) throws UnknownHostException {
	Settings settings = null;
        if ("localhost".equals(host)){
            settings=Settings.EMPTY;
        }else{
            settings=Settings.builder().put("cluster.name", "docker-cluster").build();
        }
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        adminClient = client.admin();
    }

    public void loadDataSet() throws IOException {
        String mapping = IOUtils.toString(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("dataset.mapping.json")));
        try {
	    adminClient.indices().prepareDelete(DATASET_INDEX_NAME).get();
	} catch (Exception e) {
	}
        adminClient.indices().prepareCreate(DATASET_INDEX_NAME).addMapping(DATASET_TYPE_NAME, mapping).get();
        Data data;
        ObjectMapper mapper = new ObjectMapper();
        for(int i=-170; i<=170;i+=10){
            for(int j=-80; j<=80;j+=10){
                data=new Data();
                data.id= i+"-"+j;
                data.fullname="My name is "+data.id;
                data.params.startdate=1l*(i+1000)*(j+1000);
                data.geo_params.centroid=j+","+i;
                data.params.job=jobs[((Math.abs(i)+Math.abs(j))/10)%(jobs.length-1)];
                List<LngLatAlt> coords = new ArrayList<>();
                coords.add(new LngLatAlt(i-1,j+1));
                coords.add(new LngLatAlt(i+1,j+1));
                coords.add(new LngLatAlt(i+1,j-1));
                coords.add(new LngLatAlt(i-1,j-1));
                coords.add(new LngLatAlt(i-1,j+1));
                data.geo_params.geometry=new Polygon(coords);
                IndexResponse response = client.prepareIndex(DATASET_INDEX_NAME, DATASET_TYPE_NAME, data.id)
                        .setSource(mapper.writer().writeValueAsString(data))
                        .get();
            }
        }
    }

    public void clearDataSet(){
        adminClient.indices().prepareDelete(DATASET_INDEX_NAME).get();
    }
}
