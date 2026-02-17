package de.schultraeger.api;

import de.schultraeger.api.dto.SchuleResponse;
import de.schultraeger.api.dto.SchuleRequest;
import de.schultraeger.api.dto.SchuleStammdatenResponse;
import de.schultraeger.application.SchuleService;
import de.schultraeger.application.dto.SchuleStammdatenResult;
import de.schultraeger.application.dto.SchuleStatistikenGesamt;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SvwsServer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

    @GET
    @Path("stammdaten")
    public List<SchuleStammdatenResponse> listStammdaten() {
        return service.listStammdaten()
                .stream()
                .map(this::toStammdatenResponse)
                .toList();
    }

    @GET
    @Path("{id}/statistiken")
    public SchuleStatistikenGesamt getStatistiken(@PathParam("id") UUID id) {
        return service.getStatistiken(id);
    }

    @POST
    public Response create(SchuleRequest request) {
        Schule created = service.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(toResponse(created))
                .build();
    }

    @PUT
    @Path("{id}")
    public SchuleResponse update(@PathParam("id") UUID id, SchuleRequest request) {
        Schule updated = service.update(id, request);
        return toResponse(updated);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    private SchuleResponse toResponse(Schule schule) {
        String serverName = serverRepository.getById(schule.svwsServerId())
                .map(SvwsServer::name)
                .orElse("Unknown Server (" + schule.svwsServerId() + ")");

        return new SchuleResponse(
                schule.id() != null ? schule.id().toString() : null,
                schule.svwsServerId() != null ? schule.svwsServerId().toString() : null,
                serverName,
                schule.svwsSchema(),
                schule.svwsUsername(),
                schule.createdAt() != null ? schule.createdAt().toString() : null,
                schule.updatedAt() != null ? schule.updatedAt().toString() : null
        );
    }

    private SchuleStammdatenResponse toStammdatenResponse(SchuleStammdatenResult result) {
        return new SchuleStammdatenResponse(
                result.schuleId() != null ? result.schuleId().toString() : null,
                result.svwsSchema(),
                result.svwsServerName(),
                result.stammdaten() != null ? result.stammdaten().schulNr() : null,
                result.stammdaten() != null ? result.stammdaten().bezeichnung1() : null,
                result.stammdaten() != null ? result.stammdaten().schulform() : null,
                result.error()
        );
    }
}
