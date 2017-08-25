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

import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.request.*;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.elasticsearch.common.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParamsParser {
    private static final String AGG_INTERVAL_PARAM = "interval-";
    private static final String AGG_FORMAT_PARAM = "format-";
    private static final String AGG_COLLECT_FIELD_PARAM = "collect_field-";
    private static final String AGG_COLLECT_FCT_PARAM = "collect_fct-";
    private static final String AGG_ORDER_PARAM = "order-";
    private static final String AGG_ON_PARAM = "on-";
    private static final String AGG_SIZE_PARAM = "size-";

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
        aggregationModel.field = agg.get(1);

        for (String parameter : agg) {
            if (parameter.contains(AGG_INTERVAL_PARAM)) {
                if (aggregationModel.type.equals(AggregationTypeEnum.datehistogram)) {
                    aggregationModel.interval = getDateInterval(parameter.substring(AGG_INTERVAL_PARAM.length()));
                } else {
                    aggregationModel.interval = new Interval(Integer.parseInt(parameter.substring(AGG_INTERVAL_PARAM.length())), null);
                }
            }
            if (parameter.contains(AGG_FORMAT_PARAM)) {
                aggregationModel.format = parameter.substring(AGG_FORMAT_PARAM.length());
            }
            if (parameter.contains(AGG_COLLECT_FIELD_PARAM)) {
                aggregationModel.collectField = parameter.substring(AGG_COLLECT_FIELD_PARAM.length());
            }
            if (parameter.contains(AGG_COLLECT_FCT_PARAM)) {
                aggregationModel.collectFct = parameter.substring(AGG_COLLECT_FCT_PARAM.length());
            }
            if (parameter.contains(AGG_ORDER_PARAM)) {
                aggregationModel.order = AggregationOrderEnum.valueOf(parameter.substring(AGG_ORDER_PARAM.length()));
            }
            if (parameter.contains(AGG_ON_PARAM)) {
                aggregationModel.on = AggregationOnEnum.valueOf(parameter.substring(AGG_ON_PARAM.length()));
            }
            if (parameter.contains(AGG_SIZE_PARAM)) {
                aggregationModel.size = parameter.substring(AGG_SIZE_PARAM.length());
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

    public static Integer getAggregationGeohasPrecision(Interval interval) throws ArlasException {
        if (interval != null) {
            if (interval.value > 12 || interval.value < 1) {
                throw new InvalidParameterException("Invalid geohash aggregation precision of " + interval.value + ". Must be between 1 and 12.");
            } else return interval.value;
        }
        throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
    }

    public static String getValidAggregationFormat(String aggFormat) {
        //TODO: check if format is in DateTimeFormat (joda)
        if (aggFormat != null) {
            return aggFormat;
        } else {
            return "yyyy-MM-dd-HH:mm:ss";
        }
    }

    public static Filter getFilter(List<String> filters, String q, LongParam before, LongParam after, String pwithin, String gwithin, String gintersect, String notpwithin, String notgwithin, String notgintersect) throws ArlasException {
        Filter filter = new Filter();
        filter.f = new ArrayList<>();

        for (String f : filters) {
            if (!Strings.isNullOrEmpty(f)) {
                String operands[] = f.split(":");
                if (operands.length != 3) {
                    throw new InvalidParameterException(FluidSearch.INVALID_PARAMETER_F+": '"+f+"'");
                }
                filter.f.add(new Expression(operands[0], OperatorEnum.valueOf(operands[1]), operands[2]));
            }
        }
        filter.q = q;
        if (after != null) {
            filter.after = after.get();
        }
        if (before != null) {
            filter.before = before.get();
        }
        filter.pwithin = pwithin;
        filter.gwithin = gwithin;
        filter.gintersect = gintersect;
        filter.notpwithin = notpwithin;
        filter.notgwithin = notgwithin;
        filter.notgintersect = notgintersect;
        return filter;
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
