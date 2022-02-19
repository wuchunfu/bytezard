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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.TypesDef;
import io.simforce.bytezard.metadata.type.BuiltInTypes.BigDecimalType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.BigIntegerType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.BooleanType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.ByteType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.DateType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.DoubleType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.FloatType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.IntType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.LongType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.ObjectIdType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.ShortType;
import io.simforce.bytezard.metadata.type.BuiltInTypes.StringType;
import io.simforce.bytezard.metadata.type.StructType.Attribute;

/**
 * registry for all types defined in .
 */
@Singleton
@Component
public class TypeRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(TypeRegistry.class);
    private static final int DEFAULT_LOCK_MAX_WAIT_TIME_IN_SECONDS = 15;

    protected RegistryData registryData;
    private final TypeRegistryUpdateSynchronizer updateSynchronizer;
    private final Set<String> missingRelationshipDefs;
    private final Map<String, String> commonIndexFieldNameCache;

    public TypeRegistry() {
        registryData = new RegistryData();
        updateSynchronizer = new TypeRegistryUpdateSynchronizer(this);
        missingRelationshipDefs = new HashSet<>();
        commonIndexFieldNameCache = new LinkedHashMap<>();

        resolveReferencesForRootTypes();
        resolveIndexFieldNamesForRootTypes();
    }

    // used only by TransientTypeRegistry
    protected TypeRegistry(TypeRegistry other) {
        registryData = new RegistryData();
        updateSynchronizer = other.updateSynchronizer;
        missingRelationshipDefs = other.missingRelationshipDefs;
        commonIndexFieldNameCache = other.commonIndexFieldNameCache;

        resolveReferencesForRootTypes();
        resolveIndexFieldNamesForRootTypes();
    }

    public Collection<String> getAllTypeNames() { return registryData.allTypes.getAllTypeNames(); }

    public Collection<Type> getAllTypes() { return registryData.allTypes.getAllTypes(); }

    public Set<String> getAllServiceTypes() { return registryData.allTypes.getAllServiceTypes(); }

    public boolean isRegisteredType(String typeName) {
        return registryData.allTypes.isKnownType(typeName);
    }

    public Type getType(String typeName) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> TypeRegistry.getType({})", typeName);
        }

        Type ret = registryData.allTypes.getTypeByName(typeName);

        if (ret == null) {
            if (typeName.startsWith(TYPE_ARRAY_PREFIX) && typeName.endsWith(TYPE_ARRAY_SUFFIX)) {
                int startIdx = TYPE_ARRAY_PREFIX.length();
                int endIdx = typeName.length() - TYPE_ARRAY_SUFFIX.length();
                String elementTypeName = typeName.substring(startIdx, endIdx);

                ret = new ArrayType(elementTypeName, this);
            } else if (typeName.startsWith(TYPE_MAP_PREFIX) && typeName.endsWith(TYPE_MAP_SUFFIX)) {
                int startIdx = TYPE_MAP_PREFIX.length();
                int endIdx = typeName.length() - TYPE_MAP_SUFFIX.length();
                String[] keyValueTypes = typeName.substring(startIdx, endIdx).split(TYPE_MAP_KEY_VAL_SEP, 2);
                String keyTypeName = keyValueTypes.length > 0 ? keyValueTypes[0] : null;
                String valueTypeName = keyValueTypes.length > 1 ? keyValueTypes[1] : null;

                ret = new MapType(keyTypeName, valueTypeName, this);
            } else {
                throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, typeName);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== TypeRegistry.getType({}): {}", typeName, ret);
        }

        return ret;
    }

    public Type getTypeByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> TypeRegistry.getTypeByGuid({})", guid);
        }

        Type ret = registryData.allTypes.getTypeByGuid(guid);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== TypeRegistry.getTypeByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    public BaseTypeDef getTypeDefByName(String name) { return registryData.getTypeDefByName(name); }

    public BaseTypeDef getTypeDefByGuid(String guid) { return registryData.getTypeDefByGuid(guid); }

    public Collection<EnumDef> getAllEnumDefs() { return registryData.enumDefs.getAll(); }

    public EnumDef getEnumDefByGuid(String guid) {
        return registryData.enumDefs.getTypeDefByGuid(guid);
    }

    public EnumDef getEnumDefByName(String name) {
        return registryData.enumDefs.getTypeDefByName(name);
    }

    public Collection<String> getAllEnumDefNames() { return registryData.enumDefs.getAllNames(); }

    public Collection<EnumType> getAllEnumTypes() { return registryData.enumDefs.getAllTypes(); }

    public EnumType getEnumTypeByName(String name) { return registryData.enumDefs.getTypeByName(name); }

    public Collection<StructDef> getAllStructDefs() { return registryData.structDefs.getAll(); }

    public StructDef getStructDefByGuid(String guid) {
        return registryData.structDefs.getTypeDefByGuid(guid);
    }

    public StructDef getStructDefByName(String name) { return registryData.structDefs.getTypeDefByName(name); }

    public Collection<String> getAllStructDefNames() { return registryData.structDefs.getAllNames(); }

    public Collection<StructType> getAllStructTypes() { return registryData.structDefs.getAllTypes(); }

    public StructType getStructTypeByName(String name) { return registryData.structDefs.getTypeByName(name); }


    public Collection<ClassificationDef> getAllClassificationDefs() {
        return registryData.classificationDefs.getAll();
    }

    public ClassificationDef getClassificationDefByGuid(String guid) {
        return registryData.classificationDefs.getTypeDefByGuid(guid);
    }

    public ClassificationDef getClassificationDefByName(String name) {
        return registryData.classificationDefs.getTypeDefByName(name);
    }

    public Collection<String> getAllClassificationDefNames() { return registryData.classificationDefs.getAllNames(); }

    public Collection<ClassificationType> getAllClassificationTypes() {
        return registryData.classificationDefs.getAllTypes();
    }

    public ClassificationType getClassificationTypeByName(String name) {
        return registryData.classificationDefs.getTypeByName(name);
    }

    public Collection<BusinessMetadataType> getAllBusinessMetadataTypes() {
        return registryData.businessMetadataDefs.getAllTypes();
    }

    public Collection<BusinessMetadataDef> getAllBusinessMetadataDefs() {
        return registryData.businessMetadataDefs.getAll();
    }

    public BusinessMetadataType getBusinessMetadataTypeByName(String name) { return registryData.businessMetadataDefs.getTypeByName(name); }

    public Collection<RelationshipDef> getAllRelationshipDefs() { return registryData.relationshipDefs.getAll(); }

    public Collection<EntityDef> getAllEntityDefs() { return registryData.entityDefs.getAll(); }

    public EntityDef getEntityDefByGuid(String guid) {
        return registryData.entityDefs.getTypeDefByGuid(guid);
    }

    public EntityDef getEntityDefByName(String name) {
        return registryData.entityDefs.getTypeDefByName(name);
    }

    public Collection<String> getAllEntityDefNames() { return registryData.entityDefs.getAllNames(); }

    public Collection<EntityType> getAllEntityTypes() { return registryData.entityDefs.getAllTypes(); }

    public EntityType getEntityTypeByName(String name) { return registryData.entityDefs.getTypeByName(name); }
    /**
     * @return relationshipTypes
     */
    public Collection<RelationshipType> getAllRelationshipTypes() { return registryData.relationshipDefs.getAllTypes(); }

    public RelationshipDef getRelationshipDefByGuid(String guid) {
        return registryData.relationshipDefs.getTypeDefByGuid(guid);
    }
    public RelationshipDef getRelationshipDefByName(String name) {
        return registryData.relationshipDefs.getTypeDefByName(name);
    }

    public BusinessMetadataDef getBusinessMetadataDefByGuid(String guid) {
        return registryData.businessMetadataDefs.getTypeDefByGuid(guid);
    }

    public BusinessMetadataDef getBusinessMetadataDefByName(String name) {
        return registryData.businessMetadataDefs.getTypeDefByName(name);
    }

    public RelationshipType getRelationshipTypeByName(String name) { return registryData.relationshipDefs.getTypeByName(name); }

    public TransientTypeRegistry lockTypeRegistryForUpdate() throws BaseException {
        return lockTypeRegistryForUpdate(DEFAULT_LOCK_MAX_WAIT_TIME_IN_SECONDS);
    }

    public TransientTypeRegistry lockTypeRegistryForUpdate(int lockMaxWaitTimeInSeconds) throws BaseException {
        return updateSynchronizer.lockTypeRegistryForUpdate(lockMaxWaitTimeInSeconds);
    }

    public void releaseTypeRegistryForUpdate(TransientTypeRegistry transientTypeRegistry, boolean commitUpdates) {
        updateSynchronizer.releaseTypeRegistryForUpdate(transientTypeRegistry, commitUpdates);
    }

    public void reportMissingRelationshipDef(String entityType1, String entityType2, String attributeName) {
        String key = entityType1 + "->" + entityType2 + ":" + attributeName;

        if (!missingRelationshipDefs.contains(key)) {
            LOG.warn("No RelationshipDef defined between {} and {} on attribute: {}.{}", entityType1, entityType2, entityType1, attributeName);

            missingRelationshipDefs.add(key);
        }
    }

    public void addIndexFieldName(String propertyName, String indexFieldName) {
        commonIndexFieldNameCache.put(propertyName, indexFieldName);
    }

    private void resolveReferencesForRootTypes() {
        try {
            EntityType.ENTITY_ROOT.resolveReferences(this);
            ClassificationType.CLASSIFICATION_ROOT.resolveReferences(this);
        } catch (BaseException e) {
            LOG.error("Failed to initialize root types", e);
            throw new RuntimeException(e);
        }
    }

    private void resolveIndexFieldNamesForRootTypes() {
        for (StructType structType : Arrays.asList(EntityType.ENTITY_ROOT, ClassificationType.CLASSIFICATION_ROOT)) {
            for (Attribute attribute : structType.getAllAttributes().values()) {
                String indexFieldName = getIndexFieldName(attribute.getVertexPropertyName());

                if (StringUtils.isNotEmpty(indexFieldName)) {
                    attribute.setIndexFieldName(indexFieldName);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attribute {} with index name {} is added", attribute.getVertexPropertyName(), attribute.getIndexFieldName());
                }
            }
        }
    }

    /**
     * retrieves the index field name for the common field passed in.
     * @param propertyName the name of the common field.
     * @return the index name for the common field passed in.
     */
    public String getIndexFieldName(String propertyName) {
        return commonIndexFieldNameCache.get(propertyName);
    }

    static class RegistryData {
        final TypeCache allTypes;
        final TypeDefCache<EnumDef, EnumType> enumDefs;
        final TypeDefCache<StructDef, StructType> structDefs;
        final TypeDefCache<ClassificationDef, ClassificationType> classificationDefs;
        final TypeDefCache<EntityDef, EntityType> entityDefs;
        final TypeDefCache<RelationshipDef, RelationshipType> relationshipDefs;
        final TypeDefCache<BusinessMetadataDef, BusinessMetadataType> businessMetadataDefs;
        final TypeDefCache<? extends BaseTypeDef, ? extends Type>[]   allDefCaches;

        RegistryData() {
            allTypes = new TypeCache();
            enumDefs = new TypeDefCache<>(allTypes);
            structDefs = new TypeDefCache<>(allTypes);
            classificationDefs = new TypeDefCache<>(allTypes);
            entityDefs = new TypeDefCache<>(allTypes);
            relationshipDefs = new TypeDefCache<>(allTypes);
            businessMetadataDefs = new TypeDefCache<>(allTypes);
            allDefCaches = new TypeDefCache[] { enumDefs, structDefs, classificationDefs, entityDefs, relationshipDefs, businessMetadataDefs};

            init();
        }

        void init() {
            allTypes.addType(new BooleanType());
            allTypes.addType(new ByteType());
            allTypes.addType(new ShortType());
            allTypes.addType(new IntType());
            allTypes.addType(new LongType());
            allTypes.addType(new FloatType());
            allTypes.addType(new DoubleType());
            allTypes.addType(new BigIntegerType());
            allTypes.addType(new BigDecimalType());
            allTypes.addType(new DateType());
            allTypes.addType(new StringType());
            allTypes.addType(new ObjectIdType());
        }

        BaseTypeDef getTypeDefByName(String name) {
            BaseTypeDef ret = null;

            if (name != null) {
                for (TypeDefCache typeDefCache : allDefCaches) {
                    ret = typeDefCache.getTypeDefByName(name);

                    if (ret != null) {
                        break;
                    }
                }
            }

            return ret;
        }

        BaseTypeDef getTypeDefByGuid(String guid) {
            BaseTypeDef ret = null;

            if (guid != null) {
                for (TypeDefCache typeDefCache : allDefCaches) {
                    ret = typeDefCache.getTypeDefByGuid(guid);

                    if (ret != null) {
                        break;
                    }
                }
            }

            return ret;
        }

        void updateGuid(String typeName, String guid) {
            if (typeName != null) {
                enumDefs.updateGuid(typeName, guid);
                structDefs.updateGuid(typeName, guid);
                classificationDefs.updateGuid(typeName, guid);
                entityDefs.updateGuid(typeName, guid);
                relationshipDefs.updateGuid(typeName, guid);
                businessMetadataDefs.updateGuid(typeName, guid);
            }
        }

        void removeByGuid(String guid) {
            if (guid != null) {
                enumDefs.removeTypeDefByGuid(guid);
                structDefs.removeTypeDefByGuid(guid);
                classificationDefs.removeTypeDefByGuid(guid);
                entityDefs.removeTypeDefByGuid(guid);
                relationshipDefs.removeTypeDefByGuid(guid);
                businessMetadataDefs.removeTypeDefByGuid(guid);
            }
        }

        void removeByName(String typeName) {
            if (typeName != null) {
                enumDefs.removeTypeDefByName(typeName);
                structDefs.removeTypeDefByName(typeName);
                classificationDefs.removeTypeDefByName(typeName);
                entityDefs.removeTypeDefByName(typeName);
                relationshipDefs.removeTypeDefByName(typeName);
                businessMetadataDefs.removeTypeDefByName(typeName);
            }
        }

        void clear() {
            allTypes.clear();
            enumDefs.clear();
            structDefs.clear();
            classificationDefs.clear();
            entityDefs.clear();
            relationshipDefs.clear();
            businessMetadataDefs.clear();
            init();
        }
    }

    /**
     * Temporarily cached
     */
    public static class TransientTypeRegistry extends TypeRegistry {
        private final List<BaseTypeDef> addedTypes   = new ArrayList<>();
        private final List<BaseTypeDef> updatedTypes = new ArrayList<>();
        private final List<BaseTypeDef> deletedTypes = new ArrayList<>();

        private TransientTypeRegistry(TypeRegistry parent) throws BaseException {
            super(parent);

            addTypesWithNoRefResolve(parent.getAllEnumDefs());
            addTypesWithNoRefResolve(parent.getAllStructDefs());
            addTypesWithNoRefResolve(parent.getAllClassificationDefs());
            addTypesWithNoRefResolve(parent.getAllEntityDefs());
            addTypesWithNoRefResolve(parent.getAllRelationshipDefs());
            addTypesWithNoRefResolve(parent.getAllBusinessMetadataDefs());

            addedTypes.clear();
            updatedTypes.clear();
            deletedTypes.clear();
        }

        private void resolveReferences() throws BaseException {
            for (Type type : registryData.allTypes.getAllTypes()) {
                type.resolveReferences(this);
            }

            for (Type type : registryData.allTypes.getAllTypes()) {
                type.resolveReferencesPhase2(this);
            }

            for (Type type : registryData.allTypes.getAllTypes()) {
                type.resolveReferencesPhase3(this);
            }
        }

        public void clear() {
            registryData.clear();
        }

        public void addType(BaseTypeDef typeDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.addType({})", typeDef);
            }

            if (typeDef != null) {
                addTypeWithNoRefResolve(typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.addType({})", typeDef);
            }
        }

        public void updateGuid(String typeName, String guid) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateGuid({}, {})", typeName, guid);
            }

            registryData.updateGuid(typeName, guid);

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateGuid({}, {})", typeName, guid);
            }
        }

        public void addTypes(Collection<? extends BaseTypeDef> typeDefs) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.addTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                addTypesWithNoRefResolve(typeDefs);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.addTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }
        }

        public void addTypes(TypesDef typesDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.addTypes({})", typesDef);
            }

            if (typesDef != null) {
                addTypesWithNoRefResolve(typesDef.getEnumDefs());
                addTypesWithNoRefResolve(typesDef.getStructDefs());
                addTypesWithNoRefResolve(typesDef.getClassificationDefs());
                addTypesWithNoRefResolve(typesDef.getEntityDefs());
                addTypesWithNoRefResolve(typesDef.getRelationshipDefs());
                addTypesWithNoRefResolve(typesDef.getBusinessMetadataDefs());
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.addTypes({})", typesDef);
            }
        }

        public void updateType(BaseTypeDef typeDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateType({})", typeDef);
            }

            if (typeDef != null) {
                updateTypeWithNoRefResolve(typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateType({})", typeDef);
            }
        }

        public void updateTypeByGuid(String guid, BaseTypeDef typeDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypeByGuid({})", guid);
            }

            if (guid != null && typeDef != null) {
                updateTypeByGuidWithNoRefResolve(guid, typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypeByGuid({})", guid);
            }
        }

        public void updateTypeByName(String name, BaseTypeDef typeDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateEnumDefByName({})", name);
            }

            if (name != null && typeDef != null) {
                updateTypeByNameWithNoRefResolve(name, typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateEnumDefByName({})", name);
            }
        }

        public void updateTypes(Collection<? extends BaseTypeDef> typeDefs) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                updateTypesWithNoRefResolve(typeDefs);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }
        }

        public void updateTypes(TypesDef typesDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypes({})", typesDef);
            }

            if (typesDef != null) {
                updateTypesWithNoRefResolve(typesDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypes({})", typesDef);
            }
        }

        public void updateTypesWithNoRefResolve(TypesDef typesDef) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypesWithNoRefResolve({})", typesDef);
            }

            if (typesDef != null) {
                updateTypesWithNoRefResolve(typesDef.getEnumDefs());
                updateTypesWithNoRefResolve(typesDef.getStructDefs());
                updateTypesWithNoRefResolve(typesDef.getClassificationDefs());
                updateTypesWithNoRefResolve(typesDef.getEntityDefs());
                updateTypesWithNoRefResolve(typesDef.getRelationshipDefs());
                updateTypesWithNoRefResolve(typesDef.getBusinessMetadataDefs());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypesWithNoRefResolve({})", typesDef);
            }
        }

        public void removeTypesDef(TypesDef typesDef) throws BaseException {
            if (null != typesDef && !typesDef.isEmpty()) {
                removeTypesWithNoRefResolve(typesDef.getEnumDefs());
                removeTypesWithNoRefResolve(typesDef.getStructDefs());
                removeTypesWithNoRefResolve(typesDef.getClassificationDefs());
                removeTypesWithNoRefResolve(typesDef.getEntityDefs());
                removeTypesWithNoRefResolve(typesDef.getRelationshipDefs());
                removeTypesWithNoRefResolve(typesDef.getBusinessMetadataDefs());
            }

            resolveReferences();
        }

        private void removeTypesWithNoRefResolve(Collection<? extends BaseTypeDef> typeDefs) {
            if (CollectionUtils.isNotEmpty(typeDefs)) {
                for (BaseTypeDef typeDef : typeDefs) {
                    if (StringUtils.isNotEmpty(typeDef.getGuid())) {
                        removeTypeByGuidWithNoRefResolve(typeDef);
                    } else {
                        removeTypeByNameWithNoRefResolve(typeDef);
                    }
                }
            }
        }

        private void removeTypeByNameWithNoRefResolve(BaseTypeDef typeDef) {
            switch (typeDef.getCategory()) {
                case ENUM:
                    registryData.enumDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case STRUCT:
                    registryData.structDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case CLASSIFICATION:
                    registryData.classificationDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case ENTITY:
                    registryData.entityDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case RELATIONSHIP:
                    registryData.relationshipDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case BUSINESS_METADATA:
                    registryData.businessMetadataDefs.removeTypeDefByName(typeDef.getName());
                    break;
            }
            deletedTypes.add(typeDef);
        }

        private void removeTypeByGuidWithNoRefResolve(BaseTypeDef typeDef) {
            switch (typeDef.getCategory()) {
                case ENUM:
                    registryData.enumDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case STRUCT:
                    registryData.structDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case CLASSIFICATION:
                    registryData.classificationDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case ENTITY:
                    registryData.entityDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case RELATIONSHIP:
                    registryData.relationshipDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case BUSINESS_METADATA:
                    registryData.businessMetadataDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
            }
            deletedTypes.add(typeDef);
        }

        public void removeTypeByGuid(String guid) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.removeTypeByGuid({})", guid);
            }

            BaseTypeDef typeDef = getTypeDefByGuid(guid);

            if (guid != null) {
                registryData.removeByGuid(guid);
            }

            resolveReferences();

            if (typeDef != null) {
                deletedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.removeTypeByGuid({})", guid);
            }
        }

        public void removeTypeByName(String name) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.removeTypeByName({})", name);
            }

            BaseTypeDef typeDef = getTypeDefByName(name);

            if (name != null) {
                registryData.removeByName(name);
            }

            resolveReferences();

            if (typeDef != null) {
                deletedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.removeEnumDefByName({})", name);
            }
        }

        public List<BaseTypeDef> getAddedTypes() { return addedTypes; }

        public List<BaseTypeDef> getUpdatedTypes() { return updatedTypes; }

        public List<BaseTypeDef> getDeleteedTypes() { return deletedTypes; }


        private void addTypeWithNoRefResolve(BaseTypeDef typeDef) throws BaseException{
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.addTypeWithNoRefResolve({})", typeDef);
            }

            if (typeDef != null) {
                if (typeDef.getClass().equals(EnumDef.class)) {
                    EnumDef enumDef = (EnumDef) typeDef;

                    registryData.enumDefs.addType(enumDef, new EnumType(enumDef));
                } else if (typeDef.getClass().equals(StructDef.class)) {
                    StructDef structDef = (StructDef) typeDef;

                    registryData.structDefs.addType(structDef, new StructType(structDef));
                } else if (typeDef.getClass().equals(ClassificationDef.class)) {
                    ClassificationDef classificationDef = (ClassificationDef) typeDef;

                    registryData.classificationDefs.addType(classificationDef,
                            new ClassificationType(classificationDef));
                } else if (typeDef.getClass().equals(EntityDef.class)) {
                    EntityDef entityDef = (EntityDef) typeDef;

                    registryData.entityDefs.addType(entityDef, new EntityType(entityDef));
                } else if (typeDef.getClass().equals(RelationshipDef.class)) {
                    RelationshipDef relationshipDef = (RelationshipDef) typeDef;

                    registryData.relationshipDefs.addType(relationshipDef, new RelationshipType(relationshipDef));
                } else if (typeDef.getClass().equals(BusinessMetadataDef.class)) {
                    BusinessMetadataDef businessMetadataDef = (BusinessMetadataDef) typeDef;
                    registryData.businessMetadataDefs.addType(businessMetadataDef, new BusinessMetadataType(businessMetadataDef));
                }

                addedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.addTypeWithNoRefResolve({})", typeDef);
            }
        }

        private void addTypesWithNoRefResolve(Collection<? extends BaseTypeDef> typeDefs) throws BaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.addTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                for (BaseTypeDef typeDef : typeDefs) {
                    addTypeWithNoRefResolve(typeDef);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.addTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }
        }

        private void updateTypeWithNoRefResolve(BaseTypeDef typeDef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateType({})", typeDef);
            }

            if (typeDef != null) {
                if (StringUtils.isNotBlank(typeDef.getGuid())) {
                    updateTypeByGuidWithNoRefResolve(typeDef.getGuid(), typeDef);
                } else if (StringUtils.isNotBlank(typeDef.getName())) {
                    updateTypeByNameWithNoRefResolve(typeDef.getName(), typeDef);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateType({})", typeDef);
            }
        }

        private void updateTypeByGuidWithNoRefResolve(String guid, BaseTypeDef typeDef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypeByGuidWithNoRefResolve({})", guid);
            }

            if (guid != null && typeDef != null) {
                // ignore
                if (typeDef.getClass().equals(EnumDef.class)) {
                    EnumDef enumDef = (EnumDef) typeDef;

                    registryData.enumDefs.removeTypeDefByGuid(guid);
                    registryData.enumDefs.addType(enumDef, new EnumType(enumDef));
                } else if (typeDef.getClass().equals(StructDef.class)) {
                    StructDef structDef = (StructDef) typeDef;

                    registryData.structDefs.removeTypeDefByGuid(guid);
                    registryData.structDefs.addType(structDef, new StructType(structDef));
                } else if (typeDef.getClass().equals(ClassificationDef.class)) {
                    ClassificationDef classificationDef = (ClassificationDef) typeDef;

                    registryData.classificationDefs.removeTypeDefByGuid(guid);
                    registryData.classificationDefs.addType(classificationDef,
                            new ClassificationType(classificationDef));
                } else if (typeDef.getClass().equals(EntityDef.class)) {
                    EntityDef entityDef = (EntityDef) typeDef;

                    registryData.entityDefs.removeTypeDefByGuid(guid);
                    registryData.entityDefs.addType(entityDef, new EntityType(entityDef));
                } else if (typeDef.getClass().equals(RelationshipDef.class)) {
                    RelationshipDef relationshipDef = (RelationshipDef) typeDef;

                    registryData.relationshipDefs.removeTypeDefByGuid(guid);
                    registryData.relationshipDefs.addType(relationshipDef, new RelationshipType(relationshipDef));
                } else if (typeDef.getClass().equals(BusinessMetadataDef.class)) {
                    BusinessMetadataDef businessMetadataDef = (BusinessMetadataDef) typeDef;

                    registryData.businessMetadataDefs.removeTypeDefByGuid(guid);
                    registryData.businessMetadataDefs.addType(businessMetadataDef, new BusinessMetadataType(businessMetadataDef));
                }

                updatedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypeByGuidWithNoRefResolve({})", guid);
            }
        }

        private void updateTypeByNameWithNoRefResolve(String name, BaseTypeDef typeDef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypeByNameWithNoRefResolve({})", name);
            }

            if (name != null && typeDef != null) {
                if (typeDef.getClass().equals(EnumDef.class)) {
                    EnumDef enumDef = (EnumDef) typeDef;

                    registryData.enumDefs.removeTypeDefByName(name);
                    registryData.enumDefs.addType(enumDef, new EnumType(enumDef));
                } else if (typeDef.getClass().equals(StructDef.class)) {
                    StructDef structDef = (StructDef) typeDef;

                    registryData.structDefs.removeTypeDefByName(name);
                    registryData.structDefs.addType(structDef, new StructType(structDef));
                } else if (typeDef.getClass().equals(ClassificationDef.class)) {
                    ClassificationDef classificationDef = (ClassificationDef) typeDef;

                    registryData.classificationDefs.removeTypeDefByName(name);
                    registryData.classificationDefs.addType(classificationDef,
                            new ClassificationType(classificationDef));
                } else if (typeDef.getClass().equals(EntityDef.class)) {
                    EntityDef entityDef = (EntityDef) typeDef;

                    registryData.entityDefs.removeTypeDefByName(name);
                    registryData.entityDefs.addType(entityDef, new EntityType(entityDef));
                } else if (typeDef.getClass().equals(RelationshipDef.class)) {
                    RelationshipDef relationshipDef = (RelationshipDef) typeDef;

                    registryData.relationshipDefs.removeTypeDefByName(name);
                    registryData.relationshipDefs.addType(relationshipDef, new RelationshipType(relationshipDef));
                } else if (typeDef.getClass().equals(BusinessMetadataDef.class)) {
                    BusinessMetadataDef businessMetadataDef = (BusinessMetadataDef) typeDef;

                    registryData.businessMetadataDefs.removeTypeDefByName(name);
                    registryData.businessMetadataDefs.addType(businessMetadataDef, new BusinessMetadataType(businessMetadataDef));
                }

                updatedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypeByNameWithNoRefResolve({})", name);
            }
        }

        private void updateTypesWithNoRefResolve(Collection<? extends BaseTypeDef> typeDefs) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> TypeRegistry.updateTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                for (BaseTypeDef typeDef : typeDefs) {
                    updateTypeWithNoRefResolve(typeDef);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== TypeRegistry.updateTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }
        }
    }

    static class TypeRegistryUpdateSynchronizer {
        private final TypeRegistry typeRegistry;
        private final ReentrantLock typeRegistryUpdateLock;
        private TransientTypeRegistry typeRegistryUnderUpdate = null;
        private String lockedByThread = null;

        TypeRegistryUpdateSynchronizer(io.simforce.bytezard.metadata.type.TypeRegistry typeRegistry) {
            this.typeRegistry = typeRegistry;
            this.typeRegistryUpdateLock = new ReentrantLock();
        }

        TransientTypeRegistry lockTypeRegistryForUpdate(int lockMaxWaitTimeInSeconds) throws BaseException {
            LOG.debug("==> lockTypeRegistryForUpdate()");

            boolean alreadyLockedByCurrentThread = typeRegistryUpdateLock.isHeldByCurrentThread();

            if (!alreadyLockedByCurrentThread) {
                if (lockedByThread != null) {
                    LOG.info("lockTypeRegistryForUpdate(): waiting for lock to be released by thread {}", lockedByThread);
                }
            } else {
                LOG.warn("lockTypeRegistryForUpdate(): already locked. currentLockCount={}",
                        typeRegistryUpdateLock.getHoldCount());
            }

            try {
                boolean isLocked = typeRegistryUpdateLock.tryLock(lockMaxWaitTimeInSeconds, TimeUnit.SECONDS);

                if (!isLocked) {
                    throw new BaseException(ErrorCode.FAILED_TO_OBTAIN_TYPE_UPDATE_LOCK);
                }
            } catch (InterruptedException excp) {
                throw new BaseException(ErrorCode.FAILED_TO_OBTAIN_TYPE_UPDATE_LOCK, excp);
            }

            if (!alreadyLockedByCurrentThread) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("lockTypeRegistryForUpdate(): wait over..got the lock");
                }

                typeRegistryUnderUpdate = new TransientTypeRegistry(typeRegistry);
                lockedByThread          = Thread.currentThread().getName();
            }

            LOG.debug("<== lockTypeRegistryForUpdate()");

            return typeRegistryUnderUpdate;
        }

        void releaseTypeRegistryForUpdate(TransientTypeRegistry ttr, boolean commitUpdates) {
            LOG.debug("==> releaseTypeRegistryForUpdate()");

            if (typeRegistryUpdateLock.isHeldByCurrentThread()) {
                try {
                    if (typeRegistryUnderUpdate != ttr) {
                        LOG.error("releaseTypeRegistryForUpdate(): incorrect typeRegistry returned for release" +
                                        ": found=" + ttr + "; expected=" + typeRegistryUnderUpdate,
                                new Exception().fillInStackTrace());
                    } else if (typeRegistryUpdateLock.getHoldCount() == 1) {
                        if (ttr != null && commitUpdates) {
                            // copy indexName for attributes from current typeRegistry to new one
                            copyIndexNameFromCurrent(ttr.getAllEntityTypes());
                            copyIndexNameFromCurrent(ttr.getAllBusinessMetadataTypes());

                            typeRegistry.registryData = ttr.registryData;
                        }
                    }

                    if (typeRegistryUpdateLock.getHoldCount() == 1) {
                        lockedByThread          = null;
                        typeRegistryUnderUpdate = null;
                    } else {
                        LOG.warn("releaseTypeRegistryForUpdate(): pendingReleaseCount={}", typeRegistryUpdateLock.getHoldCount() - 1);
                    }
                } finally {
                    typeRegistryUpdateLock.unlock();
                }
            } else {
                LOG.error("releaseTypeRegistryForUpdate(): current thread does not hold the lock",
                        new Exception().fillInStackTrace());
            }

            LOG.debug("<== releaseTypeRegistryForUpdate()");
        }

        private void copyIndexNameFromCurrent(Collection<? extends StructType> ttrTypes) {
            for (StructType ttrType : ttrTypes) {
                final StructType currType;

                if (ttrType instanceof EntityType) {
                    currType = typeRegistry.getEntityTypeByName(ttrType.getTypeName());
                } else if (ttrType instanceof BusinessMetadataType) {
                    currType = typeRegistry.getBusinessMetadataTypeByName(ttrType.getTypeName());
                } else {
                    currType = null;
                }

                if (currType == null) { // ttrType could be a new type introduced
                    continue;
                }

                for (Attribute ttrAttribute : ttrType.getAllAttributes().values()) {
                    if (StringUtils.isEmpty(ttrAttribute.getIndexFieldName())) {
                        Attribute currAttribute = currType.getAttribute(ttrAttribute.getName());

                        if (currAttribute != null) {
                            ttrAttribute.setIndexFieldName(currAttribute.getIndexFieldName());
                        }
                    }
                }
            }

        }
    }
}

