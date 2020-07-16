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

package io.arlas.server.app;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ArlasAuthConfiguration {
    @JsonProperty("enabled")
    public boolean enabled;

    @JsonProperty("public_uris")
    public List<String> publicUris;

    @JsonProperty("header_user")
    public String headerUser;

    @JsonProperty("header_group")
    public String headerGroup;

    @JsonProperty("claim_roles")
    public String claimRoles;

    @JsonProperty("claim_permissions")
    public String claimPermissions;

    @JsonProperty("login_url")
    public String loginUrl;

    @Deprecated
    @JsonProperty("certificate_file")
    public String certificateFile;

    @JsonProperty("certificate_url")
    public String certificateUrl;

    private String publicRegex;
    public String getPublicRegex() {
        if (this.publicRegex == null) {
            this.publicRegex = "^(" + String.join("|", this.publicUris) + ")";
        }
        return this.publicRegex;
    }

}
