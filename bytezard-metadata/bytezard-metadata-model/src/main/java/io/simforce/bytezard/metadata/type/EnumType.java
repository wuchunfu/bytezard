/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.metadata.type;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef.EnumElementDef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * class that implements behaviour of an enum-type.
 */
public class EnumType extends Type {
    private final EnumDef enumDef;
    private final Map<String, EnumElementDef> elementDefs;
    private final String                           defaultValue;

    public EnumType(EnumDef enumDef) {
        super(enumDef);

        Map<String, EnumElementDef> e = new HashMap<>();

        for (EnumElementDef elementDef : enumDef.getElementDefs()) {
            e.put(elementDef.getValue().toLowerCase(), elementDef);
        }

        String d = enumDef.getDefaultValue();

        if (d == null) {
            EnumElementDef defElem = enumDef.getElementDefs().size() > 0 ? enumDef.getElementDefs().get(0) : null;

            if (defElem != null) {
                d = defElem.getValue();
            }
        }

        this.enumDef      = enumDef;
        this.elementDefs  = Collections.unmodifiableMap(e);
        this.defaultValue = d;
    }

    public EnumDef getEnumDef() { return enumDef; }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
    }

    @Override
    public Object createDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            return elementDefs.containsKey(obj.toString().toLowerCase());
        }

        return true;
    }

    @Override
    public Object getNormalizedValue(Object obj) {
        if (obj != null) {
            EnumElementDef elementDef = elementDefs.get(obj.toString().toLowerCase());

            if (elementDef != null) {
                return elementDef.getValue();
            }
        }

        return null;
    }

    public EnumElementDef getEnumElementDef(String value) {
        if (value != null) {
            return elementDefs.get(value.toLowerCase());
        }

        return null;
    }

    public EnumElementDef getEnumElementDef(Number ordinal) {
        if (ordinal != null) {
            for (EnumElementDef elementDef : elementDefs.values()) {
                if (elementDef.getOrdinal().longValue() == ordinal.longValue()) {
                    return elementDef;
                }
            }
        }

        return null;
    }
}
