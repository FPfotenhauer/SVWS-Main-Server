package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.ServerStatus;
import de.schultraeger.domain.SvwsServer;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "svws_servers")
public class SvwsServerEntity extends PanacheEntityBase {
    @Id
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(name = "base_url", nullable = false)
    public String baseUrl;

    @Column(nullable = false)
    public String username;

    @Column(name = "password_encrypted", nullable = false)
    public String passwordEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ServerStatus status;

    @Column(name = "last_error")
    public String lastError;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public static SvwsServer toDomain(SvwsServerEntity entity) {
        return new SvwsServer(
            entity.id,
            entity.name,
            entity.baseUrl,
            entity.username,
            entity.passwordEncrypted,
            entity.status,
            entity.lastError,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public static SvwsServerEntity fromDomain(SvwsServer domain) {
        SvwsServerEntity entity = new SvwsServerEntity();
        entity.id = domain.id();
        entity.name = domain.name();
        entity.baseUrl = domain.baseUrl();
        entity.username = domain.username();
        entity.passwordEncrypted = domain.passwordEncrypted();
        entity.status = domain.status();
        entity.lastError = domain.lastError();
        entity.createdAt = domain.createdAt();
        entity.updatedAt = domain.updatedAt();
        return entity;
    }
}
