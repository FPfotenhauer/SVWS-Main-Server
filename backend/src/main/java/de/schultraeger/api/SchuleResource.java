package de.schultraeger.api;

import de.schultraeger.api.dto.SchuleResponse;
import de.schultraeger.application.SchuleService;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SvwsServer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * REST endpoints for school management.
 */
@Path("/api/schulen")
@RolesAllowed("ADMIN")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SchuleResource {
    private final SchuleService service;
    private final SvwsServerRepository serverRepository;

    public SchuleResource(SchuleService service, SvwsServerRepository serverRepository) {
        this.service = service;
        this.serverRepository = serverRepository;
    }

    @GET
    public List<SchuleResponse> list() {
        return service.list().stream().map(this::toResponse).toList();
    }

    private SchuleResponse toResponse(Schule schule) {
        String serverName = serverRepository.getById(schule.svwsServerId())
                .map(SvwsServer::name)
                .orElse("Unknown Server (" + schule.svwsServerId() + ")");

        return new SchuleResponse(
                schule.id(),
                schule.svwsServerId(),
                serverName,
                schule.svwsSchema(),
                schule.createdAt(),
                schule.updatedAt()
        );
    }
}
