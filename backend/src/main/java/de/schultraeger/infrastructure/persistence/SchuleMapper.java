package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.Schule;


/**
 * Maps between domain models and JPA entities.
 */
public class SchuleMapper {
    public Schule toDomain(SchuleEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Schule(
                entity.id,
                entity.svwsServerId,
                entity.svwsSchema,
                entity.svwsUsername,
                entity.svwsUserPasswordEncrypted,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public SchuleEntity toEntity(Schule schule) {
        SchuleEntity entity = new SchuleEntity();
        entity.id = schule.id();
        entity.svwsServerId = schule.svwsServerId();
        entity.svwsSchema = schule.svwsSchema();
        entity.svwsUsername = schule.svwsUsername();
        entity.svwsUserPasswordEncrypted = schule.svwsUserPasswordEncrypted();
        entity.createdAt = schule.createdAt();
        entity.updatedAt = schule.updatedAt();
        return entity;
    }

    public void updateEntity(SchuleEntity entity, Schule schule) {
        entity.svwsServerId = schule.svwsServerId();
        entity.svwsSchema = schule.svwsSchema();
        entity.svwsUsername = schule.svwsUsername();
        entity.svwsUserPasswordEncrypted = schule.svwsUserPasswordEncrypted();
    }
}
