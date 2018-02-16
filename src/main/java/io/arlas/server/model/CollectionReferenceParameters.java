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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.validator.constraints.NotEmpty;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@JsonSnakeCase
public class CollectionReferenceParameters {

    @NotEmpty
    public String indexName;

    @NotEmpty
    public String typeName;

    public String idPath;

    public String geometryPath;

    public String centroidPath;

    public String timestampPath;
    
    public String includeFields = null;
    
    public String excludeFields = null;

    public Map<String,String> custom_params = null;

    public ObjectNode json_schema = null;

    public CollectionReferenceParameters() {
    }

    public CollectionReferenceParameters(String indexName, String typeName, String idPath, String geometryPath,
            String centroidPath, String timestampPath, String includeFields, String excludeFields) {
        this.indexName = indexName;
        this.typeName = typeName;
        this.idPath = idPath;
        this.geometryPath = geometryPath;
        this.centroidPath = centroidPath;
        this.timestampPath = timestampPath;
        this.includeFields = includeFields;
        this.excludeFields = excludeFields;
    }

    @Override
    public String toString() {
        return "CollectionReferenceParameters [indexName=" + indexName + ", typeName=" + typeName + ", idPath=" + idPath + ", geometryPath=" + geometryPath + ", centroidPath=" + centroidPath
                + ", timestampPath=" + timestampPath + ", includeFields=" + includeFields + ", excludeFields=" + excludeFields + ", custom_params=" + custom_params + ", json_schema=" + json_schema +"]";
    }
}
