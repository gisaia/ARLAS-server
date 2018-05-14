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

package io.arlas.server.model.response;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public enum TimestampType {
    epoch_millis(null), epoch_second(null),

    basic_date(ISODateTimeFormat.basicDate()), basic_date_time(ISODateTimeFormat.basicDateTime()),
    basic_date_time_no_millis(ISODateTimeFormat.basicDateTimeNoMillis()),

    basic_ordinal_date(ISODateTimeFormat.basicOrdinalDate()), basic_ordinal_date_time(ISODateTimeFormat.basicOrdinalDateTime()),
    basic_ordinal_date_time_no_millis(ISODateTimeFormat.basicOrdinalDateTimeNoMillis()),

    basic_time(ISODateTimeFormat.basicTime()), basic_time_no_millis(ISODateTimeFormat.basicTimeNoMillis()),

    basic_t_time(ISODateTimeFormat.basicTTime()), basic_t_time_no_millis(ISODateTimeFormat.basicTTimeNoMillis()),

    basic_week_date(ISODateTimeFormat.basicWeekDate()), strict_basic_week_date(ISODateTimeFormat.basicWeekDate()),
    basic_week_date_time(ISODateTimeFormat.basicWeekDateTime()), strict_basic_week_date_time(ISODateTimeFormat.basicWeekDateTime()),
    basic_week_date_time_no_millis(ISODateTimeFormat.basicWeekDateTimeNoMillis()),
    strict_basic_week_date_time_no_millis(ISODateTimeFormat.basicWeekDateTimeNoMillis()),

    date(ISODateTimeFormat.date()), strict_date(ISODateTimeFormat.date()),
    date_hour(ISODateTimeFormat.dateHour()), strict_date_hour(ISODateTimeFormat.dateHour()),
    date_hour_minute(ISODateTimeFormat.dateHourMinute()), strict_date_hour_minute(ISODateTimeFormat.dateHourMinute()),
    date_hour_minute_second(ISODateTimeFormat.dateHourMinuteSecond()),
    strict_date_hour_minute_second(ISODateTimeFormat.dateHourMinuteSecond()),
    date_hour_minute_second_fraction(ISODateTimeFormat.dateHourMinuteSecondFraction()),
    strict_date_hour_minute_second_fraction(ISODateTimeFormat.dateHourMinuteSecondFraction()),
    date_hour_minute_second_millis(ISODateTimeFormat.dateHourMinuteSecondMillis()),
    strict_date_hour_minute_second_millis(ISODateTimeFormat.dateHourMinuteSecondMillis()),
    date_time(ISODateTimeFormat.dateTime()), strict_date_time(ISODateTimeFormat.dateTime()),
    date_time_no_millis(ISODateTimeFormat.dateTimeNoMillis()), strict_date_time_no_millis(ISODateTimeFormat.dateTimeNoMillis()),

    hour(ISODateTimeFormat.hour()), strict_hour(ISODateTimeFormat.hour()),
    hour_minute(ISODateTimeFormat.hourMinute()), strict_hour_minute(ISODateTimeFormat.hourMinute()),
    hour_minute_second(ISODateTimeFormat.hourMinuteSecond()), strict_hour_minute_second(ISODateTimeFormat.hourMinuteSecond()),
    hour_minute_second_fraction(ISODateTimeFormat.hourMinuteSecondFraction()),
    strict_hour_minute_second_fraction(ISODateTimeFormat.hourMinuteSecondFraction()),
    hour_minute_second_millis(ISODateTimeFormat.hourMinuteSecondMillis()),
    strict_hour_minute_second_millis(ISODateTimeFormat.hourMinuteSecondMillis()),

    ordinal_date(ISODateTimeFormat.ordinalDate()), strict_ordinal_date(ISODateTimeFormat.ordinalDate()),
    ordinal_date_time(ISODateTimeFormat.ordinalDateTime()), strict_ordinal_date_time(ISODateTimeFormat.ordinalDateTime()),
    ordinal_date_time_no_millis(ISODateTimeFormat.ordinalDateTimeNoMillis()),
    strict_ordinal_date_time_no_millis(ISODateTimeFormat.ordinalDateTimeNoMillis()),

    date_optional_time(ISODateTimeFormat.dateOptionalTimeParser()),
    strict_date_optional_time(ISODateTimeFormat.dateOptionalTimeParser()),

    time(ISODateTimeFormat.time()), strict_time(ISODateTimeFormat.time()),
    time_no_millis(ISODateTimeFormat.timeNoMillis()), strict_time_no_millis(ISODateTimeFormat.timeNoMillis()),

    t_time(ISODateTimeFormat.tTime()), strict_t_time(ISODateTimeFormat.tTime()),
    t_time_no_millis(ISODateTimeFormat.tTimeNoMillis()), strict_t_time_no_millis(ISODateTimeFormat.tTimeNoMillis()),

    week_date(ISODateTimeFormat.weekDate()), strict_week_date(ISODateTimeFormat.weekDate()),
    week_date_time(ISODateTimeFormat.weekDateTime()), strict_week_date_time(ISODateTimeFormat.weekDateTime()),
    week_date_time_no_millis(ISODateTimeFormat.weekDateTimeNoMillis()),
    strict_week_date_time_no_millis(ISODateTimeFormat.weekDateTimeNoMillis()),
    weekyear(ISODateTimeFormat.weekyear()), strict_weekyear(ISODateTimeFormat.weekyear()),
    weekyear_week(ISODateTimeFormat.weekyearWeek()), strict_weekyear_week(ISODateTimeFormat.weekyearWeek()),
    weekyear_week_day(ISODateTimeFormat.weekyearWeekDay()), strict_weekyear_week_day(ISODateTimeFormat.weekyearWeekDay()),

    year(ISODateTimeFormat.year()), strict_year(ISODateTimeFormat.year()),
    year_month(ISODateTimeFormat.yearMonth()), strict_year_month(ISODateTimeFormat.yearMonth()),
    year_month_day(ISODateTimeFormat.yearMonthDay()), strict_year_month_day(ISODateTimeFormat.yearMonthDay()),

    UNKNOWN(null);

    public DateTimeFormatter dateTimeFormatter;

    TimestampType(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public static TimestampType getElasticsearchPatternName(String name) {
        TimestampType ret = UNKNOWN;
        for (TimestampType t : TimestampType.values()) {
            if (t.name().equals(name)) {
                ret = t;
                break;
            }
        }
        return ret;
    }

}
