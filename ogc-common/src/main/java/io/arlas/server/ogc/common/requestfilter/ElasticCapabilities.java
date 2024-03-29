/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.ogc.common.requestfilter;

import org.geotools.filter.Capabilities;
import org.geotools.filter.capability.FilterCapabilitiesImpl;
import org.geotools.filter.capability.TemporalCapabilitiesImpl;
import org.geotools.filter.capability.TemporalOperatorImpl;
import org.geotools.filter.visitor.IsFullySupportedFilterVisitor;
import org.geotools.api.filter.*;
import org.geotools.api.filter.capability.TemporalCapabilities;
import org.geotools.api.filter.capability.TemporalOperators;
import org.geotools.api.filter.spatial.BBOX;
import org.geotools.api.filter.spatial.Intersects;
import org.geotools.api.filter.temporal.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom Capabilities supporting temporal capabilities and operators.
 * Uses a custom IsFullySupportedFilterVisitor
 * to enable support for IncludeFilter, ExcludeFilter and BegunBy.
 */
public class ElasticCapabilities extends Capabilities {
    private static Map<Class<?>, String> temporalNames;

    static {
        temporalNames = new HashMap<>();
        temporalNames.put(After.class, After.NAME);
        temporalNames.put(AnyInteracts.class, AnyInteracts.NAME);
        temporalNames.put(Before.class, Before.NAME);
        temporalNames.put(Begins.class, Begins.NAME);
        temporalNames.put(BegunBy.class, BegunBy.NAME);
        temporalNames.put(During.class, During.NAME);
        temporalNames.put(EndedBy.class, EndedBy.NAME);
        temporalNames.put(Ends.class, Ends.NAME);
        temporalNames.put(Meets.class, Meets.NAME);
        temporalNames.put(MetBy.class, MetBy.NAME);
        temporalNames.put(OverlappedBy.class, OverlappedBy.NAME);
        temporalNames.put(TContains.class, TContains.NAME);
        temporalNames.put(TEquals.class, TEquals.NAME);
        temporalNames.put(TOverlaps.class, TOverlaps.NAME);
    }

    private IsFullySupportedFilterVisitor fullySupportedVisitor;

    public ElasticCapabilities() {
        super(new ElasticFilterCapabilities());

        addAll(LOGICAL_OPENGIS);
        addAll(SIMPLE_COMPARISONS_OPENGIS);
        addType(PropertyIsNull.class);
        addType(PropertyIsBetween.class);
        addType(Id.class);
        addType(IncludeFilter.class);
        addType(ExcludeFilter.class);
        addType(PropertyIsLike.class);

        // spatial filters
        addType(BBOX.class);
        addType(Intersects.class);

        //temporal filters
        addType(After.class);
        addType(Before.class);
        addType(During.class);
    }

    @Override
    public boolean fullySupports(Filter filter) {
        if (fullySupportedVisitor == null) {
            fullySupportedVisitor = new ElasticIsFullySupportedFilterVisitor();
        }
        return filter != null ? (Boolean) filter.accept(fullySupportedVisitor, null) : false;
    }

    @Override
    public String toOperationName(@SuppressWarnings("rawtypes") Class filterType) {
        if (filterType != null && temporalNames.containsKey(filterType)) {
            return temporalNames.get(filterType);
        }
        return super.toOperationName(filterType);
    }

    @Override
    public void addName(String name) {
        if (name != null && temporalNames.containsValue(name)) {
            final TemporalOperators operators = getContents().getTemporalCapabilities().getTemporalOperators();
            operators.getOperators().add(new TemporalOperatorImpl(name));
        } else {
            super.addName(name);
        }
    }

    private static class ElasticFilterCapabilities extends FilterCapabilitiesImpl {

        TemporalCapabilitiesImpl temporal;

        @Override
        public TemporalCapabilities getTemporalCapabilities() {
            if (temporal == null) {
                temporal = new TemporalCapabilitiesImpl();
                super.setTemporal(temporal);
            }
            return temporal;
        }

    }

    private class ElasticIsFullySupportedFilterVisitor extends IsFullySupportedFilterVisitor {

        public ElasticIsFullySupportedFilterVisitor() {
            super(getContents());
        }

        public Object visit(ExcludeFilter filter, Object extraData) {
            return true;
        }

        public Object visit(IncludeFilter filter, Object extraData) {
            return true;
        }

        public Object visit(BegunBy begunBy, Object extraData) {
            return visit((BinaryTemporalOperator) begunBy, BegunBy.NAME);
        }
    }

}
