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

import co.elastic.apm.api.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            // a claim is either "rule:resource:verbs:priority" or "header:name:value" or "var:name:value"
            // value can contain ":" so we split up to 3 elements
            String[] splitClaim = claim.split(":", 3);
            if (splitClaim.length == 3) {
                switch (splitClaim[0].toLowerCase().trim()) {
                    case "r":
                    case "rule":
                        String[] remains = splitClaim[2].split(":");
                        if (remains.length == 2) {
                            rules.add(new RuleClaim(splitClaim[1], remains[0], Integer.valueOf(remains[1].trim())));
                        } else {
                            LOGGER.warn("Invalid rule claim format: " + claim);
                        }
                        break;
                    case "h":
                    case "header":
                        headers.put(splitClaim[1], splitClaim[2]);
                        break;
                    case "v":
                    case "var":
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
                LOGGER.debug("Matching rule '" + rule.toString() +"' for path '" + path + "' with method " + method);
                return true; // stop at first matching rule
            }
            LOGGER.debug("NON Matching rule '" + rule.toString() +"' for path '" + path + "' with method " + method);
        }
        return false;
    }

    public void injectHeaders(MultivaluedMap<String, String> requestHeaders, Transaction transaction) {
        headers.forEach((k,v) -> {
            LOGGER.debug("Injecting header '" + k +"' with value '" + v + "'");
            requestHeaders.add(k, v);
            transaction.addLabel(k, v);
        });
    }

    private void injectVariable(String var, String val) {
        rules.replaceAll(rc -> rc.withResource(replaceVar(rc.resource, var, Matcher.quoteReplacement(Pattern.quote(val)))));
        headers.replaceAll((header, value) -> replaceVar(value, var, val));
    }

    private String replaceVar(String original, String var, String val) {
        String result = original.replaceAll("\\$\\{" + var + "}", val);
        LOGGER.debug("Injecting variable '" + var + "' in  '" + original +"' results in '" + result + "'");
        return result;
    }
}
