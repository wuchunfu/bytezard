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

import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_DATE;
import static io.simforce.bytezard.metadata.model.typedef.BaseTypeDef.TYPE_STRING;
import static io.simforce.bytezard.metadata.type.Constants.CLASSIFICATION_ENTITY_STATUS_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.CREATED_BY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.MODIFIED_BY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.TIMESTAMP_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.TYPE_NAME_PROPERTY_KEY;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TimeBoundary;
import io.simforce.bytezard.metadata.model.instance.Classification;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DateValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * class that implements behaviour of a classification-type.
 */
public class ClassificationType extends StructType {

    public  static final ClassificationType CLASSIFICATION_ROOT      = initRootClassificationType();
    private static final String                  CLASSIFICATION_ROOT_NAME = "__CLASSIFICATION_ROOT";

    private final ClassificationDef classificationDef;
    private final String                 typeQryStr;

    private List<ClassificationType> superTypes               = Collections.emptyList();
    private Set<String>                   allSuperTypes            = Collections.emptySet();
    private Set<String>                   subTypes                 = Collections.emptySet();
    private Set<String>                   allSubTypes              = Collections.emptySet();
    private Set<String>                   typeAndAllSubTypes       = Collections.emptySet();
    private Set<String>                   typeAndAllSuperTypes     = Collections.emptySet();
    private String                        typeAndAllSubTypesQryStr = "";

    // we need to store the entityTypes specified in our supertypes. i.e. our parent classificationDefs may specify more entityTypes
    // that we also need to allow
    private Set<String> entityTypes = Collections.emptySet();

    /**
     * Note this constructor does NOT run resolveReferences - so some fields are not setup.
     * @param classificationDef
     */
    public ClassificationType(ClassificationDef classificationDef) {
        super(classificationDef);

        this.classificationDef = classificationDef;
        this.typeQryStr        = Attribute.escapeIndexQueryValue(Collections.singleton(getTypeName()), true);
    }

    /**
     * ClassificationType needs to be constructed with a type registry so that is can resolve references
     * at constructor time. This is only used by junits.
     *
     * @param classificationDef
     * @param typeRegistry
     * @throws BaseException
     */
    public ClassificationType(ClassificationDef classificationDef, TypeRegistry typeRegistry)
        throws BaseException {
        super(classificationDef);

        this.classificationDef = classificationDef;
        this.typeQryStr        = Attribute.escapeIndexQueryValue(Collections.singleton(getTypeName()), true);

        resolveReferences(typeRegistry);
    }

    public ClassificationDef getClassificationDef() { return classificationDef; }

    public static ClassificationType getClassificationRoot() {return CLASSIFICATION_ROOT; }

    @Override
    void resolveReferences(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferences(typeRegistry);

        List<ClassificationType> s    = new ArrayList<>();
        Set<String>                   allS = new HashSet<>();
        Map<String, Attribute>   allA = new HashMap<>();

        getTypeHierarchyInfo(typeRegistry, allS, allA);

        for (String superTypeName : classificationDef.getSuperTypes()) {
            Type superType = typeRegistry.getType(superTypeName);

            if (superType instanceof ClassificationType) {
                s.add((ClassificationType)superType);
            } else {
                throw new BaseException(ErrorCode.INCOMPATIBLE_SUPERTYPE, superTypeName,
                        classificationDef.getName());
            }
        }

        this.superTypes         = Collections.unmodifiableList(s);
        this.allSuperTypes      = Collections.unmodifiableSet(allS);
        this.allAttributes      = Collections.unmodifiableMap(allA);
        this.uniqAttributes     = getUniqueAttributes(this.allAttributes);
        this.subTypes           = new HashSet<>(); // this will be populated in resolveReferencesPhase2()
        this.allSubTypes        = new HashSet<>(); // this will be populated in resolveReferencesPhase2()
        this.typeAndAllSubTypes = new HashSet<>(); // this will be populated in resolveReferencesPhase2()
        this.entityTypes        = new HashSet<>(); // this will be populated in resolveReferencesPhase3()

        this.typeAndAllSubTypes.add(this.getTypeName());

        this.typeAndAllSuperTypes = new HashSet<>(this.allSuperTypes);
        this.typeAndAllSuperTypes.add(this.getTypeName());
        this.typeAndAllSuperTypes = Collections.unmodifiableSet(this.typeAndAllSuperTypes);
    }

