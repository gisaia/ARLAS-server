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
import io.arlas.server.kafka.TagKafkaProducer;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.TagRefRequest;
import io.arlas.server.model.TaggingStatus;
import io.arlas.server.model.enumerations.AggregationTypeEnum;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.UpdateResponse;
import io.arlas.server.services.UpdateServices;
import io.arlas.server.utils.ParamsParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


public class TagRefService extends KafkaConsumerRunner {
    private Logger LOGGER = LoggerFactory.getLogger(TagRefService.class);
    private final TagKafkaProducer tagKafkaProducer;
    private final UpdateServices updateServices;
    private TaggingStatus taggingStatus;
    private Long statusTimeout;

    public TagRefService(ArlasServerConfiguration configuration, String topic, String consumerGroupId,
                         TagKafkaProducer tagKafkaProducer, UpdateServices updateServices) {
        super(configuration, topic, consumerGroupId);
        this.tagKafkaProducer = tagKafkaProducer;
        this.updateServices = updateServices;
        this.taggingStatus = TaggingStatus.getInstance();
        this.statusTimeout = configuration.taggerConfiguration.statusTimeout;
    }

    @Override
    public void processRecords(ConsumerRecords<String, String> records) {
        if (records.count() > 0) {
            LOGGER.debug("Kafka polling returned " + records.count());
        }

        for (ConsumerRecord<String, String> record : records) {

            try {
                final TagRefRequest tagRequest = MAPPER.readValue(record.value(), TagRefRequest.class);
                if (tagRequest.propagation == null) {
                    LOGGER.debug("No propagation requested: " + record.value());
                    tagKafkaProducer.sendToExecuteTags(tagRequest);
                } else {
                    LOGGER.debug("Propagation requested: " + record.value());
                    AggregationResponse aggregationResponse = getArlasAggregation(tagRequest);
                    int nbResult = aggregationResponse.elements.size();
                    UpdateResponse updateResponse = taggingStatus.getStatus(tagRequest.id).orElse(new UpdateResponse());
                    updateResponse.id = tagRequest.id;
                    updateResponse.action = tagRequest.action;
                    updateResponse.propagated = nbResult;
                    for (int i = 0; i < nbResult; i++) {
                        AggregationResponse a = aggregationResponse.elements.get(i);
                        Filter filter = Optional.ofNullable(tagRequest.propagation.filter).orElse(new Filter());

                        MultiValueFilter<Expression> expression =
                                new MultiValueFilter<>(new Expression(tagRequest.propagation.field,
                                        OperatorEnum.eq, a.key.toString()));
                        if (filter.f != null) {
                            filter.f.add(expression);
                        } else {
                            filter.f = Arrays.asList(expression);
                        }

                        Search search = new Search();
                        search.filter = filter;
                        tagKafkaProducer.sendToExecuteTags(TagRefRequest.fromTagRefRequest(tagRequest, search, (int)(i+1)*100/nbResult));
                        taggingStatus.updateStatus(tagRequest.id, updateResponse, statusTimeout);

                    }
                }
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

    private AggregationResponse getArlasAggregation(final TagRefRequest tagRequest) throws ArlasException, IOException {
        CollectionReference collectionReference = Optional
                .ofNullable(updateServices.getDaoCollectionReference().getCollectionReference(tagRequest.collection))
                .orElseThrow(() -> new NotFoundException(tagRequest.collection));

        Aggregation aggregation = new Aggregation();
        aggregation.type = AggregationTypeEnum.term;
        aggregation.field = tagRequest.propagation.field;
        aggregation.size = "10000";
        AggregationsRequest aggregationsRequest = new AggregationsRequest();
        aggregationsRequest.filter = tagRequest.search.filter;
        aggregationsRequest.aggregations = new ArrayList<>(Arrays.asList(aggregation));

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(tagRequest.partitionFilter);

        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        request.headerRequest = aggregationsRequestHeader;

        SearchResponse response = updateServices.aggregate(request, collectionReference, false);
        MultiBucketsAggregation mbAggregation = (MultiBucketsAggregation) response.getAggregations().asList().get(0);

        AggregationResponse aggregationResponse = new AggregationResponse();
        aggregationResponse.totalnb = response.getHits().getTotalHits();
        aggregationResponse = updateServices.formatAggregationResult(mbAggregation, aggregationResponse, collectionReference.collectionName);
        return aggregationResponse;
    }
}
