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

package io.arlas.server.model;

import io.arlas.server.model.response.UpdateResponse;
import io.arlas.server.util.SelfExpiringHashMap;
import io.arlas.server.util.SelfExpiringMap;

import java.util.Optional;

public class TaggingStatus {
    private SelfExpiringMap<String, UpdateResponse> statusMap;
    private TaggingStatus() {
        statusMap = new SelfExpiringHashMap<>(36000000l);
    } // 10h by default

    public void updateStatus(String id, UpdateResponse status) {
        statusMap.put(id, status);
    }

    public Optional<UpdateResponse> getStatus(String id) {
        return Optional.ofNullable(statusMap.get(id));
    }
    private static TaggingStatus INSTANCE = new TaggingStatus();

    public static TaggingStatus getInstance() {
        return INSTANCE;
    }
}