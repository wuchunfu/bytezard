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
package io.simforce.bytezard.metadata.repository.store.impl;

import static io.simforce.bytezard.metadata.repository.init.TypeDefStoreInitializer.getTypesToCreate;
import static io.simforce.bytezard.metadata.repository.init.TypeDefStoreInitializer.getTypesToUpdate;
import static io.simforce.bytezard.metadata.repository.utils.GraphUtils.VERTEX_TYPE;
import static io.simforce.bytezard.metadata.type.Constants.TYPE_CATEGORY_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.VERTEX_TYPE_PROPERTY_KEY;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.graph.api.GraphFactory;
import io.simforce.bytezard.metadata.model.SearchFilter;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef;
import io.simforce.bytezard.metadata.model.typedef.TypesDef;
import io.simforce.bytezard.metadata.repository.listener.ChangedTypeDefs;
import io.simforce.bytezard.metadata.repository.listener.TypeDefChangeListener;
import io.simforce.bytezard.metadata.repository.store.DefStore;
import io.simforce.bytezard.metadata.repository.store.TypeDefStore;
import io.simforce.bytezard.metadata.repository.utils.GraphUtils;
import io.simforce.bytezard.metadata.type.BusinessMetadataType;
import io.simforce.bytezard.metadata.type.ClassificationType;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.EntityType;
import io.simforce.bytezard.metadata.type.EnumType;
import io.simforce.bytezard.metadata.type.RelationshipType;
import io.simforce.bytezard.metadata.type.StructType;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;
import io.simforce.bytezard.metadata.type.TypeRegistry.TransientTypeRegistry;
import io.simforce.bytezard.metadata.type.TypeUtil;
import io.simforce.bytezard.metadata.utils.FilterUtil;

/**
 * Abstract class for graph persistence store for TypeDef
 */
public abstract class TypeDefGraphStore implements TypeDefStore {

    private static final Logger LOG = LoggerFactory.getLogger(TypeDefGraphStore.class);

    public static final String WILDCARD_CLASSIFICATIONS = "*";
    public static final String ALL_CLASSIFICATIONS      = "_CLASSIFIED";
    public static final String NO_CLASSIFICATIONS       = "_NOT_CLASSIFIED";
    public static final String ALL_ENTITY_TYPES         = "_ALL_ENTITY_TYPES";
    public static final String ALL_CLASSIFICATION_TYPES = "_ALL_CLASSIFICATION_TYPES";

    private final TypeRegistry typeRegistry;
    private final Set<TypeDefChangeListener> typeDefChangeListeners;
    private final int typeUpdateLockMaxWaitTimeSeconds;
    private final Graph graph;
    private final GraphTraversalSource g;

    protected TypeDefGraphStore(Graph graph,
                                TypeRegistry typeRegistry,
                                Set<TypeDefChangeListener> typeDefChangeListeners) {
        this.graph = graph;
        this.g = graph.traversal();
        this.typeRegistry = typeRegistry;
        this.typeDefChangeListeners = typeDefChangeListeners;
        this.typeUpdateLockMaxWaitTimeSeconds = 10000;
    }

    public Graph getGraph() {
        return graph;
    }

    protected abstract DefStore<EnumDef> getEnumDefStore(TypeRegistry typeRegistry);

    protected abstract DefStore<StructDef> getStructDefStore(TypeRegistry typeRegistry);

    protected abstract DefStore<ClassificationDef> getClassificationDefStore(TypeRegistry typeRegistry);

    protected abstract DefStore<EntityDef> getEntityDefStore(TypeRegistry typeRegistry);

    protected abstract DefStore<RelationshipDef> getRelationshipDefStore(TypeRegistry typeRegistry);

    protected abstract DefStore<BusinessMetadataDef> getBusinessMetadataDefStore(TypeRegistry typeRegistry);

    public TypeRegistry getTypeRegistry() { return typeRegistry; }

    @Override
    public void init() throws BaseException {
        LOG.info("==> TypeDefGraphStore.init()");

        TransientTypeRegistry ttr = null;
        boolean commitUpdates = false;

        try {
            ttr = typeRegistry.lockTypeRegistryForUpdate(typeUpdateLockMaxWaitTimeSeconds);

            ttr.clear();

            TypesDef typesDef = new TypesDef(getEnumDefStore(ttr).getAll(),
                    getStructDefStore(ttr).getAll(),
                    getClassificationDefStore(ttr).getAll(),
                    getEntityDefStore(ttr).getAll(),
                    getRelationshipDefStore(ttr).getAll(),
                    getBusinessMetadataDefStore(ttr).getAll());

            rectifyTypeErrorsIfAny(typesDef);

            ttr.addTypes(typesDef);

            commitUpdates = true;
        } finally {
            typeRegistry.releaseTypeRegistryForUpdate(ttr, commitUpdates);

            LOG.info("<== TypeDefGraphStore.init()");
        }
    }

