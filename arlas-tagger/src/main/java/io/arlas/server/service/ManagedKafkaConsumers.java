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

package io.arlas.server.service;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.kafka.TagKafkaProducer;
import io.arlas.server.services.UpdateServices;
import io.dropwizard.lifecycle.Managed;

public class ManagedKafkaConsumers implements Managed {
    private ArlasServerConfiguration configuration;
    private TagRefService tagRefService;
    private TagExecService tagExecService;

    public ManagedKafkaConsumers(ArlasServerConfiguration configuration, TagKafkaProducer tagKafkaProducer, UpdateServices updateServices) {
        this.configuration = configuration;
        this.tagRefService = new TagRefService(configuration,
                configuration.taggerConfiguration.tagRefLogTopic,
                configuration.taggerConfiguration.tagRefLogConsumerGroupId,
                tagKafkaProducer, updateServices);
        this.tagExecService = new TagExecService(configuration,
                configuration.taggerConfiguration.executeTagsTopic,
                configuration.taggerConfiguration.executeTagsConsumerGroupId,
                updateServices);
    }

    @Override
    public void start() throws Exception {
        new Thread(tagRefService).start();
        new Thread(tagExecService).start();

    }

    @Override
    public void stop() throws Exception {
        this.tagRefService.stop();
        this.tagExecService.stop();
    }
}
