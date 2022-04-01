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

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.Request;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.UnknownHostException;

public abstract class AbstractTestWithDataSet extends AbstractTestContext {

    protected static Request request = new Request();

    static {
        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @BeforeClass
    public static void beforeClass() {
        try {
            DataSetTool.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArlasException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() throws IOException {
        DataSetTool.clearDataSet();
    }
}
