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

package io.arlas.server.tests;

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.Request;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

public abstract class AbstractTestWithCollection extends AbstractTestContext {

    public static String COLLECTION_NAME = CollectionTool.COLLECTION_NAME;
    public static String COLLECTION_NAME_ACTOR = CollectionTool.COLLECTION_NAME_ACTOR;
    protected static final String FLATTEN_CHAR = ArlasServerConfiguration.FLATTEN_CHAR;

    protected static Request request = new Request();

    static {
        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @BeforeClass
    public static void beforeClass() throws ArlasException, IOException {
        new CollectionTool().load(10000);
    }

    @AfterClass
    public static void afterClass() throws IOException, ArlasException {
        new CollectionTool().delete();
    }
}
