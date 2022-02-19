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

import io.simforce.bytezard.metadata.model.PageInfo;
import io.simforce.bytezard.metadata.model.SearchFilter.SortType;
import io.simforce.bytezard.metadata.model.glossary.relations.TermAssignmentHeader;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * An instance of an entity - like hive_table, hive_database.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Entity extends Struct implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KEY_GUID = "guid";
    public static final String KEY_HOME_ID = "homeId";
    public static final String KEY_IS_PROXY = "isProxy";
    public static final String KEY_IS_INCOMPLETE = "isIncomplete";
    public static final String KEY_PROVENANCE_TYPE = "provenanceType";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_BY = "createdBy";
    public static final String KEY_UPDATED_BY = "updatedBy";
    public static final String KEY_CREATE_TIME = "createTime";
    public static final String KEY_UPDATE_TIME = "updateTime";
    public static final String KEY_VERSION = "version";

    /**
     * Status of the entity - can be active or deleted.
     * Deleted entities are not removed from store.
     */
    public enum Status { ACTIVE, DELETED, PURGED }

    private String guid = null;
    private String homeId = null;
    private Boolean isProxy = Boolean.FALSE;
    private Boolean isIncomplete = Boolean.FALSE;
    private Integer provenanceType = 0;
    private Status status = Status.ACTIVE;
    private String createdBy = null;
    private String updatedBy = null;
    private Date createTime = null;
    private Date updateTime = null;
    private Long version = 0L;

    private Map<String, Object> relationshipAttributes;
    private List<Classification> classifications;
    private List<TermAssignmentHeader> meanings;
    private Map<String, String> customAttributes;
    private Map<String, Map<String, Object>> businessAttributes;
    private Set<String> labels;
    private Set<String> pendingTasks; // read-only field i.e. value provided is ignored during entity create/update

    @JsonIgnore
    private static final AtomicLong s_nextId = new AtomicLong(System.nanoTime());

    public Entity() {
        this(null, null);
    }

    public Entity(String typeName) {
        this(typeName, null);
    }

    public Entity(EntityDef entityDef) {
        this(entityDef != null ? entityDef.getName() : null, null);
    }

    public Entity(String typeName, String attrName, Object attrValue) {
        super(typeName, attrName, attrValue);

        init();
    }

    public Entity(String typeName, Map<String, Object> attributes) {
        super(typeName, attributes);

        init();
    }

    public Entity(EntityHeader header) {
        super(header.getTypeName(), header.getAttributes());

        setGuid(header.getGuid());
        setStatus(header.getStatus());
        setClassifications(header.getClassifications());
        setMeanings(header.getMeanings());
    }

    public Entity(Map map) {
        super(map);

        if (map != null) {
            Object oGuid             = map.get(KEY_GUID);
            Object homeId            = map.get(KEY_HOME_ID);
            Object isProxy           = map.get(KEY_IS_PROXY);
            Object isIncomplete      = map.get(KEY_IS_INCOMPLETE);
            Object provenanceType    = map.get(KEY_PROVENANCE_TYPE);
            Object status            = map.get(KEY_STATUS);
            Object createdBy         = map.get(KEY_CREATED_BY);
            Object updatedBy         = map.get(KEY_UPDATED_BY);
            Object createTime        = map.get(KEY_CREATE_TIME);
            Object updateTime        = map.get(KEY_UPDATE_TIME);
            Object version           = map.get(KEY_VERSION);

            if (oGuid != null) {
                setGuid(oGuid.toString());
            }

            if (homeId != null) {
                setHomeId(homeId.toString());
            }

            if (isProxy != null) {
                setIsProxy((Boolean)isProxy);
            }
            else {
                setIsProxy(Boolean.FALSE);
            }

            if (isIncomplete != null) {
                setIsIncomplete((Boolean) isIncomplete);
            } else {
                setIsIncomplete(Boolean.FALSE);
            }

            if (provenanceType instanceof Number) {
                setProvenanceType(((Number) version).intValue());
            }

            if (status != null) {
                setStatus(Status.valueOf(status.toString()));
            }

            if (createdBy != null) {
                setCreatedBy(createdBy.toString());
            }

            if (createTime instanceof Number) {
                setCreateTime(new Date(((Number) createTime).longValue()));
            }

            if (updatedBy != null) {
                setUpdatedBy(updatedBy.toString());
            }

            if (updateTime instanceof Number) {
                setUpdateTime(new Date(((Number) updateTime).longValue()));
            }

            if (version instanceof Number) {
                setVersion(((Number) version).longValue());
            }
        }
    }

    public Entity(Entity other) {
        super(other);

        if (other != null) {
            setGuid(other.getGuid());
            setHomeId(other.getHomeId());
            setIsProxy(other.isProxy());
            setIsIncomplete(other.getIsIncomplete());
            setProvenanceType(other.getProvenanceType());
            setStatus(other.getStatus());
            setCreatedBy(other.getCreatedBy());
            setUpdatedBy(other.getUpdatedBy());
            setCreateTime(other.getCreateTime());
            setUpdateTime(other.getUpdateTime());
            setVersion(other.getVersion());
            setClassifications(other.getClassifications());
            setRelationshipAttributes(other.getRelationshipAttributes());
            setMeanings(other.getMeanings());
            setCustomAttributes(other.getCustomAttributes());
            setBusinessAttributes(other.getBusinessAttributes());
            setLabels(other.getLabels());
            setPendingTasks(other.getPendingTasks());
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public Boolean isProxy() {
        return isProxy;
    }

    public void setIsProxy(Boolean isProxy) {
        this.isProxy = isProxy;
    }

    public Boolean getIsIncomplete() {
        return isIncomplete;
    }

    public void setIsIncomplete(Boolean isIncomplete) {
        this.isIncomplete = isIncomplete;
    }

    public Integer getProvenanceType() {
        return provenanceType;
    }

    public void setProvenanceType(Integer provenanceType) {
        this.provenanceType = provenanceType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Map<String, Object> getRelationshipAttributes() { return relationshipAttributes; }

    public void setRelationshipAttributes(Map<String, Object> relationshipAttributes) {
        this.relationshipAttributes = relationshipAttributes;
    }

    public void setRelationshipAttribute(String name, Object value) {
        Map<String, Object> r = this.relationshipAttributes;

        if (r != null) {
            r.put(name, value);
        } else {
            r = new HashMap<>();
            r.put(name, value);

            this.relationshipAttributes = r;
        }
    }

    public Object getRelationshipAttribute(String name) {
        Map<String, Object> a = this.relationshipAttributes;

        return a != null ? a.get(name) : null;
    }

    public boolean hasRelationshipAttribute(String name) {
        Map<String, Object> r = this.relationshipAttributes;

        return r != null && r.containsKey(name);
    }

    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Map<String, Map<String, Object>> getBusinessAttributes() {
        return businessAttributes;
    }

    public void setBusinessAttributes(Map<String, Map<String, Object>> businessAttributes) {
        this.businessAttributes = businessAttributes;
    }

    public void setBusinessAttribute(String nsName, String nsAttrName, Object nsValue) {
        Map<String, Map<String, Object>> businessAttributes = this.businessAttributes;

        if (businessAttributes == null) {
            businessAttributes = new HashMap<>();

            this.businessAttributes = businessAttributes;
        }

        Map<String, Object> businessAttributeMap = businessAttributes.computeIfAbsent(nsName, k -> new HashMap<>());

        businessAttributeMap.put(nsAttrName, nsValue);
    }

    public Object getBusinessAttribute(String bmName, String bmAttrName) {
        Map<String, Map<String, Object>> businessAttributes   = this.businessAttributes;
        Map<String, Object>              businessAttributeMap = businessAttributes == null ? null : businessAttributes.get(bmName);

        return businessAttributeMap == null ? null : businessAttributeMap.get(bmAttrName);
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public Set<String> getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(Set<String> pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public List<Classification> getClassifications() { return classifications; }

    public void setClassifications(List<Classification> classifications) { this.classifications = classifications; }

    public void addClassifications(List<Classification> classifications) {
        List<Classification> c = this.classifications;

        if (c == null) {
            c = new ArrayList<>();
        }

        c.addAll(classifications);

        this.classifications = c;
    }

    public List<TermAssignmentHeader> getMeanings() {
        return meanings;
    }

    public void setMeanings(final List<TermAssignmentHeader> meanings) {
        this.meanings = meanings;
    }

    public void addMeaning(TermAssignmentHeader meaning) {
        List<TermAssignmentHeader> meanings = this.meanings;

        if (meanings == null) {
            meanings = new ArrayList<>();
        }
        meanings.add(meaning);
        setMeanings(meanings);
    }

    private void init() {
        setGuid(nextInternalId());
        setHomeId(null);
        setIsProxy(Boolean.FALSE);
        setIsIncomplete(Boolean.FALSE);
        setProvenanceType(0);
        setStatus(null);
        setCreatedBy(null);
        setUpdatedBy(null);
        setCreateTime(null);
        setUpdateTime(null);
        setClassifications(null);
        setMeanings(null);
        setCustomAttributes(null);
        setBusinessAttributes(null);
        setLabels(null);
        setPendingTasks(null);
    }

    private static String nextInternalId() {
        return "-" + Long.toString(s_nextId.getAndIncrement());
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("Entity{");
        super.toString(sb);
        sb.append("guid='").append(guid).append('\'');
        sb.append(", homeId='").append(homeId).append('\'');
        sb.append(", isProxy='").append(isProxy).append('\'');
        sb.append(", isIncomplete=").append(isIncomplete);
        sb.append(", provenanceType=").append(provenanceType);
        sb.append(", status=").append(status);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        dumpDateField(", createTime=", createTime, sb);
        dumpDateField(", updateTime=", updateTime, sb);
        sb.append(", version=").append(version);
        sb.append(", relationshipAttributes=[");
        dumpObjects(relationshipAttributes, sb);
        sb.append("]");
        sb.append(", classifications=[");
        BaseTypeDef.dumpObjects(classifications, sb);
        sb.append(']');
        sb.append(", meanings=[");
        BaseTypeDef.dumpObjects(meanings, sb);
        sb.append(']');
        sb.append(", customAttributes=[");
        dumpObjects(customAttributes, sb);
        sb.append("]");
        sb.append(", businessAttributes=[");
        dumpObjects(businessAttributes, sb);
        sb.append("]");
        sb.append(", labels=[");
        dumpObjects(labels, sb);
        sb.append("]");
        sb.append(", pendingTasks=[");
        dumpObjects(pendingTasks, sb);
        sb.append("]");
        sb.append('}');

        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        Entity that = (Entity) o;
        return Objects.equals(guid, that.guid) &&
                Objects.equals(homeId, that.homeId) &&
                Objects.equals(isProxy, that.isProxy) &&
                Objects.equals(isIncomplete, that.isIncomplete) &&
                Objects.equals(provenanceType, that.provenanceType) &&
                status == that.status &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(version, that.version) &&
                Objects.equals(relationshipAttributes, that.relationshipAttributes) &&
                Objects.equals(customAttributes, that.customAttributes) &&
                Objects.equals(businessAttributes, that.businessAttributes) &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(classifications, that.classifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guid, homeId, isProxy, isIncomplete, provenanceType, status, createdBy, updatedBy,
                createTime, updateTime, version, relationshipAttributes, classifications, customAttributes, businessAttributes, labels);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * An instance of an entity along with extended info - like hive_table, hive_database.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class EntityExtInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Map<String, Entity> referredEntities;


        public EntityExtInfo() {
            setReferredEntities(null);
        }

        public EntityExtInfo(Entity referredEntity) {
            addReferredEntity(referredEntity);
        }

        public EntityExtInfo(Map<String, Entity> referredEntities) {
            setReferredEntities(referredEntities);
        }

        public EntityExtInfo(EntityExtInfo other) {
            if (other != null) {
                setReferredEntities(other.getReferredEntities());
            }
        }

        public Map<String, Entity> getReferredEntities() { return referredEntities; }

        public void setReferredEntities(Map<String, Entity> referredEntities) { this.referredEntities = referredEntities; }

        @JsonIgnore
        public final void addReferredEntity(Entity entity) {
            addReferredEntity(entity.getGuid(), entity);
        }

        @JsonIgnore
        public final void addReferredEntity(String guid, Entity entity) {
            Map<String, Entity> r = this.referredEntities;

            if (r == null) {
                r = new HashMap<>();

                this.referredEntities = r;
            }

            if (guid != null) {
                r.put(guid, entity);
            }
        }

        @JsonIgnore
        public final Entity removeReferredEntity(String guid) {
            Map<String, Entity> r = this.referredEntities;

            return r != null && guid != null ? r.remove(guid) : null;
        }

        @JsonIgnore
        public final Entity getReferredEntity(String guid) {
            Map<String, Entity> r = this.referredEntities;

            return r != null && guid != null ? r.get(guid) : null;
        }

        @JsonIgnore
        public Entity getEntity(String guid) {
            return getReferredEntity(guid);
        }

        @JsonIgnore
        public Entity removeEntity(String guid) {
            Map<String, Entity> r = this.referredEntities;

            return r != null && guid != null ? r.remove(guid) : null;
        }

        public void updateEntityGuid(String oldGuid, String newGuid) {
            Entity entity = getEntity(oldGuid);

            if (entity != null) {
                entity.setGuid(newGuid);

                if(removeEntity(oldGuid) != null) {
                    addReferredEntity(newGuid, entity);
                }
            }
        }

        public boolean hasEntity(String guid) {
            return getEntity(guid) != null;
        }

        public void compact() {
            // for derived classes to implement their own logic
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("EntityExtInfo{");
            sb.append("referredEntities={");
            BaseTypeDef.dumpObjects(referredEntities, sb);
            sb.append("}");
            sb.append("}");

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

            EntityExtInfo that = (EntityExtInfo) o;
            return Objects.equals(referredEntities, that.referredEntities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(referredEntities);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class EntityWithExtInfo extends EntityExtInfo {
        private static final long serialVersionUID = 1L;

        private Entity entity;

        public EntityWithExtInfo() {
            this(null, null);
        }

        public EntityWithExtInfo(Entity entity) {
            this(entity, null);
        }

        public EntityWithExtInfo(Entity entity, EntityExtInfo extInfo) {
            super(extInfo);

            this.entity = entity;
        }

        public Entity getEntity() { return entity; }

        public void setEntity(Entity entity) { this.entity = entity; }

        @JsonIgnore
        @Override
        public Entity getEntity(String guid) {
            Entity ret = super.getEntity(guid);

            if (ret == null && entity != null) {
                if (StringUtils.equals(guid, entity.getGuid())) {
                    ret = entity;
                }
            }

            return ret;
        }

        @JsonIgnore
        @Override
        public void compact() {
            super.compact();

            // remove 'entity' from referredEntities
            if (entity != null) {
                removeEntity(entity.getGuid());
            }
        }

        @Override
        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("EntityWithExtInfo{");
            sb.append("entity=").append(entity).append(",");
            super.toString(sb);
            sb.append("}");

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

            EntityWithExtInfo that = (EntityWithExtInfo) o;
            return Objects.equals(entity, that.entity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entity);
        }
    }

    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class EntitiesWithExtInfo extends EntityExtInfo {
        private static final long serialVersionUID = 1L;

        private List<Entity> entities;

        public EntitiesWithExtInfo() {
            this(null, null);
        }

        public EntitiesWithExtInfo(Entity entity) { this(Arrays.asList(entity), null);
        }

        public EntitiesWithExtInfo(List<Entity> entities) {
            this(entities, null);
        }

        public EntitiesWithExtInfo(EntityWithExtInfo entity) {
            this(Arrays.asList(entity.getEntity()), entity);
        }

        public EntitiesWithExtInfo(List<Entity> entities, EntityExtInfo extInfo) {
            super(extInfo);

            this.entities = entities;
        }

        public List<Entity> getEntities() { return entities; }

        public void setEntities(List<Entity> entities) { this.entities = entities; }

        @JsonIgnore
        @Override
        public Entity getEntity(String guid) {
            Entity ret = super.getEntity(guid);

            if (ret == null && CollectionUtils.isNotEmpty(entities)) {
                for (Entity entity : entities) {
                    if (StringUtils.equals(guid, entity.getGuid())) {
                        ret = entity;

                        break;
                    }
                }
            }

            return ret;
        }

        public void addEntity(Entity entity) {
            List<Entity> entities = this.entities;

            if (entities == null) {
                entities = new ArrayList<>();

                this.entities = entities;
            }

            entities.add(entity);
        }

        public void removeEntity(Entity entity) {
            List<Entity> entities = this.entities;

            if (entity != null && entities != null) {
                entities.remove(entity);
            }
        }

        @Override
        public void compact() {
            super.compact();

            // remove 'entities' from referredEntities
            if (CollectionUtils.isNotEmpty(entities)) {
                for (Entity entity : entities) {
                    removeReferredEntity(entity.getGuid());
                }
            }
        }

        @Override
        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("EntitiesWithExtInfo{");
            sb.append("entities=[");
            BaseTypeDef.dumpObjects(entities, sb);
            sb.append("],");
            super.toString(sb);
            sb.append("}");

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

            EntitiesWithExtInfo that = (EntitiesWithExtInfo) o;
            return Objects.equals(entities, that.entities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entities);
        }
    }

    /**
     * REST serialization friendly list.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlSeeAlso(Entity.class)
    public static class Entities extends PageInfo<Entity> {
        private static final long serialVersionUID = 1L;

        public Entities() {
            super();
        }

        public Entities(List<Entity> list) {
            super(list);
        }

        public Entities(List list, long startIndex, int pageSize, long totalCount,
                             SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}