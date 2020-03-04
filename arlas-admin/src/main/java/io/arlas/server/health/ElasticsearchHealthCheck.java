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
package io.arlas.server.health;

import com.codahale.metrics.health.HealthCheck;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.utils.ElasticClient;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

public class ElasticsearchHealthCheck extends HealthCheck {

    private ElasticAdmin admin;
    private ElasticClient client;
    public ElasticsearchHealthCheck(ElasticClient client) {
        this.admin = new ElasticAdmin(client);
        this.client =client;
    }

    @Override
    protected HealthCheck.Result check() throws ArlasException {
        ResultBuilder resultBuilder = Result.builder();
        if (checkElasticsearch()) {
            //Not yet implemented in RestHighLevelClient
//            NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
//            nodesInfoRequest.clear().jvm(false).os(false).process(true);
//            ActionFuture<NodesInfoResponse> nodesInfoResponseActionFuture = client.admin().cluster().nodesInfo(nodesInfoRequest);
            ClusterHealthResponse response = client.health();
            if (response.getStatus() != ClusterHealthStatus.RED) {
                resultBuilder.healthy();
            }
//            resultBuilder.withDetail("nodes",nodesInfoResponseActionFuture.actionGet().getNodes());
//            if(client instanceof TransportClient){
//                resultBuilder.withDetail("connected_nodes",((TransportClient)this.client).connectedNodes());
//            }
            return resultBuilder.build();
        } else {
            return Result.unhealthy("Cannot connect to elasticsearch cluster");
        }
    }

    private boolean checkElasticsearch() {
        boolean ret = true;
        try {
            admin.getAllIndicesAsCollections();
        } catch (Exception e) {
            ret = false;
        }
        return ret;
    }
}
