package de.schultraeger.api;

import de.schultraeger.api.dto.ErrorResponse;
import de.schultraeger.api.dto.SvwsServerRequest;
import de.schultraeger.api.dto.SvwsServerResponse;
import de.schultraeger.application.SvwsServerService;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.domain.ServerStatus;
import de.schultraeger.domain.SvwsServer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for SVWS server management.
 */
@Path("/api/svws-servers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SvwsServerResource {
    @Inject
    SvwsServerService service;

    @Inject
    SvwsClient svwsClient;

    @Inject
    PasswordCipher passwordCipher;

    @GET
    public List<SvwsServerResponse> list() {
        return service.listAll().stream()
            .map(SvwsServerResource::toResponse)
            .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return service.findById(id)
            .map(SvwsServerResource::toResponse)
            .map(Response::ok)
            .orElse(Response.status(Response.Status.NOT_FOUND))
            .build();
    }

    @POST
    public Response create(SvwsServerRequest request) {
        SvwsServer created = service.create(
            request.name(),
            request.baseUrl(),
            request.username(),
            request.password()
        );
        return Response.status(Response.Status.CREATED)
            .entity(toResponse(created))
            .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, SvwsServerRequest request) {
        try {
            SvwsServer updated = service.update(
                id,
                request.name(),
                request.baseUrl(),
                request.username(),
                request.password()
            );
            return Response.ok(toResponse(updated)).build();
        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/test-connection")
    public Response testConnection(@PathParam("id") UUID id) {
        try {
            SvwsServer server = service.findById(id)
                .orElseThrow(() -> new NotFoundException("SVWS server not found"));

            String password = passwordCipher.decrypt(server.passwordEncrypted());
            boolean isConnected = svwsClient.isPrivileged(
                server.baseUrl(),
                server.username(),
                password
            );

            SvwsServer updated;
            if (isConnected) {
                updated = service.updateStatus(id, ServerStatus.CONNECTED, null);
            } else {
                updated = service.updateStatus(id, ServerStatus.ERROR, "Not privileged or authentication failed");
            }

            return Response.ok(toResponse(updated)).build();
        } catch (Exception e) {
            try {
                SvwsServer updated = service.updateStatus(id, ServerStatus.ERROR, e.getMessage());
                return Response.ok(toResponse(updated)).build();
            } catch (Exception ignored) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
            }
        }
    }

    @GET
    @Path("/{id}/schools")
    public Response listSchools(@PathParam("id") UUID id) {
        try {
            SvwsServer server = service.findById(id)
                .orElseThrow(() -> new NotFoundException("SVWS server not found"));

            String password = passwordCipher.decrypt(server.passwordEncrypted());
            List<SvwsSchuleInfo> schools = svwsClient.listSchools(
                server.baseUrl(),
                server.username(),
                password
            );

            // Update server status to CONNECTED
            service.updateStatus(id, ServerStatus.CONNECTED, null);

            return Response.ok(schools).build();
        } catch (Exception e) {
            try {
                service.updateStatus(id, ServerStatus.ERROR, e.getMessage());
            } catch (Exception ignored) {
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}/version")
    public Response getVersion(@PathParam("id") UUID id) {
        try {
            SvwsServer server = service.findById(id)
                .orElseThrow(() -> new NotFoundException("SVWS server not found"));

            String version = svwsClient.getServerVersion(server.baseUrl());
            return Response.ok(version).build();
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    private static SvwsServerResponse toResponse(SvwsServer server) {
        return new SvwsServerResponse(
            server.id(),
            server.name(),
            server.baseUrl(),
            server.username(),
            server.status(),
            server.lastError(),
            server.createdAt(),
            server.updatedAt()
        );
    }
}
