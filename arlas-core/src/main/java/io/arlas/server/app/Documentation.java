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
    public static final String PAGE_PARAM_SIZE = "The maximum number of entries or sub-entries to be returned. The default value is 10";
    public static final String PAGE_PARAM_FROM = "From index to start the search from. Defaults to 0.";
    public static final String PAGE_PARAM_SORT = "Sorts the resulted hits on the given fields and/or by distance to a given point :" +
            "\n \n" +
            "> __**Syntax**__: `{field1},{field2},-{field3},geodistance:{lat} {lon},{field4}  ...`." +
            "\n \n" +
            "> **Note 1**: `{field}` can be preceded by **'-'**  for **descending** sort. By default, sort is ascending." +
            "\n \n" +
            "> **Note 2**: The order of fields matters." +
            "\n \n" +
            "> **Note 3** ***geodistance sort***: Sorts the hits centroids by distance to the given **{lat} {lon}** (ascending distance sort). It can be specified at most 1 time." +
            "\n \n" +
            "> __**Example 1**__: sort=`age,-timestamp`. Resulted hits are sorted by age. For same age hits, they are decreasingly sorted in time." +
            "\n \n" +
            "> __**Example 2**__: sort=`age,geodistance:89 179`. Resulted hits are sorted by age. For same age hits, they are sorted by closest distance to the point(89°,179°)" +
            "\n \n";
    public static final String PAGE_PARAM_AFTER = "List of values of fields present in sort param that are used to search after. " +
            "\n \n" +
            "> **What it does**: Allows to get the following hits of a previous search." +
            "\n \n" +
            "> __**Restriction 1**__: **after** param works only combined with **sort** param." +
            "\n \n" +
            "> __**Syntax**__: `after={value1},{value2},...,{valueN} & sort={field1},{field2},...,{fieldN}`." +
            "\n \n" +
            "> **Note 1**: *{value1}` and `{value2}` are the values of `{field1}` and `{field2}` in the last hit returned in the previous search" +
            "\n \n" +
            "> **Note 2**: The order of fields and values matters. *{value1},{value2}* must be in the same order of *{field1},{field2}* in **sort** param" +
            "\n \n" +
            "> **Note 3**:  The last field `{fieldN}` must be the id field specified in the collection **collection.params.idPath** (returned as **md.id**) and `{valueN}` its corresponding value." +
            "\n \n" +
            "> __**Example**__: *sort=`-date,id` & **after**=`01/02/2019,abcd1234`*. Gets the following hits of the previous search that stopped at date *01/02/2019* and id *abcd1234*." +
            "\n \n" +
            "> __**Restriction 2**__: **sort** param cannot include *geodistance* sort." +
            "\n \n" +
            "> __**Restriction 3**__: **from** param must be set to 0 or kept unset" +
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
    public static final String FILTER_PARAM_PWITHIN = "Any element having its centroid contained within the given bbox : `west, south, east, north`. ";
    public static final String FILTER_PARAM_GWITHIN = "Any element having its geometry contained within the given geometry (WKT) or the given BBOX : `west, south, east, north`";
    public static final String FILTER_PARAM_GINTERSECT = "Any element having its geometry intersecting the given geometry (WKT) or the given BBOX : `west, south, east, north`";
    public static final String FILTER_PARAM_NOTPWITHIN = "Any element having its centroid outside the given bbox : `west, south, east, north`.";
    public static final String FILTER_PARAM_NOTGWITHIN = "Any element having its geometry outside the given geometry (WKT) or the given BBOX : `west, south, east, north`";
    public static final String FILTER_PARAM_NOTGINTERSECT = "Any element having its geometry not intersecting the given geometry (WKT) nor the given BBOX : `west, south, east, north`";
    public static final String FILTER_DATE_FORMAT = "The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations";


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
            "- **fetch_geometry**" +
            "\n \n" +
            "    > **What it does**: Specifies the strategy of fetching a geometry in each aggregation bucket." +
            "\n \n" +
            "    > __**Syntax**__: `fetch_geometry` || `fetch_geometry-{strategy}` || `fetch_geometry-{field}-(first||last)`." +
            "\n \n" +
            "    > **fetch_geometry** or **fetch_geometry-byDefault**: the fetched geometry is the centroid of the geohash for `geohash` aggregation or a random geometry for the rest of aggregation types." +
            "\n \n" +
            "    > **fetch_geometry-centroid**: the fetched geometry is the centroid of data inside each bucket." +
            "\n \n" +
            "    > **fetch_geometry-bbox**: the fetched geometry is the data extend (bbox) in each bucket." +
            "\n \n" +
            "    > **fetch_geometry-geohash**: the fetched geometry is the 'geohash' extend of each bucket. This strategy is supported for **geohash** aggregation type only." +
            "\n \n" +
            "    > **fetch_geometry-first**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)" +
            "\n \n" +
            "    > **fetch_geometry-last**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)" +
            "\n \n" +
            "    > **fetch_geometry-{field}-first**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket." +
            "\n \n" +
            "    > **fetch_geometry-{field}-last**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket." +
            "\n \n" +
            "    > **Note 1**: if **fetch_geometry** is specified, the returned geometry is set int the 'geometry' attribute of the json response." +
            "\n \n" +
            "    > **Note 2**: If **fetch_geometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned in 'geometry' attribute of the json response but not in the metrics. Same for **fetch_geometry-bbox** and **collect_fct**=`geobbox`"+
            "\n \n" +
            "- **fetchHits** " +
            "\n \n" +
            "    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits." +
            "\n \n" +
            "    > __**Syntax**__: `fetchHits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`." +
            "\n \n" +
            "    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default." +
            "\n \n" +
            "    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters." +
            "\n \n" +
            "    > __**Example**__: `fetchHits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket." +
            "\n \n" +
            "**agg** parameter is multiple. Every agg parameter specified is a subaggregation of the previous one : order matters. " +
            "\n \n" +
            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md.";

    public static final String GEOAGGREGATION_OPERATION = "Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters.";
    public static final String GEOHASH_GEOAGGREGATION_OPERATION = "Aggregate the elements in the collection(s) and localized in the given geohash as features, given the filters and the aggregation parameters.";
    public static final String GEOAGGREGATION_PARAM_AGG = "- The agg parameter should be given in the following formats:  " +
            "\n \n" +
            "       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}:fetcbGeometry-{fetch_geometry values}" +
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
            "- **fetch_geometry**" +
            "\n \n" +
            "    > **What it does**: Specifies the strategy of fetching a geometry in each aggregation bucket." +
            "\n \n" +
            "    > __**Syntax**__: `fetch_geometry` || `fetch_geometry-{strategy}` || `fetch_geometry-{field}-(first||last)`." +
            "\n \n" +
            "    > **fetch_geometry** or **fetch_geometry-byDefault**: the fetched geometry is the centroid of the geohash for `geohash` aggregation or a random geometry for the rest of aggregation types." +
            "\n \n" +
            "    > **fetch_geometry-centroid**: the fetched geometry is the centroid of data inside each bucket." +
            "\n \n" +
            "    > **fetch_geometry-bbox**: the fetched geometry is the data extend (bbox) in each bucket." +
            "\n \n" +
            "    > **fetch_geometry-geohash**: the fetched geometry is the 'geohash' extend of each bucket. This strategy is supported for **geohash** aggregation type only." +
            "\n \n" +
            "    > **fetch_geometry-first**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)" +
            "\n \n" +
            "    > **fetch_geometry-last**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)" +
            "\n \n" +
            "    > **fetch_geometry-{field}-first**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket." +
            "\n \n" +
            "    > **fetch_geometry-{field}-last**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket." +
            "\n \n" +
            "    > **Note 1**: if **fetch_geometry** is specified, the returned geometry is set int the 'geometry' attribute of the geojson." +
            "\n \n" +
            "    > **Note 2**: If **fetch_geometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned in the geojson 'geometry' attribute but not in the metrics. Same for **fetch_geometry-bbox** and **collect_fct**=`geobbox`" +
            "\n \n" +
            "- **fetch_hits** " +
            "\n \n" +
            "    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits." +
            "\n \n" +
            "    > __**Syntax**__: `fetch_hits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`." +
            "\n \n" +
            "    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default." +
            "\n \n" +
            "    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters." +
            "\n \n" +
            "    > __**Example**__: `fetch_hits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket." +
            "\n \n" +
            "**agg** parameter is multiple. The first (main) aggregation must be geohash. Every agg parameter specified is a subaggregation of the previous one : order matters. " +
            "\n \n" +
            "For more details, check https://github.com/gisaia/ARLAS-server/blob/master/docs/arlas-api-exploration.md ";

    public static final String FORM_PRETTY = "Pretty print";
    public static final String FORM_FLAT = "Flats the property map: only key/value on one level";

    public static final String RANGE_OPERATION = "Calculates the min and max values of a field in the collection, given the filters";
    public static final String RANGE_FIELD = "The field whose range is calculated";

    public static final String COUNT_DISTINCT_FIELD = "The field which values are distinctly counted";
}
