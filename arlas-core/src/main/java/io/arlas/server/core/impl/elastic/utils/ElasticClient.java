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

package io.arlas.server.core.impl.elastic.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.GetFieldMappingResponse;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_field_mapping.TypeFieldMappings;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.BadRequestException;
import io.arlas.commons.exceptions.InternalServerErrorException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.ElasticConfiguration;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.utils.CollectionUtil;
import jakarta.json.stream.JsonGenerator;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.arlas.server.core.model.CollectionReference.INCLUDE_FIELDS;

@SuppressWarnings({"rawtypes"})
public class ElasticClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticClient.class);

    private final ElasticsearchClient client;
    private final JacksonJsonpMapper mapper;

    public ElasticClient(ElasticConfiguration configuration) {
        // disable JVM default policies of caching positive hostname resolutions indefinitely
        // because the Elastic load balancer can change IP addresses
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");

        // Create the low-level client
        RestClientBuilder restClientBuilder = RestClient.builder(configuration.getElasticNodes());

        // Authentication needed ?
        if (!StringUtil.isNullOrEmpty(configuration.elasticCredentials)) {
            String[] credentials = ElasticConfiguration.getCredentials(configuration.elasticCredentials);
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials[0], credentials[1]));

            restClientBuilder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        mapper = new JacksonJsonpMapper();
        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClientBuilder.build(), mapper);

        // And create the API client
        client = new ElasticsearchClient(transport);
    }

    public ElasticsearchClient getClient() {
        return client;
    }

    public boolean isClusterHealthRed() throws ArlasException {
        try {
            return client.cluster().health().status() == HealthStatus.Red;
        } catch (IOException e) {
            processException(e, "");
            return false;
        }
    }

    public void createIndex(String index, InputStream mapping) throws ArlasException {
        try {
            client.indices().create(b -> b.index(index).withJson(mapping));
        } catch (IOException e) {
            processException(e, index);
        }
    }

    public void aliasIndex(String index, String alias) throws ArlasException {
        try {
            client.indices().putAlias(b -> b.index(index).name(alias));
        } catch (IOException e) {
            processException(e, index);
        }
    }

    public boolean indexExists(String index) throws ArlasException {
        try {
            return client.indices().exists(b -> b.index(index)).value();
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
            LOGGER.warn("Exception while communicating with ES: " + e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage());
        } catch (IOException e) {
            LOGGER.warn("Exception while communicating with ES: " + e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public Map<String, Map<String, Object>> getMappings() throws ArlasException {
        return getMappings(null);
    }

    private Map<String, Object> toMap(Map<String, Property> properties) {
        final Map<String, Object> mapping = new HashMap<>();
        properties.forEach((field, props) -> {
            StringWriter writer = new StringWriter();
            try (JsonGenerator generator = mapper.jsonProvider().createGenerator(writer)) {
                mapper.serialize(props, generator);
            }
            try {
                mapping.put(field, mapper.objectMapper().readValue(writer.toString(), Map.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return mapping;
    }

    public Map<String, Map<String, Object>> getMappings(String index) throws ArlasException {
        try {
            final Map<String, Map<String, Object>> res = new HashMap<>();
            if (index != null) {
                client.indices()
                        .getMapping(b -> b.index(index))
                        .result()
                        .forEach((_index, _record) -> res.put(_index, toMap(_record.mappings().properties())));
            } else {
                client.indices()
                        .getMapping()
                        .result()
                        .forEach((_index, _record) -> res.put(_index, toMap(_record.mappings().properties())));
            }


            if (res.isEmpty()) {
                client.indices().getIndexTemplate()
                        .indexTemplates().forEach(tpl -> {
                            // verify if template's patterns match given index
                            // only support ending wildcard for pattern matching
                            AtomicReference<Boolean> matchIndex = new AtomicReference<>(false);
                            tpl.indexTemplate().indexPatterns().forEach(pattern -> {
                                if (CollectionUtil.matches(pattern, index))
                                    matchIndex.set(true);
                            });
                            // if true, add associated template's mappings
                            if (matchIndex.get()) {
                                res.put(tpl.name(), toMap(tpl.indexTemplate().template().mappings().properties()));
                            }
                        });
            }
            return res;
        } catch (IOException | ElasticsearchException e) {
            processException(e, index);
            return null;
        }
    }

    public void putMapping(String index, InputStream mapping) throws ArlasException {
        try {
            client.indices().putMapping(b -> b.index(index).withJson(mapping));
        } catch (IOException e) {
            processException(e, index);
        }
    }

    public GetFieldMappingResponse getFieldMapping(String index, String... fields) throws ArlasException {
        try {
            return client.indices().getFieldMapping(b -> b.index(index).fields(Arrays.asList(fields)));
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }
    public SearchResponse<Map> search(SearchRequest request) throws ArlasException {
        return search(request, Map.class);
    }

    public <T> SearchResponse<T> search(SearchRequest request, Class<T> cl) throws ArlasException {
        try {
            LOGGER.debug("REQUEST  : " + request.toString());
            SearchResponse<T> response = client.search(request, cl);
            LOGGER.debug("RESPONSE : " + response.toString());
            return response;
        } catch (IOException e) {
            processException(e, Arrays.toString(request.index().toArray()));
            return null;
        } catch (ElasticsearchException e) {
            String msg = e.getMessage();
            Throwable[] suppressed = e.getSuppressed();
            if (suppressed.length > 0 && suppressed[0] instanceof ResponseException) {
                msg = suppressed[0].getMessage();
            }
            throw new BadRequestException(msg);
        }
    }

    public DeleteResponse deleteDocument(String index, String ref) throws ArlasException {
        try {
            return client.delete(b -> b.index(index).id(ref));
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public void deleteIndex(String index) throws ArlasException {
        try {
            client.indices().delete(b -> b.index(index));
        } catch (IOException e) {
            processException(e, index);
        }
    }

    public IndexResponse index(String index, String id, Object source) throws ArlasException {
        try {
            return client.index(b -> b.index(index).id(id).document(source));
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public boolean isDateField(String field, String index) throws ArlasException {
        String lastKey = field.substring(field.lastIndexOf(".") + 1);
        GetFieldMappingResponse response = getFieldMapping(index, field);
        return response.result().keySet()
                .stream()
                .anyMatch(indexName -> {
                    TypeFieldMappings data = response.result().get(indexName);
                    return data != null && data.mappings().get(field).mapping().get(lastKey).isDate();
                });
    }

    public CollectionReference getCollectionReferenceFromES(String index, String ref) throws ArlasException {
        CollectionReference collection = new CollectionReference(ref);

        try {
            GetResponse<CollectionReferenceParameters> cr = client.get(b -> b
                            .index(index)
                            .id(ref)
                            .source(s -> s.fetch(true))
                            //Exclude old include_fields for support old collection
                            .sourceExcludes(INCLUDE_FIELDS),
                    CollectionReferenceParameters.class);
            if (cr.found()) {
                collection.params = cr.source();
                LOGGER.debug("****** getCollectionReferenceFromES cr=" + collection.params);
            } else {
                throw new NotFoundException("Collection " + ref + " not found.");
            }
        } catch (IOException e) {
            throw new InternalServerErrorException("Can not fetch collection " + ref, e);
        }
        return collection;
    }

    private void processException(Exception e, String index) throws ArlasException {
        if (e instanceof ResponseException) {
            if (((ResponseException) e).getResponse().getStatusLine().getStatusCode() == 404) {
                throw new NotFoundException("Index " + index + " does not exist.");
            }
        }
        LOGGER.warn("Exception while communicating with ES: " + e.getMessage(), e);
        throw new InternalServerErrorException(e.getMessage());
    }

}
