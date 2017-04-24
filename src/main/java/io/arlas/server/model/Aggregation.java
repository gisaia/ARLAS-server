package io.arlas.server.model;

import org.joda.time.format.DateTimeFormat;

/**
 * Created by hamou on 24/04/17.
 */
public class Aggregation {
    public String aggType;
    public String aggField;
    public String aggInterval;
    public String aggFormat;
    public String aggCollectField;
    public String aggCollectFct;
    public String aggOrder;
    public String aggOn;

    public Aggregation() {
    }
}
