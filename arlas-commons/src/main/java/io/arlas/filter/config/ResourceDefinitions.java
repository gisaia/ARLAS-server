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
            resources = new HashMap<>();
        }
    }

    private void loadFromFile(String yamlPath) {
        try {
            Map<String, Map<String, Map<String, List<String>>>> yamlContent =
                    mapper.readValue(new File(yamlPath), new TypeReference<>() {});
            resources = yamlContent.getOrDefault("resources", new HashMap<>());
        } catch (IOException e) {
            LOGGER.error("Could not load resources from file: {}", yamlPath, e);
            resources = new HashMap<>();
        }
    }

    public Map<String, Map<String, List<String>>> getResources() {
        return resources;
    }
}