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

package io.arlas.server.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.server.exceptions.ArlasConfigurationException;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.common.Strings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ArlasServerConfiguration extends Configuration {

    @JsonProperty("arlas-wfs")
    public WFSConfiguration wfsConfiguration;

    @JsonProperty("arlas-ogc")
    public OGCConfiguration ogcConfiguration;

    @JsonProperty("arlas-csw")
    public CSWConfiguration cswConfiguration;

    @JsonProperty("opensearch")
    public OpensearchConfiguration opensearchConfiguration;

    @JsonProperty("zipkin")
    public ZipkinFactory zipkinConfiguration;

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @JsonProperty("elastic-nodes")
    public String elasticnodes;

    @JsonProperty("elastic-host")
    public String elastichost;

    @JsonProperty("elastic-port")
    public Integer elasticport;

    @JsonProperty("elastic-sniffing")
    public Boolean elasticsniffing;

    @JsonProperty("elastic-cluster")
    public String elasticcluster;

    @JsonProperty("arlas-index")
    public String arlasindex;

    @JsonProperty("arlas-cache-size")
    public int arlascachesize;

    @JsonProperty("arlas-cache-timeout")
    public int arlascachetimeout;

    @JsonProperty("arlas-rest-cache-timeout")
    public int arlasrestcachetimeout;

    @JsonProperty("arlas-cors-enabled")
    public Boolean arlascorsenabled;

    @JsonProperty("arlas-service-collections-enabled")
    public Boolean arlasServiceCollectionsEnabled;

    @JsonProperty("arlas-service-explore-enabled")
    public Boolean arlasServiceExploreEnabled;

    @JsonProperty("arlas-service-wfs-enabled")
    public Boolean arlasServiceWFSEnabled;

    @JsonProperty("arlas-service-opensearch-enabled")
    public Boolean arlasServiceOPENSEARCHEnabled;

    @JsonProperty("arlas-service-csw-enabled")
    public Boolean arlasServiceCSWEnabled;

    @JsonProperty("arlas-service-tag-enabled")
    public Boolean arlasServiceTagEnabled;

    @JsonProperty("arlas-service-raster-tiles-enabled")
    public Boolean arlasServiceRasterTileEnabled;

    @JsonProperty("collection-auto-discover")
    public CollectionAutoDiscoverConfiguration collectionAutoDiscoverConfiguration;

    public static List<Pair<String,Integer>> getElasticNodes(String esNodes) {
        List<Pair<String,Integer>> elasticNodes = new ArrayList<>();
        if(!Strings.isNullOrEmpty(esNodes)) {
            String[] nodes = esNodes.split(",");
            for(String node : nodes) {
                String[] hostAndPort = node.split(":");
                if(hostAndPort.length == 2 && StringUtils.isNumeric(hostAndPort[1])) {
                    elasticNodes.add(new ImmutablePair<>(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
                }
            }
        }
        return elasticNodes;
    }

    public List<Pair<String,Integer>> getElasticNodes() {
        List<Pair<String,Integer>> elasticNodes = new ArrayList<>();
        if(!Strings.isNullOrEmpty(elasticnodes)) {
            elasticNodes.addAll(getElasticNodes(elasticnodes));
        } else if(!Strings.isNullOrEmpty(elastichost) && elasticport > 0) {
            elasticNodes.add(new ImmutablePair<>(elastichost, elasticport));
        }
        return elasticNodes;
    }

    public void check() throws ArlasConfigurationException {
        if (getElasticNodes().isEmpty()) {
            throw new ArlasConfigurationException("Elastic search configuration missing in config file.");
        }
        if (zipkinConfiguration == null) {
            throw new ArlasConfigurationException("Zipkin configuration missing in config file.");
        }
        if (swaggerBundleConfiguration == null) {
            throw new ArlasConfigurationException("Swagger configuration missing in config file.");
        }
        if (opensearchConfiguration != null && opensearchConfiguration.urlTemplatePrefix != null) {
            try {
                URI uri = new URI(opensearchConfiguration.urlTemplatePrefix);
            } catch (URISyntaxException e) {
                throw new ArlasConfigurationException("The url-template-prefix of Opensearch is invalid.");
            }
        }
        if (Strings.isNullOrEmpty(arlasindex)) {
            arlasindex = ".arlas";
        }
        if (arlascachesize < 0) {
            arlascachesize = 1000;
        }
        if (arlascachetimeout < 0) {
            arlascachetimeout = 60;
        }
        if (arlascorsenabled == null) {
            arlascorsenabled = false;
        }
        if (arlasServiceCollectionsEnabled == null) {
            arlasServiceCollectionsEnabled = true;
        }
        if (arlasServiceExploreEnabled == null) {
            arlasServiceExploreEnabled = true;
        }
        if (arlasServiceWFSEnabled == null) {
            arlasServiceWFSEnabled = false;
        }
        if (arlasServiceOPENSEARCHEnabled == null) {
            arlasServiceOPENSEARCHEnabled = true;
        }
        if (arlasServiceCSWEnabled == null) {
            arlasServiceCSWEnabled = false;
        }
        if(arlasServiceTagEnabled==null){
            arlasServiceTagEnabled=false;
        }
        if(arlasServiceRasterTileEnabled==null){
            arlasServiceRasterTileEnabled=false;
        }
        if(collectionAutoDiscoverConfiguration == null) {
            collectionAutoDiscoverConfiguration = new CollectionAutoDiscoverConfiguration();
            collectionAutoDiscoverConfiguration.schedule = 0;
        }
    }
}
