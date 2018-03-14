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
package io.arlas.server.wfs.filter;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;


public final class ElasticConstants {

    public static final Map<String,Object> MATCH_ALL = ImmutableMap.of("match_all", Collections.EMPTY_MAP);

    /**
     * Key used in the feature type user data to store the format for date
     * fields, if relevant.
     */
    public static final String DATE_FORMAT = "date_format";

    /**
     * Key used in the feature type user data to store the full name for fields.
     */
    public static final String FULL_NAME = "full_name";

    /**
     * Key used in the feature type user data to store the Elasticsearch geometry
     */
    public static final String GEOMETRY_TYPE = "geometry_type";

    /**
     * Key used in the feature type user data to indicate whether the field is analyzed.
     */
    public static final String ANALYZED = "analyzed";

    /**
     * Key used in the feature type user data to indicate whether the field is nested.
     */
    public static final String NESTED = "nested";

}
