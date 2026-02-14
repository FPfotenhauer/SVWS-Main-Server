package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.SvwsServer;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SvwsServerRepositoryPanache implements SvwsServerRepository, PanacheRepositoryBase<SvwsServerEntity, UUID> {

    @Override
    public List<SvwsServer> getAllServers() {
        return listAll().stream()
            .map(SvwsServerEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<SvwsServer> getById(UUID id) {
        return find("id", id)
            .firstResultOptional()
            .map(SvwsServerEntity::toDomain);
    }

    @Override
    public SvwsServer save(SvwsServer server) {
        SvwsServerEntity entity = SvwsServerEntity.fromDomain(server);
        SvwsServerEntity merged = getEntityManager().merge(entity);
        persistAndFlush(merged);
        return SvwsServerEntity.toDomain(merged);
    }

    @Override
    public void delete(UUID id) {
        delete("id", id);
    }
}
