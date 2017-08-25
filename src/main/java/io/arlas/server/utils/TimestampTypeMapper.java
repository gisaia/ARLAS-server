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

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.TimestampType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Arrays;
import java.util.List;

public class TimestampTypeMapper {

    public static Long getTimestamp(Object elasticsearchTimestampField, String elasticsearchTimestampFormat) throws ArlasException {
        Long timestamp = null;
        List<String> formatList = Arrays.asList(elasticsearchTimestampFormat.split("\\|\\|"));
        if (elasticsearchTimestampField instanceof Long) {
            if (formatList.contains(TimestampType.epoch_millis.name()))
                timestamp = (Long) elasticsearchTimestampField;
            else if (formatList.contains(TimestampType.epoch_second.name()))
                timestamp = (Long) elasticsearchTimestampField * 1000;
        } else if (elasticsearchTimestampField instanceof Integer) {
            if (formatList.contains(TimestampType.epoch_millis.name())){
                timestamp = ((Integer) elasticsearchTimestampField).longValue();
            }
            else if (formatList.contains(TimestampType.epoch_second.name())){
                timestamp = ((Integer) elasticsearchTimestampField).longValue() * 1000;
            }
        } else if (elasticsearchTimestampField instanceof String) {
            for (String format : formatList) {
                try {
                    timestamp = parseDate(format, (String) elasticsearchTimestampField);
                    break;
                } catch (IllegalArgumentException e) {
                    e.getMessage();
                }
            }
        }
        return timestamp;
    }

    private static Long parseDate(String format, String timestampPath){
        DateTimeFormatter dtf = null;
        TimestampType type = TimestampType.getElasticsearchPatternName(format);
        if (type.name().equals("UNKNOWN")){
            dtf = DateTimeFormat.forPattern(format);
        }
        else {
            dtf = type.dateTimeFormatter;
        }
        if (dtf != null){
            DateTime jodatime = dtf.parseDateTime(timestampPath);
            return jodatime.getMillis();
        }
        else {
            return null;
        }

    }
}
