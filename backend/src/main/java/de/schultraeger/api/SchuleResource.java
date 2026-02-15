package de.schultraeger.api;

import de.schultraeger.api.dto.SchuleRequest;
import de.schultraeger.api.dto.SchuleResponse;
import de.schultraeger.application.SchuleNotFoundException;
import de.schultraeger.application.SchuleService;
import de.schultraeger.application.dto.SchuleCreateData;
import de.schultraeger.application.dto.SchuleUpdateData;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.domain.Schule;
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

    public SchuleResource(SchuleService service) {
        this.service = service;
    }

    @GET
    public List<SchuleResponse> list() {
        return service.list().stream().map(SchuleResource::toResponse).toList();
    }

    @POST
    public Response create(SchuleRequest request) {
        Schule created = service.create(toCreateData(request));
        return Response.status(Response.Status.CREATED).entity(toResponse(created)).build();
    }

    @PUT
    @Path("/{id}")
    public SchuleResponse update(@PathParam("id") UUID id, SchuleRequest request) {
        try {
            return toResponse(service.update(id, toUpdateData(request)));
        } catch (SchuleNotFoundException ex) {
            throw new jakarta.ws.rs.NotFoundException(ex.getMessage());
        }
    }

    @POST
    @Path("/{id}/verify")
    public SchuleResponse verify(@PathParam("id") UUID id) {
        try {
            return toResponse(service.verify(id));
        } catch (SchuleNotFoundException ex) {
            throw new jakarta.ws.rs.NotFoundException(ex.getMessage());
        }
    }

    @POST
    @Path("/{id}/sync")
    public SchuleResponse sync(@PathParam("id") UUID id) {
        try {
            return toResponse(service.sync(id));
        } catch (SchuleNotFoundException ex) {
            throw new jakarta.ws.rs.NotFoundException(ex.getMessage());
        }
    }

    @GET
    @Path("/{id}/svws-info")
    public SvwsSchuleInfo getSvwsInfo(@PathParam("id") UUID id) {
        try {
            return service.getSvwsSchoolInfo(id);
        } catch (SchuleNotFoundException ex) {
            throw new jakarta.ws.rs.NotFoundException(ex.getMessage());
        }
    }

    private static SchuleCreateData toCreateData(SchuleRequest request) {
        return new SchuleCreateData(
                request.name(),
                request.svwsUrl(),
                request.svwsSchema(),
                request.svwsUsername(),
                request.svwsPassword()
        );
    }

    private static SchuleUpdateData toUpdateData(SchuleRequest request) {
        return new SchuleUpdateData(
                request.name(),
                request.svwsUrl(),
                request.svwsSchema(),
                request.svwsUsername(),
                request.svwsPassword()
        );
    }

    private static SchuleResponse toResponse(Schule schule) {
        return new SchuleResponse(
                schule.id(),
                schule.name(),
                schule.schulnummer(),
                schule.svwsUrl(),
                schule.svwsSchema(),
                schule.svwsUsername(),
                schule.status(),
                schule.lastSyncAt(),
                schule.lastSyncStatus(),
                schule.lastError(),
                schule.createdAt(),
                schule.updatedAt(),
                schule.strasse(),
                schule.hausnummer(),
                schule.hausnummerZusatz(),
                schule.plz(),
                schule.ort(),
                schule.telefon(),
                schule.fax(),
                schule.email(),
                schule.homepage(),
                schule.schulleiter(),
                schule.schulleiterTelefon(),
                schule.schulleiterEmail(),
                schule.kreis(),
                schule.schulamt(),
                schule.schulnummer2(),
                schule.schulstatus()
        );
    }
}
