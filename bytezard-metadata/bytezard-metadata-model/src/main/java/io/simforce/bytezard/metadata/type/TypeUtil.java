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

import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_ARRAY_PREFIX;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_ARRAY_SUFFIX;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_MAP_KEY_VAL_SEP;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_MAP_PREFIX;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_MAP_SUFFIX;

import io.simforce.bytezard.metadata.model.instance.Classification;
import io.simforce.bytezard.metadata.model.instance.Entity;
import io.simforce.bytezard.metadata.model.instance.EntityHeader;
import io.simforce.bytezard.metadata.model.instance.ObjectId;
import io.simforce.bytezard.metadata.model.instance.RelatedObjectId;
import io.simforce.bytezard.metadata.model.instance.Struct;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef.EnumElementDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.PropagateTags;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.RelationshipCategory;
import io.simforce.bytezard.metadata.model.typedef.RelationshipEndDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef.Cardinality;
import io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef;
import io.simforce.bytezard.metadata.model.typedef.TypeDefHeader;
import io.simforce.bytezard.metadata.model.typedef.TypesDef;
import io.simforce.bytezard.metadata.type.StructType.Attribute;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility methods for Type/TypeDef.
 */
public class TypeUtil {

    private static final Set<String> BUILTIN_TYPE_NAMES = new HashSet<>();
    private static final String  NAME_REGEX         = "[a-zA-Z][a-zA-Z0-9_ ]*";
    private static final String  TRAIT_NAME_REGEX   = "[a-zA-Z][a-zA-Z0-9_ .]*";
    private static final Pattern NAME_PATTERN       = Pattern.compile(NAME_REGEX);
    private static final Pattern TRAIT_NAME_PATTERN = Pattern.compile(TRAIT_NAME_REGEX);

    private static final String InvalidTypeNameErrorMessage      = "Name must consist of a letter followed by a sequence of [ letter, number, '_' ] characters.";
    private static final String InvalidTraitTypeNameErrorMessage = "Name must consist of a letter followed by a sequence of [ letter,  number, '_', '.' ] characters.";

    public static final String ATTRIBUTE_QUALIFIED_NAME = "qualifiedName";

    static {
        Collections.addAll(BUILTIN_TYPE_NAMES, BaseTypeDef.BUILTIN_TYPES);
    }

    public static Set<String> getReferencedTypeNames(String typeName) {
        Set<String> ret = new HashSet<>();

        getReferencedTypeNames(typeName, ret);

        return ret;
    }

    public static boolean isBuiltInType(String typeName) {
        return BUILTIN_TYPE_NAMES.contains(typeName);
    }

    public static boolean isArrayType(String typeName) {
        return StringUtils.startsWith(typeName, TYPE_ARRAY_PREFIX)
            && StringUtils.endsWith(typeName, TYPE_ARRAY_SUFFIX);
    }

    public static boolean isMapType(String typeName) {
        return StringUtils.startsWith(typeName, TYPE_MAP_PREFIX)
            && StringUtils.endsWith(typeName, TYPE_MAP_SUFFIX);
    }

    public static boolean isValidTypeName(String typeName) {
        Matcher m = NAME_PATTERN.matcher(typeName);
        return m.matches();
    }

    public static String getInvalidTypeNameErrorMessage() {
        return InvalidTypeNameErrorMessage;
    }

    public static boolean isValidTraitTypeName(String typeName) {
        Matcher m = TRAIT_NAME_PATTERN.matcher(typeName);

        return m.matches();
    }

    public static String getInvalidTraitTypeNameErrorMessage() {
        return InvalidTraitTypeNameErrorMessage;
    }

    public static String getStringValue(Map map, Object key) {
        Object ret = map != null ? map.get(key) : null;

        return ret != null ? ret.toString() : null;
    }