class TypeCache {
    private final Map<String, Type> typeGuidMap;
    private final Map<String, Type> typeNameMap;
    private final Set<String> serviceTypes;

    public TypeCache() {
        typeGuidMap  = new ConcurrentHashMap<>();
        typeNameMap  = new ConcurrentHashMap<>();
        serviceTypes = new HashSet<>();
    }

    public TypeCache(TypeCache other) {
        typeGuidMap  = new ConcurrentHashMap<>(other.typeGuidMap);
        typeNameMap  = new ConcurrentHashMap<>(other.typeNameMap);
        serviceTypes = new HashSet<>(other.serviceTypes);
    }

    public void addType(Type type) {
        if (type != null) {
            if (StringUtils.isNotEmpty(type.getTypeName())) {
                typeNameMap.put(type.getTypeName(), type);
            }

            if (StringUtils.isNotEmpty(type.getServiceType())) {
                serviceTypes.add(type.getServiceType());
            }
        }
    }

    public void addType(BaseTypeDef typeDef, Type type) {
        if (typeDef != null && type != null) {
            if (StringUtils.isNotEmpty(typeDef.getGuid())) {
                typeGuidMap.put(typeDef.getGuid(), type);
            }

            if (StringUtils.isNotEmpty(typeDef.getName())) {
                typeNameMap.put(typeDef.getName(), type);
            }

            if (StringUtils.isNotEmpty(type.getServiceType())) {
                serviceTypes.add(type.getServiceType());
            }
        }
    }

