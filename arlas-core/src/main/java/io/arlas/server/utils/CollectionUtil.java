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
import io.arlas.server.exceptions.NotFoundException;
import org.apache.commons.collections.IteratorUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtil {
    public static Map getFieldFromProperties(String field, LinkedHashMap properties) throws ArlasException {
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

    public static Map<String, LinkedHashMap> checkAliasMappingFields(Map<String, LinkedHashMap> mappings,
                                                                     String... fields) throws ArlasException {
        List<String> indices = IteratorUtils.toList(mappings.keySet().iterator());
        for (String index : indices) {
            LinkedHashMap properties = mappings.get(index);
            for (String field : fields) {
                getFieldFromProperties(field, properties);
            }
        }
        return mappings;
    }
}
