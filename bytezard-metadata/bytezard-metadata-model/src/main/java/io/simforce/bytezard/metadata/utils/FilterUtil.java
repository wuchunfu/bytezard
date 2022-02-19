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
package io.simforce.bytezard.metadata.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.functors.NotPredicate;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.simforce.bytezard.metadata.model.SearchFilter;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.type.ClassificationType;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.EntityType;
import io.simforce.bytezard.metadata.type.Type;

public class FilterUtil {
    public static Predicate getPredicateFromSearchFilter(SearchFilter searchFilter) {
        List<Predicate> predicates = new ArrayList<>();

        final String       type           = searchFilter.getParam(SearchFilter.PARAM_TYPE);
        final String       name           = searchFilter.getParam(SearchFilter.PARAM_NAME);
        final String       supertype      = searchFilter.getParam(SearchFilter.PARAM_SUPER_TYPE);
        final String       serviceType    = searchFilter.getParam(SearchFilter.PARAM_SERVICE_TYPE);
        final String       notSupertype   = searchFilter.getParam(SearchFilter.PARAM_NOT_SUPER_TYPE);
        final String       notServiceType = searchFilter.getParam(SearchFilter.PARAM_NOT_SERVICE_TYPE);
        final List<String> notNames       = searchFilter.getParams(SearchFilter.PARAM_NOT_NAME);

        // Add filter for the type/category
        if (StringUtils.isNotBlank(type)) {
            predicates.add(getTypePredicate(type));
        }

        // Add filter for the name
        if (StringUtils.isNotBlank(name)) {
            predicates.add(getNamePredicate(name));
        }

        // Add filter for the serviceType
        if(StringUtils.isNotBlank(serviceType)) {
            predicates.add(getServiceTypePredicate(serviceType));
        }

        // Add filter for the supertype
        if (StringUtils.isNotBlank(supertype)) {
            predicates.add(getSuperTypePredicate(supertype));
        }

        // Add filter for the supertype negation
        if (StringUtils.isNotBlank(notSupertype)) {
            predicates.add(new NotPredicate(getSuperTypePredicate(notSupertype)));
        }

        // Add filter for the serviceType negation
        // NOTE: Creating code for the exclusion of multiple service types is currently useless.
        // In fact the getSearchFilter in TypeREST.java uses the HttpServletRequest.getParameter(key)
        // that if the key takes more values it takes only the first the value. Could be useful
        // to change the getSearchFilter to use getParameterValues instead of getParameter.
        if (StringUtils.isNotBlank(notServiceType)) {
            predicates.add(new NotPredicate(getServiceTypePredicate(notServiceType)));
        }


        // Add filter for the type negation
        if (CollectionUtils.isNotEmpty(notNames)) {
            for (String notName : notNames) {
                predicates.add(new NotPredicate(getNamePredicate(notName)));
            }
        }

        return PredicateUtils.allPredicate(predicates);
    }

    private static Predicate getNamePredicate(final String name) {
        return new Predicate() {
            private boolean isType(Object o) {
                return o instanceof Type;
            }

            @Override
            public boolean evaluate(Object o) {
                return isType(o) && Objects.equals(((Type) o).getTypeName(), name);
            }
        };
    }

    private static Predicate getServiceTypePredicate(final String serviceType) {
        return new Predicate() {
            private boolean isType(Object o) {
                return o instanceof Type;
            }

            @Override
            public boolean evaluate(Object o) {
                return isType(o) && Objects.equals(((Type) o).getServiceType(), serviceType);
            }
        };
    }

    private static Predicate getSuperTypePredicate(final String supertype) {
        return new Predicate() {
            private boolean isClassificationType(Object o) {
                return o instanceof ClassificationType;
            }

            private boolean isEntityType(Object o) {
                return o instanceof EntityType;
            }

            @Override
            public boolean evaluate(Object o) {
                return (isClassificationType(o) && ((ClassificationType) o).getAllSuperTypes().contains(supertype)) ||
                       (isEntityType(o) && ((EntityType) o).getAllSuperTypes().contains(supertype));
            }
        };
    }

    private static Predicate getTypePredicate(final String type) {
        return new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                if (o instanceof Type) {
                    Type Type = (Type) o;

                    switch (type.toUpperCase()) {
                        case "CLASS":
                        case "ENTITY":
                            return Type.getTypeCategory() == TypeCategory.ENTITY;
                        case "TRAIT":
                        case "CLASSIFICATION":
                            return Type.getTypeCategory() == TypeCategory.CLASSIFICATION;
                        case "STRUCT":
                            return Type.getTypeCategory() == TypeCategory.STRUCT;
                        case "ENUM":
                            return Type.getTypeCategory() == TypeCategory.ENUM;
                        case "RELATIONSHIP":
                            return Type.getTypeCategory() == TypeCategory.RELATIONSHIP;
                        case "BUSINESS_METADATA":
                            return Type.getTypeCategory() == TypeCategory.BUSINESS_METADATA;
                        default:
                            // This shouldn't have happened
                            return false;
                    }
                }
                return false;
            }
        };
    }

    public static void addParamsToHideInternalType(SearchFilter searchFilter) {
        searchFilter.setParam(SearchFilter.PARAM_NOT_NAME, Constants.TYPE_NAME_INTERNAL);
        searchFilter.setParam(SearchFilter.PARAM_NOT_SUPER_TYPE, Constants.TYPE_NAME_INTERNAL);
    }
}
