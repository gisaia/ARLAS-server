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
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.DatabaseToolsFactory;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.health.ElasticsearchHealthCheck;
import io.arlas.server.impl.elastic.dao.ElasticCollectionReferenceDao;
import io.arlas.server.impl.elastic.services.ElasticExploreService;
import io.arlas.server.impl.elastic.utils.ElasticClient;
import io.arlas.server.managers.CacheManager;
import io.arlas.server.ogc.common.dao.ElasticOGCCollectionReferenceDao;
import io.arlas.server.ogc.common.dao.OGCCollectionReferenceDao;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.wfs.services.ElasticWFSToolService;
import io.arlas.server.ogc.wfs.services.WFSToolService;
import io.arlas.server.services.ExploreService;

import java.util.HashMap;
import java.util.Map;


public class ElasticDatabaseToolsFactory extends DatabaseToolsFactory {
    private final ElasticClient elasticClient;
    private final ExploreService exploreService;
    private final CollectionReferenceDao collectionReferenceDao;
    private OGCCollectionReferenceDao ogcDao;
    private WFSToolService wfsService;

    public ElasticDatabaseToolsFactory(ArlasServerConfiguration configuration, CacheManager cacheManager) {
        super(configuration);
        this.elasticClient = new ElasticClient(configuration.elasticConfiguration);
        this.collectionReferenceDao = new ElasticCollectionReferenceDao(elasticClient, configuration.arlasIndex, cacheManager);
        this.exploreService = new ElasticExploreService(elasticClient, collectionReferenceDao, configuration.arlasBaseUri, configuration.arlasRestCacheTimeout);
        if (configuration.arlasServiceCSWEnabled) {
            this.ogcDao = new ElasticOGCCollectionReferenceDao(elasticClient, collectionReferenceDao, configuration.arlasIndex, Service.CSW);
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
    public CollectionReferenceDao getCollectionReferenceDao() {
        return this.collectionReferenceDao;
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
