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

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.model.response.TimestampType;
import org.joda.time.*;
import org.joda.time.format.*;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
                dtf = (new DateTimeFormatterBuilder()).append(new EpochTimePrinter(false), new EpochTimeParser(false)).toFormatter().withZoneUTC();
            } else if (type.name().equals(TimestampType.epoch_millis.name())) {
                dtf = (new DateTimeFormatterBuilder()).append(new EpochTimePrinter(true), new EpochTimeParser(true)).toFormatter().withZoneUTC();
            }
        }
        return Optional.ofNullable(dtf);
    }

    public static Object formatDate(Object timestamp, String elasticsearchDateFormat) {
        List<String> formatList = Arrays.asList(elasticsearchDateFormat.split("\\|\\|"));
        DateTime timestampDate = new DateTime((Long) timestamp);
        DateTimeFormatter dtf;

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

    public static class EpochTimeParser implements DateTimeParser {

        private final boolean hasMilliSecondPrecision;

        public EpochTimeParser(boolean hasMilliSecondPrecision) {
            this.hasMilliSecondPrecision = hasMilliSecondPrecision;
        }

        @Override
        public int estimateParsedLength() {
            return hasMilliSecondPrecision ? 19 : 16;
        }

        @Override
        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            boolean isPositive = !text.startsWith("-");
            int firstDotIndex = text.indexOf('.');
            boolean isTooLong = (firstDotIndex == -1 ? text.length() : firstDotIndex) > estimateParsedLength();

            if (bucket.getZone() != DateTimeZone.UTC) {
                throw new IllegalArgumentException("time_zone must be UTC ");
            } else if (isPositive && isTooLong) {
                return -1;
            }

            int factor = hasMilliSecondPrecision ? 1 : 1000;
            try {
                long millis = new BigDecimal(text).longValue() * factor;
                DateTime dt = new DateTime(millis, DateTimeZone.UTC);
                bucket.saveField(DateTimeFieldType.year(), dt.getYear());
                bucket.saveField(DateTimeFieldType.monthOfYear(), dt.getMonthOfYear());
                bucket.saveField(DateTimeFieldType.dayOfMonth(), dt.getDayOfMonth());
                bucket.saveField(DateTimeFieldType.hourOfDay(), dt.getHourOfDay());
                bucket.saveField(DateTimeFieldType.minuteOfHour(), dt.getMinuteOfHour());
                bucket.saveField(DateTimeFieldType.secondOfMinute(), dt.getSecondOfMinute());
                bucket.saveField(DateTimeFieldType.millisOfSecond(), dt.getMillisOfSecond());
                bucket.setZone(DateTimeZone.UTC);
            } catch (Exception e) {
                return -1;
            }
            return text.length();
        }
    }

    public static class EpochTimePrinter implements DateTimePrinter {

        private final boolean hasMilliSecondPrecision;

        public EpochTimePrinter(boolean hasMilliSecondPrecision) {
            this.hasMilliSecondPrecision = hasMilliSecondPrecision;
        }

        @Override
        public int estimatePrintedLength() {
            return hasMilliSecondPrecision ? 19 : 16;
        }


        @Override
        public void printTo(StringBuffer buf, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) {
            if (hasMilliSecondPrecision) {
                buf.append(instant - displayOffset);
            } else {
                buf.append((instant  - displayOffset) / 1000);
            }
        }

        @Override
        public void printTo(Writer out, long instant, Chronology chrono, int displayOffset,
                            DateTimeZone displayZone, Locale locale) throws IOException {
            if (hasMilliSecondPrecision) {
                out.write(String.valueOf(instant - displayOffset));
            } else {
                out.append(String.valueOf((instant - displayOffset) / 1000));
            }
        }

        @Override
        public void printTo(StringBuffer buf, ReadablePartial partial, Locale locale) {
            if (hasMilliSecondPrecision) {
                buf.append(getDateTimeMillis(partial));
            } else {
                buf.append(getDateTimeMillis(partial) / 1000);
            }
        }

        @Override
        public void printTo(Writer out, ReadablePartial partial, Locale locale) throws IOException {
            if (hasMilliSecondPrecision) {
                out.append(String.valueOf(getDateTimeMillis(partial)));
            } else {
                out.append(String.valueOf(getDateTimeMillis(partial) / 1000));
            }
        }

        private long getDateTimeMillis(ReadablePartial partial) {
            int year = partial.get(DateTimeFieldType.year());
            int monthOfYear = partial.get(DateTimeFieldType.monthOfYear());
            int dayOfMonth = partial.get(DateTimeFieldType.dayOfMonth());
            int hourOfDay = partial.get(DateTimeFieldType.hourOfDay());
            int minuteOfHour = partial.get(DateTimeFieldType.minuteOfHour());
            int secondOfMinute = partial.get(DateTimeFieldType.secondOfMinute());
            int millisOfSecond = partial.get(DateTimeFieldType.millisOfSecond());
            return partial.getChronology().getDateTimeMillis(year, monthOfYear, dayOfMonth,
                    hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        }
    }
}
