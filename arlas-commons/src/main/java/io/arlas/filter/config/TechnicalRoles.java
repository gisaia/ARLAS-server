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
import java.util.Set;

public class TechnicalRoles {
    // permissions of these roles are defined in arlas-commons/src/main/resources/roles.yaml
    public static final String ROLE_IAM_ADMIN = "role/iam/admin";
    public static final String ROLE_ARLAS_OWNER = "role/arlas/owner";
    public static final String ROLE_ARLAS_USER = "role/arlas/user";
    public static final String ROLE_ARLAS_BUILDER = "role/arlas/builder";
    public static final String ROLE_ARLAS_TAGGER = "role/arlas/tagger";
    public static final String ROLE_ARLAS_IMPORTER = "role/m2m/importer";
    public static final String GROUP_PUBLIC = "group/public";
    public static final String VAR_ORG = "org";
    private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalRoles.class);
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private Map<String, Map<String, List<String>>> technicalRolesPermissions;

    public TechnicalRoles() {
        loadFromClasspath("roles.yaml");
    }

    public TechnicalRoles(String yamlPath) {
        loadFromFile(yamlPath);
    }

    private void loadFromClasspath(String rolesPath) {
        try {
            Map<String, Map<String, Map<String, List<String>>>> yamlContent =
                    mapper.readValue(
                            ResourceDefinitions.class.getClassLoader().getResourceAsStream(rolesPath),
                            new TypeReference<>() {}
                    );
            technicalRolesPermissions = yamlContent.getOrDefault("technicalRoles", new HashMap<>());
        } catch (IOException | NullPointerException e) {
            LOGGER.error("Could not roles from classpath: {}", rolesPath, e);
            technicalRolesPermissions = new HashMap<>();
        }
    }

    private void loadFromFile(String yamlPath) {
        try {
            Map<String, Map<String, Map<String, List<String>>>> yamlContent =
                    mapper.readValue(new File(yamlPath), new TypeReference<>() {});
            technicalRolesPermissions = yamlContent.getOrDefault("technicalRoles", new HashMap<>());
        } catch (IOException e) {
            LOGGER.error("Could not load resources from file: {}", yamlPath, e);
            technicalRolesPermissions = new HashMap<>();
        }
    }

    public Map<String, Map<String, List<String>>> getTechnicalRolesPermissions() {
        return technicalRolesPermissions;
    }

    public Set<String> getTechnicalRolesList() {
        return technicalRolesPermissions.keySet();
    }

    public String getDefaultGroup(String org) {
        return String.format("group/config.json/%s", org);
    }

    public String getNewDashboardGroupRole(String org, String group) {
        return String.format("group/config.json/%s/%s", org, group);
    }
}
