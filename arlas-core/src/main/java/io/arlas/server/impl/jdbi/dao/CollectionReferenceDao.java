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

package io.arlas.server.impl.jdbi.dao;

import io.arlas.server.impl.jdbi.model.ColumnDefinition;
import io.arlas.server.model.CollectionReference;
import org.jdbi.v3.json.Json;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.List;
import java.util.Optional;

@UseClasspathSqlLocator
public interface CollectionReferenceDao {
    @SqlUpdate
    void createTable();

    @SqlQuery
    @Json
    List<CollectionReference> selectAll(String arlasIndex);

    @SqlQuery
    @Json
    Optional<CollectionReference> selectOne(String arlasIndex, String collectionName);

    @SqlUpdate
    boolean delete(String arlasIndex, String collectionName);

    @SqlUpdate
    void insert(String arlasIndex, String collectionName, String indexName, @Json CollectionReference description);

    @Transaction
    default void deleteAndInsert(String arlasIndex, String collectionName, String indexName, @Json CollectionReference description) {
        delete(arlasIndex, collectionName);
        insert(arlasIndex, collectionName, indexName, description);
    }

    @SqlQuery
    @RegisterFieldMapper(ColumnDefinition.class)
    List<ColumnDefinition> describeOne(String tableName);

    @SqlQuery
    @RegisterFieldMapper(ColumnDefinition.class)
    List<ColumnDefinition> describeAll(String arlasIndex);

    @SqlQuery
    @RegisterFieldMapper(ColumnDefinition.class)
    List<ColumnDefinition> describeGeographyType(@BindList("tableNames") List<String> indexNames);

    @SqlQuery
    @RegisterFieldMapper(ColumnDefinition.class)
    List<ColumnDefinition> describeGeometryType(@BindList("tableNames") List<String> indexNames);
}
