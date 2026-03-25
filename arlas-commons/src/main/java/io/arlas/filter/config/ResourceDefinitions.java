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

package io.arlas.filter.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDefinitions.class);
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private Map<String, Map<String, List<String>>> resources = new HashMap<>();

    public ResourceDefinitions() {
        loadFromClasspath("resources.yaml");
    }

    public ResourceDefinitions(String yamlPath) {
        loadFromFile(yamlPath);
    }

    private void loadFromClasspath(String resourcePath) {
        try {
            Map<String, Map<String, Map<String, List<String>>>> yamlContent =
                    mapper.readValue(
                            ResourceDefinitions.class.getClassLoader().getResourceAsStream(resourcePath),
                            new TypeReference<>() {}
                    );
            resources = yamlContent.getOrDefault("resources", new HashMap<>());
        } catch (IOException | NullPointerException e) {
            LOGGER.error("Could not load resources from classpath: {}", resourcePath, e);
            throw new IllegalStateException("Could not load resources from classpath: " + resourcePath, e);
        }
    }

    private void loadFromFile(String yamlPath) {
        try {
            Map<String, Map<String, Map<String, List<String>>>> yamlContent =
                    mapper.readValue(new File(yamlPath), new TypeReference<>() {});
            resources = yamlContent.getOrDefault("resources", new HashMap<>());
        } catch (IOException e) {
            LOGGER.error("Could not load resources from file: {}", yamlPath, e);
            throw new IllegalStateException("Could not load resources from file: " + yamlPath, e);
        }
    }

    public Map<String, Map<String, List<String>>> getResources() {
        return resources;
    }
}