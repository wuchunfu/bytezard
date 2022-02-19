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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.utils.Json;

/**
 * base class that declares interface for all  types.
 */
public abstract class Type {
    private static final Logger LOG = LoggerFactory.getLogger(Type.class);

    private final String typeName;
    private final TypeCategory typeCategory;
    private final String serviceType;

    protected Type(BaseTypeDef typeDef) {
        this(typeDef.getName(), typeDef.getCategory(), typeDef.getServiceType());
    }

    protected Type(String typeName, TypeCategory typeCategory, String serviceType) {
        this.typeName     = typeName;
        this.typeCategory = typeCategory;
        this.serviceType  = serviceType;
    }

    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
    }

    void resolveReferencesPhase2(TypeRegistry typeRegistry) throws BaseException {
    }

    void resolveReferencesPhase3(TypeRegistry typeRegistry) throws BaseException {
    }

    public String getTypeName() { return typeName; }

    public TypeCategory getTypeCategory() { return typeCategory; }

    public String getServiceType() { return serviceType; }

    public abstract Object createDefaultValue();

    public Object createOptionalDefaultValue() {
        return createDefaultValue();
    }

    public Object createDefaultValue(Object val){
        return val == null ? createDefaultValue() : getNormalizedValue(val);
    }

    public abstract boolean isValidValue(Object obj);

    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        final boolean ret;

        if (val1 == null) {
            ret = val2 == null;
        } else if (val2 == null) {
            ret = false;
        } else {
            Object normalizedVal1 = getNormalizedValue(val1);

            if (normalizedVal1 == null) {
                ret = false;
            } else {
                Object normalizedVal2 = getNormalizedValue(val2);

                if (normalizedVal2 == null) {
                    ret = false;
                } else {
                    ret = Objects.equals(normalizedVal1, normalizedVal2);
                }
            }
        }

        return ret;
    }

    public abstract Object getNormalizedValue(Object obj);

    public boolean validateValue(Object obj, String objName, List<String> messages) {
        boolean ret = isValidValue(obj);

        if (!ret) {
            messages.add(objName + "=" + obj + ": invalid value for type " + getTypeName());
        }

        return ret;
    }

    public boolean isValidValueForUpdate(Object obj) { return isValidValue(obj); }

    public Object getNormalizedValueForUpdate(Object obj) { return getNormalizedValue(obj); }

    public boolean validateValueForUpdate(Object obj, String objName, List<String> messages) {
        return validateValue(obj, objName, messages);
    }

    /* for attribute of entity-type, the value would be of ObjectId
     * when an attribute instance is created i.e. Attribute, this method
     * will be called to get EntityType replaced with ObjectType
     */
    public Type getTypeForAttribute() {
        return this;
    }

    public static String toJson(Object obj) {
        return Json.toJson(obj);
    }

    public static <T> T fromJson(String jsonStr, Class<T> type) {
        return Json.fromJson(jsonStr, type);
    }

}
