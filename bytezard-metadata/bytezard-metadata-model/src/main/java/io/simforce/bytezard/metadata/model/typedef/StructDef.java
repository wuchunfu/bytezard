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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * class that captures details of a struct-type.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class StructDef extends BaseTypeDef implements Serializable {
    private static final long serialVersionUID = 1L;

    // do not update this list contents directly - the list might be in the middle of iteration in another thread
    // to update list contents: 1) make a copy 2) update the copy 3) assign the copy to this member
    private List<AttributeDef> attributeDefs;

    public StructDef() {
        this(null, null, null, null, null);
    }

    public StructDef(String name) {
        this(name, null, null, null, null);
    }

    public StructDef(String name, String description) {
        this(name, description, null, null, null);
    }

    public StructDef(String name, String description, String typeVersion) {
        this(name, description, typeVersion, null, null);
    }

    public StructDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs) {
        this(name, description, typeVersion, attributeDefs, null);
    }

    public StructDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs, Map<String, String> options) {
        this(TypeCategory.STRUCT, name, description, typeVersion, attributeDefs, options);
    }

    protected StructDef(TypeCategory category, String name, String description, String typeVersion, List<AttributeDef> attributeDefs, Map<String, String> options) {
        this(category, name, description, typeVersion, attributeDefs, null, options);
    }

    protected StructDef(TypeCategory category, String name, String description, String typeVersion, List<AttributeDef> attributeDefs, String serviceType, Map<String, String> options) {
        super(category, name, description, typeVersion, serviceType, options);

        setAttributeDefs(attributeDefs);
    }

    public StructDef(StructDef other) {
        super(other);

        setAttributeDefs(other != null ? other.getAttributeDefs() : null);
    }

    public List<AttributeDef> getAttributeDefs() {
        return attributeDefs;
    }

    public void setAttributeDefs(List<AttributeDef> attributeDefs) {
        if (this.attributeDefs != null && this.attributeDefs == attributeDefs) {
            return;
        }

        if (CollectionUtils.isEmpty(attributeDefs)) {
            this.attributeDefs = new ArrayList<>();
        } else {
            // if multiple attributes with same name are present, keep only the last entry
            List<AttributeDef> tmpList     = new ArrayList<>(attributeDefs.size());
            Set<String>             attribNames = new HashSet<>();

            ListIterator<AttributeDef> iter = attributeDefs.listIterator(attributeDefs.size());
            while (iter.hasPrevious()) {
                AttributeDef attributeDef = iter.previous();
                String            attribName   = attributeDef != null ? attributeDef.getName() : null;

                if (attribName != null) {
                    attribName = attribName.toLowerCase();

                    if (!attribNames.contains(attribName)) {
                        tmpList.add(new AttributeDef(attributeDef));

                        attribNames.add(attribName);
                    }
                }
            }
            Collections.reverse(tmpList);

            this.attributeDefs = tmpList;
        }
    }

    public AttributeDef getAttribute(String attrName) {
        return findAttribute(this.attributeDefs, attrName);
    }

    public void addAttribute(AttributeDef attributeDef) {
        if (attributeDef == null) {
            return;
        }

        List<AttributeDef> a = this.attributeDefs;

        List<AttributeDef> tmpList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(a)) {
            // copy existing attributes, except ones having same name as the attribute being added
            for (AttributeDef existingAttrDef : a) {
                if (!StringUtils.equalsIgnoreCase(existingAttrDef.getName(), attributeDef.getName())) {
                    tmpList.add(existingAttrDef);
                }
            }
        }
        tmpList.add(new AttributeDef(attributeDef));

        this.attributeDefs = tmpList;
    }

    public void removeAttribute(String attrName) {
        List<AttributeDef> a = this.attributeDefs;

        if (hasAttribute(a, attrName)) {
            List<AttributeDef> tmpList = new ArrayList<>();

            // copy existing attributes, except ones having same name as the attribute being removed
            for (AttributeDef existingAttrDef : a) {
                if (!StringUtils.equalsIgnoreCase(existingAttrDef.getName(), attrName)) {
                    tmpList.add(existingAttrDef);
                }
            }

            this.attributeDefs = tmpList;
        }
    }

    public boolean hasAttribute(String attrName) {
        return getAttribute(attrName) != null;
    }

    private static boolean hasAttribute(List<AttributeDef> attributeDefs, String attrName) {
        return findAttribute(attributeDefs, attrName) != null;
    }

    public static AttributeDef findAttribute(Collection<AttributeDef> attributeDefs, String attrName) {
        AttributeDef ret = null;

        if (CollectionUtils.isNotEmpty(attributeDefs)) {
            for (AttributeDef attributeDef : attributeDefs) {
                if (StringUtils.equalsIgnoreCase(attributeDef.getName(), attrName)) {
                    ret = attributeDef;
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

        sb.append("StructDef{");
        super.toString(sb);
        sb.append(", attributeDefs=[");
        if (CollectionUtils.isNotEmpty(attributeDefs)) {
            int i = 0;
            for (AttributeDef attributeDef : attributeDefs) {
                attributeDef.toString(sb);
                if (i > 0) {
                    sb.append(", ");
                }
                i++;
            }
        }
        sb.append("]");
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
        StructDef that = (StructDef) o;
        return Objects.equals(attributeDefs, that.attributeDefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attributeDefs);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * class that captures details of a struct-attribute.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class AttributeDef implements Serializable {
        private static final long serialVersionUID = 1L;
        public static final int  DEFAULT_SEARCHWEIGHT = -1;

        public static final String SEARCH_WEIGHT_ATTR_NAME = "searchWeight";
        public static final String INDEX_TYPE_ATTR_NAME = "indexType";
        public static final String ATTRDEF_OPTION_SOFT_REFERENCE = "isSoftReference";
        public static final String ATTRDEF_OPTION_APPEND_ON_PARTIAL_UPDATE = "isAppendOnPartialUpdate";
        private final String STRING_TRUE = "true";

        /**
         * single-valued attribute or multi-valued attribute.
         */
        public enum Cardinality { SINGLE, LIST, SET }

        public enum IndexType { DEFAULT, STRING}

        public static final int COUNT_NOT_SET = -1;

        private String name;
        private String typeName;
        private boolean isOptional;
        private Cardinality cardinality;
        private int valuesMinCount;
        private int valuesMaxCount;
        private boolean isUnique;
        private boolean isIndexable;
        private boolean includeInNotification;
        private String defaultValue;
        private String description;
        private int searchWeight = DEFAULT_SEARCHWEIGHT;
        private IndexType indexType = null;
        private List<ConstraintDef> constraints;
        private Map<String, String> options;
        private String displayName;

        public AttributeDef() { this(null, null); }

        public AttributeDef(String name, String typeName) {
            this(name, typeName, DEFAULT_SEARCHWEIGHT);
        }

        public AttributeDef(String name, String typeName, boolean isUnique, boolean isIndexable) {
            this(name, typeName, false, Cardinality.SINGLE, COUNT_NOT_SET, COUNT_NOT_SET, isUnique, isIndexable,
                false, null,null, null, null, DEFAULT_SEARCHWEIGHT, null);
        }

        public AttributeDef(String name, String typeName, Cardinality cardinality, boolean isUnique, boolean isIndexable) {
            this(name, typeName, false, cardinality, COUNT_NOT_SET, COUNT_NOT_SET, isUnique, isIndexable,
                false, null,null, null, null, DEFAULT_SEARCHWEIGHT, null);
        }

        public AttributeDef(String name, String typeName, int searchWeight) {
            this(name, typeName, false, Cardinality.SINGLE, searchWeight, null);
        }

        public AttributeDef(String name, String typeName, int searchWeight, IndexType indexType) {
            this(name, typeName, false, Cardinality.SINGLE, searchWeight, indexType);
        }

        public AttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality) {
            this(name, typeName, isOptional, cardinality, DEFAULT_SEARCHWEIGHT, null);
        }

        private AttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality, int searchWeight, IndexType indexType) {
            this(name, typeName, isOptional, cardinality, COUNT_NOT_SET, COUNT_NOT_SET, false, false, false, null, searchWeight, indexType);
        }

        public AttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality,
                                 int valuesMinCount, int valuesMaxCount, boolean isUnique, boolean isIndexable, boolean includeInNotification, List<ConstraintDef> constraints) {
            this(name, typeName, isOptional, cardinality, valuesMinCount, valuesMaxCount, isUnique, isIndexable, includeInNotification, constraints, DEFAULT_SEARCHWEIGHT, null);
        }

        private AttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality,
                                  int valuesMinCount, int valuesMaxCount, boolean isUnique, boolean isIndexable, boolean includeInNotification, List<ConstraintDef> constraints, int searchWeight, IndexType indexType) {
            this(name, typeName, isOptional, cardinality, valuesMinCount, valuesMaxCount, isUnique, isIndexable, includeInNotification, null, constraints, null, null, searchWeight, indexType);
        }

        public AttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality,
                                 int valuesMinCount, int valuesMaxCount, boolean isUnique, boolean isIndexable, boolean includeInNotification, String defaultValue,
                                 List<ConstraintDef> constraints, Map<String,String> options, String description, int searchWeight, IndexType indexType) {
            setName(name);
            setTypeName(typeName);
            setIsOptional(isOptional);
            setCardinality(cardinality);
            setValuesMinCount(valuesMinCount);
            setValuesMaxCount(valuesMaxCount);
            setIsUnique(isUnique);
            setIsIndexable(isIndexable);
            setIncludeInNotification(includeInNotification);
            setDefaultValue(defaultValue);
            setConstraints(constraints);
            setOptions(options);
            setDescription(description);
            setSearchWeight(searchWeight);
            setIndexType(indexType);
        }

        public AttributeDef(AttributeDef other) {
            if (other != null) {
                setName(other.getName());
                setTypeName(other.getTypeName());
                setIsOptional(other.getIsOptional());
                setCardinality(other.getCardinality());
                setValuesMinCount(other.getValuesMinCount());
                setValuesMaxCount(other.getValuesMaxCount());
                setIsUnique(other.getIsUnique());
                setIsIndexable(other.getIsIndexable());
                setIncludeInNotification(other.getIncludeInNotification());
                setDefaultValue(other.getDefaultValue());
                setConstraints(other.getConstraints());
                setOptions(other.getOptions());
                setDescription((other.getDescription()));
                setSearchWeight(other.getSearchWeight());
                setIndexType(other.getIndexType());
                setDisplayName(other.getDisplayName());
            }
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getSearchWeight() {
            return searchWeight;
        }

        public void setSearchWeight(int searchWeight) {
            this.searchWeight = searchWeight;
        }

        public void setIndexType(IndexType indexType) {
            this.indexType = indexType;
        }

        public IndexType getIndexType() {
            return indexType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public boolean getIsOptional() {
            return isOptional;
        }

        public void setIsOptional(boolean optional) { isOptional = optional; }

        public void setCardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
        }

        public Cardinality getCardinality() {
            return cardinality;
        }

        public int getValuesMinCount() {
            return valuesMinCount;
        }

        public void setValuesMinCount(int valuesMinCount) {
            this.valuesMinCount = valuesMinCount;
        }

        public int getValuesMaxCount() {
            return valuesMaxCount;
        }

        public void setValuesMaxCount(int valuesMaxCount) {
            this.valuesMaxCount = valuesMaxCount;
        }

        public boolean getIsUnique() {
            return isUnique;
        }

        public void setIsUnique(boolean unique) {
            isUnique = unique;
        }

        public boolean getIsIndexable() {
            return isIndexable;
        }

        public boolean getIncludeInNotification() { return includeInNotification; }

        public void setIncludeInNotification(Boolean isInNotification) { this.includeInNotification = isInNotification == null ? Boolean.FALSE : isInNotification; }

        public String getDefaultValue(){
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue){
            this.defaultValue = defaultValue;
        }

        public void setIsIndexable(boolean idexable) {
            isIndexable = idexable;
        }

        public List<ConstraintDef> getConstraints() { return constraints; }

        public void setConstraints(List<ConstraintDef> constraints) {
            if (this.constraints != null && this.constraints == constraints) {
                return;
            }

            if (CollectionUtils.isEmpty(constraints)) {
                this.constraints = null;
            } else {
                this.constraints = new ArrayList<>(constraints);
            }
        }

        public void addConstraint(ConstraintDef constraintDef) {
            List<ConstraintDef> cDefs = constraints;

            if (cDefs == null) {
                cDefs = new ArrayList<>();

                this.constraints = cDefs;
            }

            cDefs.add(constraintDef);
        }
        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            if (options != null) {
                this.options = new HashMap<>(options);
            } else {
                this.options = null;
            }
        }

        @JsonIgnore
        public boolean isSoftReferenced() {
            return this.options != null &&
                    getOptions().containsKey(AttributeDef.ATTRDEF_OPTION_SOFT_REFERENCE) &&
                    getOptions().get(AttributeDef.ATTRDEF_OPTION_SOFT_REFERENCE).equals(STRING_TRUE);
        }

        @JsonIgnore
        public boolean isAppendOnPartialUpdate() {
            String val = getOption(AttributeDef.ATTRDEF_OPTION_APPEND_ON_PARTIAL_UPDATE);

            return Boolean.parseBoolean(val);
        }

        @JsonIgnore
        public void setOption(String name, String value) {
            if (this.options == null) {
                this.options = new HashMap<>();
            }

            this.options.put(name, value);
        }

        @JsonIgnore
        public String getOption(String name) {
            Map<String, String> option = this.options;

            return option != null ? option.get(name) : null;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("AttributeDef{");
            sb.append("name='").append(name).append('\'');
            sb.append(", typeName='").append(typeName).append('\'');
            sb.append(", description='").append(description).append('\'');
            sb.append(", getIsOptional=").append(isOptional);
            sb.append(", cardinality=").append(cardinality);
            sb.append(", valuesMinCount=").append(valuesMinCount);
            sb.append(", valuesMaxCount=").append(valuesMaxCount);
            sb.append(", isUnique=").append(isUnique);
            sb.append(", isIndexable=").append(isIndexable);
            sb.append(", includeInNotification=").append(includeInNotification);
            sb.append(", defaultValue=").append(defaultValue);
            sb.append(", options='").append(options).append('\'');
            sb.append(", searchWeight='").append(searchWeight).append('\'');
            sb.append(", indexType='").append(indexType).append('\'');
            sb.append(", displayName='").append(displayName).append('\'');
            sb.append(", constraints=[");
            if (CollectionUtils.isNotEmpty(constraints)) {
                int i = 0;
                for (ConstraintDef constraintDef : constraints) {
                    constraintDef.toString(sb);
                    if (i > 0) {
                        sb.append(", ");
                    }
                    i++;
                }
            }
            sb.append("]");
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

            AttributeDef that = (AttributeDef) o;
            return isOptional == that.isOptional &&
                    valuesMinCount == that.valuesMinCount &&
                    valuesMaxCount == that.valuesMaxCount &&
                    isUnique == that.isUnique &&
                    isIndexable == that.isIndexable &&
                    includeInNotification == that.includeInNotification &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(typeName, that.typeName) &&
                    cardinality == that.cardinality &&
                    Objects.equals(defaultValue, that.defaultValue) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(constraints, that.constraints) &&
                    Objects.equals(options, that.options) &&
                    Objects.equals(searchWeight, that.searchWeight) &&
                    Objects.equals(indexType, that.indexType) &&
                    Objects.equals(displayName, that.displayName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, typeName, isOptional, cardinality, valuesMinCount, valuesMaxCount, isUnique, isIndexable, includeInNotification, defaultValue, constraints, options, description, searchWeight, indexType, displayName);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }



    /**
     * class that captures details of a constraint.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class ConstraintDef implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final String CONSTRAINT_TYPE_OWNED_REF   = "ownedRef";
        public static final String CONSTRAINT_TYPE_INVERSE_REF = "inverseRef";
        public static final String CONSTRAINT_PARAM_ATTRIBUTE  = "attribute";

        private String type;   // foreignKey/mappedFromRef/valueInRange
        private Map<String, Object> params; // onDelete=cascade/refAttribute=attr2/min=0,max=23

        public ConstraintDef() { }

        public ConstraintDef(String type) {
            this(type, null);
        }

        public ConstraintDef(String type, Map<String, Object> params) {
            this.type = type;

            if (params != null) {
                this.params = new HashMap<>(params);
            }
        }

        public ConstraintDef(ConstraintDef that) {
            if (that != null) {
                this.type = that.type;

                if (that.params != null) {
                    this.params = new HashMap<>(that.params);
                }
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        @JsonIgnore
        public boolean isConstraintType(String name) {
            return StringUtils.equalsIgnoreCase(name, this.type);
        }

        @JsonIgnore
        public Object getParam(String name) {
            Map<String, Object> params = this.params;

            return params != null ? params.get(name) : null;
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("ConstraintDef{");
            sb.append("type='").append(type).append('\'');
            sb.append(", params='").append(params).append('\'');
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
            ConstraintDef that = (ConstraintDef) o;
            return Objects.equals(type, that.type) &&
                    Objects.equals(params, that.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, params);
        }

        @Override
        public String toString() { return toString(new StringBuilder()).toString(); }
    }

    /**
     * REST serialization friendly list.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlSeeAlso(StructDef.class)
    public static class StructDefs extends PageInfo<StructDef> {
        private static final long serialVersionUID = 1L;

        public StructDefs() {
            super();
        }

        public StructDefs(List<StructDef> list) {
            super(list);
        }

        public StructDefs(List list, long startIndex, int pageSize, long totalCount,
                               SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
