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

import static io.simforce.bytezard.metadata.type.Constants.TYPENAME_PROPERTY_KEY;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.repository.utils.GraphUtils;
import io.simforce.bytezard.metadata.type.ClassificationType;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;

/**
 * ClassificationDef store in v1 format.
 */
class ClassificationDefStore extends AbstractDefStore<ClassificationDef> {
    private static final Logger LOG = LoggerFactory.getLogger(ClassificationDefStore.class);

    private static final String  TRAIT_NAME_REGEX   = "[a-zA-Z[^\\p{ASCII}]][a-zA-Z0-9[^\\p{ASCII}]_ .]*";

    private static final Pattern TRAIT_NAME_PATTERN = Pattern.compile(TRAIT_NAME_REGEX);

    public ClassificationDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        super(typeDefStore, typeRegistry);
    }

    @Override
    public Vertex preCreate(ClassificationDef classificationDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.preCreate({})", classificationDef);
        }

        validateType(classificationDef);

        Type type = typeRegistry.getType(classificationDef.getName());

        if (type.getTypeCategory() != TypeCategory.CLASSIFICATION) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, classificationDef.getName(), TypeCategory.TRAIT.name());
        }

        verifyTypeReadAccess(classificationDef.getSuperTypes());
        verifyTypeReadAccess(classificationDef.getEntityTypes());

        Vertex ret = typeDefStore.findTypeVertexByName(classificationDef.getName());

        if (ret != null) {
            throw new BaseException(ErrorCode.TYPE_ALREADY_EXISTS, classificationDef.getName());
        }

        ret = typeDefStore.createTypeVertex(classificationDef);

        updateVertexPreCreate(classificationDef, (ClassificationType)type, ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.preCreate({}): {}", classificationDef, ret);
        }

        return ret;
    }

    @Override
    public ClassificationDef create(ClassificationDef classificationDef, Vertex preCreateResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.create({}, {})", classificationDef, preCreateResult);
        }

        Vertex vertex = (preCreateResult == null) ? preCreate(classificationDef) : preCreateResult;

        updateVertexAddReferences(classificationDef, vertex);

        ClassificationDef ret = toClassificationDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.create({}, {}): {}", classificationDef, preCreateResult, ret);
        }

        return ret;
    }

    @Override
    public List<ClassificationDef> getAll() throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.getAll()");
        }

        List<ClassificationDef> ret = new ArrayList<>();

        Iterator<Vertex> vertices = typeDefStore.findTypeVerticesByCategory(TypeCategory.TRAIT);
        while (vertices.hasNext()) {
            ret.add(toClassificationDef(vertices.next()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.getAll(): count={}", ret.size());
        }
        return ret;
    }

    @Override
    public ClassificationDef getByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.getByName({})", name);
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.TRAIT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

//        vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

        ClassificationDef ret = toClassificationDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.getByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public ClassificationDef getByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.getByGuid({})", guid);
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.TRAIT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        ClassificationDef ret = toClassificationDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.getByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public ClassificationDef update(ClassificationDef classifiDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.update({})", classifiDef);
        }

        verifyTypeReadAccess(classifiDef.getSuperTypes());
        verifyTypeReadAccess(classifiDef.getEntityTypes());

        validateType(classifiDef);

        ClassificationDef ret = StringUtils.isNotBlank(classifiDef.getGuid())
                  ? updateByGuid(classifiDef.getGuid(), classifiDef) : updateByName(classifiDef.getName(), classifiDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.update({}): {}", classifiDef, ret);
        }

        return ret;
    }

    @Override
    public ClassificationDef updateByName(String name, ClassificationDef classificationDef)
        throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.updateByName({}, {})", name, classificationDef);
        }

        ClassificationDef existingDef   = typeRegistry.getClassificationDefByName(name);

        validateType(classificationDef);

        Type type = typeRegistry.getType(classificationDef.getName());

        if (type.getTypeCategory() != TypeCategory.CLASSIFICATION) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, classificationDef.getName(), TypeCategory.TRAIT.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.TRAIT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        updateVertexPreUpdate(classificationDef, (ClassificationType)type, vertex);
        updateVertexAddReferences(classificationDef, vertex);

        ClassificationDef ret = toClassificationDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.updateByName({}, {}): {}", name, classificationDef, ret);
        }

        return ret;
    }

    @Override
    public ClassificationDef updateByGuid(String guid, ClassificationDef classificationDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.updateByGuid({})", guid);
        }

        ClassificationDef existingDef  = typeRegistry.getClassificationDefByGuid(guid);

        validateType(classificationDef);

        Type type = typeRegistry.getTypeByGuid(guid);

        if (type.getTypeCategory() != TypeCategory.CLASSIFICATION) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, classificationDef.getName(), TypeCategory.TRAIT.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.TRAIT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        updateVertexPreUpdate(classificationDef, (ClassificationType)type, vertex);
        updateVertexAddReferences(classificationDef, vertex);

        ClassificationDef ret = toClassificationDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.updateByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.preDeleteByName({})", name);
        }

        ClassificationDef existingDef = typeRegistry.getClassificationDefByName(name);

        Vertex ret = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.TRAIT);

        if (GraphUtils.typeHasInstanceVertex(typeDefStore.getGraph(),name)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, name);
        }

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.preDeleteByName({}): ret=", name, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> ClassificationDefStoreV1.preDeleteByGuid({})", guid);
        }

//        ClassificationDef existingDef = typeRegistry.getClassificationDefByGuid(guid);

        Vertex ret = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.TRAIT);

        String typeName = GraphUtils.getEncodedProperty(ret, TYPENAME_PROPERTY_KEY, String.class);

        if (GraphUtils.typeHasInstanceVertex(typeDefStore.getGraph(),typeName)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, typeName);
        }

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== ClassificationDefStoreV1.preDeleteByGuid({}): ret=", guid, ret);
        }

        return ret;
    }

    private void updateVertexPreCreate(ClassificationDef  classificationDef,
                                       ClassificationType classificationType,
                                       Vertex             vertex) throws BaseException {
       StructDefStore.updateVertexPreCreate(classificationDef, classificationType, vertex, typeDefStore);
    }

    private void updateVertexPreUpdate(ClassificationDef  classificationDef,
                                       ClassificationType classificationType,
                                       Vertex             vertex) throws BaseException {
       StructDefStore.updateVertexPreUpdate(classificationDef, classificationType, vertex, typeDefStore);
    }

    private void updateVertexAddReferences(ClassificationDef classificationDef, Vertex vertex) throws BaseException {
       StructDefStore.updateVertexAddReferences(classificationDef, vertex, typeDefStore);

        typeDefStore.createSuperTypeEdges(vertex, classificationDef.getSuperTypes(), TypeCategory.TRAIT);
        // create edges from this vertex to entity Type vertices with the supplied entity type names
        typeDefStore.createEntityTypeEdges(vertex, classificationDef.getEntityTypes());
    }

    private ClassificationDef toClassificationDef(Vertex vertex) throws BaseException {
        ClassificationDef ret = null;

        if (vertex != null && typeDefStore.isTypeVertex(vertex, TypeCategory.TRAIT)) {
            ret = new ClassificationDef();

            StructDefStore.toStructDef(vertex, ret, typeDefStore);

            ret.setSuperTypes(typeDefStore.getSuperTypeNames(vertex));
            ret.setEntityTypes(typeDefStore.getEntityTypeNames(vertex));
        }

        return ret;
    }

    @Override
    public boolean isValidName(String typeName) {
        Matcher m = TRAIT_NAME_PATTERN.matcher(typeName);

        return m.matches();
    }
}