    @Override
    void resolveReferencesPhase2(TypeRegistry typeRegistry) throws BaseException {
        super.resolveReferencesPhase2(typeRegistry);
        ensureNoAttributeOverride(superTypes);

        for (ClassificationType superType : superTypes) {
            superType.addSubType(this);
        }

        for (String superTypeName : allSuperTypes) {
            ClassificationType superType = typeRegistry.getClassificationTypeByName(superTypeName);
            superType.addToAllSubTypes(this);
        }
    }

    /**
     * This method processes the entityTypes to ensure they are valid, using the following principles:
     * - entityTypes are supplied on the classificationDef to restrict the types of entities that this classification can be applied to
     * - Any subtypes of the specified entity type can also have this classification applied
     * - Any subtypes of the classificationDef inherit the parents entityTypes restrictions
     * - Any subtypes of the classificationDef can further restrict the parents entityTypes restrictions
     * - An empty entityTypes list when there are no parent restrictions means there are no restrictions
     * - An empty entityTypes list when there are parent restrictions means that the subtype picks up the parents restrictions
     *
     * This method validates that these priniciples are adhered to.
     *
     * Note that if duplicate Strings in the entityTypes are specified on an add / update, the duplicates are ignored - as Java Sets cannot have duplicates.
     * Note if an entityType is supplied in the list that is a subtype of one of the other supplied entityTypes, we are not policing this case as invalid.
     *
     * @param typeRegistry
     * @throws BaseException
     */
    @Override
    void resolveReferencesPhase3(TypeRegistry typeRegistry) throws BaseException {
        subTypes                 = Collections.unmodifiableSet(subTypes);
        allSubTypes              = Collections.unmodifiableSet(allSubTypes);
        typeAndAllSubTypes       = Collections.unmodifiableSet(typeAndAllSubTypes);
        typeAndAllSubTypesQryStr = ""; // will be computed on next access

        /*
          Add any entityTypes defined in our parents as restrictions.
         */
        Set<String> superTypeEntityTypes = null;

        final Set<String> classificationDefEntityTypes = classificationDef.getEntityTypes();

        // Here we find the intersection of the entityTypes specified in all our supertypes; in this way we will honour our parents restrictions.
        // This following logic assumes typeAndAllSubTypes is populated so needs to be run after resolveReferencesPhase2().

        for (String superType : this.allSuperTypes) {
            ClassificationDef superTypeDef    = typeRegistry.getClassificationDefByName(superType);
            Set<String>            entityTypeNames = superTypeDef.getEntityTypes();

            if (CollectionUtils.isEmpty(entityTypeNames)) { // no restrictions specified
                continue;
            }

            // classification is applicable for specified entityTypes and their sub-entityTypes
            Set<String> typesAndSubEntityTypes = EntityType.getEntityTypesAndAllSubTypes(entityTypeNames, typeRegistry);

            if (superTypeEntityTypes == null) {
                superTypeEntityTypes = new HashSet<>(typesAndSubEntityTypes);
            } else {
                // retain only the intersections.
                superTypeEntityTypes.retainAll(typesAndSubEntityTypes);
            }
            if (superTypeEntityTypes.isEmpty()) {
                // if we have no intersections then we are disjoint - so no need to check other supertypes
                break;
            }
        }

        if (superTypeEntityTypes == null) {  // no supertype restrictions; use current classification restrictions
            this.entityTypes = EntityType.getEntityTypesAndAllSubTypes(classificationDefEntityTypes, typeRegistry);
        } else {                             // restrictions are specified in super-types
            if (CollectionUtils.isEmpty(superTypeEntityTypes)) {
                /*
                 Restrictions in superTypes are disjoint! This means that the child cannot exist as it cannot be a restriction of it's parents.

                 For example:
                  parent1 specifies entityTypes ["EntityA"]
                  parent2 specifies entityTypes ["EntityB"]

                  In order to be a valid child of Parent1 the child could only be applied to EntityAs.
                  In order to be a valid child of Parent2 the child could only be applied to EntityBs.

                  Reject the creation of the classificationDef - as it would compromise 's integrity.
                 */
                throw new BaseException(ErrorCode.CLASSIFICATIONDEF_PARENTS_ENTITYTYPES_DISJOINT, this.classificationDef.getName());
            }

            if (CollectionUtils.isEmpty(classificationDefEntityTypes)) { // no restriction specified; use the restrictions from super-types
                this.entityTypes = superTypeEntityTypes;
            } else {
                this.entityTypes = EntityType.getEntityTypesAndAllSubTypes(classificationDefEntityTypes,typeRegistry);
                // Compatible parents and entityTypes, now check whether the specified entityTypes are the same as the effective entityTypes due to our parents or a subset.
                // Only allowed to restrict our parents.
                if (!superTypeEntityTypes.containsAll(this.entityTypes)) {
                    throw new BaseException(ErrorCode.CLASSIFICATIONDEF_ENTITYTYPES_NOT_PARENTS_SUBSET, classificationDef.getName(), classificationDefEntityTypes.toString());
                }
            }
        }

        classificationDef.setSubTypes(subTypes);
    }

