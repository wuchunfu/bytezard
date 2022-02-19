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
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.PropagateTags;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.RelationshipCategory;
import io.simforce.bytezard.metadata.model.typedef.RelationshipEndDef;
import io.simforce.bytezard.metadata.repository.utils.GraphUtils;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.RelationshipType;
import io.simforce.bytezard.metadata.type.Type;
import io.simforce.bytezard.metadata.type.TypeRegistry;

/**
 * RelationshipDef store in v1 format.
 */
public class RelationshipDefStore extends AbstractDefStore<RelationshipDef> {
    private static final Logger LOG = LoggerFactory.getLogger(RelationshipDefStore.class);

    @Inject
    public RelationshipDefStore(TypeDefGraphStore typeDefStore, TypeRegistry typeRegistry) {
        super(typeDefStore, typeRegistry);
    }

    @Override
    public Vertex preCreate(RelationshipDef relationshipDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.preCreate({})", relationshipDef);
        }

        validateType(relationshipDef);

        Type type = typeRegistry.getType(relationshipDef.getName());

        if (type.getTypeCategory() != TypeCategory.RELATIONSHIP) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, relationshipDef.getName(), TypeCategory.RELATIONSHIP.name());
        }

        verifyTypeReadAccess(relationshipDef);

        Vertex relationshipDefVertex = typeDefStore.findTypeVertexByName(relationshipDef.getName());

        if (relationshipDefVertex != null) {
            throw new BaseException(ErrorCode.TYPE_ALREADY_EXISTS, relationshipDef.getName());
        }

        relationshipDefVertex = typeDefStore.createTypeVertex(relationshipDef);

        updateVertexPreCreate(relationshipDef, (RelationshipType) type, relationshipDefVertex);

        final RelationshipEndDef endDef1  = relationshipDef.getEndDef1();
        final RelationshipEndDef endDef2  = relationshipDef.getEndDef2();
        final String type1 = endDef1.getType();
        final String type2 = endDef2.getType();
        final String name1 = endDef1.getName();
        final String name2 = endDef2.getName();
        final Vertex end1TypeVertex = typeDefStore.findTypeVertexByName(type1);
        final Vertex end2TypeVertex = typeDefStore.findTypeVertexByName(type2);

        if (end1TypeVertex == null) {
            throw new BaseException(ErrorCode.RELATIONSHIPDEF_END_TYPE_NAME_NOT_FOUND, relationshipDef.getName(), type1);
        }

        if (end2TypeVertex == null) {
            throw new BaseException(ErrorCode.RELATIONSHIPDEF_END_TYPE_NAME_NOT_FOUND, relationshipDef.getName(), type2);
        }

        // create an edge between the relationshipDef and each of the entityDef vertices.
        Edge edge1 = typeDefStore.getOrCreateEdge(relationshipDefVertex, end1TypeVertex, GraphUtils.RELATIONSHIPTYPE_EDGE_LABEL);

        /*
        Where edge1 and edge2 have the same names and types we do not need a second edge.
        We are not invoking the equals method on the RelationshipedDef, as we only want 1 edge even if propagateTags or other properties are different.
        */

        if (type1.equals(type2) && name1.equals(name2)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("RelationshipDefStoreV1.preCreate({}): created relationshipDef vertex {}," +
                        " and one edge as {}, because end1 and end2 have the same type and name", relationshipDef, relationshipDefVertex, edge1);
            }

        } else {
            Edge edge2 = typeDefStore.getOrCreateEdge(relationshipDefVertex, end2TypeVertex, GraphUtils.RELATIONSHIPTYPE_EDGE_LABEL);
            if (LOG.isDebugEnabled()) {
                LOG.debug("RelationshipDefStoreV1.preCreate({}): created relationshipDef vertex {}," +
                        " edge1 as {}, edge2 as {} ", relationshipDef, relationshipDefVertex, edge1, edge2);
            }

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.preCreate({}): {}", relationshipDef, relationshipDefVertex);
        }
        return relationshipDefVertex;
    }

    @Override
    public RelationshipDef create(RelationshipDef relationshipDef, Vertex preCreateResult)
            throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.create({}, {})", relationshipDef, preCreateResult);
        }

        Vertex vertex = (preCreateResult == null) ? preCreate(relationshipDef) : preCreateResult;

        RelationshipDef ret = toRelationshipDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.create({}, {}): {}", relationshipDef, preCreateResult, ret);
        }

        return ret;
    }

    @Override
    public List<RelationshipDef> getAll() throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.getAll()");
        }

        List<RelationshipDef> ret = new ArrayList<>();
        Iterator<Vertex> vertices = typeDefStore.findTypeVerticesByCategory(TypeCategory.RELATIONSHIP);

        while (vertices.hasNext()) {
            ret.add(toRelationshipDef(vertices.next()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.getAll(): count={}", ret.size());
        }

        return ret;
    }

    @Override
    public RelationshipDef getByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.getByName({})", name);
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.RELATIONSHIP);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

