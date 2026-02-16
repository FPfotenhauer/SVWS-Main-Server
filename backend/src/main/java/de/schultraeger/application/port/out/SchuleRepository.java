package de.schultraeger.application.port.out;

import de.schultraeger.domain.Schule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant-aware persistence port for schools.
 */
public interface SchuleRepository {
    List<Schule> findAllSchools();

    Optional<Schule> findSchoolById(UUID id);

    Schule saveSchool(Schule schule);

    Schule updateSchool(Schule schule);
}
