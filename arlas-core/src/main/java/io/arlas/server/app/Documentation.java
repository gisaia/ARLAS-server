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

package io.arlas.server.app;

public class Documentation {

    public static final String GEOSEARCH_OPERATION = "Search and return the elements found in the collection(s) as features, given the filters"; // TODO: different?
    public static final String TILED_GEOSEARCH_OPERATION = "Search and return the elements found in the collection(s) and localized in the given tile(x,y,z) as features, given the filters";
    public static final String SEARCH_OPERATION = "Search and return the elements found in the collection, given the filters";
    public static final String TAG_OPERATION=   "Search and tag the elements found in the collection, given the filters";
    public static final String UNTAG_OPERATION=   "Search and untag the elements found in the collection, given the filters";
    public static final String OPENSEARCH_OPERATION = "Access to the OpenSearch Description document for the given collection";
    public static final String OPENSEARCH_CSW_OPERATION = "Access to the OpenSearch CSW Description document";
    public static final String PROJECTION_PARAM_INCLUDE = "List the name patterns of the field to be included in the result. Seperate patterns with a comma.";
    public static final String PROJECTION_PARAM_EXCLUDE = "List the name patterns of the field to be excluded in the result. Seperate patterns with a comma.";
    public static final String SIZE_PARAM_SIZE = "The maximum number of entries or sub-entries to be returned. The default value is 10";
    public static final String SIZE_PARAM_FROM = "From index to start the search from. Defaults to 0.";
    public static final String SORT_PARAM_SORT = "- Sort the result on the given fields ascending or descending. " +
            "\n \n" +
            "- Fields can be provided several times by separating them with a comma. The order matters. " +
            "\n \n" +
            "- For a descending sort, precede the field with '-'. The sort will be ascending otherwise." +
            "\n \n" +
            "- For a geodistance sort, specify the point, from which the distances are calculated, as follow : 'geodistance:lat lon'" +
            "\n \n";

    public static final String FILTER_PARAM_F = "- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). " +
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
            "       :range:  | {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges | numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression";

    public static final String FILTER_PARAM_Q = "A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}";
    public static final String FILTER_PARAM_PWITHIN = "Any element having its centroid contained within the given bbox : 'top, left, bottom, right'. ";
    public static final String FILTER_PARAM_GWITHIN = "Any element having its geometry contained within the given geometry (WKT)";
    public static final String FILTER_PARAM_GINTERSECT = "Any element having its geometry intersecting the given geometry (WKT)";
    public static final String FILTER_PARAM_NOTPWITHIN = "Any element having its centroid outside the given bbox : 'top, left, bottom, right'.";
    public static final String FILTER_PARAM_NOTGWITHIN = "Any element having its geometry outside the given geometry (WKT)";
    public static final String FILTER_PARAM_NOTGINTERSECT = "Any element having its geometry not intersecting the given geometry (WKT)";


    public static final String AGGREGATION_OPERATION = "Aggregate the elements in the collection(s), given the filters and the aggregation parameters";
    public static final String AGGREGATION_PARAM_AGG = "- The agg parameter should be given in the following formats:  " +
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
            "   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types. " +
            "\n \n" +
            "   - It's possible to apply multiple metric aggregations by defining multiple (**collect_field**,**collect_fct**) couples." +
            "\n \n" +
            "   - (**collect_field**,**collect_fct**) couples should be unique in that case." +
            "\n \n" +
            "   - (**order**,**on**) couple is optional for all aggregation types." +
            "\n \n" +
            "   - **size** is optional for term and geohash, and must not be specified for the other types." +
            "\n \n" +
            "   - **include** is optional for term, and must not be specified for the other types." +
            "\n \n" +
            "- {type} possible values are : " +
            "\n \n" +
            "       datehistogram, histogram, geohash and term. " +
            "\n \n" +
            "- {interval} possible values depends on {type}. " +
            "\n \n" +
            "       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. " +
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
            "  {collect_fct} possible values are : " +
            "\n \n" +
            "       avg,cardinality,max,min,sum" +
            "\n \n" +
            "- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. " +
            "Its values are 'asc' or 'desc'. " +
            "\n \n" +
            "- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. " +
            "\n \n" +
            "- If {on} is equal to `result` and two or more (**collect_field**,**collect_fct**) couples are specified, then the order is applied on the first `collect_fct` that is different from geobbox and geocentroid" +
            "\n \n" +
            "- {size} Defines how many buckets should be returned. " +
            "\n \n" +
            "- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created." +
            "\n \n" +
            "**agg** parameter is multiple. Every agg parameter specified is a subaggregation of the previous one : order matters. " +
            "\n \n" +
            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md.";

