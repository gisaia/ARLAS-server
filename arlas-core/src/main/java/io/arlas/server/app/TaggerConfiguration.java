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

public class TaggerConfiguration {
    @JsonProperty("status-timeout")
    public Long statusTimeout;

    @JsonProperty("kafka-batch-size")
    public Integer batchSize;

    @JsonProperty("kafka-bootstrap-servers")
    public String bootstrapServers;

    @JsonProperty("kafka-consumer-poll-timeout")
    public Long consumerPollTimeout;

    @JsonProperty("kafka-consumer-group-id-tagref-log")
    public String tagRefLogConsumerGroupId;

    @JsonProperty("kafka-consumer-group-id-execute-tags")
    public String executeTagsConsumerGroupId;

    @JsonProperty("kafka-topic-tagref-log")
    public String tagRefLogTopic;

    @JsonProperty("kafka-topic-execute-tags")
    public String executeTagsTopic;
}
