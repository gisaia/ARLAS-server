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

package io.arlas.server.core.app;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OGCConfiguration {

    @JsonProperty("serviceProviderName")
    public String serviceProviderName;

    @JsonProperty("serviceProviderSite")
    public String serviceProviderSite;

    @JsonProperty("serviceProviderRole")
    public String serviceProviderRole;

    @JsonProperty("serviceContactIndividualName")
    public String serviceContactIndividualName;

    @JsonProperty("serviceContactMail")
    public String serviceContactMail;

    @JsonProperty("serviceContactAdressCity")
    public String serviceContactAdressCity;

    @JsonProperty("serviceContactAdressPostalCode")
    public String serviceContactAdressPostalCode;

    @JsonProperty("serviceContactAdressCountry")
    public String serviceContactAdressCountry;

    @JsonProperty("queryMaxFeature")
    public Number queryMaxFeature;

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public String getServiceProviderSite() {
        return serviceProviderSite;
    }

    public String getServiceProviderRole() {
        return serviceProviderRole;
    }

    public String getServiceContactIndividualName() {
        return serviceContactIndividualName;
    }

    public String getServiceContactAdressCity() {
        return serviceContactAdressCity;
    }

    public String getServiceContactAdressPostalCode() {
        return serviceContactAdressPostalCode;
    }

    public String getServiceContactAdressCountry() {
        return serviceContactAdressCountry;
    }

    public Number getQueryMaxFeature() {
        return queryMaxFeature;
    }

}
