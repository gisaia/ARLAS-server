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

package io.arlas.server.core.impl.jdbi.clause;

import io.arlas.server.core.exceptions.NotAllowedException;
import io.arlas.server.core.model.enumerations.UnitEnum;

import java.util.Arrays;
import java.util.List;

public class DateAggregationSelectClause extends AggregationSelectClause {
    private static final List<UnitEnum> biggerThanWeek = Arrays.asList(UnitEnum.year, UnitEnum.quarter, UnitEnum.month, UnitEnum.week);
    public UnitEnum intervalUnit;
    public Integer intervalValue;
    public String format;

    public DateAggregationSelectClause(String columnName, String asName, UnitEnum intervalUnit, Integer intervalValue, String format) throws NotAllowedException {
        super(columnName, asName);
        this.intervalUnit = intervalUnit;
        this.intervalValue = intervalValue;
        this.format = format;

        if (biggerThanWeek.contains(intervalUnit)
                && intervalValue > 1) {
            throw new NotAllowedException("The size must be equal to 1 for the unit " + intervalUnit + ".");
        }
    }
}
