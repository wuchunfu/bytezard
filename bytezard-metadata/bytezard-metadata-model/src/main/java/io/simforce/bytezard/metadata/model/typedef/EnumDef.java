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
package io.simforce.bytezard.metadata.model.typedef;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import io.simforce.bytezard.metadata.model.PageInfo;
import io.simforce.bytezard.metadata.model.SearchFilter.SortType;
import io.simforce.bytezard.metadata.model.TypeCategory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
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
 * class that captures details of an enum-type.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnumDef extends BaseTypeDef implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EnumElementDef> elementDefs;
    private String                    defaultValue;

    public EnumDef() {
        this(null, null, null, null, null, null);
    }

    public EnumDef(String name) {
        this(name, null, null, null, null, null);
    }

    public EnumDef(String name, String description) {
        this(name, description, null, null, null, null);
    }

    public EnumDef(String name, String description, String typeVersion) {
        this(name, description, typeVersion, null, null, null);
    }

    public EnumDef(String name, String description, List<EnumElementDef> elementDefs) {
        this(name, description, null, elementDefs, null, null);
    }

    public EnumDef(String name, String description, String typeVersion, List<EnumElementDef> elementDefs) {
        this(name, description, typeVersion, elementDefs, null, null);
    }

    public EnumDef(String name, String description, String typeVersion, List<EnumElementDef> elementDefs,
                   String defaultValue) {
        this(name, description, typeVersion, elementDefs, defaultValue, null);
    }

    public EnumDef(String name, String description, String typeVersion, List<EnumElementDef> elementDefs,
                   String defaultValue, Map<String, String> options) {
        this(name, description, typeVersion, elementDefs, defaultValue, null, options);
    }

    public EnumDef(String name, String description, String typeVersion, List<EnumElementDef> elementDefs,
                   String defaultValue, String serviceType, Map<String, String> options) {
        super(TypeCategory.ENUM, name, description, typeVersion, serviceType, options);

        setElementDefs(elementDefs);
        setDefaultValue(defaultValue);
    }

    public EnumDef(EnumDef other) {
        super(other);

        if (other != null) {
            setElementDefs(other.getElementDefs());
            setDefaultValue(other.getDefaultValue());
        }
    }

    public List<EnumElementDef> getElementDefs() {
        return elementDefs;
    }

    public void setElementDefs(List<EnumElementDef> elementDefs) {
        if (elementDefs != null && this.elementDefs == elementDefs) {
            return;
        }

        if (CollectionUtils.isEmpty(elementDefs)) {
            this.elementDefs = new ArrayList<>();
        } else {
            // if multiple elements with same value are present, keep only the last entry
            List<EnumElementDef> tmpList       = new ArrayList<>(elementDefs.size());
            Set<String>               elementValues = new HashSet<>();

            ListIterator<EnumElementDef> iter = elementDefs.listIterator(elementDefs.size());
            while (iter.hasPrevious()) {
                EnumElementDef elementDef   = iter.previous();
                String              elementValue = elementDef != null ? elementDef.getValue() : null;

                if (elementValue != null) {
                    elementValue = elementValue.toLowerCase();

                    if (!elementValues.contains(elementValue)) {
                        tmpList.add(new EnumElementDef(elementDef));

                        elementValues.add(elementValue);
                    }
                }
            }
            Collections.reverse(tmpList);

            this.elementDefs = tmpList;
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public EnumElementDef getElement(String elemValue) {
        return findElement(this.elementDefs, elemValue);
    }

    public void addElement(EnumElementDef elementDef) {
        List<EnumElementDef> e = this.elementDefs;

        List<EnumElementDef> tmpList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(e)) {
            // copy existing elements, except ones having same value as the element being added
            for (EnumElementDef existingElem : e) {
                if (!StringUtils.equalsIgnoreCase(existingElem.getValue(), elementDef.getValue())) {
                    tmpList.add(existingElem);
                }
            }
        }
        tmpList.add(new EnumElementDef(elementDef));

        this.elementDefs = tmpList;
    }

    public void removeElement(String elemValue) {
        List<EnumElementDef> e = this.elementDefs;

        // if element doesn't exist, no need to create the tmpList below
        if (hasElement(e, elemValue)) {
            List<EnumElementDef> tmpList = new ArrayList<>();

            // copy existing elements, except ones having same value as the element being removed
            for (EnumElementDef existingElem : e) {
                if (!StringUtils.equalsIgnoreCase(existingElem.getValue(), elemValue)) {
                    tmpList.add(existingElem);
                }
            }

            this.elementDefs = tmpList;
        }
    }

    public boolean hasElement(String elemValue) {
        return getElement(elemValue) != null;
    }

    private static boolean hasElement(List<EnumElementDef> elementDefs, String elemValue) {
        return findElement(elementDefs, elemValue) != null;
    }

    private static EnumElementDef findElement(List<EnumElementDef> elementDefs, String elemValue) {
        EnumElementDef ret = null;

        if (CollectionUtils.isNotEmpty(elementDefs)) {
            for (EnumElementDef elementDef : elementDefs) {
                if (StringUtils.equalsIgnoreCase(elementDef.getValue(), elemValue)) {
                    ret = elementDef;
                    break;
                }
            }
        }

        return ret;
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("EnumDef{");
        super.toString(sb);
        sb.append(", elementDefs=[");
        dumpObjects(elementDefs, sb);
        sb.append("]");
        sb.append(", defaultValue {");
        sb.append(defaultValue);
        sb.append('}');
        sb.append('}');

        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EnumDef that = (EnumDef) o;
        return Objects.equals(elementDefs, that.elementDefs) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elementDefs, defaultValue);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }


    /**
     * class that captures details of an enum-element.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class EnumElementDef implements Serializable {
        private static final long serialVersionUID = 1L;

        private String  value;
        private String  description;
        private Integer ordinal;

        public EnumElementDef() {
            this(null, null, null);
        }

        public EnumElementDef(String value, String description, Integer ordinal) {
            setValue(value);
            setDescription(description);
            setOrdinal(ordinal);
        }

        public EnumElementDef(EnumElementDef other) {
            if (other != null) {
                setValue(other.getValue());
                setDescription(other.getDescription());
                setOrdinal(other.getOrdinal());
            }
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getOrdinal() {
            return ordinal;
        }

        public void setOrdinal(Integer ordinal) {
            this.ordinal = ordinal;
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("EnumElementDef{");
            sb.append("value='").append(value).append('\'');
            sb.append(", description='").append(description).append('\'');
            sb.append(", ordinal=").append(ordinal);
            sb.append('}');

            return sb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnumElementDef that = (EnumElementDef) o;
            return Objects.equals(value, that.value) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(ordinal, that.ordinal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, description, ordinal);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }


    /**
     * REST serialization friendly list.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlSeeAlso(EnumDef.class)
    public static class EnumDefs extends PageInfo<EnumDef> {
        private static final long serialVersionUID = 1L;

        public EnumDefs() {
            super();
        }

        public EnumDefs(List<EnumDef> list) {
            super(list);
        }

        public EnumDefs(List list, long startIndex, int pageSize, long totalCount,
                             SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
