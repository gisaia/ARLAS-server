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

package io.arlas.server.model.response;

import io.arlas.server.rest.tag.UpdateServices;

import java.util.ArrayList;
import java.util.List;

public class UpdateResponse {
    public List<Failure> failures = new ArrayList<>();
    public long failed = 0;
    public long updated = 0;
    public UpdateServices.ACTION action;

    public static class Failure{
        public String id;
        public String message;
        public String type;
        public  Failure(){}
        public  Failure(String id, String message, String type){
            this.id = id;
            this.message = message;
            this.type = type;
        }
    }
}
