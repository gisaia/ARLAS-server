package io.arlas.server.model.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by sfalquier on 29/11/2017.
 */
public class MultiValueFilter<T> extends ArrayList<T> {
    public MultiValueFilter() {
    }

    public MultiValueFilter(Collection<? extends T> c) {
        super(c);
    }

    public MultiValueFilter(T e) {
        super(Arrays.asList(e));
    }
}
