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
package io.simforce.bytezard.metadata.model.glossary;

import io.simforce.bytezard.metadata.model.annotation.JSON;
import io.simforce.bytezard.metadata.model.glossary.relations.GlossaryHeader;
import io.simforce.bytezard.metadata.model.glossary.relations.RelatedTermHeader;
import io.simforce.bytezard.metadata.model.glossary.relations.TermCategorizationHeader;
import io.simforce.bytezard.metadata.model.instance.RelatedObjectId;
import io.simforce.bytezard.metadata.type.Type;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

@JSON
public class GlossaryTerm extends GlossaryBaseObject {

    private List<String> examples;
    private String abbreviation;
    private String usage;

    // Attributes derived from relationships
    private GlossaryHeader anchor;
    private Set<RelatedObjectId> assignedEntities;
    private Set<TermCategorizationHeader> categories;

    // Related Terms
    private Set<RelatedTermHeader> seeAlso;

    // Term Synonyms
    private Set<RelatedTermHeader> synonyms;

    // Term antonyms
    private Set<RelatedTermHeader> antonyms;

    // Term preference
    private Set<RelatedTermHeader> preferredTerms;
    private Set<RelatedTermHeader> preferredToTerms;

    // Term replacements
    private Set<RelatedTermHeader> replacementTerms;
    private Set<RelatedTermHeader> replacedBy;

    // Term translations
    private Set<RelatedTermHeader> translationTerms;
    private Set<RelatedTermHeader> translatedTerms;

    // Term classification
    private Set<RelatedTermHeader> isA;
    private Set<RelatedTermHeader> classifies;

    // Values for terms
    private Set<RelatedTermHeader> validValues;
    private Set<RelatedTermHeader> validValuesFor;

    private boolean hasTerms = false;

    public GlossaryTerm() {
    }

