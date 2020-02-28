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
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.sniff.ElasticsearchNodesSniffer;
import org.elasticsearch.client.sniff.NodesSniffer;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.joda.Joda;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ElasticTool {

    private static final String ES_DATE_TYPE = "date";
    private static final String ES_TYPE = "type";

    public static RestHighLevelClient getRestHighLevelClient(ElasticConfiguration conf) {
        // disable JVM default policies of caching positive hostname resolutions indefinitely
        // because the Elastic load balancer can change IP addresses
        java.security.Security.setProperty("networkaddress.cache.ttl" , "60");

        RestClientBuilder restClientBuilder = RestClient.builder(conf.getElasticNodes());
        if (conf.elasticSkipMaster) {
            restClientBuilder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        }

        // Authentication needed ?
        if (!StringUtil.isNullOrEmpty(conf.elasticCredentials)) {
            String[] credentials = conf.getCredentials();
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials[0], credentials[1]));

            restClientBuilder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        // Sniffing should be disabled with Elasticsearch Service (cloud)
        if (conf.elasticsniffing) {
            SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
            restClientBuilder.setFailureListener(sniffOnFailureListener);
            NodesSniffer nodesSniffer = new ElasticsearchNodesSniffer(
                    client.getLowLevelClient(),
                    ElasticsearchNodesSniffer.DEFAULT_SNIFF_REQUEST_TIMEOUT,
                    conf.elasticEnableSsl ? ElasticsearchNodesSniffer.Scheme.HTTPS : ElasticsearchNodesSniffer.Scheme.HTTP);
            Sniffer sniffer = Sniffer.builder(client.getLowLevelClient())
                    .setSniffAfterFailureDelayMillis(30000)
                    .setNodesSniffer(nodesSniffer).build();
            sniffOnFailureListener.setSniffer(sniffer);
        }

        return client;
    }

    public static CreateIndexResponse createArlasIndex(Client client, String arlasIndexName, String arlasMappingName, String arlasMappingFileName)  {
        CreateIndexResponse createIndexResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(ElasticTool.class.getClassLoader().getResourceAsStream(arlasMappingFileName)));
            CreateIndexRequest request = new CreateIndexRequest(arlasIndexName);
            request.mapping(arlasMappingName, arlasMapping, XContentType.JSON);
            createIndexResponse = client.admin().indices().create(request).actionGet();
        } catch (IOException e) {
            new InternalServerErrorException("Can not initialize the collection database", e);
        }
        return createIndexResponse;
    }

    public static AcknowledgedResponse putExtendedMapping(Client client, String arlasIndexName, String arlasMappingName, InputStream in) {
        AcknowledgedResponse putMappingResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(in));
            PutMappingRequest request = new PutMappingRequest(arlasIndexName);
            request.source(arlasMapping, XContentType.JSON);
            request.type(arlasMappingName);
            putMappingResponse = client.admin().indices().putMapping(request).actionGet();
        } catch (IOException e) {
            new InternalServerErrorException("Cannot update " + arlasIndexName + " mapping");
        }
        return putMappingResponse;
    }

    public static boolean checkIndexMappingFields(Client client, String index, String typeName, String... fields) throws ArlasException {
        GetFieldMappingsRequest request = new GetFieldMappingsRequest();
        request.indices(index);
        request.fields(fields);
        GetFieldMappingsResponse response = client.admin().indices().getFieldMappings(request).actionGet();
        for (String field : fields) {
            GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(index, typeName, field);
            if (data == null || data.isNull()) {
                throw new NotFoundException("Unable to find " + field + " from " + typeName + " in " + index + ".");
            }
        }
        return true;
    }

    public static boolean checkAliasMappingFields(Client client, String alias, String typeName, String... fields) throws ArlasException {
        List<String> indices = ElasticTool.getIndicesName(client, alias, typeName);
        for (String index : indices) { checkIndexMappingFields(client, index, typeName, fields); }
        return true;
    }

    public static List<String> getIndicesName(Client client, String alias, String typeName) throws ArlasException {
        GetMappingsResponse response;
        try {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(alias);
            response = client.admin().indices().getMappings(request).actionGet();
            if (response.getMappings().isEmpty()) {
                throw new NotFoundException("No types in " + alias + ".");
            }
        } catch (ArlasException e) {
            throw e;
        } catch (IndexNotFoundException e) {
            throw new NotFoundException("Index " + alias + " does not exist.");
        } catch (Exception e) {
            throw new NotFoundException("Unable to access " + typeName + " in " + alias + ".");
        }

        List<String> indices = IteratorUtils.toList(response.getMappings().keysIt());
        for (String index : indices) {
            //check type
            try {
                if (!response.getMappings().get(index).containsKey(typeName)) {
                    throw new NotFoundException("Type " + typeName + " does not exist in " + alias + ".");
                }
                Object properties = response.getMappings().get(index).get(typeName).sourceAsMap().get("properties");
                if (properties == null) {
                    throw new NotFoundException("Unable to find properties from " + typeName + " in " + index + ".");
                }
            } catch (Exception e) {
                throw new NotFoundException("Unable to get " + typeName + " in " + index + ".");
            }
        };
        return indices;
    }

    public static CollectionReference getCollectionReferenceFromES(Client client, String index, String type, ObjectReader reader, String ref) throws ArlasException {
        CollectionReference collection = new CollectionReference(ref);
        //Exclude old include_fields for support old collection
        GetRequest request = new GetRequest(index, ref);
        String[] includes = Strings.EMPTY_ARRAY;
        String[] excludes = new String[]{"include_fields"};
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        request.fetchSourceContext(fetchSourceContext);
        GetResponse hit = client.get(request).actionGet();
        String source = hit.getSourceAsString();
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

    public static boolean isDateField(String field, Client client, String index, String typeName) throws ArlasException {
        GetFieldMappingsResponse response;
        try {
            GetFieldMappingsRequest request = new GetFieldMappingsRequest();
            request.indices(index);
            request.fields(field);
            response = client.admin().indices().getFieldMappings(request).actionGet();
        } catch (IndexNotFoundException e) {
            throw new NotFoundException("Index " + index + " does not exist.");
        }
        String lastKey = field.substring(field.lastIndexOf(".") + 1);
        return response.mappings().keySet()
                .stream()
                .anyMatch(indexName -> {
                    GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(indexName, typeName, field);
                    boolean isFieldMetadaAMap = (data != null && !data.isNull() && data.sourceAsMap().get(lastKey) instanceof Map);
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
