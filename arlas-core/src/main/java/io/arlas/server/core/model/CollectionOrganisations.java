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
import java.util.List;
import java.util.Map;

import static io.arlas.server.core.model.CollectionReference.*;

public class CollectionOrganisations implements Serializable {
    @Serial
    private static final long serialVersionUID = 4330989126306933505L;

    @JsonProperty(value = ORGANISATIONS_OWNER, required = false)
    public String owner = null;

    @JsonProperty(value = ORGANISATIONS_SHARED, required = false)
    public List<String> sharedWith = null;

    @JsonProperty(value = ORGANISATIONS_PUBLIC, required = false)
    public Boolean isPublic = false;

}
