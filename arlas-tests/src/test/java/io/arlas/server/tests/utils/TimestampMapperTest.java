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

package io.arlas.server.tests.utils;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.utils.TimestampTypeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimestampMapperTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetTimestamp() throws ArlasException {
        assertEquals((new DateTime(2004, 05, 25, 0, 0, 0, DateTimeZone.UTC)).getMillis(),
                TimestampTypeMapper.getTimestamp("2004-05-25", "basic_date||date||epoch_second").longValue());
        assertEquals((new DateTime(1993, 07, 20, 05, 59, 40, 100, DateTimeZone.UTC)).getMillis(),
                TimestampTypeMapper.getTimestamp("20/07/1993 05:59:40.100", "dd/MM/yyy HH:mm:ss.SSS").longValue());
        assertEquals((new DateTime(1970, 01, 1, 20, 15, 54, 125, DateTimeZone.UTC)).getMillis(),
                TimestampTypeMapper.getTimestamp("20:15:54.125", "weekyear||hour_minute_second_millis").longValue());
        assertEquals((new DateTime(2017, 04, 14, 9, 13, 41, 275, DateTimeZone.UTC)).getMillis(),
                TimestampTypeMapper.getTimestamp("2017-04-14T09:13:41.275Z", "strict_date_optional_time").longValue());
        assertEquals(1085436000000l,
                TimestampTypeMapper.getTimestamp(1085436000000L, "epoch_millis").longValue());
        assertTrue(TimestampTypeMapper.getTimestamp("2004-05-25", "epoch_millis") == null);
    }
}
