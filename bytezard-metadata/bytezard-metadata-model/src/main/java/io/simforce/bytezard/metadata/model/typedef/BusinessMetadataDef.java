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

import io.simforce.bytezard.metadata.model.TypeCategory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class BusinessMetadataDef extends StructDef implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ATTR_OPTION_APPLICABLE_ENTITY_TYPES = "applicableEntityTypes";
    public static final String ATTR_MAX_STRING_LENGTH = "maxStrLength";
    public static final String ATTR_VALID_PATTERN  = "validPattern";

    public BusinessMetadataDef() {
        this(null, null, null, null);
    }

    public BusinessMetadataDef(String name, String description) {
        this(name, description, null, null, null);
    }

    public BusinessMetadataDef(String name, String description, String typeVersion) {
        this(name, description, typeVersion, null, null);
    }

    public BusinessMetadataDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs) {
        this(name, description, typeVersion, attributeDefs, null);
    }

    public BusinessMetadataDef(String name, String description, String typeVersion, List<AttributeDef> attributeDefs, Map<String, String> options) {
        super(TypeCategory.BUSINESS_METADATA, name, description, typeVersion, attributeDefs, options);
    }

    public BusinessMetadataDef(BusinessMetadataDef other) {
        super(other);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("BusinessMetadataDef{");
        super.toString(sb);
        sb.append('}');

        return sb;
    }
}