    public static final String GEOAGGREGATION_OPERATION = "Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters.";
    public static final String GEOHASH_GEOAGGREGATION_OPERATION = "Aggregate the elements in the collection(s) and localized in the given geohash as features, given the filters and the aggregation parameters.";
    public static final String GEOAGGREGATION_PARAM_AGG = "- The agg parameter should be given in the following formats:  " +
            "\n \n" +
            "       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}:fetcbGeometry-{fetchGeometry values}" +
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
            "   - **include** is optional for term, and must not be specified for the other types." +
            "\n \n" +
            "- {type} possible values are : " +
            "\n \n" +
            "       geohash, datehistogram, histogram and term. geohash must be the main aggregation." +
            "\n \n" +
            "- {interval} possible values depends on {type}. " +
            "\n \n" +
            "       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. " +
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
            "  {collect_fct} possible values are : " +
            "\n \n" +
            "       avg,cardinality,max,min,sum,geobbox,geocentroid" +
            "\n \n" +
            "- {fetchGeometry} is to be specified for `geohash` and `term` aggregations only" +
            "\n \n" +
            "- {fetchGeometry} : When it's centroid : the geoaggregation geometry is the centroid of each bucket." +
            "\n \n" +
            "- {fetchGeometry} : When it's bbox: the geoaggregation geometry is the data extend (bbox) in each bucket." +
            "\n \n" +
            "- {fetchGeometry} : When it's geohash: the geoaggregation geometry is the geohash extend of each bucket. It's applied only for Geohash aggregation tye. It is not supported for term aggregation type. " +
            "\n \n" +
            "- {fetchGeometry} : When it's first: the geoaggregation geometry is the geometry of the first document in each bucket (chronogically)" +
            "\n \n" +
            "- {fetchGeometry} : When it's last: the geoaggregation geometry is the geometry of the last document in each bucket (chronogically)" +
            "\n \n" +
            "- {fetchGeometry} : When it's {field}-first: the geoaggregation geometry is the geometry of the first document in each bucket (ordered on the {field})" +
            "\n \n" +
            "- {fetchGeometry} : When it's {field}-last: the geoaggregation geometry is the geometry of the last document in each bucket (ordered on the {field})" +
            "\n \n" +
            "- (collect_field,collect_fct) should both be specified, except when collect_fct = `geobbox` or `geocentroid`, it could be specified alone. The metrics `geobbox` and `geocentroid` are returned as features collections." +
            "\n \n" +
            "- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. " +
            "Its values are 'asc' or 'desc'. " +
            "\n \n" +
            "- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. " +
            "\n \n" +
            "- When {on} = `result`, then (collect_field,collect_fct) should be specified. Except when {collect_fct} = `geobbox` or `geocentroid`, then {on}=`result` is prohibited" +
            "\n \n" +
            "- {size} Defines how many buckets should be returned. " +
            "\n \n" +
            "- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created." +
            "\n \n" +
            "If {fetchGeometry} is specified, the returned geometry is the one used in the geojson." +
            "\n \n" +
            "If **fetchGeometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned as the geo-aggregation geometry and not in the metrics. Same for **fetchGeometry-bbox** and **collect_fct**=`geobbox`" +
            "\n \n" +
            "**agg** parameter is multiple. The first (main) aggregation must be geohash. Every agg parameter specified is a subaggregation of the previous one : order matters. " +
            "\n \n" +
            "For more details, check https://github.com/gisaia/ARLAS-server/blob/master/docs/arlas-api-exploration.md ";

    public static final String FORM_PRETTY = "Pretty print";
    public static final String FORM_FLAT = "Flat the property map: only key/value on one level";

    public static final String RANGE_OPERATION = "Calculates the min and max values of a field in the collection, given the filters";
    public static final String RANGE_FIELD = "The field whose range is calculated";

    public static final String SEARCH_AFTER_PARAM_SEARCH_AFTER = "List of values of fields present in sort param that are used to search after. " +
            "\n \n" +
            "- Values must be provided by separating them with a comma. The order matters. " +
            "\n \n" +
            "- The last value must be md.id field ." +
            "\n \n" +
            "- This param works only combined whith sort param" +
            "\n \n";
}
