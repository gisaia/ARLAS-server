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

import io.arlas.server.exceptions.*;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Projection;
import io.arlas.server.model.request.Request;
import io.arlas.server.model.response.CollectionReferenceDescription;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide functions to check columns against a column filter.
 *
 * The principle is:
 * - if the filter is empty, do not filter at all;
 * - if there is a filter, turn it to regexp predicates
 * - append some collection mandatory fields to it
 * - checking a path is then a simple regexp match
 */
public class ColumnFilterUtil {

    static Logger LOGGER = LoggerFactory.getLogger(ColumnFilterUtil.class);

    //extract fields from request but without the `excludes`
    private static final Set<String> REQUEST_FIELDS_EXTRACTOR_INCLUDE = RequestFieldsExtractor.INCLUDE_ALL.stream().filter(i -> !i.equalsIgnoreCase(RequestFieldsExtractor.INCLUDE_SEARCH_EXCLUDE)).collect(Collectors.toSet());

    /**
     * Check that there aren't forbidden fields into the requests, and that fields are compatible with FGA prerequisites.
     * Also check that the target collection is allowed (at least one column of the collection is available, or
     * there is at least one column - in the column filter - that is not related to a specific collection).
     * @param columnFilter
     * @param collectionReference
     * @param basicRequest
     * @throws InternalServerErrorException if fields cannot be extracted from requests
     * @throws ColumnUnavailableException if a field is forbidden
     * @throws CollectionUnavailableException if the target collection is forbidden
     * @return
     */
    public static void assertRequestAllowed(Optional<String> columnFilter,
                                            CollectionReference collectionReference,
                                            Request basicRequest)
            throws InternalServerErrorException, ColumnUnavailableException, CollectionUnavailableException {

        Optional<String> cleanColumnFilter = cleanColumnFilter(columnFilter);

        if (!cleanColumnFilter.isPresent()) {
            return;
        }

        Optional<String> collectionColumnFilter = getCollectionRelatedColumnFilter(cleanColumnFilter, collectionReference);
        if (!collectionColumnFilter.isPresent()) {
            throw new CollectionUnavailableException(collectionReference);
        }

        Optional<Set<String>> columnFilterPredicates = ColumnFilterUtil.getColumnFilterPredicates(collectionColumnFilter, collectionReference);

        if (!columnFilterPredicates.isPresent()) {
            return;
        }

        assertQHasCol(basicRequest);

        //do not consider user columns with wildcards - they should be checked later against real fields, if necessary
        Set<String> forbiddenFields = RequestFieldsExtractor.extract(basicRequest, REQUEST_FIELDS_EXTRACTOR_INCLUDE)
                .filter(
                        f -> !f.contains("*") && !FilterMatcherUtil.matches(columnFilterPredicates, f))
                .collect(Collectors.toSet());

        if (!forbiddenFields.isEmpty()) {
            throw new ColumnUnavailableException(forbiddenFields);
        }
    }

    /**
     * Check that the "q" filters have defined target field, which is NOT a wildcard
     * @param request
     * @throws ColumnUnavailableException if a "q" has no target field
     */
    private static void assertQHasCol(Request request) throws ColumnUnavailableException {
        long qWithoutCol = Optional.ofNullable(request.filter).flatMap(filter -> Optional.ofNullable(filter.q)
                .map(q -> q.stream().flatMap(
                        qList -> qList.stream().filter(
                                qFilter -> !qFilter.contains(":") || StringUtils.substringBefore(qFilter, ":").contains("*")))
                        .count())).orElse(0l);

        if (qWithoutCol > 0) {
            throw new ColumnUnavailableException("Searching with 'q' parameter without an explicit column is not available");
        }
    }

    public static void assertFieldAvailable(Optional<String> columnFilter, CollectionReference collectionReference, String field) throws ColumnUnavailableException {
        Optional<Set<String>> columFilterPredicates = getColumnFilterPredicates(columnFilter, collectionReference);
        assertFieldAvailable(columFilterPredicates, field);
    }

    public static void assertFieldAvailable(Optional<Set<String>> columnFilterPredicates, String field) throws ColumnUnavailableException {

        if (!FilterMatcherUtil.matches(columnFilterPredicates, field)) {
            throw new ColumnUnavailableException(new HashSet<>(Arrays.asList(field)));
        }
    }

    /**
     * Check if an openGisFilter uses only fields allowed in column filter
     * @param columnFilter
     * @param collectionDescription
     * @param openGisFilter
     * @throws ColumnUnavailableException
     */
    public static void assertOpenGisFilterAllowed(Optional<String> columnFilter, CollectionReferenceDescription collectionDescription,
                                                  List<String> openGisFilter) throws ArlasException {

        Optional<String> cleanColumnFilter = ColumnFilterUtil.cleanColumnFilter(columnFilter);

        if (!cleanColumnFilter.isPresent()) {
            return;
        }

        Optional<Set<String>> columnFilterPredicates = ColumnFilterUtil.getColumnFilterPredicates(columnFilter, collectionDescription);
        Set<String> forbiddenFields = openGisFilter.stream()
                .filter(f -> !FilterMatcherUtil.matches(columnFilterPredicates, f))
                .collect(Collectors.toSet());

        if (!forbiddenFields.isEmpty()) {
            throw new ColumnUnavailableException(forbiddenFields);
        }
    }

