package io.arlas.server.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.request.*;
import io.arlas.server.rest.explore.enumerations.AggregationType;
import io.arlas.server.rest.explore.enumerations.DateInterval;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String INVALID_AGGREGATION_PARAMETER = "Invalid aggregation syntax. Must start with {type}:{field}:...";
    private static final String INVALID_AGGREGATION = "Invalid aggregation parameters. Type and field must be specified";
    private static final String INVALID_AGGREGATION_TYPE = "Invalid aggregation TYPE. Must be datehistogram, geohash, histogram or terms ";

    public CheckParams() {
    }
    public static void checkAggregationRequest(AggregationRequest aggregationRequest) throws ArlasException{
        if (aggregationRequest == null || (aggregationRequest !=null && aggregationRequest.aggregations == null) ||
                (aggregationRequest !=null && aggregationRequest.aggregations != null && aggregationRequest.aggregations.aggregations == null))
            throw new BadRequestException("Aggregation should not be null");
        else if (aggregationRequest !=null){
            checkAggregation(aggregationRequest.aggregations);
        }
    }
    public static void checkFilter (Filter filter) throws ArlasException{
        if (filter.before != null || filter.after != null) {
            if ((filter.before != null && filter.before < 0) || (filter.after != null && filter.after < 0)
                    || (filter.before != null && filter.after != null && filter.before < filter.after))
                throw new InvalidParameterException(FluidSearch.INVALID_BEFORE_AFTER);
        }
        if (filter.pwithin != null && !filter.pwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(filter.pwithin);
            if (!(tlbr.length == 4 && tlbr[0] > tlbr[2] && tlbr[2] < tlbr[3])) {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (filter.notpwithin != null && !filter.notpwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(filter.notpwithin);
            if (!(tlbr.length == 4 && tlbr[0] > tlbr[2] && tlbr[2] < tlbr[3])) {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
    }

    public static void checkSize (Size size) throws ArlasException{
        if (size.size != null && size.size > 0) {
            if (size.from != null) {
                if(size.from < 0) {
                    throw new InvalidParameterException(FluidSearch.INVALID_FROM);
                }
            } else {
                //Default Value
                size.from = 0;
            }
        } else if (size.size == null){
            size.size = 10;
            if (size.from != null) {
                if(size.from < 0) {
                    throw new InvalidParameterException(FluidSearch.INVALID_FROM);
                }
            } else {
                //Default Value
                size.from = 0;
            }
        }
        else {
            throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
        }
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

    private static void checkAggregation(Aggregations aggregations) throws ArlasException{
        if (aggregations != null && aggregations.aggregations != null && aggregations.aggregations.size()>0){
            for (AggregationModel aggregationModel : aggregations.aggregations){
                if ( aggregationModel.type != null && aggregationModel.field != null){
                    if (!AggregationType.aggregationTypes().contains(aggregationModel.type)){
                        throw new InvalidParameterException(INVALID_AGGREGATION_TYPE);
                    }
                }
                else {
                    throw new InvalidParameterException(INVALID_AGGREGATION);
                }
            }
        }
    }

    // TODO: finish param check validation
    // Verify that interval-{interval} is set and that {interval} respects
    // {size}(unit) format.
    public static Boolean isDateIntervalAggregationValid(String[] aggParts) {

        return false;
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

    public static double[] toDoubles(String doubles) throws InvalidParameterException {
        try {
            return Arrays.stream(doubles.split(",")).mapToDouble(Double::parseDouble).toArray();
        } catch (Exception e) {
            throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
        }
    }
}
