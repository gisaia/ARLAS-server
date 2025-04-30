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

import java.util.List;


public class Asset {

    private @Valid List<String> roles = null;
    private @Valid String name = null;
    private @Valid String title = null;
    private @Valid String description = null;
    private @Valid String href = null;
    private @Valid String type = null;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("roles")
    @NotNull
    public List<String> getRoles() {
        return this.roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Asset roles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name")
    @NotNull
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Asset name(String name) {
        this.name = name;
        return this;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("title")
    @NotNull
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Asset title(String title) {
        this.title = title;
        return this;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("description")
    @NotNull
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Asset description(String description) {
        this.description = description;
        return this;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("href")
    @NotNull
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Asset href(String href) {
        this.href = href;
        return this;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("type")
    @NotNull
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Asset type(String type) {
        this.type = type;
        return this;
    }

}
