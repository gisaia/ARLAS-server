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
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.server.core.utils.StringUtil;

import java.util.Arrays;
import java.util.List;

public class CollectionAutoDiscoverConfiguration {

    @JsonProperty("preferred-id-field-name")
    public String preferredIdFieldName;

    @JsonProperty("preferred-timestamp-field-name")
    public String preferredTimestampFieldName;

    @JsonProperty("preferred-centroid-field-name")
    public String preferredCentroidFieldName;

    @JsonProperty("preferred-geometry-field-name")
    public String preferredGeometryFieldName;

    @JsonProperty("preferred-h3-field-name")
    public String preferredH3FieldName;

    @JsonProperty("schedule")
    public int schedule;

    private List<String> getFields(String fieldsComaSeparated) throws ArlasConfigurationException {
        if (StringUtil.isNullOrEmpty(fieldsComaSeparated)) {
            throw new ArlasConfigurationException("Collection auto discover configuration is missing or empty : " + this.toString());
        }
        return Arrays.asList(fieldsComaSeparated.split(","));
    }

    public List<String> getPreferredIdFieldNames() throws ArlasConfigurationException {
        return getFields(preferredIdFieldName);
    }

    public List<String> getPreferredTimestampFieldNames() throws ArlasConfigurationException {
        return getFields(preferredTimestampFieldName);
    }

    public List<String> getPreferredCentroidFieldNames() throws ArlasConfigurationException {
        return getFields(preferredCentroidFieldName);
    }

    public List<String> getPreferredGeometryFieldNames() throws ArlasConfigurationException {
        return getFields(preferredGeometryFieldName);
    }

    public List<String> getPreferredH3FieldNames() throws ArlasConfigurationException {
        return getFields(preferredH3FieldName);
    }

    @Override
    public String toString() {
        return "CollectionAutoDiscoverConfiguration [preferredIdFieldName=" + preferredIdFieldName + ", preferredTimestampFieldName=" + preferredTimestampFieldName + ", preferredCentroidFieldName="
                + preferredCentroidFieldName + ", preferredGeometryFieldName=" + preferredGeometryFieldName + ", schedule=" + schedule + "]";
    }
}
