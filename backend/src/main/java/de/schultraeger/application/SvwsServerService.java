package de.schultraeger.application;

import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.ServerStatus;
import de.schultraeger.domain.SvwsServer;
import org.eclipse.microprofile.context.ManagedExecutor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

@ApplicationScoped
public class SvwsServerService {
    @Inject
    SvwsServerRepository repository;

    @Inject
    SchuleService schuleService;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    PasswordCipher passwordCipher;

    @Inject
    Event<SvwsServer> serverCreatedEvent;

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
        SvwsServer saved = repository.save(server);
        
        serverCreatedEvent.fire(saved);
        
        return saved;
    }

    /**
     * Listens for server creation and triggers school import after the transaction succeeds.
     */
    void onServerCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) SvwsServer server) {
        managedExecutor.submit(() -> {
            try {
                System.out.println("DEBUG: Starting background school discovery for server " + server.name() + " (" + server.id() + ")");
                // Use the server parameter directly (already from DB), don't try to reload in background thread
                int imported = schuleService.importSchoolsFromSvwsServer(server);
                updateStatusQuietly(server.id(), ServerStatus.CONNECTED, "Imported " + imported + " schools");
            } catch (Exception e) {
                System.err.println("DEBUG: Error in background school discovery: " + e.getMessage());
                e.printStackTrace();
                updateStatusQuietly(server.id(), ServerStatus.ERROR, e.getMessage());
            }
        });
    }

    private void updateStatusQuietly(UUID id, ServerStatus status, String error) {
        try {
            updateStatus(id, status, error);
        } catch (Exception e) {
            System.err.println("Failed to update server status: " + e.getMessage());
        }
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
        schuleService.deleteByServerId(id);
        repository.delete(id);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
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
