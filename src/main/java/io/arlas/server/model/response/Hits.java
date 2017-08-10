package io.arlas.server.model.response;

import java.util.List;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Hits {
    public List<Hit> hits;

    public long nbhits;

    public long totalnb;
}