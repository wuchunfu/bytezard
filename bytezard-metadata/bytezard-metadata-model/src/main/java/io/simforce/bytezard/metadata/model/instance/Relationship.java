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

import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef.PropagateTags;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 *  relationship instance.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Relationship extends Struct implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KEY_GUID = "guid";
    public static final String KEY_HOME_ID = "homeId";
    public static final String KEY_PROVENANCE_TYPE = "provenanceType";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_BY = "createdBy";
    public static final String KEY_UPDATED_BY = "updatedBy";
    public static final String KEY_CREATE_TIME = "createTime";
    public static final String KEY_UPDATE_TIME = "updateTime";
    public static final String KEY_VERSION = "version";
    public static final String KEY_END1 = "end1";
    public static final String KEY_END2 = "end2";
    public static final String KEY_LABEL = "label";
    public static final String KEY_PROPAGATE_TAGS = "propagateTags";

    public static final String KEY_BLOCKED_PROPAGATED_CLASSIFICATIONS = "blockedPropagatedClassifications";
    public static final String KEY_PROPAGATED_CLASSIFICATIONS = "propagatedClassifications";

    private String guid = null;
    private String homeId = null;
    private Integer provenanceType = null;
    private ObjectId end1 = null;
    private ObjectId end2  = null;
    private String label = null;
    private PropagateTags propagateTags  = PropagateTags.NONE;
    private Status status = Status.ACTIVE;
    private String createdBy = null;
    private String updatedBy = null;
    private Date createTime = null;
    private Date updateTime = null;
    private Long version = 0L;

    /**
     *
     */
    public enum Status { ACTIVE, DELETED }

    private Set<Classification> propagatedClassifications;
    private Set<Classification> blockedPropagatedClassifications;

    @JsonIgnore
    private static final AtomicLong s_nextId = new AtomicLong(System.nanoTime());

    public Relationship() {
        super();

        init();
    }

    public Relationship(String typeName) {
        this(typeName, null);
    }

    public Relationship(String typeName, Map<String, Object> attributes) {
        super(typeName, attributes);

        init();
    }

    public Relationship(String typeName, ObjectId end1, ObjectId end2) {
        super(typeName);

        init(nextInternalId(), null, 0, end1, end2, null, null, null, null, null, null, null, 0L, null, null);
    }

    public Relationship(String typeName, ObjectId end1, ObjectId end2, Map<String, Object> attributes) {
        super(typeName, attributes);

        init(nextInternalId(), null, 0, end1, end2, null, null, null, null, null, null, null, 0L, null, null);
    }

    public Relationship(String typeName, String attrName, Object attrValue) {
        super(typeName, attrName, attrValue);

        init();
    }

    public Relationship(RelationshipDef relationshipDef) {
        this(relationshipDef != null ? relationshipDef.getName() : null);
    }

    public Relationship(Map map) {
        super(map);

        if (map != null) {
            Object oGuid = map.get(KEY_GUID);
            Object homeId = map.get(KEY_HOME_ID);
            Object provenanceType = map.get(KEY_PROVENANCE_TYPE);
            Object oEnd1 = map.get(KEY_END1);
            Object oEnd2 = map.get(KEY_END2);
            Object label = map.get(KEY_LABEL);
            Object propagateTags = map.get(KEY_PROPAGATE_TAGS);
            Object status = map.get(KEY_STATUS);
            Object createdBy = map.get(KEY_CREATED_BY);
            Object updatedBy = map.get(KEY_UPDATED_BY);
            Object createTime = map.get(KEY_CREATE_TIME);
            Object updateTime = map.get(KEY_UPDATE_TIME);
            Object version = map.get(KEY_VERSION);

            Object propagatedClassifications = map.get(KEY_PROPAGATED_CLASSIFICATIONS);
            Object blockedPropagatedClassifications = map.get(KEY_BLOCKED_PROPAGATED_CLASSIFICATIONS);

            if (oGuid != null) {
                setGuid(oGuid.toString());
            }

            if (homeId != null) {
                setHomeId(homeId.toString());
            }

            if (provenanceType instanceof Number) {
                setProvenanceType(((Number) provenanceType).intValue());
            }

            if (oEnd1 != null) {
                if (oEnd1 instanceof ObjectId) {
                    setEnd1((ObjectId) oEnd1);
                } else if (oEnd1 instanceof Map) {
                    setEnd1(new ObjectId((Map) oEnd1));
                }
            }

            if (oEnd2 != null) {
                if (oEnd2 instanceof ObjectId) {
                    setEnd2((ObjectId) oEnd2);
                } else if (oEnd2 instanceof Map) {
                    setEnd2(new ObjectId((Map) oEnd2));
                }
            }

            if (label != null) {
                setLabel(label.toString());
            }

            if (propagateTags != null) {
                setPropagateTags(PropagateTags.valueOf(propagateTags.toString()));
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

            if (CollectionUtils.isNotEmpty((List) propagatedClassifications)) {
                this.propagatedClassifications = new HashSet<>();

                for (Object elem : (List) propagatedClassifications) {
                    if (elem instanceof Classification) {
                        this.propagatedClassifications.add((Classification) elem);
                    } else if (elem instanceof Map) {
                        this.propagatedClassifications.add(new Classification((Map) elem));
                    }
                }
            }

            if (CollectionUtils.isNotEmpty((List) blockedPropagatedClassifications)) {
                this.blockedPropagatedClassifications = new HashSet<>();

                for (Object elem : (List) blockedPropagatedClassifications) {
                    if (elem instanceof Classification) {
                        this.blockedPropagatedClassifications.add((Classification) elem);
                    } else if (elem instanceof Map) {
                        this.blockedPropagatedClassifications.add(new Classification((Map) elem));
                    }
                }
            }
        }
    }

    public Relationship(Relationship other) {
        super(other);

        if (other != null) {
            init(other.guid, other.homeId, other.provenanceType, other.end1, other.end2, other.label, other.propagateTags, other.status, other.createdBy, other.updatedBy,
                 other.createTime, other.updateTime, other.version, other.propagatedClassifications, other.blockedPropagatedClassifications);
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

    public Integer getProvenanceType() {
        return provenanceType;
    }

    public void setProvenanceType(Integer provenanceType) { this.provenanceType = provenanceType; }

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

    public ObjectId getEnd1() { return end1; }

    public void setEnd1(ObjectId end1) { this.end1 = end1; }

    public ObjectId getEnd2() { return end2; }

    public void setEnd2(ObjectId end2) { this.end2 = end2; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public PropagateTags getPropagateTags() { return propagateTags; }

    public void setPropagateTags(PropagateTags propagateTags) { this.propagateTags = propagateTags; }

    private static String nextInternalId() {
        return "-" + Long.toString(s_nextId.getAndIncrement());
    }

    public Set<Classification> getPropagatedClassifications() {
        return propagatedClassifications;
    }

    public void setPropagatedClassifications(Set<Classification> propagatedClassifications) {
        this.propagatedClassifications = propagatedClassifications;
    }

    public Set<Classification> getBlockedPropagatedClassifications() {
        return blockedPropagatedClassifications;
    }

    public void setBlockedPropagatedClassifications(Set<Classification> blockedPropagatedClassifications) {
        this.blockedPropagatedClassifications = blockedPropagatedClassifications;
    }

    private void init() {
        init(nextInternalId(), null, 0 ,null, null, null, null, null,  null, null, null, null, 0L, null, null);
    }

    private void init(String guid, String homeId, Integer provenanceType, ObjectId end1, ObjectId end2, String label, PropagateTags propagateTags,
                      Status status, String createdBy, String updatedBy, Date createTime, Date updateTime, Long version,
                      Set<Classification> propagatedClassifications, Set<Classification> blockedPropagatedClassifications) {
        setGuid(guid);
        setHomeId(homeId);
        setProvenanceType(provenanceType);
        setEnd1(end1);
        setEnd2(end2);
        setLabel(label);
        setPropagateTags(propagateTags);
        setStatus(status);
        setCreatedBy(createdBy);
        setUpdatedBy(updatedBy);
        setCreateTime(createTime);
        setUpdateTime(updateTime);
        setVersion(version);
        setPropagatedClassifications(propagatedClassifications);
        setBlockedPropagatedClassifications(blockedPropagatedClassifications);
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("Relationship{");
        super.toString(sb);
        sb.append("guid='").append(guid).append('\'');
        sb.append(", homeId='").append(homeId).append('\'');
        sb.append(", provenanceType=").append(provenanceType);
        sb.append(", end1=").append(end1);
        sb.append(", end2=").append(end2);
        sb.append(", label='").append(label).append('\'');
        sb.append(", propagateTags=").append(propagateTags);
        sb.append(", status=").append(status);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        dumpDateField(", createTime=", createTime, sb);
        dumpDateField(", updateTime=", updateTime, sb);
        sb.append(", version=").append(version);
        sb.append(", propagatedClassifications=[");
        dumpObjects(propagatedClassifications, sb);
        sb.append("]");
        sb.append(", blockedPropagatedClassifications=[");
        dumpObjects(blockedPropagatedClassifications, sb);
        sb.append("]");
        sb.append('}');

        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        Relationship that = (Relationship) o;
        return Objects.equals(guid, that.guid) &&
                Objects.equals(homeId, that.homeId) &&
                Objects.equals(provenanceType, that.provenanceType) &&
                Objects.equals(end1, that.end1) &&
                Objects.equals(end2, that.end2) &&
                Objects.equals(label, that.label) &&
                propagateTags == that.propagateTags &&
                status == that.status &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(version, that.version) &&
                Objects.equals(propagatedClassifications, that.propagatedClassifications) &&
                Objects.equals(blockedPropagatedClassifications, that.blockedPropagatedClassifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guid, homeId, provenanceType, end1, end2, label, propagateTags, status, createdBy, updatedBy,
                            createTime, updateTime, version, propagatedClassifications, blockedPropagatedClassifications);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class RelationshipWithExtInfo implements Serializable {
        private Relationship relationship;
        private Map<String, EntityHeader> referredEntities;

        public RelationshipWithExtInfo() {
        }

        public RelationshipWithExtInfo(Relationship relationship) {
            setRelationship(relationship);
        }

        public Relationship getRelationship() {
            return relationship;
        }

        public void setRelationship(Relationship relationship) {
            this.relationship = relationship;
        }

        public Map<String, EntityHeader> getReferredEntities() {
            return referredEntities;
        }

        public void setReferredEntities(Map<String, EntityHeader> referredEntities) {
            this.referredEntities = referredEntities;
        }

        public boolean referredEntitiesContains(String guid) {
            return (referredEntities != null) && referredEntities.containsKey(guid);
        }

        @JsonIgnore
        public final void addReferredEntity(String guid, EntityHeader entityHeader) {
            Map<String, EntityHeader> r = this.referredEntities;

            if (r == null) {
                r = new HashMap<>();

                this.referredEntities = r;
            }

            if (guid != null) {
                r.put(guid, entityHeader);
            }
        }

        @JsonIgnore
        public final EntityHeader removeReferredEntity(String guid) {
            Map<String, EntityHeader> r = this.referredEntities;

            return r != null && guid != null ? r.remove(guid) : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            if (!super.equals(o)) { return false; }
            RelationshipWithExtInfo that = (RelationshipWithExtInfo) o;

            return Objects.equals(relationship, that.relationship) &&
                   Objects.equals(referredEntities, that.referredEntities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), relationship, referredEntities);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("RelationshipWithExtInfo{");
            sb.append("relationship=").append(relationship);
            sb.append(", referredEntities=").append(referredEntities);
            sb.append('}');

            return sb.toString();
        }
    }
}