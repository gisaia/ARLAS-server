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

import java.util.*;

public class ArlasClaims {
    private final Logger LOGGER = LoggerFactory.getLogger(ArlasClaims.class);
    private List<RuleClaim> rules;

    public ArlasClaims(List<String> claims) {
        this.rules = new ArrayList<>();

        for (String claim : claims) {
            String[] splitClaim = claim.split(":");
            if (splitClaim.length >= 3) {
                switch (splitClaim[0].toLowerCase().trim()) {
                    case "rule":
                        if (splitClaim.length == 4) {
                            rules.add(new RuleClaim(splitClaim[1], splitClaim[2], Integer.valueOf(splitClaim[3])));
                        }
                        LOGGER.warn("Invalid rule claim format: " + claim);
                        break;
                    default:
                        LOGGER.warn("Unknown claim format: " + claim);
                }
            } else {
                LOGGER.warn("Skipping invalid claim format: " + claim);
            }
        }
        Collections.sort(rules);
    }

    public boolean isAllowed(String method, String path) {
        for (RuleClaim rule : rules) {
            if (rule.match(method, path)) {
                return true; // stop at first matching rule
            }
        }
        return false;
    }
}
