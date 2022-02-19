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

package io.simforce.bytezard.metadata.repository.listener;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.glossary.GlossaryTerm;
import io.simforce.bytezard.metadata.model.instance.Classification;
import io.simforce.bytezard.metadata.model.instance.Entity;
import io.simforce.bytezard.metadata.model.instance.RelatedObjectId;
import io.simforce.bytezard.metadata.model.instance.Relationship;

/**
 * Entity change notification listener V2.
 */
public interface EntityChangeListenerV2 {
    /**
     * This is upon adding new entities to the repository.
     *
     * @param entities the created entities
     * @param isImport
     */
    void onEntitiesAdded(List<Entity> entities, boolean isImport) throws BaseException;

    /**
     * This is upon updating an entity.
     *
     * @param entities the updated entities
     * @param isImport
     */
    void onEntitiesUpdated(List<Entity> entities, boolean isImport) throws BaseException;

    /**
     * This is upon deleting entities from the repository.
     *
     * @param entities the deleted entities
     * @param isImport
     */
    void onEntitiesDeleted(List<Entity> entities, boolean isImport) throws BaseException;


    /**
     * This is upon purging entities from the repository.
     *
     * @param entities the purged entities
     */
    void onEntitiesPurged(List<Entity> entities) throws BaseException;

    /**
     * This is upon adding new classifications to an entity.
     *
     * @param entity          the entity
     * @param classifications classifications that needs to be added to an entity
     * @throws BaseException if the listener notification fails
     */
    void onClassificationsAdded(Entity entity, List<Classification> classifications) throws BaseException;

    /**
     * This is upon adding new classifications to entities.
     *
     * @param entities              list of entities
     * @param classifications classifications that are to be added to entities
     * @throws BaseException if the listener notification fails
     */
    void onClassificationsAdded(List<Entity> entities, List<Classification> classifications) throws BaseException;

    /**
     * This is upon updating classifications to an entity.
     *
     * @param entity          the entity
     * @param classifications classifications that needs to be updated for an entity
     * @throws BaseException if the listener notification fails
     */
    void onClassificationsUpdated(Entity entity, List<Classification> classifications) throws BaseException;

    /**
     * This is upon deleting classifications from an entity.
     *
     * @param entity              the entity
     * @param classifications classifications that needs to be updated for an entity
     * @throws BaseException if the listener notification fails
     */
    void onClassificationsDeleted(Entity entity, List<Classification> classifications) throws BaseException;

    /**
     * This is upon deleting classifications from entities.
     *
     * @param entities              list of entities
     * @param classifications classifications that needs to be deleted from entities
     * @throws BaseException if the listener notification fails
     */
    void onClassificationsDeleted(List<Entity> entities, List<Classification> classifications) throws BaseException;

    /**
     * This is upon adding a new term to an entity.
     *
     * @param term     the term
     * @param entities list of entities to which the term is assigned
     */
    void onTermAdded(GlossaryTerm term, List<RelatedObjectId> entities) throws BaseException;

    /**
     * This is upon removing a term from an entity.
     *
     * @param term     the term
     * @param entities list of entities to which the term is assigned
     */
    void onTermDeleted(GlossaryTerm term, List<RelatedObjectId> entities) throws BaseException;

    /**
     * This is upon adding new relationships to the repository.
     *
     * @param relationships the created relationships
     * @param isImport
     */
    void onRelationshipsAdded(List<Relationship> relationships, boolean isImport) throws BaseException;

    /**
     * This is upon updating an relationships.
     *
     * @param relationships the updated relationships
     * @param isImport
     */
    void onRelationshipsUpdated(List<Relationship> relationships, boolean isImport) throws BaseException;

    /**
     * This is upon deleting relationships from the repository.
     *
     * @param relationships the deleted relationships
     * @param isImport
     */
    void onRelationshipsDeleted(List<Relationship> relationships, boolean isImport) throws BaseException;

    /**
     * This is upon purging relationships from the repository.
     *
     * @param relationships the purged relationships
     */
    void onRelationshipsPurged(List<Relationship> relationships) throws BaseException;

    /**
     * This is upon add new labels to an entity.
     *
     * @param entity the entity
     * @param labels labels that needs to be added to an entity
     * @throws BaseException if the listener notification fails
     */
    void onLabelsAdded(Entity entity, Set<String> labels) throws BaseException;

    /**
     * This is upon deleting labels from an entity.
     *
     * @param entity the entity
     * @param labels labels that needs to be deleted for an entity
     * @throws BaseException if the listener notification fails
     */
    void onLabelsDeleted(Entity entity, Set<String> labels) throws BaseException;

    /**
     *
     * @param entity the entity
     * @param updatedBusinessAttributes business metadata attribute
     * @throws BaseException if the listener notification fails
     */
    void onBusinessAttributesUpdated(Entity entity, Map<String, Map<String, Object>> updatedBusinessAttributes) throws BaseException;
}