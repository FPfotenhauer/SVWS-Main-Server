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
 * Panache-backed repository adapter for tenant-aware school access.
 */
@ApplicationScoped
public class SchuleRepositoryPanache implements SchuleRepository, PanacheRepositoryBase<SchuleEntity, UUID> {
    private final SchuleMapper mapper = new SchuleMapper();

    @Override
    public List<Schule> findAll(UUID tenantId) {
        return list("tenantId", tenantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Schule> findById(UUID tenantId, UUID id) {
        return find("id = ?1 and tenantId = ?2", id, tenantId).firstResultOptional().map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Schule save(UUID tenantId, Schule schule) {
        SchuleEntity entity = mapper.toEntity(tenantId, schule);
        persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Schule update(UUID tenantId, Schule schule) {
        SchuleEntity entity = find("id = ?1 and tenantId = ?2", schule.id(), tenantId).firstResult();
        if (entity == null) {
            throw new IllegalStateException("Schule not found for tenant");
        }
        mapper.updateEntity(entity, schule);
        return mapper.toDomain(entity);
    }
}
