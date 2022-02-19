/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RelationshipHeader extends Struct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String guid = null;
    private Entity.Status status = Entity.Status.ACTIVE;
    private RelationshipDef.PropagateTags propagateTags = RelationshipDef.PropagateTags.NONE;
    private String label = null;
    private ObjectId end1 = null;
    private ObjectId end2  = null;

    public RelationshipHeader() {

    }

    public RelationshipHeader(String typeName, String guid) {
        super(typeName);
        setGuid(guid);
    }

    public RelationshipHeader(String typeName, String guid, ObjectId end1, ObjectId end2, RelationshipDef.PropagateTags propagateTags) {
        this(typeName, guid);
        this.propagateTags = propagateTags;
        setEnd1(end1);
        setEnd2(end2);
    }

    public RelationshipHeader(Relationship relationship) {
        this(relationship.getTypeName(), relationship.getGuid(), relationship.getEnd1(), relationship.getEnd2(), relationship.getPropagateTags());

        setLabel(relationship.getLabel());
        switch (relationship.getStatus()) {
            case ACTIVE:
                setStatus(Entity.Status.ACTIVE);
                break;

            case DELETED:
                setStatus(Entity.Status.DELETED);
                break;
        }
    }


    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Entity.Status getStatus() {
        return status;
    }

    public void setStatus(Entity.Status status) {
        this.status = status;
    }

    public RelationshipDef.PropagateTags getPropagateTags() {
        return propagateTags;
    }

    public void setPropagateTags(RelationshipDef.PropagateTags propagateTags) {
        this.propagateTags = propagateTags;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ObjectId getEnd1() {
        return end1;
    }

    public void setEnd1(ObjectId end1) {
        this.end1 = end1;
    }

    public ObjectId getEnd2() {
        return end2;
    }

    public void setEnd2(ObjectId end2) {
        this.end2 = end2;
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("RelationshipHeader{");
        sb.append("guid='").append(guid).append('\'');
        sb.append(", status=").append(status);
        sb.append(", label=").append(label);
        sb.append(", propagateTags=").append(propagateTags);
        sb.append(", end1=").append(end1);
        sb.append(", end2=").append(end2);
        super.toString(sb);
        sb.append('}');

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

        if (!super.equals(o)) {
            return false;
        }

        RelationshipHeader that = (RelationshipHeader) o;
        return Objects.equals(guid, that.guid) &&
                       status == that.status &&
                       Objects.equals(label, that.label) &&
                       Objects.equals(propagateTags, that.propagateTags) &&
                       Objects.equals(end1, that.end1) &&
                       Objects.equals(end2, that.end2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guid, status, label, propagateTags, end1, end2);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * REST serialization friendly list.
     */
    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlSeeAlso(Entity.class)
    public static class RelationshipHeaders extends PageInfo<RelationshipHeader> {
        private static final long serialVersionUID = 1L;

        public RelationshipHeaders() {
            super();
        }

        public RelationshipHeaders(List<RelationshipHeader> list) {
            super(list);
        }

        public RelationshipHeaders(List list, long startIndex, int pageSize, long totalCount,
                                  SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
