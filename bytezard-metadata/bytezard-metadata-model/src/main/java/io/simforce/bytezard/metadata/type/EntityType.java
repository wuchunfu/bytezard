/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.metadata.type;

import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_DATE;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_INT;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_STRING;
import static io.simforce.bytezard.metadata.type.Constants.CLASSIFICATION_NAMES_KEY;
import static io.simforce.bytezard.metadata.type.Constants.CLASSIFICATION_TEXT_KEY;
import static io.simforce.bytezard.metadata.type.Constants.CREATED_BY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.CUSTOM_ATTRIBUTES_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.GUID_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.HISTORICAL_GUID_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.IS_INCOMPLETE_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.LABELS_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.MODIFIED_BY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.PENDING_TASKS_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.PROPAGATED_CLASSIFICATION_NAMES_KEY;
import static io.simforce.bytezard.metadata.type.Constants.STATE_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.TIMESTAMP_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.TYPE_NAME_PROPERTY_KEY;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.Entity;
import io.simforce.bytezard.metadata.model.instance.ObjectId;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef.RelationshipAttributeDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.PropagateTags;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.type.BuiltInTypes.ObjectIdType;
import io.simforce.bytezard.metadata.type.BusinessMetadataType.BusinessAttribute;
import io.simforce.bytezard.metadata.utils.EntityUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;


/**
 * class that implements behaviour of an entity-type.
 */
public class EntityType extends io.simforce.bytezard.metadata.type.StructType {
    
    private static final Logger LOG = LoggerFactory.getLogger(EntityType.class);

    public  static final EntityType ENTITY_ROOT = new RootEntityType();

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String OWNER = "owner";
    private static final String CREATE_TIME = "createTime";
    private static final String DYN_ATTRIBUTE_PREFIX = "dynAttribute:";
    private static final String OPTION_SCHEMA_ATTRIBUTES = "schemaAttributes";
    private static final String INTERNAL_TYPE_NAME = "internal";

    public static final String OPTION_APPEND_RELATIONSHIPS_ON_PARTIAL_UPDATE = "appendRelationshipsOnPartialUpdate";

    private static final char NS_ATTRIBUTE_NAME_SEPARATOR  = '.';
    private static final char DYN_ATTRIBUTE_NAME_SEPARATOR = '.';
    private static final char DYN_ATTRIBUTE_OPEN_DELIM = '{';
    private static final char DYN_ATTRIBUTE_CLOSE_DELIM = '}';

    private static final String[] ENTITY_HEADER_ATTRIBUTES = new String[]{NAME, DESCRIPTION, OWNER, CREATE_TIME};
    private static final String ENTITY_ROOT_NAME  = "ENTITY_ROOT";

    private final EntityDef entityDef;
    private final String typeQryStr;

    private List<EntityType> superTypes = Collections.emptyList();
    private Set<String> allSuperTypes = Collections.emptySet();
    private Set<String> subTypes = Collections.emptySet();
    private Set<String> allSubTypes = Collections.emptySet();
    private Set<String> typeAndAllSubTypes = Collections.emptySet();
    private Set<String> typeAndAllSuperTypes = Collections.emptySet();
    private Map<String, Map<String, Attribute>> relationshipAttributes = Collections.emptyMap();
    private Map<String, Map<String, BusinessAttribute>> businessAttributes = Collections.emptyMap();
    private List<Attribute> ownedRefAttributes = Collections.emptyList();
    private String displayTextAttribute = null;
    private String typeAndAllSubTypesQryStr   = "";
    private boolean isInternalType = false;
    private Map<String, Attribute> headerAttributes = Collections.emptyMap();
    private Map<String, Attribute> minInfoAttributes = Collections.emptyMap();
    private List<Attribute> dynAttributes = Collections.emptyList();
    private List<Attribute> dynEvalTriggerAttributes = Collections.emptyList();
    private Map<String,List<TemplateToken>> parsedTemplates = Collections.emptyMap();
    private Set<String> tagPropagationEdges = Collections.emptySet();

    public EntityType(EntityDef entityDef) {
        super(entityDef);

        this.entityDef = entityDef;
        this.typeQryStr = Attribute.escapeIndexQueryValue(Collections.singleton(getTypeName()), true);
        this.displayTextAttribute = entityDef.getOption(EntityDef.OPTION_DISPLAY_TEXT_ATTRIBUTE);
    }

    public EntityType(EntityDef entityDef, TypeRegistry typeRegistry) throws BaseException {
        super(entityDef);

        this.entityDef = entityDef;
        this.typeQryStr = Attribute.escapeIndexQueryValue(Collections.singleton(getTypeName()), true);
        this.displayTextAttribute = entityDef.getOption(EntityDef.OPTION_DISPLAY_TEXT_ATTRIBUTE);

        resolveReferences(typeRegistry);
    }

    public EntityDef getEntityDef() {
        return entityDef;
    }

    public static EntityType getEntityRoot() {return ENTITY_ROOT; }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferences(typeRegistry);

        List<EntityType> entityTypes = new ArrayList<>();
        Set<String> allS = new HashSet<>();
        Map<String, Attribute> allA = new HashMap<>();

        getTypeHierarchyInfo(typeRegistry, allS, allA);

        for (String superTypeName : entityDef.getSuperTypes()) {
            Type superType = typeRegistry.getType(superTypeName);

            if (superType instanceof EntityType) {
                entityTypes.add((EntityType) superType);
            } else {
                throw new BaseException(ErrorCode.INCOMPATIBLE_SUPERTYPE, superTypeName, entityDef.getName());
            }
        }