    @Override
    public EnumDef getEnumDefByName(String name) throws BaseException {
        EnumDef ret = typeRegistry.getEnumDefByName(name);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }
        return ret;
    }

    @Override
    public EnumDef getEnumDefByGuid(String guid) throws BaseException {
        EnumDef ret = typeRegistry.getEnumDefByGuid(guid);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }
        return ret;
    }

    @Override
    public EnumDef updateEnumDefByName(String name, EnumDef enumDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();

        tryUpdateByName(name, enumDef, ttr);

        return getEnumDefStore(ttr).updateByName(name, enumDef);
    }

    @Override
    public EnumDef updateEnumDefByGuid(String guid, EnumDef enumDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByGUID(guid, enumDef, ttr);
        return getEnumDefStore(ttr).updateByGuid(guid, enumDef);
    }

    @Override
    public StructDef getStructDefByName(String name) throws BaseException {
        StructDef ret = typeRegistry.getStructDefByName(name);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }
        return ret;
    }

    @Override
    public StructDef getStructDefByGuid(String guid) throws BaseException {
        StructDef ret = typeRegistry.getStructDefByGuid(guid);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }
        return ret;
    }

    @Override
    public RelationshipDef getRelationshipDefByName(String name) throws BaseException {
        RelationshipDef ret = typeRegistry.getRelationshipDefByName(name);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }
        return ret;
    }

    @Override
    public RelationshipDef getRelationshipDefByGuid(String guid) throws BaseException {
        RelationshipDef ret = typeRegistry.getRelationshipDefByGuid(guid);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }
        return ret;
    }

    @Override
    public BusinessMetadataDef getBusinessMetadataDefByName(String name) throws BaseException {
        BusinessMetadataDef ret = typeRegistry.getBusinessMetadataDefByName(name);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }
        return ret;
    }

    @Override
    public BusinessMetadataDef getBusinessMetadataDefByGuid(String guid) throws BaseException {
        BusinessMetadataDef ret = typeRegistry.getBusinessMetadataDefByGuid(guid);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }
        return ret;
    }

    @Override
    public StructDef updateStructDefByName(String name, StructDef structDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByName(name, structDef, ttr);
        return getStructDefStore(ttr).updateByName(name, structDef);
    }

    @Override
    public StructDef updateStructDefByGuid(String guid, StructDef structDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByGUID(guid, structDef, ttr);
        return getStructDefStore(ttr).updateByGuid(guid, structDef);
    }

    @Override
    public ClassificationDef getClassificationDefByName(String name) throws BaseException {
        ClassificationDef ret = typeRegistry.getClassificationDefByName(name);

        if (ret == null) {
            ret = StringUtils.equalsIgnoreCase(name, ALL_CLASSIFICATION_TYPES) ? ClassificationType.getClassificationRoot().getClassificationDef() : null;

            if (ret == null) {
                throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
            }
            return ret;
        }

        return ret;
    }

    @Override
    public ClassificationDef getClassificationDefByGuid(String guid) throws BaseException {
        ClassificationDef ret = typeRegistry.getClassificationDefByGuid(guid);

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        return ret;
    }

    @Override
    public ClassificationDef updateClassificationDefByName(String name, ClassificationDef classificationDef)
            throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();

        tryUpdateByName(name, classificationDef, ttr);

        return getClassificationDefStore(ttr).updateByName(name, classificationDef);
    }

    @Override
    public ClassificationDef updateClassificationDefByGuid(String guid, ClassificationDef classificationDef)
            throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();

        tryUpdateByGUID(guid, classificationDef, ttr);

        return getClassificationDefStore(ttr).updateByGuid(guid, classificationDef);
    }

    @Override
    public EntityDef getEntityDefByName(String name) throws BaseException {
        EntityDef ret = typeRegistry.getEntityDefByName(name);

        if (ret == null) {
            ret = StringUtils.equals(name, ALL_ENTITY_TYPES) ? EntityType.getEntityRoot().getEntityDef() : null;

            if (ret == null) {
                throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
            }
            return ret;
        }
        return ret;
    }

    @Override
    public EntityDef getEntityDefByGuid(String guid) throws BaseException {
        EntityDef ret = typeRegistry.getEntityDefByGuid(guid);
        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }
        return ret;
    }

    @Override
    public EntityDef updateEntityDefByName(String name, EntityDef entityDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByName(name, entityDef, ttr);
        return getEntityDefStore(ttr).updateByName(name, entityDef);
    }

    @Override
    public EntityDef updateEntityDefByGuid(String guid, EntityDef entityDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByGUID(guid, entityDef, ttr);
        return getEntityDefStore(ttr).updateByGuid(guid, entityDef);
    }

    @Override
    public RelationshipDef updateRelationshipDefByName(String name, RelationshipDef relationshipDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByName(name, relationshipDef, ttr);
        return getRelationshipDefStore(ttr).updateByName(name, relationshipDef);
    }

    @Override
    public RelationshipDef updateRelationshipDefByGuid(String guid, RelationshipDef relationshipDef) throws BaseException {
        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryUpdateByGUID(guid, relationshipDef, ttr);
        return getRelationshipDefStore(ttr).updateByGuid(guid, relationshipDef);
    }

    @Override
    public TypesDef createTypesDef(TypesDef typesDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> TypeDefGraphStore.createTypesDef(enums={}, structs={}, classifications={}, entities={}, relationships={}, businessMetadataDefs={})",
                    CollectionUtils.size(typesDef.getEnumDefs()),
                    CollectionUtils.size(typesDef.getStructDefs()),
                    CollectionUtils.size(typesDef.getClassificationDefs()),
                    CollectionUtils.size(typesDef.getEntityDefs()),
                    CollectionUtils.size(typesDef.getRelationshipDefs()),
                    CollectionUtils.size(typesDef.getBusinessMetadataDefs()));
        }

        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();
        tryTypeCreation(typesDef, ttr);


        TypesDef ret = addToGraphStore(typesDef, ttr);

        try {
            ttr.updateTypes(ret);
        } catch (BaseException e) { // this shouldn't happen, as the types were already validated
            LOG.error("failed to update the registry after updating the store", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== TypeDefGraphStore.createTypesDef(enums={}, structs={}, classfications={}, entities={}, relationships={}, businessMetadataDefs={})",
                    CollectionUtils.size(typesDef.getEnumDefs()),
                    CollectionUtils.size(typesDef.getStructDefs()),
                    CollectionUtils.size(typesDef.getClassificationDefs()),
                    CollectionUtils.size(typesDef.getEntityDefs()),
                    CollectionUtils.size(typesDef.getRelationshipDefs()),
                    CollectionUtils.size(typesDef.getBusinessMetadataDefs()));
        }

        return ret;
    }

    @Override
    public TypesDef createUpdateTypesDef(TypesDef typesDef) throws BaseException {
        TypesDef typesToCreate = getTypesToCreate(typesDef, typeRegistry);
        TypesDef typesToUpdate = getTypesToUpdate(typesDef, typeRegistry, false);

        return createUpdateTypesDef(typesToCreate, typesToUpdate);
    }

    @Override
    public TypesDef createUpdateTypesDef(TypesDef typesToCreate, TypesDef typesToUpdate) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> TypeDefGraphStore.createUpdateTypesDef({}, {})", typesToCreate, typesToUpdate);
        }

        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();

        if (!typesToUpdate.isEmpty()) {
            ttr.updateTypesWithNoRefResolve(typesToUpdate);
        }

        // Translate any NOT FOUND errors to BAD REQUEST
        tryTypeCreation(typesToCreate, ttr);

        TypesDef ret = addToGraphStore(typesToCreate, ttr);

        if (!typesToUpdate.isEmpty()) {
            TypesDef updatedTypes = updateGraphStore(typesToUpdate, ttr);

            if (CollectionUtils.isNotEmpty(updatedTypes.getEnumDefs())) {
                for (EnumDef enumDef : updatedTypes.getEnumDefs()) {
                    ret.getEnumDefs().add(enumDef);
                }
            }

            if (CollectionUtils.isNotEmpty(updatedTypes.getStructDefs())) {
                for (StructDef structDef : updatedTypes.getStructDefs()) {
                    ret.getStructDefs().add(structDef);
                }
            }

            if (CollectionUtils.isNotEmpty(updatedTypes.getClassificationDefs())) {
                for (ClassificationDef classificationDef : updatedTypes.getClassificationDefs()) {
                    ret.getClassificationDefs().add(classificationDef);
                }
            }

            if (CollectionUtils.isNotEmpty(updatedTypes.getEntityDefs())) {
                for (EntityDef entityDef : updatedTypes.getEntityDefs()) {
                    ret.getEntityDefs().add(entityDef);
                }
            }
        }

        try {
            ttr.updateTypes(ret);
        } catch (BaseException e) { // this shouldn't happen, as the types were already validated
            LOG.error("failed to update the registry after updating the store", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== TypeDefGraphStore.createUpdateTypesDef({}, {}): {}", typesToCreate, typesToUpdate, ret);
        }

        return ret;
    }

    @Override
    public TypesDef updateTypesDef(TypesDef typesDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> TypeDefGraphStore.updateTypesDef(enums={}, structs={}, classfications={}, entities={}, relationships{}, businessMetadataDefs={})",
                    CollectionUtils.size(typesDef.getEnumDefs()),
                    CollectionUtils.size(typesDef.getStructDefs()),
                    CollectionUtils.size(typesDef.getClassificationDefs()),
                    CollectionUtils.size(typesDef.getEntityDefs()),
                    CollectionUtils.size(typesDef.getRelationshipDefs()),
                    CollectionUtils.size(typesDef.getBusinessMetadataDefs()));
        }

        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();

        // Translate any NOT FOUND errors to BAD REQUEST
        try {
            ttr.updateTypes(typesDef);
        } catch (BaseException e) {
            if (ErrorCode.TYPE_NAME_NOT_FOUND == e.getErrorCode()) {
                throw new BaseException(ErrorCode.BAD_REQUEST, e.getMessage());
            } else {
                throw e;
            }
        }

        TypesDef ret = updateGraphStore(typesDef, ttr);

        try {
            ttr.updateTypes(ret);
        } catch (BaseException e) { // this shouldn't happen, as the types were already validated
            LOG.error("failed to update the registry after updating the store", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== TypeDefGraphStore.updateTypesDef(enums={}, structs={}, classfications={}, entities={}, relationships={}, businessMetadataDefs={})",
                    CollectionUtils.size(typesDef.getEnumDefs()),
                    CollectionUtils.size(typesDef.getStructDefs()),
                    CollectionUtils.size(typesDef.getClassificationDefs()),
                    CollectionUtils.size(typesDef.getEntityDefs()),
                    CollectionUtils.size(typesDef.getRelationshipDefs()),
                    CollectionUtils.size(typesDef.getBusinessMetadataDefs()));
        }

        return ret;

    }

    @Override
    public void deleteTypesDef(TypesDef typesDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> TypeDefGraphStore.deleteTypesDef(enums={}, structs={}, classfications={}, entities={}, relationships={}, businessMetadataDefs={})",
                    CollectionUtils.size(typesDef.getEnumDefs()),
                    CollectionUtils.size(typesDef.getStructDefs()),
                    CollectionUtils.size(typesDef.getClassificationDefs()),
                    CollectionUtils.size(typesDef.getEntityDefs()),
                    CollectionUtils.size(typesDef.getRelationshipDefs()),
                    CollectionUtils.size(typesDef.getBusinessMetadataDefs()));
        }

        TransientTypeRegistry ttr = lockTypeRegistryAndReleasePostCommit();

        DefStore<EnumDef> enumDefStore = getEnumDefStore(ttr);
        DefStore<StructDef> structDefStore = getStructDefStore(ttr);
        DefStore<ClassificationDef> classifiDefStore = getClassificationDefStore(ttr);
        DefStore<EntityDef> entityDefStore = getEntityDefStore(ttr);
        DefStore<RelationshipDef> relationshipDefStore = getRelationshipDefStore(ttr);
        DefStore<BusinessMetadataDef> businessMetadataDefStore = getBusinessMetadataDefStore(ttr);

        List<Vertex> preDeleteStructDefs = new ArrayList<>();
        List<Vertex> preDeleteClassifiDefs = new ArrayList<>();
        List<Vertex> preDeleteEntityDefs = new ArrayList<>();
        List<Vertex> preDeleteRelationshipDefs = new ArrayList<>();

        // pre deletes

        // do the relationships first.
        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                if (StringUtils.isNotBlank(relationshipDef.getGuid())) {
                    preDeleteRelationshipDefs.add(relationshipDefStore.preDeleteByGuid(relationshipDef.getGuid()));
                } else {
                    preDeleteRelationshipDefs.add(relationshipDefStore.preDeleteByName(relationshipDef.getName()));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef structDef : typesDef.getStructDefs()) {
                if (StringUtils.isNotBlank(structDef.getGuid())) {
                    preDeleteStructDefs.add(structDefStore.preDeleteByGuid(structDef.getGuid()));
                } else {
                    preDeleteStructDefs.add(structDefStore.preDeleteByName(structDef.getName()));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef classifiDef : typesDef.getClassificationDefs()) {
                if (StringUtils.isNotBlank(classifiDef.getGuid())) {
                    preDeleteClassifiDefs.add(classifiDefStore.preDeleteByGuid(classifiDef.getGuid()));
                } else {
                    preDeleteClassifiDefs.add(classifiDefStore.preDeleteByName(classifiDef.getName()));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                if (StringUtils.isNotBlank(entityDef.getGuid())) {
                    preDeleteEntityDefs.add(entityDefStore.preDeleteByGuid(entityDef.getGuid()));
                } else {
                    preDeleteEntityDefs.add(entityDefStore.preDeleteByName(entityDef.getName()));
                }
            }
        }

        // run the actual deletes

        // run the relationshipDef delete first - in case there is a enumDef or entityDef dependancy that is going to be deleted.
        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            int i = 0;
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                if (StringUtils.isNotBlank(relationshipDef.getGuid())) {
                    relationshipDefStore.deleteByGuid(relationshipDef.getGuid(), preDeleteRelationshipDefs.get(i));
                } else {
                    relationshipDefStore.deleteByName(relationshipDef.getName(), preDeleteRelationshipDefs.get(i));
                }
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            int i = 0;
            for (StructDef structDef : typesDef.getStructDefs()) {
                if (StringUtils.isNotBlank(structDef.getGuid())) {
                    structDefStore.deleteByGuid(structDef.getGuid(), preDeleteStructDefs.get(i));
                } else {
                    structDefStore.deleteByName(structDef.getName(), preDeleteStructDefs.get(i));
                }
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            int i = 0;
            for (ClassificationDef classifiDef : typesDef.getClassificationDefs()) {
                if (StringUtils.isNotBlank(classifiDef.getGuid())) {
                    classifiDefStore.deleteByGuid(classifiDef.getGuid(), preDeleteClassifiDefs.get(i));
                } else {
                    classifiDefStore.deleteByName(classifiDef.getName(), preDeleteClassifiDefs.get(i));
                }
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            int i = 0;
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                if (StringUtils.isNotBlank(entityDef.getGuid())) {
                    entityDefStore.deleteByGuid(entityDef.getGuid(), preDeleteEntityDefs.get(i));
                } else {
                    entityDefStore.deleteByName(entityDef.getName(), preDeleteEntityDefs.get(i));
                }
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEnumDefs())) {
            for (EnumDef enumDef : typesDef.getEnumDefs()) {
                if (StringUtils.isNotBlank(enumDef.getGuid())) {
                    enumDefStore.deleteByGuid(enumDef.getGuid(), null);
                } else {
                    enumDefStore.deleteByName(enumDef.getName(), null);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                if (StringUtils.isNotBlank(businessMetadataDef.getGuid())) {
                    businessMetadataDefStore.deleteByGuid(businessMetadataDef.getGuid(), null);
                } else {
                    businessMetadataDefStore.deleteByName(businessMetadataDef.getName(), null);
                }
            }
        }

        // Remove all from
        ttr.removeTypesDef(typesDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== TypeDefGraphStore.deleteTypesDef(enums={}, structs={}, classfications={}, entities={})",
                    CollectionUtils.size(typesDef.getEnumDefs()),
                    CollectionUtils.size(typesDef.getStructDefs()),
                    CollectionUtils.size(typesDef.getClassificationDefs()),
                    CollectionUtils.size(typesDef.getEntityDefs()));
        }
    }

    @Override
    public void deleteTypeByName(String typeName) throws BaseException {
        Type Type = typeRegistry.getType(typeName);
        if (Type == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, typeName);
        }

        TypesDef typesDef = new TypesDef();
        BaseTypeDef baseTypeDef = getByNameNoAuthz(typeName);

        if (baseTypeDef instanceof ClassificationDef) {
            typesDef.setClassificationDefs(Collections.singletonList((ClassificationDef) baseTypeDef));
        } else if (baseTypeDef instanceof EntityDef) {
            typesDef.setEntityDefs(Collections.singletonList((EntityDef) baseTypeDef));
        } else if (baseTypeDef instanceof EnumDef) {
            typesDef.setEnumDefs(Collections.singletonList((EnumDef) baseTypeDef));
        } else if (baseTypeDef instanceof RelationshipDef) {
            typesDef.setRelationshipDefs(Collections.singletonList((RelationshipDef) baseTypeDef));
        } else if (baseTypeDef instanceof BusinessMetadataDef) {
            typesDef.setBusinessMetadataDefs(Collections.singletonList((BusinessMetadataDef) baseTypeDef));
        } else if (baseTypeDef instanceof StructDef) {
            typesDef.setStructDefs(Collections.singletonList((StructDef) baseTypeDef));
        }

        deleteTypesDef(typesDef);
    }

    @Override
    public TypesDef searchTypesDef(SearchFilter searchFilter) throws BaseException {
        final TypesDef typesDef = new TypesDef();
        Predicate searchPredicates = FilterUtil.getPredicateFromSearchFilter(searchFilter);

        for(EnumType enumType : typeRegistry.getAllEnumTypes()) {
            if (searchPredicates.evaluate(enumType)) {
                typesDef.getEnumDefs().add(enumType.getEnumDef());
            }
        }

        for(StructType structType : typeRegistry.getAllStructTypes()) {
            if (searchPredicates.evaluate(structType)) {
                typesDef.getStructDefs().add(structType.getStructDef());
            }
        }

        for(ClassificationType classificationType : typeRegistry.getAllClassificationTypes()) {
            if (searchPredicates.evaluate(classificationType)) {
                typesDef.getClassificationDefs().add(classificationType.getClassificationDef());
            }
        }

        for(EntityType entityType : typeRegistry.getAllEntityTypes()) {
            if (searchPredicates.evaluate(entityType)) {
                typesDef.getEntityDefs().add(entityType.getEntityDef());
            }
        }

        for(RelationshipType relationshipType : typeRegistry.getAllRelationshipTypes()) {
            if (searchPredicates.evaluate(relationshipType)) {
                typesDef.getRelationshipDefs().add(relationshipType.getRelationshipDef());
            }
        }

        for(BusinessMetadataType businessMetadataType : typeRegistry.getAllBusinessMetadataTypes()) {
            if (searchPredicates.evaluate(businessMetadataType)) {
                typesDef.getBusinessMetadataDefs().add(businessMetadataType.getBusinessMetadataDef());
            }
        }

        return typesDef;
    }

    @Override
    public BaseTypeDef getByName(String name) throws BaseException {
        if (StringUtils.isBlank(name)) {
            throw new BaseException(ErrorCode.TYPE_NAME_INVALID, "", name);
        }
        Type type = typeRegistry.getType(name);
        BaseTypeDef ret = getTypeDefFromTypeWithNoAuthz(type);

        return ret;
    }

    @Override
    public BaseTypeDef getByGuid(String guid) throws BaseException {
        if (StringUtils.isBlank(guid)) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }
        Type type = typeRegistry.getTypeByGuid(guid);
        BaseTypeDef ret = getTypeDefFromTypeWithNoAuthz(type);

        return ret;
    }

    private BaseTypeDef getByNameNoAuthz(String name) throws BaseException {
        if (StringUtils.isBlank(name)) {
            throw new BaseException(ErrorCode.TYPE_NAME_INVALID, "", name);
        }

        Type type = typeRegistry.getType(name);

        return getTypeDefFromTypeWithNoAuthz(type);
    }

    private BaseTypeDef getTypeDefFromTypeWithNoAuthz(Type type) throws BaseException {
        BaseTypeDef ret;
        switch (type.getTypeCategory()) {
            case ENUM:
                ret = ((EnumType) type).getEnumDef();
                break;
            case STRUCT:
                ret = ((StructType) type).getStructDef();
                break;
            case CLASSIFICATION:
                ret = ((ClassificationType) type).getClassificationDef();
                break;
            case ENTITY:
                ret = ((EntityType) type).getEntityDef();
                break;
            case RELATIONSHIP:
                ret = ((RelationshipType) type).getRelationshipDef();
                break;
            case BUSINESS_METADATA:
                ret = ((BusinessMetadataType) type).getBusinessMetadataDef();
                break;
            case PRIMITIVE:
            case OBJECT_ID_TYPE:
            case ARRAY:
            case MAP:
            default:
                throw new BaseException(ErrorCode.SYSTEM_TYPE, type.getTypeCategory().name());
        }

        return ret;
    }

    private TransientTypeRegistry lockTypeRegistryAndReleasePostCommit() throws BaseException {
        TransientTypeRegistry ttr = typeRegistry.lockTypeRegistryForUpdate(typeUpdateLockMaxWaitTimeSeconds);

//        new TypeRegistryUpdateHook(ttr);

        return ttr;
    }

    private void rectifyTypeErrorsIfAny(TypesDef typesDef) {
        final Set<String> entityNames = new HashSet<>();

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                entityNames.add(entityDef.getName());
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef structDef : typesDef.getStructDefs()) {
                rectifyAttributesIfNeeded(entityNames, structDef);
            }
            removeDuplicateTypeIfAny(typesDef.getStructDefs());
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef classificationDef : typesDef.getClassificationDefs()) {
                rectifyAttributesIfNeeded(entityNames, classificationDef);
            }
            removeDuplicateTypeIfAny(typesDef.getClassificationDefs());
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                rectifyAttributesIfNeeded(entityNames, entityDef);
            }
            removeDuplicateTypeIfAny(typesDef.getEntityDefs());
        }
    }

    private <T extends BaseTypeDef> void removeDuplicateTypeIfAny(List<T> defList) {
        final Set<String> entityDefNames = new HashSet<>();

        for (int i = 0; i < defList.size(); i++) {
            if (!entityDefNames.add((defList.get(i)).getName())) {
                LOG.warn(" Found Duplicate Type => " + defList.get(i).getName());
                defList.remove(i);
                i--;
            }
        }
    }


    private void rectifyAttributesIfNeeded(final Set<String> entityNames, StructDef structDef) {
        List<AttributeDef> attributeDefs = structDef.getAttributeDefs();

        if (CollectionUtils.isNotEmpty(attributeDefs)) {
            for (AttributeDef attributeDef : attributeDefs) {
                if (!hasOwnedReferenceConstraint(attributeDef.getConstraints())) {
                    continue;
                }

                Set<String> referencedTypeNames = TypeUtil.getReferencedTypeNames(attributeDef.getTypeName());

                boolean valid = false;

                for (String referencedTypeName : referencedTypeNames) {
                    if (entityNames.contains(referencedTypeName)) {
                        valid = true;
                        break;
                    }
                }

                if (!valid) {
                    rectifyOwnedReferenceError(structDef, attributeDef);
                }
            }
        }
    }

    private boolean hasOwnedReferenceConstraint(List<ConstraintDef> constraints) {
        if (CollectionUtils.isNotEmpty(constraints)) {
            for (ConstraintDef constraint : constraints) {
                if (constraint.isConstraintType(ConstraintDef.CONSTRAINT_TYPE_OWNED_REF)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void rectifyOwnedReferenceError(StructDef structDef, AttributeDef attributeDef) {
        List<ConstraintDef> constraints = attributeDef.getConstraints();

        if (CollectionUtils.isNotEmpty(constraints)) {
            for (int i = 0; i < constraints.size(); i++) {
                ConstraintDef constraint = constraints.get(i);

                if (constraint.isConstraintType(ConstraintDef.CONSTRAINT_TYPE_OWNED_REF)) {
                    LOG.warn("Invalid constraint ownedRef for attribute {}.{}", structDef.getName(), attributeDef.getName());

                    constraints.remove(i);
                    i--;
                }
            }
        }
    }

    private TypesDef addToGraphStore(TypesDef typesDef, TransientTypeRegistry ttr) throws BaseException {
        TypesDef ret = new TypesDef();

        DefStore<EnumDef> enumDefStore = getEnumDefStore(ttr);
        DefStore<StructDef> structDefStore = getStructDefStore(ttr);
        DefStore<ClassificationDef> classificationDefStore  = getClassificationDefStore(ttr);
        DefStore<EntityDef> entityDefStore = getEntityDefStore(ttr);
        DefStore<RelationshipDef> relationshipDefStore  = getRelationshipDefStore(ttr);
        DefStore<BusinessMetadataDef> businessMetadataDefStore = getBusinessMetadataDefStore(ttr);

        List<Vertex> preCreateStructDefs = new ArrayList<>();
        List<Vertex> preCreateClassificationDefs = new ArrayList<>();
        List<Vertex> preCreateEntityDefs = new ArrayList<>();
        List<Vertex> preCreateRelationshipDefs = new ArrayList<>();
        List<Vertex> preCreateBusinessMetadataDefs = new ArrayList<>();

        // for enumerations run the create
        if (CollectionUtils.isNotEmpty(typesDef.getEnumDefs())) {
            for (EnumDef enumDef : typesDef.getEnumDefs()) {
                EnumDef createdDef = enumDefStore.create(enumDef, null);

                ttr.updateGuid(createdDef.getName(), createdDef.getGuid());

                ret.getEnumDefs().add(createdDef);
            }
        }
        // run the preCreates

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef structDef : typesDef.getStructDefs()) {
                preCreateStructDefs.add(structDefStore.preCreate(structDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef classifiDef : typesDef.getClassificationDefs()) {
                preCreateClassificationDefs.add(classificationDefStore.preCreate(classifiDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                preCreateEntityDefs.add(entityDefStore.preCreate(entityDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                preCreateRelationshipDefs.add(relationshipDefStore.preCreate(relationshipDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                preCreateBusinessMetadataDefs.add(businessMetadataDefStore.preCreate(businessMetadataDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            int i = 0;
            for (StructDef structDef : typesDef.getStructDefs()) {
                StructDef createdDef = structDefStore.create(structDef, preCreateStructDefs.get(i));

                ttr.updateGuid(createdDef.getName(), createdDef.getGuid());

                ret.getStructDefs().add(createdDef);
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            int i = 0;
            for (ClassificationDef classificationDef : typesDef.getClassificationDefs()) {
                ClassificationDef createdDef = classificationDefStore.create(classificationDef, preCreateClassificationDefs.get(i));

                ttr.updateGuid(createdDef.getName(), createdDef.getGuid());

                ret.getClassificationDefs().add(createdDef);
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            int i = 0;
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                EntityDef createdDef = entityDefStore.create(entityDef, preCreateEntityDefs.get(i));

                ttr.updateGuid(createdDef.getName(), createdDef.getGuid());

                ret.getEntityDefs().add(createdDef);
                i++;
            }
        }
        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            int i = 0;
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                RelationshipDef createdDef = relationshipDefStore.create(relationshipDef, preCreateRelationshipDefs.get(i));

                ttr.updateGuid(createdDef.getName(), createdDef.getGuid());

                ret.getRelationshipDefs().add(createdDef);
                i++;
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            int i = 0;
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                BusinessMetadataDef createdDef = businessMetadataDefStore.create(businessMetadataDef, preCreateBusinessMetadataDefs.get(i));

                ttr.updateGuid(createdDef.getName(), createdDef.getGuid());

                ret.getBusinessMetadataDefs().add(createdDef);
                i++;
            }
        }

        return ret;
    }

    private TypesDef updateGraphStore(TypesDef typesDef, TransientTypeRegistry ttr) throws BaseException {
        TypesDef ret = new TypesDef();

        DefStore<EnumDef> enumDefStore = getEnumDefStore(ttr);
        DefStore<StructDef> structDefStore = getStructDefStore(ttr);
        DefStore<ClassificationDef> classificationDefStore = getClassificationDefStore(ttr);
        DefStore<EntityDef> entityDefStore = getEntityDefStore(ttr);
        DefStore<RelationshipDef> relationDefStore = getRelationshipDefStore(ttr);
        DefStore<BusinessMetadataDef> businessMetadataDefStore = getBusinessMetadataDefStore(ttr);

        if (CollectionUtils.isNotEmpty(typesDef.getEnumDefs())) {
            for (EnumDef enumDef : typesDef.getEnumDefs()) {
                ret.getEnumDefs().add(enumDefStore.update(enumDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef structDef : typesDef.getStructDefs()) {
                ret.getStructDefs().add(structDefStore.update(structDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef classificationDef : typesDef.getClassificationDefs()) {
                ret.getClassificationDefs().add(classificationDefStore.update(classificationDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                ret.getEntityDefs().add(entityDefStore.update(entityDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                ret.getRelationshipDefs().add(relationDefStore.update(relationshipDef));
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                ret.getBusinessMetadataDefs().add(businessMetadataDefStore.update(businessMetadataDef));
            }
        }

        return ret;
    }

//    private class TypeRegistryUpdateHook extends GraphTransactionInterceptor.PostTransactionHook {
//
//        private final TransientTypeRegistry ttr;
//
//        private TypeRegistryUpdateHook(TransientTypeRegistry ttr) {
//            super();
//
//            this.ttr = ttr;
//        }
//        @Override
//        public void onComplete(boolean isSuccess) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("==> TypeRegistryUpdateHook.onComplete({})", isSuccess);
//            }
//
//            typeRegistry.releaseTypeRegistryForUpdate(ttr, isSuccess);
//
//            if (isSuccess) {
//                notifyListeners(ttr);
//            }
//
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("<== TypeRegistryUpdateHook.onComplete({})", isSuccess);
//            }
//        }
//
//        private void notifyListeners(TransientTypeRegistry ttr) {
//            if (CollectionUtils.isNotEmpty(typeDefChangeListeners)) {
//                ChangedTypeDefs changedTypeDefs = new ChangedTypeDefs(ttr.getAddedTypes(),
//                        ttr.getUpdatedTypes(),
//                        ttr.getDeleteedTypes());
//
//                for (TypeDefChangeListener changeListener : typeDefChangeListeners) {
//                    try {
//                        changeListener.onChange(changedTypeDefs);
//                    } catch (Throwable t) {
//                        LOG.error("OnChange failed for listener {}", changeListener.getClass().getName(), t);
//                    }
//                }
//            }
//        }
//
//    }

    @Override
    public void notifyLoadCompletion(){
        for (TypeDefChangeListener changeListener : typeDefChangeListeners) {
            try {
                changeListener.onLoadCompletion();
            } catch (Throwable t) {
                LOG.error("OnLoadCompletion failed for listener {}", changeListener.getClass().getName(), t);
            }
        }
    }

    private void tryUpdateByName(String name, BaseTypeDef typeDef, TransientTypeRegistry ttr) throws BaseException {
        try {
            ttr.updateTypeByName(name, typeDef);
        } catch (BaseException e) {
            if (ErrorCode.TYPE_NAME_NOT_FOUND == e.getErrorCode()) {
                throw new BaseException(ErrorCode.BAD_REQUEST, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private void tryUpdateByGUID(String guid, BaseTypeDef typeDef, TransientTypeRegistry ttr) throws BaseException {
        try {
            ttr.updateTypeByGuid(guid, typeDef);
        } catch (BaseException e) {
            if (ErrorCode.TYPE_GUID_NOT_FOUND == e.getErrorCode()) {
                throw new BaseException(ErrorCode.BAD_REQUEST, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private void tryTypeCreation(TypesDef typesDef, TransientTypeRegistry ttr) throws BaseException {
        // Translate any NOT FOUND errors to BAD REQUEST
        try {
            ttr.addTypes(typesDef);
        } catch (BaseException e) {
            if (ErrorCode.TYPE_NAME_NOT_FOUND == e.getErrorCode() ||
                    ErrorCode.TYPE_GUID_NOT_FOUND == e.getErrorCode()) {
                throw new BaseException(ErrorCode.BAD_REQUEST, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @VisibleForTesting
    public Vertex findTypeVertexByName(String typeName) {
        Iterator<Vertex> results = g.V().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                .has(Constants.TYPENAME_PROPERTY_KEY, typeName)
                .toList().iterator();

        return results.hasNext() ?  results.next() : null;
    }

    Vertex findTypeVertexByNameAndCategory(String typeName, TypeCategory category) {
        Iterator<Vertex> results = g.V().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                .has(Constants.TYPENAME_PROPERTY_KEY, typeName)
                .has(TYPE_CATEGORY_PROPERTY_KEY, category)
                .toList().iterator();

        return results.hasNext() ? results.next() : null;
    }

    Vertex findTypeVertexByGuid(String typeGuid) {
        Iterator<Vertex> vertices = graph.traversal().V().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                .has(Constants.GUID_PROPERTY_KEY, typeGuid)
                .toList().iterator();

        return vertices.hasNext() ? vertices.next() : null;
    }

    Vertex findTypeVertexByGuidAndCategory(String typeGuid, TypeCategory category) {
        Iterator<Vertex> vertices = g.V().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                .has(Constants.GUID_PROPERTY_KEY, typeGuid)
                .has(TYPE_CATEGORY_PROPERTY_KEY, category)
                .toList().iterator();

        return vertices.hasNext() ? vertices.next() : null;
    }

    Iterator<Vertex> findTypeVerticesByCategory(TypeCategory category) {
        return (Iterator<Vertex>) g.V().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                .has(TYPE_CATEGORY_PROPERTY_KEY, category)
                .toList().iterator();
    }

    Vertex createTypeVertex(BaseTypeDef typeDef) {
        // Validate all the required checks
        Preconditions.checkArgument(StringUtils.isNotBlank(typeDef.getName()), "Type name can't be null/empty");

        Vertex ret = graph.addVertex();

        if (StringUtils.isBlank(typeDef.getTypeVersion())) {
            typeDef.setTypeVersion("1.0");
        }

        if (typeDef.getVersion() == null) {
            typeDef.setVersion(1L);
        }

        if (StringUtils.isBlank(typeDef.getGuid())) {
            typeDef.setGuid(UUID.randomUUID().toString());
        }

        if (typeDef.getCreateTime() == null) {
            typeDef.setCreateTime(new Date());
        }

        if (typeDef.getUpdateTime() == null) {
            typeDef.setUpdateTime(new Date());
        }

        ret.property(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE); // Mark as type vertex
        ret.property(TYPE_CATEGORY_PROPERTY_KEY, getTypeCategory(typeDef));

        ret.property(Constants.TYPENAME_PROPERTY_KEY, typeDef.getName());
        ret.property(Constants.TYPEDESCRIPTION_PROPERTY_KEY,
                StringUtils.isNotBlank(typeDef.getDescription()) ? typeDef.getDescription() : typeDef.getName());

        if (StringUtils.isNotEmpty(typeDef.getServiceType())) {
            ret.property(Constants.TYPESERVICETYPE_PROPERTY_KEY, typeDef.getServiceType());
        }

        ret.property(Constants.TYPEVERSION_PROPERTY_KEY, typeDef.getTypeVersion());
        ret.property(Constants.GUID_PROPERTY_KEY, typeDef.getGuid());
//        ret.property(Constants.CREATED_BY_KEY, getCurrentUser());
        ret.property(Constants.TIMESTAMP_PROPERTY_KEY, typeDef.getCreateTime().getTime());
//        ret.property(Constants.MODIFIED_BY_KEY, getCurrentUser());
        ret.property(Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY, typeDef.getUpdateTime().getTime());
        ret.property(Constants.VERSION_PROPERTY_KEY, typeDef.getVersion());
        ret.property(Constants.TYPEOPTIONS_PROPERTY_KEY, Type.toJson(typeDef.getOptions()));

        return ret;
    }

    void updateTypeVertex(BaseTypeDef typeDef, Vertex vertex) {
        if (!isTypeVertex(vertex)) {
            LOG.warn("updateTypeVertex(): not a type-vertex - {}", vertex);

            return;
        }

        updateVertexProperty(vertex, Constants.GUID_PROPERTY_KEY, typeDef.getGuid());
        /*
         * rename of a type is supported yet - as the typename is used to in the name of the edges from this vertex
         * To support rename of types, he edge names should be derived from an internal-name - not directly the typename
         *
        updateVertexProperty(vertex, Constants.TYPENAME_PROPERTY_KEY, typeDef.getName());
         */
        updateVertexProperty(vertex, Constants.TYPEDESCRIPTION_PROPERTY_KEY, typeDef.getDescription());
        updateVertexProperty(vertex, Constants.TYPEVERSION_PROPERTY_KEY, typeDef.getTypeVersion());
        updateVertexProperty(vertex, Constants.TYPEOPTIONS_PROPERTY_KEY, Type.toJson(typeDef.getOptions()));

        if (StringUtils.isNotEmpty(typeDef.getServiceType())) {
            updateVertexProperty(vertex, Constants.TYPESERVICETYPE_PROPERTY_KEY, typeDef.getServiceType());
        }

        markVertexUpdated(vertex);
    }

    void deleteTypeVertexOutEdges(Vertex vertex) throws BaseException {
        Iterator<Edge> edges = vertex.edges(Direction.OUT);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (edge != null) {
                edge.remove();
            }
        }
    }

    /**
     * Look to see if there are any IN edges with the supplied label
     * @param vertex
     * @param label
     * @return
     * @throws BaseException
     */
    boolean hasIncomingEdgesWithLabel(Vertex vertex, String label) throws BaseException {
        boolean foundEdges = false;
        Iterator<Edge> inEdges = vertex.edges(Direction.IN);

        while (inEdges.hasNext()) {
            Edge edge = inEdges.next();

            if (label.equals(edge.label())) {
                foundEdges = true;
                break;
            }
        }
        return foundEdges;
    }

    void deleteTypeVertex(Vertex vertex) throws BaseException {
        Iterator<Edge> inEdges = vertex.edges(Direction.IN);
        if (inEdges.hasNext()) {
            String name = (String)vertex.property(Constants.TYPENAME_PROPERTY_KEY).value();
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, name);
        }

        Iterator<Edge> edges = vertex.edges(Direction.OUT);

        while (edges.hasNext()) {
           Edge edge = edges.next();
           if (edge != null) {
               edge.remove();
           }
        }
        vertex.remove();
    }

    void vertexToTypeDef(Vertex vertex, BaseTypeDef typeDef) {
        String name = (String)vertex.property(Constants.TYPENAME_PROPERTY_KEY).value();
        String description = GraphUtils.getProperty(vertex, Constants.TYPEDESCRIPTION_PROPERTY_KEY, String.class);
        String serviceType = GraphUtils.getProperty(vertex, Constants.TYPESERVICETYPE_PROPERTY_KEY, String.class);
        String typeVersion = GraphUtils.getProperty(vertex, Constants.TYPEVERSION_PROPERTY_KEY, String.class);
        String guid = GraphUtils.getProperty(vertex, Constants.GUID_PROPERTY_KEY, String.class);
        String createdBy = GraphUtils.getProperty(vertex, Constants.CREATED_BY_KEY, String.class);
        String updatedBy = GraphUtils.getProperty(vertex, Constants.MODIFIED_BY_KEY, String.class);
        Long createTime = GraphUtils.getProperty(vertex, Constants.TIMESTAMP_PROPERTY_KEY, Long.class);
        Long updateTime = GraphUtils.getProperty(vertex, Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY, Long.class);
        Object versionObj = GraphUtils.getProperty(vertex, Constants.VERSION_PROPERTY_KEY, Object.class);
        String options = GraphUtils.getProperty(vertex, Constants.TYPEOPTIONS_PROPERTY_KEY, String.class);

        Long version = null;

        if(versionObj instanceof Number) {
            version = ((Number)versionObj).longValue();
        } else if (versionObj != null) {
            version = Long.valueOf(versionObj.toString());
        } else {
            version = 0L;
        }

        typeDef.setName(name);
        typeDef.setDescription(description);
        typeDef.setServiceType(serviceType);
        typeDef.setTypeVersion(typeVersion);
        typeDef.setGuid(guid);
        typeDef.setCreatedBy(createdBy);
        typeDef.setUpdatedBy(updatedBy);

        if (createTime != null) {
            typeDef.setCreateTime(new Date(createTime));
        }

        if (updateTime != null) {
            typeDef.setUpdateTime(new Date(updateTime));
        }

        typeDef.setVersion(version);

        if (options != null) {
            typeDef.setOptions(Type.fromJson(options, Map.class));
        }
    }

    boolean isTypeVertex(Vertex vertex) {
        String vertexType = GraphUtils.getProperty(vertex, VERTEX_TYPE_PROPERTY_KEY, String.class);

        return VERTEX_TYPE.equals(vertexType);
    }

    @VisibleForTesting
    public boolean isTypeVertex(Vertex vertex, TypeCategory category) {
        boolean ret = false;

        if (isTypeVertex(vertex)) {
            Object objTypeCategory = GraphUtils.getProperty(vertex, TYPE_CATEGORY_PROPERTY_KEY, Object.class);

            TypeCategory vertexCategory = null;

            if(objTypeCategory instanceof TypeCategory) {
                vertexCategory = (TypeCategory) objTypeCategory;
            } else if (objTypeCategory != null) {
                vertexCategory = TypeCategory.valueOf(objTypeCategory.toString());
            }

            ret = category.equals(vertexCategory);
        }

        return ret;
    }

    boolean isTypeVertex(Vertex vertex, TypeCategory[] categories) {
        boolean ret = false;

        if (isTypeVertex(vertex)) {
            TypeCategory vertexCategory = GraphUtils.getProperty(vertex, TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

            for (TypeCategory category : categories) {
                if (category.equals(vertexCategory)) {
                    ret = true;

                    break;
                }
            }
        }

        return ret;
    }

    Edge getOrCreateEdge(Vertex outVertex, Vertex inVertex, String edgeLabel) {
        Edge ret = null;
        Iterator<Edge> edges = outVertex.edges(Direction.OUT, edgeLabel);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (edge.inVertex().id().equals(inVertex.id())) {
                ret = edge;
                break;
            }
        }

        if (ret == null) {
            ret = addEdge(outVertex, inVertex, edgeLabel);
        }

        return ret;
    }

    Edge addEdge(Vertex outVertex, Vertex inVertex, String edgeLabel) {
        return inVertex.addEdge(edgeLabel,outVertex);
    }

    void removeEdge(Vertex outVertex, Vertex inVertex, String edgeLabel) {
        Iterator<Edge> edges = outVertex.edges(Direction.OUT, edgeLabel);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (edge.inVertex().id().equals(inVertex.id())) {
                edge.remove();
            }
        }
    }

    void createSuperTypeEdges(Vertex vertex, Set<String> superTypes, TypeCategory typeCategory)
            throws BaseException {
        Set<String> currentSuperTypes = getSuperTypeNames(vertex);

        if (CollectionUtils.isNotEmpty(superTypes)) {
            if (! superTypes.containsAll(currentSuperTypes)) {
                throw new BaseException(ErrorCode.SUPERTYPE_REMOVAL_NOT_SUPPORTED);
            }

            for (String superType : superTypes) {
                Vertex superTypeVertex = findTypeVertexByNameAndCategory(superType, typeCategory);

                getOrCreateEdge(vertex, superTypeVertex, GraphUtils.SUPERTYPE_EDGE_LABEL);
            }
        } else if (CollectionUtils.isNotEmpty(currentSuperTypes)) {
            throw new BaseException(ErrorCode.SUPERTYPE_REMOVAL_NOT_SUPPORTED);
        }
    }

    public void createEntityTypeEdges(Vertex classificationVertex, Set<String> entityTypes) throws BaseException {
        Set<String> currentEntityTypes = getEntityTypeNames(classificationVertex);
        String classificationTypeName = GraphUtils.getProperty(classificationVertex,Constants.TYPENAME_PROPERTY_KEY, String.class);

        if (CollectionUtils.isNotEmpty(entityTypes)) {
            if (!entityTypes.containsAll(currentEntityTypes)) {
                throw new BaseException(ErrorCode.ENTITYTYPE_REMOVAL_NOT_SUPPORTED, classificationTypeName);
            }

            for (String entityType : entityTypes) {
                Vertex entityTypeVertex = findTypeVertexByNameAndCategory(entityType, TypeCategory.CLASS);
                if (entityTypeVertex == null) {
                    throw new BaseException(ErrorCode.CLASSIFICATIONDEF_INVALID_ENTITYTYPES, classificationTypeName,entityType);

                }
                getOrCreateEdge(classificationVertex, entityTypeVertex, GraphUtils.ENTITYTYPE_EDGE_LABEL);
            }
        } else if (CollectionUtils.isNotEmpty(currentEntityTypes)) { // remove the restrictions, if present
            for (String entityType : currentEntityTypes) {
                Vertex entityTypeVertex = findTypeVertexByNameAndCategory(entityType, TypeCategory.CLASS);

                if (entityTypeVertex == null) {
                    throw new BaseException(ErrorCode.CLASSIFICATIONDEF_INVALID_ENTITYTYPES, classificationTypeName,entityType);

                }

                removeEdge(classificationVertex, entityTypeVertex, GraphUtils.ENTITYTYPE_EDGE_LABEL);
            }

        }
    }

    Set<String>  getSuperTypeNames(Vertex vertex) {
        return getTypeNamesFromEdges(vertex, GraphUtils.SUPERTYPE_EDGE_LABEL);
    }

    Set<String>  getEntityTypeNames(Vertex vertex) {
        return getTypeNamesFromEdges(vertex, GraphUtils.ENTITYTYPE_EDGE_LABEL);
    }

    /**
     * Get the typename properties from the edges, that are associated with the vertex and have the supplied edge label.
     * @param vertex
     * @param edgeLabel
     * @return set of type names
     */
    private Set<String> getTypeNamesFromEdges(Vertex vertex,String edgeLabel) {
        Set<String> ret = new HashSet<>();
        Iterator<Edge> edges = vertex.edges(Direction.OUT, edgeLabel);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            ret.add(GraphUtils.getProperty(edge.inVertex(), Constants.TYPENAME_PROPERTY_KEY, String.class));
        }

        return ret;
    }

    TypeCategory getTypeCategory(BaseTypeDef typeDef) {
        switch (typeDef.getCategory()) {
            case ENTITY:
                return TypeCategory.CLASS;

            case CLASSIFICATION:
                return TypeCategory.TRAIT;

            case STRUCT:
                return TypeCategory.STRUCT;

            case ENUM:
                return TypeCategory.ENUM;

            case RELATIONSHIP:
                return TypeCategory.RELATIONSHIP;

            case BUSINESS_METADATA:
                return TypeCategory.BUSINESS_METADATA;
        }

        return null;
    }

    /*
     * update the given vertex property, if the new value is not-blank
     */
    private void updateVertexProperty(Vertex vertex, String propertyName, String newValue) {
        if (StringUtils.isNotBlank(newValue)) {
            String currValue = GraphUtils.getProperty(vertex, propertyName, String.class);

            if (!StringUtils.equals(currValue, newValue)) {
                vertex.property(propertyName, newValue);
            }
        }
    }

    /*
     * update the given vertex property, if the new value is not-null
     */
    private void updateVertexProperty(Vertex vertex, String propertyName, Date newValue) {
        if (newValue != null) {
            Number currValue = GraphUtils.getProperty(vertex, propertyName, Number.class);

            if (currValue == null || !currValue.equals(newValue.getTime())) {
                vertex.property(propertyName, newValue.getTime());
            }
        }
    }

    /*
     * increment the version value for this vertex
     */
    private void markVertexUpdated(Vertex vertex) {
        Number currVersion = GraphUtils.getProperty(vertex, Constants.VERSION_PROPERTY_KEY, Number.class);
        long newVersion = currVersion == null ? 1 : (currVersion.longValue() + 1);

//        vertex.property(Constants.MODIFIED_BY_KEY, getCurrentUser());
        vertex.property(Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY, System.currentTimeMillis());
        vertex.property(Constants.VERSION_PROPERTY_KEY, newVersion);
    }

//    public static String getCurrentUser() {
//        return RequestContext.getCurrentUser();
//    }
}
