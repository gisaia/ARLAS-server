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


    @JsonProperty("supportedVersion")
    public String supportedVersion;

    @JsonProperty("supportedEncoding")
    public String supportedEncoding;

    @JsonProperty("queryMaxFeature")
    public Number queryMaxFeature;

    @JsonProperty("supportedRequest")
    public String supportedRequest;

    private List<String> getFields(String fieldsComaSeparated) throws ArlasConfigurationException {
        if(Strings.isNullOrEmpty(fieldsComaSeparated)) {
            throw new ArlasConfigurationException("Collection auto discover configuration is missing or empty : " + this.toString());
        }
        return Arrays.asList(fieldsComaSeparated.split(","));
    }

    public List<String> getSupportedVersion() throws ArlasConfigurationException {
        return getFields(supportedVersion);
    }

    public List<String> getSupportedEncoding() throws ArlasConfigurationException {
        return getFields(supportedEncoding);
    }

    public Number getQueryMaxFeature() throws ArlasConfigurationException {
        return queryMaxFeature;
    }

    public List<String> getSupportedRequest() throws ArlasConfigurationException {
        return getFields(supportedRequest);
    }
}
