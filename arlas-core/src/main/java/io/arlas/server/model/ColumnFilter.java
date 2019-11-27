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

package io.arlas.server.model;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.model.request.Aggregation;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.MultiValueFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColumnFilter {

    Optional<Set<String>> filteredColumns;

    public ColumnFilter() {
        filteredColumns = Optional.empty();
    }

    public ColumnFilter(Optional<String> columnFilter, List<String> defaultColumns) {
        filteredColumns = columnFilter.filter(StringUtils::isNotBlank).map(cf -> Arrays.stream(cf.replaceAll("\\.\\*", "").replaceAll(" ", "").split(",")).collect(Collectors.toSet()));
        filteredColumns.map(cols -> cols.addAll(defaultColumns));
    }

    public boolean isFilterDefined() {
        return this.filteredColumns.isPresent() && !this.filteredColumns.get().isEmpty();
    }

    public boolean isAllowed(String field) {
        if (!filteredColumns.isPresent()) {
            //no filter - always allowed
            return true;
        }

        if (filteredColumns.get().contains(field)) {
            return true;
        }

        String[] splitted = field.split("\\.");
        if (splitted.length == 1) {
            return false;
        }

        String withoutLastPart = String.join(".", Arrays.copyOfRange(splitted, 0, splitted.length - 1));
        return isAllowed(withoutLastPart);
    }

    public boolean isForbidden(String field) {
        return !this.isAllowed(field);
    }

    public String filterInclude(String includes) {
        if (!filteredColumns.isPresent()) {
            return includes;
        }
        return Arrays.stream(includes.split(",")).flatMap(c -> {
            if (this.isAllowed(c)) {
                return Arrays.asList(c).stream();
            }

            //return only allowed sub-fields
            return filteredColumns.get().stream().filter(col -> col.startsWith(c + ".")).collect(Collectors.toList()).stream();
        }).collect(Collectors.joining(","));
    }

    public Optional<Set<String>> getFilteredColumns() {
        return filteredColumns;
    }

    public String filterSort(String sort) {

        if (!isFilterDefined()) {
            return sort;
        }

        return Arrays.stream(sort.split(","))
                .filter(a -> {
                    if (a.startsWith("-")) {
                        return this.isAllowed(a.substring(1));
                    } else if (a.contains(":")) {
                        return this.isAllowed(a.substring(0, a.indexOf(":")));
                    } else {
                        return this.isAllowed(a);
                    }
                }).collect(Collectors.joining(","));
    }

    public String filterAfter(String sort, String before) {

        if (!isFilterDefined() || StringUtils.isBlank(sort)) {
            return before;
        }

        String[] sortColumns = sort.split(",");
        Set<Integer> filteredSortIndices = IntStream.range(0, sortColumns.length)
                .filter(i -> {
                    String column = sortColumns[i];
                    if (column.startsWith("-")) {
                        return this.isAllowed(column.substring(1));
                    } else if (column.contains(":")) {
                        return this.isAllowed(column.substring(0, column.indexOf(":")));
                    } else {
                        return this.isAllowed(column);
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

    public MultiValueFilter getFilteredQ(MultiValueFilter<String> q) {
        return q.stream()
                .filter(c -> !c.contains(":") || this.isAllowed(StringUtils.substringBefore(c, ":")))
                .collect(Collectors.toCollection(MultiValueFilter::new));
    }

    //TODO define exception
    public List<Aggregation> filterAggregations(List<Aggregation> aggregations) throws ArlasException {

        for(Aggregation aggregation : aggregations) {
            if (this.isForbidden(aggregation.field)) {
                throw new ArlasException("Aggregation field is not allowed");
            }
            if (aggregation.fetchGeometry != null && this.isForbidden(aggregation.fetchGeometry.field)) {
                throw new ArlasException("Aggregation fetch geometry field is not allowed");
            }
        }

        return aggregations.stream().map(aggregation -> {
            if (aggregation.metrics != null) {
                aggregation.metrics = aggregation.metrics.stream().filter(m -> this.isAllowed(m.collectField)).collect(Collectors.toList());
            }

            if (aggregation.fetchHits != null && aggregation.fetchHits.include != null) {
                aggregation.fetchHits.include = aggregation.fetchHits.include.stream().filter(h -> this.isAllowed(h)).collect(Collectors.toList());
            }
            return aggregation;
        }).collect(Collectors.toList());
    }

    public MultiValueFilter<Expression> filterF(MultiValueFilter<Expression> f) {
        return f.stream().filter((m -> this.isAllowed(m.field))).collect(Collectors.toCollection(MultiValueFilter::new));
    }

}
