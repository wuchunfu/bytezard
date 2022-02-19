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

import static io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef.ATTR_OPTION_APPLICABLE_ENTITY_TYPES;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.type.BusinessMetadataType;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.StructType;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;
import io.simforce.bytezard.metadata.utils.Json;

public class BusinessMetadataDefStore extends AbstractDefStore<BusinessMetadataDef> {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessMetadataDefStore.class);

    @Inject
    public BusinessMetadataDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        super(typeDefStore, typeRegistry);
    }

    @Override
    public Vertex preCreate(BusinessMetadataDef businessMetadataDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.preCreate({})", businessMetadataDef);
        }

        validateType(businessMetadataDef);

        Type type = typeRegistry.getType(businessMetadataDef.getName());

        if (type.getTypeCategory() != TypeCategory.BUSINESS_METADATA) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, businessMetadataDef.getName(),
                    TypeCategory.BUSINESS_METADATA.name());
        }

        Vertex ret = typeDefStore.findTypeVertexByName(businessMetadataDef.getName());

        if (ret != null) {
            throw new BaseException(ErrorCode.TYPE_ALREADY_EXISTS, businessMetadataDef.getName());
        }

        ret = typeDefStore.createTypeVertex(businessMetadataDef);

        updateVertexPreCreate(businessMetadataDef, (BusinessMetadataType) type, ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.preCreate({}): {}", businessMetadataDef, ret);
        }

        return ret;
    }

    @Override
    public void validateType(BaseTypeDef typeDef) throws BaseException {
        super.validateType(typeDef);
        BusinessMetadataDef businessMetadataDef = (BusinessMetadataDef) typeDef;
        if (CollectionUtils.isNotEmpty(businessMetadataDef.getAttributeDefs())) {
            for (StructDef.AttributeDef attributeDef : businessMetadataDef.getAttributeDefs()) {
                if (!isValidName(attributeDef.getName())) {
                    throw new BaseException(ErrorCode.ATTRIBUTE_NAME_INVALID_CHARS, attributeDef.getName());
                }
            }
        }
    }

    @Override
    public BusinessMetadataDef create(BusinessMetadataDef businessMetadataDef, Vertex preCreateResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.create({}, {})", businessMetadataDef, preCreateResult);
        }

        verifyAttributeTypeReadAccess(businessMetadataDef.getAttributeDefs());

        if (CollectionUtils.isNotEmpty(businessMetadataDef.getAttributeDefs())) {
            BusinessMetadataType businessMetadataType = typeRegistry.getBusinessMetadataTypeByName(businessMetadataDef.getName());
            for (StructType.Attribute attribute : businessMetadataType.getAllAttributes().values()) {
                BusinessMetadataType.BusinessAttribute bmAttribute = (BusinessMetadataType.BusinessAttribute) attribute;
                verifyTypesReadAccess(bmAttribute.getApplicableEntityTypes());
            }
        }


        Vertex vertex = (preCreateResult == null) ? preCreate(businessMetadataDef) : preCreateResult;

        BusinessMetadataDef ret = toBusinessMetadataDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.create({}, {}): {}", businessMetadataDef, preCreateResult, ret);
        }

        return ret;
    }

    @Override
    public List<BusinessMetadataDef> getAll() throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDef.getAll()");
        }

        List<BusinessMetadataDef> ret = new ArrayList<>();

        Iterator<Vertex> vertices = typeDefStore.findTypeVerticesByCategory(TypeCategory.BUSINESS_METADATA);
        while (vertices.hasNext()) {
            ret.add(toBusinessMetadataDef(vertices.next()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.getAll(): count={}", ret.size());
        }
        return ret;
    }

    @Override
    public BusinessMetadataDef getByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.getByName({})", name);
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.BUSINESS_METADATA);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

