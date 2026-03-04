package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.port.out.WebnotenmanagerRepository;
import de.schultraeger.domain.Webnotenmanager;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache-backed repository adapter for Webnotenmanager access.
 */
@ApplicationScoped
public class WebnotenmanagerRepositoryPanache implements WebnotenmanagerRepository, PanacheRepositoryBase<WebnotenmanagerEntity, UUID> {
    private final WebnotenmanagerMapper mapper = new WebnotenmanagerMapper();

    @Override
    public List<Webnotenmanager> findAllConfigs() {
        return listAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Webnotenmanager> findConfigById(UUID id) {
        return findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Webnotenmanager> findConfigBySchuleId(UUID schuleId) {
        return find("schuleId = ?1", schuleId)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Webnotenmanager saveConfig(Webnotenmanager webnotenmanager) {
        WebnotenmanagerEntity entity = mapper.toEntity(webnotenmanager);
        persist(entity);
        flush();
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Webnotenmanager updateConfig(Webnotenmanager webnotenmanager) {
        WebnotenmanagerEntity entity = findByIdOptional(webnotenmanager.id())
                .orElseThrow(() -> new IllegalArgumentException("Webnotenmanager not found with id: " + webnotenmanager.id()));
        mapper.updateEntity(entity, webnotenmanager);
        flush();
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteConfigById(UUID id) {
        deleteById(id);
    }

    @Override
    @Transactional
    public void deleteConfigBySchuleId(UUID schuleId) {
        delete("schuleId = ?1", schuleId);
    }
}
