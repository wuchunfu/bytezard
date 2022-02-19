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

import static io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef.CONSTRAINT_TYPE_OWNED_REF;
import static io.simforce.bytezard.metadata.type.StructType.Attribute.RelationshipEdgeDirection;
import static io.simforce.bytezard.metadata.type.StructType.Attribute.RelationshipEdgeDirection.BOTH;
import static io.simforce.bytezard.metadata.type.StructType.Attribute.RelationshipEdgeDirection.IN;
import static io.simforce.bytezard.metadata.type.StructType.Attribute.RelationshipEdgeDirection.OUT;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.ObjectId;
import io.simforce.bytezard.metadata.model.instance.Relationship;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.RelationshipCategory;
import io.simforce.bytezard.metadata.model.typedef.RelationshipEndDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef.Cardinality;
import io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class that implements behaviour of an relationship-type.
 */
public class RelationshipType extends StructType {
    private static final Logger LOG = LoggerFactory.getLogger(RelationshipType.class);

    private final RelationshipDef relationshipDef;
    private final boolean hasLegacyAttributeEnd;
    private String relationshipLabel;
    private EntityType end1Type;
    private EntityType end2Type;

    public RelationshipType(RelationshipDef relationshipDef) {
        super(relationshipDef);

        RelationshipEndDef end1Def = relationshipDef != null ? relationshipDef.getEndDef1() : null;
        RelationshipEndDef end2Def = relationshipDef != null ? relationshipDef.getEndDef2() : null;

        this.relationshipDef = relationshipDef;
        this.hasLegacyAttributeEnd = (end1Def != null && end1Def.getIsLegacyAttribute()) || (end2Def != null && end2Def.getIsLegacyAttribute());
    }

    public RelationshipType(RelationshipDef relationshipDef, TypeRegistry typeRegistry) throws BaseException {
        this(relationshipDef);

        resolveReferences(typeRegistry);
    }

    public RelationshipDef getRelationshipDef() { return relationshipDef; }

    public boolean hasLegacyAttributeEnd() {
        return this.hasLegacyAttributeEnd;
    }

    public String getRelationshipLabel() {
        return this.relationshipLabel;
    }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferences(typeRegistry);

        if (relationshipDef == null) {
            throw new BaseException(ErrorCode.INVALID_VALUE, "relationshipDef is null");
        }

        String end1TypeName = relationshipDef.getEndDef1() != null ? relationshipDef.getEndDef1().getType() : null;
        String end2TypeName = relationshipDef.getEndDef2() != null ? relationshipDef.getEndDef2().getType() : null;