//        vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, String.class);

        BusinessMetadataDef ret = toBusinessMetadataDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.getByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public BusinessMetadataDef getByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.getByGuid({})", guid);
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.BUSINESS_METADATA);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        BusinessMetadataDef ret = toBusinessMetadataDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.getByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public BusinessMetadataDef update(BusinessMetadataDef typeDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.update({})", typeDef);
        }

        verifyAttributeTypeReadAccess(typeDef.getAttributeDefs());

        if (CollectionUtils.isNotEmpty(typeDef.getAttributeDefs())) {
            BusinessMetadataType businessMetadataType = typeRegistry.getBusinessMetadataTypeByName(typeDef.getName());
            for (StructType.Attribute attribute : businessMetadataType.getAllAttributes().values()) {
                BusinessMetadataType.BusinessAttribute bmAttribute = (BusinessMetadataType.BusinessAttribute) attribute;
                verifyTypesReadAccess(bmAttribute.getApplicableEntityTypes());
            }
        }

        validateType(typeDef);

        BusinessMetadataDef ret = StringUtils.isNotBlank(typeDef.getGuid()) ? updateByGuid(typeDef.getGuid(), typeDef)
                : updateByName(typeDef.getName(), typeDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.update({}): {}", typeDef, ret);
        }

        return ret;
    }

    @Override
    public BusinessMetadataDef updateByName(String name, BusinessMetadataDef typeDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.updateByName({}, {})", name, typeDef);
        }

        BusinessMetadataDef existingDef = typeRegistry.getBusinessMetadataDefByName(name);

        validateType(typeDef);

        Type type = typeRegistry.getType(typeDef.getName());

        if (type.getTypeCategory() != TypeCategory.BUSINESS_METADATA) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, typeDef.getName(), TypeCategory.BUSINESS_METADATA.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.BUSINESS_METADATA);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }


        updateVertexPreUpdate(typeDef, (BusinessMetadataType)type, vertex);

        BusinessMetadataDef ret = toBusinessMetadataDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.updateByName({}, {}): {}", name, typeDef, ret);
        }

        return ret;
    }

    @Override
    public BusinessMetadataDef updateByGuid(String guid, BusinessMetadataDef typeDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.updateByGuid({})", guid);
        }

        BusinessMetadataDef existingDef   = typeRegistry.getBusinessMetadataDefByGuid(guid);

        validateType(typeDef);

        Type type = typeRegistry.getTypeByGuid(guid);

        if (type.getTypeCategory() != TypeCategory.BUSINESS_METADATA) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, typeDef.getName(), TypeCategory.BUSINESS_METADATA.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.BUSINESS_METADATA);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        updateVertexPreUpdate(typeDef, (BusinessMetadataType)type, vertex);

        BusinessMetadataDef ret = toBusinessMetadataDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.updateByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.preDeleteByName({})", name);
        }

        BusinessMetadataDef existingDef = typeRegistry.getBusinessMetadataDefByName(name);

        Vertex ret = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.BUSINESS_METADATA);

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        checkBusinessMetadataRef(existingDef.getName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.preDeleteByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> BusinessMetadataDefStoreV2.preDeleteByGuid({})", guid);
        }

        BusinessMetadataDef existingDef = typeRegistry.getBusinessMetadataDefByGuid(guid);

        Vertex ret = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.BUSINESS_METADATA);

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        checkBusinessMetadataRef(existingDef.getName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== BusinessMetadataDefStoreV2.preDeleteByGuid({}): ret={}", guid, ret);
        }

        return ret;
    }

    private void updateVertexPreCreate(BusinessMetadataDef businessMetadataDef, BusinessMetadataType businessMetadataType,
                                       Vertex vertex) throws BaseException {
       StructDefStore.updateVertexPreCreate(businessMetadataDef, businessMetadataType, vertex, typeDefStore);
    }

    private void updateVertexPreUpdate(BusinessMetadataDef businessMetadataDef, BusinessMetadataType businessMetadataType,
                                       Vertex vertex) throws BaseException {
        // Load up current struct definition for matching attributes
        BusinessMetadataDef currentBusinessMetadataDef = toBusinessMetadataDef(vertex);

        // Check to verify that in an update call we only allow addition of new entity types, not deletion of existing
        // entity types
        if (CollectionUtils.isNotEmpty(businessMetadataDef.getAttributeDefs())) {
            for (StructDef.AttributeDef attributeDef : businessMetadataDef.getAttributeDefs()) {
                String updatedApplicableEntityTypesString = attributeDef.getOption(ATTR_OPTION_APPLICABLE_ENTITY_TYPES);
                Set<String> updatedApplicableEntityTypes = StringUtils.isBlank(updatedApplicableEntityTypesString) ? null : Type.fromJson(updatedApplicableEntityTypesString, Set.class);

                StructDef.AttributeDef existingAttribute = currentBusinessMetadataDef.getAttribute(attributeDef.getName());
                if (existingAttribute != null) {
                    String existingApplicableEntityTypesString = existingAttribute.getOption(ATTR_OPTION_APPLICABLE_ENTITY_TYPES);
                    Set<String> existingApplicableEntityTypes = StringUtils.isBlank(existingApplicableEntityTypesString) ? null : Type.fromJson(existingApplicableEntityTypesString, Set.class);

                    if (existingApplicableEntityTypes != null) {
                        if (!updatedApplicableEntityTypes.containsAll(existingApplicableEntityTypes)) {
                            throw new BaseException(ErrorCode.APPLICABLE_ENTITY_TYPES_DELETION_NOT_SUPPORTED,
                                    attributeDef.getName(), businessMetadataDef.getName());
                        }
                    }
                }
            }
        }

       StructDefStore.updateVertexPreUpdate(businessMetadataDef, businessMetadataType, vertex, typeDefStore);
    }

    private BusinessMetadataDef toBusinessMetadataDef(Vertex vertex) throws BaseException {
        BusinessMetadataDef ret = null;

        if (vertex != null && typeDefStore.isTypeVertex(vertex, TypeCategory.BUSINESS_METADATA)) {
            ret = new BusinessMetadataDef();

            StructDefStore.toStructDef(vertex, ret, typeDefStore);
        }

        return ret;
    }

    private void checkBusinessMetadataRef(String typeName) throws BaseException {
        BusinessMetadataDef businessMetadataDef = typeRegistry.getBusinessMetadataDefByName(typeName);
        if (businessMetadataDef != null) {
            List<StructDef.AttributeDef> attributeDefs = businessMetadataDef.getAttributeDefs();

            for (StructDef.AttributeDef attributeDef : attributeDefs) {
                String      qualifiedName       = StructType.Attribute.getQualifiedAttributeName(businessMetadataDef, attributeDef.getName());
                String      vertexPropertyName  = StructType.Attribute.generateVertexPropertyName(businessMetadataDef, attributeDef, qualifiedName);
                Set<String> applicableTypes     = Json.fromJson(attributeDef.getOption(ATTR_OPTION_APPLICABLE_ENTITY_TYPES), Set.class);

                if (isBusinessAttributePresent(vertexPropertyName, applicableTypes)) {
                    throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, typeName);
                }
            }
        }
    }

    private boolean isBusinessAttributePresent(String attrName, Set<String> applicableTypes) throws BaseException {
//        SearchParameters.FilterCriteria criteria = new SearchParameters.FilterCriteria();
//        criteria.setAttributeName(attrName);
//        criteria.setOperator(SearchParameters.Operator.NOT_EMPTY);
//
//        SearchParameters.FilterCriteria entityFilters = new SearchParameters.FilterCriteria();
//        entityFilters.setCondition(SearchParameters.FilterCriteria.Condition.OR);
//        entityFilters.setCriterion(Collections.singletonList(criteria));
//
//        SearchParameters searchParameters = new SearchParameters();
//        searchParameters.setTypeName(String.join(SearchContext.TYPENAME_DELIMITER, applicableTypes));
//        searchParameters.setExcludeDeletedEntities(true);
//        searchParameters.setIncludeSubClassifications(false);
//        searchParameters.setEntityFilters(entityFilters);
//        searchParameters.setAttributes(Collections.singleton(attrName));
//        searchParameters.setOffset(0);
//        searchParameters.setLimit(1);
//
//        SearchResult SearchResult = entityDiscoveryService.searchWithParameters(searchParameters);
//
//        return CollectionUtils.isNotEmpty(SearchResult.getEntities());
        return true;
    }

}
