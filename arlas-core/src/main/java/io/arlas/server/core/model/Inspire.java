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

package io.arlas.server.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class Inspire implements Serializable {
    private static final long serialVersionUID = 6148838306573157991L;

    @JsonProperty(value = "keywords", required = false)
    public List<Keyword> keywords;
    @JsonProperty(value = "topic_categories", required = false)
    public List<String> topicCategories;
    @JsonProperty(value = "lineage", required = false)
    public String lineage;
    @JsonProperty(value = "languages", required = false)
    public List<String> languages;
    @JsonProperty(value = "spatial_resolution", required = false)
    public InspireSpatialResolution spatialResolution;
    @JsonProperty(value = "inspire_uri", required = false)
    public InspireURI inspireURI;
    @JsonProperty(value = "inspire_limitation_access", required = false)
    public InspireLimitationAccess inspireLimitationAccess;
    @JsonProperty(value = "inspire_use_conditions", required = false)
    public String inspireUseConditions = "no conditions apply";

    public Inspire() {
        inspireLimitationAccess = new InspireLimitationAccess();
    }
}
