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

package io.arlas.server.ogc.common.utils;

import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.InvalidParameterException;
import org.geotools.filter.BinaryLogicAbstract;
import org.geotools.filter.MultiCompareFilterImpl;
import org.opengis.filter.*;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.temporal.BinaryTemporalOperator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extract the fields of an OpenGIS filter
 */
public class OpenGISFieldsExtractor {

    public static List<String> extract(Filter filter) throws InvalidParameterException, InternalServerErrorException {
        return getFieldsExtractorFromFilter(filter).doExtract(filter);
    }

    @FunctionalInterface
    private interface FieldsExtractor<T extends Filter> {
        List<String> doExtract(T filter) throws InvalidParameterException, InternalServerErrorException;
    }

    //Map of (OpenGIS Filter Implementation -> a method that returns the fields within)
    static private Map<Class<? extends Filter>, FieldsExtractor> OPENGIS_FILTER_FIELD_EXTRACTORS = new HashMap<>();
    static {
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(MultiCompareFilterImpl.class, (FieldsExtractor<MultiCompareFilterImpl>) filter -> Arrays.asList(filter.getExpression1().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(BinaryLogicAbstract.class, (FieldsExtractor<BinaryLogicAbstract>) filter -> {
            List<String> fields= new ArrayList<>();
            for (Filter subFilter : filter.getChildren()) {
                fields.addAll(extract(subFilter));
            }
            return fields;
        });
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(PropertyIsBetween.class, (FieldsExtractor<PropertyIsBetween>) filter -> Arrays.asList(filter.getExpression().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(PropertyIsNull.class, (FieldsExtractor<PropertyIsNull>) filter -> Arrays.asList(filter.getExpression().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(PropertyIsNil.class, (FieldsExtractor<PropertyIsNil>) filter -> Arrays.asList(filter.getExpression().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(PropertyIsLike.class, (FieldsExtractor<PropertyIsLike>) filter -> Arrays.asList(filter.getExpression().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(BinaryTemporalOperator.class, (FieldsExtractor<BinaryTemporalOperator>) filter -> Arrays.asList(filter.getExpression1().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(BinarySpatialOperator.class, (FieldsExtractor<BinarySpatialOperator>) filter -> Arrays.asList(filter.getExpression1().toString()));
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(Id.class, (FieldsExtractor<Id>) filter -> Arrays.asList());
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(IncludeFilter.class, (FieldsExtractor<IncludeFilter>) filter -> Arrays.asList());
        OPENGIS_FILTER_FIELD_EXTRACTORS.put(ExcludeFilter.class, (FieldsExtractor<ExcludeFilter>) filter -> Arrays.asList());
    }

    private static FieldsExtractor getFieldsExtractorFromFilter(Filter openGisFilter) throws InvalidParameterException, InternalServerErrorException {

        List<FieldsExtractor> extractors = OPENGIS_FILTER_FIELD_EXTRACTORS.entrySet().stream()
                .filter(e -> e.getKey().isInstance(openGisFilter))
                .map(e -> e.getValue())
                .collect(Collectors.toList());

        if (extractors.isEmpty()) {
            throw new InvalidParameterException("Filter " + openGisFilter.getClass().getSimpleName() + " is not supported in " + OpenGISFieldsExtractor.class.getSimpleName());

        } else if (extractors.size() > 1) {
            throw new InternalServerErrorException("Multiple extractors found for filter " + openGisFilter.getClass().getSimpleName());

        } else {
            return extractors.get(0);
        }
    }

}
