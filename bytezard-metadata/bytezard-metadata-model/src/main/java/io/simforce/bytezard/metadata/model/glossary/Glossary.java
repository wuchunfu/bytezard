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
import io.simforce.bytezard.metadata.model.glossary.relations.RelatedCategoryHeader;
import io.simforce.bytezard.metadata.model.glossary.relations.RelatedTermHeader;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JSON
public class Glossary extends GlossaryBaseObject {
    private String language;
    private String usage;

    private Set<RelatedTermHeader>     terms;
    private Set<RelatedCategoryHeader> categories;

    public Glossary() {
    }

    public Glossary(final Glossary other) {
        super(other);
        super.setQualifiedName(other.getQualifiedName());
        super.setGuid(other.getGuid());
        super.setName(other.name);
        super.setShortDescription(other.shortDescription);
        super.setLongDescription(other.longDescription);
        this.language = other.language;
        this.usage = other.usage;
        this.terms = other.terms;
        this.categories = other.categories;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(final String usage) {
        this.usage = usage;
    }

    public Set<RelatedCategoryHeader> getCategories() {
        return categories;
    }

    public void setCategories(final Set<RelatedCategoryHeader> categories) {
        this.categories = categories;
    }

    public Set<RelatedTermHeader> getTerms() {
        return terms;
    }

    public void setTerms(final Set<RelatedTermHeader> terms) {
        this.terms = terms;
    }

    @JsonIgnore
    @Override
    public void setAttribute(String attrName, String attrVal) {
        Objects.requireNonNull(attrName, "Glossary attribute name");
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
            case "language":
                setLanguage(attrVal);
                break;
            case "usage":
                setUsage(attrVal);
                break;
            default:
                throw new IllegalArgumentException("Invalid attribute '" + attrName + "' for object Glossary");
        }
    }

    @JsonIgnore
    public void addTerm(RelatedTermHeader relatedTermId) {
        Set<RelatedTermHeader> terms = this.terms;
        if (terms == null) {
            terms = new HashSet<>();
        }
        terms.add(relatedTermId);
        setTerms(terms);
    }

    @JsonIgnore
    public void addCategory(RelatedCategoryHeader relatedCategoryId) {
        Set<RelatedCategoryHeader> categories = this.categories;
        if (categories == null) {
            categories = new HashSet<>();
        }
        categories.add(relatedCategoryId);
        setCategories(categories);
    }

    @JsonIgnore
    public void removeTerm(RelatedTermHeader relatedTermId) {
        if (CollectionUtils.isNotEmpty(terms)) {
            terms.remove(relatedTermId);
        }
    }

    @JsonIgnore
    public void removeCategory(RelatedCategoryHeader relatedCategoryId) {
        if (CollectionUtils.isNotEmpty(categories)) {
            categories.remove(relatedCategoryId);
        }
    }

    @Override
    protected StringBuilder toString(final StringBuilder sb) {
        sb.append(", language='").append(language).append('\'');
        sb.append(", usage='").append(usage).append('\'');
        sb.append(", terms=").append(terms);
        sb.append(", categories=").append(categories);

        return sb;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Glossary)) return false;
        if (!super.equals(o)) return false;
        final Glossary glossary = (Glossary) o;
        return Objects.equals(language, glossary.language) &&
                       Objects.equals(usage, glossary.usage) &&
                       Objects.equals(terms, glossary.terms) &&
                       Objects.equals(categories, glossary.categories);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), language, usage, terms, categories);
    }

    @JSON
    public static class GlossaryExtInfo extends Glossary {
        private Map<String, GlossaryTerm>     termInfo;
        private Map<String, GlossaryCategory> categoryInfo;

        public GlossaryExtInfo() {
        }

        public GlossaryExtInfo(Glossary glossary) {
            super(glossary);
        }

        public Map<String, GlossaryTerm> getTermInfo() {
            return termInfo;
        }

        public void addTermInfo(final GlossaryTerm term) {
            if (termInfo == null) {
                termInfo = new HashMap<>();
            }
            termInfo.put(term.getGuid(), term);
        }

        public void setTermInfo(final Map<String, GlossaryTerm> termInfo) {
            this.termInfo = termInfo;
        }

        public Map<String, GlossaryCategory> getCategoryInfo() {
            return categoryInfo;
        }

        public void addCategoryInfo(final GlossaryCategory category) {
            if (categoryInfo == null) {
                categoryInfo = new HashMap<>();
            }
            categoryInfo.put(category.getGuid(), category);
        }

        public void setCategoryInfo(final Map<String, GlossaryCategory> categoryInfo) {
            this.categoryInfo = categoryInfo;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof GlossaryExtInfo)) return false;
            if (!super.equals(o)) return false;
            final GlossaryExtInfo that = (GlossaryExtInfo) o;
            return Objects.equals(termInfo, that.termInfo) &&
                           Objects.equals(categoryInfo, that.categoryInfo);
        }

        @Override
        public int hashCode() {

            return Objects.hash(super.hashCode(), termInfo, categoryInfo);
        }

        @Override
        public StringBuilder toString(StringBuilder sb) {
            sb.append(", termInfo=").append(termInfo);
            sb.append(", categoryInfo=").append(categoryInfo);

            return sb;
        }


    }
}