    public static Optional<String> getFilteredIncludes(Optional<String> columnFilter, Projection projection, Set<String> collectionAllowedFields) {

        if (projection == null || StringUtils.isBlank(projection.includes)) {
            return Optional.ofNullable(columnFilter.get());
        }

        Optional<Set<String>> includesPredicates = FilterMatcherUtil.filterToPredicatesAsSet(Optional.of(projection.includes));

        return collectionAllowedFields.stream()
                .filter(
                        f -> FilterMatcherUtil.matches(includesPredicates, f))
                .reduce((left, right) -> left + "," + right);
    }

    /**
     * Check if a column filter is set, with valid values
     * @param columnFilter
     * @return
     */
    public static boolean isValidColumnFilterPresent(Optional<String> columnFilter) {
        return cleanColumnFilter(columnFilter).isPresent();
    }

    public static Optional<String> cleanColumnFilter(Optional<String> columnFilter) {

        if (columnFilter == null) {
            LOGGER.error("column filter is null, Optional.empty() is expected instead");
            return Optional.empty();
        }

        return columnFilter.filter(StringUtils::isNotBlank);
    }

    /**
     * Add collection mandatory fields, but only if a column filter is provided
     * @param columnFilter
     * @param collectionReference
     * @return
     */
    public static Optional<Set<String>> getColumnFilterPredicates(Optional<String> columnFilter, CollectionReference collectionReference) {
        return FilterMatcherUtil.filterToPredicatesAsStream(columnFilter)
                .map(getCollectionFilters(collectionReference))
                .map(cols -> Stream.concat(
                        cols,
                        getCollectionMandatoryPaths(collectionReference)
                                .stream()
                                .map(
                                        c -> c.replaceAll("\\.", "\\\\.")))
                        .collect(Collectors.toSet()));
    }

    public static List<String> getCollectionMandatoryPaths(CollectionReference collectionReference) {
        return Arrays.asList(
                collectionReference.params.idPath,
                collectionReference.params.geometryPath,
                collectionReference.params.centroidPath,
                collectionReference.params.timestampPath);
    }

    /**
     * Return the column filter related to a collection.
     * It DOES NOT include the collection mandatory path.
     * @param columnFilter
     * @param collectionReference
     * @return
     */
    public static Optional<String> getCollectionRelatedColumnFilter(Optional<String> columnFilter, CollectionReference collectionReference) {
        return cleanColumnFilter(columnFilter)
                .map(cf -> Arrays.stream(cf.split(",")))
                .map(getCollectionFilters(collectionReference))
                .map(cols -> cols.collect(Collectors.joining(",")))
                .filter(StringUtils::isNotBlank);
    }

    private static Function<Stream<String>, Stream<String>> getCollectionFilters(CollectionReference collectionReference) {
        return cols -> cols
                .filter(col -> !col.contains(":") // "param"
                        || col.indexOf(":") == 0 // ":param"
                        || ( // "col*:param" -> col and col_foo must match
                                (col.substring(0, col.indexOf(":")).endsWith("*") &&
                                        collectionReference.collectionName.startsWith(col.substring(0, col.indexOf("*"))))
                        )
                        || ( // "col:param" -> col must match but not "col_foo"
                                (!col.substring(0, col.indexOf(":")).endsWith("*") &&
                                        collectionReference.collectionName.equals(col.substring(0, col.indexOf(":"))))
                        )
                )
                .map(col -> col.contains(":") ? col.substring(col.indexOf(":")+1) : col);
    }

    public static void assertCollectionsAllowed(Optional<String> columnFilter, List<CollectionReference> collections) throws CollectionUnavailableException {
        Optional<String> cleanColumnFilter = cleanColumnFilter(columnFilter);

        if (!cleanColumnFilter.isPresent()) {
            return;
        }

        for (CollectionReference collection : collections) {
            if (!getCollectionRelatedColumnFilter(cleanColumnFilter, collection).isPresent()) {
                throw new CollectionUnavailableException(collection);
            }
        }
    }

    public static Set<String> getAllowedCollections(Optional<String> columnFilter) {
        if (columnFilter == null || !columnFilter.isPresent()) {
           return Collections.singleton("*"); // all collections allowed
        } else {
            Set<String> res = Arrays.stream(columnFilter.get().split(","))
                    .map(f -> f.contains(":") ? (f.split(":")[0].length() != 0 ? f.split(":")[0] : "*") : "*") // "param" or ":param" or "col:param"
                    .collect(Collectors.toSet());
            if (res.contains("*")) { // if one of the collection is "all" then
                return Collections.singleton("*"); // all collections allowed
            } else {
                return res;
            }
        }
    }
}
