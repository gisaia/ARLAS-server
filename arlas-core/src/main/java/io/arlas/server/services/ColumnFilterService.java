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
import io.arlas.server.exceptions.NotAllowedException;
import io.arlas.server.model.request.Aggregation;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.MultiValueFilter;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provide functions related to column filtering.
 * The principle is:
 * - if the filter is empty, do not filter at all
 * - if there is a filter, then only allow to filter & return allowed columns
 */
public class ColumnFilterService {

    public ColumnFilterService() {
    }

    public boolean isFilterDefined(Set<String> columnFilter) {
        return !columnFilter.isEmpty();
    }

    public boolean isAllowed(Set<String> columnFilter, String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }

        if (!isFilterDefined(columnFilter)) {
            //no filter - always allowed
            return true;
        }

        if (columnFilter.contains(field)) {
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
        return isAllowed(columnFilter, withoutLastPart);
    }

    public boolean isForbidden(Set<String> columnFilter, String field) {
        return !this.isAllowed(columnFilter, field);
    }

    /**
     * Filter the "includes" .
     * In the case where a parent field is included (eg. params.*) where only view sub-fields are allowed,
     * then only allowed sub-fields are included
     * @param columnFilter
     * @param includes
     * @return
     */
    public String filterInclude(Set<String> columnFilter, String includes) {
        if (!isFilterDefined(columnFilter)) {
            return includes;
        }
        return Arrays.stream(includes.split(",")).flatMap(c -> {
            if (this.isAllowed(columnFilter, c)) {
                return Arrays.asList(c).stream();
            }

            //return only allowed sub-fields
            return columnFilter.stream().filter(col -> col.startsWith(c + ".")).collect(Collectors.toList()).stream();
        }).collect(Collectors.joining(","));
    }

    /**
     * Filter a "sort".
     * @param columnFilter
     * @param sort
     * @return
     */
    public String filterSort(Set<String> columnFilter, String sort) {

        if (!isFilterDefined(columnFilter)) {
            return sort;
        }

        return Arrays.stream(sort.split(","))
                .filter(a -> {
                    if (a.startsWith("-")) {
                        return this.isAllowed(columnFilter, a.substring(1));
                    } else if (a.contains(":")) {
                        return this.isAllowed(columnFilter, a.substring(0, a.indexOf(":")));
                    } else {
                        return this.isAllowed(columnFilter, a);
                    }
                }).collect(Collectors.joining(","));
    }

    /**
     * Filter an "after" by considering a "sort".
     * If some fields in the "sort" aren't allowed, then their value is removed from the "after".
     * @param columnFilter
     * @param sort
     * @param before
     * @return
     */
    public String filterAfter(Set<String> columnFilter, String sort, String before) {

        if (!isFilterDefined(columnFilter) || StringUtils.isBlank(sort)) {
            return before;
        }

        String[] sortColumns = sort.split(",");
        Set<Integer> filteredSortIndices = IntStream.range(0, sortColumns.length)
                .filter(i -> {
                    String column = sortColumns[i];
                    if (column.startsWith("-")) {
                        return this.isAllowed(columnFilter, column.substring(1));
                    } else if (column.contains(":")) {
                        return this.isAllowed(columnFilter, column.substring(0, column.indexOf(":")));
                    } else {
                        return this.isAllowed(columnFilter, column);
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

    /**
     * Filter a "q", if a field is provided.
     * @param columnFilter
     * @param q
     * @return
     */
    public MultiValueFilter<String> getFilteredQ(Set<String> columnFilter, MultiValueFilter<String> q) {

        if (!isFilterDefined(columnFilter)) {
            return q;
        }

        return q.stream()
                .filter(c -> !c.contains(":") || this.isAllowed(columnFilter, StringUtils.substringBefore(c, ":")))
                .collect(Collectors.toCollection(MultiValueFilter::new));
    }

    /**
     * Filter "aggregations".
     * If the main field or the geometry fields of an aggregation aren't allowed, their can't be any result - so an exception is thrown.
     * If metrics or hits fields aren't allowed, they are simply removed from the request.
     * @param columnFilter
     * @param aggregations
     * @return
     * @throws ArlasException
     */
    public List<Aggregation> filterAggregations(Set<String> columnFilter, List<Aggregation> aggregations) throws NotAllowedException {

        if (!this.isFilterDefined(columnFilter)) {
            return aggregations;
        }

        for(Aggregation aggregation : aggregations) {
            if (this.isForbidden(columnFilter, aggregation.field)) {
                throw new NotAllowedException("Aggregation field is not allowed");
            }
            if (aggregation.fetchGeometry != null && aggregation.fetchGeometry.field != null && this.isForbidden(columnFilter, aggregation.fetchGeometry.field)) {
                throw new NotAllowedException("Aggregation fetch geometry field is not allowed");
            }
        }

        return aggregations.stream().map(aggregation -> {
            if (aggregation.metrics != null) {
                aggregation.metrics = aggregation.metrics.stream().filter(m -> this.isAllowed(columnFilter, m.collectField)).collect(Collectors.toList());
            }

            if (aggregation.fetchHits != null && aggregation.fetchHits.include != null) {
                aggregation.fetchHits.include = aggregation.fetchHits.include.stream().filter(h -> {
                    String hitColName = h.startsWith("-") ? h.substring(1) : h;
                    return this.isAllowed(columnFilter, hitColName);
                }).collect(Collectors.toList());
            }
            return aggregation;
        }).collect(Collectors.toList());
    }

    /**
     * Filter a "f".
     * @param columnFilter
     * @param f
     * @return
     */
    public MultiValueFilter<Expression> filterF(Set<String> columnFilter, MultiValueFilter<Expression> f) {

        if (!this.isFilterDefined(columnFilter)) {
            return f;
        }

        return f.stream().filter((m -> this.isAllowed(columnFilter, m.field))).collect(Collectors.toCollection(MultiValueFilter::new));
    }

    public String filterRange(Set<String> columnFilter, String field) throws NotAllowedException {

        if (this.isFilterDefined(columnFilter) && this.isForbidden(columnFilter, field)) {
            throw new NotAllowedException("Range field is not allowed");
        }
        return field;
    }

    public String getColumnFilterAsString(Set<String> columnFilter) {
        return columnFilter.stream().collect(Collectors.joining(","));
    }


    public Set<String> getEmptyFilter() {
        return new HashSet<>();
    }

}
