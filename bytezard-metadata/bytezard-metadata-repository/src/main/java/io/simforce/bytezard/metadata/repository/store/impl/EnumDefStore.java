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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef.EnumElementDef;
import io.simforce.bytezard.metadata.repository.utils.GraphUtils;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.TypeRegistry;

/**
 * EnumDef store in v2 format.
 */
class EnumDefStore extends AbstractDefStore<EnumDef> {
    private static final Logger LOG = LoggerFactory.getLogger(EnumDefStore.class);

    public EnumDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        super(typeDefStore, typeRegistry);
    }

    @Override
    public Vertex preCreate(EnumDef enumDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
          LOG.debug("==> EnumDefStoreV2.preCreate({})", enumDef);
        }

        validateType(enumDef);

        Vertex vertex = typeDefStore.findTypeVertexByName(enumDef.getName());

        if (vertex != null) {
            throw new BaseException(ErrorCode.TYPE_ALREADY_EXISTS, enumDef.getName());
        }

        vertex = typeDefStore.createTypeVertex(enumDef);

        toVertex(enumDef, vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.preCreate({}): {}", enumDef, vertex);
        }

        return vertex;
    }

    @Override
    public EnumDef create(EnumDef enumDef, Vertex preCreateResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
          LOG.debug("==> EnumDefStoreV2.create({}, {})", enumDef, preCreateResult);
        }


        Vertex vertex = (preCreateResult == null) ? preCreate(enumDef) : preCreateResult;

        EnumDef ret = toEnumDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EntityDefStoreV2.create({}, {}): {}", enumDef, preCreateResult, ret);
        }

        return ret;
    }

    @Override
    public List<EnumDef> getAll() throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EnumDefStoreV2.getAll()");
        }

        List<EnumDef> ret = new ArrayList<>();

        Iterator<Vertex> vertices = typeDefStore.findTypeVerticesByCategory(TypeCategory.ENUM);
        while (vertices.hasNext()) {
            ret.add(toEnumDef(vertices.next()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.getAll(): count={}", ret.size());
        }

        return ret;
    }

    @Override
    public EnumDef getByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EnumDefStoreV2.getByName({})", name);
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.ENUM);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

//        vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

        EnumDef ret = toEnumDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.getByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public EnumDef getByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EnumDefStoreV2.getByGuid({})", guid);
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.ENUM);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        EnumDef ret = toEnumDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.getByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public EnumDef update(EnumDef enumDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EnumDefStoreV2.update({})", enumDef);
        }

        validateType(enumDef);

        EnumDef ret = StringUtils.isNotBlank(enumDef.getGuid()) ? updateByGuid(enumDef.getGuid(), enumDef)
                                                                     : updateByName(enumDef.getName(), enumDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.update({}): {}", enumDef, ret);
        }

        return ret;
    }

    @Override
    public EnumDef updateByName(String name, EnumDef enumDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EnumDefStoreV2.updateByName({}, {})", name, enumDef);
        }

        EnumDef existingDef = typeRegistry.getEnumDefByName(name);

        validateType(enumDef);

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.ENUM);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        typeDefStore.updateTypeVertex(enumDef, vertex);

        toVertex(enumDef, vertex);

        EnumDef ret = toEnumDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.updateByName({}, {}): {}", name, enumDef, ret);
        }

        return ret;
    }

    @Override
    public EnumDef updateByGuid(String guid, EnumDef enumDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> EnumDefStoreV2.updateByGuid({})", guid);
        }

        EnumDef existingDef = typeRegistry.getEnumDefByGuid(guid);

        validateType(enumDef);

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.ENUM);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        typeDefStore.updateTypeVertex(enumDef, vertex);

        toVertex(enumDef, vertex);

        EnumDef ret = toEnumDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== EnumDefStoreV2.updateByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByName(String name) throws BaseException {
        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.ENUM);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        EnumDef existingDef = typeRegistry.getEnumDefByName(name);

        return vertex;
    }

    @Override
    public Vertex preDeleteByGuid(String guid) throws BaseException {
        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.ENUM);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        EnumDef existingDef = typeRegistry.getEnumDefByGuid(guid);

        return vertex;
    }

    private void toVertex(EnumDef enumDef, Vertex vertex) throws BaseException {
        if (CollectionUtils.isEmpty(enumDef.getElementDefs())) {
            throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, enumDef.getName(), "values");
        }

        List<String> values = new ArrayList<>(enumDef.getElementDefs().size());

        for (EnumElementDef element : enumDef.getElementDefs()) {
            // Validate the enum element
            if (StringUtils.isEmpty(element.getValue()) || null == element.getOrdinal()) {
                throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, enumDef.getName(), "elementValue");
            }

            String elemKey = GraphUtils.getTypeDefPropertyKey(enumDef, element.getValue());

            GraphUtils.setProperty(vertex, elemKey, element.getOrdinal());

            if (StringUtils.isNotBlank(element.getDescription())) {
                String descKey = GraphUtils.getTypeDefPropertyKey(elemKey, "description");

                GraphUtils.setProperty(vertex, descKey, element.getDescription());
            }

            values.add(element.getValue());
        }
        GraphUtils.setProperty(vertex, GraphUtils.getTypeDefPropertyKey(enumDef), values);

        String defaultValueKey = GraphUtils.getTypeDefPropertyKey(enumDef, "defaultValue");
        GraphUtils.setProperty(vertex, defaultValueKey, enumDef.getDefaultValue());

    }

    private EnumDef toEnumDef(Vertex vertex) {
        EnumDef ret = null;

        if (vertex != null && typeDefStore.isTypeVertex(vertex, TypeCategory.ENUM)) {
            ret = toEnumDef(vertex, new EnumDef(), typeDefStore);
        }

        return ret;
    }

    private static EnumDef toEnumDef(Vertex vertex, EnumDef enumDef, TypeDefGraphStore typeDefStore) {
        EnumDef ret = enumDef != null ? enumDef : new EnumDef();

        typeDefStore.vertexToTypeDef(vertex, ret);

        List<EnumElementDef> elements = new ArrayList<>();
        Iterator<VertexProperty<Object>> it = vertex.properties(GraphUtils.getTypeDefPropertyKey(ret));

        List<String> elemValues = new ArrayList<>();
        while (it.hasNext()) {
            elemValues.add((String)it.next().value());
        }
        for (String elemValue : elemValues) {
            String elemKey = GraphUtils.getTypeDefPropertyKey(ret, elemValue);
            String descKey = GraphUtils.getTypeDefPropertyKey(elemKey, "description");

            Integer ordinal = GraphUtils.getProperty(vertex, elemKey, Integer.class);
            String  desc    = GraphUtils.getProperty(vertex, descKey, String.class);

            elements.add(new EnumElementDef(elemValue, desc, ordinal));
        }
        ret.setElementDefs(elements);

        String defaultValueKey = GraphUtils.getTypeDefPropertyKey(ret, "defaultValue");
        String defaultValue = GraphUtils.getProperty(vertex, defaultValueKey, String.class);
        ret.setDefaultValue(defaultValue);

        return ret;
    }
}
