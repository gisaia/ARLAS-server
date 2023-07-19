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

import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.model.response.CollectionReferenceDescriptionProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapExplorer {
    private static ObjectMapper mapper = new ObjectMapper();
    public static Object getObjectFromPath(String path, Object source)  {
        if (StringUtil.isNullOrEmpty(path)) {
            return source;
        } else {
            String[] pathLevels = path.split("\\.");
            StringBuffer newPath = new StringBuffer();
            for (int i = 1; i < pathLevels.length; i++) {
                newPath.append(pathLevels[i]);
                if (i < pathLevels.length - 1) {
                    newPath.append(".");
                }
            }

            if (source instanceof Map && ((Map) source).containsKey(pathLevels[0])) {
                return getObjectFromPath(newPath.toString(), ((Map) source).get(pathLevels[0]));
            } else if (source instanceof CollectionReferenceDescriptionProperty
                    && ((CollectionReferenceDescriptionProperty) source).properties != null
                    && ((CollectionReferenceDescriptionProperty) source).properties.containsKey(pathLevels[0])) {
                return getObjectFromPath(newPath.toString(), ((CollectionReferenceDescriptionProperty) source).properties.get(pathLevels[0]));
            } else if(source instanceof JsonData && ((JsonData) source).toJson().asJsonObject().containsKey(pathLevels[0])) {
                Map<String,JsonData> data = null;
                data = ((JsonData) source).to(Map.class);
                return getObjectFromPath(newPath.toString(), ((Map) data).get(pathLevels[0]));
            }
                return null;
            }
        }

    public static Map<String, Object> flat(Object source, Function<Map<List<String>, Object>,Map<String, Object>> keyStringifier, Set<String> exclude) {
        Map<List<String>, Object> flatted= new HashMap<>();
        flat(new ArrayList<>(),source, flatted, exclude);
        return keyStringifier.apply(flatted);
    }

    private static void flat(List<String> keyParts, Object source, Map<List<String>, Object> flatted, Set<String> exclude) {
        if (source == null) {
            flatted.put(keyParts, source);
        } else if (exclude.stream().anyMatch(donotstartwith->String.join(".",keyParts).startsWith(donotstartwith))) {
            // Nothing to do: should not be exported in the map
        } else if (source instanceof Map) {
            ((Map) source).forEach((key,value)->{
                List<String> extendedParts=new ArrayList<>(keyParts);
                extendedParts.add((String)key);
                flat(extendedParts,value, flatted, exclude);
            });
        } else if (source instanceof Collection || source.getClass().isArray()) {
            Collection<Object> collection = source instanceof Collection?(Collection)source:Arrays.asList(source);
            ReactiveSeq.fromStream(collection.stream()).zipWithIndex().forEach((tuple) -> {
                List<String> extendedParts=new ArrayList<>(keyParts);
                extendedParts.add("" + tuple._2());
                flat(extendedParts,tuple._1(), flatted, exclude);
            });
        } else {
            flatted.put(keyParts,source);
        }
    }

    public static class ReduceArrayOnKey implements Function<Map<List<String>, Object>,Map<String, Object>> {
        private String separator="/";
        public ReduceArrayOnKey(){}
        public ReduceArrayOnKey(String separator){this.separator = separator;}

        @Override
        public Map<String, Object> apply(Map<List<String>, Object> flat) {
            return flat.entrySet().stream().filter(e->e.getValue()!=null).map(e->new Tuple2<>(String.join(separator,e.getKey()),e.getValue())).collect(Collectors.toMap(Tuple2::_1,Tuple2::_2));
        }
    }
}
