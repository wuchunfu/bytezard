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

import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.SERVICE_TYPE_CORE;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class that implements behaviour of a map-type.
 */
public class MapType extends Type {
    private static final Logger LOG = LoggerFactory.getLogger(MapType.class);

    private final String keyTypeName;
    private final String valueTypeName;

    private io.simforce.bytezard.metadata.type.Type keyType;
    private io.simforce.bytezard.metadata.type.Type valueType;

    public MapType(io.simforce.bytezard.metadata.type.Type keyType, io.simforce.bytezard.metadata.type.Type valueType) {
        super(BaseTypeDef.getMapTypeName(keyType.getTypeName(), valueType.getTypeName()), TypeCategory.MAP, SERVICE_TYPE_CORE);

        this.keyTypeName   = keyType.getTypeName();
        this.valueTypeName = valueType.getTypeName();
        this.keyType       = keyType;
        this.valueType     = valueType;
    }

    public MapType(String keyTypeName, String valueTypeName, TypeRegistry typeRegistry)
        throws BaseException {
        super(BaseTypeDef.getMapTypeName(keyTypeName, valueTypeName), TypeCategory.MAP, SERVICE_TYPE_CORE);

        this.keyTypeName   = keyTypeName;
        this.valueTypeName = valueTypeName;

        resolveReferences(typeRegistry);
    }

    public String getKeyTypeName() {
        return keyTypeName;
    }

    public String getValueTypeName() {
        return valueTypeName;
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }

