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

package io.arlas.server.utils;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ESTool {

    public static CreateIndexResponse createArlasIndex(Client client, String arlasIndexName, String arlasMappingName, String arlasMappingFileName)  {
        CreateIndexResponse createIndexResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(ESTool.class.getClassLoader().getResourceAsStream(arlasMappingFileName)));
            createIndexResponse = client.admin().indices().prepareCreate(arlasIndexName).addMapping(arlasMappingName, arlasMapping, XContentType.JSON).get();
        } catch (IOException e) {
            new InternalServerErrorException("Can not initialize the collection database", e);
        }
        return createIndexResponse;
    }

    public static PutMappingResponse putExtendedMapping(Client client, String arlasIndexName, String arlasMappingName, InputStream in) {
        PutMappingResponse putMappingResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(in));
            putMappingResponse = client.admin().indices().preparePutMapping(arlasIndexName).setType(arlasMappingName).setSource(arlasMapping, XContentType.JSON).get();
        } catch (IOException e) {
            new InternalServerErrorException("Cannot update " + arlasIndexName + " mapping");
        }
        return putMappingResponse;
    }

    public static boolean checkIndexMappingFields(Client client, String index, String typeName, String... fields) throws ArlasException {
        GetFieldMappingsResponse response = client.admin().indices().prepareGetFieldMappings(index).setTypes(typeName).setFields(fields).get();
        for (String field : fields) {
            GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(index, typeName, field);
            if (data == null || data.isNull()) {
                throw new NotFoundException("Unable to find " + field + " from " + typeName + " in " + index + ".");
            }
        }
        return true;
    }
}
