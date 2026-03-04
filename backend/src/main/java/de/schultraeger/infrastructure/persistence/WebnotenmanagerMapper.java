package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.Webnotenmanager;

/**
 * Maps between domain models and JPA entities for Webnotenmanager.
 */
public class WebnotenmanagerMapper {
    public Webnotenmanager toDomain(WebnotenmanagerEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Webnotenmanager(
                entity.id,
                entity.schuleId,
                entity.notenserverBaseUrl,
                entity.oauthSecretEncrypted,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public WebnotenmanagerEntity toEntity(Webnotenmanager webnotenmanager) {
        WebnotenmanagerEntity entity = new WebnotenmanagerEntity();
        entity.id = webnotenmanager.id();
        entity.schuleId = webnotenmanager.schuleId();
        entity.notenserverBaseUrl = webnotenmanager.notenserverBaseUrl();
        entity.oauthSecretEncrypted = webnotenmanager.oauthSecretEncrypted();
        entity.createdAt = webnotenmanager.createdAt();
        entity.updatedAt = webnotenmanager.updatedAt();
        return entity;
    }

    public void updateEntity(WebnotenmanagerEntity entity, Webnotenmanager webnotenmanager) {
        entity.notenserverBaseUrl = webnotenmanager.notenserverBaseUrl();
        entity.oauthSecretEncrypted = webnotenmanager.oauthSecretEncrypted();
    }
}
