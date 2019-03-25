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

package io.arlas.server.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.arlas.server.model.enumerations.CollectionFunction;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Metric {
    public String collectField;
    public CollectionFunction collectFct;

    public Metric(String collectField, CollectionFunction collectFct) {
        this.collectField = collectField;
        this.collectFct = collectFct;
    }

    public Metric() {}

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Metric)) {
            return false;
        }
        if (collectField != null && collectFct != null) {
            return ((Metric) object).collectField.equals(collectField) && ((Metric) object).collectFct.name().equals(collectFct.name());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (collectField != null && collectFct != null) {
            result = 31 * result + collectField.hashCode();
            result = 31 * result + collectFct.name().hashCode();
        }
        return result;
    }
}
