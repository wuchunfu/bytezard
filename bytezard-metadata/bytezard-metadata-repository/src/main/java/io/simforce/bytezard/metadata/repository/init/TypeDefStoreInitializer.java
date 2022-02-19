package io.simforce.bytezard.metadata.repository.init;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.BusinessMetadataDef;
import io.simforce.bytezard.metadata.model.typedef.ClassificationDef;
import io.simforce.bytezard.metadata.model.typedef.EntityDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef.EnumElementDef;
import io.simforce.bytezard.metadata.model.typedef.RelationshipDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef;
import io.simforce.bytezard.metadata.model.typedef.StructDef.AttributeDef;
import io.simforce.bytezard.metadata.model.typedef.TypesDef;
import io.simforce.bytezard.metadata.type.TypeRegistry;

public class TypeDefStoreInitializer {

    public static TypesDef getTypesToCreate(TypesDef typesDef, TypeRegistry typeRegistry) {
        TypesDef typesToCreate = new TypesDef();

        if (CollectionUtils.isNotEmpty(typesDef.getEnumDefs())) {
            for (EnumDef enumDef : typesDef.getEnumDefs()) {
                if (!typeRegistry.isRegisteredType(enumDef.getName())) {
                    typesToCreate.getEnumDefs().add(enumDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef structDef : typesDef.getStructDefs()) {
                if (!typeRegistry.isRegisteredType(structDef.getName())) {
                    typesToCreate.getStructDefs().add(structDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef classificationDef : typesDef.getClassificationDefs()) {
                if (!typeRegistry.isRegisteredType(classificationDef.getName())) {
                    typesToCreate.getClassificationDefs().add(classificationDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef entityDef : typesDef.getEntityDefs()) {
                if (!typeRegistry.isRegisteredType(entityDef.getName())) {
                    typesToCreate.getEntityDefs().add(entityDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                if (!typeRegistry.isRegisteredType(relationshipDef.getName())) {
                    typesToCreate.getRelationshipDefs().add(relationshipDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                if (!typeRegistry.isRegisteredType(businessMetadataDef.getName())) {
                    typesToCreate.getBusinessMetadataDefs().add(businessMetadataDef);
                }
            }
        }

        return typesToCreate;
    }

    public static TypesDef getTypesToUpdate(TypesDef typesDef, TypeRegistry typeRegistry, boolean checkTypeVersion) {
        TypesDef typesToUpdate = new TypesDef();

        if (CollectionUtils.isNotEmpty(typesDef.getStructDefs())) {
            for (StructDef newStructDef : typesDef.getStructDefs()) {
                StructDef  oldStructDef = typeRegistry.getStructDefByName(newStructDef.getName());

                if (oldStructDef == null) {
                    continue;
                }

                if (updateTypeAttributes(oldStructDef, newStructDef, checkTypeVersion)) {
                    typesToUpdate.getStructDefs().add(newStructDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getClassificationDefs())) {
            for (ClassificationDef newClassifDef : typesDef.getClassificationDefs()) {
                ClassificationDef  oldClassifDef = typeRegistry.getClassificationDefByName(newClassifDef.getName());

                if (oldClassifDef == null) {
                    continue;
                }

                if (updateTypeAttributes(oldClassifDef, newClassifDef, checkTypeVersion)) {
                    typesToUpdate.getClassificationDefs().add(newClassifDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEntityDefs())) {
            for (EntityDef newEntityDef : typesDef.getEntityDefs()) {
                EntityDef  oldEntityDef = typeRegistry.getEntityDefByName(newEntityDef.getName());

                if (oldEntityDef == null) {
                    continue;
                }

                if (updateTypeAttributes(oldEntityDef, newEntityDef, checkTypeVersion)) {
                    typesToUpdate.getEntityDefs().add(newEntityDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getEnumDefs())) {
            for (EnumDef newEnumDef : typesDef.getEnumDefs()) {
                EnumDef  oldEnumDef = typeRegistry.getEnumDefByName(newEnumDef.getName());

                if (oldEnumDef == null) {
                    continue;
                }

                if (isTypeUpdateApplicable(oldEnumDef, newEnumDef, checkTypeVersion)) {
                    if (CollectionUtils.isNotEmpty(oldEnumDef.getElementDefs())) {
                        for (EnumElementDef oldEnumElem : oldEnumDef.getElementDefs()) {
                            if (!newEnumDef.hasElement(oldEnumElem.getValue())) {
                                newEnumDef.addElement(oldEnumElem);
                            }
                        }
                    }

                    typesToUpdate.getEnumDefs().add(newEnumDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getRelationshipDefs())) {
            for (RelationshipDef relationshipDef : typesDef.getRelationshipDefs()) {
                RelationshipDef  oldRelationshipDef = typeRegistry.getRelationshipDefByName(relationshipDef.getName());

                if (oldRelationshipDef == null) {
                    continue;
                }

                if (updateTypeAttributes(oldRelationshipDef, relationshipDef, checkTypeVersion)) {
                    typesToUpdate.getRelationshipDefs().add(relationshipDef);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(typesDef.getBusinessMetadataDefs())) {
            for (BusinessMetadataDef businessMetadataDef : typesDef.getBusinessMetadataDefs()) {
                BusinessMetadataDef oldDef = typeRegistry.getBusinessMetadataDefByName(businessMetadataDef.getName());

                if (oldDef == null) {
                    continue;
                }

                if (updateTypeAttributes(oldDef, businessMetadataDef, checkTypeVersion)) {
                    typesToUpdate.getBusinessMetadataDefs().add(businessMetadataDef);
                }
            }
        }

        return typesToUpdate;
    }

    private static boolean updateTypeAttributes(StructDef oldStructDef, StructDef newStructDef, boolean checkTypeVersion) {
        boolean ret = isTypeUpdateApplicable(oldStructDef, newStructDef, checkTypeVersion);

        if (ret) {
            // make sure that all attributes in oldDef are in newDef as well
            if (CollectionUtils.isNotEmpty(oldStructDef.getAttributeDefs())){
                for (AttributeDef oldAttrDef : oldStructDef.getAttributeDefs()) {
                    if (!newStructDef.hasAttribute(oldAttrDef.getName())) {
                        newStructDef.addAttribute(oldAttrDef);
                    }
                }
            }
        }

        return ret;
    }

    private static boolean isTypeUpdateApplicable(BaseTypeDef oldTypeDef, BaseTypeDef newTypeDef, boolean checkVersion) {
        boolean ret = true;

        if (checkVersion) {
            String oldTypeVersion = oldTypeDef.getTypeVersion();
            String newTypeVersion = newTypeDef.getTypeVersion();

            ret = ObjectUtils.compare(newTypeVersion, oldTypeVersion) > 0;
        }

        return ret;
    }
}