    public boolean isKnownType(String typeName) {
        return typeNameMap.containsKey(typeName);
    }

    public Collection<String> getAllTypeNames() {
        return Collections.unmodifiableCollection(typeNameMap.keySet());
    }

    public Collection<Type> getAllTypes() {
        return Collections.unmodifiableCollection(typeNameMap.values());
    }

    public Set<String> getAllServiceTypes() {
        return Collections.unmodifiableSet(serviceTypes);
    }

    public Type getTypeByGuid(String guid) {
        return guid != null ? typeGuidMap.get(guid) : null;
    }

    public Type getTypeByName(String name) {
        return name != null ? typeNameMap.get(name) : null;
    }

    public void updateGuid(String typeName, String currGuid, String newGuid) {
        if (currGuid != null) {
            typeGuidMap.remove(currGuid);
        }

        if (typeName != null && newGuid != null) {
            Type type = typeNameMap.get(typeName);

            if (type != null) {
                typeGuidMap.put(newGuid, type);
            }
        }
    }

    public void removeTypeByGuid(String guid) {
        if (guid != null) {
            typeGuidMap.remove(guid);
        }
    }

    public void removeTypeByName(String name) {
        if (name != null) {
            typeNameMap.remove(name);
        }
    }

    public void clear() {
        typeGuidMap.clear();
        typeNameMap.clear();
    }
}

