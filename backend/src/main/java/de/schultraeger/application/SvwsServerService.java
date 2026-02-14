package de.schultraeger.application;

import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.ServerStatus;
import de.schultraeger.domain.SvwsServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SvwsServerService {
    @Inject
    SvwsServerRepository repository;

    @Inject
    PasswordCipher passwordCipher;

    public List<SvwsServer> listAll() {
        return repository.getAllServers();
    }

    public Optional<SvwsServer> findById(UUID id) {
        return repository.getById(id);
    }

    @Transactional
    public SvwsServer create(String name, String baseUrl, String username, String password) {
        String encrypted = passwordCipher.encrypt(password);
        SvwsServer server = new SvwsServer(
            UUID.randomUUID(),
            name,
            baseUrl,
            username,
            encrypted,
            ServerStatus.UNTESTED,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        return repository.save(server);
    }

    @Transactional
    public SvwsServer update(UUID id, String name, String baseUrl, String username, String password) throws Exception {
        Optional<SvwsServer> existing = repository.getById(id);
        if (existing.isEmpty()) {
            throw new Exception("SVWS server not found: " + id);
        }

        String encrypted = password != null && !password.isEmpty()
            ? passwordCipher.encrypt(password)
            : existing.get().passwordEncrypted();

        SvwsServer updated = new SvwsServer(
            id,
            name,
            baseUrl,
            username,
            encrypted,
            ServerStatus.UNTESTED,
            null,
            existing.get().createdAt(),
            LocalDateTime.now()
        );
        return repository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(id);
    }

    @Transactional
    public SvwsServer updateStatus(UUID id, ServerStatus status, String error) throws Exception {
        Optional<SvwsServer> existing = repository.getById(id);
        if (existing.isEmpty()) {
            throw new Exception("SVWS server not found: " + id);
        }

        SvwsServer current = existing.get();
        SvwsServer updated = new SvwsServer(
            current.id(),
            current.name(),
            current.baseUrl(),
            current.username(),
            current.passwordEncrypted(),
            status,
            error,
            current.createdAt(),
            LocalDateTime.now()
        );
        return repository.save(updated);
    }
}
