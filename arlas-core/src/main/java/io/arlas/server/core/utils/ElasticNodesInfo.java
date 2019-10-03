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

package io.arlas.server.core.utils;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticNodesInfo {
    static Logger LOGGER = LoggerFactory.getLogger(ElasticNodesInfo.class);

    public static void printNodesInfo(Client client, PreBuiltTransportClient transportClient) {
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
        nodesInfoRequest.clear().jvm(false).os(false).process(true);
        ActionFuture<NodesInfoResponse> nodesInfoResponseActionFuture = client.admin().cluster().nodesInfo(nodesInfoRequest);
        LOGGER.info("Number of  Node : ".concat(String.valueOf(nodesInfoResponseActionFuture.actionGet().getNodes().size())));
        nodesInfoResponseActionFuture.actionGet().getNodes().forEach(nodeInfo -> {
            DiscoveryNode node = nodeInfo.getNode();
            LOGGER.info("Node Name : ".concat(node.getName()));
            LOGGER.info("Node Id : ".concat(node.getId()));
            LOGGER.info("Node EphemeralId : ".concat(node.getEphemeralId()));
            LOGGER.info("Node Host address : ".concat(node.getHostAddress()));
            LOGGER.info("Node Host name : ".concat(node.getHostName()));
            LOGGER.info("Node Transport address : ".concat(node.getAddress().getAddress()));
            LOGGER.info("Node role : ".concat(node.getRoles().toString()));
        });
        LOGGER.info("Number of Connected Node : ".concat(String.valueOf(transportClient.connectedNodes().size())));
        transportClient.connectedNodes().forEach(node -> {
            LOGGER.info("Connected Name : ".concat(node.getName()));
            LOGGER.info("Connected Id : ".concat(node.getId()));
            LOGGER.info("Connected EphemeralId : ".concat(node.getEphemeralId()));
            LOGGER.info("Connected Host address : ".concat(node.getHostAddress()));
            LOGGER.info("Connected Host name : ".concat(node.getHostName()));
            LOGGER.info("Connected Transport address : ".concat(node.getAddress().getAddress()));
            LOGGER.info("Connected role : ".concat(node.getRoles().toString()));
        });

    }
}
