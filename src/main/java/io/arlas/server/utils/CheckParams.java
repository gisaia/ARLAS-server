package io.arlas.server.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.search.sort.SortOrder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.rest.explore.enumerations.AggregationType;
import io.arlas.server.rest.explore.enumerations.DateInterval;

/**
 * Created by hamou on 13/04/17.
 */
public class CheckParams {
    private static final String POLYGON_TYPE = "POLYGON";
    private static final String INVALID_SORT_PARAMETER = "Invalid sort syntax. Please use the following syntax : 'fieldName:ASC' or 'fieldName:DESC'. ";
    private static final String INVALID_DATE_UNIT = "Invalid date unit. Please use the following list : year,quarter,month,week,day,hour,minute,second. ";
    private static final String INVALID_DATE_SIZE = "Invalid date size. Please specify an integer. ";
    private static final String INVALID_DATE_INTERVAL = "Invalid date interval. Please use the following syntax : '{size}(year,quarter,month,week,day,hour,minute,second). ";
    private static final String OUTRANGE_GEOHASH_PRECISION = "Precision must be between 1 and 12. ";
    private static final String INVALID_PRECISION_TYPE = "Precision must be an integer between 1 and 12. ";
    private static final String INVALID_INTERVAL_TYPE = "Interval must be numeric. ";
    private static final String INVALID_AGGREGATION_PARAMETER = "Invalid aggregation syntax. Must at least contain {type}:{field}";
    private static final String INVALID_AGGREGATION_TYPE = "Invalid aggregation TYPE. Must be datehistogram, geohash, histogram or terms ";

    public CheckParams() {
    }

    public static Boolean isPolygon(Geometry geometry) {
        String geometryType = geometry.getGeometryType().toUpperCase();
        if (geometryType.equals(POLYGON_TYPE))
            return true;
        else
            return false;
    }

    public static Boolean isSimplePolygon(Geometry geometry) {
        String geometryType = geometry.getGeometryType().toUpperCase();
        if (isPolygon(geometry)) {
            Integer interiorRings = ((Polygon) geometry).getNumInteriorRing();
            return interiorRings == 0;
        } else
            return false;
    }

    // Verify that the sort parameter respects the specified syntax. Returns the
    // field and the ASC/DESC
    public static String[] checkSortParam(String sort) throws ArlasException {
        String[] sortOperands = sort.split(":");
        if (sortOperands.length == 3) {
            if (sortOperands[2].equals(SortOrder.ASC) || sortOperands[2].equals(SortOrder.DESC)) {
                return sortOperands;
            } else
                throw new InvalidParameterException(INVALID_SORT_PARAMETER);
        } else
            throw new InvalidParameterException(INVALID_SORT_PARAMETER);
    }

    // Verify if agg parameter contains at least type:field and verify that type
    // matches : datehistogram, geohash, histogram or terms
    public static Boolean isAggregationParamValid(String agg) throws ArlasException {
        String[] aggParts = agg.split(":");
        if (aggParts.length > 1) {
            if (AggregationType.aggregationTypes().contains(aggParts[0]))
                return true;
            else
                throw new InvalidParameterException(INVALID_AGGREGATION_TYPE);
        } else
            throw new InvalidParameterException(INVALID_AGGREGATION_PARAMETER);
    }

    // TODO: finish param check validation
    // Verify that interval-{interval} is set and that {interval} respects
    // {size}(unit) format.
    public static Boolean isDateIntervalAggregationValid(String[] aggParts) {

        return false;
    }

    // verify if the aggregation interval suits the date type aggregation.
    // Retruns the interval (size+unit) if it is ok.
    public static Map<Integer, String> getValidAggDateInterval(String aggInterval) throws ArlasException {
        if (aggInterval != null) {
            String[] interval = aggInterval.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=[a-zA-Z])");
            if (interval.length == 2) {
                Integer size = tryParseInteger(interval[0]);
                String unit = interval[1].toLowerCase();
                if (size != null) {
                    if (DateInterval.contains(unit)) {
                        Map<Integer, String> map = new HashMap<Integer, String>();
                        map.put(size, unit);
                        return map;
                    } else
                        throw new InvalidParameterException(INVALID_DATE_UNIT);
                } else
                    throw new InvalidParameterException(INVALID_DATE_SIZE);
            } else
                throw new InvalidParameterException(INVALID_DATE_INTERVAL);
        }
        return null;
    }

    public static Integer getValidGeoHashPrecision(String aggInterval) throws ArlasException {
        if (aggInterval != null) {
            Integer precision = tryParseInteger(aggInterval);
            if (precision != null && precision < 13) {
                return precision;
            } else if (precision > 12)
                throw new InvalidParameterException(OUTRANGE_GEOHASH_PRECISION);
            else if (precision == null)
                throw new InvalidParameterException(INVALID_PRECISION_TYPE);
        }
        return null;
    }

    public static Double getValidHistogramInterval(String aggInterval) throws ArlasException {
        if (aggInterval != null) {
            Double interval = tryParseDouble(aggInterval);
            if (interval != null) {
                return interval;
            } else
                throw new InvalidParameterException(INVALID_INTERVAL_TYPE);
        }
        return null;
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

    public static double[] toDoubles(String doubles) {
        return Arrays.stream(doubles.split(",")).mapToDouble(Double::parseDouble).toArray();
    }
}