class TypeDefCache<T1 extends BaseTypeDef, T2 extends Type> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TypeDefCache.class);

    private final io.simforce.bytezard.metadata.type.TypeCache typeCache;
    private final Map<String, T1> typeDefGuidMap;
    private final Map<String, T1> typeDefNameMap;
    private final Map<String, T2> typeNameMap;

    public TypeDefCache(TypeCache typeCache) {
        this.typeCache      = typeCache;
        this.typeDefGuidMap = new ConcurrentHashMap<>();
        this.typeDefNameMap = new ConcurrentHashMap<>();
        this.typeNameMap    = new ConcurrentHashMap<>();
    }

    public TypeDefCache(TypeDefCache other, TypeCache typeCache) {
        this.typeCache      = typeCache;
        this.typeDefGuidMap = new ConcurrentHashMap<>(other.typeDefGuidMap);
        this.typeDefNameMap = new ConcurrentHashMap<>(other.typeDefNameMap);
        this.typeNameMap    = new ConcurrentHashMap<>(other.typeNameMap);
    }

    public void addType(T1 typeDef, T2 type) {
        if (typeDef != null && type != null) {
            if (StringUtils.isNotEmpty(typeDef.getGuid())) {
                typeDefGuidMap.put(typeDef.getGuid(), typeDef);
            }

            if (StringUtils.isNotEmpty(typeDef.getName())) {
                typeDefNameMap.put(typeDef.getName(), typeDef);
                typeNameMap.put(typeDef.getName(), type);
            }

            typeCache.addType(typeDef, type);
        }
    }

    public Collection<T1> getAll() {
        return Collections.unmodifiableCollection(typeDefNameMap.values());
    }

    public Collection<String> getAllNames() { return Collections.unmodifiableCollection(typeDefNameMap.keySet()); }

    public T1 getTypeDefByGuid(String guid) {
        return guid != null ? typeDefGuidMap.get(guid) : null;
    }

    public T1 getTypeDefByName(String name) {
        return name != null ? typeDefNameMap.get(name) : null;
    }

    public Collection<T2> getAllTypes() {
        return Collections.unmodifiableCollection(typeNameMap.values());
    }

    public T2 getTypeByName(String name) {
        return name != null ? typeNameMap.get(name) : null;
    }

    public void updateGuid(String typeName, String newGuid) {
        if (typeName != null) {
            T1 typeDef = typeDefNameMap.get(typeName);

            if (typeDef != null) {
                String currGuid = typeDef.getGuid();
                if (!typeDefGuidMap.containsKey(newGuid) || !StringUtils.equals(currGuid, newGuid)) {
                    if(LOG.isDebugEnabled()) {
                        if (!typeDefGuidMap.containsKey(newGuid)) {
                            LOG.debug("TypeDefGuidMap doesn't contain entry for guid {}. Adding new entry", newGuid);
                        } else {
                            LOG.debug("Removing entry for guid {} and adding entry for guid {}", currGuid, newGuid);
                        }
                    }
                    if (currGuid != null) {
                        typeDefGuidMap.remove(currGuid);
                    }

                    typeDef.setGuid(newGuid);

                    if (newGuid != null) {
                        typeDefGuidMap.put(newGuid, typeDef);
                    }

                    typeCache.updateGuid(typeName, currGuid, newGuid);
                }
            }
        }
    }

    public void removeTypeDefByGuid(String guid) {
        if (guid != null) {
            T1 typeDef = typeDefGuidMap.remove(guid);

            typeCache.removeTypeByGuid(guid);

            String name = typeDef != null ? typeDef.getName() : null;

            if (name != null) {
                typeDefNameMap.remove(name);
                typeNameMap.remove(name);
                typeCache.removeTypeByName(name);
            }

        }
    }

    public void removeTypeDefByName(String name) {
        if (name != null) {
            T1 typeDef = typeDefNameMap.remove(name);

            typeNameMap.remove(name);
            typeCache.removeTypeByName(name);

            String guid = typeDef != null ? typeDef.getGuid() : null;

            if (guid != null) {
                typeDefGuidMap.remove(guid);
                typeCache.removeTypeByGuid(guid);
            }
        }
    }

    public void clear() {
        typeCache.clear();
        typeDefGuidMap.clear();
        typeDefNameMap.clear();
        typeNameMap.clear();
    }

}
