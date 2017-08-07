package io.arlas.server.utils;

import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.request.*;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hamou on 24/04/17.
 */
public class ParamsParser {
    private static final String AGG_INTERVAL_PARAM = "interval-";
    private static final String AGG_FORMAT_PARAM = "format-";
    private static final String AGG_COLLECT_FIELD_PARAM = "collect_field-";
    private static final String AGG_COLLECT_FCT_PARAM = "collect_fct-";
    private static final String AGG_ORDER_PARAM = "order-";
    private static final String AGG_ON_PARAM = "on-";
    private static final String AGG_SIZE_PARAM = "size-";


    public static List<Aggregation> getAggregations(List<String> agg) throws ArlasException{
        List<Aggregation> aggregations = new ArrayList<>();
        if (agg != null && agg.size()>0 ){
            for (String aggregation : agg){
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

    public static Aggregation getAggregationModel(List<String> agg){
        Aggregation aggregationModel = new Aggregation();
        aggregationModel.type = agg.get(0);
        aggregationModel.field = agg.get(1);

        for (String parameter : agg){
            if (parameter.contains(AGG_INTERVAL_PARAM)){
                aggregationModel.interval = parameter.substring(AGG_INTERVAL_PARAM.length());
            }
            if (parameter.contains(AGG_FORMAT_PARAM)){
                aggregationModel.format = parameter.substring(AGG_FORMAT_PARAM.length());
            }
            if (parameter.contains(AGG_COLLECT_FIELD_PARAM)){
                aggregationModel.collectField = parameter.substring(AGG_COLLECT_FIELD_PARAM.length());
            }
            if (parameter.contains(AGG_COLLECT_FCT_PARAM)){
                aggregationModel.collectFct = parameter.substring(AGG_COLLECT_FCT_PARAM.length());
            }
            if (parameter.contains(AGG_ORDER_PARAM)){
                aggregationModel.order = parameter.substring(AGG_ORDER_PARAM.length());
            }
            if (parameter.contains(AGG_ON_PARAM)){
                aggregationModel.on = parameter.substring(AGG_ON_PARAM.length());
            }
            if (parameter.contains(AGG_SIZE_PARAM)){
                aggregationModel.size = parameter.substring(AGG_SIZE_PARAM.length());
            }
        }
        return aggregationModel;
    }

    public static String getAggregationParam (List<String> aggParameters, String param){
        for (String parameter : aggParameters){
            if (parameter.contains(param)){
                return parameter.substring(param.length());
            }
        }
        return null;
    }

    public static DateAggregationInterval getAggregationDateInterval(String aggInterval) throws ArlasException{
        DateAggregationInterval dateAggregationInterval = null;
        if (aggInterval != null && !aggInterval.equals("")){
            String[] sizeAndUnit = aggInterval.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=[a-zA-Z])");
            if (sizeAndUnit.length == 2) {
                dateAggregationInterval = new DateAggregationInterval();
                dateAggregationInterval.aggsize = tryParseInteger(sizeAndUnit[0]);
                if(dateAggregationInterval.aggsize == null){
                    throw new InvalidParameterException(FluidSearch.INVALID_SIZE + sizeAndUnit[0]);
                }
                dateAggregationInterval.aggunit = sizeAndUnit[1].toLowerCase();
                return dateAggregationInterval;
            }
            else throw new InvalidParameterException("The date interval " + aggInterval + "is not valid");
        }
        else throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
    }

    public static Integer getAggregationGeohasPrecision(String aggInterval) throws ArlasException{
        if(aggInterval != null){
            Integer precision = tryParseInteger(aggInterval);
            if (precision != null){
                if( precision >12 || precision<1)
                    throw new InvalidParameterException("Invalid geohash aggregation precision of " + precision + ". Must be between 1 and 12.");
                else return precision;
            }
            else throw new InvalidParameterException("Invalid geohash aggregation precision of  '" + aggInterval + "'. Must be an integer.");
        }
        else throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
    }

    public static Double getAggregationHistogramLength(String aggInterval) throws ArlasException{
        if(aggInterval != null){
            Double length = tryParseDouble(aggInterval);
            if (length != null){
                return length;
            }
            else throw new InvalidParameterException("Invalid histogram aggregation precision of '" + aggInterval + "'. Must be a numeric value.");
        }
        else throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
    }

    public static String getValidAggregationFormat(String aggFormat){
        //TODO: check if format is in DateTimeFormat (joda)
        if (aggFormat != null) {
            return aggFormat;
        }
        else {
            return "yyyy-MM-dd-HH:mm:ss";
        }
    }

    public static Filter getFilter(List<String> f, String q, LongParam before, LongParam after, String pwithin, String gwithin, String gintersect,String notpwithin, String notgwithin, String notgintersect) throws ArlasException{
        Filter filter = new Filter();
        filter.f  = f;
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
        filter.notpwithin =notpwithin;
        filter.notgwithin = notgwithin;
        filter.notgintersect = notgintersect;
        return filter;
    }

    public static Size getSize(IntParam size, IntParam from) throws ArlasException{
        Size sizeObject = new Size();
        sizeObject.size = size.get();
        sizeObject.from = from.get();
        return sizeObject;
    }

    public static Sort getSort(String sort){
        Sort sortObject = new Sort();
        sortObject.sort = sort;
        return sortObject;
    }

    public static Integer getValidAggregationSize(String size) throws ArlasException{
        Integer s  = tryParseInteger(size);
        if (s != null){
            return s;
        }
        else throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
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
