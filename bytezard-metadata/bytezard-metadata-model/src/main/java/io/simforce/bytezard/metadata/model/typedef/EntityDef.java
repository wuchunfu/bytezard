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
import org.apache.commons.collections.MapUtils;

import java.io.Serializable;
import java.util.HashSet;
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
 * class that captures details of a entity-type.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EntityDef extends StructDef implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String OPTION_DISPLAY_TEXT_ATTRIBUTE = "displayTextAttribute";

    private Set<String> superTypes;

    // this is a read-only field, any value provided during create & update operation is ignored
    // the value of this field is derived from 'superTypes' specified in all EntityDef
    private Set<String> subTypes;

    // this is a read-only field, any value provided during create & update operation is ignored
    // the value of this field is derived from all the relationshipDefs this entityType is referenced in
    private List<RelationshipAttributeDef> relationshipAttributeDefs;

    // this is a read-only field, any value provided during create & update operation is ignored
    // the value of this field is derived from all the businessMetadataDefs this entityType is referenced in
    private Map<String, List<AttributeDef>> businessAttributeDefs;


    public EntityDef() {
        this(null, null, null, null, null, null, null);
    }

    public EntityDef(String name) {
        this(name, null, null, null, null, null, null);
    }

    public EntityDef(String name, String description) {
        this(name, description, null, null, null, null, null);
    }

    public EntityDef(String name, String description, String typeVersion) {
        this(name, description, typeVersion, null, null, null, null);
    }
    
    public EntityDef(String name, String description, String typeVersion, String serviceType) {
        this(name, description, typeVersion, serviceType, null, null, null);
    }


    public EntityDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs) {
        this(name, description, typeVersion, attributeDefs, null);
    }
    
    public EntityDef(String name, String description, String typeVersion, String serviceType, List<AttributeDef> attributeDefs) {
        this(name, description, typeVersion, serviceType, attributeDefs, null, null);
    }

    public EntityDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs,
                     Set<String> superTypes) {
        this(name, description, typeVersion, attributeDefs, superTypes, null);
    }
    
    public EntityDef(String name, String description, String typeVersion, String serviceType, List<AttributeDef> attributeDefs,
                     Set<String> superTypes) {
    	this(name, description, typeVersion, serviceType, attributeDefs, superTypes, null);
    }


    public EntityDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs,
                     Set<String> superTypes, Map<String, String> options) {
        super(TypeCategory.ENTITY, name, description, typeVersion, attributeDefs, options);

        setSuperTypes(superTypes);
    }
    
    public EntityDef(String name, String description, String typeVersion, String serviceType, List<AttributeDef> attributeDefs,
                     Set<String> superTypes, Map<String, String> options) {
    	super(TypeCategory.ENTITY, name, description, typeVersion, attributeDefs, serviceType, options);

		setSuperTypes(superTypes);
	}


    public EntityDef(EntityDef other) {
        super(other);

        if (other != null) {
            setSuperTypes(other.getSuperTypes());
            setSubTypes(other.getSubTypes());
            setRelationshipAttributeDefs(other.getRelationshipAttributeDefs());
            setBusinessAttributeDefs(other.getBusinessAttributeDefs());
        }
    }



	public Set<String> getSuperTypes() {
        return superTypes;
    }

    public void setSuperTypes(Set<String> superTypes) {
        if (superTypes != null && this.superTypes == superTypes) {
            return;
        }

        if (CollectionUtils.isEmpty(superTypes)) {
            this.superTypes = new HashSet<>();
        } else {
            this.superTypes = new HashSet<>(superTypes);
        }
    }

    public Set<String> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(Set<String> subTypes) {
        this.subTypes = subTypes;
    }

    public List<RelationshipAttributeDef> getRelationshipAttributeDefs() {
        return relationshipAttributeDefs;
    }

    public void setRelationshipAttributeDefs(List<RelationshipAttributeDef> relationshipAttributeDefs) {
        this.relationshipAttributeDefs = relationshipAttributeDefs;
    }

    public Map<String, List<AttributeDef>> getBusinessAttributeDefs() {
        return businessAttributeDefs;
    }

    public void setBusinessAttributeDefs(Map<String, List<AttributeDef>> businessAttributeDefs) {
        this.businessAttributeDefs = businessAttributeDefs;
    }

    public boolean hasSuperType(String typeName) {
        return hasSuperType(superTypes, typeName);
    }

    public void addSuperType(String typeName) {
        Set<String> s = this.superTypes;

        if (!hasSuperType(s, typeName)) {
            s = new HashSet<>(s);

            s.add(typeName);

            this.superTypes = s;
        }
    }

    public void removeSuperType(String typeName) {
        Set<String> s = this.superTypes;

        if (hasSuperType(s, typeName)) {
            s = new HashSet<>(s);

            s.remove(typeName);

            this.superTypes = s;
        }
    }

    private static boolean hasSuperType(Set<String> superTypes, String typeName) {
        return superTypes != null && typeName != null && superTypes.contains(typeName);
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("EntityDef{");
        super.toString(sb);
        sb.append(", superTypes=[");
        dumpObjects(superTypes, sb);
        sb.append("]");
        sb.append(", relationshipAttributeDefs=[");
        if (CollectionUtils.isNotEmpty(relationshipAttributeDefs)) {
            int i = 0;
            for (RelationshipAttributeDef attributeDef : relationshipAttributeDefs) {
                if (i > 0) {
                    sb.append(", ");
                }

                attributeDef.toString(sb);

                i++;
            }
        }
        sb.append(']');
        sb.append(", businessAttributeDefs={");
        if (MapUtils.isNotEmpty(businessAttributeDefs)) {
            int nsIdx = 0;

            for (Map.Entry<String, List<AttributeDef>> entry : businessAttributeDefs.entrySet()) {
                String                  nsName  = entry.getKey();
                List<AttributeDef> nsAttrs = entry.getValue();

                if (nsIdx > 0) {
                    sb.append(", ");
                }

                sb.append(nsName).append("=[");

                int attrIdx = 0;
                for (AttributeDef attributeDef : nsAttrs) {
                    if (attrIdx > 0) {
                        sb.append(", ");
                    }

                    attributeDef.toString(sb);

                    attrIdx++;
                }
                sb.append(']');

                nsIdx++;
            }
        }
        sb.append('}');
        sb.append('}');

        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        EntityDef that = (EntityDef) o;
        return Objects.equals(superTypes, that.superTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), superTypes);
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
    public static class RelationshipAttributeDef extends AttributeDef implements Serializable {
        private static final long serialVersionUID = 1L;

        private String  relationshipTypeName;
        private boolean isLegacyAttribute;

        public RelationshipAttributeDef() { }

        public RelationshipAttributeDef(String relationshipTypeName, boolean isLegacyAttribute, AttributeDef attributeDef) {
            super(attributeDef);

            this.relationshipTypeName = relationshipTypeName;
            this.isLegacyAttribute    = isLegacyAttribute;
        }

        public String getRelationshipTypeName() {
            return relationshipTypeName;
        }

        public void setRelationshipTypeName(String relationshipTypeName) {
            this.relationshipTypeName = relationshipTypeName;
        }

        public boolean getIsLegacyAttribute() {
            return isLegacyAttribute;
        }

        public void setIsLegacyAttribute(boolean isLegacyAttribute) {
            this.isLegacyAttribute = isLegacyAttribute;
        }

        @Override
        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("RelationshipAttributeDef{");
            super.toString(sb);
            sb.append(", relationshipTypeName='").append(relationshipTypeName).append('\'');
            sb.append(", isLegacyAttribute='").append(isLegacyAttribute).append('\'');
            sb.append('}');

            return sb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            RelationshipAttributeDef that = (RelationshipAttributeDef) o;

            return super.equals(that) &&
                   isLegacyAttribute == that.isLegacyAttribute &&
                   Objects.equals(relationshipTypeName, that.relationshipTypeName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), relationshipTypeName, isLegacyAttribute);
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
    @XmlSeeAlso(EntityDef.class)
    public static class EntityDefs extends PageInfo<EntityDef> {
        private static final long serialVersionUID = 1L;

        public EntityDefs() {
            super();
        }

        public EntityDefs(List<EntityDef> list) {
            super(list);
        }

        public EntityDefs(List list, long startIndex, int pageSize, long totalCount,
                               SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
