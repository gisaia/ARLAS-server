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

package io.arlas.server.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A filter here is a comma-separated list of fields or pattern.
 * This util provides functions to check json paths against a filter .
 */
public class FilterMatcherUtil {

    //use a cache with compiled patterns
    private static final Map<String, Pattern> PATTERN_COMPILED_CACHE = new HashMap<>();
    private static final Pattern EMPTY_PATTERN = Pattern.compile("");

    //these are metacharacters, i.a. characters that can be used within regexp. If present in filter: we escape them. Except the "star" that is a wildcard.
    private static final Map<String, String> PREDICATE_REPLACE_CHAR = Arrays.asList("\\","^","$","{","}","[","]","(",")",".","+","?","|","<",">","-","&","%").stream()
            .collect(Collectors.toMap(c -> c, c -> "\\" + c));
    static {
        PREDICATE_REPLACE_CHAR.put(" ", "");
        PREDICATE_REPLACE_CHAR.put("*", ".*");
    }

    /**
     * Check if a field matches the predicates
     * @param predicates
     * @param field
     * @return
     */
    public static boolean matches(Optional<Set<String>> predicates, String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }

        return predicates.map(
                cf -> cf.stream().anyMatch(c ->
                        addOrGetFromCache(c).matcher(field).matches()))
                //default if filter isn't present: allow
                .orElse(true);
    }

    /**
     * Check if a path or any of its subpath matches the predicates.
     * Checking paths `params` and `params.city`, this will return `true` for both if `params.city` belongs to predicates
     * @param predicates
     * @param path
     * @param checkWithin
     * @return
     */
    public static boolean matchesOrWithin(Optional<Set<String>> predicates, String path, boolean checkWithin) {
        if (StringUtils.isBlank(path)) {
            return false;
        }

        return predicates.map(
                cf -> cf.stream().anyMatch(c ->
                        //for fields: test if match. For parent path: also test if a filter starts with path
                        addOrGetFromCache(c).matcher(path).matches() || checkWithin && c.startsWith(path + "\\.")))
                //default if filter isn't present: allow
                .orElse(true);
    }

    /**
     * Convert the filter to regexp predicates that can be used later to check if a field is allowed.
     * @param filter a list of comma separated fields. Wildcards are supported
     * @return
     */
    public static Optional<Stream<String>> filterToPredicatesAsStream(Optional<String> filter) {
        return filter
                .filter(StringUtils::isNotBlank)
                .map(cf -> Arrays.stream(cf.split(","))
                        .map(c -> {
                            String[] cString = c.split(":");
                            if (Arrays.asList(cString).size() == 2) {
                                // input: collection*:param* => avoid replacing collection* with collection.*.
                                // Only param* should be replaced with param.*
                                return cString[0] + ':' + EMPTY_PATTERN.splitAsStream(cString[1])
                                        .map(cs -> PREDICATE_REPLACE_CHAR.containsKey(cs) ? PREDICATE_REPLACE_CHAR.get(cs) : cs)
                                        .collect(Collectors.joining());
                            }
                            return EMPTY_PATTERN.splitAsStream(c)
                                    .map(cs -> PREDICATE_REPLACE_CHAR.containsKey(cs) ? PREDICATE_REPLACE_CHAR.get(cs) : cs)
                                    .collect(Collectors.joining());

                        })
                        //filters not ending with ".*" are duplicated to same filter postfixed with ".*"
                        //eg. the param "params" allows the fields "params.weight", "params.age" aso.
                        .map(c2 -> c2.endsWith(".*") ? Arrays.asList(c2) : Arrays.asList(c2, c2 + "\\..*"))
                        .flatMap(Collection::stream)
                );
    }

    public static Optional<Set<String>> filterToPredicatesAsSet(Optional<String> filter) {
        return filterToPredicatesAsStream(filter)
                .map(
                        p -> p.collect(Collectors.toSet()));
    }

    /**
     * Get a pattern from a cache. If it doesn't already exists, the pattern is compiled and cached.
     * @param pattern
     * @return
     */
    private static Pattern addOrGetFromCache(String pattern) {
        if (PATTERN_COMPILED_CACHE.containsKey(pattern)) {
            return PATTERN_COMPILED_CACHE.get(pattern);
        } else {
            Pattern compiled = Pattern.compile(pattern);
            PATTERN_COMPILED_CACHE.put(pattern, compiled);
            return compiled;
        }
    }

}
