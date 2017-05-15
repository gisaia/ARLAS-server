package io.arlas.server.rest.explore;

/**
 * Created by sylvaingaudan on 03/05/2017.
 */
public class Documentation {

    public static final String GEOSEARCH_OPERATION="Search and return the elements found in the collection(s) as features, given the filters"; // TODO: different?
    public static final String SEARCH_OPERATION=   "Search and return the elements found in the collection, given the filters";
    public static final String PROJECTION_PARAM_INCLUDE="List the name patterns of the field to be included in the result. Seperate patterns with a comma.";
    public static final String PROJECTION_PARAM_EXCLUDE="List the name patterns of the field to be excluded in the result. Seperate patterns with a comma.";
    public static final String SIZE_PARAM_SIZE="The maximum number of entries or sub-entries to be returned. The default value is 10";
    public static final String SIZE_PARAM_FROM="From index to start the search from. Defaults to 0.";
    public static final String SORT_PARAM_SORT="- Sort the result on the given fields ascending or descending. " +
            "\n \n" +
            "- Fields can be provided several times by separating them with a comma. The order matters. " +
            "\n \n" +
            "- For a descending sort, precede the field with '-'. The sort will be ascending otherwise." +
            "\n \n";

    public static final String FILTER_PARAM_F="- A triplet for filtering the result. Multiple filter can be provided. " +
            "The order does not matter. " +
            "\n \n" +
            "- A triplet is composed of a field name, a comparison operator and a value. " +
            "\n \n" +
            "  The possible values of the comparison operator are : " +
            "\n \n" +
            "       Operator |                   Description                    | value type" +
            "\n \n" +
            "       :eq:     | {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values | numeric or strings " +
            "\n \n" +
            "       :ne:     | {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values | numeric or strings " +
            "\n \n" +
            "       :like:   | {fieldName}  is like {value}                     | numeric or strings " +
            "\n \n" +
            "       :gte:    | {fieldName} is greater than or equal to  {value} | numeric " +
            "\n \n" +
            "       :gt:     | {fieldName} is greater than {value}              | numeric " +
            "\n \n" +
            "       :lte:    | {fieldName} is less than or equal to {value}     | numeric " +
            "\n \n" +
            "       :lt:     | {fieldName}  is less than {value}                | numeric " +
            "\n \n" +
            "       :lt:     | {fieldName}  is less than {value}                | numeric " +
            "\n \n" +
            "\n \n" +
            "- The AND operator is applied between filters having different fieldNames. " +
            "\n \n" +
            "- The OR operator is applied on filters having the same fieldName. " +
            "\n \n" +
            "- If the fieldName starts with - then a must not filter is used" +
            "\n \n" +
            "- If the fieldName starts with - then a must not filter is used" +
            "\n \n" +
            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md ";

    public static final String FILTER_PARAM_Q="A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}";
    public static final String FILTER_PARAM_BEFORE="Any element having its point in time reference before the given timestamp";
    public static final String FILTER_PARAM_AFTER="Any element having its point in time reference after the given timestamp";
    public static final String FILTER_PARAM_PWITHIN="Any element having its centroid contained within the given geometry (WKT)";
    public static final String FILTER_PARAM_GWITHIN="Any element having its geometry contained within the given geometry (WKT)";
    public static final String FILTER_PARAM_GINTERSECT="Any element having its geometry intersecting the given geometry (WKT)";
    public static final String FILTER_PARAM_NOTPWITHIN="Any element having its centroid outside the given geometry (WKT)";
    public static final String FILTER_PARAM_NOTGWITHIN="Any element having its geometry outside the given geometry (WKT)";
    public static final String FILTER_PARAM_NOTGINTERSECT="Any element having its geometry not intersecting the given geometry (WKT)";


