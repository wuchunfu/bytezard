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

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.repository.utils.GraphUtils;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.EntityType;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;

/**
 * EntityDef store in v1 format.
 */
public class EntityDefStore extends AbstractDefStore<EntityDef> {
    private static final Logger LOG = LoggerFactory.getLogger(EntityDefStore.class);

    @Inject
    public EntityDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        super(typeDefStore, typeRegistry);
    }

    @Override
    public Vertex preCreate(EntityDef entityDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.preCreate({})", entityDef);
        }

        validateType(entityDef);

        Type type = typeRegistry.getType(entityDef.getName());

        if (type.getTypeCategory() != TypeCategory.ENTITY) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, entityDef.getName(), TypeCategory.CLASS.name());
        }

        verifyAttributeTypeReadAccess(entityDef.getAttributeDefs());
        
        Vertex ret = typeDefStore.findTypeVertexByName(entityDef.getName());

        if (ret != null) {
            throw new BaseException(ErrorCode.TYPE_ALREADY_EXISTS, entityDef.getName());
        }

        ret = typeDefStore.createTypeVertex(entityDef);

        updateVertexPreCreate(entityDef, (EntityType)type, ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.preCreate({}): {}", entityDef, ret);
        }

        return ret;
    }

    @Override
    public EntityDef create(EntityDef entityDef, Vertex preCreateResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.create({}, {})", entityDef, preCreateResult);
        }

        Vertex vertex = (preCreateResult == null) ? preCreate(entityDef) : preCreateResult;

        updateVertexAddReferences(entityDef, vertex);

        EntityDef ret = toEntityDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.create({}, {}): {}", entityDef, preCreateResult, ret);
        }

        return ret;
    }

    @Override
    public List<EntityDef> getAll() throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.getAll()");
        }

        List<EntityDef>  ret      = new ArrayList<>();
        Iterator<Vertex> vertices = typeDefStore.findTypeVerticesByCategory(TypeCategory.CLASS);

        while (vertices.hasNext()) {
            ret.add(toEntityDef(vertices.next()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.getAll(): count={}", ret.size());
        }

        return ret;
    }

    @Override
    public EntityDef getByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.getByName({})", name);
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.CLASS);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

//        vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

        EntityDef ret = toEntityDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.getByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public EntityDef getByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.getByGuid({})", guid);
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.CLASS);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        EntityDef ret = toEntityDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.getByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public EntityDef update(EntityDef entityDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.update({})", entityDef);
        }

        verifyAttributeTypeReadAccess(entityDef.getAttributeDefs());

        validateType(entityDef);

        EntityDef ret = StringUtils.isNotBlank(entityDef.getGuid()) ? updateByGuid(entityDef.getGuid(), entityDef)
                                                                         : updateByName(entityDef.getName(), entityDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.update({}): {}", entityDef, ret);
        }

        return ret;
    }

    @Override
    public EntityDef updateByName(String name, EntityDef entityDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.updateByName({}, {})", name, entityDef);
        }

        EntityDef existingDef = typeRegistry.getEntityDefByName(name);

        validateType(entityDef);

        Type type = typeRegistry.getType(entityDef.getName());

        if (type.getTypeCategory() != TypeCategory.ENTITY) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, entityDef.getName(), TypeCategory.CLASS.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.CLASS);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        updateVertexPreUpdate(entityDef, (EntityType)type, vertex);
        updateVertexAddReferences(entityDef, vertex);

        EntityDef ret = toEntityDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.updateByName({}, {}): {}", name, entityDef, ret);
        }

        return ret;
    }

    @Override
    public EntityDef updateByGuid(String guid, EntityDef entityDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.updateByGuid({})", guid);
        }

        EntityDef existingDef = typeRegistry.getEntityDefByGuid(guid);

        validateType(entityDef);

        Type type = typeRegistry.getTypeByGuid(guid);

        if (type.getTypeCategory() != TypeCategory.ENTITY) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, entityDef.getName(), TypeCategory.CLASS.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.CLASS);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        updateVertexPreUpdate(entityDef, (EntityType)type, vertex);
        updateVertexAddReferences(entityDef, vertex);

        EntityDef ret = toEntityDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.updateByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.preDeleteByName({})", name);
        }

        EntityDef existingDef = typeRegistry.getEntityDefByName(name);

        Vertex ret = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.CLASS);

        if (GraphUtils.typeHasInstanceVertex(typeDefStore.getGraph(), name)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, name);
        }

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        // error if we are trying to delete an entityDef that has a relationshipDef
        if (typeDefStore.hasIncomingEdgesWithLabel(ret, GraphUtils.RELATIONSHIPTYPE_EDGE_LABEL)){
            throw new BaseException(ErrorCode.TYPE_HAS_RELATIONSHIPS, name);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.preDeleteByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EntityDefStoreV1.preDeleteByGuid({})", guid);
        }

        EntityDef existingDef = typeRegistry.getEntityDefByGuid(guid);

        Vertex ret = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.CLASS);

        String typeName = GraphUtils.getEncodedProperty(ret, Constants.TYPENAME_PROPERTY_KEY, String.class);

        if (GraphUtils.typeHasInstanceVertex(typeDefStore.getGraph(), typeName)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, typeName);
        }

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        // error if we are trying to delete an entityDef that has a relationshipDef
        if (typeDefStore.hasIncomingEdgesWithLabel(ret, GraphUtils.RELATIONSHIPTYPE_EDGE_LABEL)){
            throw new BaseException(ErrorCode.TYPE_HAS_RELATIONSHIPS, typeName);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV1.preDeleteByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    private void updateVertexPreCreate(EntityDef entityDef, EntityType entityType, Vertex vertex) throws BaseException {
        StructDefStore.updateVertexPreCreate(entityDef, entityType, vertex, typeDefStore);
    }

    private void updateVertexPreUpdate(EntityDef entityDef, EntityType entityType, Vertex vertex)
        throws BaseException {
        StructDefStore.updateVertexPreUpdate(entityDef, entityType, vertex, typeDefStore);
    }

    private void updateVertexAddReferences(EntityDef  entityDef, Vertex vertex) throws BaseException {
        StructDefStore.updateVertexAddReferences(entityDef, vertex, typeDefStore);

        typeDefStore.createSuperTypeEdges(vertex, entityDef.getSuperTypes(), TypeCategory.CLASS);
    }

    private EntityDef toEntityDef(Vertex vertex) throws BaseException {
        EntityDef ret = null;

        if (vertex != null && typeDefStore.isTypeVertex(vertex, TypeCategory.CLASS)) {
            ret = new EntityDef();

            StructDefStore.toStructDef(vertex, ret, typeDefStore);

            ret.setSuperTypes(typeDefStore.getSuperTypeNames(vertex));
        }

        return ret;
    }
}
