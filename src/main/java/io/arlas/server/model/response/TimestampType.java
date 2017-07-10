package io.arlas.server.model.response;

public enum TimestampType {
    epoch_millis("epoch_millis"), epoch_second("epoch_second"),

    basic_date("yyyyMMdd"), basic_date_time("yyyyMMdd'T'HHmmss.SSSZ"), basic_date_time_no_millis("yyyyMMdd'T'HHmmssZ"),

    basic_ordinal_date("yyyyDDD"),  basic_ordinal_date_time("yyyyDDD'T'HHmmss.SSSZ"),
    basic_ordinal_date_time_no_millis("yyyyDDD'T'HHmmssZ"),

    basic_time("HHmmss.SSSZ"),  basic_time_no_millis("HHmmssZ"),

    basic_t_time("'T'HHmmss.SSSZ"), basic_t_time_no_millis("'T'HHmmssZ"),

    basic_week_date("xxxx'W'wwe"), strict_basic_week_date("xxxx'W'wwe"),
    basic_week_date_time("xxxx'W'wwe'T'HHmmss.SSSZ"), strict_basic_week_date_time("xxxx'W'wwe'T'HHmmss.SSSZ"),
    basic_week_date_time_no_millis("xxxx'W'wwe'T'HHmmssZ"), strict_basic_week_date_time_no_millis("xxxx'W'wwe'T'HHmmssZ"),

    date("yyyy-MM-dd"), strict_date("yyyy-MM-dd"),
    date_hour("yyyy-MM-dd'T'HH"), strict_date_hour("yyyy-MM-dd'T'HH"),
    date_hour_minute("yyyy-MM-dd'T'HH:mm"), strict_date_hour_minute("yyyy-MM-dd'T'HH:mm"),
    date_hour_minute_second("yyyy-MM-dd'T'HH:mm:ss"), strict_date_hour_minute_second("yyyy-MM-dd'T'HH:mm:ss"),
    date_hour_minute_second_fraction("yyyy-MM-dd'T'HH:mm:ss.SSS"), strict_date_hour_minute_second_fraction("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    date_hour_minute_second_millis("yyyy-MM-dd'T'HH:mm:ss.SSS"), strict_date_hour_minute_second_millis("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    date_time("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"), strict_date_time("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
    date_time_no_millis("yyyy-MM-dd'T'HH:mm:ssZZ"), strict_date_time_no_millis("yyyy-MM-dd'T'HH:mm:ssZZ"),

    hour("HH"), strict_hour("HH"),
    hour_minute("HH:mm"), strict_hour_minute("HH:mm"),
    hour_minute_second("HH:mm:ss"), strict_hour_minute_second("HH:mm:ss"),
    hour_minute_second_fraction("HH:mm:ss.SSS"), strict_hour_minute_second_fraction("HH:mm:ss.SSS"),
    hour_minute_second_millis("HH:mm:ss.SSS"), strict_hour_minute_second_millis("HH:mm:ss.SSS"),

    ordinal_date("yyyy-DDD"), strict_ordinal_date("yyyy-DDD"),
    ordinal_date_time("yyyy-DDD'T'HH:mm:ss.SSSZZ"), strict_ordinal_date_time("yyyy-DDD'T'HH:mm:ss.SSSZZ"),
    ordinal_date_time_no_millis("yyyy-DDD'T'HH:mm:ssZZ"), strict_ordinal_date_time_no_millis("yyyy-DDD'T'HH:mm:ssZZ"),

    date_optional_time("yyyy ['-' MM ['-' dd]] ['T' [HH [':' mm [':' ss [('.' | ',') digit+]] | [('.' | ',') digit+]] |" +
            " [('.' | ',') digit+]] ['Z' | (('+' | '-') HH [':' mm [':' ss [('.' | ',') SSS]]])]]"),
    strict_date_optional_time("yyyy ['-' MM ['-' dd]] ['T' [HH [':' mm [':' ss [('.' | ',') digit+]] | [('.' | ',') digit+]]" +
            " | [('.' | ',') digit+]] ['Z' | (('+' | '-') HH [':' mm [':' ss [('.' | ',') SSS]]])]]"),

    time("HH:mm:ss.SSSZZ"), strict_time("HH:mm:ss.SSSZZ"),
    time_no_millis("HH:mm:ssZZ"), strict_time_no_millis("HH:mm:ssZZ"),

    t_time("'T'HH:mm:ss.SSSZZ"), strict_t_time("'T'HH:mm:ss.SSSZZ"),
    t_time_no_millis("'T'HH:mm:ssZZ"), strict_t_time_no_millis("'T'HH:mm:ssZZ"),

    week_date("xxxx-'W'ww-e"), strict_week_date("xxxx-'W'ww-e"),
    week_date_time("xxxx-'W'ww-e'T'HH:mm:ss.SSSZZ"), strict_week_date_time("xxxx-'W'ww-e'T'HH:mm:ss.SSSZZ"),
    week_date_time_no_millis("xxxx-'W'ww-e'T'HH:mm:ssZZ"), strict_week_date_time_no_millis("xxxx-'W'ww-e'T'HH:mm:ssZZ"),
    weekyear("xxxx"), strict_weekyear("xxxx"),
    weekyear_week("xxxx-'W'ww"), strict_weekyear_week("xxxx-'W'ww"),
    weekyear_week_day("xxxx-'W'ww-e"), strict_weekyear_week_day("xxxx-'W'ww-e"),

    year("yyyy"), strict_year("yyyy"),
    year_month("yyyy-MM"), strict_year_month("yyyy-MM"),
    year_month_day("yyyy-MM-dd"), strict_year_month_day("yyyy-MM-dd"),

    UNKNOWN("unknown");

    public final String pattern;

    TimestampType(String pattern) {
        this.pattern = pattern;
    }

    public static TimestampType getElasticsearchPatternName(String type) {
        TimestampType ret = UNKNOWN;
        for(TimestampType t : TimestampType.values()) {
            if(t.name().equals(type)) {
                ret = t;
                break;
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
