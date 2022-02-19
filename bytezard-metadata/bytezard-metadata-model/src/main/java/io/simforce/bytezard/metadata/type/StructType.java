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

import static io.simforce.bytezard.metadata.model.TypeCategory.OBJECT_ID_TYPE;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_STRING;
import static io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE;
import static io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef.CONSTRAINT_TYPE_INVERSE_REF;
import static io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef.CONSTRAINT_TYPE_OWNED_REF;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.Entity;
import io.simforce.bytezard.metadata.model.instance.Struct;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef.Cardinality;
import io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class that implements behaviour of a struct-type.
 */
public class StructType extends Type {

    private static final Logger LOG = LoggerFactory.getLogger(StructType.class);

    public static final String UNIQUE_ATTRIBUTE_SHADE_PROPERTY_PREFIX = "__u_";

    private final StructDef structDef;

    protected Map<String, Attribute> allAttributes  = Collections.emptyMap();
    protected Map<String, Attribute> uniqAttributes = Collections.emptyMap();

    public StructType(StructDef structDef) {
        super(structDef);

        this.structDef = structDef;
    }

    public StructType(StructDef structDef, TypeRegistry typeRegistry) throws BaseException {
        super(structDef);

        this.structDef = structDef;

        this.resolveReferences(typeRegistry);
    }

    public StructDef getStructDef() { return structDef; }

    public Type getAttributeType(String attributeName) {
        Attribute attribute = getAttribute(attributeName);

        return attribute != null ? attribute.getAttributeType() : null;
    }

    public AttributeDef getAttributeDef(String attributeName) {
        Attribute attribute = getAttribute(attributeName);

        return attribute != null ? attribute.getAttributeDef() : null;
    }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
        Map<String, Attribute> a = new HashMap<>();

        for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
            Type attrType = typeRegistry.getType(attributeDef.getTypeName());
            Attribute attribute = new Attribute(this, attributeDef, attrType);

            Cardinality cardinality = attributeDef.getCardinality();

            if (cardinality == Cardinality.LIST || cardinality == Cardinality.SET) {
                if (!(attrType instanceof ArrayType)) {
                    throw new BaseException(ErrorCode.INVALID_ATTRIBUTE_TYPE_FOR_CARDINALITY,
                                                 getTypeName(), attributeDef.getName());
                }

                ArrayType arrayType = (ArrayType)attrType;

                arrayType.setMinCount(attributeDef.getValuesMinCount());
                arrayType.setMaxCount(attributeDef.getValuesMaxCount());
                arrayType.setCardinality(cardinality);
            }

            //check if attribute type is not classification
            if (attrType instanceof ArrayType) {
                attrType = ((ArrayType) attrType).getElementType();
            } else if (attrType instanceof MapType) {
                attrType = ((MapType) attrType).getValueType();
            }

            if (attrType instanceof ClassificationType) {
                throw new BaseException(ErrorCode.ATTRIBUTE_TYPE_INVALID, getTypeName(), attributeDef.getName());
            }

            if (attrType instanceof BusinessMetadataType) {
                throw new BaseException(ErrorCode.ATTRIBUTE_TYPE_INVALID, getTypeName(), attributeDef.getName());
            }

