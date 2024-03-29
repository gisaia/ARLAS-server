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
import java.util.Map;

import static io.arlas.server.core.model.CollectionReference.COLLECTION_DISPLAY_NAME;
import static io.arlas.server.core.model.CollectionReference.FIELD_DISPLAY_NAME;

public class CollectionDisplayNames implements Serializable {
    private static final long serialVersionUID = 7438255714694047836L;

    @JsonProperty(value = COLLECTION_DISPLAY_NAME, required = false)
    public String collection = null;

    @JsonProperty(value = FIELD_DISPLAY_NAME, required = false)
    public Map<String, String> fields = null;

    @JsonProperty(value = "shape_columns", required = false)
    public Map<String, String> shapeColumns = null;
}
