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
package io.arlas.server.core.utils;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import org.apache.commons.collections4.IteratorUtils;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public class CollectionUtil {

    public static boolean matches(String indexPattern, String indexName) {
        return (indexPattern.endsWith("*") && indexName.startsWith(indexPattern.substring(0, indexPattern.indexOf("*"))))
                            || indexPattern.equals(indexName);
    }

    public static Map getFieldFromProperties(String field, Map properties) throws ArlasException {
        if (properties == null) {
            throw new NotFoundException("Unable to find properties in index");
        }
        Map<String, Object> res = properties;
        String[] stringList = field.split("\\.");
        int last = stringList.length - 1;
        for (int i = 0; i <= last; i++) {
            res = (Map) res.get(stringList[i]);
            if (res == null) {
                throw new NotFoundException("Field '" + field + "' not found in index mapping");
            } else {
                if (i != last) {
                    res = (Map) res.get("properties");
                }
            }
        }
        return res;
    }

    public static Map<String, Map<String, Object>> checkAliasMappingFields(Map<String, Map<String, Object>> mappings,
                                                                             String... fields) throws ArlasException {
        List<String> indices = IteratorUtils.toList(mappings.keySet().iterator());
        for (String index : indices) {
            Map properties = mappings.get(index);
            for (String field : fields) {
                getFieldFromProperties(field, properties);
            }
        }
        return mappings;
    }
}
