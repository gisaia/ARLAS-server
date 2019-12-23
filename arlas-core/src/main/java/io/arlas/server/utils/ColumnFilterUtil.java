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

    /**
     * Add collection mandatory fields, but only if a column filter is provided
     * @param columnFilter
     * @param collectionReference
     * @return
     */
    public static Optional<Set<String>> getColumnFilterPredicates(Optional<String> columnFilter, CollectionReference collectionReference) {
        return FilterMatcherUtil.filterToPredicatesAsStream(columnFilter)
                .map(cols -> Stream.concat(
                        cols,
                        Arrays.asList(
                                collectionReference.params.idPath,
                                collectionReference.params.geometryPath,
                                collectionReference.params.centroidPath,
                                collectionReference.params.timestampPath)
                                .stream()
                                .map(
                                        c -> c.replaceAll("\\.", "\\\\.")))
                        .collect(Collectors.toSet()));
    }

}
