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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.request.*;
import io.arlas.server.model.request.AggregationTypeEnum;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Arrays;

public class CheckParams {
    private static final String POLYGON_TYPE = "POLYGON";
    private static final String INVALID_SORT_PARAMETER = "Invalid sort syntax. Please use the following syntax : 'fieldName:ASC' or 'fieldName:DESC'. ";
    private static final String INVALID_XYZ_PARAMETER = "Z must be between 0 and 22. X and Y must be between 0 and (2^Z-1)";
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

    public static void checkAggregationRequest(Request request) throws ArlasException{
        if (request == null ||  !(request instanceof AggregationsRequest) || ((AggregationsRequest)request).aggregations == null)
            throw new BadRequestException("Aggregation should not be null");
        else if (request !=null){
            checkAggregation((AggregationsRequest)request);
        }
    }

    public static void checkFilter (Filter filter) throws ArlasException{
        if (filter.pwithin != null && !filter.pwithin.isEmpty()) {
            for(MultiValueFilter<String> multiPwithin : filter.pwithin) {
                for(String pw : multiPwithin) {
                    checkBbox(pw);
                }
            }
        }
        if (filter.notpwithin != null && !filter.notpwithin.isEmpty()) {
            for(MultiValueFilter<String> multiNotPwithin : filter.notpwithin) {
                for(String npw : multiNotPwithin) {
                    checkBbox(npw);
                }
            }
        }
    }

    public static void checkBbox(String bbox) throws InvalidParameterException {
        double[] tlbr = CheckParams.toDoubles(bbox);
        // west, south, east, north
        if (!(tlbr.length == 4 && isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
        }
    }

    public static void checkNullityOfAggregationIncludeParameter(String include) throws ArlasException {
        if (include != null) {
            throw new BadRequestException(FluidSearch.NO_INCLUDE_TO_SPECIFY);
        }
    }

    public static void checkNullityOfAggregationIntervalParameter(Interval interval) throws ArlasException {
        if(interval==null || interval.value==null){
            throw new BadRequestException(FluidSearch.INTREVAL_NOT_SPECIFIED);
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

    public static void checkRangeValidity(String range) throws ArlasException {
        if((range.isEmpty() || !(range.startsWith("[") || range.startsWith("]")) ||
                        !(range.endsWith("[") || range.endsWith("]")) ||
                        !(range.contains("<")))) {
            throw new java.security.InvalidParameterException(FluidSearch.INVALID_PARAMETER_F);
        }
    }

    public static void checkTimestampFormatValidity(String timestamp) throws ArlasException {
        if (tryParseLong(timestamp) == null) {
            throw new InvalidParameterException(FluidSearch.INVALID_TIMESTAMP_RANGE);
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
            try{
                AggregationTypeEnum.valueOf(aggParts[0]);
                return true;
            }catch(Exception e){
                throw new InvalidParameterException(INVALID_AGGREGATION_TYPE);
            }
        } else {
            throw new InvalidParameterException(INVALID_AGGREGATION_PARAMETER);
        }
    }

    private static void checkAggregation(AggregationsRequest aggregations) throws ArlasException{
        if (aggregations != null && aggregations.aggregations != null && aggregations.aggregations.size()>0){
            for (Aggregation aggregationModel : aggregations.aggregations){
                if ( aggregationModel.type == null ||
                        (aggregationModel.field == null && aggregationModel.type != AggregationTypeEnum.datehistogram)){
                    throw new InvalidParameterException(INVALID_AGGREGATION);
                }
            }
        }
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

    public static void checkXYZTileValidity (int x, int y, int z) throws ArlasException {
        if (z>=0 && z<=22) {
            if (!isIntegerInXYZRange(x, z) || !isIntegerInXYZRange(x, z)){
                throw new InvalidParameterException(INVALID_XYZ_PARAMETER);
            }
        } else {
            throw new InvalidParameterException(INVALID_XYZ_PARAMETER);
        }
    }

    public static boolean isBboxLatLonInCorrectRanges(double[] tlbr) {
        // west, south, east, north
        return tlbr[1] >= -90 && tlbr[3] >= -90 && tlbr[1] <= 90 && tlbr[3] <= 90 &&
                tlbr[0] >= -180 && tlbr[2] >= -180 && tlbr[0] <= 180 && tlbr[2] <= 180;
    }

    private static boolean isIntegerInXYZRange(int n, int z) {
        long minRange = 0;
        long maxRange = (long)(Math.pow(2,z) - 1);
        return (n>=minRange && n<=maxRange);
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

    private static Long tryParseLong(String text) {
        try {
            return Long.parseLong(text);
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
