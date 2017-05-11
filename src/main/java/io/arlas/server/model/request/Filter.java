package io.arlas.server.model.request;

import java.util.List;

public class Filter {
    public List<String> f;
    public String q;
    public Long before;
    public Long after;
    public String pwithin;
    public String gwithin;
    public String gintersect;
    public String notpwithin;
    public String notgwithin;
    public String notgintersect;
}
