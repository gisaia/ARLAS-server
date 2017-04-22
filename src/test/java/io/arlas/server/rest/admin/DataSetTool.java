package io.arlas.server.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataSetTool {
    public final static String DATASET_INDEX_NAME="dataset";
    public final static String DATASET_TYPE_NAME="mytype";
    public final static String DATASET_ID_PATH="id";
    public final static String DATASET_GEOMETRY_PATH="geometry";
    public final static String DATASET_CENTROID_PATH="centroid";
    public final static String DATASET_TIMESTAMP_PATH="startdate";
    public static final String[] jobs= {"Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter"};


    AdminClient adminClient;
    TransportClient client;

    public static void main(String [] args) throws IOException {
        DataSetTool dst = DataSetTool.init(args[0], Integer.parseInt(args[1]));
        dst.loadDataSet();
    }

    static public DataSetTool init() throws UnknownHostException {
        return new DataSetTool(System.getenv("ELASTICSEARCH_PORT_9300_TCP_ADDR"), Integer.parseInt(System.getenv("ELASTICSEARCH_PORT_9300_TCP_PORT")));
    }

    static public DataSetTool init(String host, int port) throws UnknownHostException {
        return new DataSetTool(host, port);
    }

    private DataSetTool(String host, int port) throws UnknownHostException {
	Settings settings = null;
        if("localhost".equals(host)){
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
        for(int i=-80; i<80;i+=10){
            for(int j=-170; j<170;j+=10){
                data=new Data();
                data.id= UUID.randomUUID().toString();
                data.fullname="My name is "+data.id;
                data.startdate=System.currentTimeMillis();
                data.centroid=i+","+j;
                data.job=jobs[i%(jobs.length-1)];
                List<LngLatAlt> coords = new ArrayList<>();
                coords.add(new LngLatAlt(i,j));
                coords.add(new LngLatAlt(i,j-1));
                coords.add(new LngLatAlt(i+1,j-1));
                coords.add(new LngLatAlt(i+1,j));
                coords.add(new LngLatAlt(i,j));
                data.geometry=new Polygon(coords);
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
