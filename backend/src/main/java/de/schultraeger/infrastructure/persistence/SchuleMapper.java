package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.Schule;

import java.util.UUID;

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
                entity.name,
            entity.schulnummer,
                entity.svwsUrl,
            entity.svwsSchema,
                entity.svwsUsername,
                entity.svwsPasswordEncrypted,
                entity.status,
                entity.lastSyncAt,
                entity.lastSyncStatus,
                entity.lastError,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public SchuleEntity toEntity(UUID tenantId, Schule schule) {
        SchuleEntity entity = new SchuleEntity();
        entity.id = schule.id();
        entity.tenantId = tenantId;
        entity.name = schule.name();
        entity.schulnummer = schule.schulnummer();
        entity.svwsUrl = schule.svwsUrl();
        entity.svwsSchema = schule.svwsSchema();
        entity.svwsUsername = schule.svwsUsername();
        entity.svwsPasswordEncrypted = schule.svwsPasswordEncrypted();
        entity.status = schule.status();
        entity.lastSyncAt = schule.lastSyncAt();
        entity.lastSyncStatus = schule.lastSyncStatus();
        entity.lastError = schule.lastError();
        entity.createdAt = schule.createdAt();
        entity.updatedAt = schule.updatedAt();
        return entity;
    }

    public void updateEntity(SchuleEntity entity, Schule schule) {
        entity.name = schule.name();
        entity.schulnummer = schule.schulnummer();
        entity.svwsUrl = schule.svwsUrl();
        entity.svwsSchema = schule.svwsSchema();
        entity.svwsUsername = schule.svwsUsername();
        entity.svwsPasswordEncrypted = schule.svwsPasswordEncrypted();
        entity.status = schule.status();
        entity.lastSyncAt = schule.lastSyncAt();
        entity.lastSyncStatus = schule.lastSyncStatus();
        entity.lastError = schule.lastError();
    }
}
