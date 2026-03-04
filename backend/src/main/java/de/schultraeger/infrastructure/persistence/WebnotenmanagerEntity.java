package de.schultraeger.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the webnotenmanager table.
 */
@Entity
@Table(name = "webnotenmanager")
public class WebnotenmanagerEntity {
    @Id
    public UUID id;

    @Column(name = "schule_id", nullable = false)
    public UUID schuleId;

    @Column(name = "notenserver_base_url", nullable = false, length = 512)
    public String notenserverBaseUrl;

    @Column(name = "oauth_secret_encrypted", nullable = false, length = 512)
    public String oauthSecretEncrypted;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
