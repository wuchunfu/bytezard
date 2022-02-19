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
import io.simforce.bytezard.metadata.model.glossary.relations.RelatedCategoryHeader;
import io.simforce.bytezard.metadata.model.glossary.relations.RelatedTermHeader;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JSON
public class GlossaryCategory extends GlossaryBaseObject {
    private GlossaryHeader anchor;

    private RelatedCategoryHeader parentCategory;
    private Set<RelatedCategoryHeader> childrenCategories;

    // Terms associated with this category
    private Set<RelatedTermHeader> terms;

    public GlossaryCategory() {
    }

    public GlossaryCategory(final GlossaryCategory other) {
        super(other);
        this.anchor = other.anchor;
        this.parentCategory = other.parentCategory;
        this.childrenCategories = other.childrenCategories;
        this.terms = other.terms;
    }

    public GlossaryHeader getAnchor() {
        return anchor;
    }

    public void setAnchor(final GlossaryHeader anchor) {
        this.anchor = anchor;
    }

    public RelatedCategoryHeader getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(final RelatedCategoryHeader parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Set<RelatedCategoryHeader> getChildrenCategories() {
        return childrenCategories;
    }

    public void setChildrenCategories(final Set<RelatedCategoryHeader> childrenCategories) {
        this.childrenCategories = childrenCategories;
    }

    public Set<RelatedTermHeader> getTerms() {
        return terms;
    }

    public void setTerms(final Set<RelatedTermHeader> terms) {
        this.terms = terms;
    }

    @JsonIgnore
    public void addChild(RelatedCategoryHeader child) {
        Set<RelatedCategoryHeader> children = this.childrenCategories ;
        if (children == null) {
            children = new HashSet<>();
        }
        children.add(child);
        setChildrenCategories(children);
    }

    @JsonIgnore
    public void removeChild(RelatedCategoryHeader child) {
        if (CollectionUtils.isNotEmpty(childrenCategories)) {
            childrenCategories.remove(child);
        }
    }

    @JsonIgnore
    public void addTerm(RelatedTermHeader term) {
        Set<RelatedTermHeader> terms = this.terms;
        if (terms == null) {
            terms = new HashSet<>();
        }
        terms.add(term);
        setTerms(terms);
    }

    @JsonIgnore
    public void removeTerm(RelatedTermHeader term) {
        if (CollectionUtils.isNotEmpty(terms)) {
            terms.remove(term);
        }
    }

    @JsonIgnore
    @Override
    public void setAttribute(String attrName, String attrVal) {
        Objects.requireNonNull(attrName, "Glossary attribute name");
        switch(attrName) {
            case "name":
                setName(attrVal);
                break;
            case "shortDescription":
                setShortDescription(attrVal);
                break;
            case "longDescription":
                setLongDescription(attrVal);
                break;
            default:
                throw new IllegalArgumentException("Invalid attribute '" + attrName + "' for object GlossaryCategory");
        }
    }

    @Override
    protected StringBuilder toString(final StringBuilder sb) {
        sb.append(", anchor=").append(anchor);
        sb.append(", parentCategory=").append(parentCategory);
        sb.append(", childrenCategories=").append(childrenCategories);
        sb.append(", terms=").append(terms);

        return sb;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof GlossaryCategory)) return false;
        if (!super.equals(o)) return false;
        final GlossaryCategory category = (GlossaryCategory) o;
        return Objects.equals(anchor, category.anchor) &&
                       Objects.equals(parentCategory, category.parentCategory) &&
                       Objects.equals(childrenCategories, category.childrenCategories) &&
                       Objects.equals(terms, category.terms);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), anchor, parentCategory, childrenCategories, terms);
    }
}
