package io.arlas.server.model.request;

public class Aggregation {
    public String type;
    public String field;
    public String interval;
    public String format;
    public String collectField;
    public String collectFct;
    public String order;
    public String on;
    public String size;

    public Aggregation() {
    }
}