    public void setKeyType(io.simforce.bytezard.metadata.type.Type keyType) {
        this.keyType = keyType;
    }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
        this.keyType   = typeRegistry.getType(keyTypeName);
        this.valueType = typeRegistry.getType(valueTypeName);
    }

    @Override
    public Map<Object, Object>  createDefaultValue() {
        Map<Object, Object> ret = new HashMap<>();

        Object key = keyType.createDefaultValue();

        if ( key != null) {
            ret.put(key, valueType.createDefaultValue());
        }

        return ret;
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            if (obj instanceof Map) {
                Map<Object, Objects> map = (Map<Object, Objects>) obj;

                for (Map.Entry e : map.entrySet()) {
                    if (!keyType.isValidValue(e.getKey()) || !valueType.isValidValue(e.getValue())) {
                        // invalid key/value
                        return false;
                    }
                }
            } else {
                // invalid type
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        boolean ret = true;

        if (val1 == null) {
            ret = isEmptyMapValue(val2);
        } else if (val2 == null) {
            ret = isEmptyMapValue(val1);
        } else {
            Map map1 = getMapFromValue(val1);

            if (map1 == null) {
                ret = false;
            } else {
                Map map2 = getMapFromValue(val2);

                if (map2 == null) {
                    ret = false;
                } else {
                    int len = map1.size();

                    if (len != map2.size()) {
                        ret = false;
                    } else {
                        for (Object key : map1.keySet()) {
                            if (!valueType.areEqualValues(map1.get(key), map2.get(key), guidAssignments)) {
                                ret = false;

                                break;
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public boolean isValidValueForUpdate(Object obj) {
        if (obj != null) {
            if (obj instanceof Map) {
                Map<Object, Objects> map = (Map<Object, Objects>) obj;

                for (Map.Entry e : map.entrySet()) {
                    if (!keyType.isValidValueForUpdate(e.getKey()) || !valueType.isValidValueForUpdate(e.getValue())) {
                        return false; // invalid key/value
                    }
                }
            } else {
                return false; // invalid type
            }
        }

        return true;
    }

    @Override
    public Map<Object, Object> getNormalizedValue(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            obj = Type.fromJson(obj.toString(), Map.class);
        }

        if (obj instanceof Map) {
            Map<Object, Object> ret = new HashMap<>();

            Map<Object, Objects> map = (Map<Object, Objects>) obj;

            for (Map.Entry e : map.entrySet()) {
                Object normalizedKey = keyType.getNormalizedValue(e.getKey());

                if (normalizedKey != null) {
                    Object value = e.getValue();

                    if (value != null) {
                        Object normalizedValue = valueType.getNormalizedValue(e.getValue());

                        if (normalizedValue != null) {
                            ret.put(normalizedKey, normalizedValue);
                        } else {
                            return null; // invalid value
                        }
                    } else {
                        ret.put(normalizedKey, value);
                    }
                } else {
                    return null; // invalid key
                }
            }

            return ret;
        }

        return null;
    }

    @Override
    public Map<Object, Object> getNormalizedValueForUpdate(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Map) {
            Map<Object, Object> ret = new HashMap<>();

            Map<Object, Objects> map = (Map<Object, Objects>) obj;

            for (Map.Entry e : map.entrySet()) {
                Object normalizedKey = keyType.getNormalizedValueForUpdate(e.getKey());

                if (normalizedKey != null) {
                    Object value = e.getValue();

                    if (value != null) {
                        Object normalizedValue = valueType.getNormalizedValueForUpdate(e.getValue());

                        if (normalizedValue != null) {
                            ret.put(normalizedKey, normalizedValue);
                        } else {
                            return null; // invalid value
                        }
                    } else {
                        ret.put(normalizedKey, value);
                    }
                } else {
                    return null; // invalid key
                }
            }

            return ret;
        }

        return null;
    }

    @Override
    public boolean validateValue(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            if (obj instanceof Map) {
                Map<Object, Objects> map = (Map<Object, Objects>) obj;

                for (Map.Entry e : map.entrySet()) {
                    Object key = e.getKey();

                    if (!keyType.isValidValue(key)) {
                        ret = false;

                        messages.add(objName + "." + key + ": invalid key for type " + getTypeName());
                    } else {
                        Object value = e.getValue();

                        ret = valueType.validateValue(value, objName + "." + key, messages) && ret;
                    }
                }
            } else {
                ret = false;

                messages.add(objName + "=" + obj + ": invalid value for type " + getTypeName());
            }
        }

        return ret;
    }

    @Override
    public boolean validateValueForUpdate(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            if (obj instanceof Map) {
                Map<Object, Objects> map = (Map<Object, Objects>) obj;

                for (Map.Entry e : map.entrySet()) {
                    Object key = e.getKey();

                    if (!keyType.isValidValueForUpdate(key)) {
                        ret = false;

                        messages.add(objName + "." + key + ": invalid key for type " + getTypeName());
                    } else {
                        Object value = e.getValue();

                        ret = valueType.validateValueForUpdate(value, objName + "." + key, messages) && ret;
                    }
                }
            } else {
                ret = false;

                messages.add(objName + "=" + obj + ": invalid value for type " + getTypeName());
            }
        }

        return ret;
    }

    @Override
    public Type getTypeForAttribute() {
        Type keyAttributeType   = keyType.getTypeForAttribute();
        Type valueAttributeType = valueType.getTypeForAttribute();

        if (keyAttributeType == keyType && valueAttributeType == valueType) {
            return this;
        } else {
            Type attributeType = new MapType(keyAttributeType, valueAttributeType);

            if (LOG.isDebugEnabled()) {
                LOG.debug("getTypeForAttribute(): {} ==> {}", getTypeName(), attributeType.getTypeName());
            }

            return attributeType;
        }
    }

    private boolean isEmptyMapValue(Object val) {
        if (val == null) {
            return true;
        } else if (val instanceof Map) {
            return ((Map) val).isEmpty();
        } else if (val instanceof String) {
            Map map = Type.fromJson(val.toString(), Map.class);

            return map == null || map.isEmpty();
        }

        return false;
    }

    private Map getMapFromValue(Object val) {
        final Map ret;

        if (val instanceof Map) {
            ret = (Map) val;
        } else if (val instanceof String) {
            ret = Type.fromJson(val.toString(), Map.class);
        } else {
            ret = null;
        }

        return ret;
    }
}
