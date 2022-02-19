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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.CheckStateRequest;
import io.simforce.bytezard.metadata.model.instance.CheckStateResult;
import io.simforce.bytezard.metadata.model.instance.Classification;
import io.simforce.bytezard.metadata.model.instance.Entity.EntitiesWithExtInfo;
import io.simforce.bytezard.metadata.model.instance.Entity.EntityWithExtInfo;
import io.simforce.bytezard.metadata.model.instance.EntityHeader;
import io.simforce.bytezard.metadata.model.instance.EntityHeader.EntityHeaders;
import io.simforce.bytezard.metadata.model.instance.EntityMutationResponse;
import io.simforce.bytezard.metadata.model.instance.ObjectId;
import io.simforce.bytezard.metadata.type.EntityType;

/**
 * Persistence/Retrieval API for Entity
 */
public interface EntityStore {

    /**
     * List all the entity guids for a given typename
     * @param typename
     * @return
     * @throws BaseException
     */
    List<String> getEntityGUIDS(String typename) throws BaseException;

    /**
     *
     * Get entity definition by its guid
     * @param guid
     * @return Entity
     */
    EntityWithExtInfo getById(String guid) throws BaseException;

    /**
     *
     * Get entity definition by its guid
     * @param guid
     * @param isMinExtInfo
     * @return Entity
     */
    EntityWithExtInfo getById(String guid, boolean isMinExtInfo, boolean ignoreRelationships) throws BaseException;

    /**
     * Get entity header for the given GUID
     * @param guid
     * @return
     * @throws BaseException
     */
    EntityHeader getHeaderById(String guid) throws BaseException;

    public EntityHeader getEntityHeaderByUniqueAttributes(EntityType entityType, Map<String, Object> uniqAttributes) throws BaseException;

    /**
     * Batch GET to retrieve entities by their ID
     * @param guid
     * @return
     * @throws BaseException
     */
    EntitiesWithExtInfo getByIds(List<String> guid) throws BaseException;

    /**
     * Batch GET to retrieve entities by their ID
     * @param guid
     * @param isMinExtInfo
     * @return
     * @throws BaseException
     */
    EntitiesWithExtInfo getByIds(List<String> guid, boolean isMinExtInfo, boolean ignoreRelationships) throws BaseException;

    /**
     * Batch GET to retrieve entities by their uniqueIds
     * @param entityType
     * @param uniqueAttributes
     * @param isMinExtInfo
     * @param ignoreRelationships
     * @return
     * @throws BaseException
     */
    EntitiesWithExtInfo getEntitiesByUniqueAttributes(EntityType entityType, List<Map<String, Object>> uniqueAttributes, boolean isMinExtInfo, boolean ignoreRelationships) throws BaseException;

    /**
     *
     * Get an eneity by its unique attribute
     * @param entityType     type of the entity
     * @param uniqAttributes Attributes that uniquely identify the entity
     * @return EntityMutationResponse details of the updates performed by this call
     */
    EntityWithExtInfo getByUniqueAttributes(EntityType entityType, Map<String, Object> uniqAttributes)
            throws BaseException;

    /**
     *
     * Get an eneity by its unique attribute
     * @param entityType     type of the entity
     * @param uniqAttributes Attributes that uniquely identify the entity
     * @param isMinExtInfo
     * @param ignoreRelationships ignore relationship attributes
     * @return EntityMutationResponse details of the updates performed by this call
     */
    EntityWithExtInfo getByUniqueAttributes(EntityType entityType, Map<String, Object> uniqAttributes, boolean isMinExtInfo, boolean ignoreRelationships)
            throws BaseException;

    /**
     * Check state of entities in the store
     * @param request CheckStateRequest
     * @return CheckStateResult
     * @throws BaseException
     */
    CheckStateResult checkState(CheckStateRequest request) throws BaseException;

    /**
     * Create or update  entities in the stream
     * @param entityStream EntityStream
     * @return EntityMutationResponse Entity mutations operations with the corresponding set of entities on which these operations were performed
     * @throws BaseException
     */
    EntityMutationResponse createOrUpdate(EntityStream entityStream, boolean isPartialUpdate) throws BaseException;

    /**
     * Create or update  entities with parameters necessary for import process
     * @param entityStream EntityStream
     * @return EntityMutationResponse Entity mutations operations with the corresponding set of entities on which these operations were performed
     * @throws BaseException
     */
    EntityMutationResponse createOrUpdateForImport(EntityStream entityStream) throws BaseException;

