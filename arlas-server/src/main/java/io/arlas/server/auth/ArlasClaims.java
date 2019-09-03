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
package io.arlas.server.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

public class ArlasClaims {
    private final Logger LOGGER = LoggerFactory.getLogger(ArlasClaims.class);
    private List<RuleClaim> rules;
    private Map<String, String> headers;
    private Map<String, String> variables;

    public ArlasClaims(List<String> claims) {
        this.rules = new ArrayList<>();
        this.headers = new HashMap<>();
        this.variables = new HashMap<>();

        for (String claim : claims) {
            String[] splitClaim = claim.split(":");
            if (splitClaim.length >= 3) {
                switch (splitClaim[0].toLowerCase().trim()) {
                    case "rule":
                        if (splitClaim.length == 4) {
                            rules.add(new RuleClaim(splitClaim[1], splitClaim[2], Integer.valueOf(splitClaim[3])));
                        } else {
                            LOGGER.warn("Invalid rule claim format: " + claim);
                        }
                        break;
                    case "header":
                        headers.put(splitClaim[1], splitClaim[2]);
                        break;
                    case "variable":
                        variables.put(splitClaim[1], splitClaim[2]);
                        break;
                    default:
                        LOGGER.warn("Unknown claim format: " + claim);
                }
            } else {
                LOGGER.warn("Skipping invalid claim format: " + claim);
            }
        }

        Collections.sort(rules);
        variables.forEach((var,val) -> injectVariable(var,val));
    }

    public boolean isAllowed(String method, String path) {
        for (RuleClaim rule : rules) {
            if (rule.match(method, path)) {
                return true; // stop at first matching rule
            }
        }
        return false;
    }

    public void injectHeaders(MultivaluedMap<String, String> requestHeaders) {
        headers.forEach((k,v) -> requestHeaders.add(k, v));
    }

    private void injectVariable(String var, String val) {
        rules.replaceAll(rc -> rc.withResource(replaceVar(rc.resource, var, val)));
        headers.replaceAll((header, value) -> replaceVar(value, var, val));
    }

    private String replaceVar(String original, String var, String val) {
        return original.replaceAll("\\$\\{" + var + "}", val);
    }
}
