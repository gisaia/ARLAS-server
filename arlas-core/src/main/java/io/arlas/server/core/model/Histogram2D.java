package io.arlas.server.core.model;

public class Histogram2D {
    public Float angle;
    public Float flux;
    public Float probability;

    public Histogram2D(Float angle, Float flux, Float probability) {
        this.angle = angle;
        this.flux = flux;
        this.probability = probability;
    }

}
