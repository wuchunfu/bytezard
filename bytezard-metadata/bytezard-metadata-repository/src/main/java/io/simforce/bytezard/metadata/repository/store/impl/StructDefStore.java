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

import static io.simforce.bytezard.metadata.type.StructType.Attribute.encodePropertyKey;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.ConstraintDef;
import io.simforce.bytezard.metadata.repository.utils.GraphUtils;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.RelationshipType;
import io.simforce.bytezard.metadata.type.StructType;
import io.simforce.bytezard.metadata.type.StructType.Attribute;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;
import io.simforce.bytezard.metadata.type.TypeUtil;

/**
 * StructDef store in v1 format.
 */
public class StructDefStore extends AbstractDefStore<StructDef> {
    private static final Logger LOG = LoggerFactory.getLogger(StructDefStore.class);

    public StructDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        super(typeDefStore, typeRegistry);
    }

    @Override
    public Vertex preCreate(StructDef structDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.preCreate({})", structDef);
        }

        validateType(structDef);

        Type type = typeRegistry.getType(structDef.getName());

        if (type.getTypeCategory() != TypeCategory.STRUCT) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, structDef.getName(), TypeCategory.STRUCT.name());
        }
        
        Vertex ret = typeDefStore.findTypeVertexByName(structDef.getName());

        if (ret != null) {
            throw new BaseException(ErrorCode.TYPE_ALREADY_EXISTS, structDef.getName());
        }

        ret = typeDefStore.createTypeVertex(structDef);

        StructDefStore.updateVertexPreCreate(structDef, (StructType)type, ret, typeDefStore);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.preCreate({}): {}", structDef, ret);
        }

        return ret;
    }

    @Override
    public StructDef create(StructDef structDef, Vertex preCreateResult) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.create({}, {})", structDef, preCreateResult);
        }

        verifyAttributeTypeReadAccess(structDef.getAttributeDefs());


        if (CollectionUtils.isEmpty(structDef.getAttributeDefs())) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Missing attributes for structdef");
        }

        Vertex vertex = (preCreateResult == null) ? preCreate(structDef) : preCreateResult;

        StructDefStore.updateVertexAddReferences(structDef, vertex, typeDefStore);

        StructDef ret = toStructDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.create({}, {}): {}", structDef, preCreateResult, ret);
        }

        return ret;
    }

    @Override
    public List<StructDef> getAll() throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.getAll()");
        }

        List<StructDef> ret = new ArrayList<>();

        Iterator<Vertex> vertices = typeDefStore.findTypeVerticesByCategory(TypeCategory.STRUCT);
        while (vertices.hasNext()) {
            ret.add(toStructDef(vertices.next()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.getAll(): count={}", ret.size());
        }
        return ret;
    }

    @Override
    public StructDef getByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.getByName({})", name);
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.STRUCT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

//        vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, String.class);

        StructDef ret = toStructDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.getByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public StructDef getByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.getByGuid({})", guid);
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.STRUCT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        StructDef ret = toStructDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.getByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public StructDef update(StructDef structDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.update({})", structDef);
        }

        verifyAttributeTypeReadAccess(structDef.getAttributeDefs());


        validateType(structDef);

        StructDef ret = StringUtils.isNotBlank(structDef.getGuid()) ? updateByGuid(structDef.getGuid(), structDef)
                                                                         : updateByName(structDef.getName(), structDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.update({}): {}", structDef, ret);
        }

        return ret;
    }

    @Override
    public StructDef updateByName(String name, StructDef structDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.updateByName({}, {})", name, structDef);
        }

        StructDef existingDef = typeRegistry.getStructDefByName(name);

        validateType(structDef);

        Type type = typeRegistry.getType(structDef.getName());

        if (type.getTypeCategory() != TypeCategory.STRUCT) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, structDef.getName(), TypeCategory.STRUCT.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.STRUCT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        StructDefStore.updateVertexPreUpdate(structDef, (StructType)type, vertex, typeDefStore);
        StructDefStore.updateVertexAddReferences(structDef, vertex, typeDefStore);

        StructDef ret = toStructDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.updateByName({}, {}): {}", name, structDef, ret);
        }

        return ret;
    }

    @Override
    public StructDef updateByGuid(String guid, StructDef structDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.updateByGuid({})", guid);
        }

        StructDef existingDef = typeRegistry.getStructDefByGuid(guid);

        validateType(structDef);

        Type type = typeRegistry.getTypeByGuid(guid);

        if (type.getTypeCategory() != TypeCategory.STRUCT) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, structDef.getName(), TypeCategory.STRUCT.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.STRUCT);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        StructDefStore.updateVertexPreUpdate(structDef, (StructType)type, vertex, typeDefStore);
        StructDefStore.updateVertexAddReferences(structDef, vertex, typeDefStore);

        StructDef ret = toStructDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.updateByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.preDeleteByName({})", name);
        }

        StructDef existingDef = typeRegistry.getStructDefByName(name);

        Vertex ret = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.STRUCT);

        if (GraphUtils.typeHasInstanceVertex(typeDefStore.getGraph(), name)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, name);
        }

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.preDeleteByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> StructDefStoreV1.preDeleteByGuid({})", guid);
        }

        StructDef existingDef = typeRegistry.getStructDefByGuid(guid);

        Vertex ret = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.STRUCT);

        String typeName = GraphUtils.getEncodedProperty(ret, Constants.TYPENAME_PROPERTY_KEY, String.class);

        if (GraphUtils.typeHasInstanceVertex(typeDefStore.getGraph(), typeName)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, typeName);
        }

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== StructDefStoreV1.preDeleteByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    private StructDef toStructDef(Vertex vertex) throws BaseException {
        StructDef ret = null;

        if (vertex != null && typeDefStore.isTypeVertex(vertex, TypeCategory.STRUCT)) {
            ret = toStructDef(vertex, new StructDef(), typeDefStore);
        }

        return ret;
    }

    public static void updateVertexPreCreate(StructDef structDef, StructType structType,
                                             Vertex vertex, TypeDefGraphStore typeDefStore) throws BaseException {
        List<String> attrNames = new ArrayList<>(structDef.getAttributeDefs().size());

        for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
            // Validate the mandatory features of an attribute (compatibility with legacy type system)
            if (StringUtils.isEmpty(attributeDef.getName())) {
                throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, structDef.getName(), "name");
            }

            if (StringUtils.isEmpty(attributeDef.getTypeName())) {
                throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, structDef.getName(), "typeName");
            }

            String propertyKey = GraphUtils.getTypeDefPropertyKey(structDef, attributeDef.getName());
            String encodedPropertyKey = GraphUtils.encodePropertyKey(propertyKey);

            vertex.property(encodedPropertyKey, toJsonFromAttribute(structType.getAttribute(attributeDef.getName())));

            attrNames.add(attributeDef.getName());
        }

        String typeNamePropertyKey = GraphUtils.getTypeDefPropertyKey(structDef);
        String encodedtypeNamePropertyKey = GraphUtils.encodePropertyKey(typeNamePropertyKey);

        vertex.property(encodedtypeNamePropertyKey, attrNames);
    }

    public static void updateVertexPreUpdate(StructDef structDef, StructType structType,
                                             Vertex vertex, TypeDefGraphStore typeDefStore)
        throws BaseException {

        List<String> attrNames = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(structDef.getAttributeDefs())) {
            for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                attrNames.add(attributeDef.getName());
            }
        }

        String structDefPropertyKey = GraphUtils.getTypeDefPropertyKey(structDef);
        String encodedStructDefPropertyKey = encodePropertyKey(structDefPropertyKey);
        List<String> currAttrNames = GraphUtils.getProperty(vertex, encodedStructDefPropertyKey, List.class);

        // delete attributes that are not present in updated structDef
        if (CollectionUtils.isNotEmpty(currAttrNames)) {
            List<String> removedAttributes = null;

            for (String currAttrName : currAttrNames) {
                if (!attrNames.contains(currAttrName)) {
//                    if (RequestContext.get().isInTypePatching()) {
//                        String propertyKey = GraphUtils.getTypeDefPropertyKey(structDef, currAttrName);
//
//                        GraphUtils.setProperty(vertex, propertyKey, null);
//
//                        if (removedAttributes == null) {
//                            removedAttributes = new ArrayList<>();
//                        }
//
//                        removedAttributes.add(currAttrName);
//
//                        LOG.warn("REMOVED ATTRIBUTE: {}.{}", structDef.getName(), currAttrName);
//                    } else {
//                        throw new BaseException(ErrorCode.ATTRIBUTE_DELETION_NOT_SUPPORTED,
//                                structDef.getName(), currAttrName);
//                    }
                }
            }

            if (removedAttributes != null) {
                currAttrNames.removeAll(removedAttributes);

                vertex.property(encodedStructDefPropertyKey, currAttrNames);
            }
        }

        typeDefStore.updateTypeVertex(structDef, vertex);

        // Load up current struct definition for matching attributes
        StructDef currentStructDef = toStructDef(vertex, new StructDef(), typeDefStore);

        // add/update attributes that are present in updated structDef
        if (CollectionUtils.isNotEmpty(structDef.getAttributeDefs())) {
            for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
                if (CollectionUtils.isEmpty(currAttrNames) || !currAttrNames.contains(attributeDef.getName())) {

                    // this could have been an attribute removed by a REMOVE_LEGACY_REF_ATTRIBUTE patch
                    // in such case, don't add this attribute; ignore and continue
                    RelationshipType relationship = TypeUtil.findRelationshipWithLegacyRelationshipEnd(structDef.getName(), attributeDef.getName(), typeDefStore.getTypeRegistry());

                    if (relationship != null) {
                        attrNames.remove(attributeDef.getName());

                        LOG.warn("Ignoring attempt to add legacy attribute {}.{}, which is already present in relationship {}", structDef.getName(), attributeDef.getName(), relationship.getTypeName());

                        continue;
                    }

                    // new attribute - allow optional by default or allow mandatory only with typedef patch ADD_MANDATORY_ATTRIBUTE
                    if (!attributeDef.getIsOptional() && !isInAddMandatoryAttributePatch()) {
                        throw new BaseException(ErrorCode.CANNOT_ADD_MANDATORY_ATTRIBUTE, structDef.getName(), attributeDef.getName());
                    }
                }

                // Validate the mandatory features of an attribute (compatibility with legacy type system)
                if (StringUtils.isEmpty(attributeDef.getName())) {
                    throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, structDef.getName(), "name");
                }
                if (StringUtils.isEmpty(attributeDef.getTypeName())) {
                    throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, structDef.getName(), "typeName");
                }

                AttributeDef existingAttribute = currentStructDef.getAttribute(attributeDef.getName());
                if (null != existingAttribute && !attributeDef.getTypeName().equals(existingAttribute.getTypeName())) {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "Data type update for attribute is not supported");
                }

                String propertyKey = GraphUtils.getTypeDefPropertyKey(structDef, attributeDef.getName());

                GraphUtils.setProperty(vertex, propertyKey, toJsonFromAttribute(structType.getAttribute(attributeDef.getName())));
            }
        }

        GraphUtils.setEncodedProperty(vertex, encodedStructDefPropertyKey, attrNames);
    }

    public static boolean isInAddMandatoryAttributePatch() {
        return true;
//        return RequestContext.get().isInTypePatching() &&
//                StringUtils.equals(Constants.TYPEDEF_PATCH_ADD_MANDATORY_ATTRIBUTE, RequestContext.get().getCurrentTypePatchAction());
    }

    public static void updateVertexAddReferences(StructDef structDef, Vertex vertex,
                                                 TypeDefGraphStore typeDefStore) throws BaseException {
        for (AttributeDef attributeDef : structDef.getAttributeDefs()) {
            addReferencesForAttribute(vertex, attributeDef, typeDefStore);
        }
    }

    public static StructDef toStructDef(Vertex vertex, StructDef structDef, TypeDefGraphStore typeDefStore)
                                             throws BaseException {
        StructDef ret = (structDef != null) ? structDef : new StructDef();

        typeDefStore.vertexToTypeDef(vertex, ret);

        List<AttributeDef> attributeDefs = new ArrayList<>();
        String typePropertyKey = GraphUtils.getTypeDefPropertyKey(ret);
        String encodedTypePropertyKey = GraphUtils.encodePropertyKey(typePropertyKey);
        List<String> attrNames = GraphUtils.getProperty(vertex, encodedTypePropertyKey, List.class);

        if (CollectionUtils.isNotEmpty(attrNames)) {
            for (String attrName : attrNames) {
                String attrPropertyKey = GraphUtils.getTypeDefPropertyKey(ret, attrName);
                String encodedAttrPropertyKey = GraphUtils.encodePropertyKey(attrPropertyKey);
                String attrJson  = GraphUtils.getProperty(vertex, encodedAttrPropertyKey, String.class);

                if (StringUtils.isEmpty(attrJson)) {
                    LOG.warn("attribute not found {}.{}. Ignoring..", structDef.getName(), attrName);

                    continue;
                }

                attributeDefs.add(toAttributeDefFromJson(structDef, Type.fromJson(attrJson, Map.class), typeDefStore));
            }
        }

        ret.setAttributeDefs(attributeDefs);

        return ret;
    }

    private static void addReferencesForAttribute(Vertex vertex, AttributeDef attributeDef,
                                                  TypeDefGraphStore typeDefStore) throws BaseException {
        Set<String> referencedTypeNames = TypeUtil.getReferencedTypeNames(attributeDef.getTypeName());

        String typeName = GraphUtils.getProperty(vertex,Constants.TYPENAME_PROPERTY_KEY, String.class);

        for (String referencedTypeName : referencedTypeNames) {
            if (!TypeUtil.isBuiltInType(referencedTypeName)) {
                Vertex referencedTypeVertex = typeDefStore.findTypeVertexByName(referencedTypeName);

                if (referencedTypeVertex == null) {
                    throw new BaseException(ErrorCode.UNKNOWN_TYPE, referencedTypeName, typeName, attributeDef.getName());
                }

                String label = GraphUtils.getEdgeLabel(typeName, attributeDef.getName());

                typeDefStore.getOrCreateEdge(vertex, referencedTypeVertex, label);
            }
        }
    }

    @VisibleForTesting
    public static String toJsonFromAttribute(Attribute attribute) {
        AttributeDef attributeDef = attribute.getAttributeDef();
        Map<String, Object> attribInfo   = new HashMap<>();

        attribInfo.put("name", attributeDef.getName());
        attribInfo.put("dataType", attributeDef.getTypeName());
        attribInfo.put("isUnique", attributeDef.getIsUnique());
        attribInfo.put("isIndexable", attributeDef.getIsIndexable());
        attribInfo.put("includeInNotification", attributeDef.getIncludeInNotification());
        attribInfo.put("isComposite", attribute.isOwnedRef());
        attribInfo.put("reverseAttributeName", attribute.getInverseRefAttributeName());
        attribInfo.put("defaultValue", attributeDef.getDefaultValue());
        attribInfo.put("description", attributeDef.getDescription());
        attribInfo.put("searchWeight", attributeDef.getSearchWeight());
        attribInfo.put("indexType", attributeDef.getIndexType());

        if(attributeDef.getOptions() != null) {
            attribInfo.put("options", Type.toJson(attributeDef.getOptions()));
        }

        attribInfo.put("displayName", attributeDef.getDisplayName());

        final int lower;
        final int upper;

        if (attributeDef.getCardinality() == AttributeDef.Cardinality.SINGLE) {
            lower = attributeDef.getIsOptional() ? 0 : 1;
            upper = 1;
        } else {
            if(attributeDef.getIsOptional()) {
                lower = 0;
            } else {
                lower = Math.max(attributeDef.getValuesMinCount(), 1);
            }

            upper = attributeDef.getValuesMaxCount() < 2 ? Integer.MAX_VALUE : attributeDef.getValuesMaxCount();
        }

        Map<String, Object> multiplicity = new HashMap<>();
        multiplicity.put("lower", lower);
        multiplicity.put("upper", upper);
        multiplicity.put("isUnique", AttributeDef.Cardinality.SET.equals(attributeDef.getCardinality()));

        attribInfo.put("multiplicity", Type.toJson(multiplicity));

        return Type.toJson(attribInfo);
    }

    @VisibleForTesting
    public static AttributeDef toAttributeDefFromJson(StructDef structDef,
                                                      Map attribInfo,
                                                      TypeDefGraphStore typeDefStore) throws BaseException {
        AttributeDef ret = new AttributeDef();

        ret.setName((String) attribInfo.get("name"));
        ret.setTypeName((String) attribInfo.get("dataType"));
        ret.setIsUnique((Boolean) attribInfo.get("isUnique"));
        ret.setIsIndexable((Boolean) attribInfo.get("isIndexable"));
        ret.setIncludeInNotification((Boolean) attribInfo.get("includeInNotification"));
        ret.setDefaultValue((String) attribInfo.get("defaultValue"));
        ret.setDescription((String) attribInfo.get("description"));

        if(attribInfo.get("options") != null) {
            ret.setOptions(Type.fromJson((String) attribInfo.get("options"), Map.class));
        }

        ret.setDisplayName((String) attribInfo.get("displayName"));

        if ((Boolean)attribInfo.get("isComposite")) {
            ret.addConstraint(new ConstraintDef(ConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
        }

        final String reverseAttributeName = (String) attribInfo.get("reverseAttributeName");
        if (StringUtils.isNotBlank(reverseAttributeName)) {
            ret.addConstraint(new ConstraintDef(ConstraintDef.CONSTRAINT_TYPE_INVERSE_REF,
                    new HashMap<String, Object>() {{
                        put(ConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, reverseAttributeName);
                    }}));
        }

        Map multiplicity = Type.fromJson((String) attribInfo.get("multiplicity"), Map.class);
        Number minCount = (Number) multiplicity.get("lower");
        Number maxCount = (Number) multiplicity.get("upper");
        Boolean isUnique = (Boolean) multiplicity.get("isUnique");

        if (minCount == null || minCount.intValue() == 0) {
            ret.setIsOptional(true);
            ret.setValuesMinCount(0);
        } else {
            ret.setIsOptional(false);
            ret.setValuesMinCount(minCount.intValue());
        }

        if (maxCount == null || maxCount.intValue() < 2) {
            ret.setCardinality(AttributeDef.Cardinality.SINGLE);
            ret.setValuesMaxCount(1);
        } else {
            if (isUnique == null || isUnique.equals(Boolean.FALSE)) {
                ret.setCardinality(AttributeDef.Cardinality.LIST);
            } else {
                ret.setCardinality(AttributeDef.Cardinality.SET);
            }

            ret.setValuesMaxCount(maxCount.intValue());
        }

        Number searchWeight = (Number) attribInfo.get("searchWeight");
        if( searchWeight != null ) {
            ret.setSearchWeight(searchWeight.intValue());
        } else {
            ret.setSearchWeight(-1);
        }

        String indexType = (String) attribInfo.get("indexType");
        if(!StringUtils.isEmpty(indexType)) {
            ret.setIndexType(AttributeDef.IndexType.valueOf(indexType));
        }
        return ret;
    }
}