        this.superTypes = Collections.unmodifiableList(entityTypes);
        this.allSuperTypes = Collections.unmodifiableSet(allS);
        this.allAttributes = Collections.unmodifiableMap(allA);
        this.uniqAttributes = getUniqueAttributes(this.allAttributes);
        this.subTypes = new HashSet<>(); // this will be populated in resolveReferencesPhase2()
        this.allSubTypes = new HashSet<>(); // this will be populated in resolveReferencesPhase2()
        this.typeAndAllSubTypes = new HashSet<>(); // this will be populated in resolveReferencesPhase2()
        this.relationshipAttributes = new HashMap<>(); // this will be populated in resolveReferencesPhase3()
        this.businessAttributes = new HashMap<>(); // this will be populated in resolveReferences(), from BusinessMetadataType
        this.tagPropagationEdges = new HashSet<>(); // this will be populated in resolveReferencesPhase2()

        this.typeAndAllSubTypes.add(this.getTypeName());

        this.typeAndAllSuperTypes = new HashSet<>(this.allSuperTypes);
        this.typeAndAllSuperTypes.add(this.getTypeName());
        this.typeAndAllSuperTypes = Collections.unmodifiableSet(this.typeAndAllSuperTypes);

        // headerAttributes includes uniqAttributes & ENTITY_HEADER_ATTRIBUTES
        this.headerAttributes = new HashMap<>(this.uniqAttributes);

        for (String headerAttributeName : ENTITY_HEADER_ATTRIBUTES) {
            Attribute headerAttribute = getAttribute(headerAttributeName);

            if (headerAttribute != null) {
                this.headerAttributes.put(headerAttributeName, headerAttribute);
            }
        }

        // minInfoAttributes includes all headerAttributes & schema-attributes
        this.minInfoAttributes = new HashMap<>(this.headerAttributes);

        Map<String, String> typeDefOptions = entityDef.getOptions();
        String jsonList = typeDefOptions != null ? typeDefOptions.get(OPTION_SCHEMA_ATTRIBUTES) : null;
        List<String> schemaAttributeNames = StringUtils.isNotEmpty(jsonList) ? Type.fromJson(jsonList, List.class) : null;

        if (CollectionUtils.isNotEmpty(schemaAttributeNames)) {
            for (String schemaAttributeName : schemaAttributeNames) {
                Attribute schemaAttribute = getAttribute(schemaAttributeName);

                if (schemaAttribute != null) {
                    this.minInfoAttributes.put(schemaAttributeName, schemaAttribute);
                }
            }
        }

        if (this.displayTextAttribute != null) {
            if (getAttribute(this.displayTextAttribute) == null) {
                LOG.warn("{}: ignoring option {}, as attribute {} does not exist", getTypeName(), EntityDef.OPTION_DISPLAY_TEXT_ATTRIBUTE, this.displayTextAttribute);

                this.displayTextAttribute = null;
            }
        }

