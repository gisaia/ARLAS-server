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

package io.arlas.server.utils;

import io.arlas.server.ExtendedArlasMappingTool;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;


public class ESToolTest {
    static PutMappingResponse putMappingResponse;

    @BeforeClass
    public static void beforeClass() {
        ExtendedArlasMappingTool.createIndexWithOldMapping();
        putMappingResponse = ESTool.putExtendedMapping(ExtendedArlasMappingTool.client, ExtendedArlasMappingTool.ARLAS_INDEX_NAME,
                ExtendedArlasMappingTool.ARLAS_TYPE_NAME, ESTool.class.getClassLoader().getResourceAsStream("arlas.mapping.json"));
    }

    @Test
    public void testPutValidExtendedMapping() throws ArlasException {
        assertEquals(putMappingResponse.isAcknowledged(), true);
        String[] newFieldsInMapping = {"dublin_core_element_name.identifier",  "inspire.keywords.value"};
        assertEquals(ESTool.checkIndexMappingFields(ExtendedArlasMappingTool.client, ExtendedArlasMappingTool.ARLAS_INDEX_NAME, ExtendedArlasMappingTool.ARLAS_TYPE_NAME, newFieldsInMapping), true);
    }

    @Test(expected = NotFoundException.class)
    public void testNonExistingFieldsInMapping() throws ArlasException {
        String[] newFieldsInMapping = {"foo.bar"};
        ESTool.checkIndexMappingFields(ExtendedArlasMappingTool.client, ExtendedArlasMappingTool.ARLAS_INDEX_NAME, ExtendedArlasMappingTool.ARLAS_TYPE_NAME, newFieldsInMapping);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutInvalidExtendedMapping() throws ArlasException {
        ExtendedArlasMappingTool.deleteIndex();
        ExtendedArlasMappingTool.createIndexWithOldMapping();
        putMappingResponse = ESTool.putExtendedMapping(ExtendedArlasMappingTool.client, ExtendedArlasMappingTool.ARLAS_INDEX_NAME,
                ExtendedArlasMappingTool.ARLAS_TYPE_NAME, this.getClass().getClassLoader().getResourceAsStream("arlas.invalid.mapping.json"));
    }

    @AfterClass
    public static void afterClass() {
        ExtendedArlasMappingTool.deleteIndex();
    }
}
