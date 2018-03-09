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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.*;
import io.arlas.server.rest.explore.enumerations.MetricAggregationEnum;
import io.dropwizard.jersey.params.IntParam;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParamsParser {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String AGG_INTERVAL_PARAM = "interval-";
    private static final String AGG_FORMAT_PARAM = "format-";
    private static final String AGG_COLLECT_FIELD_PARAM = "collect_field-";
    private static final String AGG_COLLECT_FCT_PARAM = "collect_fct-";
    private static final String AGG_ORDER_PARAM = "order-";
    private static final String AGG_ON_PARAM = "on-";
    private static final String AGG_SIZE_PARAM = "size-";
    private static final String AGG_INCLUDE_PARAM = "include-";
    private static final String AGG_GEOCENTROID_PARAM = "withGeoCentroid-";
    private static final String AGG_GEOBBOX_PARAM = "withGeoBBOX-";

    public static List<Aggregation> getAggregations(List<String> agg) throws ArlasException {
        List<Aggregation> aggregations = new ArrayList<>();
        if (agg != null && agg.size() > 0) {
            for (String aggregation : agg) {
                Aggregation aggregationModel;
                if (CheckParams.isAggregationParamValid(aggregation)) {
                    List<String> aggParameters = Arrays.asList(aggregation.split(":"));
                    aggregationModel = getAggregationModel(aggParameters);
                    aggregations.add(aggregationModel);
                }
            }
        }
        return aggregations;
    }

    public static Aggregation getAggregationModel(List<String> agg) throws ArlasException {
        Aggregation aggregationModel = new Aggregation();
        aggregationModel.type = AggregationTypeEnum.valueOf(agg.get(0));

        for (String parameter : agg) {
            if (parameter.contains(AGG_INTERVAL_PARAM)) {
                if (aggregationModel.type.equals(AggregationTypeEnum.datehistogram)) {
                    aggregationModel.interval = getDateInterval(parameter.substring(AGG_INTERVAL_PARAM.length()));
                } else {
                    aggregationModel.interval = new Interval(Integer.parseInt(parameter.substring(AGG_INTERVAL_PARAM.length())), null);
                }
            } else if (parameter.contains(AGG_FORMAT_PARAM)) {
                aggregationModel.format = parameter.substring(AGG_FORMAT_PARAM.length());
            } else if (parameter.contains(AGG_COLLECT_FIELD_PARAM)) {
                aggregationModel.collectField = parameter.substring(AGG_COLLECT_FIELD_PARAM.length());
            } else if (parameter.contains(AGG_COLLECT_FCT_PARAM)) {
                aggregationModel.collectFct = MetricAggregationEnum.valueOf(parameter.substring(AGG_COLLECT_FCT_PARAM.length()).toUpperCase());
            } else if (parameter.contains(AGG_ORDER_PARAM)) {
                aggregationModel.order = AggregationOrderEnum.valueOf(parameter.substring(AGG_ORDER_PARAM.length()));
            } else if (parameter.contains(AGG_ON_PARAM)) {
                aggregationModel.on = AggregationOnEnum.valueOf(parameter.substring(AGG_ON_PARAM.length()));
            } else if (parameter.contains(AGG_SIZE_PARAM)) {
                aggregationModel.size = parameter.substring(AGG_SIZE_PARAM.length());
            } else if (parameter.contains(AGG_INCLUDE_PARAM)) {
                aggregationModel.include = parameter.substring(AGG_INCLUDE_PARAM.length());
            } else if (parameter.contains(AGG_GEOCENTROID_PARAM)) {
                aggregationModel.withGeoCentroid = Boolean.valueOf(parameter.substring(AGG_GEOCENTROID_PARAM.length()));
            } else if (parameter.contains(AGG_GEOBBOX_PARAM)) {
                aggregationModel.withGeoBBOX = Boolean.valueOf(parameter.substring(AGG_GEOBBOX_PARAM.length()));
            } else if (parameter.equals(agg.get(1))) {
                aggregationModel.field = parameter;
            }
        }
        return aggregationModel;
    }

    public static Interval getDateInterval(String intervalString) throws ArlasException {
        if (intervalString != null && !intervalString.equals("")) {
            String[] sizeAndUnit = intervalString.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=[a-zA-Z])");
            if (sizeAndUnit.length == 2) {
                Interval interval = new Interval(tryParseInteger(sizeAndUnit[0]), UnitEnum.valueOf(sizeAndUnit[1].toLowerCase()));
                if (interval.value == null) {
                    throw new InvalidParameterException(FluidSearch.INVALID_SIZE + sizeAndUnit[0]);
                }
                return interval;
            } else throw new InvalidParameterException("The date interval " + intervalString + "is not valid");
        } else throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
    }

    public static Integer getAggregationGeohashPrecision(Interval interval) throws ArlasException {
        if (interval != null) {
            if (interval.value > 12 || interval.value < 1) {
                throw new InvalidParameterException("Invalid geohash aggregation precision of " + interval.value + ". Must be between 1 and 12.");
            } else {
                return interval.value;
            }
        } else {
            throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
        }
    }

    public static String getValidAggregationFormat(String aggFormat) {
        //TODO: check if format is in DateTimeFormat (joda)
        if (aggFormat != null) {
            return aggFormat;
        } else {
            return "yyyy-MM-dd-HH:mm:ss";
        }
    }

    public static Filter getFilter(String serializedFilter) throws InvalidParameterException {
        if(serializedFilter!=null) {
            try {
                return objectMapper.readValue(serializedFilter, Filter.class);
            } catch (IOException e) {
                throw new InvalidParameterException(FluidSearch.INVALID_FILTER + ": '" + serializedFilter + "'");
            }
        } else {
            return null;
        }
    }

    public static Filter getFilter(List<String> filters, List<String> q, List<String> pwithin, List<String> gwithin, List<String> gintersect, List<String> notpwithin, List<String> notgwithin, List<String> notgintersect) throws ArlasException {
        Filter filter = new Filter();
        filter.f = new ArrayList<>();

        for (String multiF : filters) {
            MultiValueFilter<Expression> multiFilter = new MultiValueFilter<>();
            for(String f : getMultiFiltersFromSemiColonsSeparatedString(multiF)) {
                if (!Strings.isNullOrEmpty(f)) {
                    String operands[] = f.split(":");
                    if (operands.length != 3) {
                        throw new InvalidParameterException(FluidSearch.INVALID_PARAMETER_F + ": '" + f + "'");
                    }
                    multiFilter.add(new Expression(operands[0], OperatorEnum.valueOf(operands[1]), operands[2]));
                }
            }
            filter.f.add(multiFilter);
        }
        filter.q = getStringMultiFilter(q);
        filter.pwithin = getStringMultiFilter(pwithin);
        filter.gwithin = getStringMultiFilter(gwithin);
        filter.gintersect = getStringMultiFilter(gintersect);
        filter.notpwithin = getStringMultiFilter(notpwithin);
        filter.notgwithin = getStringMultiFilter(notgwithin);
        filter.notgintersect = getStringMultiFilter(notgintersect);
        return filter;
    }

    public static List<MultiValueFilter<String>> getStringMultiFilter(List<String> filters) {
        List<MultiValueFilter<String>> ret = null;
        if(filters != null && !filters.isEmpty()) {
            ret = new ArrayList<>();
            for (String multiFilterString : filters) {
                MultiValueFilter<String> multiFilter = new MultiValueFilter<>();
                for (String filter : getMultiFiltersFromSemiColonsSeparatedString(multiFilterString)) {
                    if (!Strings.isNullOrEmpty(filter)) {
                        multiFilter.add(filter);
                    }
                }
                ret.add(multiFilter);
            }
        }
        return ret;
    }

    private static String[] getMultiFiltersFromSemiColonsSeparatedString(String filters) {
        return filters.split(";");
    }

    public static Size getSize(IntParam size, IntParam from) throws ArlasException {
        Size sizeObject = new Size();
        sizeObject.size = size.get();
        sizeObject.from = from.get();
        return sizeObject;
    }

    public static Sort getSort(String sort) {
        Sort sortObject = new Sort();
        sortObject.sort = sort;
        return sortObject;
    }

    public static GeoPoint getGeoSortParams(String geoSort) throws ArlasException {
        List<String> geoSortList = Arrays.asList(geoSort.split(":"));
        String geoDistance;
        String latLon;
        if (geoSortList.size() > 1) {
            geoDistance = geoSortList.get(0);
            latLon = geoSortList.get(1);
            if (!geoDistance.toLowerCase().equals(FluidSearch.GEO_DISTANCE)) {
                throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LABEL);
            }
        } else {
            throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LABEL);
        }
        String[] geoSortLatLon = latLon.split(" ");
        if (geoSortLatLon.length > 1) {
            Double lat = tryParseDouble(geoSortLatLon[0]);
            Double lon = tryParseDouble(geoSortLatLon[1]);
            if (lat != null && lon != null ) {
                return new GeoPoint(lat, lon);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LAT_LON);
            }
        } else {
            throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LAT_LON);
        }
    }

    public static Projection getProjection(String includes, String excludes) {
        Projection projObject = new Projection();
        projObject.includes = includes;
        projObject.excludes = excludes;
        return projObject;
    }

    public static Integer getValidAggregationSize(String size) throws ArlasException {
        Integer s = tryParseInteger(size);
        if (s != null) {
            return s;
        } else throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
    }

    public static void formatRangeValues(Long min, Long max, CollectionReference collectionReference) {
        String format = collectionReference.params.customParams.get(CollectionReference.TIMESTAMP_FORMAT);
        TimestampTypeMapper.formatDate(min, format);
        TimestampTypeMapper.formatDate(max, format);
    }

    public static List<String> simplifyPwithinAgainstBbox(List<String>  pwithin, BoundingBox bbox) throws ArlasException {
        List<String> simplifiedPwithin = new ArrayList<>();
        List<MultiValueFilter<String>> pwithinFilters = ParamsParser.getStringMultiFilter(pwithin);
        if(pwithinFilters != null && !pwithinFilters.isEmpty()){
            for(MultiValueFilter<String> pws : pwithinFilters) {
                StringBuffer buff = new StringBuffer();
                for(String pw : pws) {
                    BoundingBox simplifiedBbox = GeoTileUtil.bboxIntersects(bbox, pw);
                    if(simplifiedBbox != null && simplifiedBbox.getNorth() > simplifiedBbox.getSouth()) {
                        if(buff.length()>0)
                            buff.append(";");
                        buff.append(simplifiedBbox.getNorth() + "," + simplifiedBbox.getWest() + "," + simplifiedBbox.getSouth() + "," + simplifiedBbox.getEast());
                    }
                }
                if(buff.length()>0) {
                    simplifiedPwithin.add(buff.toString());
                }
            }
        }
        return simplifiedPwithin;
    }

    private static Integer tryParseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double tryParseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
