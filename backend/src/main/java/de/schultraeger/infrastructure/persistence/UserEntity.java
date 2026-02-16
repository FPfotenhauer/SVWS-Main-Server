package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {
    @Id
    public UUID id;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public static User toDomain(UserEntity entity) {
        return new User(
            entity.id,
            entity.username,
            entity.passwordHash,
            
            entity.createdAt,
            entity.updatedAt
        );
    }

    public static UserEntity fromDomain(User domain) {
        UserEntity entity = new UserEntity();
        entity.id = domain.id();
        entity.username = domain.username();
        entity.passwordHash = domain.passwordHash();
        
        entity.createdAt = domain.createdAt();
        entity.updatedAt = domain.updatedAt();
        return entity;
    }
}
