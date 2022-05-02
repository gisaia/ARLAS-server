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

package io.arlas.server.core.impl.jdbi.service;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.exceptions.CollectionUnavailableException;
import io.arlas.server.core.impl.jdbi.dao.CollectionReferenceDao;
import io.arlas.server.core.impl.jdbi.model.ColumnDefinition;
import io.arlas.server.core.managers.CacheManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class JdbiCollectionReferenceService extends CollectionReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbiCollectionReferenceService.class);

    final CollectionReferenceDao dao;

    public JdbiCollectionReferenceService(Jdbi jdbi, String arlasIndex, CacheManager cacheManager) {
        super(arlasIndex, cacheManager);
        this.dao = jdbi.onDemand(CollectionReferenceDao.class);
    }

    @Override
    public void initCollectionDatabase() {
        LOGGER.info("Create table if not exists");
        dao.createTable();
    }

    @Override
    protected CollectionReference getCollectionReferenceFromDao(String ref) throws ArlasException {
        return dao.selectOne(this.arlasIndex, ref).orElseThrow(() -> new NotFoundException("Collection " + ref + " not found."));
    }

    @Override
    protected Map<String, LinkedHashMap> getMappingFromDao(String indexName) throws ArlasException {
        List<ColumnDefinition> input = dao.describeOne(indexName);
        input.addAll(dao.describeGeographyType(Collections.singletonList(indexName)));
        input.addAll(dao.describeGeometryType(Collections.singletonList(indexName)));
        Map<String, LinkedHashMap> mapping = getMappingFromColumnDefinition(input);
        if (mapping.size() == 0) {
            throw new NotFoundException("Table not found: " + indexName);
        }
        return mapping;
    }

    // @Override
    protected Map<String, LinkedHashMap> getAllMappingsFromDao(String arlasIndex) {
        List<ColumnDefinition> input = dao.describeAll(arlasIndex);
        List<String> indexNames = input.stream().map(c -> c.tableName).distinct().collect(Collectors.toList());
        input.addAll(dao.describeGeographyType(indexNames));
        input.addAll(dao.describeGeometryType(indexNames));
        return getMappingFromColumnDefinition(dao.describeAll(arlasIndex));
    }

    private Map<String, LinkedHashMap> getMappingFromColumnDefinition(List<ColumnDefinition> definition) {
        Map<String, LinkedHashMap> mappings = new HashMap<>();
        definition.forEach(c -> {
            LinkedHashMap<String, Object> properties = mappings.get(c.tableName);
            if (properties == null) {
                properties = new LinkedHashMap<>();
            }
            properties.put(c.columnName, Collections.singletonMap("type", c.dataType));
            mappings.put(c.tableName, properties);
        });
        return mappings;
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences(Optional<String> columnFilter) throws CollectionUnavailableException {
        Set<String> allowedCollections = ColumnFilterUtil.getAllowedCollections(columnFilter);
        List<CollectionReference> all = dao.selectAll(this.arlasIndex);
        List<CollectionReference> result = new ArrayList<>();
        for (CollectionReference cr : all) {
            for (String c : allowedCollections) {
                if ((c.endsWith("*") && cr.collectionName.startsWith(c.substring(0, c.indexOf("*"))))
                        || cr.collectionName.equals(c)){
                    result.add(cr);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void deleteCollectionReference(String collectionName) throws ArlasException {
        if (dao.delete(this.arlasIndex, collectionName)) {
            cacheManager.removeCollectionReference(collectionName);
        } else {
            throw new NotFoundException("collection " + collectionName + " not found.");
        }
    }

    @Override
    protected void putCollectionReferenceWithDao(CollectionReference c) throws ArlasException {
        if (c.params.indexName.contains("-") || c.params.indexName.contains(".")) {
            throw new InvalidParameterException("index_name cannot contain '-' or '.' characters in SQL");
        }
        dao.deleteAndInsert(this.arlasIndex, c.collectionName, c.params.indexName, c);
    }
}