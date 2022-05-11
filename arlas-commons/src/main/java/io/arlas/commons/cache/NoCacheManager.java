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

package io.arlas.commons.cache;

/**
 * This cache holds a replicated map (named 'collections') for storing the collection references
 * and one replicated map per collection (named '<collection name>') for storing the elastic types.
 */
public class NoCacheManager implements BaseCacheManager {
    public NoCacheManager(int cacheTimeout) {
    }

    @Override
    public Object getObject(String key, String ref) {
        return null;
    }

    @Override
    public void putObject(String key, String ref, Object o) {
    }

    @Override
    public void removeObject(String key, String ref) {
    }

    @Override
    public void putDecision(String path, Boolean decision) {
    }

    @Override
    public Boolean getDecision(String path) {
        return null;
    }

    @Override
    public void removeDecision(String path) {
    }

    @Override
    public void putPermission(String token, String rpt) {
    }

    @Override
    public String getPermission(String token) {
        return null;
    }

    @Override
    public void removePermission(String token) {
    }

}
