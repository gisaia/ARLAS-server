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
package io.arlas.server.admin.health;

import com.codahale.metrics.health.HealthCheck;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;

public class ElasticsearchHealthCheck extends HealthCheck {

    private ElasticClient client;

    public ElasticsearchHealthCheck(ElasticClient client) {
        this.client = client;
    }

    @Override
    protected HealthCheck.Result check() {
        ResultBuilder resultBuilder = Result.builder();
        try {
            if (client.isClusterHealthRed()) {
                resultBuilder.unhealthy();
            } else {
                resultBuilder.healthy();
            }
            return resultBuilder.build();
        } catch (Exception e) {
            return Result.unhealthy("Cannot connect to elasticsearch cluster");
        }
    }
}
