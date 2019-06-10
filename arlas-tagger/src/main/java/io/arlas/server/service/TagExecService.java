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
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotAllowedException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.TagRefRequest;
import io.arlas.server.model.TaggingStatus;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.response.UpdateResponse;
import io.arlas.server.services.UpdateServices;
import io.arlas.server.utils.ParamsParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class TagExecService extends KafkaConsumerRunner {
    private Logger LOGGER = LoggerFactory.getLogger(TagExecService.class);
    private UpdateServices updateServices;
    private TaggingStatus taggingStatus;


    public TagExecService(ArlasServerConfiguration configuration, String topic, String consumerGroupId, UpdateServices updateServices) {
        super(configuration, topic, consumerGroupId);
        this.updateServices = updateServices;
        this.taggingStatus = TaggingStatus.getInstance();
    }

    @Override
    public void processRecords(ConsumerRecords<String, String> records) {
        if (records.count() > 0) {
            LOGGER.debug("Kafka polling returned " + records.count());
        }
        for (ConsumerRecord<String, String> record : records) {

            try {
                final TagRefRequest tagRequest = MAPPER.readValue(record.value(), TagRefRequest.class);
                LOGGER.debug("Processing record " + tagRequest.toString());
                CollectionReference collectionReference = Optional
                        .ofNullable(updateServices.getDaoCollectionReference().getCollectionReference(tagRequest.collection))
                        .orElseThrow(() -> new NotFoundException(tagRequest.collection));
                Search searchHeader = new Search();
                searchHeader.filter = ParamsParser.getFilter(tagRequest.partitionFilter);
                MixedRequest request = new MixedRequest();
                request.basicRequest = tagRequest.search;
                request.headerRequest = searchHeader;
                UpdateResponse updateResponse = taggingStatus.getStatus(tagRequest.id).orElse(new UpdateResponse());
                updateResponse.id = tagRequest.id;
                updateResponse.progress = tagRequest.progress;
                updateResponse.action = tagRequest.action;
                switch (tagRequest.action) {
                    case ADD:
                        updateResponse.add(updateServices.tag(collectionReference, request, tagRequest.tag, Integer.MAX_VALUE));
                        break;
                    case REMOVE:
                        updateResponse.add(updateServices.unTag(collectionReference, request, tagRequest.tag, Integer.MAX_VALUE));
                        break;
                    case REMOVEALL:
                        updateResponse.add(updateServices.removeAll(collectionReference, request, tagRequest.tag, Integer.MAX_VALUE));
                        break;
                    default:
                        LOGGER.warn("Unknown action received in tag request: " + tagRequest.action);
                        break;
                }
                taggingStatus.updateStatus(tagRequest.id, updateResponse);
            } catch (IOException e) {
                LOGGER.warn("Could not parse record " + record.value());
            } catch (NotFoundException e) {
                LOGGER.warn("Could not get collection: " + record.value());
            } catch (NotAllowedException e) {
                LOGGER.warn("The path string is not part of the fields that can be tagged. " + record.value());
            } catch (InvalidParameterException e) {
                LOGGER.warn("Invalid parameters for request " + record.value());
            } catch (ArlasException e) {
                LOGGER.warn("Arlas exception for " + record.value(), e);
            }
        }
        LOGGER.debug("End of records processing");
    }
}
