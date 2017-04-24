package io.arlas.server.utils;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.AggregationModel;

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


    public static AggregationModel getAggregation(List<String> aggParameters){
        AggregationModel aggregationModel = new AggregationModel();
        aggregationModel.aggType = aggParameters.get(0);
        aggregationModel.aggField = aggParameters.get(1);

        for (String parameter : aggParameters){
            if (parameter.contains(AGG_INTERVAL_PARAM)){
                aggregationModel.aggInterval = parameter.substring(AGG_INTERVAL_PARAM.length());
            }
            if (parameter.contains(AGG_FORMAT_PARAM)){
                aggregationModel.aggFormat = parameter.substring(AGG_FORMAT_PARAM.length());
            }
            if (parameter.contains(AGG_COLLECT_FIELD_PARAM)){
                aggregationModel.aggCollectField = parameter.substring(AGG_COLLECT_FIELD_PARAM.length());
            }
            if (parameter.contains(AGG_COLLECT_FCT_PARAM)){
                aggregationModel.aggCollectFct = parameter.substring(AGG_COLLECT_FCT_PARAM.length());
            }
            if (parameter.contains(AGG_ORDER_PARAM)){
                aggregationModel.aggOrder = parameter.substring(AGG_ORDER_PARAM.length());
            }
            if (parameter.contains(AGG_ON_PARAM)){
                aggregationModel.aggOn = parameter.substring(AGG_ON_PARAM.length());
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
        if (!aggInterval.equals("")){
            String[] sizeAndUnit = aggInterval.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=[a-zA-Z])");
            if (sizeAndUnit.length == 2) {
                dateAggregationInterval = new DateAggregationInterval();
                dateAggregationInterval.aggsize = tryParseInteger(sizeAndUnit[0]);
                dateAggregationInterval.aggunit = sizeAndUnit[1].toLowerCase();
                return dateAggregationInterval;
            }
            else throw new InvalidParameterException("The date interval " + aggInterval + "is not valid");
        }
        else throw new InvalidParameterException("No date interval specified");
    }

    public static Integer getAggregationGeohasPrecision(String aggInterval) throws ArlasException{
        Integer precision = tryParseInteger(aggInterval);
        if (precision != null){
            return precision;
        }
        else throw new InvalidParameterException(aggInterval + " must be an integer.");
    }

    public static Double getAggregationHistogramLength(String aggInterval) throws ArlasException{
        Double length = tryParseDouble(aggInterval);
        if (length != null){
            return length;
        }
        else throw new InvalidParameterException(aggInterval + " must be a numeric.");
    }

    public static String getValidAggregationFormat(String aggFormat){
        //TODO: check if format is in DateTimeFormat (joda)
        if (aggFormat != null) {
            return aggFormat;
        }
        else {
            return "yyyyMMdd";
        }
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
