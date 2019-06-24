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

package io.arlas.server.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Try;
import io.arlas.server.app.ArlasServerConfiguration;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;

public class TagKafkaProducer extends KafkaProducer<String, String> {

    private Logger LOGGER = LoggerFactory.getLogger(TagKafkaProducer.class);

    private static ObjectMapper jacksonMapper = new ObjectMapper();
    private String tagRefLogTopic;
    private String executeTagsTopic;

    public TagKafkaProducer(Properties properties, String tagRefLogTopic, String executeTagsTopic) {
        super(properties);
        this.tagRefLogTopic = tagRefLogTopic;
        this.executeTagsTopic = executeTagsTopic;
    }

    public Try<Void, Exception> sendToTagRefLog(Object object) {
        return send(tagRefLogTopic, object);
    }

    public Try<Void, Exception> sendToExecuteTags(Object object) {
        return send(executeTagsTopic, object);
    }

    private Try<Void, Exception> send(String topic, Object object) {

        LOGGER.debug("Sending to Kafka on topic " + topic);
        return Try.runWithCatch(() -> {

            this.send(new ProducerRecord<>(topic, jacksonMapper.writeValueAsString(object)),
                    (metadata, exception) -> {
                        if (metadata == null) {
                            throw new RuntimeException(exception);
                        }
                    });

        }, JsonProcessingException.class);
    }

    public static TagKafkaProducer build(ArlasServerConfiguration configuration) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.taggerConfiguration.bootstrapServers);
        kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new TagKafkaProducer(kafkaProperties,
                configuration.taggerConfiguration.tagRefLogTopic,
                configuration.taggerConfiguration.executeTagsTopic);
    }
}
