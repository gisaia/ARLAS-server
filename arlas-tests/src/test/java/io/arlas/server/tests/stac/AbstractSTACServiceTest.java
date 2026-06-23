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

package io.arlas.server.tests.stac;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.tests.CollectionTool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public class AbstractSTACServiceTest extends AbstractSTACTestContext {

    @BeforeAll
    public static void beforeClass() throws ArlasException {
        new CollectionTool().load(10000,false,true,false);
    }

    @AfterAll
    public static void afterClass() throws IOException, ArlasException {
        new CollectionTool().delete(true);
    }
}
