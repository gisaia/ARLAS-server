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

public class OpenSearch {
    @JsonProperty(value = "short_name", required = false)
    public String shortName = "";

    @JsonProperty(value = "description", required = false)
    public String description = "";

    @JsonProperty(value = "contact", required = false)
    public String contact = "";

    @JsonProperty(value = "tags", required = false)
    public String tags = "";

    @JsonProperty(value = "long_name", required = false)
    public String longName = "";

    @JsonProperty(value = "image_height", required = false)
    public String imageHeight = "";

    @JsonProperty(value = "image_width", required = false)
    public String imageWidth = "";

    @JsonProperty(value = "image_type", required = false)
    public String imageType = "";

    @JsonProperty(value = "image_url", required = false)
    public String imageUrl;

    @JsonProperty(value = "developer", required = false)
    public String developer = "";

    @JsonProperty(value = "attribution", required = false)
    public String attribution = "";

    @JsonProperty(value = "syndication_right", required = false)
    public String syndicationRight;

    @JsonProperty(value = "adult_content", required = false)
    public String adultContent = "";

    @JsonProperty(value = "language", required = false)
    public String language = "";

    @JsonProperty(value = "input_encoding", required = false)
    public String inputEncoding = "";

    @JsonProperty(value = "output_encoding", required = false)
    public String outputEncoding = "";

    @JsonProperty(value = "url_template_prefix", required = false)
    public String urlTemplatePrefix = "";
}
