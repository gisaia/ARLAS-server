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

public class Feed {
    @JsonProperty(value = "author", required = false)
    public Person author;

    @JsonProperty(value = "contributor", required = false)
    public Person contributor;

    @JsonProperty(value = "icon", required = false)
    public String icon;

    @JsonProperty(value = "logo", required = false)
    public String logo;

    @JsonProperty(value = "rights", required = false)
    public String rights;

    @JsonProperty(value = "subtitle", required = false)
    public String subtitle;

    @JsonProperty(value = "generator", required = false)
    public Generator generator;

    public class Entry {
        @JsonProperty(value = "title_path", required = false)
        public String titlePath;

        @JsonProperty(value = "author_name_path", required = false)
        public String authorNamePath;

        @JsonProperty(value = "author_email_path", required = false)
        public String authorEmailPath;

        @JsonProperty(value = "contributor_name_path", required = false)
        public String contributorNamePath;

        @JsonProperty(value = "contributor_email_path", required = false)
        public String contributorEmailPath;

        @JsonProperty(value = "id_path", required = false)
        public String idPath;

        @JsonProperty(value = "updated_path", required = false)
        public String updatedPath;

        @JsonProperty(value = "published_path", required = false)
        public String publishedPath;

        @JsonProperty(value = "rights_path", required = false)
        public String rightsPath;

        @JsonProperty(value = "includes", required = false)
        public String includes;

        @JsonProperty(value = "excludes", required = false)
        public String excludes;
    }

    public class Person {
        @JsonProperty(value = "name", required = false)
        public String name;

        @JsonProperty(value = "email", required = false)
        public String email;

        @JsonProperty(value = "uri", required = false)
        public String uri;
    }

    public class Generator {
        @JsonProperty(value = "name", required = false)
        public String name;

        @JsonProperty(value = "version", required = false)
        public String version;

        @JsonProperty(value = "uri", required = false)
        public String uri;
    }
}