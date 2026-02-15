package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.port.out.UserRepository;
import de.schultraeger.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepositoryPanache implements PanacheRepository<UserEntity>, UserRepository {

    @Override
    public Optional<User> findByUsername(String username) {
        return find("username", username)
            .firstResultOptional()
            .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        // Check if entity with this ID already exists in session
        // If it does, getEntityManager().merge() will update it instead of creating a new one
        UserEntity merged = getEntityManager().merge(entity);
        persistAndFlush(merged);
        return UserEntity.toDomain(merged);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return find("id", id)
            .firstResultOptional()
            .map(UserEntity::toDomain);
    }
}
