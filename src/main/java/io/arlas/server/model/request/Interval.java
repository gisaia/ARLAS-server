package io.arlas.server.model.request;

public class Interval {
    public Integer value;
    public UnitEnum unit;

    public Interval(Integer value, UnitEnum unit){
        this.value = value;
        this.unit = unit;
    }
}
