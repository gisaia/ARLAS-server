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

import java.io.Serial;
import java.io.Serializable;

public class CollectionReference implements Serializable {
    @Serial
    private static final long serialVersionUID = 2270763501550669101L;

    public static final String COLLECTION_NAME = "collection_name";
    public static final String INDEX_NAME = "index_name";
    public static final String ID_PATH = "id_path";
    public static final String GEOMETRY_PATH = "geometry_path";
    public static final String CENTROID_PATH = "centroid_path";
    public static final String TIMESTAMP_PATH = "timestamp_path";
    public static final String TIMESTAMP_FORMAT = "timestamp_format";
    public static final String DEFAULT_TIMESTAMP_FORMAT = "strict_date_optional_time||epoch_millis";
    public static final String INCLUDE_FIELDS = "include_fields";
    public static final String EXCLUDE_FIELDS = "exclude_fields";
    public static final String EXCLUDE_WFS_FIELDS = "exclude_wfs_fields";
    public static final String DISPLAY_NAMES = "display_names";
    public static final String COLLECTION_DISPLAY_NAME = "collection";
    public static final String FIELD_DISPLAY_NAME = "fields";
    public static final String SHAPE_COLUMN_DISPLAY_NAME = "shape_columns";
    public static final String ORGANISATIONS = "organisations";
    public static final String ORGANISATIONS_OWNER = "owner";
    public static final String ORGANISATIONS_SHARED = "shared";
    public static final String ORGANISATIONS_PUBLIC = "public";
    public static final String INSPIRE_PATH = "inspire";
    public static final String DUBLIN_CORE_PATH = "dublin_core_element_name";
    public static final String INSPIRE_LINEAGE = "lineage";
    public static final String INSPIRE_TOPIC_CATEGORIES = "topic_categories";
    public static final String DUBLIN_CORE_TITLE = "title";
    public static final String DUBLIN_CORE_DESCRIPTION = "description";
    public static final String DUBLIN_CORE_LANGUAGE = "language";


    @JsonProperty(value = COLLECTION_NAME, required = true)
    public String collectionName;

    @JsonProperty(value = "params", required = true)
    public CollectionReferenceParameters params;

    public CollectionReference() {
    }

    public CollectionReference(String collectionName) {
        this.collectionName = collectionName;
    }

    public CollectionReference(String collectionName, CollectionReferenceParameters params) {
        this.collectionName = collectionName;
        this.params = params;

    }
}
