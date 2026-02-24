package de.schultraeger.application.port.out;

import de.schultraeger.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findAllUsers();
}
