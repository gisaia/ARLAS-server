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

package io.arlas.server.utils;

import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.server.app.ElasticConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.NodeSelector;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetFieldMappingsResponse;
import org.elasticsearch.client.sniff.ElasticsearchNodesSniffer;
import org.elasticsearch.client.sniff.NodesSniffer;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.joda.Joda;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ElasticTool {

    private static final String ES_DATE_TYPE = "date";
    private static final String ES_TYPE = "type";

    public static ImmutablePair<RestHighLevelClient, Sniffer> getRestHighLevelClient(ElasticConfiguration conf) {
        return ElasticTool.getRestHighLevelClient(conf.getElasticNodes(),
                conf.elasticEnableSsl,
                conf.elasticCredentials,
                conf.elasticSkipMaster,
                conf.elasticsniffing);
    }
    public static ImmutablePair<RestHighLevelClient, Sniffer> getRestHighLevelClient(HttpHost[] nodes,
                                                                            boolean ssl,
                                                                            String cred,
                                                                            boolean skipMaster,
                                                                            boolean sniffing) {
        // disable JVM default policies of caching positive hostname resolutions indefinitely
        // because the Elastic load balancer can change IP addresses
        java.security.Security.setProperty("networkaddress.cache.ttl" , "60");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl" , "0");

        RestClientBuilder restClientBuilder = RestClient.builder(nodes);
        if (skipMaster) {
            restClientBuilder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        }

        // Authentication needed ?
        if (!StringUtil.isNullOrEmpty(cred)) {
            String[] credentials = ElasticConfiguration.getCredentials(cred);
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials[0], credentials[1]));

            restClientBuilder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        // Sniffing should be disabled with Elasticsearch Service (cloud)
        Sniffer sniffer = null;
        if (sniffing) {
            SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
            restClientBuilder.setFailureListener(sniffOnFailureListener);
            NodesSniffer nodesSniffer = new ElasticsearchNodesSniffer(
                    client.getLowLevelClient(),
                    ElasticsearchNodesSniffer.DEFAULT_SNIFF_REQUEST_TIMEOUT,
                    ssl ? ElasticsearchNodesSniffer.Scheme.HTTPS : ElasticsearchNodesSniffer.Scheme.HTTP);
            sniffer = Sniffer.builder(client.getLowLevelClient())
                    .setSniffAfterFailureDelayMillis(30000)
                    .setNodesSniffer(nodesSniffer).build();
            sniffOnFailureListener.setSniffer(sniffer);
        }

        return new ImmutablePair<>(client, sniffer);
    }

    public static CreateIndexResponse createArlasIndex(ElasticClient client,
                                                       String arlasIndexName,
                                                       String arlasMappingFileName) throws ArlasException {
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(ElasticTool.class.getClassLoader().getResourceAsStream(arlasMappingFileName)));
            return client.createIndex(arlasIndexName, arlasMapping);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public static AcknowledgedResponse putExtendedMapping(ElasticClient client,
                                                          String arlasIndexName,
                                                          InputStream in) throws ArlasException {
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(in));
            return client.putMapping(arlasIndexName, arlasMapping);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public static boolean checkIndexMappingFields(ElasticClient client,
                                                  String index,
                                                  String... fields) throws ArlasException {
        GetFieldMappingsResponse response = client.getFieldMapping(index, fields);
        for (String field : fields) {
            GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(index, field);
            if (data == null || data.sourceAsMap().isEmpty()) {
                throw new NotFoundException("Unable to find `" + field + "` field in `" + index + "` index.");
            }
        }
        return true;
    }

    public static boolean checkAliasMappingFields(ElasticClient client,
                                                  String alias,
                                                  String... fields) throws ArlasException {
        List<String> indices = ElasticTool.getIndicesName(client, alias);
        for (String index : indices) { checkIndexMappingFields(client, index, fields); }
        return true;
    }

    public static List<String> getIndicesName(ElasticClient client, String alias) throws ArlasException {
        Map<String, LinkedHashMap> response = client.getMappings(alias);

        List<String> indices = IteratorUtils.toList(response.keySet().iterator());
        for (String index : indices) {
            Object properties = response.get(index);
            if (properties == null) {
                throw new NotFoundException("Unable to find properties in " + index + ".");
            }
        }
        return indices;
    }

    public static CollectionReference getCollectionReferenceFromES(ElasticClient client, String index, ObjectReader reader, String ref) throws ArlasException, IOException {
        CollectionReference collection = new CollectionReference(ref);
        //Exclude old include_fields for support old collection
        String[] includes = Strings.EMPTY_ARRAY;
        String[] excludes = new String[]{"include_fields"};

        String source = client.getHit(index, ref, includes, excludes);
        if (source != null) {
            try {
                collection.params = reader.readValue(source);
            } catch (IOException e) {
                throw new InternalServerErrorException("Can not fetch collection " + ref, e);
            }
        } else {
            throw new NotFoundException("Collection " + ref + " not found.");
        }
        return collection;
    }

    public static boolean isDateField(String field, ElasticClient client, String index) throws ArlasException {
        GetFieldMappingsResponse response = client.getFieldMapping(index, field);

        String lastKey = field.substring(field.lastIndexOf(".") + 1);
        return response.mappings().keySet()
                .stream()
                .anyMatch(indexName -> {
                    GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(indexName, field);
                    boolean isFieldMetadaAMap = (data != null && data.sourceAsMap().get(lastKey) instanceof Map);
                    if (isFieldMetadaAMap) {
                        return Optional.of(((Map)data.sourceAsMap().get(lastKey)))
                                .map(m -> m.get(ES_TYPE))
                                .map(Object::toString)
                                .filter(t -> t.equals(ES_DATE_TYPE))
                                .isPresent();
                    } else {
                        // TODO : check if there is another way to fetch field type in this case
                        return false;
                    }
                });
    }

    public static Joda.EpochTimeParser getElasticEpochTimeParser(boolean isMilliSecond) {
        return new Joda.EpochTimeParser(BooleanUtils.isTrue(isMilliSecond));
    }

    public static Joda.EpochTimePrinter getElasticEpochTimePrinter(boolean isMilliSecond) {
        return new Joda.EpochTimePrinter(BooleanUtils.isTrue(isMilliSecond));
    }
}
