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

package io.arlas.server.core.utils;

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.utils.ElasticTool;
import io.arlas.server.core.model.response.TimestampType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            if (formatList.contains(TimestampType.epoch_millis.name())) {
                timestamp = ((Integer) elasticsearchTimestampField).longValue();
            } else if (formatList.contains(TimestampType.epoch_second.name())) {
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

    public static Optional<DateTimeFormatter> getDateTimeFormatter(String format) throws ArlasException {
        DateTimeFormatter dtf = null;
        if (!StringUtil.isNullOrEmpty(format)) {
            TimestampType type = TimestampType.getElasticsearchPatternName(format);
            if (type.name().equals(TimestampType.UNKNOWN.name())) {
                CheckParams.checkDateFormat(format);
                dtf = DateTimeFormat.forPattern(format).withZoneUTC();
            } else if (!type.name().equals(TimestampType.epoch_second.name()) && !type.name().equals(TimestampType.epoch_millis.name())) {
                dtf = type.dateTimeFormatter.withZoneUTC();
            } else if (type.name().equals(TimestampType.epoch_second.name())) {
                dtf = (new DateTimeFormatterBuilder()).append(ElasticTool.getElasticEpochTimePrinter(false), ElasticTool.getElasticEpochTimeParser(false)).toFormatter().withZoneUTC();
            } else if (type.name().equals(TimestampType.epoch_millis.name())) {
                dtf = (new DateTimeFormatterBuilder()).append(ElasticTool.getElasticEpochTimePrinter(true), ElasticTool.getElasticEpochTimeParser(true)).toFormatter().withZoneUTC();
            }
        }
        return Optional.ofNullable(dtf);
    }

    public static Object formatDate(Object timestamp, String elasticsearchDateFormat) {
        List<String> formatList = Arrays.asList(elasticsearchDateFormat.split("\\|\\|"));
        DateTime timestampDate = new DateTime((Long) timestamp);
        DateTimeFormatter dtf = null;

        if (formatList.size() == 1) {
            String format = formatList.get(0);
            TimestampType type = TimestampType.getElasticsearchPatternName(format);
            if (!type.name().equals(TimestampType.epoch_second.name()) && !type.name().equals(TimestampType.epoch_millis.name())) {
                if (type.name().equals(TimestampType.UNKNOWN.name())) {
                    dtf = DateTimeFormat.forPattern(format);
                } else {
                    dtf = type.dateTimeFormatter;
                }
                if (dtf != null) {
                    return timestampDate.toString(dtf);
                }
            } else {
                if (type.name().equals(TimestampType.epoch_second.name())) {
                    return (Long) timestamp / 1000;
                }
            }
        }
        return timestamp;
        // if formatList.size() != 1 then the format value is ES default value ==> timestamp is in millisecond
        // ==> no formatting needed
    }

    public static Long parseDate(String format, String dateValue) throws ArlasException {
        return getDateTimeFormatter(format).map(dtf -> dtf.parseDateTime(dateValue).getMillis()).orElse(null);
    }

}
