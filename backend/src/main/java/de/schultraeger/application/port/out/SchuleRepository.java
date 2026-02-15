package de.schultraeger.application.port.out;

import de.schultraeger.domain.Schule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant-aware persistence port for schools.
 */
public interface SchuleRepository {
    List<Schule> findAll(UUID tenantId);

    Optional<Schule> findById(UUID tenantId, UUID id);

    Schule save(UUID tenantId, Schule schule);

    Schule update(UUID tenantId, Schule schule);
}
