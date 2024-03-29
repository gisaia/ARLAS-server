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

package io.arlas.server.core.model.request;

import java.util.List;

public class RawGeometry {
    public String geometry;
    public String sort;
    public String signedSort;
    public List<String> include;

    public RawGeometry() {
    }

    public RawGeometry(String geometry) {
        this.geometry = geometry;
    }

    public RawGeometry(String geometry, String sort) {
        this.geometry = geometry;
        this.sort = sort;
    }

    public String getSignedSort() {
        return signedSort;
    }

    public void setSignedSort(String signedSort) {
        this.signedSort = signedSort;
    }

    public List<String> getInclude() {
        return include;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }
}
