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

import java.util.Objects;

@JSON
public class GlossaryTermHeader {
    private String termGuid;
    private String qualifiedName;

    public GlossaryTermHeader(String termGuid) {
        this.termGuid = termGuid;
    }

    public GlossaryTermHeader(String termGuid, String qualifiedName) {
        this.termGuid = termGuid;
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GlossaryTermHeader{");
        sb.append("termGuid='").append(termGuid).append('\'');
        sb.append(", qualifiedName='").append(qualifiedName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public GlossaryTermHeader() {
    }

    public String getTermGuid() {
        return termGuid;
    }

    public void setTermGuid(final String termGuid) {
        this.termGuid = termGuid;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof io.simforce.bytezard.metadata.model.glossary.GlossaryTermHeader)) {
            return false;
        }
        final io.simforce.bytezard.metadata.model.glossary.GlossaryTermHeader that = (io.simforce.bytezard.metadata.model.glossary.GlossaryTermHeader) o;
        return Objects.equals(termGuid, that.termGuid) &&
                Objects.equals(qualifiedName, that.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(termGuid, qualifiedName);
    }

}
