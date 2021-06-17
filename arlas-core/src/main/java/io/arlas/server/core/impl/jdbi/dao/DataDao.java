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

package io.arlas.server.core.impl.jdbi.dao;

import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.core.result.ResultIterator;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindMap;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.FetchSize;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Map;

public interface DataDao {
    @SqlQuery("select count(*) from <index_name> <where_clause>")
    int count(@Define("index_name") String index_name,
              @Define("where_clause") String where_clause,
              @BindMap Map<String, Object> params);

    @SqlQuery("select <select_clause> from <index_name> <where_clause> <group_clause> <order_clause> <limit_clause>")
    @RegisterRowMapper(MapMapper.class)
    @FetchSize(1000)
    ResultIterator<Map<String,Object>> select(@Define("index_name") String indexName,
                                              @Define("select_clause") String selectClause,
                                              @Define("where_clause") String whereClause,
                                              @Define("group_clause") String groupClause,
                                              @Define("order_clause") String orderClause,
                                              @Define("limit_clause") String limitClause,
                                              @BindMap Map<String, Object> params);
}
