package de.schultraeger.application.port.out;

import de.schultraeger.domain.SvwsServer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SvwsServerRepository {
    List<SvwsServer> getAllServers();
    Optional<SvwsServer> getById(UUID id);
    SvwsServer save(SvwsServer server);
    void delete(UUID id);
}
