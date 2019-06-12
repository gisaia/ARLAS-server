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

import io.arlas.server.model.enumerations.Action;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.request.TagRequest;

import java.util.UUID;

public class TagRefRequest extends TagRequest {
    public Action action;
    public String collection;
    public String partitionFilter;
    public String id; // id used to follow up the request, automatically generated
    public float progress; // value between 0 and 100 giving an indication of the request progress

    public static TagRefRequest fromTagRequest(TagRequest t, String collection, String partitionFilter, Action action) {
        TagRefRequest tagRefRequest = new TagRefRequest();
        tagRefRequest.id = UUID.randomUUID().toString();
        tagRefRequest.search = t.search;
        tagRefRequest.tag = t.tag;
        tagRefRequest.propagation = t.propagation;
        tagRefRequest.collection = collection;
        tagRefRequest.partitionFilter = partitionFilter;
        tagRefRequest.action = action;
        return tagRefRequest;
    }

    public static TagRefRequest fromTagRefRequest(TagRefRequest t, Search search, float progress) {
        TagRefRequest tagRefRequest = new TagRefRequest();
        tagRefRequest.id = t.id;
        tagRefRequest.search = search;
        tagRefRequest.tag = t.tag;
        tagRefRequest.collection = t.collection;
        tagRefRequest.action = t.action;
        tagRefRequest.progress = progress;
        return tagRefRequest;
    }

    @Override
    public String toString() {
        return "TagRefRequest{" +
                "action=" + action +
                ", collection='" + collection + '\'' +
                ", partitionFilter='" + partitionFilter + '\'' +
                ", id=" + id +
                ", progress=" + progress +
                '}';
    }
}