    /**
     * Create or update  entities with parameters necessary for import process without commit. Caller will have to do take care of commit.
     * @param entityStream EntityStream
     * @return EntityMutationResponse Entity mutations operations with the corresponding set of entities on which these operations were performed
     * @throws BaseException
     */
    EntityMutationResponse createOrUpdateForImportNoCommit(EntityStream entityStream) throws BaseException;

    /**
     * Update a single entity
     * @param objectId     ID of the entity
     * @param updatedEntityInfo updated entity information
     * @return EntityMutationResponse details of the updates performed by this call
     * @throws BaseException
     *
     */
    EntityMutationResponse updateEntity(ObjectId objectId, EntityWithExtInfo updatedEntityInfo, boolean isPartialUpdate) throws BaseException;

    /**
     * Update a single entity
     * @param entityType     type of the entity
     * @param uniqAttributes Attributes that uniquely identify the entity
     * @return EntityMutationResponse details of the updates performed by this call
     * @throws BaseException
     *
     */
    EntityMutationResponse updateByUniqueAttributes(EntityType entityType, Map<String, Object> uniqAttributes,
                                                    EntityWithExtInfo entity) throws BaseException;

    /**
     * Partial update entities attribute using its guid.
     * @param guid Entity guid
     * @param attrName attribute name to be updated
     * @param attrValue updated attribute value
     * @return EntityMutationResponse details of the updates performed by this call
     * @throws BaseException
     */
    EntityMutationResponse updateEntityAttributeByGuid(String guid, String attrName, Object attrValue) throws BaseException;

    /**
     * Delete an entity by its guid
     * @param guid
     * @return
     */
    EntityMutationResponse deleteById(String guid) throws BaseException;

    /**
     * Deletes an entity using its type and unique attributes
     * @param entityType      type of the entity
     * @param uniqAttributes Attributes that uniquely identify the entity
     * @return EntityMutationResponse details of the updates performed by this call
     * @throws BaseException
     */
    EntityMutationResponse deleteByUniqueAttributes(EntityType entityType, Map<String, Object> uniqAttributes)
            throws BaseException;
    /**
     *
     * Get an entity guid by its unique attributes
     * @param entityType     type of the entity
     * @param uniqAttributes Attributes that uniquely identify the entity
     * @return String entity guid
     * @throws BaseException
     */

    String getGuidByUniqueAttributes(EntityType entityType, Map<String, Object> uniqAttributes) throws BaseException;

    /*
     * Return list of deleted entity guids
     */
    EntityMutationResponse deleteByIds(List<String> guid) throws BaseException;

    /*
     * Return list of purged entity guids
     */
    EntityMutationResponse purgeByIds(Set<String> guids) throws BaseException;

    /**
     * Add classification(s)
     */
    void addClassifications(String guid, List<Classification> classification) throws BaseException;

    /**
     * Update classification(s)
     */
    void updateClassifications(String guid, List<Classification> classifications) throws BaseException;

    void addClassification(List<String> guids, Classification classification) throws BaseException;

    /**
     * Delete classification
     */
    void deleteClassification(String guid, String classificationName) throws BaseException;

    void deleteClassification(String guid, String classificationName, String associatedEntityGuid) throws BaseException;

    List<Classification> getClassifications(String guid) throws BaseException;

    Classification getClassification(String guid, String classificationName) throws BaseException;

    String setClassifications(EntityHeaders entityHeaders);

    /**
     * Set labels to given entity, if labels is null/empty, existing labels will all be removed.
     */
    void setLabels(String guid, Set<String> labels) throws BaseException;

    /**
     *
     * @param guid
     * @param businessAttrbutes
     * @param isOverwrite
     * @throws BaseException
     */
    void addOrUpdateBusinessAttributes(String guid, Map<String, Map<String, Object>> businessAttrbutes, boolean isOverwrite) throws BaseException;

    /**
     *
     * @param guid
     * @param businessAttributes
     * @throws BaseException
     */
    void removeBusinessAttributes(String guid, Map<String, Map<String, Object>> businessAttributes) throws BaseException;

    /**
     * Remove given labels, if labels is null/empty, no labels will be removed. If any labels in
     * labels set are non-existing labels, they will be ignored, only existing labels will be removed.
     */
    void removeLabels(String guid, Set<String> labels) throws BaseException;

    /**
     * Add given labels to the given entity, if labels is null/empty, no labels will be added.
     */
    void addLabels(String guid, Set<String> labels) throws BaseException;

    /**
     *
     * @param inputStream
     * @param fileName
     * @throws BaseException
     *
     */
//    BulkImportResponse bulkCreateOrUpdateBusinessAttributes(InputStream inputStream, String fileName) throws BaseException;
}