        if (StringUtils.isEmpty(end1TypeName)) {
            throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef1", "type");
        } else {
            Type type1 = typeRegistry.getType(end1TypeName);

            if (type1 instanceof EntityType) {
                end1Type = (EntityType) type1;
            } else {
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_INVALID_END_TYPE, getTypeName(), end1TypeName);
            }
        }

        if (StringUtils.isEmpty(end2TypeName)) {
            throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef2", "type");
        } else {
           Type type2 = typeRegistry.getType(end2TypeName);

            if (type2 instanceof EntityType) {
                end2Type = (EntityType) type2;
            } else {
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_INVALID_END_TYPE, getTypeName(), end2TypeName);
            }
        }

        if (StringUtils.isEmpty(relationshipDef.getEndDef1().getName())) {
            throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef1", "name");
        }

        if (StringUtils.isEmpty(relationshipDef.getEndDef2().getName())) {
            throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef2", "name");
        }

        validateRelationshipDef(relationshipDef);
    }

    @Override
    void resolveReferencesPhase2(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferencesPhase2(typeRegistry);

        RelationshipEndDef endDef1 = relationshipDef.getEndDef1();
        RelationshipEndDef endDef2 = relationshipDef.getEndDef2();
        String relationshipLabel = relationshipDef.getRelationshipLabel();

        if (relationshipLabel == null) {
            // if legacyLabel is not specified at both ends, use relationshipDef name as relationship label.
            // if legacyLabel is specified in any one end, use it as the relationship label for both ends (legacy case).
            // if legacyLabel is specified at both ends use the respective end's legacyLabel as relationship label (legacy case).
            if (!endDef1.getIsLegacyAttribute() && !endDef2.getIsLegacyAttribute()) {
                relationshipLabel = "r:" + getTypeName();
            } else if (endDef1.getIsLegacyAttribute() && !endDef2.getIsLegacyAttribute()) {
                relationshipLabel = getLegacyEdgeLabel(end1Type, endDef1.getName());
            } else if (!endDef1.getIsLegacyAttribute() && endDef2.getIsLegacyAttribute()) {
                relationshipLabel = getLegacyEdgeLabel(end2Type, endDef2.getName());
            }
        }

        this.relationshipLabel = relationshipLabel;

        addRelationshipAttributeToEndType(endDef1, end1Type, end2Type.getTypeName(), typeRegistry, relationshipLabel);

        addRelationshipAttributeToEndType(endDef2, end2Type, end1Type.getTypeName(), typeRegistry, relationshipLabel);

        // add relationship edge direction information
        addRelationshipEdgeDirection();
    }

    private void addRelationshipEdgeDirection() {
        RelationshipEndDef endDef1 = relationshipDef.getEndDef1();
        RelationshipEndDef endDef2 = relationshipDef.getEndDef2();

        if (StringUtils.equals(endDef1.getType(), endDef2.getType()) &&
                StringUtils.equals(endDef1.getName(), endDef2.getName())) {

            Attribute endAttribute = end1Type.getRelationshipAttribute(endDef1.getName(), relationshipDef.getName());

            endAttribute.setRelationshipEdgeDirection(BOTH);
        } else {
            Attribute end1Attribute = end1Type.getRelationshipAttribute(endDef1.getName(), relationshipDef.getName());
            Attribute end2Attribute = end2Type.getRelationshipAttribute(endDef2.getName(), relationshipDef.getName());

            //default relationship edge direction is end1 (out) -> end2 (in)
            RelationshipEdgeDirection end1Direction = OUT;
            RelationshipEdgeDirection end2Direction = IN;

            if (endDef1.getIsLegacyAttribute() && endDef2.getIsLegacyAttribute()) {
                if (relationshipDef.getRelationshipLabel() == null) { // only if label hasn't been overridden
                    end2Direction = OUT;
                }
            } else if (!endDef1.getIsLegacyAttribute() && endDef2.getIsLegacyAttribute()) {
                end1Direction = IN;
                end2Direction = OUT;
            }

            end1Attribute.setRelationshipEdgeDirection(end1Direction);
            end2Attribute.setRelationshipEdgeDirection(end2Direction);
        }
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            if (obj instanceof Relationship) {
                return validateRelationship((Relationship) obj);
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        final boolean ret;

        if (val1 == null) {
            ret = val2 == null;
        } else if (val2 == null) {
            ret = false;
        } else {
            Relationship rel1 = getRelationshipFromValue(val1);

            if (rel1 == null) {
                ret = false;
            } else {
                Relationship rel2 = getRelationshipFromValue(val2);

                if (rel2 == null) {
                    ret = false;
                } else if (!super.areEqualValues(rel1, rel2, guidAssignments)) {
                    ret = false;
                } else {
                    ret = Objects.equals(rel1.getGuid(), rel2.getGuid()) &&
                          Objects.equals(rel1.getEnd1(), rel2.getEnd1()) &&
                          Objects.equals(rel1.getEnd2(), rel2.getEnd2()) &&
                          Objects.equals(rel1.getLabel(), rel2.getLabel()) &&
                          Objects.equals(rel1.getPropagateTags(), rel2.getPropagateTags()) &&
                          Objects.equals(rel1.getStatus(), rel2.getStatus());
                }
            }
        }

        return ret;
    }

    @Override
    public boolean isValidValueForUpdate(Object obj) {
        if (obj != null) {
            if (obj instanceof Relationship) {
                return validateRelationship((Relationship) obj);
            } else {
                return false;
            }
        }

        return true;
    }

    public io.simforce.bytezard.metadata.type.EntityType getEnd1Type() { return end1Type; }

    public io.simforce.bytezard.metadata.type.EntityType getEnd2Type() { return end2Type; }

    /**
     * Validate the fields in the the RelationshipType are consistent with respect to themselves.
     * @param relationship
     * @throws BaseException
     */
    private boolean validateRelationship(Relationship relationship) {

        ObjectId end1 = relationship.getEnd1();
        ObjectId end2 = relationship.getEnd2();

        if (end1 != null && end2 != null) {

            String end1TypeName = end1.getTypeName();
            String end2TypeName = end2.getTypeName();

            if (StringUtils.isNotEmpty(end1TypeName) && StringUtils.isNotEmpty(end2TypeName)) {

                return end1Type.isTypeOrSuperTypeOf(end1TypeName) && end2Type.isTypeOrSuperTypeOf(end2TypeName) && super.isValidValue(relationship);

            } else {

                return StringUtils.isNotEmpty(end1.getGuid()) && StringUtils.isNotEmpty(end2.getGuid());

            }

        }

        return false;

    }

    /**
     * Throw an exception so we can junit easily.
     *
     * This method assumes that the 2 ends are not null.
     *
     * @param relationshipDef
     * @throws BaseException
     */
    public static void validateRelationshipDef(RelationshipDef relationshipDef) throws BaseException {

        RelationshipEndDef endDef1              = relationshipDef.getEndDef1();
        RelationshipEndDef endDef2              = relationshipDef.getEndDef2();
        RelationshipCategory    relationshipCategory = relationshipDef.getRelationshipCategory();
        String                  name                 = relationshipDef.getName();
        boolean                 isContainer1         = endDef1.getIsContainer();
        boolean                 isContainer2         = endDef2.getIsContainer();

        if ((endDef1.getCardinality() == AttributeDef.Cardinality.LIST) ||
                (endDef2.getCardinality() == AttributeDef.Cardinality.LIST)) {
            throw new BaseException(ErrorCode.RELATIONSHIPDEF_LIST_ON_END, name);
        }
        if (isContainer1 && isContainer2) {
            // we support 0 or 1 of these flags.
            throw new BaseException(ErrorCode.RELATIONSHIPDEF_DOUBLE_CONTAINERS, name);
        }
        if ((isContainer1 || isContainer2)) {
            // we have an isContainer defined in an end
            if (relationshipCategory == RelationshipCategory.ASSOCIATION) {
                // associations are not containment relationships - so do not allow an endpoint with isContainer
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_ASSOCIATION_AND_CONTAINER, name);
            }
        } else {
            // we do not have an isContainer defined on an end
            if (relationshipCategory == RelationshipCategory.COMPOSITION) {
                // COMPOSITION needs one end to be the container.
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_COMPOSITION_NO_CONTAINER, name);
            } else if (relationshipCategory == RelationshipCategory.AGGREGATION) {
                // AGGREGATION needs one end to be the container.
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_AGGREGATION_NO_CONTAINER, name);
            }
        }
        if (relationshipCategory == RelationshipCategory.COMPOSITION) {
            // composition children should not be multiple cardinality
            if (endDef1.getCardinality() == AttributeDef.Cardinality.SET &&
                    !endDef1.getIsContainer()) {
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_COMPOSITION_MULTIPLE_PARENTS, name);
            }
            if ((endDef2.getCardinality() == AttributeDef.Cardinality.SET) &&
                    !endDef2.getIsContainer()) {
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_COMPOSITION_MULTIPLE_PARENTS, name);
            }
        }
    }

    private void addRelationshipAttributeToEndType(RelationshipEndDef endDef, io.simforce.bytezard.metadata.type.EntityType entityType, String attrTypeName,
                                                   TypeRegistry typeRegistry, String relationshipLabel) throws BaseException {

        String attrName = (endDef != null) ? endDef.getName() : null;

        if (StringUtils.isEmpty(attrName)) {
            return;
        }

        Attribute attribute = entityType.getAttribute(attrName);

        // if relationshipLabel is null, then legacyLabel is mentioned at both ends,
        // use the respective end's legacyLabel as relationshipLabel
        if (relationshipLabel == null) {
            relationshipLabel = getLegacyEdgeLabel(entityType, attrName);
        }

        //attr doesn't exist in type - is a new relationship attribute
        if (attribute == null) {
            Cardinality cardinality = endDef.getCardinality();
            boolean isOptional  = true;
            ConstraintDef constraint  = null;

            if (cardinality == Cardinality.SET) {
                attrTypeName = BaseTypeDef.getArrayTypeName(attrTypeName);
            }

            if (relationshipDef.getRelationshipCategory() == RelationshipCategory.COMPOSITION) {
                if (endDef.getIsContainer()) {
                    constraint = new ConstraintDef(CONSTRAINT_TYPE_OWNED_REF);
                } else {
                    isOptional = false;
                }
            }

            AttributeDef attributeDef = new AttributeDef(attrName, attrTypeName, isOptional, cardinality);

            if (constraint != null) {
                attributeDef.addConstraint(constraint);
            }

            io.simforce.bytezard.metadata.type.Type attrType = typeRegistry.getType(attrTypeName);

            if (attrType instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) attrType;

                arrayType.setCardinality(attributeDef.getCardinality());
            }

            attribute = new Attribute(entityType, attributeDef, attrType, getTypeName(), relationshipLabel);

            attribute.setLegacyAttribute(endDef.getIsLegacyAttribute());
        } else {
            // attribute already exists (legacy attribute which is also a relationship attribute)
            // add relationshipLabel information to existing attribute
            attribute.setRelationshipName(getTypeName());
            attribute.setRelationshipEdgeLabel(relationshipLabel);
            attribute.setLegacyAttribute(true);
        }

        entityType.addRelationshipAttribute(attrName, attribute, this);
    }

    private String getLegacyEdgeLabel(EntityType entityType, String attributeName) {
        String ret = null;
        Attribute attribute = entityType.getAttribute(attributeName);

        if (attribute != null) {
            ret = attribute.getQualifiedName();
        }

        return ret;
    }

    private Relationship getRelationshipFromValue(Object val) {
        final Relationship ret;

        if (val instanceof Relationship) {
            ret = (Relationship) val;
        } else if (val instanceof Map) {
            ret = new Relationship((Map) val);
        } else {
            ret = null;
        }

        return ret;
    }
}