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
package io.arlas.server.core.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.commons.utils.StringUtil;
import org.apache.http.HttpHost;

import java.util.Arrays;

public class ElasticConfiguration {
    @JsonProperty("elastic-nodes")
    public String elasticnodes;

    @JsonProperty("elastic-enable-ssl")
    public Boolean elasticEnableSsl;

    @JsonProperty("elastic-ignore-certs")
    public Boolean elasticIgnoreCerts = true;

    @JsonProperty("elastic-credentials")
    public String elasticCredentials;

    @JsonProperty("elastic-socket-timeout")
    public Integer elasticSocketTimeout;

    @JsonProperty("elastic-max-precision-threshold")
    public Integer elasticMaxPrecisionThreshold;

    public void check() throws ArlasConfigurationException {
        if (getElasticNodes().length == 0) {
            throw new ArlasConfigurationException("Elastic search configuration missing in config file.");
        }
        if (elasticEnableSsl == null) {
            elasticEnableSsl = false;
        }
    }

    public static String[] getCredentials(String elasticCredentials) {
        return elasticCredentials.contains(":") ?
                new String[]{ elasticCredentials.substring(0, elasticCredentials.indexOf(":")),
                        elasticCredentials.substring(elasticCredentials.indexOf(":") + 1) } :
                new String[]{ elasticCredentials, "" };

    }

    public HttpHost[] getElasticNodes() {
        if(!StringUtil.isNullOrEmpty(elasticnodes)) {
            return getElasticNodes(elasticnodes, elasticEnableSsl);
        }
        return new HttpHost[0];
    }

    public static HttpHost[] getElasticNodes(String esNodes, boolean enableSsl) {
        return Arrays.stream(esNodes.split(","))
                .map(e -> e.contains(":") ?
                        new HttpHost(e.substring(0, e.indexOf(":")), Integer.parseInt(e.substring(e.indexOf(":") + 1)), enableSsl ? "https" : "http") :
                        new HttpHost(e, enableSsl ? 9243 : 9200, enableSsl ? "https" : "http"))
                .toArray(HttpHost[]::new);
    }
}
