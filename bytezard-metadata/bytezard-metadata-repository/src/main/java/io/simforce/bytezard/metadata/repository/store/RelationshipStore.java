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
package io.simforce.bytezard.metadata.repository.store;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.Relationship;
import io.simforce.bytezard.metadata.model.instance.Relationship.RelationshipWithExtInfo;

/**
 * Persistence/Retrieval API for Relationship
 */
public interface RelationshipStore {
    /**
     * Create a new relationship instance.
     * @param relationship relationship instance defin`ition
     * @return Relationship d
     */
    Relationship create(Relationship relationship) throws BaseException;

    /**
     * Update an existing relationship instance.
     * @param relationship relationship instance definition
     * @return Relationship d
     */
    Relationship update(Relationship relationship) throws BaseException;

    /**
     * Retrieve a relationship instance using guid.
     * @param guid relationship instance guid
     * @return Relationship
     */
    Relationship getById(String guid) throws BaseException;

    /**
     * Retrieve a relationship instance and its referred entities using guid.
     * @param guid relationship instance guid
     * @return Relationship
     */
    RelationshipWithExtInfo getExtInfoById(String guid) throws BaseException;

    Edge getOrCreate(Vertex end1Vertex, Vertex end2Vertex, Relationship relationship) throws BaseException;

    Edge getRelationship(Vertex end1Vertex, Vertex end2Vertex, Relationship relationship) throws BaseException;

    Edge createRelationship(Vertex end1Vertex, Vertex end2Vertex, Relationship relationship) throws BaseException;

    /**
     * Retrieve a relationship if it exists or creates a new relationship instance.
     * @param relationship relationship instance definition
     * @return Relationship
     */
    Relationship getOrCreate(Relationship relationship) throws BaseException;

    /**
     * Delete a relationship instance using guid.
     * @param guid relationship instance guid
     */
    void deleteById(String guid) throws BaseException;

    /**
     * Delete a relationship instance using guid.
     * @param guid relationship instance guid
     * @param forceDelete force delete the relationship edge
     */
    void deleteById(String guid, boolean forceDelete) throws BaseException;
}