            a.put(attributeDef.getName(), attribute);
        }

        resolveConstraints(typeRegistry);

        this.allAttributes  = Collections.unmodifiableMap(a);
        this.uniqAttributes = getUniqueAttributes(this.allAttributes);
    }

    private void resolveConstraints(TypeRegistry typeRegistry) throws BaseException {
        for (AttributeDef attributeDef : getStructDef().getAttributeDefs()) {
            if (CollectionUtils.isEmpty(attributeDef.getConstraints())) {
                continue;
            }

            for (ConstraintDef constraint : attributeDef.getConstraints()) {
                if (constraint.isConstraintType(CONSTRAINT_TYPE_OWNED_REF)) {
                    EntityType attrType = getReferencedEntityType(typeRegistry.getType(attributeDef.getTypeName()));

                    if (attrType == null) {
                        throw new BaseException(ErrorCode.CONSTRAINT_OWNED_REF_ATTRIBUTE_INVALID_TYPE,
                                getTypeName(), attributeDef.getName(), CONSTRAINT_TYPE_OWNED_REF, attributeDef.getTypeName());
                    }
                } else if (constraint.isConstraintType(CONSTRAINT_TYPE_INVERSE_REF)) {
                    EntityType attrType = getReferencedEntityType(typeRegistry.getType(attributeDef.getTypeName()));

                    if (attrType == null) {
                        throw new BaseException(ErrorCode.CONSTRAINT_INVERSE_REF_ATTRIBUTE_INVALID_TYPE,
                                getTypeName(), attributeDef.getName(), CONSTRAINT_TYPE_INVERSE_REF,
                                attributeDef.getTypeName());
                    }

                    String inverseRefAttrName = TypeUtil.getStringValue(constraint.getParams(), CONSTRAINT_PARAM_ATTRIBUTE);

                    if (StringUtils.isBlank(inverseRefAttrName)) {
                        throw new BaseException(ErrorCode.CONSTRAINT_MISSING_PARAMS,
                                getTypeName(), attributeDef.getName(),
                                CONSTRAINT_PARAM_ATTRIBUTE, CONSTRAINT_TYPE_INVERSE_REF,
                                String.valueOf(constraint.getParams()));
                    }

                    AttributeDef inverseRefAttrDef = attrType.getStructDef().getAttribute(inverseRefAttrName);

                    if (inverseRefAttrDef == null) {
                        throw new BaseException(ErrorCode.CONSTRAINT_INVERSE_REF_INVERSE_ATTRIBUTE_NON_EXISTING,
                                getTypeName(), attributeDef.getName(),
                                CONSTRAINT_TYPE_INVERSE_REF, attrType.getTypeName(), inverseRefAttrName);
                    }

                    EntityType inverseRefAttrType = getReferencedEntityType(typeRegistry.getType(inverseRefAttrDef.getTypeName()));

                    if (inverseRefAttrType == null) {
                        throw new BaseException(ErrorCode.CONSTRAINT_INVERSE_REF_INVERSE_ATTRIBUTE_INVALID_TYPE,
                                getTypeName(), attributeDef.getName(),
                                CONSTRAINT_TYPE_INVERSE_REF, attrType.getTypeName(), inverseRefAttrName);
                    }
                }
            }
        }
    }

    @Override
    void resolveReferencesPhase2(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferencesPhase2(typeRegistry);
        for (Attribute attribute : allAttributes.values()) {
            if (attribute.getInverseRefAttributeName() == null) {
                continue;
            }
            // Set the inverse reference attribute.
            Type referencedType = typeRegistry.getType(attribute.getAttributeDef().getTypeName());
            EntityType referencedEntityType = getReferencedEntityType(referencedType);
            Attribute inverseReference = referencedEntityType.getAttribute(attribute.getInverseRefAttributeName());

            attribute.setInverseRefAttribute(inverseReference);
         }
    }

    @Override
    public Struct createDefaultValue() {
        Struct ret = new Struct(structDef.getName());

        populateDefaultValues(ret);

        return  ret;
    }

    @Override
    public Object createDefaultValue(Object defaultValue) {
        Struct ret = new Struct(structDef.getName());

        populateDefaultValues(ret);

        return  ret;
    }

    public Map<String, Attribute> getAllAttributes() {
        return allAttributes;
    }

    public Map<String, Attribute> getUniqAttributes() {
        return uniqAttributes;
    }

    public Attribute getAttribute(String attributeName) {
        Attribute ret = allAttributes.get(attributeName);

        if (ret == null) {
            ret = getSystemAttribute(attributeName);
        }

        if (ret == null) {
            ret = getBusinesAAttribute(attributeName);
        }

        return ret;
    }

    public Attribute getSystemAttribute(String attributeName) {
        return null;
    }

    public Attribute getBusinesAAttribute(String attributeName) {
        return null;
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            if (obj instanceof Struct) {
                Struct structObj = (Struct) obj;

                for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                    if (!isAssignableValue(structObj.getAttribute(attributeDef.getName()), attributeDef)) {
                        return false;
                    }
                }
            } else if (obj instanceof Map) {
                Map map = TypeUtil.toStructAttributes((Map) obj);

                for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                    if (!isAssignableValue(map.get(attributeDef.getName()), attributeDef)) {
                        return false; // no value for non-optinal attribute
                    }
                }
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        boolean ret = true;

        if (val1 == null) {
            ret = val2 == null;
        } else if (val2 == null) {
            ret = false;
        } else {
            Struct structVal1 = getStructFromValue(val1);

            if (structVal1 == null) {
                ret = false;
            } else {
                Struct structVal2 = getStructFromValue(val2);

                if (structVal2 == null) {
                    ret = false;
                } else if (!StringUtils.equalsIgnoreCase(structVal1.getTypeName(), structVal2.getTypeName())) {
                    ret = false;
                } else {
                    for (Attribute attribute : getAllAttributes().values()) {
                        Object attrValue1 = structVal1.getAttribute(attribute.getName());
                        Object attrValue2 = structVal2.getAttribute(attribute.getName());

                        if (!attribute.getAttributeType().areEqualValues(attrValue1, attrValue2, guidAssignments)) {
                            ret = false;

                            break;
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
            Map<String, Object> attributes;

            if (obj instanceof Struct) {
                Struct structObj = (Struct) obj;
                attributes = structObj.getAttributes();

            } else if (obj instanceof Map) {
                attributes = TypeUtil.toStructAttributes((Map) obj);

            } else {
                return false;
            }

            if (MapUtils.isNotEmpty(attributes)) {
                for (Map.Entry<String, Object> e : attributes.entrySet()) {
                    String            attrName  = e.getKey();
                    Object            attrValue = e.getValue();
                    AttributeDef attrDef   = structDef.getAttribute(attrName);

                    if (attrValue == null || attrDef == null) {
                        continue;
                    }

                    if (!isAssignableValueForUpdate(attrValue, attrDef)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public Object getNormalizedValue(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValue(obj)) {
                if (obj instanceof Struct) {
                    normalizeAttributeValues((Struct) obj);
                    ret = obj;
                } else if (obj instanceof Map) {
                    normalizeAttributeValues((Map) obj);
                    ret = obj;
                }
            }
        }

        return ret;
    }

    @Override
    public Object getNormalizedValueForUpdate(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValueForUpdate(obj)) {
                if (obj instanceof Struct) {
                    normalizeAttributeValuesForUpdate((Struct) obj);
                    ret = obj;
                } else if (obj instanceof Map) {
                    normalizeAttributeValuesForUpdate((Map) obj);
                    ret = obj;
                }
            }
        }

        return ret;
    }

    @Override
    public boolean validateValue(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            if (obj instanceof Struct) {
                Struct structObj = (Struct) obj;

                for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                    String         attrName  = attributeDef.getName();
                    Attribute attribute = allAttributes.get(attributeDef.getName());

                    if (attribute != null) {
                        Type dataType  = attribute.getAttributeType();
                        Object    value     = structObj.getAttribute(attrName);
                        String    fieldName = objName + "." + attrName;

                        if (value != null) {
                            ret = dataType.validateValue(value, fieldName, messages) && ret;
                        } else if (!attributeDef.getIsOptional()) {
                            // if required attribute is null, check if attribute value specified in relationship
                            if (structObj instanceof Entity) {
                                Entity entityObj = (Entity) structObj;

                                if (entityObj.getRelationshipAttribute(attrName) == null) {
                                    ret = false;
                                    messages.add(fieldName + ": mandatory attribute value missing in type " + getTypeName());
                                }
                            } else {
                                ret = false;
                                messages.add(fieldName + ": mandatory attribute value missing in type " + getTypeName());
                            }
                        }
                    }
                }
            } else if (obj instanceof Map) {
                Map attributes             = TypeUtil.toStructAttributes((Map)obj);
                Map relationshipAttributes = TypeUtil.toRelationshipAttributes((Map)obj);

                for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                    String             attrName  = attributeDef.getName();
                    Attribute     attribute = allAttributes.get(attributeDef.getName());

                    if (attribute != null) {
                        Type dataType  = attribute.getAttributeType();
                        Object    value     = attributes.get(attrName);
                        String    fieldName = objName + "." + attrName;

                        if (value != null) {
                            ret = dataType.validateValue(value, fieldName, messages) && ret;
                        } else if (!attributeDef.getIsOptional()) {
                            // if required attribute is null, check if attribute value specified in relationship
                            if (MapUtils.isEmpty(relationshipAttributes) || !relationshipAttributes.containsKey(attrName)) {
                                ret = false;
                                messages.add(fieldName + ": mandatory attribute value missing in type " + getTypeName());
                            }
                        }
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
        boolean             ret        = true;
        Map<String, Object> attributes = null;

        if (obj != null) {
            if (obj instanceof Struct) {
                Struct structObj = (Struct) obj;
                attributes = structObj.getAttributes();

            } else if (obj instanceof Map) {
                attributes = TypeUtil.toStructAttributes((Map) obj);

            } else {
                ret = false;
                messages.add(objName + "=" + obj + ": invalid value for type " + getTypeName());
            }

            if (MapUtils.isNotEmpty(attributes)) {
                for (Map.Entry<String, Object> e : attributes.entrySet()) {
                    String         attrName  = e.getKey();
                    Object         attrValue = e.getValue();
                    Attribute attribute = allAttributes.get(attrName);

                    if (attrValue == null) {
                        continue;
                    }

                    if (attribute != null) {
                        Type dataType  = attribute.getAttributeType();
                        String    fieldName = objName + "." + attrName;

                        ret = dataType.validateValueForUpdate(attrValue, fieldName, messages) && ret;
                    }
                }
            }
        }

        return ret;
    }

    public void normalizeAttributeValues(Struct obj) {
        if (obj != null) {
            for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                String attributeName = attributeDef.getName();

                if (obj.hasAttribute(attributeName)) {
                    Object attributeValue = getNormalizedValue(obj.getAttribute(attributeName), attributeDef);

                    obj.setAttribute(attributeName, attributeValue);
                } else if (!attributeDef.getIsOptional()) {
                    obj.setAttribute(attributeName, createDefaultValue(attributeDef));
                }
            }
        }
    }

    public void normalizeAttributeValuesForUpdate(Struct obj) {
        if (obj != null) {
            for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                String attributeName = attributeDef.getName();

                if (obj.hasAttribute(attributeName)) {
                    Object attributeValue = getNormalizedValueForUpdate(obj.getAttribute(attributeName), attributeDef);
                    obj.setAttribute(attributeName, attributeValue);
                }
            }
        }
    }

    public void normalizeAttributeValues(Map<String, Object> obj) {
        if (obj != null) {
            for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                String attributeName = attributeDef.getName();

                if (obj.containsKey(attributeName)) {
                    Object attributeValue = getNormalizedValue(obj.get(attributeName), attributeDef);

                    obj.put(attributeName, attributeValue);
                } else if (!attributeDef.getIsOptional()) {
                    obj.put(attributeName, createDefaultValue(attributeDef));
                }
            }
        }
    }

    public void normalizeAttributeValuesForUpdate(Map<String, Object> obj) {
        if (obj != null) {
            for (AttributeDef attrDef : structDef.getAttributeDefs()) {
                String attrName  = attrDef.getName();
                Object attrValue = obj.get(attrName);

                if (obj.containsKey(attrName)) {
                    attrValue = getNormalizedValueForUpdate(attrValue, attrDef);
                    obj.put(attrName, attrValue);
                }
            }
        }
    }

    public void populateDefaultValues(Struct obj) {
        if (obj != null) {
            Map<String, Object> attributes = obj.getAttributes();

            if (attributes == null) {
                attributes = new HashMap<>();
            }

            for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                if (!attributeDef.getIsOptional()) {
                    attributes.put(attributeDef.getName(), createDefaultValue(attributeDef));
                }
            }

            obj.setAttributes(attributes);
        }
    }

    private Object createDefaultValue(AttributeDef attributeDef) {
        Object ret = null;

        if (attributeDef != null) {
            Attribute attribute = allAttributes.get(attributeDef.getName());

            if (attribute != null) {
                Type dataType = attribute.getAttributeType();

                ret = dataType.createDefaultValue(attributeDef.getDefaultValue());
            }
        }

        return ret;
    }

    private boolean isAssignableValue(Object value, AttributeDef attributeDef) {
        boolean ret = true;

        if (value != null) {
            Attribute attribute = allAttributes.get(attributeDef.getName());

            if (attribute != null) {
                Type attrType = attribute.getAttributeType();

                    if (!attrType.isValidValue(value)) {
                        ret = false; // invalid value
                    }
            }
        } else if (!attributeDef.getIsOptional()) {
            ret = false; // mandatory attribute not present
        }

        return ret;
    }

    private boolean isAssignableValueForUpdate(Object value, AttributeDef attributeDef) {
        boolean ret = true;

        if (value != null) {
            Attribute attribute = allAttributes.get(attributeDef.getName());

            if (attribute != null) {
                Type attrType = attribute.getAttributeType();

                if (!attrType.isValidValueForUpdate(value)) {
                    ret = false; // invalid value
                }
            }
        }

        return ret;
    }

    private Object getNormalizedValue(Object value, AttributeDef attributeDef) {
        Attribute attribute = allAttributes.get(attributeDef.getName());

        if (attribute != null) {
            Type attrType = attribute.getAttributeType();

            if (value == null) {
                if (!attributeDef.getIsOptional()) {
                    return attrType.createDefaultValue();
                }
            } else {
                return attrType.getNormalizedValue(value);
            }
        }

        return null;
    }

    private Object getNormalizedValueForUpdate(Object value, AttributeDef attributeDef) {
        Attribute attribute = allAttributes.get(attributeDef.getName());

        if (attribute != null) {
            Type attrType = attribute.getAttributeType();

            if (value != null) {
                return attrType.getNormalizedValueForUpdate(value);
            }
        }

        return null;
    }

    public String getVertexPropertyName(String attrName) throws BaseException {
        Attribute attribute = getAttribute(attrName);

        if (attribute != null) {
            return attribute.getVertexPropertyName();
        }

        throw new BaseException(ErrorCode.UNKNOWN_ATTRIBUTE, attrName, structDef.getName());
    }

    public String getQualifiedAttributePropertyKey(String attrName) throws BaseException {
        if ( allAttributes.containsKey(attrName)) {
            return allAttributes.get(attrName).getVertexPropertyName();
        }

        throw new BaseException(ErrorCode.UNKNOWN_ATTRIBUTE, attrName, structDef.getName());
    }

    static EntityType getReferencedEntityType(Type type) {
        if (type instanceof ArrayType) {
            type = ((ArrayType)type).getElementType();
        }

        if (type instanceof MapType) {
            type = ((MapType)type).getValueType();
        }

        return type instanceof EntityType ? (EntityType)type : null;
    }

    protected Map<String, Attribute> getUniqueAttributes(Map<String, Attribute> attributes) {
        Map<String, Attribute> ret = new HashMap<>();

        if (MapUtils.isNotEmpty(attributes)) {
            for (Attribute attribute : attributes.values()) {
                if (attribute.getAttributeDef().getIsUnique()) {
                    ret.put(attribute.getName(), attribute);
                }
            }
        }

        return Collections.unmodifiableMap(ret);
    }

    private Struct getStructFromValue(Object val) {
        final Struct ret;

        if (val instanceof Struct) {
            ret = (Struct) val;
        } else if (val instanceof Map) {
            ret = new Struct((Map) val);
        } else if (val instanceof String) {
            Map map = Type.fromJson(val.toString(), Map.class);

            if (map == null) {
                ret = null;
            } else {
                ret = new Struct((Map) val);
            }
        } else {
            ret = null;
        }

        return ret;
    }

    protected void ensureNoAttributeOverride(List<? extends StructType> superTypes) throws BaseException {
        for (StructType superType : superTypes) {
            for (AttributeDef attributeDef : this.structDef.getAttributeDefs()) {
                if (superType.getAllAttributes().containsKey(attributeDef.getName())) {
                    throw new BaseException(ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS_IN_PARENT_TYPE, getStructDef().getName(), attributeDef.getName(), superType.getStructDef().getName());
                }
            }
        }
    }

    public static class Attribute {
        public static final Object VERTEX_PROPERTY_PREFIX_STRING_INDEX_TYPE = "__s_";
        private final io.simforce.bytezard.metadata.type.StructType definedInType;
        private final Type attributeType;
        private final AttributeDef        attributeDef;
        private final String                   qualifiedName;
        private final String                   vertexPropertyName;
        private final String                   vertexUniquePropertyName;
        private final boolean                  isOwnedRef;
        private final boolean                  isObjectRef;
        private final String                   inverseRefAttributeName;
        private Attribute                 inverseRefAttribute;
        private String                         relationshipName;
        private String                         relationshipEdgeLabel;
        private RelationshipEdgeDirection relationshipEdgeDirection;
        private boolean                        isLegacyAttribute;
        private String                         indexFieldName;

        private boolean isDynAttribute            = false;
        private boolean isDynAttributeEvalTrigger = false;

        public Attribute(io.simforce.bytezard.metadata.type.StructType definedInType, AttributeDef attrDef, Type attributeType, String relationshipName, String relationshipLabel) {
            this.definedInType            = definedInType;
            this.attributeDef             = attrDef;
            this.attributeType            = attributeType.getTypeForAttribute();
            this.qualifiedName            = getQualifiedAttributeName(definedInType.getStructDef(), attributeDef.getName());
            this.vertexPropertyName       = generateVertexPropertyName(definedInType.getStructDef(), attributeDef, qualifiedName);
            this.vertexUniquePropertyName = attrDef.getIsUnique() ? encodePropertyKey(getQualifiedAttributeName(definedInType.getStructDef(), UNIQUE_ATTRIBUTE_SHADE_PROPERTY_PREFIX + attributeDef.getName())) : null;
            this.relationshipName         = relationshipName;
            this.relationshipEdgeLabel    = getRelationshipEdgeLabel(relationshipLabel);
            boolean isOwnedRef            = false;
            String  inverseRefAttribute   = null;

            LOG.debug("Attribute {} will use the vertext property name {}.", qualifiedName, vertexPropertyName);

            if (CollectionUtils.isNotEmpty(attributeDef.getConstraints())) {
                for (ConstraintDef constraint : attributeDef.getConstraints()) {
                    if (constraint.isConstraintType(CONSTRAINT_TYPE_OWNED_REF)) {
                        isOwnedRef = true;
                    }

                    if (constraint.isConstraintType(CONSTRAINT_TYPE_INVERSE_REF)) {
                        Object val = constraint.getParam(CONSTRAINT_PARAM_ATTRIBUTE);

                        if (val != null) {
                            inverseRefAttribute = val.toString();
                        }
                    }
                }
            }

            this.isOwnedRef                = isOwnedRef;
            this.inverseRefAttributeName   = inverseRefAttribute;
            this.relationshipEdgeDirection = RelationshipEdgeDirection.OUT;

            switch (this.attributeType.getTypeCategory()) {
                case OBJECT_ID_TYPE:
                    isObjectRef = true;
                    break;

                case MAP:
                    MapType mapType = (MapType) this.attributeType;

                    isObjectRef = mapType.getValueType().getTypeCategory() == OBJECT_ID_TYPE;
                    break;

                case ARRAY:
                    ArrayType arrayType = (ArrayType) this.attributeType;

                    isObjectRef = arrayType.getElementType().getTypeCategory() == OBJECT_ID_TYPE;
                    break;

                default:
                    isObjectRef = false;
                    break;
            }
        }

        public Attribute(io.simforce.bytezard.metadata.type.StructType definedInType, AttributeDef attrDef, Type attributeType) {
            this(definedInType, attrDef, attributeType, null, null);
        }

        public Attribute(Attribute other) {
            this.definedInType             = other.definedInType;
            this.attributeType             = other.attributeType;
            this.attributeDef              = other.attributeDef;
            this.qualifiedName             = other.qualifiedName;
            this.vertexPropertyName        = other.vertexPropertyName;
            this.vertexUniquePropertyName  = other.vertexUniquePropertyName;
            this.isOwnedRef                = other.isOwnedRef;
            this.isObjectRef               = other.isObjectRef;
            this.inverseRefAttributeName   = other.inverseRefAttributeName;
            this.inverseRefAttribute       = other.inverseRefAttribute;
            this.relationshipName          = other.relationshipName;
            this.relationshipEdgeLabel     = other.relationshipEdgeLabel;
            this.relationshipEdgeDirection = other.relationshipEdgeDirection;
            this.isLegacyAttribute         = other.isLegacyAttribute;
            this.indexFieldName            = other.indexFieldName;
            this.isDynAttribute            = false;
            this.isDynAttributeEvalTrigger = false;
        }

        public io.simforce.bytezard.metadata.type.StructType getDefinedInType() { return definedInType; }

        public StructDef getDefinedInDef() { return definedInType.getStructDef(); }

        public Type getAttributeType() {
            return attributeType;
        }

        public AttributeDef getAttributeDef() {
            return attributeDef;
        }

        public String getName() { return attributeDef.getName(); }

        public String getTypeName() { return attributeDef.getTypeName(); }

        public String getQualifiedName() { return qualifiedName; }

        public String getVertexPropertyName() { return vertexPropertyName; }

        public String getVertexUniquePropertyName() { return vertexUniquePropertyName; }

        public boolean isOwnedRef() { return isOwnedRef; }

        public boolean isObjectRef() { return isObjectRef; }

        public String getInverseRefAttributeName() { return inverseRefAttributeName; }

        public Attribute getInverseRefAttribute() { return inverseRefAttribute; }

        public void setInverseRefAttribute(Attribute inverseAttr) { inverseRefAttribute = inverseAttr; }

        public String getRelationshipName() { return relationshipName; }

        public void setRelationshipName(String relationshipName) { this.relationshipName = relationshipName; }

        public String getRelationshipEdgeLabel() { return relationshipEdgeLabel; }

        public void setRelationshipEdgeLabel(String relationshipEdgeLabel) { this.relationshipEdgeLabel = relationshipEdgeLabel; }

        public RelationshipEdgeDirection getRelationshipEdgeDirection() { return relationshipEdgeDirection; }

        public void setRelationshipEdgeDirection(RelationshipEdgeDirection relationshipEdgeDirection) {
            this.relationshipEdgeDirection = relationshipEdgeDirection;
        }

        public boolean isLegacyAttribute() { return isLegacyAttribute; }

        public void setLegacyAttribute(boolean legacyAttribute) { isLegacyAttribute = legacyAttribute; }

        public String getIndexFieldName() { return indexFieldName; }

        public void setIndexFieldName(String indexFieldName) { this.indexFieldName = indexFieldName; }

        public int getSearchWeight() { return attributeDef.getSearchWeight(); }

        public AttributeDef.IndexType getIndexType() { return attributeDef.getIndexType();}

        public boolean getIsDynAttribute() { return isDynAttribute; }

        public void setIsDynAttribute(boolean isDynAttribute){ this.isDynAttribute = isDynAttribute; }

        public boolean getIsDynAttributeEvalTrigger() { return isDynAttributeEvalTrigger; }

        public void setIsDynAttributeEvalTrigger(boolean isDynAttributeEvalTrigger) { this.isDynAttributeEvalTrigger = isDynAttributeEvalTrigger; }

        public Set<String> getOptionSet(String optionName) {
            String      strValue = attributeDef.getOption(optionName);
            Set<String> ret      = StringUtils.isBlank(strValue) ? null : Type.fromJson(strValue, Set.class);

            return ret;
        }

        public Integer getOptionInt(String optionName) {
            String  strValue = attributeDef.getOption(optionName);
            Integer ret      = StringUtils.isBlank(strValue) ? null : Integer.parseInt(strValue);

            return ret;
        }

        public String getOptionString(String optionName) {
            String  strValue = attributeDef.getOption(optionName);
            String ret      = StringUtils.isBlank(strValue) ? null : strValue;

            return ret;
        }

        public static String getEdgeLabel(String property) {
            return "__" + property;
        }

        public static String encodePropertyKey(String key) {
            if (StringUtils.isBlank(key)) {
                return key;
            }

            for (String[] strMap : RESERVED_CHAR_ENCODE_MAP) {
                key = key.replace(strMap[0], strMap[1]);
            }

            return key;
        }

        public static String decodePropertyKey(String key) {
            if (StringUtils.isBlank(key)) {
                return key;
            }

            for (String[] strMap : RESERVED_CHAR_ENCODE_MAP) {
                key = key.replace(strMap[1], strMap[0]);
            }

            return key;
        }

        public static String escapeIndexQueryValue(Collection<String> values) {
            return escapeIndexQueryValue(values, false);
        }

        public static String escapeIndexQueryValue(Collection<String> values, boolean allowWildcard) {
            StringBuilder sb = new StringBuilder();

            sb.append(BRACE_OPEN_CHAR);

            if (CollectionUtils.isNotEmpty(values)) {
                Iterator<String> iter = values.iterator();

                sb.append(escapeIndexQueryValue(iter.next(), allowWildcard));

                while (iter.hasNext()) {
                    sb.append(SPACE_CHAR).append(escapeIndexQueryValue(iter.next(), allowWildcard));
                }
            }

            sb.append(BRACE_CLOSE_CHAR);

            return sb.toString();
        }

        public static String escapeIndexQueryValue(String value) {
            return escapeIndexQueryValue(value, false, true);
        }

        public static String escapeIndexQueryValue(String value, boolean allowWildcard) {
            return escapeIndexQueryValue(value, allowWildcard, true);
        }

        public static String escapeIndexQueryValue(String value, boolean allowWildcard, boolean shouldQuote) {
            String  ret        = value;
            boolean quoteValue = false;

            if (hasIndexQueryEscapeChar(value)) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < value.length(); i++) {
                    char c = value.charAt(i);

                    if (!(allowWildcard && c == '*') && isIndexQueryEscapeChar(c)) {
                        sb.append('\\');
                    }

                    if (shouldQuote && !quoteValue) {
                        quoteValue = shouldQuoteIndexQueryForChar(c);
                    }

                    sb.append(c);
                }

                ret = sb.toString();
            } else if (value != null) {
                for (int i = 0; i < value.length(); i++) {
                    if (shouldQuote && shouldQuoteIndexQueryForChar(value.charAt(i))) {
                        quoteValue = true;

                        break;
                    }
                }
            }

            if (quoteValue) {
                boolean isQuoteAtStart = ret.charAt(0) == DOUBLE_QUOTE_CHAR;
                boolean isQuoteAtEnd   = ret.charAt(ret.length() - 1) == DOUBLE_QUOTE_CHAR;

                if (!isQuoteAtStart) {
                    if (!isQuoteAtEnd) {
                        ret = DOUBLE_QUOTE_CHAR + ret + DOUBLE_QUOTE_CHAR;
                    } else {
                        ret = DOUBLE_QUOTE_CHAR + ret;
                    }
                } else if (!isQuoteAtEnd) {
                    ret = ret + DOUBLE_QUOTE_CHAR;
                }

            }

            return ret;
        }

        private static boolean hasIndexQueryEscapeChar(String value) {
            if (value != null) {
                for (int i = 0; i < value.length(); i++) {
                    if (isIndexQueryEscapeChar(value.charAt(i))) {
                        return true;
                    }
                }
            }

            return false;
        }

        private static boolean isIndexQueryEscapeChar(char c) {
            switch (c) {
                case '+':
                case '-':
                case '&':
                case '|':
                case '!':
                case '(':
                case ')':
                case '{':
                case '}':
                case '[':
                case ']':
                case '^':
                case '"':
                case '~':
                case '*':
                case '?':
                case ':':
                case '\\':
                case '/':
                case ' ':
                    return true;
            }

            return false;
        }

        public static boolean hastokenizeChar(String value) {
            if (value != null) {
                for (int i = 0; i < value.length(); i++) {
                    if (hastokenizeChar(value, i)) {
                        return true;
                    }
                }
            }

            return false;
        }


        private static boolean hastokenizeChar(String value, int i) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                switch (c) {
                    case '_':
                        return false;
                    case '.':
                    case ':':
                    case '\'':
                        if (i > 0 && !Character.isAlphabetic(value.charAt(i - 1))) {
                            return true;
                        }
                        if (i < value.length() - 1 && !Character.isAlphabetic(value.charAt(i + 1))) {
                            return true;
                        }
                        return false;
                }

                return true;
            }

            return false;
        }

        private static boolean shouldQuoteIndexQueryForChar(char c) {
            switch (c) {
                case '@':
                case ' ':
                case '+':
                case '-':
                case '&':
                case '|':
                case '!':
                case '(':
                case ')':
                case '{':
                case '}':
                case '[':
                case ']':
                case '^':
                case '"':
                case '~':
                case '?':
                case ':':
                case '\\':
                case '/':
                    return true;
            }

            return false;
        }

        private String getRelationshipEdgeLabel(String relationshipLabel) {
            return (relationshipLabel == null) ? getEdgeLabel(qualifiedName) : relationshipLabel;
        }

        public EntityType getReferencedEntityType(TypeRegistry typeRegistry) throws BaseException {
            Type type = typeRegistry.getType(attributeDef.getTypeName());
            return io.simforce.bytezard.metadata.type.StructType.getReferencedEntityType(type);
        }

        public static String getQualifiedAttributeName(StructDef structDef, String attrName) {
            if (isRootType(structDef)) {
                return attrName;
            } else {
                return attrName.contains(".") ? attrName : String.format("%s.%s", structDef.getName(), attrName);
            }
        }

        public static String generateVertexPropertyName(StructDef structDef, AttributeDef attrDef, String qualifiedName) {
            String vertexPropertyName = qualifiedName;
            String attrName           = attrDef.getName();
            if (isRootType(structDef)) {
                return attrName;
            } else {
                if(!attrDef.getName().contains(".") &&
                    AttributeDef.IndexType.STRING.equals(attrDef.getIndexType()) &&
                    TYPE_STRING.equalsIgnoreCase(attrDef.getTypeName())) {
                    vertexPropertyName = String.format("%s.%s%s", structDef.getName(), VERTEX_PROPERTY_PREFIX_STRING_INDEX_TYPE, attrDef.getName());
                }
            }
            return encodePropertyKey(vertexPropertyName);
        }

        private static boolean isRootType(StructDef structDef) {
            return StringUtils.equals(structDef.getName(), EntityType.ENTITY_ROOT.getTypeName()) ||
                   StringUtils.equals(structDef.getName(), ClassificationType.CLASSIFICATION_ROOT.getTypeName());
        }

        // Keys copied from org.janusgraph.graphdb.types.system.SystemTypeManager.RESERVED_CHARS
        // JanusGraph checks that these chars are not part of any keys hence encoding
        // also including Titan reserved characters to support migrated property keys
        private static final String[][] RESERVED_CHAR_ENCODE_MAP = new String[][]{
                new String[] {"{", "_o"},
                new String[] {"}", "_c"},
                new String[] {"\"", "_q"},
                new String[] {"$", "_d"}, //titan reserved character
                new String[] {"%", "_p"}, //titan reserved characters
        };

        private static final char   BRACE_OPEN_CHAR         = '(';
        private static final char   BRACE_CLOSE_CHAR        = ')';
        private static final char   DOUBLE_QUOTE_CHAR       = '"';
        private static final char   SPACE_CHAR              = ' ';

        public enum RelationshipEdgeDirection { IN, OUT, BOTH }
    }
}
