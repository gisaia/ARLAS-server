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

import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.server.core.FieldMD;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import org.apache.commons.lang.BooleanUtils;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;

public class ElasticTool {

    private static final String ES_DATE_TYPE = "date";
    private static final String ES_TYPE = "type";

    public static CreateIndexResponse createArlasIndex(Client client, String arlasIndexName, String arlasMappingName, String arlasMappingFileName)  {
        CreateIndexResponse createIndexResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(ElasticTool.class.getClassLoader().getResourceAsStream(arlasMappingFileName)));
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

    public static CollectionReference getCollectionReferenceFromES(Client client, String index, String type, ObjectReader reader, String ref) throws ArlasException {
        CollectionReference collection = new CollectionReference(ref);
        //Exclude old include_fields for support old collection
        GetResponse hit = client.prepareGet(index, type, ref).setFetchSource(null, "include_fields").get();
        String source = hit.getSourceAsString();
        if (source != null) {
            try {
                collection.params = reader.readValue(source);
            } catch (IOException e) {
                throw new InternalServerErrorException("Can not fetch collection " + ref, e);
            }
        } else {
            throw new NotFoundException("Collection " + ref + " not found.");
        }
        return collection;
    }

    public static FieldMD getFieldMD (String field, Client client, String index, String typeName) throws ArlasException {
        FieldMD fieldMD = new FieldMD();
        fieldMD.path = field;
        fieldMD.exists = false;
        fieldMD.isIndexed = false;
        GetFieldMappingsResponse response;
        try {
            response = client.admin().indices().prepareGetFieldMappings(index).setTypes(typeName).setFields(field).get();
        } catch (IndexNotFoundException e) {
            throw new NotFoundException("Index " + index + " does not exist.");
        }
        String lastKey = field.substring(field.lastIndexOf(".") + 1);
        response.mappings().keySet()
                .stream()
                .forEach(indexName -> {
                    GetFieldMappingsResponse.FieldMappingMetaData data = response.fieldMappings(indexName, typeName, field);
                    if(!fieldMD.exists) {
                        fieldMD.exists = (data != null);
                    }
                    boolean isFieldMetadaAMap = (data != null && !data.isNull() && data.sourceAsMap().get(lastKey) instanceof Map);
                    if (isFieldMetadaAMap) {
                        if (StringUtil.isNullOrEmpty(fieldMD.type)) {
                            fieldMD.type = Optional.ofNullable(((Map)data.sourceAsMap().get(lastKey)))
                                    .map(m -> m.get(ES_TYPE))
                                    .map(Object::toString).get();
                        }
                        if (!fieldMD.isIndexed) {
                            fieldMD.isIndexed = BooleanUtils.toBoolean(Optional.ofNullable(((Map)data.sourceAsMap().get(lastKey)))
                                    .map(m -> m.get("index"))
                                    .map(Object::toString).get());
                        }

                    }
                });

        return fieldMD;
    }
    public static boolean isDateField(FieldMD fieldStatus) throws ArlasException {
        if (fieldStatus != null) {
            return ES_DATE_TYPE.equals(fieldStatus.type);
        }
        return false;
    }
}