    public GlossaryTerm(final GlossaryTerm other) {
        super(other);
        this.examples = other.examples;
        this.abbreviation = other.abbreviation;
        this.usage = other.usage;
        this.anchor = other.anchor;
        this.assignedEntities = other.assignedEntities;
        this.categories = other.categories;
        this.seeAlso = other.seeAlso;
        this.synonyms = other.synonyms;
        this.antonyms = other.antonyms;
        this.preferredTerms = other.preferredTerms;
        this.preferredToTerms = other.preferredToTerms;
        this.replacementTerms = other.replacementTerms;
        this.replacedBy = other.replacedBy;
        this.translationTerms = other.translationTerms;
        this.translatedTerms = other.translatedTerms;
        this.isA = other.isA;
        this.classifies = other.classifies;
        this.validValues = other.validValues;
        this.validValuesFor = other.validValuesFor;
        this.hasTerms = other.hasTerms;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(final List<String> examples) {
        this.examples = examples;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(final String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(final String usage) {
        this.usage = usage;
    }

    public GlossaryHeader getAnchor() {
        return anchor;
    }

    public void setAnchor(final GlossaryHeader anchor) {
        this.anchor = anchor;
    }

    public Set<TermCategorizationHeader> getCategories() {
        return categories;
    }

    public void setCategories(final Set<TermCategorizationHeader> categories) {
        this.categories = categories;
    }

    public void addCategory(final TermCategorizationHeader category) {
        Set<TermCategorizationHeader> categories = this.categories;
        if (categories == null) {
            categories = new HashSet<>();
        }
        categories.add(category);
        setCategories(categories);
    }

    public Set<RelatedObjectId> getAssignedEntities() {
        return assignedEntities;
    }

    public void setAssignedEntities(final Set<RelatedObjectId> assignedEntities) {
        this.assignedEntities = assignedEntities;
    }

    public void addAssignedEntity(final RelatedObjectId ObjectId) {
        Set<RelatedObjectId> assignedEntities = this.assignedEntities;
        if (assignedEntities == null) {
            assignedEntities = new HashSet<>();
        }
        assignedEntities.add(ObjectId);
        setAssignedEntities(assignedEntities);
    }

    public Set<RelatedTermHeader> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(final Set<RelatedTermHeader> seeAlso) {
        this.seeAlso = seeAlso;

        if (CollectionUtils.isNotEmpty(seeAlso)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(final Set<RelatedTermHeader> synonyms) {
        this.synonyms = synonyms;

        if (CollectionUtils.isNotEmpty(synonyms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getAntonyms() {
        return antonyms;
    }

    public void setAntonyms(final Set<RelatedTermHeader> antonyms) {
        this.antonyms = antonyms;

        if (CollectionUtils.isNotEmpty(antonyms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getPreferredTerms() {
        return preferredTerms;
    }

    public void setPreferredTerms(final Set<RelatedTermHeader> preferredTerms) {
        this.preferredTerms = preferredTerms;

        if (CollectionUtils.isNotEmpty(preferredTerms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getPreferredToTerms() {
        return preferredToTerms;
    }

    public void setPreferredToTerms(final Set<RelatedTermHeader> preferredToTerms) {
        this.preferredToTerms = preferredToTerms;

        if (CollectionUtils.isNotEmpty(preferredToTerms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getReplacementTerms() {
        return replacementTerms;
    }

    public void setReplacementTerms(final Set<RelatedTermHeader> replacementTerms) {
        this.replacementTerms = replacementTerms;

        if (CollectionUtils.isNotEmpty(replacementTerms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(final Set<RelatedTermHeader> replacedBy) {
        this.replacedBy = replacedBy;

        if (CollectionUtils.isNotEmpty(replacedBy)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getTranslationTerms() {
        return translationTerms;
    }

    public void setTranslationTerms(final Set<RelatedTermHeader> translationTerms) {
        this.translationTerms = translationTerms;

        if (CollectionUtils.isNotEmpty(translationTerms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getTranslatedTerms() {
        return translatedTerms;
    }

    public void setTranslatedTerms(final Set<RelatedTermHeader> translatedTerms) {
        this.translatedTerms = translatedTerms;

        if (CollectionUtils.isNotEmpty(translatedTerms)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getIsA() {
        return isA;
    }

    public void setIsA(final Set<RelatedTermHeader> isA) {
        this.isA = isA;

        if (CollectionUtils.isNotEmpty(isA)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getClassifies() {
        return classifies;
    }

    public void setClassifies(final Set<RelatedTermHeader> classifies) {
        this.classifies = classifies;

        if (CollectionUtils.isNotEmpty(classifies)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getValidValues() {
        return validValues;
    }

    public void setValidValues(final Set<RelatedTermHeader> validValues) {
        this.validValues = validValues;

        if (CollectionUtils.isNotEmpty(validValues)) {
            hasTerms = true;
        }
    }

    public Set<RelatedTermHeader> getValidValuesFor() {
        return validValuesFor;
    }

    public void setValidValuesFor(final Set<RelatedTermHeader> validValuesFor) {
        this.validValuesFor = validValuesFor;

        if (CollectionUtils.isNotEmpty(validValuesFor)) {
            hasTerms = true;
        }
    }

    public GlossaryTermHeader getGlossaryTermHeader() {
        return new GlossaryTermHeader(this.getGuid(), this.getQualifiedName());
    }

    @JsonIgnore
    public String toAuditString() {
        GlossaryTerm t = new GlossaryTerm();
        t.setGuid(this.getGuid());
        t.setName(this.getName());
        t.setQualifiedName(this.getQualifiedName());

        return Type.toJson(t);
    }

    @JsonIgnore
    public boolean hasTerms() {
        return hasTerms;
    }

    @JsonIgnore
    @Override
    public void setAttribute(String attrName, String attrVal) {
        Objects.requireNonNull(attrName, "GlossaryTerm attribute name");
        switch (attrName) {
            case "name":
                setName(attrVal);
                break;
            case "shortDescription":
                setShortDescription(attrVal);
                break;
            case "longDescription":
                setLongDescription(attrVal);
                break;
            case "abbreviation":
                setAbbreviation(attrVal);
                break;
            case "usage":
                setUsage(attrVal);
                break;
            default:
                throw new IllegalArgumentException("Invalid attribute '" + attrName + "' for object GlossaryTerm");
        }
    }

    @JsonIgnore
    public Map<Relation, Set<RelatedTermHeader>> getRelatedTerms() {
        Map<Relation, Set<RelatedTermHeader>> ret = new HashMap<>();

        if (CollectionUtils.isNotEmpty(seeAlso)) {
            ret.put(Relation.SEE_ALSO, seeAlso);
        }

        if (CollectionUtils.isNotEmpty(synonyms)) {
            ret.put(Relation.SYNONYMS, synonyms);
        }

        if (CollectionUtils.isNotEmpty(antonyms)) {
            ret.put(Relation.ANTONYMS, antonyms);
        }

        if (CollectionUtils.isNotEmpty(preferredTerms)) {
            ret.put(Relation.PREFERRED_TERMS, preferredTerms);
        }

        if (CollectionUtils.isNotEmpty(preferredToTerms)) {
            ret.put(Relation.PREFERRED_TO_TERMS, preferredToTerms);
        }

        if (CollectionUtils.isNotEmpty(replacementTerms)) {
            ret.put(Relation.REPLACEMENT_TERMS, replacementTerms);
        }

        if (CollectionUtils.isNotEmpty(replacedBy)) {
            ret.put(Relation.REPLACED_BY, replacedBy);
        }

        if (CollectionUtils.isNotEmpty(translationTerms)) {
            ret.put(Relation.TRANSLATION_TERMS, translationTerms);
        }

        if (CollectionUtils.isNotEmpty(translatedTerms)) {
            ret.put(Relation.TRANSLATED_TERMS, translatedTerms);
        }

        if (CollectionUtils.isNotEmpty(isA)) {
            ret.put(Relation.ISA, isA);
        }

        if (CollectionUtils.isNotEmpty(classifies)) {
            ret.put(Relation.CLASSIFIES, classifies);
        }

        if (CollectionUtils.isNotEmpty(validValues)) {
            ret.put(Relation.VALID_VALUES, validValues);
        }

        if (CollectionUtils.isNotEmpty(validValuesFor)) {
            ret.put(Relation.VALID_VALUES_FOR, validValuesFor);
        }

        return ret;
    }

    @Override
    protected StringBuilder toString(final StringBuilder sb) {
        sb.append("examples=").append(examples);
        sb.append(", abbreviation='").append(abbreviation).append('\'');
        sb.append(", usage='").append(usage).append('\'');
        sb.append(", anchor=").append(anchor);
        sb.append(", assignedEntities=").append(assignedEntities);
        sb.append(", categories=").append(categories);
        sb.append(", seeAlso=").append(seeAlso);
        sb.append(", synonyms=").append(synonyms);
        sb.append(", antonyms=").append(antonyms);
        sb.append(", preferredTerms=").append(preferredTerms);
        sb.append(", preferredToTerms=").append(preferredToTerms);
        sb.append(", replacementTerms=").append(replacementTerms);
        sb.append(", replacedBy=").append(replacedBy);
        sb.append(", translationTerms=").append(translationTerms);
        sb.append(", translatedTerms=").append(translatedTerms);
        sb.append(", isA=").append(isA);
        sb.append(", classifies=").append(classifies);
        sb.append(", validValues=").append(validValues);
        sb.append(", validValuesFor=").append(validValuesFor);

        return sb;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GlossaryTerm)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final GlossaryTerm that = (GlossaryTerm) o;
        return Objects.equals(examples, that.examples) &&
                       Objects.equals(abbreviation, that.abbreviation) &&
                       Objects.equals(usage, that.usage) &&
                       Objects.equals(anchor, that.anchor) &&
                       Objects.equals(assignedEntities, that.assignedEntities) &&
                       Objects.equals(categories, that.categories) &&
                       Objects.equals(seeAlso, that.seeAlso) &&
                       Objects.equals(synonyms, that.synonyms) &&
                       Objects.equals(antonyms, that.antonyms) &&
                       Objects.equals(preferredTerms, that.preferredTerms) &&
                       Objects.equals(preferredToTerms, that.preferredToTerms) &&
                       Objects.equals(replacementTerms, that.replacementTerms) &&
                       Objects.equals(replacedBy, that.replacedBy) &&
                       Objects.equals(translationTerms, that.translationTerms) &&
                       Objects.equals(translatedTerms, that.translatedTerms) &&
                       Objects.equals(isA, that.isA) &&
                       Objects.equals(classifies, that.classifies) &&
                       Objects.equals(validValues, that.validValues) &&
                       Objects.equals(validValuesFor, that.validValuesFor);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), examples, abbreviation, usage, anchor, assignedEntities, categories,
                            seeAlso, synonyms, antonyms, preferredTerms, preferredToTerms, replacementTerms, replacedBy,
                            translationTerms, translatedTerms, isA, classifies, validValues, validValuesFor);
    }

    public enum Relation {
        SEE_ALSO("GlossaryRelatedTerm", "seeAlso"),
        SYNONYMS("GlossarySynonym", "synonyms"),
        ANTONYMS("GlossaryAntonym", "antonyms"),
        PREFERRED_TO_TERMS("GlossaryPreferredTerm", "preferredToTerms", true),
        PREFERRED_TERMS("GlossaryPreferredTerm", "preferredTerms"),
        REPLACEMENT_TERMS("GlossaryReplacementTerm", "replacementTerms", true),
        REPLACED_BY("GlossaryReplacementTerm", "replacedBy"),
        TRANSLATION_TERMS("GlossaryTranslation", "translationTerms", true),
        TRANSLATED_TERMS("GlossaryTranslation", "translatedTerms"),
        ISA("GlossaryIsARelationship", "isA", true),
        CLASSIFIES("GlossaryIsARelationship", "classifies"),
        VALID_VALUES("GlossaryValidValue", "validValues", true),
        VALID_VALUES_FOR("GlossaryValidValue", "validValuesFor"),
        ;

        private String  name;
        private String  attrName;
        private boolean isEnd2Attr;

        Relation(final String name, final String attrName) {
            this(name, attrName, false);
        }

        Relation(final String name, final String attrName, final boolean isEnd2Attr) {
            this.name = name;
            this.attrName = attrName;
            this.isEnd2Attr = isEnd2Attr;
        }

        public String getName() {
            return name;
        }

        @JsonValue
        public String getAttrName() {
            return attrName;
        }

        public boolean isEnd2Attr() {
            return isEnd2Attr;
        }
    }
}
