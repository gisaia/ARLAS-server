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

import io.arlas.server.app.ArlasServerConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

public class ESConnector {
    public static AdminClient adminClient;
    public static Client client;
    public static List<Pair<String,Integer>> esNodes;

    static Logger LOGGER = LoggerFactory.getLogger(ESConnector.class);

    static {
        try {
            Settings settings = null;
            esNodes = ArlasServerConfiguration.getElasticNodes(Optional.ofNullable(System.getenv("ARLAS_ELASTIC_NODES")).orElse("localhost:9300"));
            if ("localhost".equals(esNodes.get(0).getLeft())) {
                settings = Settings.EMPTY;
            } else {
                settings = Settings.builder().put("cluster.name", "docker-cluster").build();
            }
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(esNodes.get(0).getLeft()), esNodes.get(0).getRight()));
            adminClient = client.admin();

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected static void createIndex(String indexName, String mappingFileName, String typeName) {
       try {
           String mapping = IOUtils.toString(new InputStreamReader(ESConnector.class.getClassLoader().getResourceAsStream(mappingFileName)));
           try {
               adminClient.indices().prepareDelete(indexName).get();
           } catch (Exception e) {
           }
           adminClient.indices().prepareCreate(indexName).addMapping(typeName, mapping, XContentType.JSON).get();
       } catch (IOException e) {
           LOGGER.error(e.getMessage(), e);
       }
    }
}