        if (this.displayTextAttribute == null) { // find displayTextAttribute in direct superTypes
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                // read from superType's entityDef; not from superType.getDisplayTextAttribute(), as that might have been resolved to its superType
                this.displayTextAttribute = superType.getEntityDef().getOption(EntityDef.OPTION_DISPLAY_TEXT_ATTRIBUTE);

                if (this.displayTextAttribute != null) {
                    if (getAttribute(this.displayTextAttribute) == null) { // if displayTextAttribute in superType is invalid, ignore
                        this.displayTextAttribute = null;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    @Override
    void resolveReferencesPhase2(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferencesPhase2(typeRegistry);
        ensureNoAttributeOverride(superTypes);

        for (EntityType superType : superTypes) {
            superType.addSubType(this);
        }

        for (String superTypeName : allSuperTypes) {
            EntityType superType = typeRegistry.getEntityTypeByName(superTypeName);
            superType.addToAllSubTypes(this);
        }
    }

    @Override
    void resolveReferencesPhase3(TypeRegistry typeRegistry) throws BaseException {
        for (AttributeDef attributeDef : getStructDef().getAttributeDefs()) {
            String attributeName = attributeDef.getName();
            Type attributeType = typeRegistry.getType(attributeDef.getTypeName());
            EntityType attributeEntityType = getReferencedEntityType(attributeType);

            // validate if RelationshipDefs is defined for all entityDefs
            if (attributeEntityType != null && !hasRelationshipAttribute(attributeName)) {
                typeRegistry.reportMissingRelationshipDef(getTypeName(), attributeEntityType.getTypeName(), attributeName);
            }
        }

        for (String superTypeName : allSuperTypes) {
            if (INTERNAL_TYPE_NAME.equals(superTypeName)) {
                isInternalType = true;
            }

            EntityType superType = typeRegistry.getEntityTypeByName(superTypeName);
            Map<String, Map<String, Attribute>> superTypeRelationshipAttributes = superType.getRelationshipAttributes();

            if (MapUtils.isNotEmpty(superTypeRelationshipAttributes)) {
                for (String attrName : superTypeRelationshipAttributes.keySet()) {
                    Map<String, Attribute> superTypeAttributes = superTypeRelationshipAttributes.get(attrName);

                    if (MapUtils.isNotEmpty(superTypeAttributes)) {
                        Map<String, Attribute> attributes = relationshipAttributes.computeIfAbsent(attrName, k -> new HashMap<>());

                        for (String relationshipType : superTypeAttributes.keySet()) {
                            if (!attributes.containsKey(relationshipType)) {
                                attributes.put(relationshipType, superTypeAttributes.get(relationshipType));
                            }
                        }
                    }
                }
            }

            Map<String, Map<String, BusinessAttribute>> superTypeBusinessMetadata = superType.getBusinessAttributes();

            if (MapUtils.isNotEmpty(superTypeBusinessMetadata)) {
                for (Map.Entry<String, Map<String, BusinessAttribute>> entry : superTypeBusinessMetadata.entrySet()) {
                    String bmName = entry.getKey();
                    Map<String, BusinessAttribute> superTypeBmAttrs = entry.getValue();
                    Map<String, BusinessAttribute> bmAttrs = businessAttributes.computeIfAbsent(bmName, k -> new HashMap<>());

                    for (Map.Entry<String, BusinessAttribute> bmAttrEntry : superTypeBmAttrs.entrySet()) {
                        bmAttrs.put(bmAttrEntry.getKey(), bmAttrEntry.getValue());
                    }
                }
            }

            tagPropagationEdges.addAll(superType.tagPropagationEdges);
        }

        ownedRefAttributes = new ArrayList<>();

        for (Attribute attribute : allAttributes.values()) {
            if (attribute.isOwnedRef()) {
                ownedRefAttributes.add(attribute);
            }
        }

        for (Map<String, Attribute> attributes : relationshipAttributes.values()) {
            for (Attribute attribute : attributes.values()) {
                if (attribute.isOwnedRef()) {
                    ownedRefAttributes.add(attribute);
                }
            }
        }

        subTypes = Collections.unmodifiableSet(subTypes);
        allSubTypes = Collections.unmodifiableSet(allSubTypes);
        typeAndAllSubTypes = Collections.unmodifiableSet(typeAndAllSubTypes);
        typeAndAllSubTypesQryStr = ""; // will be computed on next access
        relationshipAttributes = Collections.unmodifiableMap(relationshipAttributes);
        businessAttributes = Collections.unmodifiableMap(businessAttributes);
        ownedRefAttributes = Collections.unmodifiableList(ownedRefAttributes);
        tagPropagationEdges = Collections.unmodifiableSet(tagPropagationEdges);

        entityDef.setSubTypes(subTypes);

        List<RelationshipAttributeDef> relationshipAttrDefs = new ArrayList<>();

        for (Map.Entry<String, Map<String, Attribute>> attrEntry : relationshipAttributes.entrySet()) {
            Map<String, Attribute> relations = attrEntry.getValue();

            for (Map.Entry<String, Attribute> relationsEntry : relations.entrySet()) {
                String relationshipType = relationsEntry.getKey();
                Attribute relationshipAttr = relationsEntry.getValue();

                RelationshipAttributeDef relationshipAttributeDef =
                        new RelationshipAttributeDef(relationshipType, relationshipAttr.isLegacyAttribute(), relationshipAttr.getAttributeDef());

                updateRelationshipAttrDefForPartialUpdate(relationshipAttributeDef, entityDef);

                relationshipAttrDefs.add(relationshipAttributeDef);
            }
        }

        entityDef.setRelationshipAttributeDefs(Collections.unmodifiableList(relationshipAttrDefs));

        Map<String, List<AttributeDef>> bmAttributeDefs = new HashMap<>();

        for (Map.Entry<String, Map<String, BusinessAttribute>> entry : businessAttributes.entrySet()) {
            String bmName = entry.getKey();
            Map<String, BusinessAttribute> bmAttrs = entry.getValue();
            List<AttributeDef> bmAttrDefs = new ArrayList<>();

            for (BusinessAttribute bmAttr : bmAttrs.values()) {
                bmAttrDefs.add(bmAttr.getAttributeDef());
            }

            bmAttributeDefs.put(bmName, bmAttrDefs);
        }

        entityDef.setBusinessAttributeDefs(bmAttributeDefs);

        if (this.displayTextAttribute == null) {
            for (String superTypeName : allSuperTypes) { // find displayTextAttribute in all superTypes
                EntityType superType = typeRegistry.getEntityTypeByName(superTypeName);

                this.displayTextAttribute = superType.getDisplayTextAttribute();

                if (this.displayTextAttribute != null) {
                    break;
                }
            }
        }

        this.parsedTemplates = parseDynAttributeTemplates();

        populateDynFlagsInfo();

        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveReferencesPhase3({}): tagPropagationEdges={}", getTypeName(), tagPropagationEdges);
        }
    }

    private void updateRelationshipAttrDefForPartialUpdate(RelationshipAttributeDef relationshipAttributeDef, EntityDef entityDef) {
        String appendRelationshipsOnPartialUpdate = entityDef.getOption(OPTION_APPEND_RELATIONSHIPS_ON_PARTIAL_UPDATE);
        String relationshipAttributeName = relationshipAttributeDef.getName();

        if (StringUtils.isNotEmpty(appendRelationshipsOnPartialUpdate)) {
            Set<String> relationshipTypesToAppend = Type.fromJson(appendRelationshipsOnPartialUpdate, Set.class);

            if (CollectionUtils.isNotEmpty(relationshipTypesToAppend) && relationshipTypesToAppend.contains(relationshipAttributeName)) {
                relationshipAttributeDef.setOption(AttributeDef.ATTRDEF_OPTION_APPEND_ON_PARTIAL_UPDATE, Boolean.toString(true));
            }
        }
    }

    @Override
    public Attribute getSystemAttribute(String attributeName) {
        return EntityType.ENTITY_ROOT.allAttributes.get(attributeName);
    }

    @Override
    public BusinessAttribute getBusinesAAttribute(String bmAttrQualifiedName) {
        BusinessAttribute ret = null;

        if (bmAttrQualifiedName != null) {
            int idxSep = bmAttrQualifiedName.indexOf(io.simforce.bytezard.metadata.type.EntityType.NS_ATTRIBUTE_NAME_SEPARATOR);

            if (idxSep != -1) {
                String bmName     = bmAttrQualifiedName.substring(0, idxSep);
                String bmAttrName = bmAttrQualifiedName.substring(idxSep + 1);

                ret = getBusinessAttribute(bmName, bmAttrName);
            }
        }

        return ret;
    }

    public Set<String> getSuperTypes() {
        return entityDef.getSuperTypes();
    }

    public Set<String> getAllSuperTypes() {
        return allSuperTypes;
    }

    public Set<String> getSubTypes() {
        return subTypes;
    }

    public Set<String> getAllSubTypes() {
        return allSubTypes;
    }

    public Set<String> getTypeAndAllSubTypes() {
        return typeAndAllSubTypes;
    }

    public Set<String> getTypeAndAllSuperTypes() {
        return typeAndAllSuperTypes;
    }

    public Map<String, Attribute> getHeaderAttributes() { return headerAttributes; }

    public Map<String, Attribute> getMinInfoAttributes() { return minInfoAttributes; }

    public boolean isSuperTypeOf(io.simforce.bytezard.metadata.type.EntityType entityType) {
        return entityType != null && allSubTypes.contains(entityType.getTypeName());
    }

    public boolean isSuperTypeOf(String entityTypeName) {
        return StringUtils.isNotEmpty(entityTypeName) && allSubTypes.contains(entityTypeName);
    }

    public boolean isTypeOrSuperTypeOf(String entityTypeName) {
        return StringUtils.isNotEmpty(entityTypeName) && typeAndAllSubTypes.contains(entityTypeName);
    }

    public boolean isSubTypeOf(io.simforce.bytezard.metadata.type.EntityType entityType) {
        return entityType != null && allSuperTypes.contains(entityType.getTypeName());
    }

    public boolean isSubTypeOf(String entityTypeName) {
        return StringUtils.isNotEmpty(entityTypeName) && allSuperTypes.contains(entityTypeName);
    }

    public boolean isInternalType() {
        return isInternalType;
    }

    public Map<String, Map<String, Attribute>> getRelationshipAttributes() {
        return relationshipAttributes;
    }

    public List<Attribute> getOwnedRefAttributes() {
        return ownedRefAttributes;
    }

    public String getDisplayTextAttribute() {
        return displayTextAttribute;
    }

    public List<Attribute> getDynEvalAttributes() { return dynAttributes; }

    @VisibleForTesting
    public void setDynEvalAttributes(List<Attribute> dynAttributes) { this.dynAttributes = dynAttributes; }

    public List<Attribute> getDynEvalTriggerAttributes() { return dynEvalTriggerAttributes; }

    @VisibleForTesting
    public void setDynEvalTriggerAttributes(List<Attribute> dynEvalTriggerAttributes) { this.dynEvalTriggerAttributes = dynEvalTriggerAttributes; }

    public Set<String> getTagPropagationEdges() {
        return this.tagPropagationEdges;
    }

    public String[] getTagPropagationEdgesArray() {
        return CollectionUtils.isNotEmpty(tagPropagationEdges) ? tagPropagationEdges.toArray(new String[tagPropagationEdges.size()]) : null;
    }

    public Map<String,List<TemplateToken>> getParsedTemplates() { return parsedTemplates; }

    public Attribute getRelationshipAttribute(String attributeName, String relationshipType) {
        final Attribute ret;
        Map<String, Attribute> attributes = relationshipAttributes.get(attributeName);

        if (MapUtils.isNotEmpty(attributes)) {
            if (relationshipType != null && attributes.containsKey(relationshipType)) {
                ret = attributes.get(relationshipType);
            } else {
                ret = attributes.values().iterator().next();
            }
        } else {
            ret = null;
        }

        return ret;
    }

    // this method should be called from RelationshipType.resolveReferencesPhase2()
    void addRelationshipAttribute(String attributeName, Attribute attribute, RelationshipType relationshipType) {
        Map<String, Attribute> attributes = relationshipAttributes.get(attributeName);

        if (attributes == null) {
            attributes = new HashMap<>();

            relationshipAttributes.put(attributeName, attributes);
        }

        attributes.put(relationshipType.getTypeName(), attribute);

        // determine if tags from this entity-type propagate via this relationship
        PropagateTags propagation = relationshipType.getRelationshipDef().getPropagateTags();

        if (propagation == null) {
            propagation = PropagateTags.NONE;
        }

        final boolean propagatesTags;

        switch (propagation) {
            case BOTH:
                propagatesTags = true;
            break;

            case ONE_TO_TWO:
                propagatesTags = StringUtils.equals(relationshipType.getEnd1Type().getTypeName(), getTypeName());
            break;

            case TWO_TO_ONE:
                propagatesTags = StringUtils.equals(relationshipType.getEnd2Type().getTypeName(), getTypeName());
            break;

            case NONE:
            default:
                propagatesTags = false;
            break;
        }

        if (propagatesTags) {
            tagPropagationEdges.add(relationshipType.getRelationshipLabel());
        }
    }

    public Set<String> getAttributeRelationshipTypes(String attributeName) {
        Map<String, Attribute> attributes = relationshipAttributes.get(attributeName);

        return attributes != null ? attributes.keySet() : null;
    }

    public Map<String, Map<String, BusinessAttribute>> getBusinessAttributes() {
        return businessAttributes;
    }

    public Map<String, BusinessAttribute> getBusinessAttributes(String bmName) {
        return businessAttributes.get(bmName);
    }

    public BusinessAttribute getBusinessAttribute(String bmName, String bmAttrName) {
        Map<String, BusinessAttribute> bmAttrs = businessAttributes.get(bmName);
        BusinessAttribute ret     = bmAttrs != null ? bmAttrs.get(bmAttrName) : null;

        return ret;
    }

    public void addBusinessAttribute(BusinessAttribute attribute) {
        String                              bmName  = attribute.getDefinedInType().getTypeName();
        Map<String, BusinessAttribute> bmAttrs = businessAttributes.get(bmName);

        if (bmAttrs == null) {
            bmAttrs = new HashMap<>();

            businessAttributes.put(bmName, bmAttrs);
        }

        bmAttrs.put(attribute.getName(), attribute);
    }

    public String getTypeAndAllSubTypesQryStr() {
        if (StringUtils.isEmpty(typeAndAllSubTypesQryStr)) {
            typeAndAllSubTypesQryStr = Attribute.escapeIndexQueryValue(typeAndAllSubTypes, true);
        }

        return typeAndAllSubTypesQryStr;
    }

    public String getTypeQryStr() {
        return typeQryStr;
    }

    public boolean hasAttribute(String attributeName) {
        return allAttributes.containsKey(attributeName);
    }

    public boolean hasRelationshipAttribute(String attributeName) {
        return relationshipAttributes.containsKey(attributeName);
    }

    @Override
    public String getVertexPropertyName(String attrName) throws BaseException {
        Attribute ret = getAttribute(attrName);

        if (ret == null) {
            ret = relationshipAttributes.get(attrName).values().iterator().next();
        }

        if (ret != null) {
            return ret.getVertexPropertyName();
        }

        throw new BaseException(ErrorCode.UNKNOWN_ATTRIBUTE, attrName, entityDef.getName());
    }

    @Override
    public Entity createDefaultValue() {
        Entity ret = new Entity(entityDef.getName());

        populateDefaultValues(ret);

        return ret;
    }

    @Override
    public Entity createDefaultValue(Object defaultValue) {
        Entity ret = new Entity(entityDef.getName());

        populateDefaultValues(ret);

        return ret;
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                if (!superType.isValidValue(obj)) {
                    return false;
                }
            }

            return super.isValidValue(obj) && validateRelationshipAttributes(obj);
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
            if (!superType.areEqualValues(val1, val2, guidAssignments)) {
                return false;
            }
        }

        return super.areEqualValues(val1, val2, guidAssignments);
    }

