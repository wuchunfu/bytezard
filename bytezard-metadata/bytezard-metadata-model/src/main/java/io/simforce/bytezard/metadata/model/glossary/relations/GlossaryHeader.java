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

import java.util.Objects;

@JSON
public class GlossaryHeader {
    private String glossaryGuid;
    private String relationGuid;
    private String displayText;

    public GlossaryHeader(String glossaryGuid) {
        this.glossaryGuid = glossaryGuid;
    }

    public GlossaryHeader() {
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public String getGlossaryGuid() {
        return glossaryGuid;
    }

    public void setGlossaryGuid(final String glossaryGuid) {
        this.glossaryGuid = glossaryGuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GlossaryHeader)) {
            return false;
        }
        final GlossaryHeader that = (GlossaryHeader) o;
        return Objects.equals(glossaryGuid, that.glossaryGuid) &&
                       Objects.equals(relationGuid, that.relationGuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(glossaryGuid, relationGuid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GlossaryId{");
        sb.append("glossaryGuid='").append(glossaryGuid).append('\'');
        sb.append(", relationGuid='").append(relationGuid).append('\'');
        sb.append(", displayText='").append(displayText).append('\'');
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
