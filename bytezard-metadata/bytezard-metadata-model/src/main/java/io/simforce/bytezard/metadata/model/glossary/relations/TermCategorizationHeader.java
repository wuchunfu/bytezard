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
package io.simforce.bytezard.metadata.model.glossary.relations;

import io.simforce.bytezard.metadata.model.annotation.JSON;
import io.simforce.bytezard.metadata.model.glossary.enums.TermRelationshipStatus;

import java.util.Objects;

@JSON
public class TermCategorizationHeader {
    private String categoryGuid;
    private String relationGuid;
    private String description;
    private String displayText;

    private TermRelationshipStatus status;

    public TermCategorizationHeader() {
    }

    public String getCategoryGuid() {
        return categoryGuid;
    }

    public void setCategoryGuid(final String categoryGuid) {
        this.categoryGuid = categoryGuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public TermRelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(final TermRelationshipStatus status) {
        this.status = status;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TermCategorizationHeader)) return false;
        final TermCategorizationHeader that = (TermCategorizationHeader) o;
        return Objects.equals(categoryGuid, that.categoryGuid) &&
                       Objects.equals(relationGuid, that.relationGuid) &&
                       Objects.equals(description, that.description) &&
                       status == that.status;
    }

    @Override
    public int hashCode() {

        return Objects.hash(categoryGuid, relationGuid, description, status);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TermCategorizationId{");
        sb.append("categoryGuid='").append(categoryGuid).append('\'');
        sb.append(", relationGuid='").append(relationGuid).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", displayText='").append(displayText).append('\'');
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }


    public String getRelationGuid() {
        return relationGuid;
    }

    public void setRelationGuid(final String relationGuid) {
        this.relationGuid = relationGuid;
    }
}
