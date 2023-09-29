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

package io.arlas.filter.core;

import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.filter.config.TechnicalRoles;

import javax.ws.rs.core.HttpHeaders;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class IdentityParam {

    public final String userId;
    public final Optional<String> email;
    public final List<String> organisation;
    public final List<String> groups;
    public final boolean isAnonymous;


    public IdentityParam(ArlasAuthConfiguration configuration, HttpHeaders headers) {
        this.userId = Optional.ofNullable(headers.getHeaderString(configuration.headerUser))
                .orElse(configuration.anonymousValue);
        this.email = Optional.ofNullable(headers.getHeaderString(configuration.headerEmail));
        this.isAnonymous = this.userId.equals(configuration.anonymousValue);

        // in a context where resources are publicly available, no organisation is defined
        String filterOrg = headers.getHeaderString(ARLAS_ORG_FILTER);
        this.organisation = Arrays.stream(
                        Optional.ofNullable(headers.getHeaderString(ARLAS_ORGANISATION))
                                .orElse(NO_ORG)
                                .split(","))
                .filter(o -> filterOrg == null || o.equals(filterOrg))
                .collect(Collectors.toList());

        this.groups = Arrays.stream(Optional.ofNullable(headers.getHeaderString(configuration.headerGroup))
                        .orElse(TechnicalRoles.GROUP_PUBLIC)
                        .split(","))
                .map(String::trim)
                .toList();
    }

}