    @Override
    public boolean isValidValueForUpdate(Object obj) {
        if (obj != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                if (!superType.isValidValueForUpdate(obj)) {
                    return false;
                }
            }
            return super.isValidValueForUpdate(obj);
        }

        return true;
    }

    @Override
    public Object getNormalizedValue(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValue(obj)) {
                if (obj instanceof Entity) {
                    normalizeAttributeValues((Entity) obj);
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
                if (obj instanceof Entity) {
                    normalizeAttributeValuesForUpdate((Entity) obj);
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
            if (obj instanceof Entity || obj instanceof Map) {
                for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                    ret = superType.validateValue(obj, objName, messages) && ret;
                }

                ret = super.validateValue(obj, objName, messages) && validateRelationshipAttributes(obj, objName, messages) && ret;
            } else {
                ret = false;
                messages.add(objName + ": invalid value type '" + obj.getClass().getName());
            }
        }

        return ret;
    }

    @Override
    public boolean validateValueForUpdate(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            if (obj instanceof Entity || obj instanceof Map) {
                for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                    ret = superType.validateValueForUpdate(obj, objName, messages) && ret;
                }

                ret = super.validateValueForUpdate(obj, objName, messages) && ret;

            } else {
                ret = false;
                messages.add(objName + ": invalid value type '" + obj.getClass().getName());
            }
        }

        return ret;
    }

    @Override
    public Type getTypeForAttribute() {
        Type attributeType = new ObjectIdType(getTypeName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTypeForAttribute(): {} ==> {}", getTypeName(), attributeType.getTypeName());
        }

        return attributeType;
    }

    public void normalizeAttributeValues(Entity ent) {
        if (ent != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                superType.normalizeAttributeValues(ent);
            }

            super.normalizeAttributeValues(ent);

            normalizeRelationshipAttributeValues(ent, false);
        }
    }

    public void normalizeAttributeValuesForUpdate(Entity ent) {
        if (ent != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                superType.normalizeAttributeValuesForUpdate(ent);
            }

            super.normalizeAttributeValuesForUpdate(ent);

            normalizeRelationshipAttributeValues(ent, true);
        }
    }

    @Override
    public void normalizeAttributeValues(Map<String, Object> obj) {
        if (obj != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                superType.normalizeAttributeValues(obj);
            }

            super.normalizeAttributeValues(obj);

            normalizeRelationshipAttributeValues(obj, false);
        }
    }

    @Override
    public void normalizeAttributeValuesForUpdate(Map<String, Object> obj) {
        if (obj != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                superType.normalizeAttributeValuesForUpdate(obj);
            }

            super.normalizeAttributeValuesForUpdate(obj);

            normalizeRelationshipAttributeValues(obj, true);
        }
    }

    public void populateDefaultValues(Entity ent) {
        if (ent != null) {
            for (io.simforce.bytezard.metadata.type.EntityType superType : superTypes) {
                superType.populateDefaultValues(ent);
            }

            super.populateDefaultValues(ent);
        }
    }

    private void addSubType(io.simforce.bytezard.metadata.type.EntityType subType) {
        subTypes.add(subType.getTypeName());
    }

    private void addToAllSubTypes(io.simforce.bytezard.metadata.type.EntityType subType) {
        allSubTypes.add(subType.getTypeName());
        typeAndAllSubTypes.add(subType.getTypeName());
    }

    private void getTypeHierarchyInfo(TypeRegistry typeRegistry,
                                      Set<String> allSuperTypeNames,
                                      Map<String, Attribute> allAttributes) throws BaseException {
        List<String> visitedTypes = new ArrayList<>();
        Map<String, String> attributeToEntityNameMap = new HashMap<>();

        collectTypeHierarchyInfo(typeRegistry, allSuperTypeNames, allAttributes, attributeToEntityNameMap, visitedTypes);
    }

    /*
     * This method should not assume that resolveReferences() has been called on all superTypes.
     * this.entityDef is the only safe member to reference here
     */
    private void collectTypeHierarchyInfo(TypeRegistry typeRegistry,
                                          Set<String> allSuperTypeNames,
                                          Map<String, Attribute> allAttributes,
                                          Map<String, String> attributeToEntityNameMap,
                                          List<String> visitedTypes) throws BaseException {
        if (visitedTypes.contains(entityDef.getName())) {
            throw new BaseException(ErrorCode.CIRCULAR_REFERENCE, entityDef.getName(),
                                         visitedTypes.toString());
        }

        if (CollectionUtils.isNotEmpty(entityDef.getSuperTypes())) {
            visitedTypes.add(entityDef.getName());
            for (String superTypeName : entityDef.getSuperTypes()) {
                EntityType superType = typeRegistry.getEntityTypeByName(superTypeName);

                if (superType != null) {
                    superType.collectTypeHierarchyInfo(typeRegistry, allSuperTypeNames, allAttributes, attributeToEntityNameMap, visitedTypes);
                }
            }
            visitedTypes.remove(entityDef.getName());
            allSuperTypeNames.addAll(entityDef.getSuperTypes());
        }

        if (CollectionUtils.isNotEmpty(entityDef.getAttributeDefs())) {
            for (AttributeDef attributeDef : entityDef.getAttributeDefs()) {
                Type type = typeRegistry.getType(attributeDef.getTypeName());
                String attributeName = attributeDef.getName();

                if (attributeToEntityNameMap.containsKey(attributeName)
                        && !attributeToEntityNameMap.get(attributeName).equals(entityDef.getName())) {
                    throw new BaseException(ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS_IN_ANOTHER_PARENT_TYPE,
                            entityDef.getName(), attributeName, attributeToEntityNameMap.get(attributeName));
                }

                allAttributes.put(attributeName, new Attribute(this, attributeDef, type));
                attributeToEntityNameMap.put(attributeName, entityDef.getName());
            }
        }
    }

    private void populateDynFlagsInfo() {
        dynAttributes              = new ArrayList<>();
        dynEvalTriggerAttributes   = new ArrayList<>();

        for (String attributeName : parsedTemplates.keySet()) {
            Attribute attribute = getAttribute(attributeName);
            if (attribute != null) {
                dynAttributes.add(attribute);
            }
        }

        //reorder dynAttributes in a topological sort
        dynAttributes = reorderDynAttributes();

        for (List<TemplateToken> parsedTemplate : parsedTemplates.values()) {
            for (TemplateToken token : parsedTemplate) {
                // If token is an instance of AttributeToken means that the attribute is of this entity type
                // so it must be added to the dynEvalTriggerAttributes list
                if (token instanceof AttributeToken) {
                    Attribute attribute = getAttribute(token.getValue());

                    if (attribute != null) {
                        dynEvalTriggerAttributes.add(attribute);
                    }
                }
            }
        }

        dynAttributes = Collections.unmodifiableList(dynAttributes);
        dynEvalTriggerAttributes = Collections.unmodifiableList(dynEvalTriggerAttributes);

        for (Attribute attribute : dynAttributes) {
            attribute.setIsDynAttribute(true);
        }

        for (Attribute attribute : dynEvalTriggerAttributes) {
            attribute.setIsDynAttributeEvalTrigger(true);
        }
    }

    private Map<String, List<TemplateToken>> parseDynAttributeTemplates(){
        Map<String, List<TemplateToken>> ret = new HashMap<>();
        Map<String, String> options = entityDef.getOptions();
        if (options == null || options.size() == 0) {
            return ret;
        }

        for (String key : options.keySet()) {
            if (key.startsWith(DYN_ATTRIBUTE_PREFIX)) {
                String         attributeName   = key.substring(DYN_ATTRIBUTE_PREFIX.length());
                Attribute attribute       = getAttribute(attributeName);

                if (attribute == null) {
                    LOG.warn("Ignoring {} attribute of {} type as dynamic attribute because attribute does not exist", attributeName, this.getTypeName());
                    continue;
                }

                if (!(attribute.getAttributeType() instanceof BuiltInTypes.StringType)) {
                    LOG.warn("Ignoring {} attribute of {} type as dynamic attribute because attribute isn't a string type", attributeName, this.getTypeName());
                    continue;
                }

                String template = options.get(key);
                List<TemplateToken> splitTemplate = templateSplit(template);

                ret.put(attributeName,splitTemplate);
            }
        }

        return Collections.unmodifiableMap(ret);
    }

    // own split function that also designates the right subclass for each token
    private List<TemplateToken> templateSplit(String template) {
        List<TemplateToken> ret = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean isInAttrName = false;

        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);

            switch (c) {
                case DYN_ATTRIBUTE_OPEN_DELIM:
                    isInAttrName = true;

                    if (token.length() > 0) {
                        ret.add(new ConstantToken(token.toString()));
                        token.setLength(0);
                    }
                    break;

                case DYN_ATTRIBUTE_CLOSE_DELIM:
                    if (isInAttrName) {
                        isInAttrName = false;

                        if (token.length() > 0) {
                            String attrName = token.toString();

                            if (attrName.indexOf(DYN_ATTRIBUTE_NAME_SEPARATOR) != -1) {
                                ret.add(new DependentToken(token.toString()));
                            } else {
                                ret.add(new AttributeToken(token.toString()));
                            }

                            token.setLength(0);
                        }
                    } else {
                        token.append(c);
                    }
                    break;

                default:
                    token.append(c);
                    break;
            }
        }

        return ret;
    }

    boolean isAssignableFrom(ObjectId objId) {
        boolean ret = TypeUtil.isValid(objId) && (StringUtils.equals(objId.getTypeName(), getTypeName()) || isSuperTypeOf(objId.getTypeName()));

        return ret;
    }

    private boolean validateRelationshipAttributes(Object obj) {
        if (obj != null && MapUtils.isNotEmpty(relationshipAttributes)) {
            if (obj instanceof Entity) {
                Entity entityObj = (Entity) obj;

                for (String attributeName : relationshipAttributes.keySet()) {
                    Object attributeValue  = entityObj.getRelationshipAttribute(attributeName);
                    String relationshipType = EntityUtil.getRelationshipType(attributeValue);
                    Attribute attribute = getRelationshipAttribute(attributeName, relationshipType);
                    AttributeDef attributeDef = attribute.getAttributeDef();

                    if (!isAssignableValue(attributeValue, attributeDef)) {
                        return false;
                    }
                }
            } else if (obj instanceof Map) {
                Map map = TypeUtil.toRelationshipAttributes((Map) obj);

                for (String attributeName : relationshipAttributes.keySet()) {
                    Object attributeValue   = map.get(attributeName);
                    String relationshipType = EntityUtil.getRelationshipType(attributeValue);
                    Attribute attribute = getRelationshipAttribute(attributeName, relationshipType);
                    AttributeDef attributeDef = attribute.getAttributeDef();

                    if (!isAssignableValue(attributeValue, attributeDef)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Takes a set of entityType names and a registry and returns a set of the entitytype names and the names of all their subTypes.
     *
     * @param entityTypes
     * @param typeRegistry
     * @return set of strings of the types and their subtypes.
     */
    static public Set<String> getEntityTypesAndAllSubTypes(Set<String> entityTypes, TypeRegistry typeRegistry) throws BaseException {
        Set<String> ret = new HashSet<>();

        for (String typeName : entityTypes) {
            io.simforce.bytezard.metadata.type.EntityType entityType = typeRegistry.getEntityTypeByName(typeName);
            if (entityType == null) {
                throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, typeName);
            }

            ret.addAll(entityType.getTypeAndAllSubTypes());
        }

        return ret;
    }

    private boolean isAssignableValue(Object value, AttributeDef attributeDef) {
        boolean ret = true;

        if (value != null) {
            String         relationshipType = EntityUtil.getRelationshipType(value);
            Attribute attribute        = getRelationshipAttribute(attributeDef.getName(), relationshipType);

            if (attribute != null) {
                Type attrType = attribute.getAttributeType();

                if (!isValidRelationshipType(attrType) && !attrType.isValidValue(value)) {
                    ret = false;
                }
            }
        }

        return ret;
    }

    private boolean isValidRelationshipType(Type attributeType) {
        boolean ret = false;

        if (attributeType != null) {
            if (attributeType instanceof ArrayType) {
                attributeType = ((ArrayType) attributeType).getElementType();
            }

            if (attributeType instanceof ObjectIdType || attributeType instanceof io.simforce.bytezard.metadata.type.EntityType) {
                ret = true;
            }
        }

        return ret;
    }

    private void normalizeRelationshipAttributeValues(Entity entity, boolean isUpdate) {
        if (entity != null) {
            for (String attributeName : relationshipAttributes.keySet()) {
                if (entity.hasRelationshipAttribute(attributeName)) {
                    Object attributeValue = entity.getRelationshipAttribute(attributeName);
                    String relationshipType = EntityUtil.getRelationshipType(attributeValue);
                    Attribute attribute = getRelationshipAttribute(attributeName, relationshipType);

                    if (attribute != null) {
                        Type attrType = attribute.getAttributeType();

                        if (isValidRelationshipType(attrType)) {
                            if (isUpdate) {
                                attributeValue = attrType.getNormalizedValueForUpdate(attributeValue);
                            } else {
                                attributeValue = attrType.getNormalizedValue(attributeValue);
                            }

                            entity.setRelationshipAttribute(attributeName, attributeValue);
                        }
                    }
                }
            }
        }
    }

    public void normalizeRelationshipAttributeValues(Map<String, Object> obj, boolean isUpdate) {
        if (obj != null) {
            for (String attributeName : relationshipAttributes.keySet()) {
                if (obj.containsKey(attributeName)) {
                    Object         attributeValue   = obj.get(attributeName);
                    String         relationshipType = EntityUtil.getRelationshipType(attributeValue);
                    Attribute attribute        = getRelationshipAttribute(attributeName, relationshipType);

                    if (attribute != null) {
                        Type attrType = attribute.getAttributeType();

                        if (isValidRelationshipType(attrType)) {
                            if (isUpdate) {
                                attributeValue = attrType.getNormalizedValueForUpdate(attributeValue);
                            } else {
                                attributeValue = attrType.getNormalizedValue(attributeValue);
                            }

                            obj.put(attributeName, attributeValue);
                        }
                    }
                }
            }
        }
    }

    private boolean validateRelationshipAttributes(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null && MapUtils.isNotEmpty(relationshipAttributes)) {
            if (obj instanceof Entity) {
                Entity entityObj = (Entity) obj;

                for (String attributeName : relationshipAttributes.keySet()) {
                    Object         value            = entityObj.getRelationshipAttribute(attributeName);
                    String         relationshipType = EntityUtil.getRelationshipType(value);
                    Attribute attribute        = getRelationshipAttribute(attributeName, relationshipType);

                    if (attribute != null) {
                        Type dataType = attribute.getAttributeType();

                        if (!attribute.getAttributeDef().getIsOptional()) {
                            // if required attribute is null, check if attribute value specified in relationship
                            if (value == null) {
                                value = entityObj.getRelationshipAttribute(attributeName);
                            }

                            if (value == null) {
                                ret = false;
                                messages.add(objName + "." + attributeName + ": mandatory attribute value missing in type " + getTypeName());
                            }
                        }

                        if (isValidRelationshipType(dataType) && value != null) {
                            ret = dataType.validateValue(value, objName + "." + attributeName, messages) && ret;
                        }
                    }
                }
            } else if (obj instanceof Map) {
                Map attributes = TypeUtil.toRelationshipAttributes((Map) obj);

                for (String attributeName : relationshipAttributes.keySet()) {
                    Object         value            = attributes.get(attributeName);
                    String         relationshipType = EntityUtil.getRelationshipType(value);
                    Attribute attribute        = getRelationshipAttribute(attributeName, relationshipType);

                    if (attribute != null) {
                        Type dataType = attribute.getAttributeType();

                        if (isValidRelationshipType(dataType) && value != null) {
                            ret = dataType.validateValue(value, objName + "." + attributeName, messages) && ret;
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

    private List<Attribute> reorderDynAttributes() {
        Map<Attribute, List<Attribute>> adj = createTokenAttributesMap();

        return topologicalSort(adj);
    }

    private List<Attribute> topologicalSort(Map<Attribute, List<Attribute>> adj){
        List<Attribute> order   = new ArrayList<>();
        Set<Attribute>  visited = new HashSet<>();

        for (Attribute attribute : adj.keySet()) {
            visitAttribute(attribute, visited, order, adj);
        }

        Collections.reverse(order);

        return order;
    }

    private void visitAttribute(Attribute attribute, Set<Attribute> visited, List<Attribute> order, Map<Attribute, List<Attribute>> adj) {
        if (!visited.contains(attribute)) {
            visited.add(attribute);

            for (Attribute neighbor : adj.get(attribute)) {
                visitAttribute(neighbor, visited, order, adj);
            }

            order.add(attribute);
        }
    }

    private Map<Attribute, List<Attribute>> createTokenAttributesMap() {
        Map<Attribute, List<Attribute>> adj = new HashMap<>();

        for (Attribute attribute : dynAttributes) {
            adj.put(attribute, new ArrayList<>());
        }

        for (Attribute attribute : adj.keySet()) {
            for (TemplateToken token : parsedTemplates.get(attribute.getName())) {
                if (token instanceof AttributeToken) {
                    Attribute tokenAttribute = getAttribute(token.getValue());

                    if (adj.containsKey(tokenAttribute)) {
                        adj.get(tokenAttribute).add(attribute);
                    }
                }
            }
        }

        return adj;
    }

    /* this class provides abstractions that help basic-search and dsl-search to deal with
     * system-attributes and business-metadata-attributes
     */
    private static class RootEntityType extends io.simforce.bytezard.metadata.type.EntityType {
        private TypeRegistry typeRegistry = null;

        public RootEntityType() {
            super(getRootEntityDef());
        }

        @Override
        void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
            super.resolveReferences(typeRegistry);

            // save typeRegistry for use in getBusinessAttribute()
            this.typeRegistry = typeRegistry;
        }

        @Override
        public BusinessAttribute getBusinessAttribute(String bmName, String bmAttrName) {
            BusinessMetadataType bmType = typeRegistry != null ? typeRegistry.getBusinessMetadataTypeByName(bmName) : null;
            Attribute bmAttr = bmType != null ? bmType.getAttribute(bmAttrName) : null;

            return (bmAttr instanceof BusinessAttribute) ? (BusinessAttribute) bmAttr : null;
        }

        private static EntityDef getRootEntityDef() {
            List<AttributeDef> attributeDefs = new ArrayList<AttributeDef>() {{
                add(new AttributeDef(TIMESTAMP_PROPERTY_KEY, TYPE_DATE, false, true));
                add(new AttributeDef(MODIFICATION_TIMESTAMP_PROPERTY_KEY, TYPE_DATE, false, true));
                add(new AttributeDef(MODIFIED_BY_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(CREATED_BY_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(STATE_PROPERTY_KEY, TYPE_STRING, false, true));

                add(new AttributeDef(GUID_PROPERTY_KEY, TYPE_STRING, true, true));
                add(new AttributeDef(HISTORICAL_GUID_PROPERTY_KEY, TYPE_STRING, true, true));
                add(new AttributeDef(TYPE_NAME_PROPERTY_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(CLASSIFICATION_TEXT_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(CLASSIFICATION_NAMES_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(PROPAGATED_CLASSIFICATION_NAMES_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(IS_INCOMPLETE_PROPERTY_KEY, TYPE_INT, false, true));
                add(new AttributeDef(LABELS_PROPERTY_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(CUSTOM_ATTRIBUTES_PROPERTY_KEY, TYPE_STRING, false, true));
                add(new AttributeDef(PENDING_TASKS_PROPERTY_KEY, TYPE_STRING, false, true));
            }};

            return new EntityDef(ENTITY_ROOT_NAME, "Root entity for system attributes", "1.0", attributeDefs);
        }
    }
}
