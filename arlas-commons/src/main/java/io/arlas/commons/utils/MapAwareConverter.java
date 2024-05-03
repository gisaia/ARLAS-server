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

package io.arlas.commons.utils;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MapAwareConverter implements ModelConverter {

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            Schema schema = chain.next().resolve(type, context, chain);
            JavaType _type = Json.mapper().constructType(type.getType());
            if (_type != null) {
                Class<?> cls = _type.getRawClass();
                if (HashMap.class.isAssignableFrom(cls)) {
                    if (schema != null && schema.getProperties() != null) {
                        schema.getProperties().remove("empty");
                        if (schema.getProperties().isEmpty()) {
                            schema.setProperties(null);
                            if (schema.getAdditionalProperties() != null) {
                                schema.setAdditionalProperties(null);
                            }
                        }
                    }
                }
            }
            return schema;
        } else {
            return null;
        }
    }
}