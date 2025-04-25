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

package io.arlas.server.stac.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public class SortBy {

    private @Valid String field = null;

    private @Valid String direction = null;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("field")
    @NotNull

    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("direction")
    @NotNull

    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public static String getCharDirection(String direction){
        if(direction.equals("asc")){
            return "+";
        }else{
            return "-";
        }
    }
}
