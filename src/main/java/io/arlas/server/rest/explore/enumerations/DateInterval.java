package io.arlas.server.rest.explore.enumerations;

import java.util.List;

/**
 * Created by hamou on 13/04/17.
 */
public enum DateInterval {
    year,quarter,month,week,day,hour,minute,second;
    public String toString(){
        switch(this){
            case year :
                return "year";
            case quarter :
                return "quarter";
            case month :
                return "month";
            case week :
                return "week";
            case day :
                return "day";
            case hour :
                return "hour";
            case minute :
                return "minute";
            case second :
                return "second";
        }
        return null;
    }
    public static Boolean contains( String value){
        if(value.equalsIgnoreCase(year.toString()))
            return true;
        else if(value.equalsIgnoreCase(quarter.toString()))
            return true;
        else if(value.equalsIgnoreCase(month.toString()))
            return true;
        else
            return false;
    }
}