    public static final String AGGREGATION_OPERATION="Aggregate the elements in the collection(s), given the filters and the aggregation parameters";
    public static final String AGGREGATION_PARAM_AGG="- The agg parameter should be given in the following formats:  " +
            "\n \n" +
            "       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size} " +
            "\n \n" +
            "Where :" +
            "\n \n" +
            "   - **{type}:{field}** part is mandatory. " +
            "\n \n" +
            "   - **interval** must be specified only when aggregation type is datehistogram, histogram and geohash." +
            "\n \n" +
            "   - **format** is optional for datehistogram, and must not be specified for the other types." +
            "\n \n" +
            "   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types." +
            "\n \n" +
            "   - (**order**,**on**) couple is optional for all aggregation types." +
            "\n \n" +
            "   - **size** is optional for term and geohash, and must not be specified for the other types." +
            "\n \n" +
            "- {type} possible values are : " +
            "\n \n" +
            "       datehistogram, histogram, geohash and term. " +
            "\n \n" +
            "- {interval} possible values depends on {type}. " +
            "\n \n" +
            "       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). " +
            "\n \n" +
            "       If {type} = histogram, then {interval} = {size}. " +
            "\n \n" +
            "       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. " +
            "\n \n" +
            "       If {type} = term, then interval-{interval} is not needed. " +
            "\n \n" +
            "- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss." +
            "\n \n" +
            "- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. " +
            "\n \n" +
            "  {collect_fct} possible values are : "+
            "\n \n" +
            "       avg,cardinality,max,min,sum" +
            "\n \n" +
            "- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. " +
            "Its values are 'asc' or 'desc'. " +
            "\n \n" +
            "- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. " +
            "\n \n" +
            "- {size} Defines how many buckets should be returned. " +
            "\n \n" +
            "**agg** parameter is multiple. Every agg parameter specified is a subaggregation of the previous one : order matters. "+
            "\n \n" +
            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md.";

    public static final String GEOAGGREGATION_OPERATION="Aggregate the elements in the collection(s), given the filters and the aggregation parameters";
    public static final String GEOAGGREGATION_PARAM_AGG="- The agg parameter should be given in the following formats:  " +
            "\n \n" +
            "       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size} " +
            "\n \n" +
            "Where :" +
            "\n \n" +
            "   - **{type}:{field}** part is mandatory. " +
            "\n \n" +
            "   - **interval** must be specified only when aggregation type is datehistogram, histogram and geohash." +
            "\n \n" +
            "   - **format** is optional for datehistogram, and must not be specified for the other types." +
            "\n \n" +
            "   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types." +
            "\n \n" +
            "   - (**order**,**on**) couple is optional for all aggregation types." +
            "\n \n" +
            "   - **size** is optional for term and geohash, and must not be specified for the other types." +
            "\n \n" +
            "- {type} possible values are : " +
            "\n \n" +
            "       geohash, datehistogram, histogram and term. geohash must be the main aggregation." +
            "\n \n" +
            "- {interval} possible values depends on {type}. " +
            "\n \n" +
            "       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). " +
            "\n \n" +
            "       If {type} = histogram, then {interval} = {size}. " +
            "\n \n" +
            "       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. " +
            "\n \n" +
            "       If {type} = term, then interval-{interval} is not needed. " +
            "\n \n" +
            "- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss." +
            "\n \n" +
            "- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. " +
            "\n \n" +
            "  {collect_fct} possible values are : "+
            "\n \n" +
            "       avg,cardinality,max,min,sum" +
            "\n \n" +
            "- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. " +
            "Its values are 'asc' or 'desc'. " +
            "\n \n" +
            "- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. " +
            "\n \n" +
            "- {size} Defines how many buckets should be returned. " +
            "\n \n" +
            "**agg** parameter is multiple. The first (main) aggregation must be geohash. Every agg parameter specified is a subaggregation of the previous one : order matters. "+
            "\n \n" +
            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md ";

    public static final String FORM_PRETTY="Pretty print";
    public static final String FORM_HUMAN="Human readable print";
}
