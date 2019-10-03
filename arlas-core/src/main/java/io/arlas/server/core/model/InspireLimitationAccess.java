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
import io.arlas.server.core.model.enumerations.AccessConstraintEnum;
import io.arlas.server.core.model.enumerations.InspireAccessClassificationEnum;

public class InspireLimitationAccess {
    @JsonProperty(value = "access_constraints", required = false, defaultValue = "otherRestrictions")
    public String accessConstraints = AccessConstraintEnum.otherRestrictions.name();
    @JsonProperty(value = "other_constraints", required = false, defaultValue = "no limitation")
    public String otherConstraints = "no limitations apply";
    @JsonProperty(value = "classification", required = false, defaultValue = "unclassified")
    public String classification = InspireAccessClassificationEnum.unclassified.name();
}
