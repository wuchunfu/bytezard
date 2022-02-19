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

import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.instance.ObjectId;
import io.simforce.bytezard.metadata.model.instance.RelatedObjectId;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Built-in types in .
 */
public class BuiltInTypes {

    /**
     * class that implements behaviour of boolean type.
     */
    public static class BooleanType extends Type {
        private static final Boolean DEFAULT_VALUE = Boolean.FALSE;

        public BooleanType() {
            super(BaseTypeDef.TYPE_BOOLEAN, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Boolean createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null) {
                return true;
            }
            return getNormalizedValue(obj) != null;
        }

        @Override
        public Boolean getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof Boolean) {
                    return (Boolean)obj;
                } else if (obj instanceof String){
                    if (obj.toString().equalsIgnoreCase("true") || obj.toString().equalsIgnoreCase("false")) {
                        return Boolean.valueOf(obj.toString());
                    }
                }
            }
            return null;
        }
    }

    /**
     * class that implements behaviour of byte type.
     */
    public static class ByteType extends Type {
        private static final Byte       DEFAULT_VALUE = (byte) 0;
        private static final BigInteger MIN_VALUE     = BigInteger.valueOf(Byte.MIN_VALUE);
        private static final BigInteger MAX_VALUE     = BigInteger.valueOf(Byte.MAX_VALUE);

        public ByteType() {
            super(BaseTypeDef.TYPE_BYTE, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Byte createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof Byte) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public Byte getNormalizedValue(Object obj) {

            if (obj != null) {
                if (obj instanceof Byte) {
                    return (Byte) obj;
                } else if (obj instanceof Number) {
                    return isValidRange((Number) obj) ? ((Number) obj).byteValue() : null;
                } else {
                    String strValue = obj.toString();

                    if (StringUtils.isNotEmpty(strValue)) {
                        try {
                            return Byte.valueOf(strValue);
                        } catch(NumberFormatException excp) {
                            // ignore
                        }
                    }
                }
            }

            return null;
        }

        private boolean isValidRange(Number num) {
            final boolean ret;

            if (num instanceof Byte) {
                ret = true;
            } else if (num instanceof Double || num instanceof Float || num instanceof Long || num instanceof Integer || num instanceof Short) {
                long longVal = num.longValue();

                ret = longVal >= Byte.MIN_VALUE && longVal <= Byte.MAX_VALUE;
            } else {
                BigInteger bigInt = toBigInteger(num);

                ret = bigInt.compareTo(MIN_VALUE) >= 0 && bigInt.compareTo(MAX_VALUE) <= 0;
            }

            return ret;
        }
    }

    /**
     * class that implements behaviour of short type.
     */
    public static class ShortType extends Type {
        private static final Short      DEFAULT_VALUE = (short) 0;
        private static final BigInteger MIN_VALUE     = BigInteger.valueOf(Short.MIN_VALUE);
        private static final BigInteger MAX_VALUE     = BigInteger.valueOf(Short.MAX_VALUE);

        public ShortType() {
            super(BaseTypeDef.TYPE_SHORT, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Short createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof Short) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public Short getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof Short) {
                    return (Short)obj;
                } else if (obj instanceof Number) {
                    return isValidRange((Number) obj) ? ((Number) obj).shortValue() : null;
                } else {
                    try {
                        return Short.valueOf(obj.toString());
                    } catch(NumberFormatException excp) {
                        // ignore
                    }
                }
            }
            return null;
        }

        private boolean isValidRange(Number num) {
            final boolean ret;

            if (num instanceof Short || num instanceof Byte) {
                ret = true;
            } else if (num instanceof Double || num instanceof Float || num instanceof Long || num instanceof Integer) {
                long longVal = num.longValue();

                ret = longVal >= Short.MIN_VALUE && longVal <= Short.MAX_VALUE;
            } else {
                BigInteger bigInt = toBigInteger(num);

                ret = bigInt.compareTo(MIN_VALUE) >= 0 && bigInt.compareTo(MAX_VALUE) <= 0;
            }

            return ret;
        }
    }

    /**
     * class that implements behaviour of integer type.
     */
    public static class IntType extends Type {
        private static final Integer    DEFAULT_VALUE = 0;
        private static final BigInteger MIN_VALUE     = BigInteger.valueOf(Integer.MIN_VALUE);
        private static final BigInteger MAX_VALUE     = BigInteger.valueOf(Integer.MAX_VALUE);

        public IntType() {
            super(BaseTypeDef.TYPE_INT, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Integer createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof Integer) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public Integer getNormalizedValue(Object obj) {

            if (obj != null) {
                if (obj instanceof Integer) {
                    return (Integer) obj;
                } else if (obj instanceof Number) {
                    return isValidRange((Number) obj) ? ((Number) obj).intValue() : null;
                } else {
                    try {
                        return Integer.valueOf(obj.toString());
                    } catch (NumberFormatException excp) {
                        // ignore
                    }
                }
            }

            return null;
        }

        private boolean isValidRange(Number num) {
            final boolean ret;

            if (num instanceof Integer || num instanceof Short || num instanceof Byte) {
                ret = true;
            } else if (num instanceof Double || num instanceof Float || num instanceof Long) {
                long longVal = num.longValue();

                ret = longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE;
            } else {
                BigInteger bigInt = toBigInteger(num);

                ret = bigInt.compareTo(MIN_VALUE) >= 0 && bigInt.compareTo(MAX_VALUE) <= 0;
            }

            return ret;
        }
    }

    /**
     * class that implements behaviour of long type.
     */
    public static class LongType extends Type {
        private static final Long       DEFAULT_VALUE = 0L;
        private static final BigInteger MIN_VALUE     = BigInteger.valueOf(Long.MIN_VALUE);
        private static final BigInteger MAX_VALUE     = BigInteger.valueOf(Long.MAX_VALUE);

        public LongType() {
            super(BaseTypeDef.TYPE_LONG, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Long createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof Long) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public Long getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof Long) {
                    return (Long) obj;
                } else if (obj instanceof Number) {
                    return isValidRange((Number) obj) ? ((Number) obj).longValue() : null;
                } else {
                    try {
                        return Long.valueOf(obj.toString());
                    } catch (NumberFormatException excp) {
                        // ignore
                    }
                }
            }

            return null;
        }

        private boolean isValidRange(Number num) {
            final boolean ret;

            if (num instanceof Long || num instanceof Integer || num instanceof Short || num instanceof Byte) {
                ret = true;
            } else {
                BigInteger number = toBigInteger(num);

                ret = (number.compareTo(MIN_VALUE) >= 0) && (number.compareTo(MAX_VALUE) <= 0);
            }

            return ret;
        }
    }

    /**
     * class that implements behaviour of float type.
     */
    public static class FloatType extends Type {
        private static final Float      DEFAULT_VALUE = 0f;
        private static final Float      FLOAT_EPSILON = 0.00000001f;
        private static final BigDecimal MIN_VALUE     = BigDecimal.valueOf(-Float.MAX_VALUE);
        private static final BigDecimal MAX_VALUE     = BigDecimal.valueOf(Float.MAX_VALUE);

        public FloatType() {
            super(BaseTypeDef.TYPE_FLOAT, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Float createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
            final boolean ret;

            if (val1 == null) {
                ret = val2 == null;
            } else if (val2 == null) {
                ret = false;
            } else {
                Float floatVal1 = getNormalizedValue(val1);

                if (floatVal1 == null) {
                    ret = false;
                } else {
                    Float floatVal2 = getNormalizedValue(val2);

                    if (floatVal2 == null) {
                        ret = false;
                    } else {
                        ret = Math.abs(floatVal1 - floatVal2) < FLOAT_EPSILON;
                    }
                }
            }

            return ret;
        }

        @Override
        public Float getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof Float) {
                    if (!Float.isInfinite((float) obj)) {
                        return (Float) obj;
                    } else {
                        return null;
                    }
                } else if (obj instanceof Number) {
                    return isValidRange((Number) obj) ? ((Number) obj).floatValue() : null;
                } else {
                    try {
                        Float f = Float.valueOf(obj.toString());
                        if(!Float.isInfinite(f)) {
                            return f;
                        } else {
                            return null;
                        }
                    } catch (NumberFormatException excp) {
                        // ignore
                    }
                }
            }

            return null;
        }

        private boolean isValidRange(Number num) {
            final boolean ret;

            if (num instanceof Float || num instanceof Long || num instanceof Integer || num instanceof Short || num instanceof Byte) {
                ret = true;
            } else if (num instanceof Double) {
                ret = num.floatValue() >= MIN_VALUE.floatValue() && num.floatValue() <= MAX_VALUE.floatValue();
            } else {
                BigDecimal number = new BigDecimal(num.doubleValue());

                ret = (number.compareTo(MIN_VALUE) >= 0) && (number.compareTo(MAX_VALUE) <= 0);
            }

            return ret;
        }
    }

    /**
     * class that implements behaviour of double type.
     */
    public static class DoubleType extends Type {
        private static final Double     DEFAULT_VALUE  = 0d;
        private static final Double     DOUBLE_EPSILON = 0.00000001d;
        private static final BigDecimal MIN_VALUE     = BigDecimal.valueOf(-Double.MAX_VALUE);
        private static final BigDecimal MAX_VALUE     = BigDecimal.valueOf(Double.MAX_VALUE);

        public DoubleType() {
            super(BaseTypeDef.TYPE_DOUBLE, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Double createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
            final boolean ret;

            if (val1 == null) {
                ret = val2 == null;
            } else if (val2 == null) {
                ret = false;
            } else {
                Double doubleVal1 = getNormalizedValue(val1);

                if (doubleVal1 == null) {
                    ret = false;
                } else {
                    Double doubleVal2 = getNormalizedValue(val2);

                    if (doubleVal2 == null) {
                        ret = false;
                    } else {
                        ret = Math.abs(doubleVal1 - doubleVal2) < DOUBLE_EPSILON;
                    }
                }
            }

            return ret;
        }

        @Override
        public Double getNormalizedValue(Object obj) {
            Double ret;

            if (obj != null) {
                if (obj instanceof Double) {
                    if (!Double.isInfinite((double) obj)) {
                        return (Double) obj;
                    } else {
                        return null;
                    }
                } else if (obj instanceof Number) {
                    return isValidRange((Number) obj) ? ((Number) obj).doubleValue() : null;
                } else {
                    try {
                        Double d = Double.valueOf(obj.toString());
                        if(!Double.isInfinite(d)) {
                            return d;
                        } else {
                            return null;
                        }
                    } catch (NumberFormatException excp) {
                        // ignore
                    }
                }
            }

            return null;
        }

        private boolean isValidRange(Number num) {
            final boolean ret;

            if (num instanceof Double || num instanceof Float || num instanceof Long || num instanceof Integer || num instanceof Short || num instanceof Byte) {
                ret = true;
            } else {
                BigDecimal number = new BigDecimal(num.toString());

                ret = (number.compareTo(MIN_VALUE) >= 0) && (number.compareTo(MAX_VALUE) <= 0);
            }

            return ret;
        }
    }

    /**
     * class that implements behaviour of Java BigInteger type.
     */
    public static class BigIntegerType extends Type {
        private static final BigInteger DEFAULT_VALUE = BigInteger.ZERO;

        public BigIntegerType() {
            super(BaseTypeDef.TYPE_BIGINTEGER, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public BigInteger createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof BigInteger) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public BigInteger getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof BigInteger) {
                    return (BigInteger) obj;
                } else if (obj instanceof BigDecimal) {
                    return ((BigDecimal) obj).toBigInteger();
                } else if (obj instanceof Number) {
                    return BigInteger.valueOf(((Number) obj).longValue());
                } else {
                    try {
                        return new BigDecimal(obj.toString()).toBigInteger();
                    } catch (NumberFormatException excp) {
                        // ignore
                    }
                }
            }

            return null;
        }
    }

    /**
     * class that implements behaviour of Java BigDecimal type.
     */
    public static class BigDecimalType extends Type {
        private static final BigDecimal DEFAULT_VALUE = BigDecimal.ZERO;

        public BigDecimalType() {
            super(BaseTypeDef.TYPE_BIGDECIMAL, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public BigDecimal createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof Number) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public BigDecimal getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof BigDecimal) {
                    return (BigDecimal) obj;
                } else if (obj instanceof BigInteger) {
                    return new BigDecimal((BigInteger) obj);
                } else if (obj instanceof Number) {
                    return obj.equals(0) ? BigDecimal.ZERO : BigDecimal.valueOf(((Number) obj).doubleValue());
                } else {
                    try {
                        return new BigDecimal(obj.toString());
                    } catch (NumberFormatException excp) {
                        // ignore
                    }
                }
            }

            return null;
        }
    }

    /**
     * class that implements behaviour of Date type.
     */
    public static class DateType extends Type {
        private static final Date DEFAULT_VALUE = new Date(0);

        public DateType() {
            super(BaseTypeDef.TYPE_DATE, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public Date createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof Date || obj instanceof Number) {
                return true;
            }

            if (obj instanceof String && StringUtils.isEmpty((String) obj)) {
                return true;
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public Date getNormalizedValue(Object obj) {
            if (obj != null) {
                if (obj instanceof Date) {
                    return (Date) obj;
                } else if (obj instanceof Number) {
                    return new Date(((Number) obj).longValue());
                } else {
                    try {
                        return BaseTypeDef.getDateFormatter().parse(obj.toString());
                    } catch (ParseException excp) {
                        try { // try to read it as a number
                            long longDate = Long.valueOf(obj.toString());
                            return new Date(longDate);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }

            return null;
        }
    }

    /**
     * class that implements behaviour of String type.
     */
    public static class StringType extends Type {
        private static final String DEFAULT_VALUE = "";
        private static final String OPTIONAL_DEFAULT_VALUE = null;

        public StringType() {
            super(BaseTypeDef.TYPE_STRING, TypeCategory.PRIMITIVE, SERVICE_TYPE_CORE);
        }

        @Override
        public String createDefaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public Object createOptionalDefaultValue() {
            return OPTIONAL_DEFAULT_VALUE;
        }

        @Override
        public boolean isValidValue(Object obj) {
            return true;
        }

        @Override
        public String getNormalizedValue(Object obj) {
            if (obj != null) {
                return obj.toString();
            }

            return null;
        }
    }

    /**
     * class that implements behaviour of  object-id type.
     */
    public static class ObjectIdType extends Type {
        public static final String DEFAULT_UNASSIGNED_GUID = "-1";

        private final String objectType;

        public ObjectIdType() {
            super(BaseTypeDef.TYPE_OBJECT_ID, TypeCategory.OBJECT_ID_TYPE, SERVICE_TYPE_CORE);

            objectType = BaseTypeDef.TYPE_ASSET;
        }

        public ObjectIdType(String objectType) {
            super(BaseTypeDef.TYPE_OBJECT_ID, TypeCategory.OBJECT_ID_TYPE, SERVICE_TYPE_CORE);

            this.objectType = objectType;
        }

        public String getObjectType() { return objectType; }

        @Override
        public ObjectId createDefaultValue() {
            return new ObjectId(DEFAULT_UNASSIGNED_GUID, objectType);
        }

        @Override
        public boolean isValidValue(Object obj) {
            if (obj == null || obj instanceof ObjectId) {
                return true;
            } else if (obj instanceof Map) {
                return isValidMap((Map)obj);
            }

            return getNormalizedValue(obj) != null;
        }

        @Override
        public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
            boolean ret = true;

            if (val1 == null) {
                ret = val2 == null;
            } else if (val2 == null) {
                ret = false;
            } else {
                ObjectId v1 = getNormalizedValue(val1);
                ObjectId v2 = getNormalizedValue(val2);

                if (v1 == null || v2 == null) {
                    ret = false;
                } else {
                    String guid1 = v1.getGuid();
                    String guid2 = v2.getGuid();

                    if (guidAssignments != null ) {
                        if (guidAssignments.containsKey(guid1)) {
                            guid1 = guidAssignments.get(guid1);
                        }

                        if (guidAssignments.containsKey(guid2)) {
                            guid2 = guidAssignments.get(guid2);
                        }
                    }

                    boolean isV1AssignedGuid = TypeUtil.isAssignedGuid(guid1);
                    boolean isV2AssignedGuid = TypeUtil.isAssignedGuid(guid2);

                    if (isV1AssignedGuid == isV2AssignedGuid) { // if both have assigned/unassigned guids, compare guids
                        ret = Objects.equals(guid1, guid2);
                    } else { // if one has assigned and other unassigned guid, compare typeName and unique-attribute
                        ret = Objects.equals(v1.getTypeName(), v2.getTypeName()) && Objects.equals(v1.getUniqueAttributes(), v2.getUniqueAttributes());
                    }
                }
            }

            return ret;
        }

        @Override
        public ObjectId getNormalizedValue(Object obj) {
            ObjectId ret = null;

            if (obj != null) {
                if (obj instanceof ObjectId) {
                    ret = (ObjectId) obj;
                } else if (obj instanceof Map) {
                    Map map = (Map) obj;

                    if (isValidMap(map)) {
                        if (map.containsKey(RelatedObjectId.KEY_RELATIONSHIP_TYPE)) {
                            ret = new RelatedObjectId(map);
                        } else {
                            ret = new ObjectId(map);
                        }
                    }
                }
            }

            return ret;
        }

        private boolean isValidMap(Map map) {
            Object guid = map.get(ObjectId.KEY_GUID);

            if (guid != null && StringUtils.isNotEmpty(guid.toString())) {
                return true;
            } else {
                Object typeName = map.get(ObjectId.KEY_TYPENAME);
                if (typeName != null && StringUtils.isNotEmpty(typeName.toString())) {
                    Object uniqueAttributes = map.get(ObjectId.KEY_UNIQUE_ATTRIBUTES);

                    if (uniqueAttributes instanceof Map && MapUtils.isNotEmpty((Map) uniqueAttributes)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static BigInteger toBigInteger(Number num) {
        final BigInteger ret;

        if (num instanceof BigInteger) {
            ret = (BigInteger) num;
        } else if (num instanceof Byte || num instanceof Short || num instanceof Integer || num instanceof Long) {
            ret = BigInteger.valueOf(num.longValue());
        } else if (num instanceof BigDecimal) {
            ret = ((BigDecimal) num).toBigInteger();
        } else {
            ret = new BigDecimal(num.toString()).toBigInteger();
        }

        return ret;
    }
}
