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

package io.arlas.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InspireConformity {
    public static final String INSPIRE_NETWORK_SERVICES_CONFORMITY_TITLE = "Commission Regulation (EC) No 1088/2010 - Conformance to Download services";
    public static final String INSPIRE_NETWORK_SERVICES_CONFORMITY_DATE = "2010-11-23";
    public static final String INSPIRE_METADATA_CONFORMITY_TITLE = "Commission Regulation (EC) No 1205/2008";
    public static final String INSPIRE_METADATA_CONFORMITY_DATE = "2008-12-03";
    public static final String INSPIRE_INTEROPERABILITY_CONFORMITY_TITLE = "Commission Regulation (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services";
    public static final String INSPIRE_INTEROPERABILITY_CONFORMITY_DATE = "2010-11-23";

    @JsonProperty(value = "specification_title", required = false)
    public String specificationTitle;
    @JsonProperty(value = "specification_date", required = false)
    public String specificationDate;
    @JsonProperty(value = "specification_date_type", required = false)
    public String specificationDateType;
    @JsonProperty(value = "degree", required = false)
    public String degree;


}
