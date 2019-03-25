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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.app.ArlasServerConfiguration;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Not;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class MapExplorerTest {

    @Test
    public void testFlat() throws IOException {
        Map<String, Object> flat =
                MapExplorer.flat(
                        new ObjectMapper().reader(new TypeReference<Map<String, Object>>(){}).readValue(this.getClass().getClassLoader().getResourceAsStream("flatMapTest.json")),
                        new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), Collections.singleton("a.e.g.2"));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_b_0_c", 1));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_b_1_c", 2));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_b_2_d", "a"));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_b_3_d", "b"));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_e_f", "a"));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_e_g_0", 1));
        Assert.assertThat(flat,IsMapContaining.hasEntry("a_e_g_1", 2));
        Assert.assertThat(flat,IsNot.not(IsMapContaining.hasEntry("a_e_g_2", 3)));
    }
}
