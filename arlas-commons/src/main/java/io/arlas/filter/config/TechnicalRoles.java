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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Map<String, Map<String, List<String>>> technicalRolesPermissions;

    static {
        try {
            technicalRolesPermissions = (Map<String, Map<String, List<String>>>) mapper.readValue(
                            TechnicalRoles.class.getClassLoader().getResourceAsStream("roles.yaml"), Map.class)
                    .get("technicalRoles");
        } catch (IOException e) {
            technicalRolesPermissions = new HashMap<>();
            LOGGER.error("!-----! Technical roles file could not be read !-----!");
        }
    }

    public static Map<String, Map<String, List<String>>> getTechnicalRolesPermissions() {
        return technicalRolesPermissions;
    }

    public static Set<String> getTechnicalRolesList() {
        return technicalRolesPermissions.keySet();
    }

    public static String getDefaultGroup(String org) {
        return String.format("group/config.json/%s", org);
    }

    public static String getNewDashboardGroupRole(String org, String group) {
        return String.format("group/config.json/%s/%s", org, group);
    }
}
