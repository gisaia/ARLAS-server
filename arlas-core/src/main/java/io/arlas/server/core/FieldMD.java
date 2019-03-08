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

package io.arlas.server.core;

public class FieldMD {
    public String path;
    public String type;
    public boolean exists;
    public boolean isIndexed;

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof FieldMD)) {
            return false;
        }
        if (path != null) {
            return path.equals(((FieldMD) object).path);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (path != null) {
            result = 31 * result + path.hashCode();
        }
        return result;
    }
}
