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

import io.arlas.server.model.CollectionReference;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide functions to check columns against a column filter.
 *
 * The principle is:
 * - if the filter is empty, do not filter at all;
 * - if there is a filter, turn it to regexp predicates
 * - append some mandatory fields to it (from the collection)
 * - checking a path is then a simple regexp match
 */
public class ColumnFilterUtil {

    public static boolean isAllowed(Optional<Set<String>> columnFilter, String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }

        return columnFilter.map(
                cf -> cf.stream().anyMatch(c ->
                        //for fields: test if match. For parent path: also test if a filter starts with path
                        path.matches(c) || c.startsWith(path + "\\.")))
                //default if filter isn't present: allow
                .orElse(true);
    }

    /**
     * Add collection mandatory fields, but only if a column filter is provided
     * @param columnFilter
     * @param collectionReference
     * @return
     */
    public static Optional<Set<String>> getColumnFilterPredicates(Optional<String> columnFilter, CollectionReference collectionReference) {
        return filterToPredicates(columnFilter, collectionReference)
                .map(cols -> Stream.concat(
                        cols,
                        getCollectionPredicates(collectionReference))
                        .collect(Collectors.toSet()));
    }

    /**
     * Append the collection mandatory fields to the  filter.
     * Convert the filter to regexp predicates that can be used later to check if a field is allowed.
     * @param filter a list of coma separated fields. Wildcards are supported
     * @param collectionReference
     * @return
     */
    public static Optional<Stream<String>> filterToPredicates(Optional<String> filter, CollectionReference collectionReference) {
        return filter
                .filter(StringUtils::isNotBlank)
                .map(cf -> Arrays.stream(
                        cf
                                //some cleaning just in case
                                .replaceAll(" ", "")
                                //prepare regexp matching: in regexp a "." is any char. Escaping those in the filter
                                .replaceAll("\\.", "\\\\.")
                                //in regexp, the wildcard ".*" indicates any character repeated 0 to n times
                                .replaceAll("\\*", ".*")
                                .split(","))
                        //filters not ending with ".*" are duplicated to same filter postfixed with ".*"
                        //eg. the param "params" allows the fields "params.weight", "params.age" aso.
                        .map(c -> c.endsWith(".*") ? Arrays.asList(c) : Arrays.asList(c, c + "\\..*"))
                        .flatMap(Collection::stream));
    }

    private static Stream<String> getCollectionPredicates(CollectionReference collectionReference) {
        return Arrays.asList(
                collectionReference.params.idPath,
                collectionReference.params.geometryPath,
                collectionReference.params.centroidPath,
                collectionReference.params.timestampPath)
                .stream()
                .map(
                        c -> c.replaceAll("\\.", "\\\\."));
    }

}
