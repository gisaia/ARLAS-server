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

package io.arlas.server.model.request;

import io.arlas.server.utils.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class RawGeometries {
    public List<String> geometries;
    public String sort;

    public RawGeometries() {
    }

    public RawGeometries(List<String> geometries, String sort) {
        this.geometries = geometries;
        this.sort = sort;
    }

    public RawGeometries(List<String> geometries) {
        this.geometries = geometries;
    }

    public String flatten() {
        if (!StringUtil.isNullOrEmpty(sort)) {
            return geometries.stream().collect(Collectors.joining(",")) + "-(" + sort + ")";
        } else {
            return geometries.stream().collect(Collectors.joining(","));
        }
    }
}
