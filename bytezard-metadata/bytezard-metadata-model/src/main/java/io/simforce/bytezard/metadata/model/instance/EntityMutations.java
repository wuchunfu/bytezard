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
package io.simforce.bytezard.metadata.model.instance;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EntityMutations implements Serializable {

    private List<EntityMutation> entityMutations = new ArrayList<>();

    /**
     *
     */
    public enum EntityOperation {
        CREATE,
        UPDATE,
        PARTIAL_UPDATE,
        DELETE,
        PURGE
    }

    public static final class EntityMutation implements Serializable {
        private final EntityOperation op;
        private final Entity entity;

        public EntityMutation(EntityOperation op, Entity entity) {
            this.op = op;
            this.entity = entity;
        }

        public StringBuilder toString(StringBuilder sb) {
            if ( sb == null) {
                sb = new StringBuilder();
            }
            sb.append("EntityMutation {");
            sb.append("op=").append(op);
            if (entity != null) {
                sb.append(", entity=");
                entity.toString(sb);
            }
            sb.append("}");

            return sb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            EntityMutation that = (EntityMutation) o;
            return op == that.op &&
                    Objects.equals(entity, that.entity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(op, entity);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    public EntityMutations(List<EntityMutation> entityMutations) {
        this.entityMutations = entityMutations;
    }

    public StringBuilder toString(StringBuilder sb) {
        if ( sb == null) {
            sb = new StringBuilder();
        }
        sb.append("EntityMutations{");
        if (CollectionUtils.isNotEmpty(entityMutations)) {
            for (int i = 0; i < entityMutations.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                entityMutations.get(i).toString(sb);
            }
        }
        sb.append("}");

        return sb;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        io.simforce.bytezard.metadata.model.instance.EntityMutations that = (io.simforce.bytezard.metadata.model.instance.EntityMutations) o;
        return Objects.equals(entityMutations, that.entityMutations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityMutations);
    }
}