    @Override
    public Attribute getSystemAttribute(String attributeName) {
        return ClassificationType.CLASSIFICATION_ROOT.allAttributes.get(attributeName);
    }

    private void addSubType(ClassificationType subType) {
        subTypes.add(subType.getTypeName());
    }

    private void addToAllSubTypes(ClassificationType subType) {
        allSubTypes.add(subType.getTypeName());
        typeAndAllSubTypes.add(subType.getTypeName());
    }

    public Set<String> getSuperTypes() {
        return classificationDef.getSuperTypes();
    }

    public Set<String> getAllSuperTypes() { return allSuperTypes; }

    public Set<String> getSubTypes() { return subTypes; }

    public Set<String> getAllSubTypes() { return allSubTypes; }

    public Set<String> getTypeAndAllSubTypes() { return typeAndAllSubTypes; }

    public Set<String> getTypeAndAllSuperTypes() { return typeAndAllSuperTypes; }

    public String getTypeQryStr() { return typeQryStr; }

    public String getTypeAndAllSubTypesQryStr() {
        if (StringUtils.isEmpty(typeAndAllSubTypesQryStr)) {
            typeAndAllSubTypesQryStr = Attribute.escapeIndexQueryValue(typeAndAllSubTypes, true);
        }

        return typeAndAllSubTypesQryStr;
    }

    public boolean isSuperTypeOf(ClassificationType classificationType) {
        return classificationType != null && allSubTypes.contains(classificationType.getTypeName());
    }

    public boolean isSuperTypeOf(String classificationName) {
        return StringUtils.isNotEmpty(classificationName) && allSubTypes.contains(classificationName);
    }

    public boolean isSubTypeOf(ClassificationType classificationType) {
        return classificationType != null && allSuperTypes.contains(classificationType.getTypeName());
    }

    public boolean isSubTypeOf(String classificationName) {
        return StringUtils.isNotEmpty(classificationName) && allSuperTypes.contains(classificationName);
    }

    public boolean hasAttribute(String attrName) {
        return allAttributes.containsKey(attrName);
    }

    /**
     * List of all the entity type names that are valid for this classification type.
     *
     * An empty list means there are no restrictions on which entities can be classified by these classifications.
     * @return
     */
    public Set<String> getEntityTypes() {
        return entityTypes;
    }

    @Override
    public Classification createDefaultValue() {
        Classification ret = new Classification(classificationDef.getName());

        populateDefaultValues(ret);

        return ret;
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            for (ClassificationType superType : superTypes) {
                if (!superType.isValidValue(obj)) {
                    return false;
                }
            }

            if (!validateTimeBoundaries(obj, null)) {
                return false;
            }

            return super.isValidValue(obj);
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        for (ClassificationType superType : superTypes) {
            if (!superType.areEqualValues(val1, val2, guidAssignments)) {
                return false;
            }
        }

        return super.areEqualValues(val1, val2, guidAssignments);
    }

    @Override
    public boolean isValidValueForUpdate(Object obj) {
        if (obj != null) {
            for (ClassificationType superType : superTypes) {
                if (!superType.isValidValueForUpdate(obj)) {
                    return false;
                }
            }

            if (!validateTimeBoundaries(obj, null)) {
                return false;
            }

            return super.isValidValueForUpdate(obj);
        }

        return true;
    }

