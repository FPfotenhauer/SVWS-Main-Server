package de.schultraeger.api;

import de.schultraeger.api.dto.SchuleRequest;
import de.schultraeger.api.dto.SchuleResponse;
import de.schultraeger.application.SchuleNotFoundException;
import de.schultraeger.application.SchuleService;
import de.schultraeger.application.dto.SchuleCreateData;
import de.schultraeger.application.dto.SchuleUpdateData;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SvwsServer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

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

    private static SchuleCreateData toCreateData(SchuleRequest request) {
        return new SchuleCreateData(
                request.svwsServerId(),
                request.svwsSchema(),
                request.svwsUsername(),
                request.svwsPassword()
        );
    }

    private static SchuleUpdateData toUpdateData(SchuleRequest request) {
        return new SchuleUpdateData(
                request.svwsServerId(),
                request.svwsSchema(),
                request.svwsUsername(),
                request.svwsPassword()
        );
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