    private static void getReferencedTypeNames(String typeName, Set<String> referencedTypeNames) {
        if (StringUtils.isNotBlank(typeName) && !referencedTypeNames.contains(typeName)) {
            if (typeName.startsWith(TYPE_ARRAY_PREFIX) && typeName.endsWith(TYPE_ARRAY_SUFFIX)) {
                int startIdx = TYPE_ARRAY_PREFIX.length();
                int endIdx = typeName.length() - TYPE_ARRAY_SUFFIX.length();
                String elementTypeName = typeName.substring(startIdx, endIdx);

                getReferencedTypeNames(elementTypeName, referencedTypeNames);
            } else if (typeName.startsWith(TYPE_MAP_PREFIX) && typeName.endsWith(TYPE_MAP_SUFFIX)) {
                int startIdx = TYPE_MAP_PREFIX.length();
                int endIdx = typeName.length() - TYPE_MAP_SUFFIX.length();
                String[] keyValueTypes = typeName.substring(startIdx, endIdx).split(TYPE_MAP_KEY_VAL_SEP, 2);
                String   keyTypeName   = keyValueTypes.length > 0 ? keyValueTypes[0] : null;
                String   valueTypeName = keyValueTypes.length > 1 ? keyValueTypes[1] : null;

                getReferencedTypeNames(keyTypeName, referencedTypeNames);
                getReferencedTypeNames(valueTypeName, referencedTypeNames);
            } else {
                referencedTypeNames.add(typeName);
            }
        }
    }

    public static RelationshipType findRelationshipWithLegacyRelationshipEnd(String entityTypeName, String attributeName, TypeRegistry typeRegistry) {
        RelationshipType ret = null;

        for (RelationshipDef relationshipDef : typeRegistry.getAllRelationshipDefs()) {
            RelationshipEndDef end1Def = relationshipDef.getEndDef1();
            RelationshipEndDef end2Def = relationshipDef.getEndDef2();

            if ((end1Def.getIsLegacyAttribute() && StringUtils.equals(end1Def.getType(), entityTypeName) && StringUtils.equals(end1Def.getName(), attributeName)) ||
                (end2Def.getIsLegacyAttribute() && StringUtils.equals(end2Def.getType(), entityTypeName) && StringUtils.equals(end2Def.getName(), attributeName))) {
                ret = typeRegistry.getRelationshipTypeByName(relationshipDef.getName());

                break;
            }
        }

        return ret;
    }

