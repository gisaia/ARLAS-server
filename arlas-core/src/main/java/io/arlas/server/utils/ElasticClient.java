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

import io.arlas.server.app.ElasticConfiguration;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.Link;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.client.sniff.Sniffer;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ElasticClient {
    private static Logger LOGGER = LoggerFactory.getLogger(ElasticAdmin.class);

    private RestHighLevelClient client;
    private Sniffer sniffer;

    public ElasticClient(ElasticConfiguration configuration) {
        ImmutablePair<RestHighLevelClient, Sniffer> pair = ElasticTool.getRestHighLevelClient(configuration);
        this.client = pair.getLeft();
        this.sniffer = pair.getRight();
    }

    public ElasticClient(RestHighLevelClient client) {
        this(client, null);
    }

    public ElasticClient(RestHighLevelClient client, Sniffer sniffer) {
        this.client = client;
        this.sniffer = sniffer;
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public ClusterHealthResponse health() throws ArlasException {
        try {
            return client.cluster().health(new ClusterHealthRequest(), RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, "");
            return null;
        }
    }

    public CreateIndexResponse createIndex(String index, String mapping) throws ArlasException {
        try {
            CreateIndexRequest request = new CreateIndexRequest(index);
            request.mapping(mapping, XContentType.JSON);
            return client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public AcknowledgedResponse aliasIndex(String index, String alias) throws ArlasException {
        try {
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(index)
                            .alias(alias);
            request.addAliasAction(aliasAction);
            return client.indices().updateAliases(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public boolean indexExists(String index) throws ArlasException {
        try {
            return client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
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

    public Map<String, LinkedHashMap> getMappings() throws ArlasException {
        return getMappings(null);

    }

    public Map<String, LinkedHashMap> getMappings(String index) throws ArlasException {
        try {
            GetMappingsRequest request = new GetMappingsRequest();
            if (index != null) {
                request.indices(index);
            }
            final Map<String, LinkedHashMap> res = new HashMap<>();
            client.indices().getMapping(request, RequestOptions.DEFAULT).mappings()
                    .forEach((k,v) -> res.put(k, (LinkedHashMap) v.sourceAsMap().get("properties")));
            return res;
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public AcknowledgedResponse putMapping(String index, String mapping) throws ArlasException {
        try {
            PutMappingRequest request = new PutMappingRequest(index);
            request.source(mapping, XContentType.JSON);
            return client.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public GetFieldMappingsResponse getFieldMapping(String index, String... fields) throws ArlasException {
        try {
            GetFieldMappingsRequest request = new GetFieldMappingsRequest();
            request.indices(index);
            request.fields(fields);
            return client.indices().getFieldMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public String getHit(String index, String ref, String[] includes, String[] excludes) throws ArlasException {
        try {
            GetRequest request = new GetRequest(index, ref);
            FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
            request.fetchSourceContext(fetchSourceContext);
            GetResponse hit = client.get(request, RequestOptions.DEFAULT);
            return hit.getSourceAsString();
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public SearchResponse search(SearchRequest request) throws ArlasException {
        try {
            return client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, request.indices().toString());
            return null;
        }
    }

    public SearchResponse searchScroll(SearchScrollRequest request) throws ArlasException {
        try {
            return client.scroll(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, request.getDescription());
            return null;
        }
    }

    public ClearScrollResponse clearScroll(ClearScrollRequest request) throws ArlasException {
        try {
            return client.clearScroll(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, request.getDescription());
            return null;
        }
    }

    public DeleteResponse delete(String index, String ref) throws ArlasException {
        try {
            DeleteRequest request = new DeleteRequest(index, ref);
            return client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public AcknowledgedResponse deleteIndex(String index) throws ArlasException {
        try {
            return client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    public IndexResponse index(String index, String id, String source) throws ArlasException {
        try {
            IndexRequest request = new IndexRequest(index).id(id);
            request.source(source, XContentType.JSON);
            return client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            processException(e, index);
            return null;
        }
    }

    private void processException(IOException e, String index) throws ArlasException {
        if (e instanceof ResponseException) {
            if (((ResponseException) e).getResponse().getStatusLine().getStatusCode() == 404) {
                throw new NotFoundException("Index " + index + " does not exist.");
            }
        }
        LOGGER.warn("Exception while communicating with ES: " + e.getMessage(), e);
        throw new InternalServerErrorException(e.getMessage());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

    public void close() {
        try {
            if (this.sniffer != null) {
                this.sniffer.close();
            }
            this.client.close();
        } catch (IOException e) {
        }
    }
}
