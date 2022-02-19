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

import org.apache.commons.lang3.StringUtils;

import io.simforce.bytezard.metadata.model.PageInfo;
import io.simforce.bytezard.metadata.model.SearchFilter.SortType;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Reference to an object-instance of an  type - like entity.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ObjectId implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KEY_GUID  = "guid";
    public static final String KEY_TYPENAME = "typeName";
    public static final String KEY_UNIQUE_ATTRIBUTES = "uniqueAttributes";

    private String guid;
    private String typeName;
    private Map<String, Object> uniqueAttributes;

    public ObjectId() {
        this(null, null, (Map<String, Object>)null);
    }

    public ObjectId(String guid) {
        this(guid, null, (Map<String, Object>)null);
    }

    public ObjectId(String guid, String typeName) {
        this(guid, typeName, (Map<String, Object>)null);
    }

    public ObjectId(String typeName, Map<String, Object> uniqueAttributes) {
        this(null, typeName, uniqueAttributes);
    }

    public ObjectId(String typeName, final String attrName, final Object attrValue) {
        this(null, typeName, new HashMap<String, Object>() {{ put(attrName, attrValue); }});
    }

    public ObjectId(String guid, String typeName, Map<String, Object> uniqueAttributes) {
        setGuid(guid);
        setTypeName(typeName);
        setUniqueAttributes(uniqueAttributes);
    }

    public ObjectId(ObjectId other) {
        if (other != null) {
            setGuid(other.getGuid());
            setTypeName(other.getTypeName());
            setUniqueAttributes(other.getUniqueAttributes());
        }
    }

    public ObjectId(Map objIdMap) {
        if (objIdMap != null) {
            Object g = objIdMap.get(KEY_GUID);
            Object t = objIdMap.get(KEY_TYPENAME);
            Object u = objIdMap.get(KEY_UNIQUE_ATTRIBUTES);

            if (g != null) {
                setGuid(g.toString());
            }

            if (t != null) {
                setTypeName(t.toString());
            }

            if (u instanceof Map) {
                setUniqueAttributes((Map)u);
            }
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Map<String, Object> getUniqueAttributes() {
        return uniqueAttributes;
    }

    public void setUniqueAttributes(Map<String, Object> uniqueAttributes) {
        this.uniqueAttributes = uniqueAttributes;
    }

    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("ObjectId{");
        sb.append("guid='").append(guid).append('\'');
        sb.append(", typeName='").append(typeName).append('\'');
        sb.append(", uniqueAttributes={");
        BaseTypeDef.dumpObjects(uniqueAttributes, sb);
        sb.append('}');
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

        ObjectId that = (ObjectId) o;

        // if guid is empty/null, equality should be based on typeName/uniqueAttributes
        if (StringUtils.isEmpty(guid) && StringUtils.isEmpty(that.guid)) {
            return Objects.equals(typeName, that.typeName) && Objects.equals(uniqueAttributes, that.uniqueAttributes);
        } else {
            return Objects.equals(guid, that.guid);
        }
    }

    @Override
    public int hashCode() {
        return guid != null ? Objects.hash(guid) : Objects.hash(typeName, uniqueAttributes);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * REST serialization friendly list.
     */
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlSeeAlso(ObjectId.class)
    public static class ObjectIds extends PageInfo<ObjectId> {
        private static final long serialVersionUID = 1L;

        public ObjectIds() {
            super();
        }

        public ObjectIds(List<ObjectId> list) {
            super(list);
        }

        public ObjectIds(List list, long startIndex, int pageSize, long totalCount,
                              SortType sortType, String sortBy) {
            super(list, startIndex, pageSize, totalCount, sortType, sortBy);
        }
    }
}