//        vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

        RelationshipDef ret = toRelationshipDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.getByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public RelationshipDef getByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.getByGuid({})", guid);
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.RELATIONSHIP);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        RelationshipDef ret = toRelationshipDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.getByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public RelationshipDef update(RelationshipDef relationshipDef) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.update({})", relationshipDef);
        }

        verifyTypeReadAccess(relationshipDef);

        validateType(relationshipDef);

        RelationshipDef ret = StringUtils.isNotBlank(relationshipDef.getGuid())
                ? updateByGuid(relationshipDef.getGuid(), relationshipDef)
                : updateByName(relationshipDef.getName(), relationshipDef);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.update({}): {}", relationshipDef, ret);
        }

        return ret;
    }

    @Override
    public RelationshipDef updateByName(String name, RelationshipDef relationshipDef)
            throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.updateByName({}, {})", name, relationshipDef);
        }

        RelationshipDef existingDef = typeRegistry.getRelationshipDefByName(name);

        validateType(relationshipDef);

        Type type = typeRegistry.getType(relationshipDef.getName());

        if (type.getTypeCategory() != TypeCategory.RELATIONSHIP) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, relationshipDef.getName(), TypeCategory.RELATIONSHIP.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.RELATIONSHIP);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        preUpdateCheck(relationshipDef, (RelationshipType) type, vertex);

        RelationshipDef ret = toRelationshipDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.updateByName({}, {}): {}", name, relationshipDef, ret);
        }

        return ret;
    }

    @Override
    public RelationshipDef updateByGuid(String guid, RelationshipDef relationshipDef)
            throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.updateByGuid({})", guid);
        }

        RelationshipDef existingDef = typeRegistry.getRelationshipDefByGuid(guid);

        validateType(relationshipDef);

        Type type = typeRegistry.getTypeByGuid(guid);

        if (type.getTypeCategory() != TypeCategory.RELATIONSHIP) {
            throw new BaseException(ErrorCode.TYPE_MATCH_FAILED, relationshipDef.getName(), TypeCategory.RELATIONSHIP.name());
        }

        Vertex vertex = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.RELATIONSHIP);

        if (vertex == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        preUpdateCheck(relationshipDef, (RelationshipType) type, vertex);
        // updates should not effect the edges between the types as we do not allow updates that change the endpoints.

        RelationshipDef ret = toRelationshipDef(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.updateByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByName(String name) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.preDeleteByName({})", name);
        }

        RelationshipDef existingDef = typeRegistry.getRelationshipDefByName(name);

        Vertex ret = typeDefStore.findTypeVertexByNameAndCategory(name, TypeCategory.RELATIONSHIP);

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, name);
        }

        if (GraphUtils.relationshipTypeHasInstanceEdges(typeDefStore.getGraph(), name)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, name);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.preDeleteByName({}): {}", name, ret);
        }

        return ret;
    }

    @Override
    public Vertex preDeleteByGuid(String guid) throws BaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RelationshipDefStoreV1.preDeleteByGuid({})", guid);
        }

        RelationshipDef existingDef = typeRegistry.getRelationshipDefByGuid(guid);

        Vertex ret = typeDefStore.findTypeVertexByGuidAndCategory(guid, TypeCategory.RELATIONSHIP);

        if (ret == null) {
            throw new BaseException(ErrorCode.TYPE_GUID_NOT_FOUND, guid);
        }

        String typeName = GraphUtils.getEncodedProperty(ret, Constants.TYPENAME_PROPERTY_KEY, String.class);

        if (GraphUtils.relationshipTypeHasInstanceEdges(typeDefStore.getGraph(), typeName)) {
            throw new BaseException(ErrorCode.TYPE_HAS_REFERENCES, typeName);
        }

        typeDefStore.deleteTypeVertexOutEdges(ret);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RelationshipDefStoreV1.preDeleteByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    private void updateVertexPreCreate(RelationshipDef relationshipDef, RelationshipType relationshipType,
                                       Vertex vertex) throws BaseException {
        RelationshipEndDef end1 = relationshipDef.getEndDef1();
        RelationshipEndDef end2 = relationshipDef.getEndDef2();

        // check whether the names added on the relationship Ends are reserved if required.
//        final boolean allowReservedKeywords;
//        try {
//            allowReservedKeywords = ApplicationProperties.get().getBoolean(ALLOW_RESERVED_KEYWORDS, true);
//        } catch (Exception e) {
//            throw new BaseException(e);
//        }

//        if (!allowReservedKeywords) {
//            if (DSL.Parser.isKeyword(end1.getName())) {
//                throw new BaseException(ErrorCode.RELATIONSHIPDEF_END1_NAME_INVALID, end1.getName());
//            }
//
//            if (DSL.Parser.isKeyword(end2.getName())) {
//                throw new BaseException(ErrorCode.RELATIONSHIPDEF_END2_NAME_INVALID, end2.getName());
//            }
//        }

        StructDefStore.updateVertexPreCreate(relationshipDef, relationshipType, vertex, typeDefStore);
        // Update ends
        setVertexPropertiesFromRelationshipDef(relationshipDef, vertex);
    }

    private void preUpdateCheck(RelationshipDef newRelationshipDef, RelationshipType relationshipType,
                                Vertex vertex) throws BaseException {
        // We will not support an update to endpoints or category key
        RelationshipDef existingRelationshipDef = toRelationshipDef(vertex);

        preUpdateCheck(newRelationshipDef, existingRelationshipDef);
        // we do allow change to tag propagation and the addition of new attributes.

        StructDefStore.updateVertexPreUpdate(newRelationshipDef, relationshipType, vertex, typeDefStore);

        setVertexPropertiesFromRelationshipDef(newRelationshipDef, vertex);
    }

    /**
     * Check ends are the same and relationshipCategory is the same.
     *
     * We do this by comparing 2 relationshipDefs to avoid exposing the Vertex to unit testing.
     *
     * @param newRelationshipDef
     * @param existingRelationshipDef
     * @throws BaseException
     */
    public static void preUpdateCheck(RelationshipDef newRelationshipDef, RelationshipDef existingRelationshipDef) throws BaseException {
        // do not allow renames of the Def.
        String existingName = existingRelationshipDef.getName();
        String newName      = newRelationshipDef.getName();

        if (!existingName.equals(newName)) {
            throw new BaseException(ErrorCode.RELATIONSHIPDEF_INVALID_NAME_UPDATE,
                    newRelationshipDef.getGuid(),existingName, newName);
        }

        RelationshipCategory existingRelationshipCategory = existingRelationshipDef.getRelationshipCategory();
        RelationshipCategory newRelationshipCategory      = newRelationshipDef.getRelationshipCategory();

        if ( !existingRelationshipCategory.equals(newRelationshipCategory)){
//            if (!RequestContext.get().isInTypePatching()) {
//                throw new BaseException(ErrorCode.RELATIONSHIPDEF_INVALID_CATEGORY_UPDATE,
//                        newRelationshipDef.getName(), newRelationshipCategory.name(),
//                        existingRelationshipCategory.name());
//            } else {
//                LOG.warn("RELATIONSHIP UPDATE: relationship category from {} to {} for {}", existingRelationshipCategory.name(), newRelationshipCategory.name(), newRelationshipDef.getName());
//            }
        }

        RelationshipEndDef existingEnd1 = existingRelationshipDef.getEndDef1();
        RelationshipEndDef existingEnd2 = existingRelationshipDef.getEndDef2();
        RelationshipEndDef newEnd1      = newRelationshipDef.getEndDef1();
        RelationshipEndDef newEnd2      = newRelationshipDef.getEndDef2();
        boolean                 endsSwaped   = false;

        if ( !isValidUpdate(existingEnd1, newEnd1) ) {
//            if (RequestContext.get().isInTypePatching() && isValidUpdate(existingEnd1, newEnd2)) { // allow swap of ends during type-patch
//                endsSwaped = true;
//            } else {
//                throw new BaseException(ErrorCode.RELATIONSHIPDEF_INVALID_END1_UPDATE,
//                                             newRelationshipDef.getName(), newEnd1.toString(), existingEnd1.toString());
//            }
        }

        RelationshipEndDef newEndToCompareWith = endsSwaped ? newEnd1 : newEnd2;

        if ( !isValidUpdate(existingEnd2, newEndToCompareWith) ) {
                throw new BaseException(ErrorCode.RELATIONSHIPDEF_INVALID_END2_UPDATE,
                                             newRelationshipDef.getName(), newEndToCompareWith.toString(), existingEnd2.toString());
        }
    }

    public static void setVertexPropertiesFromRelationshipDef(RelationshipDef relationshipDef, Vertex vertex) {
        vertex.property(Constants.RELATIONSHIPTYPE_END1_KEY, Type.toJson(relationshipDef.getEndDef1()));
        vertex.property(Constants.RELATIONSHIPTYPE_END2_KEY, Type.toJson(relationshipDef.getEndDef2()));

        // default the relationship category to association if it has not been specified.
        String relationshipCategory = RelationshipCategory.ASSOCIATION.name();
        if (relationshipDef.getRelationshipCategory()!=null) {
            relationshipCategory =relationshipDef.getRelationshipCategory().name();
        }

        // Update RelationshipCategory
        vertex.property(Constants.RELATIONSHIPTYPE_CATEGORY_KEY, relationshipCategory);
        if (relationshipDef.getRelationshipLabel() == null) {
            vertex.property(Constants.RELATIONSHIPTYPE_LABEL_KEY).remove();
        } else {
            vertex.property(Constants.RELATIONSHIPTYPE_LABEL_KEY, relationshipDef.getRelationshipLabel());
        }

        if (relationshipDef.getPropagateTags() == null) {
            vertex.property(Constants.RELATIONSHIPTYPE_TAG_PROPAGATION_KEY, RelationshipDef.PropagateTags.NONE.name());
        } else {
            vertex.property(Constants.RELATIONSHIPTYPE_TAG_PROPAGATION_KEY, relationshipDef.getPropagateTags().name());
        }
    }

    private RelationshipDef toRelationshipDef(Vertex vertex) throws BaseException {
        RelationshipDef ret = null;
        if (vertex != null && typeDefStore.isTypeVertex(vertex, TypeCategory.RELATIONSHIP)) {
            String name = GraphUtils.getProperty(vertex, Constants.TYPENAME_PROPERTY_KEY, String.class);
            String description = GraphUtils.getProperty(vertex, Constants.TYPEDESCRIPTION_PROPERTY_KEY, String.class);
            String version = GraphUtils.getProperty(vertex, Constants.TYPEVERSION_PROPERTY_KEY, String.class);
            String label = GraphUtils.getProperty(vertex, Constants.RELATIONSHIPTYPE_LABEL_KEY, String.class);
            String end1Str = GraphUtils.getProperty(vertex, Constants.RELATIONSHIPTYPE_END1_KEY, String.class);
            String end2Str = GraphUtils.getProperty(vertex, Constants.RELATIONSHIPTYPE_END2_KEY, String.class);
            String relationStr = GraphUtils.getProperty(vertex, Constants.RELATIONSHIPTYPE_CATEGORY_KEY, String.class);
            String propagateStr = GraphUtils.getProperty(vertex, Constants.RELATIONSHIPTYPE_TAG_PROPAGATION_KEY, String.class);

            // set the ends
            RelationshipEndDef endDef1 = Type.fromJson(end1Str, RelationshipEndDef.class);
            RelationshipEndDef endDef2 = Type.fromJson(end2Str, RelationshipEndDef.class);

            // set the relationship Category
            RelationshipCategory relationshipCategory = null;
            for (RelationshipCategory value : RelationshipCategory.values()) {
                if (value.name().equals(relationStr)) {
                    relationshipCategory = value;
                }
            }

            // set the propagateTags
            PropagateTags propagateTags = null;
            for (PropagateTags value : PropagateTags.values()) {
                if (value.name().equals(propagateStr)) {
                    propagateTags = value;
                }
            }

            ret = new RelationshipDef(name, description, version, relationshipCategory,  propagateTags, endDef1, endDef2);

            ret.setRelationshipLabel(label);

            // add in the attributes
            StructDefStore.toStructDef(vertex, ret, typeDefStore);
        }

        return ret;
    }

    private static boolean isValidUpdate(RelationshipEndDef currentDef, RelationshipEndDef updatedDef) {
        // permit updates to description and isLegacyAttribute (ref type-patch REMOVE_LEGACY_REF_ATTRIBUTES)
        return Objects.equals(currentDef.getType(), updatedDef.getType()) &&
                Objects.equals(currentDef.getName(), updatedDef.getName()) &&
                Objects.equals(currentDef.getIsContainer(), updatedDef.getIsContainer()) &&
                Objects.equals(currentDef.getCardinality(), updatedDef.getCardinality());
    }

    private void verifyTypeReadAccess(RelationshipDef relationshipDef) throws BaseException {
        verifyTypeReadAccess(relationshipDef.getEndDef1().getType());
        verifyTypeReadAccess(relationshipDef.getEndDef2().getType());
    }

}
