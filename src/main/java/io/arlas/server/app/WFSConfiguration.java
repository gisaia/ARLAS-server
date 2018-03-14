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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.server.exceptions.ArlasConfigurationException;
import org.elasticsearch.common.Strings;
import java.util.Arrays;
import java.util.List;

public class WFSConfiguration {

    @JsonProperty("featureNamespace")
    public String featureNamespace;

    @JsonProperty("queryMaxFeature")
    public Number queryMaxFeature;

    @JsonProperty("serverUri")
    public String serverUri;

    @JsonProperty("serviceProviderName")
    public String serviceProviderName;

    @JsonProperty("serviceProviderSite")
    public String serviceProviderSite;

    @JsonProperty("serviceProviderRole")
    public String serviceProviderRole;

    @JsonProperty("serviceContactIndividualName")
    public String serviceContactIndividualName;

    @JsonProperty("serviceContactAdressCity")
    public String serviceContactAdressCity;

    @JsonProperty("serviceContactAdressPostalCode")
    public String serviceContactAdressPostalCode;

    @JsonProperty("serviceContactAdressCountry")
    public String serviceContactAdressCountry;


    public String getFeatureNamespace() {
        return featureNamespace;
    }

    public Number getQueryMaxFeature()  {
        return queryMaxFeature;
    }

    public String getServerUri() {
        return serverUri;
    }

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
}
