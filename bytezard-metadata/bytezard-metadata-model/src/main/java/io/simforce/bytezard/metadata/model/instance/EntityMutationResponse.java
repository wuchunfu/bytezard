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
package io.simforce.bytezard.metadata.model.instance;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import io.simforce.bytezard.metadata.model.instance.EntityMutations.EntityOperation;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EntityMutationResponse {

    private Map<EntityOperation, List<EntityHeader>> mutatedEntities;
    private Map<String, String>                           guidAssignments;

    public EntityMutationResponse() {
    }

    public EntityMutationResponse(final Map<EntityOperation, List<EntityHeader>> mutatedEntities) {
        this.mutatedEntities = mutatedEntities;
    }

    public Map<EntityOperation, List<EntityHeader>> getMutatedEntities() {
        return mutatedEntities;
    }

    public void setMutatedEntities(final Map<EntityOperation, List<EntityHeader>> mutatedEntities) {
        this.mutatedEntities = mutatedEntities;
    }

    public void setGuidAssignments(Map<String,String> guidAssignments) {
        this.guidAssignments = guidAssignments;
    }

    public Map<String,String> getGuidAssignments() {
        return guidAssignments;
    }


    @JsonIgnore
    public List<EntityHeader> getEntitiesByOperation(EntityOperation op) {
        if ( mutatedEntities != null) {
            return mutatedEntities.get(op);
        }
        return null;
    }

    @JsonIgnore
    public List<EntityHeader> getCreatedEntities() {
        if ( mutatedEntities != null) {
            return mutatedEntities.get(EntityOperation.CREATE);
        }
        return null;
    }

    @JsonIgnore
    public List<EntityHeader> getUpdatedEntities() {
        if ( mutatedEntities != null) {
            return mutatedEntities.get(EntityOperation.UPDATE);
        }
        return null;
    }

    public List<EntityHeader> getPartialUpdatedEntities() {
        if ( mutatedEntities != null) {
            return mutatedEntities.get(EntityOperation.PARTIAL_UPDATE);
        }
        return null;
    }

    @JsonIgnore
    public List<EntityHeader> getDeletedEntities() {
        if ( mutatedEntities != null) {
            return mutatedEntities.get(EntityOperation.DELETE);
        }
        return null;
    }

    @JsonIgnore
    public List<EntityHeader> getPurgedEntities() {
        if ( mutatedEntities != null) {
            return mutatedEntities.get(EntityOperation.PURGE);
        }
        return null;
    }

    @JsonIgnore
    public String getPurgedEntitiesIds() {
        String                  ret = null;
        List<EntityHeader> purgedEntities = getPurgedEntities();

        if (CollectionUtils.isNotEmpty(purgedEntities)) {
            List<String> entityIds = purgedEntities.stream().map(entity -> entity.getGuid()).collect(Collectors.toList());

            ret = String.join(",", entityIds);
        }

        return  ret;
    }

    @JsonIgnore
    public EntityHeader getFirstEntityCreated() {
        final List<EntityHeader> entitiesByOperation = getEntitiesByOperation(EntityOperation.CREATE);
        if ( entitiesByOperation != null && entitiesByOperation.size() > 0) {
            return entitiesByOperation.get(0);
        }

        return null;
    }

    @JsonIgnore
    public EntityHeader getFirstEntityUpdated() {
        final List<EntityHeader> entitiesByOperation = getEntitiesByOperation(EntityOperation.UPDATE);

        if ( entitiesByOperation != null && entitiesByOperation.size() > 0) {
            return entitiesByOperation.get(0);
        }

        return null;
    }

    @JsonIgnore
    public EntityHeader getFirstEntityPartialUpdated() {
        final List<EntityHeader> entitiesByOperation = getEntitiesByOperation(EntityOperation.PARTIAL_UPDATE);
        if ( entitiesByOperation != null && entitiesByOperation.size() > 0) {
            return entitiesByOperation.get(0);
        }

        return null;
    }

    @JsonIgnore
    public EntityHeader getFirstCreatedEntityByTypeName(String typeName) {
        return getFirstEntityByType(getEntitiesByOperation(EntityOperation.CREATE), typeName);
    }

    @JsonIgnore
    public EntityHeader getFirstDeletedEntityByTypeName(String typeName) {
        return getFirstEntityByType(getEntitiesByOperation(EntityOperation.DELETE), typeName);
    }

    @JsonIgnore
    public List<EntityHeader> getCreatedEntitiesByTypeName(String typeName) {
        return getEntitiesByType(getEntitiesByOperation(EntityOperation.CREATE), typeName);
    }

    @JsonIgnore
    public List<EntityHeader> getPartialUpdatedEntitiesByTypeName(String typeName) {
        return getEntitiesByType(getEntitiesByOperation(EntityOperation.PARTIAL_UPDATE), typeName);
    }

    @JsonIgnore
    public EntityHeader getCreatedEntityByTypeNameAndAttribute(String typeName, String attrName, String attrVal) {
        return getEntityByTypeAndUniqueAttribute(getEntitiesByOperation(EntityOperation.CREATE), typeName, attrName, attrVal);
    }

    @JsonIgnore

    public EntityHeader getUpdatedEntityByTypeNameAndAttribute(String typeName, String attrName, String attrVal) {
        return getEntityByTypeAndUniqueAttribute(getEntitiesByOperation(EntityOperation.UPDATE), typeName, attrName, attrVal);
    }

    @JsonIgnore
    public List<EntityHeader> getUpdatedEntitiesByTypeName(String typeName) {
        return getEntitiesByType(getEntitiesByOperation(EntityOperation.UPDATE), typeName);
    }

    @JsonIgnore
    public List<EntityHeader> getDeletedEntitiesByTypeName(String typeName) {
        return getEntitiesByType(getEntitiesByOperation(EntityOperation.DELETE), typeName);
    }

    @JsonIgnore
    public EntityHeader getFirstUpdatedEntityByTypeName(String typeName) {
        return getFirstEntityByType(getEntitiesByOperation(EntityOperation.UPDATE), typeName);
    }

    @JsonIgnore
    public EntityHeader getFirstPartialUpdatedEntityByTypeName(String typeName) {
        return getFirstEntityByType(getEntitiesByOperation(EntityOperation.PARTIAL_UPDATE), typeName);
    }

    @JsonIgnore
    public void addEntity(EntityOperation op, EntityHeader header) {
        // if an entity is already included in CREATE, update the header, to capture propagated classifications
        if (op == EntityOperation.UPDATE || op == EntityOperation.PARTIAL_UPDATE) {
            if (entityHeaderExists(getCreatedEntities(), header.getGuid())) {
                op = EntityOperation.CREATE;
            }
        }

        if (mutatedEntities == null) {
            mutatedEntities = new HashMap<>();
        }

        List<EntityHeader> opEntities = mutatedEntities.computeIfAbsent(op, k -> new ArrayList<>());

        if (!entityHeaderExists(opEntities, header.getGuid())) {
            opEntities.add(header);
        }
    }

    private boolean entityHeaderExists(List<EntityHeader> entityHeaders, String guid) {
        boolean ret = false;

        if (CollectionUtils.isNotEmpty(entityHeaders) && guid != null) {
            for (EntityHeader entityHeader : entityHeaders) {
                if (StringUtils.equals(entityHeader.getGuid(), guid)) {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    public StringBuilder toString(StringBuilder sb) {
        if ( sb == null) {
            sb = new StringBuilder();
        }

        BaseTypeDef.dumpObjects(mutatedEntities, sb);

        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        io.simforce.bytezard.metadata.model.instance.EntityMutationResponse that = (io.simforce.bytezard.metadata.model.instance.EntityMutationResponse) o;
        return Objects.equals(mutatedEntities, that.mutatedEntities) &&
               Objects.equals(guidAssignments, that.guidAssignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mutatedEntities, guidAssignments);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    private EntityHeader getFirstEntityByType(List<EntityHeader> entitiesByOperation, String typeName) {
        if ( entitiesByOperation != null && entitiesByOperation.size() > 0) {
            for (EntityHeader header : entitiesByOperation) {
                if ( header.getTypeName().equals(typeName)) {
                    return header;
                }
            }
        }
        return null;
    }

    private List<EntityHeader> getEntitiesByType(List<EntityHeader> entitiesByOperation, String typeName) {
        List<EntityHeader> ret = new ArrayList<>();

        if ( entitiesByOperation != null && entitiesByOperation.size() > 0) {
            for (EntityHeader header : entitiesByOperation) {
                if ( header.getTypeName().equals(typeName)) {
                    ret.add(header);
                }
            }
        }
        return ret;
    }

    private EntityHeader getEntityByTypeAndUniqueAttribute(List<EntityHeader> entitiesByOperation, String typeName, String attrName, String attrVal) {
        if (entitiesByOperation != null && entitiesByOperation.size() > 0) {
            for (EntityHeader header : entitiesByOperation) {
                if (header.getTypeName().equals(typeName)) {
                    if (attrVal != null && attrVal.equals(header.getAttribute(attrName))) {
                        return header;
                    }
                }
            }
        }
        return null;
    }
}
