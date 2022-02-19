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
import io.simforce.bytezard.metadata.model.glossary.relations.TermAssignmentHeader;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * An instance of an entity - like hive_table, hive_database.
 */
@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EntityHeader extends Struct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String  guid = null;
    private Entity.Status status = Entity.Status.ACTIVE;
    private String displayText = null;
    private List<String> classificationNames = null;
    private List<Classification> classifications = null;
    private List<String> meaningNames = null;
    private List<TermAssignmentHeader> meanings = null;
    private Boolean isIncomplete = Boolean.FALSE;
    private Set<String> labels = null;

    public EntityHeader() {
        this(null, null);
    }

    public EntityHeader(String typeName) {
        this(typeName, null);
    }

    public EntityHeader(EntityDef entityDef) {
        this(entityDef != null ? entityDef.getName() : null, null);
    }

    public EntityHeader(String typeName, Map<String, Object> attributes) {
        super(typeName, attributes);

        setClassificationNames(null);
        setClassifications(null);
        setLabels(null);
    }

    public EntityHeader(String typeName, String guid, Map<String, Object> attributes) {
        super(typeName, attributes);
        setGuid(guid);
        setClassificationNames(null);
        setClassifications(null);
        setLabels(null);
    }

    public EntityHeader(EntityHeader other) {
        super(other);

        if (other != null) {
            setGuid(other.getGuid());
            setStatus(other.getStatus());
            setDisplayText(other.getDisplayText());
            setClassificationNames(other.getClassificationNames());
            setClassifications(other.getClassifications());
            setIsIncomplete(other.getIsIncomplete());
            setLabels(other.getLabels());
        }
    }

    public EntityHeader(Entity entity) {
        super(entity.getTypeName(), entity.getAttributes());
        setGuid(entity.getGuid());
        setStatus(entity.getStatus());
        setClassifications(entity.getClassifications());
        setIsIncomplete(entity.getIsIncomplete());

        if (CollectionUtils.isNotEmpty(entity.getClassifications())) {
            this.classificationNames = new ArrayList<>(entity.getClassifications().size());

            for (Classification classification : entity.getClassifications()) {
                this.classificationNames.add(classification.getTypeName());
            }
        }

        if (CollectionUtils.isNotEmpty(entity.getLabels())) {
            setLabels(entity.getLabels());
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

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public List<String> getClassificationNames() {
        return classificationNames;
    }

    public void setClassificationNames(List<String> classificationNames) {
        this.classificationNames = classificationNames;
    }

    public List<Classification> getClassifications() {
        return classifications;
    }

    public void setClassifications(List<Classification> classifications) {
        this.classifications = classifications;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public Boolean getIsIncomplete() {
        return isIncomplete;
    }

    public void setIsIncomplete(Boolean isIncomplete) {
        this.isIncomplete = isIncomplete;
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("EntityHeader{");
        sb.append("guid='").append(guid).append('\'');
        sb.append(", status=").append(status);
        sb.append(", displayText=").append(displayText);
        sb.append(", classificationNames=[");
        dumpObjects(classificationNames, sb);
        sb.append("], ");
        sb.append("classifications=[");
        BaseTypeDef.dumpObjects(classifications, sb);
        sb.append("], ");
        sb.append("labels=[");
        dumpObjects(labels, sb);
        sb.append("], ");
        sb.append("isIncomplete=").append(isIncomplete);
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

        EntityHeader that = (EntityHeader) o;
        return Objects.equals(guid, that.guid) &&
                       status == that.status &&
                       Objects.equals(displayText, that.displayText) &&
                       Objects.equals(classificationNames, that.classificationNames) &&
                       Objects.equals(meaningNames, that.classificationNames) &&
                       Objects.equals(classifications, that.classifications) &&
                       Objects.equals(labels, that.labels) &&
                       Objects.equals(isIncomplete, that.isIncomplete) &&
                       Objects.equals(meanings, that.meanings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guid, status, displayText, classificationNames, classifications, meaningNames, meanings, isIncomplete, labels);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public List<String> getMeaningNames() {
        return meaningNames;
    }

    public void setMeaningNames(final List<String> meaningNames) {
        this.meaningNames = meaningNames;
    }

    public List<TermAssignmentHeader> getMeanings() {
        return meanings;
    }

    public void setMeanings(final List<TermAssignmentHeader> meanings) {
        this.meanings = meanings;
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
    public static class EntityHeaders extends PageInfo<EntityHeader> {
        private static final long serialVersionUID = 1L;

        public EntityHeaders() {
            super();
        }

        public EntityHeaders(List<EntityHeader> list) {
            super(list);
        }

        public EntityHeaders(List list, long startIndex, int pageSize, long totalCount,
                                  SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
