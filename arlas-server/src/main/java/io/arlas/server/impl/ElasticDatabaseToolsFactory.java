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
package io.arlas.server.impl;

import com.codahale.metrics.health.HealthCheck;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.app.DatabaseToolsFactory;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.server.admin.health.ElasticsearchHealthCheck;
import io.arlas.server.core.impl.elastic.services.ElasticCollectionReferenceService;
import io.arlas.server.core.impl.elastic.exceptions.ElasticsearchExceptionMapper;
import io.arlas.server.core.impl.elastic.services.ElasticExploreService;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.ogc.common.dao.ElasticOGCCollectionReferenceDao;
import io.arlas.server.ogc.common.dao.OGCCollectionReferenceDao;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.wfs.services.ElasticWFSToolService;
import io.arlas.server.ogc.wfs.services.WFSToolService;
import io.arlas.server.core.services.ExploreService;
import io.dropwizard.setup.Environment;

import java.util.HashMap;
import java.util.Map;


public class ElasticDatabaseToolsFactory extends DatabaseToolsFactory {
    private final ElasticClient elasticClient;
    private final ExploreService exploreService;
    private final CollectionReferenceService collectionReferenceService;
    private OGCCollectionReferenceDao ogcDao;
    private WFSToolService wfsService;

    public ElasticDatabaseToolsFactory(Environment environment, ArlasServerConfiguration configuration, CacheManager cacheManager) throws ArlasConfigurationException {
        super(configuration);
        configuration.elasticConfiguration.check();
        environment.jersey().register(new ElasticsearchExceptionMapper());

        this.elasticClient = new ElasticClient(configuration.elasticConfiguration);
        this.collectionReferenceService = new ElasticCollectionReferenceService(elasticClient, configuration.arlasIndex, cacheManager);
        this.exploreService = new ElasticExploreService(elasticClient, collectionReferenceService, configuration.arlasBaseUri, configuration.arlasRestCacheTimeout);
        if (configuration.arlasServiceCSWEnabled) {
            this.ogcDao = new ElasticOGCCollectionReferenceDao(elasticClient, collectionReferenceService, configuration.arlasIndex, Service.CSW);
        }
        if (configuration.arlasServiceWFSEnabled) {
            this.wfsService = new ElasticWFSToolService((ElasticExploreService) exploreService);
        }
    }

    @Override
    public ExploreService getExploreService() {
        return this.exploreService;
    }

    @Override
    public CollectionReferenceService getCollectionReferenceService() {
        return this.collectionReferenceService;
    }

    @Override
    public OGCCollectionReferenceDao getOGCCollectionReferenceDao() {
        return ogcDao;
    }

    @Override
    public WFSToolService getWFSToolService() {
        return wfsService;
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {
        Map<String, HealthCheck> ret = new HashMap<>();
        ret.put("elasticsearch", new ElasticsearchHealthCheck(elasticClient));
        return ret;
    }
}
