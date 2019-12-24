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

package io.arlas.server.utils;

import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.model.request.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

public class RequestFieldsExtractor {

    private static final List<String> GEOSORT_IGNORED_LIST = Arrays.asList("geodistance");
    public static final String INCLUDE_F = "f";
    public static final String INCLUDE_Q = "q";
    public static final String INCLUDE_SEARCH_SORT = "search_sort";
    public static final String INCLUDE_SEARCH_INCLUDE = "search_include";
    public static final String INCLUDE_SEARCH_EXCLUDE = "search_exclude";
    public static final String INCLUDE_SEARCH_RETURNED_GEOMETRIES = "search_returned_geometries";
    public static final String INCLUDE_AGG_FIELD = "agg_field";
    public static final String INCLUDE_AGG_FETCH_GEOMETRY = "agg_fetch_geometry";
    public static final String INCLUDE_AGG_FETCH_HITS = "agg_fetch_hits";
    public static final String INCLUDE_AGG_METRICS = "agg_metrics";
    public static final String INCLUDE_RANGE_FIELD = "range_field";
    public static final String INCLUDE_COMPUTATION_FIELD = "computation_field";

    public static final Set<String> INCLUDE_ALL  = new HashSet(Arrays.asList(INCLUDE_F, INCLUDE_Q, INCLUDE_SEARCH_SORT,INCLUDE_SEARCH_INCLUDE, INCLUDE_SEARCH_EXCLUDE, INCLUDE_SEARCH_RETURNED_GEOMETRIES,
            INCLUDE_AGG_FIELD, INCLUDE_AGG_FETCH_GEOMETRY, INCLUDE_AGG_FETCH_HITS, INCLUDE_AGG_METRICS, INCLUDE_RANGE_FIELD, INCLUDE_COMPUTATION_FIELD));

    public static Stream<String> extract(Request request, Set<String> includeFields) throws InternalServerErrorException {
        IRequestFieldsExtractor requestExtractor;

        //comparing classes (and not with "instanceof") to be sure that no subclass with new fields has been added
        if (Arrays.asList(Request.class, Count.class).contains(request.getClass())) {
            requestExtractor = new BasicRequestFieldsExtractor();
        } else if (request.getClass() == Search.class) {
            requestExtractor = new SearchRequestFieldsExtractor();
        } else if (request.getClass() == AggregationsRequest.class) {
            requestExtractor = new AggregationRequestFieldsExtractor();
        } else if (request.getClass() == RangeRequest.class) {
            requestExtractor = new RangeRequestFieldsExtractor();
        } else if (request.getClass() == ComputationRequest.class) {
            requestExtractor = new ComputationRequestFieldsExtractor();
        } else {
            throw new InternalServerErrorException("Request type is not supported");
        }

        Stream<String> colsStream = requestExtractor.getCols(request, includeFields);
        return colsStream.filter(StringUtils::isNotBlank);
    }

    private interface IRequestFieldsExtractor<T extends Request> {
        Stream<String> getCols(T request, Set<String> includeFields);
    }

    private static class BasicRequestFieldsExtractor<T extends Request> implements IRequestFieldsExtractor<T> {

        public Stream<String> getCols(T request, Set<String> includeFields) {
            Stream<String> fCols = includeFields.contains(INCLUDE_F) ? getFCols(request) : Stream.of();
            Stream<String> qCols = includeFields.contains(INCLUDE_Q) ? getQCols(request) : Stream.of();
            return Stream.concat(fCols, qCols);
        }

        private Stream<String> getFCols(Request request) {
            return Optional.ofNullable(request.filter).flatMap(filter -> Optional.ofNullable(filter.f))
                    .map(f -> f.stream()
                            .flatMap(fList ->
                                    fList.stream().map(fFilter -> fFilter.field))
                    ).orElse(Stream.of());
        }

        private Stream<String> getQCols(Request request) {
            return  Optional.ofNullable(request.filter).flatMap(filter -> Optional.ofNullable(filter.q)
                    .map(q ->
                            q.stream().flatMap(qList ->
                                    qList.stream()
                                            .filter(qFilter -> qFilter.contains(":"))
                                            .map(qFilter -> qFilter.split(":")[0]))
                    )).orElse(Stream.of());
        }
    }

    private static class SearchRequestFieldsExtractor extends BasicRequestFieldsExtractor<Search> {

        @Override
        public Stream<String> getCols(Search request, Set<String> includeFields) {
            Stream<String> sortCols = includeFields.contains(INCLUDE_SEARCH_SORT) ? getSortCols(request) : Stream.of();
            Stream<String> includeCols = includeFields.contains(INCLUDE_SEARCH_INCLUDE) ? getIncludeCols(request) : Stream.of();
            Stream<String> excludeCols = includeFields.contains(INCLUDE_SEARCH_EXCLUDE) ? getExcludeCols(request) : Stream.of();
            Stream<String> returnedGeometries = includeFields.contains(INCLUDE_SEARCH_RETURNED_GEOMETRIES) ? getReturnedGeometriesCols(request) : Stream.of();

            return Stream.of(super.getCols(request, includeFields), sortCols, includeCols, excludeCols, returnedGeometries)
                    .flatMap(x -> x);
        }

