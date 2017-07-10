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
        switch (type){
            case basic_date:
                dtf = ISODateTimeFormat.basicDate();
                break;
            case basic_date_time:
                dtf = ISODateTimeFormat.basicDateTime();
                break;
            case basic_date_time_no_millis:
                dtf = ISODateTimeFormat.basicDateTimeNoMillis();
                break;
            case basic_t_time:
                dtf = ISODateTimeFormat.basicTTime();
                break;
            case basic_t_time_no_millis:
                dtf = ISODateTimeFormat.basicTTimeNoMillis();
                break;
            case basic_time:
                dtf = ISODateTimeFormat.basicTime();
                break;
            case basic_time_no_millis:
                dtf = ISODateTimeFormat.basicTimeNoMillis();
                break;
            case basic_ordinal_date:
                dtf = ISODateTimeFormat.basicOrdinalDate();
                break;
            case basic_ordinal_date_time:
                dtf = ISODateTimeFormat.basicOrdinalDateTime();
                break;
            case basic_ordinal_date_time_no_millis:
                dtf = ISODateTimeFormat.basicOrdinalDateTimeNoMillis();
                break;
            case basic_week_date:
            case strict_basic_week_date:
                dtf =  ISODateTimeFormat.basicWeekDate();
                break;
            case basic_week_date_time:
            case strict_basic_week_date_time:
                dtf = ISODateTimeFormat.basicWeekDateTime();
                break;
            case basic_week_date_time_no_millis:
            case strict_basic_week_date_time_no_millis:
                dtf = ISODateTimeFormat.basicWeekDateTimeNoMillis();
                break;
            case date:
            case strict_date:
                dtf = ISODateTimeFormat.date();
                break;
            case date_hour:
            case strict_date_hour:
                dtf = ISODateTimeFormat.dateHour();
                break;
            case date_hour_minute:
            case strict_date_hour_minute:
                dtf = ISODateTimeFormat.dateHourMinute();
                break;
            case date_hour_minute_second:
            case strict_date_hour_minute_second:
                dtf = ISODateTimeFormat.dateHourMinuteSecond();
                break;
            case date_hour_minute_second_fraction:
            case strict_date_hour_minute_second_fraction:
                dtf =ISODateTimeFormat.dateHourMinuteSecondFraction();
                break;
            case date_hour_minute_second_millis:
            case strict_date_hour_minute_second_millis:
                dtf = ISODateTimeFormat.dateHourMinuteSecondMillis();
                break;
            case date_time:
            case strict_date_time:
                dtf = ISODateTimeFormat.dateTime();
                break;
            case date_time_no_millis:
            case strict_date_time_no_millis:
                dtf = ISODateTimeFormat.dateTimeNoMillis();
                break;
            case date_optional_time:
            case strict_date_optional_time:
                dtf = ISODateTimeFormat.dateOptionalTimeParser();
                break;
            case hour:
            case strict_hour:
                dtf = ISODateTimeFormat.hour();
                break;
            case hour_minute:
            case strict_hour_minute:
                dtf = ISODateTimeFormat.hourMinute();
                break;
            case hour_minute_second:
            case strict_hour_minute_second:
                dtf = ISODateTimeFormat.hourMinuteSecond();
                break;
            case hour_minute_second_fraction:
            case strict_hour_minute_second_fraction:
                dtf = ISODateTimeFormat.hourMinuteSecondFraction();
                break;
            case hour_minute_second_millis:
            case strict_hour_minute_second_millis:
                dtf = ISODateTimeFormat.hourMinuteSecondMillis();
                break;
            case ordinal_date:
            case strict_ordinal_date:
                dtf = ISODateTimeFormat.ordinalDate();
                break;
            case ordinal_date_time:
            case strict_ordinal_date_time:
                dtf = ISODateTimeFormat.ordinalDateTime();
                break;
            case ordinal_date_time_no_millis:
            case strict_ordinal_date_time_no_millis:
                dtf = ISODateTimeFormat.ordinalDateTimeNoMillis();
                break;
            case t_time:
            case strict_t_time:
                dtf = ISODateTimeFormat.tTime();
                break;
            case t_time_no_millis:
            case strict_t_time_no_millis:
                dtf = ISODateTimeFormat.tTimeNoMillis();
                break;
            case time:
            case strict_time:
                dtf = ISODateTimeFormat.time();
                break;
            case time_no_millis:
            case strict_time_no_millis:
                dtf = ISODateTimeFormat.timeNoMillis();
                break;
            case week_date:
            case strict_week_date:
                dtf = ISODateTimeFormat.weekDate();
                break;
            case week_date_time:
            case strict_week_date_time:
                dtf = ISODateTimeFormat.weekDateTime();
                break;
            case week_date_time_no_millis:
            case strict_week_date_time_no_millis:
                dtf = ISODateTimeFormat.weekDateTimeNoMillis();
                break;
            case weekyear:
            case strict_weekyear:
                dtf = ISODateTimeFormat.weekyear();
                break;
            case weekyear_week:
            case strict_weekyear_week:
                dtf = ISODateTimeFormat.weekyearWeek();
                break;
            case weekyear_week_day:
            case strict_weekyear_week_day:
                dtf = ISODateTimeFormat.weekyearWeekDay();
                break;
            case year:
            case strict_year:
                dtf = ISODateTimeFormat.year();
                break;
            case year_month:
            case strict_year_month:
                dtf = ISODateTimeFormat.yearMonth();
                break;
            case year_month_day:
            case strict_year_month_day:
                dtf = ISODateTimeFormat.yearMonthDay();
                break;
            case UNKNOWN:
                dtf = DateTimeFormat.forPattern(format);
                break;
            case epoch_millis:
            case epoch_second:
                break;

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