    public static AttributeDef createOptionalAttrDef(String name, io.simforce.bytezard.metadata.type.Type dataType) {
        return new AttributeDef(name, dataType.getTypeName(), true,
            Cardinality.SINGLE, 0, 1,
            false, false, false,
            Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createOptionalAttrDef(String name, String dataType) {
        return new AttributeDef(name, dataType, true,
            Cardinality.SINGLE, 0, 1,
            false, false, false,
            Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createOptionalAttrDef(String name, String dataType, Map<String, String> options, String desc) {
        return new AttributeDef(name, dataType, true,
                Cardinality.SINGLE, 0, 1,
                false, false, false, "",
                Collections.<ConstraintDef>emptyList(), options, desc, 0, null);
    }

    public static AttributeDef createRequiredAttrDef(String name, String dataType) {
        return new AttributeDef(name, dataType, false,
            Cardinality.SINGLE, 1, 1,
            false, true, false,
            Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createListRequiredAttrDef(String name, String dataType) {
        return new AttributeDef(name, dataType, false,
                Cardinality.LIST, 1, Integer.MAX_VALUE,
                false, true, false,
                Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createOptionalListAttrDef(String name, String dataType) {
        return new AttributeDef(name, dataType, true,
                Cardinality.LIST, 1, Integer.MAX_VALUE,
                false, true, false,
                Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createRequiredListAttrDefWithConstraint(String name, String dataType, String type, Map param) {
        AttributeDef ret = TypeUtil.createListRequiredAttrDef(name, dataType);
        ret.addConstraint(new ConstraintDef(type, param));

        return ret;
    }

    public static AttributeDef createRequiredAttrDefWithConstraint(String name, String typeName, String type, Map param) {
        AttributeDef ret = TypeUtil.createRequiredAttrDef(name, typeName);
        ret.addConstraint(new ConstraintDef(type, param));

        return ret;
    }

    public static AttributeDef createOptionalAttrDefWithConstraint(String name, String typeName, String type, Map param) {
        AttributeDef ret = TypeUtil.createOptionalAttrDef(name, typeName);
        ret.addConstraint(new ConstraintDef(type, param));

        return ret;
    }

    public static AttributeDef createUniqueRequiredAttrDef(String name, io.simforce.bytezard.metadata.type.Type dataType) {
        return new AttributeDef(name, dataType.getTypeName(), false,
            Cardinality.SINGLE, 1, 1,
            true, true, false,
            Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createUniqueRequiredAttrDef(String name, String typeName) {
        return new AttributeDef(name, typeName, false,
            Cardinality.SINGLE, 1, 1,
            true, true, false,
            Collections.<ConstraintDef>emptyList());
    }

    public static AttributeDef createRequiredAttrDef(String name, io.simforce.bytezard.metadata.type.Type dataType) {
        return new AttributeDef(name, dataType.getTypeName(), false,
            Cardinality.SINGLE, 1, 1,
            false, true, false,
            Collections.<ConstraintDef>emptyList());
    }

    public static EnumDef createEnumTypeDef(String name, String description, EnumElementDef... enumValues) {
        return new EnumDef(name, description, "1.0", Arrays.asList(enumValues));
    }

    public static ClassificationDef createTraitTypeDef(String name, Set<String> superTypes, AttributeDef... attrDefs) {
        return createTraitTypeDef(name, null, superTypes, attrDefs);
    }

    public static ClassificationDef createTraitTypeDef(String name, String description, Set<String> superTypes, AttributeDef... attrDefs) {
        return createTraitTypeDef(name, description, "1.0", superTypes, attrDefs);
    }

    public static ClassificationDef createTraitTypeDef(String name, String description, String version, Set<String> superTypes, AttributeDef... attrDefs) {
        return new ClassificationDef(name, description, version, Arrays.asList(attrDefs), superTypes);
    }

    public static ClassificationDef createClassificationDef(String name, String description, String version, Set<String> superTypes, Set<String> entityTypes, AttributeDef... attrDefs) {
        return new ClassificationDef(name, description, version, Arrays.asList(attrDefs), superTypes, entityTypes, null);
    }

    public static StructDef createStructTypeDef(String name, AttributeDef... attrDefs) {
        return createStructTypeDef(name, null, attrDefs);
    }

    public static StructDef createStructTypeDef(String name, String description, AttributeDef... attrDefs) {
        return new StructDef(name, description, "1.0", Arrays.asList(attrDefs));
    }

    public static EntityDef createClassTypeDef(String name, Set<String> superTypes, AttributeDef... attrDefs) {
        return createClassTypeDef(name, null, "1.0", superTypes, attrDefs);
    }

    public static EntityDef createClassTypeDef(String name, String description, Set<String> superTypes, AttributeDef... attrDefs) {
        return createClassTypeDef(name, description, "1.0", superTypes, attrDefs);
    }

    public static EntityDef createClassTypeDef(String name, String description, String version, Set<String> superTypes, AttributeDef... attrDefs) {
        return new EntityDef(name, description, version, Arrays.asList(attrDefs), superTypes);
    }

    public static EntityDef createClassTypeDef(String name, String description, String version, Set<String> superTypes, Map<String, String> options, AttributeDef... attrDefs) {
        return new EntityDef(name, description, version, Arrays.asList(attrDefs), superTypes, options);
    }

    public static BusinessMetadataDef createBusinessMetadataDef(String name, String description, String typeVersion, AttributeDef... attributeDefs) {
        if (attributeDefs == null || attributeDefs.length == 0) {
            return new BusinessMetadataDef(name, description, typeVersion);
        }
        return new BusinessMetadataDef(name, description, typeVersion, Arrays.asList(attributeDefs));
    }

    public static RelationshipDef createRelationshipTypeDef(String name,
                                                            String description,
                                                            String version,
                                                            RelationshipCategory relationshipCategory,
                                                            PropagateTags propagateTags,
                                                            RelationshipEndDef endDef1,
                                                            RelationshipEndDef endDef2,
                                                            AttributeDef... attrDefs) {
        return new RelationshipDef(name, description, version, relationshipCategory, propagateTags,
                                        endDef1, endDef2, Arrays.asList(attrDefs));
    }

    public static RelationshipEndDef createRelationshipEndDef(String typeName, String name, Cardinality cardinality, boolean isContainer) {
        return new RelationshipEndDef(typeName, name, cardinality, isContainer);
    }

    public static TypesDef getTypesDef(List<EnumDef> enums,
                                       List<StructDef> structs,
                                       List<ClassificationDef> traits,
                                       List<EntityDef> classes) {
        return new TypesDef(enums, structs, traits, classes);
    }

    public static TypesDef getTypesDef(List<EnumDef> enums,
                                       List<StructDef> structs,
                                       List<ClassificationDef> traits,
                                       List<EntityDef> classes,
                                       List<RelationshipDef> relations) {
        return new TypesDef(enums, structs, traits, classes, relations);
    }

    public static TypesDef getTypesDef(List<EnumDef> enums,
                                       List<StructDef> structs,
                                       List<ClassificationDef> traits,
                                       List<EntityDef> classes,
                                       List<RelationshipDef> relations,
                                       List<BusinessMetadataDef> businessMetadataDefs) {
        return new TypesDef(enums, structs, traits, classes, relations, businessMetadataDefs);
    }

    public static List<TypeDefHeader> toTypeDefHeader(TypesDef typesDef) {
        List<TypeDefHeader> headerList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(typesDef.getEnumDefs())) {
            for (EnumDef enumDef : typesDef.getEnumDefs()) {
                headerList.add(new TypeDefHeader(enumDef));
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef structDef : typesDef.getStructDefs()) {
                headerList.add(new TypeDefHeader(structDef));
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef classificationDef : typesDef.getClassificationDefs()) {
                headerList.add(new TypeDefHeader(classificationDef));
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                headerList.add(new TypeDefHeader(entityDef));
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                headerList.add(new TypeDefHeader(relationshipDef));
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                headerList.add(new TypeDefHeader(businessMetadataDef));
            }
        }

        return headerList;
    }

    public static TypesDef getTypesDef(BaseTypeDef typeDef) {
        TypesDef ret = new TypesDef();

        if (typeDef != null) {
            if (typeDef.getClass().equals(EntityDef.class)) {
                ret.getEntityDefs().add((EntityDef) typeDef);
            } else if (typeDef.getClass().equals(RelationshipDef.class)) {
                ret.getRelationshipDefs().add((RelationshipDef) typeDef);
            } else if (typeDef.getClass().equals(ClassificationDef.class)) {
                ret.getClassificationDefs().add((ClassificationDef) typeDef);
            } else if (typeDef.getClass().equals(StructDef.class)) {
                ret.getStructDefs().add((StructDef) typeDef);
            } else if (typeDef.getClass().equals(EnumDef.class)) {
                ret.getEnumDefs().add((EnumDef) typeDef);
            }
        }

        return ret;
    }

    public static Collection<ObjectId> toObjectIds(Collection<Entity> entities) {
        List<ObjectId> ret = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(entities)) {
            for (Entity entity : entities) {
                if (entity != null) {
                    ret.add(TypeUtil.getObjectId(entity));
                }
            }
        }

        return ret;
    }

    public static Collection<RelatedObjectId> toRelatedObjectIds(Collection<Entity> entities) {
        List<RelatedObjectId> ret = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(entities)) {
            for (Entity entity : entities) {
                if (entity != null) {
                    ret.add(toRelatedObjectId(entity));
                }
            }
        }

        return ret;
    }

    public static Map toStructAttributes(Map map) {
        if (map != null && map.containsKey("typeName") && map.containsKey("attributes") && map.get("attributes") instanceof Map) {
            return (Map)map.get("attributes");
        }

        return map;
    }

    public static Map toRelationshipAttributes(Map map) {
        Map ret = null;

        if (map != null && map.containsKey("typeName") && map.containsKey("relationshipAttributes") && map.get("relationshipAttributes") instanceof Map) {
            ret = (Map) map.get("relationshipAttributes");
        }

        return ret;
    }

    public static RelatedObjectId toRelatedObjectId(Entity entity) {
        return new RelatedObjectId(getObjectId(entity));
    }

    public static RelatedObjectId toRelatedObjectId(Entity entity, String relationshipType){
        return new RelatedObjectId(getObjectId(entity), relationshipType);
    }

    public static RelatedObjectId toRelatedObjectId(Entity entity, TypeRegistry typeRegistry) {
        return new RelatedObjectId(getObjectId(entity, typeRegistry));
    }

    public static ObjectId getObjectId(Entity entity, TypeRegistry typeRegistry) {
        String              typeName       = entity.getTypeName();
        io.simforce.bytezard.metadata.type.EntityType entityType     = typeRegistry.getEntityTypeByName(typeName);
        Map<String, Object> uniqAttributes = null;

        if (entityType != null && MapUtils.isNotEmpty(entityType.getUniqAttributes())) {
            for (Attribute attribute : entityType.getUniqAttributes().values()) {
                Object attrValue = entity.getAttribute(attribute.getName());

                if (attrValue != null) {
                    if (uniqAttributes == null) {
                        uniqAttributes = new HashMap<>();
                    }

                    uniqAttributes.put(attribute.getName(), attrValue);
                }
            }
        }

        return new ObjectId(entity.getGuid(), typeName, uniqAttributes);
    }

    public static ObjectId getObjectId(EntityHeader header) {
        return new ObjectId(header.getGuid(), header.getTypeName());
    }

    public static List<ObjectId> getObjectIds(List<Entity> entities) {
        final List<ObjectId> ret;

        if (CollectionUtils.isNotEmpty(entities)) {
            ret = new ArrayList<>(entities.size());

            for (Entity entity : entities) {
                ret.add(getObjectId(entity));
            }
        } else {
            ret = new ArrayList<>();
        }

        return ret;
    }

    public static RelatedObjectId getRelatedObjectId(Entity entity, String relationshipType) {
        return getRelatedObjectId(getObjectId(entity), relationshipType);
    }

    public static RelatedObjectId getRelatedObjectId(ObjectId objectId, String relationShipType) {
        RelatedObjectId RelatedObjectId = new RelatedObjectId(objectId, relationShipType);
        return RelatedObjectId;
    }

    public static List<RelatedObjectId> getRelatedObjectIds(List<Entity> entities, String relationshipType) {
        final List<RelatedObjectId> ret;
        if (CollectionUtils.isNotEmpty(entities)) {
            ret = new ArrayList<>(entities.size());
            for (Entity entity : entities) {
                ret.add(getRelatedObjectId(entity, relationshipType));
            }
        } else {
            ret = Collections.emptyList();
        }
        return ret;
    }

    public static List<RelatedObjectId> getRelatedObjectIdList(List<ObjectId> objectIds, String relationshipType) {
        final List<RelatedObjectId> ret;
        if (CollectionUtils.isNotEmpty(objectIds)) {
            ret = new ArrayList<>(objectIds.size());
            for (ObjectId objectId : objectIds) {
                ret.add(getRelatedObjectId(objectId, relationshipType));
            }
        } else {
            ret = Collections.emptyList();
        }
        return ret;
    }

    public static ObjectId getObjectId(Entity entity) {
        String qualifiedName = (String) entity.getAttribute(ATTRIBUTE_QUALIFIED_NAME);
        ObjectId ret = new ObjectId(entity.getGuid(), entity.getTypeName(), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));

        return ret;
    }

    public static boolean isValidGuid(ObjectId objId) {
        return isValidGuid(objId.getGuid());
    }

    public static boolean isAssignedGuid(ObjectId objId) {
        return isAssignedGuid(objId.getGuid());
    }

    public static boolean isUnAssignedGuid(ObjectId objId) {
        return isUnAssignedGuid(objId.getGuid());
    }

    public static boolean isValidGuid(String guid) {
        return isAssignedGuid(guid) || isUnAssignedGuid(guid);
    }

    public static boolean isAssignedGuid(String guid) {
        /**
         * The rule for whether a GUID is 'assigned' is that it must always be non-null, non-empty
         * and must not start with a '-' character, because in  the '-' prefix character
         * signifies an  'unassigned' GUID. There are no other GUID formatting constraints.
         *
         * An object from a remote repository can be saved into  with its existing (external) GUID
         * if that GUID conforms to the same 3 conditions. If, in future, it is required to save objects from
         * a remote repository that assigns GUIDs that can start with the '-' character, then it will be
         * necessary to enhance this isAssignedGUID() method to accepts and check the object's homeId, such
         * that if homeId is not null (the object is from a remote repository), then the '-' prefix constraint
         * is relaxed. Such a change would require a pervasive change to  classes and therefore should
         * only be implemented if it is found to be necessary.
         */
        if (guid != null) {
            return guid.length() > 0 && guid.charAt(0) != '-';
        }
        return false;
    }

    public static boolean isUnAssignedGuid(String guid) {
        return guid != null && guid.length() > 0 && guid.charAt(0) == '-';
    }

    public static boolean isValid(ObjectId objId) {
        if (isAssignedGuid(objId) || isUnAssignedGuid(objId)) {
            return true;
        } else {
            return StringUtils.isNotEmpty(objId.getTypeName()) && MapUtils.isNotEmpty(objId.getUniqueAttributes());
        }
    }

    public static String toDebugString(TypesDef typesDef) {
        StringBuilder sb = new StringBuilder();

        sb.append("typesDef={");
        if (typesDef != null) {
            sb.append("enumDefs=[");
            dumpTypeNames(typesDef.getEnumDefs(), sb);
            sb.append("],");

            sb.append("structDefs=[");
            dumpTypeNames(typesDef.getStructDefs(), sb);
            sb.append("],");

            sb.append("classificationDefs=[");
            dumpTypeNames(typesDef.getClassificationDefs(), sb);
            sb.append("],");

            sb.append("entityDefs=[");
            dumpTypeNames(typesDef.getEntityDefs(), sb);
            sb.append("]");

            sb.append("relationshipDefs=[");
            dumpTypeNames(typesDef.getRelationshipDefs(), sb);
            sb.append("]");
        }
        sb.append("}");

        return sb.toString();
    }

    public static Map<String, Object> toMap(Entity entity) {
        Map<String, Object> ret = null;

        if (entity != null) {
            ret = new LinkedHashMap<>();

            // Id type
            ret.put("$typeName$", entity.getTypeName());
            ret.put("$id$", new LinkedHashMap<String, Object>(){{
                put("id", entity.getGuid());
                put("$typeName$", entity.getTypeName());
                put("version", entity.getVersion().intValue());
                put("state", entity.getStatus().name());
            }});

            // System attributes
            ret.put("$systemAttributes$", new LinkedHashMap<String, String>() {{
                put("createdBy", entity.getCreatedBy());
                put("modifiedBy", entity.getUpdatedBy());
                put("createdTime", entity.getCreateTime().toString());
                put("modifiedTime", entity.getUpdateTime().toString());
            }});

            // Traits
            if (CollectionUtils.isNotEmpty(entity.getClassifications())) {
                Map<String, HashMap> traitDetails = entity.getClassifications()
                                                          .stream()
                                                          .collect(Collectors.toMap(Struct::getTypeName, TypeUtil::getNestedTraitDetails));
                ret.put("$traits$", traitDetails);
            }

            // All attributes
            if (MapUtils.isNotEmpty(entity.getAttributes())) {
                for (Map.Entry<String, Object> entry : entity.getAttributes().entrySet()) {
                    if (entry.getValue() instanceof ObjectId) {
                        ret.put(entry.getKey(), new LinkedHashMap<String, Object>(){{
                            put("id", ((ObjectId) entry.getValue()).getGuid());
                            put("$typeName$", ((ObjectId) entry.getValue()).getTypeName());
//                        put("version", entity.getVersion().intValue());
//                        put("state", entity.getStatus().name());
                        }});
                    } else {
                        ret.put(entry.getKey(), entry.getValue());
                    }
                }
            }

        }

        return ret;
    }

    private static HashMap getNestedTraitDetails(final Classification Classification) {
        return new HashMap<String, Object>() {{
            put("$typeName$", Classification.getTypeName());

            if (MapUtils.isNotEmpty(Classification.getAttributes())) {
                putAll(Classification.getAttributes());
            }
        }};
    }

    private static void dumpTypeNames(List<? extends BaseTypeDef> typeDefs, StringBuilder sb) {
        if (CollectionUtils.isNotEmpty(typeDefs)) {
            for (int i = 0; i < typeDefs.size(); i++) {
                BaseTypeDef typeDef = typeDefs.get(i);

                if (i > 0) {
                    sb.append(",");
                }

                sb.append(typeDef.getName());
            }
        }
    }
}