    @Override
    public Object getNormalizedValue(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValue(obj)) {
                if (obj instanceof Classification) {
                    normalizeAttributeValues((Classification) obj);
                    ret = obj;
                } else if (obj instanceof Map) {
                    normalizeAttributeValues((Map) obj);
                    ret = obj;
                }
            }
        }

        return ret;
    }

    @Override
    public Object getNormalizedValueForUpdate(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValueForUpdate(obj)) {
                if (obj instanceof Classification) {
                    normalizeAttributeValuesForUpdate((Classification) obj);
                    ret = obj;
                } else if (obj instanceof Map) {
                    normalizeAttributeValuesForUpdate((Map) obj);
                    ret = obj;
                }
            }
        }

        return ret;
    }

    @Override
    public boolean validateValue(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            for (ClassificationType superType : superTypes) {
                ret = superType.validateValue(obj, objName, messages) && ret;
            }

            ret = validateTimeBoundaries(obj, messages) && ret;

            ret = super.validateValue(obj, objName, messages) && ret;
        }

        return ret;
    }

    @Override
    public boolean validateValueForUpdate(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            for (ClassificationType superType : superTypes) {
                ret = superType.validateValueForUpdate(obj, objName, messages) && ret;
            }

            ret = validateTimeBoundaries(obj, messages) && ret;

            ret = super.validateValueForUpdate(obj, objName, messages) && ret;
        }

        return ret;
    }

    public void normalizeAttributeValues(Classification classification) {
        if (classification != null) {
            for (ClassificationType superType : superTypes) {
                superType.normalizeAttributeValues(classification);
            }

            super.normalizeAttributeValues(classification);
        }
    }

    public void normalizeAttributeValuesForUpdate(Classification classification) {
        if (classification != null) {
            for (ClassificationType superType : superTypes) {
                superType.normalizeAttributeValuesForUpdate(classification);
            }

            super.normalizeAttributeValuesForUpdate(classification);
        }
    }

    @Override
    public void normalizeAttributeValues(Map<String, Object> obj) {
        if (obj != null) {
            for (ClassificationType superType : superTypes) {
                superType.normalizeAttributeValues(obj);
            }

            super.normalizeAttributeValues(obj);
        }
    }

    @Override
    public void normalizeAttributeValuesForUpdate(Map<String, Object> obj) {
        if (obj != null) {
            for (ClassificationType superType : superTypes) {
                superType.normalizeAttributeValuesForUpdate(obj);
            }

            super.normalizeAttributeValuesForUpdate(obj);
        }
    }

    public void populateDefaultValues(Classification classification) {
        if (classification != null) {
            for (ClassificationType superType : superTypes) {
                superType.populateDefaultValues(classification);
            }

            super.populateDefaultValues(classification);
        }
    }

    /**
     * Check whether the supplied entityType can be applied to this classification.
     *
     * We can apply this classification to the supplied entityType if
     * - we have no restrictions (entityTypes empty including null)
     * or
     * - the entityType is in our list of restricted entityTypes (which includes our parent classification restrictions)
     *
     * @param entityType
     * @return whether can apply
     */
    /**
     * Check whether the supplied entityType can be applied to this classification.
     *
     * @param entityType
     * @return whether can apply
     */
    public boolean canApplyToEntityType(EntityType entityType) {
        return CollectionUtils.isEmpty(this.entityTypes) || this.entityTypes.contains(entityType.getTypeName());
    }

    private static ClassificationType initRootClassificationType() {
        List<AttributeDef> attributeDefs = new ArrayList<AttributeDef>() {{
            add(new AttributeDef(TYPE_NAME_PROPERTY_KEY, BaseTypeDef.TYPE_STRING, false, true));
            add(new AttributeDef(TIMESTAMP_PROPERTY_KEY, TYPE_DATE, false, true));
            add(new AttributeDef(MODIFICATION_TIMESTAMP_PROPERTY_KEY, TYPE_DATE, false, true));
            add(new AttributeDef(MODIFIED_BY_KEY, TYPE_STRING, false, true));
            add(new AttributeDef(CREATED_BY_KEY, TYPE_STRING, false, true));
            add(new AttributeDef(CLASSIFICATION_ENTITY_STATUS_PROPERTY_KEY, TYPE_STRING, false, true));

        }};

        ClassificationDef classificationDef = new ClassificationDef(CLASSIFICATION_ROOT_NAME, "Root classification for system attributes", "1.0", attributeDefs);

        return new ClassificationType(classificationDef);
    }

    private void getTypeHierarchyInfo(TypeRegistry              typeRegistry,
                                      Set<String>                    allSuperTypeNames,
                                      Map<String, Attribute>    allAttributes) throws BaseException {
        List<String> visitedTypes = new ArrayList<>();

        collectTypeHierarchyInfo(typeRegistry, allSuperTypeNames, allAttributes, visitedTypes);
    }

    /*
     * This method should not assume that resolveReferences() has been called on all superTypes.
     * this.classificationDef is the only safe member to reference here
     */
    private void collectTypeHierarchyInfo(TypeRegistry              typeRegistry,
                                          Set<String>                    allSuperTypeNames,
                                          Map<String, Attribute>    allAttributes,
                                          List<String>                   visitedTypes) throws BaseException {
        if (visitedTypes.contains(classificationDef.getName())) {
            throw new BaseException(ErrorCode.CIRCULAR_REFERENCE, classificationDef.getName(),
                                         visitedTypes.toString());
        }

        if (CollectionUtils.isNotEmpty(classificationDef.getSuperTypes())) {
            visitedTypes.add(classificationDef.getName());
            for (String superTypeName : classificationDef.getSuperTypes()) {
                ClassificationType superType = typeRegistry.getClassificationTypeByName(superTypeName);

                if (superType != null) {
                    superType.collectTypeHierarchyInfo(typeRegistry, allSuperTypeNames, allAttributes, visitedTypes);
                }
            }
            visitedTypes.remove(classificationDef.getName());

            allSuperTypeNames.addAll(classificationDef.getSuperTypes());
        }

        if (CollectionUtils.isNotEmpty(classificationDef.getAttributeDefs())) {
            for (AttributeDef attributeDef : classificationDef.getAttributeDefs()) {
                Type type = typeRegistry.getType(attributeDef.getTypeName());
                allAttributes.put(attributeDef.getName(), new Attribute(this, attributeDef, type));
            }
        }
    }

    private boolean validateTimeBoundaries(Object classificationObj, List<String> messages) {
        boolean             ret            = true;
        Classification classification = null;

        if (classificationObj instanceof Classification) {
            classification = (Classification) classificationObj;
        } else if (classificationObj instanceof Map) {
            classification = new Classification((Map) classificationObj);
        }

        if (classification != null && classification.getValidityPeriods() != null) {
            for (TimeBoundary timeBoundary : classification.getValidityPeriods()) {
                if (timeBoundary != null) {
                    ret = validateTimeBoundry(timeBoundary, messages) && ret;
                }
            }
        }

        return ret;
    }

    private boolean validateTimeBoundry(TimeBoundary timeBoundary, List<String> messages) {
        boolean        ret           = true;
        DateValidator  dateValidator = DateValidator.getInstance();
        Date           startDate     = null;
        Date           endDate       = null;
        final TimeZone timezone;

        if (StringUtils.isNotEmpty(timeBoundary.getTimeZone())) {
            if (!isValidTimeZone(timeBoundary.getTimeZone())) {
                addValidationMessageIfNotPresent(new BaseException(ErrorCode.INVALID_TIMEBOUNDRY_TIMEZONE, timeBoundary.getTimeZone()), messages);

                ret = false;
            }

            timezone = TimeZone.getTimeZone(timeBoundary.getTimeZone());
        } else {
            timezone = TimeZone.getDefault();
        }

        if (StringUtils.isNotEmpty(timeBoundary.getStartTime())) {
            startDate = dateValidator.validate(timeBoundary.getStartTime(), TimeBoundary.TIME_FORMAT, timezone);

            if (startDate == null) {
                addValidationMessageIfNotPresent(new BaseException(ErrorCode.INVALID_TIMEBOUNDRY_START_TIME, timeBoundary.getStartTime()), messages);

                ret = false;
            }
        }

        if (StringUtils.isNotEmpty(timeBoundary.getEndTime())) {
            endDate = dateValidator.validate(timeBoundary.getEndTime(), TimeBoundary.TIME_FORMAT, timezone);

            if (endDate == null) {
                addValidationMessageIfNotPresent(new BaseException(ErrorCode.INVALID_TIMEBOUNDRY_END_TIME, timeBoundary.getEndTime()), messages);

                ret = false;
            }
        }

        if (startDate != null && endDate != null) {
            if (endDate.before(startDate)) {
                addValidationMessageIfNotPresent(new BaseException(ErrorCode.INVALID_TIMEBOUNDRY_DATERANGE, timeBoundary.getStartTime(), timeBoundary.getEndTime()), messages);

                ret = false;
            }
        }

        return ret;
    }

    public static boolean isValidTimeZone(final String timeZone) {
        final String DEFAULT_GMT_TIMEZONE = "GMT";
        if (timeZone.equals(DEFAULT_GMT_TIMEZONE)) {
            return true;
        } else {
            // if custom time zone is invalid,
            // time zone id returned is always "GMT" by default
            String id = TimeZone.getTimeZone(timeZone).getID();
            if (!id.equals(DEFAULT_GMT_TIMEZONE)) {
                return true;
            }
        }

        return false;
    }

    private void addValidationMessageIfNotPresent(BaseException excp, List<String> messages) {
        String msg = excp.getMessage();

        if (messages != null && !messages.contains(msg)) {
            messages.add(msg);
        }
    }
}
