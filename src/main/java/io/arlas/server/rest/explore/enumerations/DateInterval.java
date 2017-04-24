package io.arlas.server.rest.explore.enumerations;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamou on 13/04/17.
 */
public enum DateInterval {
    year, quarter, month, week, day, hour, minute, second;

    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String QUARTER = "quarter";
    public static final String WEEK = "week";
    public static final String DAY = "day";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String SECOND = "seconde";

    public static List<String> aggregationTypes() {
        List<String> aggregationTypes = new ArrayList<>();
        aggregationTypes.add(YEAR);
        aggregationTypes.add(QUARTER);
        aggregationTypes.add(MONTH);
        aggregationTypes.add(WEEK);
        aggregationTypes.add(DAY);
        aggregationTypes.add(HOUR);
        aggregationTypes.add(MINUTE);
        aggregationTypes.add(SECOND);
        return aggregationTypes;
    }

    public String toString() {
        switch (this) {
        case year:
            return "year";
        case quarter:
            return "quarter";
        case month:
            return "month";
        case week:
            return "week";
        case day:
            return "day";
        case hour:
            return "hour";
        case minute:
            return "minute";
        case second:
            return "second";
        }
        return null;
    }

    public static Boolean contains(String value) {
        if (value.equalsIgnoreCase(year.toString()))
            return true;
        else if (value.equalsIgnoreCase(quarter.toString()))
            return true;
        else if (value.equalsIgnoreCase(month.toString()))
            return true;
        else
            return false;
    }
}
