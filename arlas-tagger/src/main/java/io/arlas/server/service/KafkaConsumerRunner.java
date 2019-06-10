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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.kafka.TagKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class KafkaConsumerRunner implements Runnable {
    Logger LOGGER = LoggerFactory.getLogger(TagRefService.class);

    private final ArlasServerConfiguration configuration;
    private final String topic;
    private final String consumerGroupId;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private KafkaConsumer consumer;
    protected static ObjectMapper MAPPER = new ObjectMapper();

    public KafkaConsumerRunner(ArlasServerConfiguration configuration, String topic, String consumerGroupId) {
        this.configuration = configuration;
        this.topic = topic;
        this.consumerGroupId = consumerGroupId;
    }

    public abstract void processRecords(ConsumerRecords<String, String> records);

    @Override
    public void run() {
        try {
            LOGGER.info("Starting consumer of topic " + topic);
            consumer = TagKafkaConsumer.build(configuration, topic, consumerGroupId);
            while (true) {

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100l));
                processRecords(records);
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        } finally {
            LOGGER.info("Closing consumer of topic " + topic);
            consumer.close();
        }
    }

    // Shutdown hook which can be called from a separate thread
    public void stop() {
        closed.set(true);
        consumer.wakeup();
    }
}
