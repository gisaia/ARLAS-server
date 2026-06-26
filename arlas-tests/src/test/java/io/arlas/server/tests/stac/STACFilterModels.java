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

package io.arlas.server.tests.stac;

import io.arlas.server.core.model.request.Filter;

import java.util.List;

public final class STACFilterModels {

    public enum TypeOfGet {
        STAC, OGC_FEATURE
    }

    public enum HttpMethod {
        GET, POST
    }

    public enum FilterOperator {
        EQ("="),
        NE("<>"),
        GT(">"),
        GTE(">="),
        LT("<"),
        LTE("<="),
        BETWEEN("between"),
        LIKE("like"),
        ST_INTERSECTS("s_intersects"),
        ST_WITHIN("s_within"),
        BBOX("bbox");
        private final String symbol;

        FilterOperator(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }
    }

    public record FilterClause(
            String property,
            FilterOperator operator,
            Object value
    ) {
    }

    public record RequestTarget(
            HttpMethod mode,
            TypeOfGet typeOfGet
    ) {
        public static RequestTarget stacGet() {
            return new RequestTarget(HttpMethod.GET, TypeOfGet.STAC);
        }

        public static RequestTarget ogcGet() {
            return new RequestTarget(HttpMethod.GET, TypeOfGet.OGC_FEATURE);
        }

        public static RequestTarget post() {
            return new RequestTarget(HttpMethod.POST, null);
        }

        @Override
        public String toString() {
            return switch (mode) {
                case GET -> "GET " + typeOfGet;
                case POST -> "POST";
            };
        }
    }

    public enum FilterLang {
        CQL2_TEXT("cql2-text"),
        CQL2_JSON("cql2-json");

        private final String value;

        FilterLang(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public record StacFilterScenario(
            List<FilterClause> clauses,
            int limit,
            int expectedMatched,
            Filter partitionFilter,
            String columnFilter,
            List<String> ids,
            String datetime,
            String bbox
    ) {
        public static StacFilterScenario simple(
                String property,
                FilterOperator operator,
                Object value,
                int limit,
                int expectedMatched
        ) {
            return new StacFilterScenario(
                    List.of(new FilterClause(property, operator, value)),
                    limit,
                    expectedMatched,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        public static StacFilterScenario and(
                List<FilterClause> clauses,
                int limit,
                int expectedMatched
        ) {
            return new StacFilterScenario(
                    clauses,
                    limit,
                    expectedMatched,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        public StacFilterScenario withPartitionFilter(Filter partitionFilter) {
            return new StacFilterScenario(
                    clauses, limit, expectedMatched, partitionFilter, columnFilter, ids, datetime, bbox
            );
        }

        public StacFilterScenario withColumnFilter(String columnFilter) {
            return new StacFilterScenario(
                    clauses, limit, expectedMatched, partitionFilter, columnFilter, ids, datetime, bbox
            );
        }

        public StacFilterScenario withIds(List<String> ids) {
            return new StacFilterScenario(
                    clauses, limit, expectedMatched, partitionFilter, columnFilter, ids, datetime, bbox
            );
        }

        public StacFilterScenario withDatetime(String datetime) {
            return new StacFilterScenario(
                    clauses, limit, expectedMatched, partitionFilter, columnFilter, ids, datetime, bbox
            );
        }

        public StacFilterScenario withBbox(String bbox) {
            return new StacFilterScenario(
                    clauses, limit, expectedMatched, partitionFilter, columnFilter, ids, datetime, bbox
            );
        }
    }

    public record BetweenValue(
            Object lower,
            Object upper
    ) { }

    public record BboxValue(
            double minX,
            double minY,
            double maxX,
            double maxY
    ) { }
}