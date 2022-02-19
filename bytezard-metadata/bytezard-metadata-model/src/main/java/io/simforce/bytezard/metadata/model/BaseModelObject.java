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
package io.simforce.bytezard.metadata.model;

import io.simforce.bytezard.metadata.model.annotation.JSON;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JSON
public abstract class BaseModelObject implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private static final AtomicLong S_NEXT_ID = new AtomicLong(System.nanoTime());

    private String guid;

    protected void init() {
        setGuid("-" + Long.toString(S_NEXT_ID.incrementAndGet()));
    }

    protected BaseModelObject() {
        init();
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BaseModelObject(final io.simforce.bytezard.metadata.model.BaseModelObject other) {
        this.guid = other.guid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("guid=").append(guid);
        toString(sb);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BaseModelObject)) {
            return false;
        }

        final BaseModelObject that = (BaseModelObject) o;
        return Objects.equals(guid, that.guid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(guid);
    }

    protected abstract StringBuilder toString(StringBuilder sb);
}
