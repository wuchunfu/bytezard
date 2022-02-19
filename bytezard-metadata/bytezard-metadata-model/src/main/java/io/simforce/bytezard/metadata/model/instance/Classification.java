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
import io.simforce.bytezard.metadata.model.TimeBoundary;
import io.simforce.bytezard.metadata.model.instance.Entity.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * An instance of a classification; it doesn't have an identity, this object exists only when associated with an entity.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Classification extends Struct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String entityGuid = null;
    private Status entityStatus = Status.ACTIVE;
    private Boolean propagate = null;
    private List<TimeBoundary> validityPeriods = null;
    private Boolean removePropagationsOnEntityDelete  = null;

    public Classification() {
        this(null, null);
    }

    public Classification(String typeName) {
        this(typeName, null);
    }

    public Classification(String typeName, Map<String, Object> attributes) {
        super(typeName, attributes);
    }

    public Classification(String typeName, String attrName, Object attrValue) {
        super(typeName, attrName, attrValue);
    }

    public Classification(Map map) {
        super(map);
    }

    public Classification(Classification other) {
        if (other != null) {
            setTypeName(other.getTypeName());
            setAttributes(other.getAttributes());
            setEntityGuid(other.getEntityGuid());
            setEntityStatus(other.getEntityStatus());
            setPropagate(other.isPropagate());
            setValidityPeriods(other.getValidityPeriods());
            setRemovePropagationsOnEntityDelete(other.getRemovePropagationsOnEntityDelete());
        }
    }

    public String getEntityGuid() {
        return entityGuid;
    }

    public void setEntityGuid(String entityGuid) {
        this.entityGuid = entityGuid;
    }

    public Boolean isPropagate() {
        return propagate;
    }

    public Boolean getPropagate() {
        return propagate;
    }

    public void setPropagate(Boolean propagate) {
        this.propagate = propagate;
    }

    public List<TimeBoundary> getValidityPeriods() {
        return validityPeriods;
    }

    public void setValidityPeriods(List<TimeBoundary> validityPeriods) {
        this.validityPeriods = validityPeriods;
    }

    public Status getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(Status entityStatus) {
        this.entityStatus = entityStatus;
    }

    public Boolean getRemovePropagationsOnEntityDelete() {
        return removePropagationsOnEntityDelete;
    }

    public void setRemovePropagationsOnEntityDelete(Boolean removePropagationsOnEntityDelete) {
        this.removePropagationsOnEntityDelete = removePropagationsOnEntityDelete;
    }

    @JsonIgnore
    public void addValityPeriod(TimeBoundary validityPeriod) {
        List<TimeBoundary> vpList = this.validityPeriods;

        if (vpList == null) {
            vpList = new ArrayList<>();

            this.validityPeriods = vpList;
        }

        vpList.add(validityPeriod);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }
        Classification that = (Classification) o;
        return Objects.equals(propagate, that.propagate) &&
               Objects.equals(removePropagationsOnEntityDelete, that.removePropagationsOnEntityDelete) &&
               Objects.equals(entityGuid, that.entityGuid) &&
               entityStatus == that.entityStatus &&
               Objects.equals(validityPeriods, that.validityPeriods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), entityGuid, entityStatus, propagate, removePropagationsOnEntityDelete);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Classification{");
        super.toString(sb);
        sb.append("entityGuid='").append(entityGuid).append('\'');
        sb.append(", entityStatus=").append(entityStatus);
        sb.append(", propagate=").append(propagate);
        sb.append(", removePropagationsOnEntityDelete=").append(removePropagationsOnEntityDelete);
        sb.append(", validityPeriods=").append(validityPeriods);
        sb.append(", validityPeriods=").append(validityPeriods);
        sb.append('}');
        return sb.toString();
    }

    /**
     * REST serialization friendly list.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlSeeAlso(Classification.class)
    public static class Classifications extends PageInfo<Classification> {
        private static final long serialVersionUID = 1L;

        public Classifications() {
            super();
        }

        public Classifications(List<Classification> list) {
            super(list);
        }

        public Classifications(List list, long startIndex, int pageSize, long totalCount,
                                    SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
