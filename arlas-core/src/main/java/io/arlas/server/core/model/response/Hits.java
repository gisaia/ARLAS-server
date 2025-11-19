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

package io.arlas.server.core.model.response;

import io.arlas.server.core.model.Link;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Hits {
    public String collection;
    public List<ArlasHit> hits;
    public long nbhits;
    public long totalnb;
    public HashMap<String, Link> links;

    public Hits(String collection) {
        this.collection = collection;
    }

    public Hits(String collection, List<ArlasHit> hits, long totalnb, HashMap<String, Link> links) {
        this.collection = collection;
        this.hits = hits;
        this.nbhits = hits.size();
        this.totalnb = totalnb;
        this.links = links;

    }
}