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

package io.arlas.server.services;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.request.Aggregation;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.MultiValueFilter;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColumnFilterService {

    public ColumnFilterService() {
    }

    public boolean isFilterDefined(Set<String> filteredColumns) {
        return !filteredColumns.isEmpty();
    }

    public boolean isAllowed(Set<String> filteredColumns, String field) {
        if (!isFilterDefined(filteredColumns)) {
            //no filter - always allowed
            return true;
        }

        if (filteredColumns.contains(field)) {
            //field itself is allowed
            return true;
        }

        String[] splitted = field.split("\\.");
        if (splitted.length == 1) {
            //field itself not allowed, and no parent field
            return false;
        }

        //check if parent field is allowed
        String withoutLastPart = String.join(".", Arrays.copyOfRange(splitted, 0, splitted.length - 1));
        return isAllowed(filteredColumns, withoutLastPart);
    }

    public boolean isForbidden(Set<String> filteredColumns, String field) {
        return !this.isAllowed(filteredColumns, field);
    }

    public String filterInclude(Set<String> filteredColumns, String includes) {
        if (!isFilterDefined(filteredColumns)) {
            return includes;
        }
        return Arrays.stream(includes.split(",")).flatMap(c -> {
            if (this.isAllowed(filteredColumns, c)) {
                return Arrays.asList(c).stream();
            }

            //return only allowed sub-fields
            return filteredColumns.stream().filter(col -> col.startsWith(c + ".")).collect(Collectors.toList()).stream();
        }).collect(Collectors.joining(","));
    }

    public String filterSort(Set<String> filteredColumns, String sort) {

        if (!isFilterDefined(filteredColumns)) {
            return sort;
        }

        return Arrays.stream(sort.split(","))
                .filter(a -> {
                    if (a.startsWith("-")) {
                        return this.isAllowed(filteredColumns, a.substring(1));
                    } else if (a.contains(":")) {
                        return this.isAllowed(filteredColumns, a.substring(0, a.indexOf(":")));
                    } else {
                        return this.isAllowed(filteredColumns, a);
                    }
                }).collect(Collectors.joining(","));
    }

    public String filterAfter(Set<String> filteredColumns, String sort, String before) {

        if (!isFilterDefined(filteredColumns) || StringUtils.isBlank(sort)) {
            return before;
        }

        String[] sortColumns = sort.split(",");
        Set<Integer> filteredSortIndices = IntStream.range(0, sortColumns.length)
                .filter(i -> {
                    String column = sortColumns[i];
                    if (column.startsWith("-")) {
                        return this.isAllowed(filteredColumns, column.substring(1));
                    } else if (column.contains(":")) {
                        return this.isAllowed(filteredColumns, column.substring(0, column.indexOf(":")));
                    } else {
                        return this.isAllowed(filteredColumns, column);
                    }
                })
                .boxed()
                .collect(Collectors.toSet());

        String[] afterValues = before.split(",");

        return IntStream.range(0, afterValues.length)
                .filter(i -> filteredSortIndices.contains(i))
                .mapToObj(i -> afterValues[i])
                .collect(Collectors.joining(","));
    }

    public MultiValueFilter<String> getFilteredQ(Set<String> filteredColumns, MultiValueFilter<String> q) {

        if (!isFilterDefined(filteredColumns)) {
            return q;
        }

        return q.stream()
                .filter(c -> !c.contains(":") || this.isAllowed(filteredColumns, StringUtils.substringBefore(c, ":")))
                .collect(Collectors.toCollection(MultiValueFilter::new));
    }

    //TODO define exception
    public List<Aggregation> filterAggregations(Set<String> filteredColumns, List<Aggregation> aggregations) throws ArlasException {

        for(Aggregation aggregation : aggregations) {
            if (this.isForbidden(filteredColumns, aggregation.field)) {
                throw new ArlasException("Aggregation field is not allowed");
            }
            if (aggregation.fetchGeometry != null && this.isForbidden(filteredColumns, aggregation.fetchGeometry.field)) {
                throw new ArlasException("Aggregation fetch geometry field is not allowed");
            }
        }

        return aggregations.stream().map(aggregation -> {
            if (aggregation.metrics != null) {
                aggregation.metrics = aggregation.metrics.stream().filter(m -> this.isAllowed(filteredColumns, m.collectField)).collect(Collectors.toList());
            }

            if (aggregation.fetchHits != null && aggregation.fetchHits.include != null) {
                aggregation.fetchHits.include = aggregation.fetchHits.include.stream().filter(h -> this.isAllowed(filteredColumns, h)).collect(Collectors.toList());
            }
            return aggregation;
        }).collect(Collectors.toList());
    }

    public MultiValueFilter<Expression> filterF(Set<String> filteredColumns, MultiValueFilter<Expression> f) {
        return f.stream().filter((m -> this.isAllowed(filteredColumns, m.field))).collect(Collectors.toCollection(MultiValueFilter::new));
    }

    public String getFilteredColumnsAsString(Set<String> filteredColumns) {
        return filteredColumns.stream().collect(Collectors.joining(","));
    }

}
