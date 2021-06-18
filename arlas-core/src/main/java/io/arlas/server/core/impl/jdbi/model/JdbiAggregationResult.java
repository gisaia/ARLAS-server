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

package io.arlas.server.core.impl.jdbi.model;

import org.jdbi.v3.core.result.ResultIterable;

import java.util.*;

import static io.arlas.server.core.impl.jdbi.model.SelectRequest.QKEY;

public class JdbiAggregationResult {
    public static final String AGG_NAME = "aggName";
    private Map<String, Map> results = new LinkedHashMap<>();

    public JdbiAggregationResult(int maxDepth, ResultIterable<Map<String, Object>> rows) {
        rows.forEach(row -> {
            final Map<String, Map<String, Object>> dataPerAgg = new TreeMap<>();
            row.forEach((colName, value) -> {
                String[] p = colName.split("_", 3); // aggName_depth_qualifier
                Map<String, Object> colValues = Optional.ofNullable(dataPerAgg.get(p[1])).orElse(new HashMap<>());
                colValues.put(p[2], value);
                colValues.put(AGG_NAME, p[0]);
                dataPerAgg.put(p[1], colValues);
            }); // map { aggIndex -> map { qualifier -> value } }

            Map tmpResults = results;
            for (int i = 0; i < maxDepth; i++) {
                Map values = dataPerAgg.get(String.valueOf(i));
                Map bucket = (Map) Optional.ofNullable(tmpResults.get(values.get(QKEY))).orElse(new LinkedHashMap());
                bucket.put("values", values);
                Map subAgg = (Map) Optional.ofNullable(bucket.get("subAgg")).orElse(new LinkedHashMap());
                bucket.put("subAgg", subAgg);
                tmpResults.put(values.get(QKEY), bucket);
                tmpResults = subAgg;
            }
        });
    }

    public Map getResults() {
        return results;
    }
}
