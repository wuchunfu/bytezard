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
package io.simforce.bytezard.metadata.type;

import static io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef.ATTR_MAX_STRING_LENGTH;
import static io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef.ATTR_OPTION_APPLICABLE_ENTITY_TYPES;
import static io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef.ATTR_VALID_PATTERN;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.Struct;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessMetadataType extends StructType {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessMetadataType.class);

    private final BusinessMetadataDef businessMetadataDef;

    public BusinessMetadataType(BusinessMetadataDef businessMetadataDef) {
        super(businessMetadataDef);

        this.businessMetadataDef = businessMetadataDef;
    }

    @Override
    public boolean isValidValue(Object o) {
        // there is no runtime instance for businessMetadataDef, so return true
        return true;
    }

    @Override
    public Struct createDefaultValue() {
        // there is no runtime instance for businessMetadataDef, so return null
        return null;
    }

    @Override
    public Object getNormalizedValue(Object a) {
        // there is no runtime instance for businessMetadataDef, so return null
        return null;
    }

    public BusinessMetadataDef getBusinessMetadataDef() {
        return businessMetadataDef;
    }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferences(typeRegistry);

        Map<String, BusinessAttribute> a = new HashMap<>();

        for (Attribute attribute : super.allAttributes.values()) {
            AttributeDef attributeDef = attribute.getAttributeDef();
            String            attrName     = attribute.getName();
            io.simforce.bytezard.metadata.type.Type attrType     = attribute.getAttributeType();

            if (attrType instanceof ArrayType) {
                attrType = ((ArrayType) attrType).getElementType();
            } else if (attrType instanceof MapType) {
                attrType = ((MapType) attrType).getValueType();
            }

            // check if attribute type is not struct/classification/entity/business-metadata
            if (attrType instanceof io.simforce.bytezard.metadata.type.StructType) {
                throw new BaseException(ErrorCode.BUSINESS_METADATA_DEF_ATTRIBUTE_TYPE_INVALID, getTypeName(), attrName);
            }

            Set<String>          entityTypeNames = attribute.getOptionSet(ATTR_OPTION_APPLICABLE_ENTITY_TYPES);
            Set<io.simforce.bytezard.metadata.type.EntityType> entityTypes     = new HashSet<>();

            if (CollectionUtils.isNotEmpty(entityTypeNames)) {
                for (String entityTypeName : entityTypeNames) {
                    io.simforce.bytezard.metadata.type.EntityType entityType = typeRegistry.getEntityTypeByName(entityTypeName);

                    if (entityType == null) {
                        throw new BaseException(ErrorCode.TYPE_NAME_NOT_FOUND, entityTypeName);
                    }

                    entityTypes.add(entityType);
                }
            }

            BusinessAttribute bmAttribute;
            if (attrType instanceof BuiltInTypes.StringType) {
                Integer maxStringLength = attribute.getOptionInt(ATTR_MAX_STRING_LENGTH);
                if (maxStringLength == null) {
                    throw new BaseException(ErrorCode.MISSING_MANDATORY_ATTRIBUTE, attributeDef.getName(), "options." + ATTR_MAX_STRING_LENGTH);
                }

                String validPattern = attribute.getOptionString(ATTR_VALID_PATTERN);
                bmAttribute = new BusinessAttribute(attribute, entityTypes, maxStringLength, validPattern);
            } else {
                bmAttribute = new BusinessAttribute(attribute, entityTypes);
            }

            a.put(attrName, bmAttribute);
        }

        super.allAttributes = Collections.unmodifiableMap(a);
    }

    @Override
    void resolveReferencesPhase2(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferencesPhase2(typeRegistry);

        for (Attribute attribute : super.allAttributes.values()) {
            BusinessAttribute bmAttribute = (BusinessAttribute) attribute;
            Set<io.simforce.bytezard.metadata.type.EntityType>   entityTypes = bmAttribute.getApplicableEntityTypes();

            if (CollectionUtils.isNotEmpty(entityTypes)) {
                for (io.simforce.bytezard.metadata.type.EntityType entityType : entityTypes) {
                    entityType.addBusinessAttribute(bmAttribute);
                }
            }
        }
    }

    public static class BusinessAttribute extends Attribute {
        private final Set<io.simforce.bytezard.metadata.type.EntityType> applicableEntityTypes;
        private final int                  maxStringLength;
        private final String               validPattern;

        public BusinessAttribute(Attribute attribute, Set<io.simforce.bytezard.metadata.type.EntityType> applicableEntityTypes) {
            super(attribute);

            this.maxStringLength       = 0;
            this.validPattern          = null;
            this.applicableEntityTypes = applicableEntityTypes;
        }

        public BusinessAttribute(Attribute attribute, Set<io.simforce.bytezard.metadata.type.EntityType> applicableEntityTypes, int maxStringLength, String validPattern) {
            super(attribute);

            this.maxStringLength       = maxStringLength;
            this.validPattern          = validPattern;
            this.applicableEntityTypes = applicableEntityTypes;
        }

        @Override
        public io.simforce.bytezard.metadata.type.BusinessMetadataType getDefinedInType() {
            return (io.simforce.bytezard.metadata.type.BusinessMetadataType) super.getDefinedInType();
        }

        public Set<io.simforce.bytezard.metadata.type.EntityType> getApplicableEntityTypes() {
            return applicableEntityTypes;
        }

        public String getValidPattern() {
            return validPattern;
        }

        public int getMaxStringLength() {
            return maxStringLength;
        }

        public boolean isValidLength(Object value) {
            boolean ret = true;
            if (value != null) {
                io.simforce.bytezard.metadata.type.Type attrType = getAttributeType();

                if (attrType instanceof BuiltInTypes.StringType) {
                    ret = isValidStringValue(value);
                } else if (attrType instanceof ArrayType) {
                    attrType = ((ArrayType) attrType).getElementType();
                    if (attrType instanceof BuiltInTypes.StringType) {
                        ret = isValidArrayValue(value);
                    }
                }
            }
            return ret;
        }

        private boolean isValidStringValue(Object obj) {
            return obj == null || String.valueOf(obj).length() <= this.maxStringLength;
        }

        private boolean isValidArrayValue(Object obj) {
            if (obj instanceof List || obj instanceof Set) {
                Collection objList = (Collection) obj;

                for (Object element : objList) {
                    if (!isValidStringValue(element)) {
                        return false;
                    }
                }
            } else if (obj.getClass().isArray()) {
                int arrayLen = Array.getLength(obj);
                for (int i = 0; i < arrayLen; i++) {
                    if (!isValidStringValue(Array.get(obj, i))) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
