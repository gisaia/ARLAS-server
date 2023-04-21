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
package io.arlas.server.core.services;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.AggregationTypeEnum;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.request.Aggregation;
import io.arlas.server.core.model.request.Expression;
import io.arlas.server.core.model.request.MultiValueFilter;
import io.arlas.server.core.model.request.Page;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class FluidSearchService {
    private static Logger LOGGER = LoggerFactory.getLogger(FluidSearchService.class);
    public static final String INVALID_FILTER = "Invalid filter parameter.";
    public static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    public static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'eq', gte', 'gt', 'lte', 'lt', 'like' or 'ne'. ";
    public static final String INVALID_Q_FILTER = "Invalid parameter. Please specify the text to search directly or '{fieldname}:{text to search}'. ";
    public static final String INVALID_BBOX = "Invalid BBOX";
    public static final String INVALID_SIZE = "Invalid size parameter. It should be a strictly positive integer";
    public static final String INVALID_FROM = "Invalid from parameter: should be a positive integer.";
    public static final String INVALID_DATE_UNIT = "Invalid date unit.";
    public static final String INVALID_GEOSORT_LAT_LON = "'lat lon' must be numeric values separated by a space";
    public static final String INVALID_GEOSORT_LABEL = "To sort by geo_distance, please specifiy the point, from which the distances are calculated, as following 'geodistance:lat lon'";
    public static final String INVALID_TIMESTAMP_RANGE = "Timestamp range values must be a timestamp in millisecond or a date expression. Otherwise, please set the `dateformat` parameter if your date value has a custom format";

    public static final String DATEHISTOGRAM_AGG = "Datehistogram aggregation";
    public static final String HISTOGRAM_AGG = "Histogram aggregation";
    public static final String TERM_AGG = "Term aggregation";
    public static final String GEOHASH_AGG = "Geohash aggregation";
    public static final String GEOTILE_AGG = "Geotile aggregation";
    public static final String GEOHEX_AGG = "Geohex aggregation";
    public static final String FETCH_HITS_AGG = "fetched_hits";
    public static final String GEO_DISTANCE = "geodistance";
    public static final String NO_INCLUDE_TO_SPECIFY = "'include-' should not be specified for this aggregation";
    public static final String NO_FORMAT_TO_SPECIFY = "'format-' should not be specified for this aggregation.";
    public static final String NO_SIZE_TO_SPECIFY = "'size-' should not be specified for this aggregation.";
    public static final String NO_ORDER_ON_TO_SPECIFY = "'order-' and 'on-' should not be specified for this aggregation.";
    public static final String COLLECT_FCT_NOT_SPECIFIED = "The aggregation function 'collect_fct' is not specified.";
    public static final String COLLECT_FIELD_NOT_SPECIFIED = "The aggregation field 'collect_field' is not specified.";
    public static final String ORDER_NOT_SPECIFIED = "'order-' is not specified.";
    public static final String ON_NOT_SPECIFIED = "'on-' is not specified.";
    public static final String ORDER_PARAM_NOT_ALLOWED = "Order is not allowed for geohash aggregation.";
    public static final String ORDER_ON_RESULT_NOT_ALLOWED = "'on-result' sorts 'collect_field' and 'collect_fct' results. Please specify 'collect_field' and 'collect_fct'.";
    public static final String ORDER_ON_GEO_RESULT_NOT_ALLOWED = "Ordering on 'result' is not allowed for geo-box neither geo-centroid metric aggregation. ";
    public static final String SIZE_NOT_IMPLEMENTED = "Size is not implemented for geohash/geotile.";

    public static final String FIELD_MIN_VALUE = "field_min_value";
    public static final String FIELD_MAX_VALUE = "field_max_value";
    public static final String FIELD_AVG_VALUE = "field_avg_value";
    public static final String FIELD_SUM_VALUE = "field_sum_value";
    public static final String FIELD_CARDINALITY_VALUE = "field_cardinality_value";
    public static final String FIELD_GEOBBOX_VALUE = "field_bbox_value";
    public static final String FIELD_GEOCENTROID_VALUE = "field_centroid_value";

    public static final String AGGREGATED_GEOMETRY_SUFFIX = "_aggregated_geometry";
    public static final String RAW_GEOMETRY_SUFFIX = "_raw_geometry";

    protected CollectionReference collectionReference;
    protected CollectionReferenceManager collectionReferenceManager;
    protected List<String> include = new ArrayList<>();
    protected List<String> exclude = new ArrayList<>();

    public FluidSearchService(CollectionReference collectionReference) {
        this.collectionReferenceManager = CollectionReferenceManager.getInstance();
        this.collectionReference = collectionReference;
    }

    public CollectionReference getCollectionReference() {
        return collectionReference;
    }

    public List<String> getCollectionPaths() {
        return Arrays.asList(collectionReference.params.idPath,
                collectionReference.params.geometryPath,
                collectionReference.params.centroidPath,
                collectionReference.params.timestampPath);
    }

    // -------

    public Pair<String[], String[]> computeIncludeExclude(boolean expand) throws ArlasException {
        if (!StringUtil.isNullOrEmpty(collectionReference.params.excludeFields)) {
            if (exclude.isEmpty()) {
                exclude(collectionReference.params.excludeFields);
            } else {
                Set<String> excludeSet = new HashSet<>();
                excludeSet.addAll(exclude);
                excludeSet.addAll(Arrays.asList(collectionReference.params.excludeFields.split(",")));
                exclude = new ArrayList<>(excludeSet);
            }
        }
        List<String> includeFieldList = new ArrayList<>();
        if (!include.isEmpty()) {
            includeFieldList.addAll(include);
            for (String path : getCollectionPaths()) {
                boolean alreadyIncluded = false;
                for (String includeField : include) {
                    if (includeField.equals("*") || (!StringUtil.isNullOrEmpty(path) && path.startsWith(includeField))) {
                        alreadyIncluded = true;
                        break;
                    }
                }
                if (!alreadyIncluded && !StringUtil.isNullOrEmpty(path) ) {
                    includeFieldList.add(path);
                }
            }
        }
        String[] includeFields = new HashSet<>(includeFieldList).toArray(new String[0]);
        if (includeFields.length == 0) {
            includeFields = expand ? collectionReferenceManager
                    .collectionReferenceService
                    .getCollectionFields(collectionReference, Optional.empty())
                    .toArray(new String[0]): new String[]{"*"};
        }
        String[] excludeFields = exclude.toArray(new String[exclude.size()]);
        return Pair.of(includeFields, excludeFields);
    }

    public FluidSearchService exclude(String exclude) {
        Optional.ofNullable(exclude).ifPresent(e -> this.exclude.addAll(Arrays.asList(e.split(","))));
        return this;
    }

    public FluidSearchService include(String include) {
        Optional.ofNullable(include).ifPresent(e -> this.include.addAll(Arrays.asList(e.split(","))));
        return this;
    }

    public boolean isDateField(String field) throws ArlasException {
        return collectionReferenceManager.getCollectionReferenceService().isDateField(field, collectionReference.params.indexName);
    }

    public boolean isGeoField(String field) throws ArlasException {
        return collectionReferenceManager.getCollectionReferenceService().isGeoField(field, collectionReference.params.indexName);
    }

    public static String getAggregationName(String aggName) {
        switch (AggregationTypeEnum.valueOf(aggName)) {
            case datehistogram:
                return DATEHISTOGRAM_AGG;
            case geohash:
                return GEOHASH_AGG;
            case geotile:
                return GEOTILE_AGG;
            case histogram:
                return HISTOGRAM_AGG;
            case term:
                return TERM_AGG;
            case geohex:
                return GEOHEX_AGG;
            default:
                LOGGER.warn("Getting name for a non defined aggregation type: " + aggName);
                return aggName;
        }
    }

    // -------

    public abstract FluidSearchService aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException;

    public abstract FluidSearchService compute(String field, ComputationEnum metric, int precisionThresold) throws ArlasException;

    abstract public FluidSearchService filter(MultiValueFilter<Expression> f, String dateFormat, Boolean rightHand) throws ArlasException;

    abstract public FluidSearchService filterQ(MultiValueFilter<String> q) throws ArlasException;

    abstract public FluidSearchService sort(String sort) throws ArlasException;

    abstract public FluidSearchService filterSize(Integer size, Integer from);

    abstract public FluidSearchService searchAfter(Page page, String after);
}
