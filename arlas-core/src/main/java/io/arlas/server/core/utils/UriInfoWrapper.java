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

package io.arlas.server.core.utils;

import io.arlas.commons.utils.StringUtil;

import javax.ws.rs.core.UriInfo;

public class UriInfoWrapper {
    private UriInfo uriInfo;
    private String baseUri;

    public UriInfoWrapper(UriInfo uriInfo, String baseUri) {
        this.uriInfo = uriInfo;
        this.baseUri = baseUri;
    }

    public String getBaseUri() {
        if (StringUtil.isNullOrEmpty(baseUri)) {
            baseUri = uriInfo.getBaseUri().toString();
        }
        return baseUri;
    }

    public String getPathUri() {
        return uriInfo.getPath();
    }

    public String getAbsoluteUri() {
        return getBaseUri() + getPathUri();
    }

    public String getQueryParameters() {
        return uriInfo.getRequestUriBuilder().toTemplate().replace(uriInfo.getAbsolutePath().toString(), "");
    }

    public String getNextQueryParameters(String afterValue) {
        return uriInfo.getRequestUriBuilder()
                .replaceQueryParam("after", afterValue)
                .replaceQueryParam("before", "")
                .toTemplate()
                .replace(uriInfo.getAbsolutePath().toString(), "").replace("&before=","").replace("before=&","");
    }

    public String getPreviousQueryParameters(String afterValue) {
        return uriInfo.getRequestUriBuilder()
                .replaceQueryParam("after", "")
                .replaceQueryParam("before", afterValue)
                .toTemplate()
                .replace(uriInfo.getAbsolutePath().toString(), "").replace("&after=","").replace("after=&","");
    }

    public String getRequestUri() {
        return getAbsoluteUri() + getQueryParameters();
    }

    public String getNextHref(String afterValue) {
        return getAbsoluteUri() + getNextQueryParameters(afterValue);
    }
    public String getPreviousHref(String afterValue) {
        return getAbsoluteUri() + getPreviousQueryParameters(afterValue);
    }

}