        private Stream<String> getSortCols(Search search) {
            return Optional.ofNullable(search.page).flatMap(p -> Optional.ofNullable(p.sort)
                    .map(sort -> Arrays.stream(sort.split(",")).map(sortCol -> {
                        if (sortCol.startsWith("-")) {
                            return sortCol.substring(1);
                        } else if (sortCol.contains(":")) {
                            String geosortField = sortCol.substring(0, sortCol.indexOf(":"));
                            return GEOSORT_IGNORED_LIST.contains(geosortField) ? null : geosortField;
                        } else {
                            return sortCol;
                        }
                    }).filter(c -> c != null))
            ).orElse(Stream.of());
        }

        private Stream<String> getIncludeCols(Search search) {
            return Optional.ofNullable(search.projection).flatMap(p -> Optional.ofNullable(p.includes)
                    .map(i -> Arrays.stream(i.split(","))))
                    .orElse(Stream.of());
        }

        private Stream<String> getExcludeCols(Search search) {
            return Optional.ofNullable(search.projection).flatMap(p -> Optional.ofNullable(p.excludes)
                    .map(i -> Arrays.stream(i.split(","))))
                    .orElse(Stream.of());
        }

        private Stream<String> getReturnedGeometriesCols(Search search) {
            return Optional.ofNullable(search.returned_geometries)
                    .map(i -> Arrays.stream(i.split(",")))
                    .orElse(Stream.of());
        }
    }

    private static class AggregationRequestFieldsExtractor extends BasicRequestFieldsExtractor<AggregationsRequest> {
        @Override
        public Stream<String> getCols(AggregationsRequest request, Set<String> includeFields) {

            Stream<String> superCols = super.getCols(request, includeFields);

            Stream<String> aggregationsCols = Optional.ofNullable(request.aggregations).map(aggs ->
                    aggs.stream().flatMap(agg -> {

                        Stream<String> fieldStream = includeFields.contains(INCLUDE_AGG_FIELD) ? getAggCol(agg) : Stream.of();
                        Stream<String> fetchGeometryField = includeFields.contains(INCLUDE_AGG_FETCH_GEOMETRY) ? getFetchGeometryCol(agg) : Stream.of();
                        Stream<String> fetchHitsStream = includeFields.contains(INCLUDE_AGG_FETCH_HITS) ? getFetchHitsCols(agg) : Stream.of();
                        Stream<String> metricsStream = includeFields.contains(INCLUDE_AGG_METRICS) ? getMetricsCols(agg) : Stream.of();

                        return Stream.of(fieldStream, fetchGeometryField, fetchHitsStream, metricsStream).flatMap(x -> x);
                    })
            ).orElse(Stream.of());

            return Stream.concat(superCols, aggregationsCols);
        }

        private Stream<String> getAggCol(Aggregation agg) {
            return Optional.ofNullable(agg.field).map(Stream::of).orElse(Stream.of());
        }

        private Stream<String> getFetchGeometryCol(Aggregation agg) {
            return Optional.ofNullable(agg.fetchGeometry).flatMap(
                    g -> Optional.ofNullable(g.field).map(Stream::of))
                    .orElse(Stream.of());
        }

        private Stream<String> getFetchHitsCols(Aggregation agg) {
            return Optional.ofNullable(agg.fetchHits).flatMap(
                    f -> Optional.ofNullable(f.include).map(
                            inc -> inc.stream().map(
                                    i -> i.startsWith("-") ? i.substring(1) : i)))
                    .orElse(Stream.of());
        }

        private Stream<String> getMetricsCols(Aggregation agg) {
            return Optional.ofNullable(agg.metrics).map(
                    metrics -> metrics.stream().map(
                            m -> m.collectField))
                    .orElse(Stream.of());
        }

    }

    private static class RangeRequestFieldsExtractor extends BasicRequestFieldsExtractor<RangeRequest> {
        @Override
        public Stream<String> getCols(RangeRequest request, Set<String> includeFields) {

            Stream<String> rangeCol = includeFields.contains(INCLUDE_RANGE_FIELD) ? getRangeCol(request) : Stream.of();

            return Stream.concat(super.getCols(request, includeFields), rangeCol);
        }

        private Stream<String> getRangeCol(RangeRequest request) {
            return Optional.ofNullable(request.field)
                    .map(Stream::of)
                    .orElse(Stream.of());
        }
    }

    private static class ComputationRequestFieldsExtractor extends BasicRequestFieldsExtractor<ComputationRequest> {

        @Override
        public Stream<String> getCols(ComputationRequest request, Set<String> includeFields) {

            Stream<String> computationCol = includeFields.contains(INCLUDE_COMPUTATION_FIELD) ? getComputationCol(request) : Stream.of();

            return Stream.concat(super.getCols(request, includeFields), computationCol);
        }

        private Stream<String> getComputationCol(ComputationRequest request) {
            return Optional.ofNullable(request.field)
                    .map(Stream::of)
                    .orElse(Stream.of());
        }
    }

}

