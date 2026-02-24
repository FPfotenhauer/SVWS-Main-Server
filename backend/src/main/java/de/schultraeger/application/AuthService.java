package de.schultraeger.application;

import de.schultraeger.application.port.out.PasswordHasher;
import de.schultraeger.application.port.out.UserRepository;
import de.schultraeger.domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthService {
    @Inject
    UserRepository userRepository;

    @Inject
    PasswordHasher passwordHasher;

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
            .filter(user -> passwordHasher.verify(password, user.passwordHash()));
    }

    public List<User> listUsers() {
        return userRepository.findAllUsers().stream()
            .sorted((a, b) -> a.username().compareToIgnoreCase(b.username()))
            .toList();
    }

    @Transactional
    public User createUser(String username, String password) throws UserAlreadyExistsException {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        User user = new User(
            java.util.UUID.randomUUID(),
            username,
            passwordHasher.hash(password),
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );
        return userRepository.save(user);
    }

    @Transactional
    public User changePassword(String username, String currentPassword, String newPassword) throws Exception {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found: " + username);
        }

        User user = userOpt.get();
        if (!passwordHasher.verify(currentPassword, user.passwordHash())) {
            throw new Exception("Current password is incorrect");
        }

        User updatedUser = new User(
            user.id(),
            user.username(),
            passwordHasher.hash(newPassword),
            user.createdAt(),
            java.time.LocalDateTime.now()
        );
        return userRepository.save(updatedUser);
    }

    @Transactional
    public User updateUser(UUID id, String username, String newPassword) throws Exception {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new Exception("User not found: " + id);
        }

        User existingUser = existingUserOpt.get();

        Optional<User> userWithSameUsername = userRepository.findByUsername(username);
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().id().equals(id)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        String passwordHash = existingUser.passwordHash();
        if (newPassword != null && !newPassword.isBlank()) {
            passwordHash = passwordHasher.hash(newPassword);
        }

        User updatedUser = new User(
            existingUser.id(),
            username,
            passwordHash,
            existingUser.createdAt(),
            java.time.LocalDateTime.now()
        );

        return userRepository.save(updatedUser);
    }
}
