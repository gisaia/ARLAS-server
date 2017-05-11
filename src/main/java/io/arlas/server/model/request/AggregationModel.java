package io.arlas.server.model.request;

import org.joda.time.format.DateTimeFormat;

/**
 * Created by hamou on 24/04/17.
 */
public class AggregationModel {
    public String type;
    public String field;
    public String interval;
    public String format;
    public String collectField;
    public String collectFct;
    public String order;
    public String on;
    public String size;

    public AggregationModel() {
    }
}
