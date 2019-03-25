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

import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.*;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.Inspire;
import io.arlas.server.model.Keyword;
import io.arlas.server.model.enumerations.*;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.RangeResponse;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CheckParams {

    private static final String POLYGON_TYPE = "POLYGON";
    private static final String INVALID_SORT_PARAMETER = "Invalid sort syntax. Please use the following syntax : 'fieldName:ASC' or 'fieldName:DESC'. ";
    private static final String INVALID_XYZ_PARAMETER = "Z must be between 0 and 28. X and Y must be between 0 and (2^Z-1)";
    private static final String INVALID_DATE_MATH_UNIT = "Invalid date math unit. Please use the following list : y, M, w, d, h, H, m, s. ";
    private static final String INVALID_DATE_MATH_VALUE = "Invalid date math value. Please specify an integer. ";
    private static final String INVALID_DATE_MATH_OPERATOR = "Invalid date math operator. Please use the following list : /, +, -";
    private static final String MISSING_DATE_MATH_UNIT = "Missing date math unit";
    private static final String INVALID_DATE_MATH_EXPRESSION = "Invalid date math expression";
    private static final String OUTRANGE_GEOHASH_PRECISION = "Precision must be between 1 and 12. ";
    private static final String INVALID_PRECISION_TYPE = "Precision must be an integer between 1 and 12. ";
    private static final String INVALID_INTERVAL_TYPE = "Interval must be numeric. ";
    private static final String INVALID_AGGREGATION_PARAMETER = "Invalid aggregation syntax. Must start with {type}:{field}:...";
    private static final String INVALID_AGGREGATION = "Invalid aggregation parameters. Type and field must be specified";
    private static final String INVALID_AGGREGATION_TYPE = "Invalid aggregation TYPE. Must be datehistogram, geohash, histogram or terms ";
    private static final String INVALID_RANGE_FIELD = "The field name/path should not be null.";
    private static final String INVALID_ORDER_VALUE = "Invalid 'order-' value : ";
    private static final String REDUNDANT_COLLECT_FIELD_COLLECT_FCT = "Bad request : the same 'collect-fct' is applied to the same 'collect-field' twice or more.";
    private static final String INVALID_ON_VALUE = "Invalid 'on-' value : ";
    private static final String BAD_COLLECT_FCT_COLLECT_FIELD_NUMBERS = "'collect_field' and 'collect_fct' occurrences should be even.";
    private static final String UNEXISTING_FIELD = "The field name/pattern doesn't exist in the collection";
    private static final String MIN_MAX_AGG_RESPONSE_FOR_UNEXISTING_FIELD = "Infinity";
    private static final String DATE_NOW = "now";
    private static final String GEOHASH_STRATEGY_NOT_SUPPORTED = "'geohash' strategy is only supported for geohash aggregation type.";

    public static final String INTERVAL_NOT_SPECIFIED = "Interval parameter is not specified.";
    public static final String INTERVAL_VALUE_NOT_SPECIFIED = "Interval value is missing.";
    public static final String INTERVAL_UNIT_NOT_SPECIFIED = "Interval unit is missing.";
    public static final String NO_INTERVAL_UNIT_FOR_GEOHASH_NOR_HISTOGRAM = "Interval unit must not be specified for geohash nor histogram aggregations.";
    public static final String NO_TERM_INTERVAL = "'Interval' should not be specified for term aggregation.";
    public static final String INVALID_FETCHGEOMETRY = "Invalid `fetch_geometry` strategy. It should be `fetch_geometry-bbox`, `fetch_geometry-centroid`, `fetch_geometry-byDefault`" +
            "`fetch_geometry-first`, `fetch_geometry-last`, `fetch_geometry-{field}-first`, `fetch_geometry-{field}-last` or `fetch_geometry";


    public CheckParams() {
    }

    public static void checkAggregationRequest(Request request) throws ArlasException {
        if (request == null || !(request instanceof AggregationsRequest) || ((AggregationsRequest) request).aggregations == null)
            throw new BadRequestException("Aggregation should not be null");
        else if (request != null) {
            checkAggregations((AggregationsRequest) request);
        }
    }

    public static void checkCountDistinctRequest(CountDistinct request) throws ArlasException {
        if (request == null) {
            throw new BadRequestException("CountDistinct request should not be null");
        }
        if (StringUtil.isNullOrEmpty(request.field)) {
            throw new BadRequestException("CountDistinct request field should not be null nor empty");
        }
    }

    public static void checkAggregationModel(Aggregation aggregation) throws ArlasException {
        // Check 'type' & 'field'
        if (aggregation.type == null || (aggregation.field == null && aggregation.type != AggregationTypeEnum.datehistogram)) {
            throw new InvalidParameterException(INVALID_AGGREGATION);
        }
        // Check 'order'
        if (aggregation.order != null && aggregation.order != Order.asc && aggregation.order != Order.desc) {
            throw new InvalidParameterException(INVALID_ORDER_VALUE + aggregation.order.name());
        }
        // Check 'on'
        if (aggregation.on != null && aggregation.on != OrderOn.count && aggregation.on != OrderOn.field && aggregation.on != OrderOn.result) {
            throw new InvalidParameterException(INVALID_ON_VALUE + aggregation.on.name());
        }
        // Check 'collect-field' and 'collect-fct' are not redundant
        if (aggregation.metrics != null && aggregation.metrics.stream().distinct().count() != aggregation.metrics.stream().count()) {
            throw new InvalidParameterException(REDUNDANT_COLLECT_FIELD_COLLECT_FCT);
        }
        // Check Interval parameter validity according to aggregation type
        checkAggregationIntervalParameter(aggregation);
        // Check include parameter validity according to aggregation type
        checkAggregationIncludeParameter(aggregation);
        // Check fetch_geometry validity according to aggregation type
        checkFetchGeometryParameter(aggregation);
    }

    public static void checkRangeRequestField(Request request) throws ArlasException {
        if (request == null || !(request instanceof RangeRequest))
            throw new BadRequestException("Range request should not be null");
        else if (request != null) {
            if (((RangeRequest) request).field == null || ((RangeRequest) request).field.length() == 0) {
                throw new InvalidParameterException(INVALID_RANGE_FIELD);
            }
        }
    }

    public static void checkFilter(Filter filter) throws ArlasException {
        if (filter.pwithin != null && !filter.pwithin.isEmpty()) {
            for (MultiValueFilter<String> multiPwithin : filter.pwithin) {
                for (String pw : multiPwithin) {
                    checkBbox(pw);
                }
            }
        }
        if (filter.notpwithin != null && !filter.notpwithin.isEmpty()) {
            for (MultiValueFilter<String> multiNotPwithin : filter.notpwithin) {
                for (String npw : multiNotPwithin) {
                    checkBbox(npw);
                }
            }
        }
        if ((filter.f == null || filter.f.isEmpty()) && !StringUtil.isNullOrEmpty(filter.dateformat)) {
            throw new BadRequestException("Date format is specified but no date field is queried in f filter");
        }
        checkDateFormat(filter.dateformat);
    }

    public static void checkDateFormat(String dateFormat) throws ArlasException {
        if (!StringUtil.isNullOrEmpty(dateFormat)) {
            if (dateFormat.contains("||")) {
                throw new NotAllowedException("'||' are not allowed for date formats");
            }
            try {
                DateTimeFormat.forPattern(dateFormat);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException("Invalid date format '" + dateFormat + "'. Reason : " + e.getMessage());
            }
        }
    }

    public static void checkRangeFieldExists(RangeResponse rangeResponse) throws ArlasException {
        if (rangeResponse.min.toString().equals(MIN_MAX_AGG_RESPONSE_FOR_UNEXISTING_FIELD) || rangeResponse.min.toString().equals("-" + MIN_MAX_AGG_RESPONSE_FOR_UNEXISTING_FIELD)) {
            throw new InvalidParameterException(UNEXISTING_FIELD);
        }
    }

    public static void checkBbox(String bbox) throws InvalidParameterException {
        double[] tlbr = CheckParams.toDoubles(bbox);
        // west, south, east, north
        if (!(tlbr.length == 4 && isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
        }
    }

    public static void checkAggregationIncludeParameter(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.include != null && aggregationModel.type != AggregationTypeEnum.term) {
            throw new BadRequestException(FluidSearch.NO_INCLUDE_TO_SPECIFY);
        }
    }

    public static void checkFetchGeometryParameter(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.fetchGeometry != null) {
            AggregatedGeometryStrategyEnum fetchGeometryOption = aggregationModel.fetchGeometry.strategy;
            if ((fetchGeometryOption == AggregatedGeometryStrategyEnum.byDefault || fetchGeometryOption == AggregatedGeometryStrategyEnum.centroid
                    || fetchGeometryOption == AggregatedGeometryStrategyEnum.bbox) && aggregationModel.fetchGeometry.field != null) {
                throw new BadRequestException("field should not be specified for byDefault & centroid & bbox fetch_geometry strategies");
            }
            if (fetchGeometryOption == AggregatedGeometryStrategyEnum.geohash && aggregationModel.type != AggregationTypeEnum.geohash) {
                throw new NotAllowedException(GEOHASH_STRATEGY_NOT_SUPPORTED);
            }
        }
    }

    public static void checkAggregationIntervalParameter(Aggregation aggregationModel) throws ArlasException {
        // - Aggregation Interval must be specified for geohash, histogram and datehistogram only
        // - Interval value must be a positive Integer for geohash and datehistogram aggregations and positive decimal for histogram aggregation
        // - Interval unit must be specified for datehistogram aggregation only
        if (aggregationModel.type == AggregationTypeEnum.term) {
            if (aggregationModel.interval != null) {
                throw new BadRequestException(NO_TERM_INTERVAL);
            }
        } else {
            Interval interval = aggregationModel.interval;
            if (interval == null) {
                throw new BadRequestException(INTERVAL_NOT_SPECIFIED);
            } else {
                if (interval.value == null) {
                    throw new BadRequestException(INTERVAL_VALUE_NOT_SPECIFIED);
                } else {
                    Number intervalValue = 0;
                    switch (aggregationModel.type) {
                        case datehistogram:
                            intervalValue = ParamsParser.tryParseInteger(interval.value.toString());
                            break;
                        case geohash:
                            intervalValue = ParamsParser.tryParseInteger(interval.value.toString());
                            break;
                        case histogram:
                            intervalValue = ParamsParser.tryParseDouble(interval.value.toString());
                            break;
                    }
                    if (intervalValue == null || intervalValue.doubleValue() <= 0) {
                        switch (aggregationModel.type) {
                            case datehistogram:
                                throw new InvalidParameterException("The datehistogram interval must be a positive integer.");
                            case geohash:
                                throw new InvalidParameterException("The geohash precision is not valid. It must be an integer between 1 and 12.");
                            case histogram:
                                throw new InvalidParameterException("The histogram interval is not valid. It must be a positive decimal number.");
                        }
                    }
                    if (intervalValue != null && aggregationModel.type == AggregationTypeEnum.geohash && ParamsParser.tryParseInteger(interval.value.toString()) >= 13) {
                        throw new InvalidParameterException("The geohash precision is not valid. It must be an integer between 1 and 12.");
                    } else if (intervalValue != null) {
                        aggregationModel.interval.value = intervalValue;
                    }
                }
                if (interval.unit != null && aggregationModel.type != AggregationTypeEnum.datehistogram) {
                    throw new BadRequestException(NO_INTERVAL_UNIT_FOR_GEOHASH_NOR_HISTOGRAM);
                }
                if (interval.unit == null && aggregationModel.type == AggregationTypeEnum.datehistogram) {
                    throw new BadRequestException(INTERVAL_UNIT_NOT_SPECIFIED);
                }
            }
        }
    }

    public static void checkPageSize(Page page) throws ArlasException {
        if (page != null) {
            if (page.size == null) {
                //Default Value
                page.size = 10;
            } else if (page.size <= 0){
                throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
            }
        }
    }

    public static void checkPageFrom(Page page) throws ArlasException {
        if (page != null) {
            if (page.from == null) {
                //Default Value
                page.from = 0;
            } else if (page.from < 0) {
                throw new InvalidParameterException(FluidSearch.INVALID_FROM);
            }
        }
    }

    public static void checkPageAfter(Page page, String idCollectionField) throws ArlasException {
        if (page != null && page.after != null) {
            /** check compatibility between after with from*/
            if (page.from != null && page.from != 0) {
                throw new BadRequestException("'after' parameter cannot be used if 'from' parameter is higher than 0. If you want to use 'after', please set 'from' to 0 or keep it empty");
            }
            /** check compatibility between after and sort parameters*/
            List<String> afterList = Arrays.asList(page.after.split(","));
            int afterSize = afterList.size();
            if (page.sort == null) {
                throw new BadRequestException("'after' parameter cannot be used without setting 'sort' parameter.");
            }
            /** check if sort contains geodistance */
            if (page.sort.toLowerCase().contains(FluidSearch.GEO_DISTANCE)) {
                throw new BadRequestException("'after' parameter cannot be used when geodistance is set in 'sort' parameter");
            }
            String[] sortList = page.sort.split(",");
            int sortSize = sortList.length;
            if (afterSize != sortSize){
                throw new BadRequestException("The number of 'after' elements must be equal to the number of 'sort' elements");
            }
            String lastSortElement = sortList[sortSize-1];
            if(lastSortElement.startsWith(("-"))){
                lastSortElement = lastSortElement.substring(1);
            }
            if(lastSortElement.compareTo(idCollectionField) != 0){
                throw new InvalidParameterException("If 'after' parameter is set, the last element of 'sort' must be equal to {collection.params.idPath} and the corresponding value for `after` must be the one returned in {md.id}");
            }
        }
    }

    public static void checkRangeValidity(String range) throws ArlasException {
        if ((range.isEmpty() || !(range.startsWith("[") || range.startsWith("]")) ||
                !(range.endsWith("[") || range.endsWith("]")) ||
                !(range.contains("<")))) {
            throw new java.security.InvalidParameterException(FluidSearch.INVALID_PARAMETER_F);
        }
    }

    public static void checkTimestampFormatValidity(String timestamp) throws ArlasException {
        if (ParamsParser.tryParseLong(timestamp) == null) {
            // Check date math validity
            if (timestamp.length() >= 3) {
                // Check if the anchor date is equal to "now"
                if (timestamp.substring(0, 3).equals(DATE_NOW)) {
                    if (timestamp.length() > 3) {
                        String postAnchor = timestamp.substring(3);
                        checkPostAnchorValidity(postAnchor);
                    }
                } else {
                    // If the anchor date is not equal to "now", then it should be a millisecond timestamp ending with "||"
                    if (timestamp.contains("||")) {
                        String[] operands = timestamp.split("\\|\\|");
                        if (ParamsParser.tryParseLong(operands[0]) == null) {
                            throw new InvalidParameterException(FluidSearch.INVALID_TIMESTAMP_RANGE);
                        } else {
                            if (operands.length == 1) {
                                throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                            } else {
                                checkPostAnchorValidity(operands[1]);
                            }
                        }
                    } else {
                        throw new InvalidParameterException(FluidSearch.INVALID_TIMESTAMP_RANGE);
                    }
                }
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_TIMESTAMP_RANGE);
            }
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

    // Verify if agg parameter contains at least type:field and verify that type
    // matches : datehistogram, geohash, histogram or terms
    public static Boolean isAggregationParamValid(String agg) throws ArlasException {
        String[] aggParts = agg.split(":");
        if (aggParts.length > 1) {
            try {
                AggregationTypeEnum.valueOf(aggParts[0]);
                return true;
            } catch (Exception e) {
                throw new InvalidParameterException(INVALID_AGGREGATION_TYPE);
            }
        } else {
            throw new InvalidParameterException(INVALID_AGGREGATION_PARAMETER);
        }
    }

    public static void checkCollectionFunctionValidity(List<String> collectField, List<CollectionFunction> collectFct) throws ArlasException {
        if (collectFct.size() != collectField.size()) {
            throw new BadRequestException(BAD_COLLECT_FCT_COLLECT_FIELD_NUMBERS);
        }
    }

    private static void checkAggregations(AggregationsRequest aggregations) throws ArlasException {
        if (aggregations != null && aggregations.aggregations != null && aggregations.aggregations.size() > 0) {
            for (Aggregation aggregationModel : aggregations.aggregations) {
                checkAggregationModel(aggregationModel);
            }
        }
    }

    public static void checkXYZTileValidity(int x, int y, int z) throws ArlasException {
        if (z >= 0 && z <= 28) {
            if (!isIntegerInXYZRange(x, z) || !isIntegerInXYZRange(x, z)) {
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

    public static void checkExcludeField(List<String> excludeFields, List<String> fields) throws NotAllowedException {
        ArrayList<Pattern> excludeFieldsPattern = new ArrayList<>();
        excludeFields.forEach(field ->
                excludeFieldsPattern.add(Pattern.compile("^" + field.replace(".", "\\.").replace("*", ".*") + ".*$"))
        );
        boolean excludePath;
        for (String field : fields) {
            excludePath = excludeFieldsPattern.stream().anyMatch(pattern -> pattern.matcher(field).matches());
            if (excludePath)
                throw new NotAllowedException("Unable to exclude field " + field + " used for id, geometry, centroid or timestamp.");
        }
    }

    public static void checkMissingInspireParameters(CollectionReference collectionReference) throws ArlasException {
        Inspire collectionReferenceInspire = collectionReference.params.inspire;
        if (collectionReferenceInspire == null) {
            throw new BadRequestException("Inspire node must be set in Collection Reference parameters");
        }
        // check keywords
        if (collectionReferenceInspire.keywords == null || collectionReferenceInspire.keywords.size() == 0) {
            throw new BadRequestException("Missing keywords");
        } else {
            for (Keyword k : collectionReferenceInspire.keywords) {
                if (k.value == null || k.value.equals("")) {
                    throw new BadRequestException("Keyword value must not be null nor empty");
                }
            }
        }
        //Check if topic category is set
        if (collectionReferenceInspire.topicCategories == null || collectionReferenceInspire.topicCategories.isEmpty()) {
            throw new BadRequestException("inspire.topic_categories must not be null nor empty");
        }
        // Check Lineage
        if (collectionReferenceInspire.lineage == null || collectionReferenceInspire.lineage.equals("")) {
            throw new BadRequestException("inspire.lineage must not be null or empty");
        }
        // Check limitation access
        if (collectionReferenceInspire.inspireLimitationAccess == null) {
            throw new BadRequestException("inspire.inspire_limitation_access must not be null");
        }
    }

    public static void checkInvalidDublinCoreElementsForInspire(CollectionReference collectionReference) throws ArlasException {
        // check title
        if (collectionReference.params.dublinCoreElementName.title == null || collectionReference.params.dublinCoreElementName.title.equals("")) {
            throw new BadRequestException("dublin_core_element_name.title must not be null nor empty");
        }
        // check description/abstract
        if (collectionReference.params.dublinCoreElementName.description == null || collectionReference.params.dublinCoreElementName.description.equals("")) {
            throw new BadRequestException("dublin_core_element_name.description must not be null nor empty");
        }
        // check language
        if (collectionReference.params.dublinCoreElementName.language == null || collectionReference.params.dublinCoreElementName.language.equals("")) {
            throw new BadRequestException("dublin_core_element_name.language must not be null nor empty");
        } else {
            try {
                InspireSupportedLanguages.valueOf(collectionReference.params.dublinCoreElementName.language);
            } catch (IllegalArgumentException e) {
                String listOfLanguages = "";
                for (InspireSupportedLanguages sl : InspireSupportedLanguages.values()) {
                    listOfLanguages += "'" + sl.name() + "', ";
                }
                throw new InvalidParameterException("'dublin_core_element_name.language : " + collectionReference.params.dublinCoreElementName.language + "' is not a valid language. Metadata languages must be one of the 24 Official languages of the EU in ISO 639-2 (B) : " + listOfLanguages);
            }
        }
    }

    public static void checkInvalidInspireParameters(CollectionReference collectionReference) throws ArlasException {
        Inspire collectionReferenceInspire = collectionReference.params.inspire;

        // check keywords
        if (collectionReferenceInspire != null && collectionReferenceInspire.keywords != null) {
            for (Keyword k : collectionReferenceInspire.keywords) {
                if (k.dateOfPublication != null) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        df.parse(k.dateOfPublication);
                    } catch (ParseException e) {
                        throw new InvalidParameterException("'dateOfPublication' of the keyword '" + k.value + "' must be a date which format is yyyy-MM-dd");
                    }
                }
            }
        }
        //Check if topic category is set
        if (collectionReferenceInspire != null && collectionReferenceInspire.topicCategories != null) {
            for (String topicCategory : collectionReferenceInspire.topicCategories) {
                try {
                    TopicCategory.fromValue(topicCategory);
                } catch (IllegalArgumentException e) {
                    String listOfTopicCategories = "";
                    for (TopicCategory tc : TopicCategory.values()) {
                        listOfTopicCategories += "'" + tc.value() + "', ";
                    }
                    throw new BadRequestException("Invalid topic category " + topicCategory + ". Must be one of : " + listOfTopicCategories);
                }
            }

        }
        // check languages
        if (collectionReferenceInspire != null && collectionReferenceInspire.languages != null) {
            for (String language : collectionReferenceInspire.languages) {
                if (LanguageAlpha3Code.getByCode(language) == null) {
                    throw new InvalidParameterException(language + " is not a valid language. Languages must be one of the languages expressed in ISO 639-2");
                }
            }
        }
        // check spatial resolution
        if (collectionReferenceInspire != null && collectionReferenceInspire.spatialResolution != null) {
            if (collectionReferenceInspire.spatialResolution.value == null) {
                throw new BadRequestException("The spatial resolution value must not be null nor empty");
            }
            if (collectionReferenceInspire.spatialResolution.unitOfMeasure == null) {
                try {
                    Integer.parseInt(collectionReferenceInspire.spatialResolution.value.toString());
                } catch (IllegalArgumentException e) {
                    throw new InvalidParameterException("The equivalent scale must be an Integer. If you meant to specify a resolution distance, then please set the unit of measure.");
                }
            } else {
                try {
                    Double.parseDouble(collectionReferenceInspire.spatialResolution.value.toString());
                } catch (IllegalArgumentException e) {
                    throw new InvalidParameterException("The resolution distance should be a decimal number");
                }
            }
        }

        // Check limitation access
        if (collectionReferenceInspire != null && collectionReferenceInspire.inspireLimitationAccess != null) {
            try {
                AccessConstraintEnum.valueOf(collectionReferenceInspire.inspireLimitationAccess.accessConstraints);
            } catch (IllegalArgumentException e) {
                String listOfAccessConstraintEnum = "";
                for (AccessConstraintEnum ace : AccessConstraintEnum.values()) {
                    listOfAccessConstraintEnum += "'" + ace.name() + "', ";
                }
                throw new InvalidParameterException("accessConstraints is invalid. Please choose one of : " + listOfAccessConstraintEnum);
            }
            try {
                InspireAccessClassificationEnum.valueOf(collectionReferenceInspire.inspireLimitationAccess.classification);
            } catch (IllegalArgumentException e) {
                String listOfClassificationEnum = "";
                for (InspireAccessClassificationEnum iace : InspireAccessClassificationEnum.values()) {
                    listOfClassificationEnum += "'" + iace.name() + "', ";
                }
                throw new InvalidParameterException("Inspire Access Classification is invalid. Please choose one of : " + listOfClassificationEnum);
            }
        }

    }

    public static double[] toDoubles(String doubles) throws InvalidParameterException {
        try {
            return Arrays.stream(doubles.split(",")).mapToDouble(Double::parseDouble).toArray();
        } catch (Exception e) {
            throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
        }
    }

    private static void checkPostAnchorValidity(String postAnchor) throws ArlasException {
        // Check if it starts with an operator
        // "/" operator is for rounding the date up or down
        String op = postAnchor.substring(0, 1);
        if (!op.equals("/") && !op.equals("-") && !op.equals("+")) {
            throw new InvalidParameterException(INVALID_DATE_MATH_OPERATOR);
        } else {
            if (op.equals("/")) {
                // If the operator is "/", it should be followed by one character : a date math unit,
                if (postAnchor.length() == 2) {
                    checkDateMathUnit(postAnchor.substring(1, 2));
                } else {
                    throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                }
            } else {
                if (op.equals("+") || op.equals("-")) {
                    // example of postAnchor value : -2h/M
                    if (postAnchor.length() > 1) {
                        String[] operands = postAnchor.substring(1).split("/");
                        //translationDuration == 2
                        String translationDuration = operands[0].substring(0, operands[0].length() - 1);
                        if (ParamsParser.tryParseInteger(translationDuration) == null) {
                            throw new InvalidParameterException(INVALID_DATE_MATH_VALUE);
                        }
                        //translationUnit == h
                        String translationUnit = operands[0].substring(operands[0].length() - 1);
                        checkDateMathUnit(translationUnit);
                        if (operands.length == 2) {
                            // roundingUnit == M
                            String roundingUnit = operands[1];
                            checkDateMathUnit(roundingUnit);
                        } else {
                            if (postAnchor.substring(1).contains("/")) {
                                throw new InvalidParameterException(MISSING_DATE_MATH_UNIT);
                            }
                        }
                    } else {
                        throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                    }

                }
            }
        }
    }

    private static void checkDateMathUnit(String unit) throws ArlasException {
        try {
            DateUnitEnum.valueOf(unit);
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException(INVALID_DATE_MATH_UNIT);
        }
    }

    private static boolean isIntegerInXYZRange(int n, int z) {
        long minRange = 0;
        long maxRange = (long) (Math.pow(2, z) - 1);
        return (n >= minRange && n <= maxRange);
    }
}
