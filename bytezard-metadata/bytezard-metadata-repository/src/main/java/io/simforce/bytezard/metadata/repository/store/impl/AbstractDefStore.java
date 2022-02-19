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
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collection;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.repository.store.DefStore;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;

public abstract class AbstractDefStore<T extends BaseTypeDef> implements DefStore<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDefStore.class);

    protected final TypeDefGraphStore typeDefStore;
    protected final TypeRegistry typeRegistry;

    private static final String  NAME_REGEX            = "[a-zA-Z][a-zA-Z0-9_ ]*";
    private static final String  INTERNAL_NAME_REGEX   = "__" + NAME_REGEX;
    private static final Pattern NAME_PATTERN          = Pattern.compile(NAME_REGEX);
    private static final Pattern INTERNAL_NAME_PATTERN = Pattern.compile(INTERNAL_NAME_REGEX);

    public static final String ALLOW_RESERVED_KEYWORDS = ".types.allowReservedKeywords";

    public AbstractDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        this.typeDefStore = typeDefStore;
        this.typeRegistry = typeRegistry;
    }

    public void verifyTypesReadAccess(Collection<? extends Type> types) throws BaseException {
        if (CollectionUtils.isNotEmpty(types)) {
            for (Type type : types) {
                BaseTypeDef def = typeRegistry.getTypeDefByName(type.getTypeName());
            }
        }
    }

    public void verifyTypeReadAccess(Collection<String> types) throws BaseException {
        if (CollectionUtils.isNotEmpty(types)) {
            for (String type : types) {
                BaseTypeDef def = typeRegistry.getTypeDefByName(type);
            }
        }
    }

    public void verifyTypeReadAccess(String type) throws BaseException {
        if (StringUtils.isNotEmpty(type)) {
                BaseTypeDef def = typeRegistry.getTypeDefByName(type);
        }
    }

    public void verifyAttributeTypeReadAccess(Collection<StructDef.AttributeDef> types) throws BaseException {
        if (CollectionUtils.isNotEmpty(types)) {
            for (StructDef.AttributeDef attributeDef : types) {
                BaseTypeDef def = typeRegistry.getTypeDefByName(attributeDef.getTypeName());
            }
        }
    }

    public void validateType(BaseTypeDef typeDef) throws BaseException {
        if (!isValidName(typeDef.getName())) {
            throw new BaseException(ErrorCode.TYPE_NAME_INVALID_FORMAT, typeDef.getName(), typeDef.getCategory().name());
        }

//        try {
//            final boolean allowReservedKeywords = ApplicationProperties.get().getBoolean(ALLOW_RESERVED_KEYWORDS, true);
//
//            if (!allowReservedKeywords && typeDef instanceof StructDef) {
//                final List<StructDef.AttributeDef> attributeDefs = ((StructDef) typeDef).getAttributeDefs();
//                for (StructDef.AttributeDef attrDef : attributeDefs) {
//                    if (DSL.Parser.isKeyword(attrDef.getName())) {
//                        throw new BaseException(ErrorCode.ATTRIBUTE_NAME_INVALID, attrDef.getName(), typeDef.getCategory().name());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            LOG.error("Exception while loading configuration ", e);
//            throw new BaseException(ErrorCode.INTERNAL_ERROR, "Could not load configuration");
//        }
    }

    public boolean isValidName(String typeName) {
        return NAME_PATTERN.matcher(typeName).matches() || INTERNAL_NAME_PATTERN.matcher(typeName).matches();
    }

    @Override
    public void deleteByName(String name, Vertex preDeleteResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AbstractDefStoreV1.deleteByName({}, {})", name, preDeleteResult);
        }

        Vertex vertex = (preDeleteResult == null) ? preDeleteByName(name) : preDeleteResult;

        typeDefStore.deleteTypeVertex(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AbstractDefStoreV1.deleteByName({}, {})", name, preDeleteResult);
        }
    }

    @Override
    public void deleteByGuid(String guid, Vertex preDeleteResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AbstractDefStoreV1.deleteByGuid({}, {})", guid, preDeleteResult);
        }

        Vertex vertex = (preDeleteResult == null) ? preDeleteByGuid(guid) : preDeleteResult;

        typeDefStore.deleteTypeVertex(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AbstractDefStoreV1.deleteByGuid({}, {})", guid, preDeleteResult);
        }
    }
}
