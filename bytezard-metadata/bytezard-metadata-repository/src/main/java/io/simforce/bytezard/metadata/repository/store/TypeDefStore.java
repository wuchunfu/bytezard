package io.simforce.bytezard.metadata.repository.store;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.SearchFilter;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.TypesDef;

public interface TypeDefStore {

    void init() throws BaseException;

    /* EnumDef operations */

    EnumDef getEnumDefByName(String name) throws BaseException;

    EnumDef getEnumDefByGuid(String guid) throws BaseException;

    EnumDef updateEnumDefByName(String name, EnumDef enumDef) throws BaseException;

    EnumDef updateEnumDefByGuid(String guid, EnumDef enumDef) throws BaseException;

    /* StructDef operations */

    StructDef getStructDefByName(String name) throws BaseException;

    StructDef getStructDefByGuid(String guid) throws BaseException;

    StructDef updateStructDefByName(String name, StructDef structDef) throws BaseException;

    StructDef updateStructDefByGuid(String guid, StructDef structDef) throws BaseException;

    /* ClassificationDef operations */

    ClassificationDef getClassificationDefByName(String name) throws BaseException;

    ClassificationDef getClassificationDefByGuid(String guid) throws BaseException;

    ClassificationDef updateClassificationDefByName(String name, ClassificationDef classificationDef)
            throws BaseException;

    ClassificationDef updateClassificationDefByGuid(String guid, ClassificationDef classificationDef)
            throws BaseException;

    /* EntityDef operations */

    EntityDef getEntityDefByName(String name) throws BaseException;

    EntityDef getEntityDefByGuid(String guid) throws BaseException;

    EntityDef updateEntityDefByName(String name, EntityDef entityDef) throws BaseException;

    EntityDef updateEntityDefByGuid(String guid, EntityDef entityDef) throws BaseException;
    /* RelationshipDef operations */

    RelationshipDef getRelationshipDefByName(String name) throws BaseException;

    RelationshipDef getRelationshipDefByGuid(String guid) throws BaseException;

    RelationshipDef updateRelationshipDefByName(String name, RelationshipDef relationshipDef) throws BaseException;

    RelationshipDef updateRelationshipDefByGuid(String guid, RelationshipDef relationshipDef) throws BaseException;

    /* business-metadata Def operations */

    BusinessMetadataDef getBusinessMetadataDefByName(String name) throws BaseException;

    BusinessMetadataDef getBusinessMetadataDefByGuid(String guid) throws BaseException;

    /* Bulk Operations */

    TypesDef createTypesDef(TypesDef typesDef) throws BaseException;

    TypesDef updateTypesDef(TypesDef typesDef) throws BaseException;

    TypesDef createUpdateTypesDef(TypesDef typesToCreateUpdate) throws BaseException;

    TypesDef createUpdateTypesDef(TypesDef typesToCreate, TypesDef typesToUpdate) throws BaseException;

    void deleteTypesDef(TypesDef typesDef) throws BaseException;

    TypesDef searchTypesDef(SearchFilter searchFilter) throws BaseException;


    /* Generic operation */

    BaseTypeDef getByName(String name) throws BaseException;

    BaseTypeDef getByGuid(String guid) throws BaseException;

    void deleteTypeByName(String typeName) throws BaseException;

    void notifyLoadCompletion();
}
