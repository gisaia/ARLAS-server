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
package io.arlas.server.admin.auth;

public class RuleClaim implements Comparable {
    public String resource; // regex
    public String verbs; // comma separated list of verbs: GET,POST
    public Integer priority; // number used to sort rules (matching order)

    RuleClaim(String resource, String verbs, Integer priority) {
        this.resource = resource;
        this.verbs = verbs.toLowerCase();
        this.priority = priority;
    }

    public RuleClaim withResource(String r) {
        this.resource = r;
        return this;
    }

    public boolean match(String method, String path) {
        return this.verbs.contains(method.toLowerCase()) && path.matches(this.resource);
    }

    @Override
    public int compareTo(Object other) {
        return ((RuleClaim)other).priority - this.priority;
    }

    @Override
    public String toString() {
        return "Rule[" +
                "r='" + resource + '\'' +
                "/v='" + verbs + '\'' +
                "/p=" + priority +
                ']';
    }
}
