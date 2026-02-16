package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.port.out.SchuleRepository;
import de.schultraeger.domain.Schule;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache-backed repository adapter for school access.
 *
 * Note: Tenancy is implemented via separate databases per tenant; repository logic is tenant-neutral.
 */
@ApplicationScoped
public class SchuleRepositoryPanache implements SchuleRepository, PanacheRepositoryBase<SchuleEntity, UUID> {
    private final SchuleMapper mapper = new SchuleMapper();

    @Override
    public List<Schule> findAllSchools() {
        return listAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Schule> findSchoolById(UUID id) {
        return find("id = ?1", id).firstResultOptional().map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Schule saveSchool(Schule schule) {
        SchuleEntity entity = mapper.toEntity(schule);
        persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Schule updateSchool(Schule schule) {
        SchuleEntity entity = find("id = ?1", schule.id()).firstResult();
        if (entity == null) {
            throw new IllegalStateException("Schule not found");
        }
        mapper.updateEntity(entity, schule);
        return mapper.toDomain(entity);
    }
}
