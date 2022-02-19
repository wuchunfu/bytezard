/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.metadata.repository.listener;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;

public class ChangedTypeDefs {
    private List<? extends BaseTypeDef> createdTypeDefs;
    private List<? extends BaseTypeDef> updatedTypeDefs;
    private List<? extends BaseTypeDef> deletedTypeDefs;

    public ChangedTypeDefs(List<? extends BaseTypeDef> createdTypeDefs,
                           List<? extends BaseTypeDef> updatedTypeDefs,
                           List<? extends BaseTypeDef> deletedTypeDefs) {
        this.createdTypeDefs = createdTypeDefs;
        this.updatedTypeDefs = updatedTypeDefs;
        this.deletedTypeDefs = deletedTypeDefs;
    }

    public ChangedTypeDefs() {
        createdTypeDefs = new ArrayList<>();
        updatedTypeDefs = new ArrayList<>();
        deletedTypeDefs = new ArrayList<>();
    }

    public List<? extends BaseTypeDef> getCreatedTypeDefs() {
        return createdTypeDefs;
    }

    public ChangedTypeDefs setCreatedTypeDefs(List<? extends BaseTypeDef> createdTypeDefs) {
        this.createdTypeDefs = createdTypeDefs;
        return this;
    }

    public List<? extends BaseTypeDef> getUpdatedTypeDefs() {
        return updatedTypeDefs;
    }

    public ChangedTypeDefs setUpdatedTypeDefs(List<? extends BaseTypeDef> updatedTypeDefs) {
        this.updatedTypeDefs = updatedTypeDefs;
        return this;
    }

    public List<? extends BaseTypeDef> getDeletedTypeDefs() {
        return deletedTypeDefs;
    }

    public ChangedTypeDefs setDeletedTypeDefs(List<? extends BaseTypeDef> deletedTypeDefs) {
        this.deletedTypeDefs = deletedTypeDefs;
        return this;
    }

    public boolean hasEntityDef() {
        return hasEntityDef(createdTypeDefs) || hasEntityDef(updatedTypeDefs) || hasEntityDef(deletedTypeDefs);
    }

    private boolean hasEntityDef(List<? extends BaseTypeDef> typeDefs) {
        boolean ret = false;

        if (CollectionUtils.isNotEmpty(typeDefs)) {
            for (BaseTypeDef typeDef : typeDefs) {
                if (typeDef instanceof EntityDef) {
                    ret = true;

                    break;
                }
            }
        }

        return ret;
    }

    public boolean hasBusinessMetadataDef() {
        return hasBusinessMetadataDef(createdTypeDefs) || hasBusinessMetadataDef(updatedTypeDefs) || hasBusinessMetadataDef(deletedTypeDefs);
    }

    private boolean hasBusinessMetadataDef(List<? extends BaseTypeDef> typeDefs) {
        boolean ret = false;

        if (CollectionUtils.isNotEmpty(typeDefs)) {
            for (BaseTypeDef typeDef : typeDefs) {
                if (typeDef instanceof BusinessMetadataDef) {
                    ret = true;

                    break;
                }
            }
        }

        return ret;
    }
}
