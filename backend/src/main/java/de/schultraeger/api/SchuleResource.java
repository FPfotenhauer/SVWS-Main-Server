package de.schultraeger.api;

import de.schultraeger.api.dto.SchuleResponse;
import de.schultraeger.api.dto.SchuleRequest;
import de.schultraeger.api.dto.SchuleStammdatenResponse;
import de.schultraeger.api.dto.SchuelerAdresseResponse;
import de.schultraeger.api.dto.SchuelerAuswahlResponse;
import de.schultraeger.application.SchuleService;
import de.schultraeger.application.EntfernungsberechnungService;
import de.schultraeger.application.dto.SchuleStammdatenResult;
import de.schultraeger.application.dto.SchuleStatistikenGesamt;
import de.schultraeger.application.dto.SchuelerStammdaten;
import de.schultraeger.application.dto.DistanceResult;
import de.schultraeger.application.dto.SchuelerAdresse;
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST endpoints for school management.
 */
@Path("/api/schulen")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SchuleResource {
    private final SchuleService service;
    private final EntfernungsberechnungService distanceService;
    private final SvwsServerRepository serverRepository;

    public SchuleResource(SchuleService service, EntfernungsberechnungService distanceService, SvwsServerRepository serverRepository) {
        this.service = service;
        this.distanceService = distanceService;
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

    @GET
    @Path("{id}/schueler/auswahlliste")
    public List<SchuelerAuswahlResponse> getSchuelerAuswahlliste(@PathParam("id") UUID id) {
        try {
            return service.getSchuelerAuswahlliste(id)
                    .stream()
                    .map(s -> new SchuelerAuswahlResponse(
                            s.id(),
                            s.nachname(),
                            s.vorname(),
                            s.geburtsdatum(),
                            s.status()
                    ))
                    .toList();
        } catch (IllegalStateException ex) {
            throw new WebApplicationException(ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("{id}/schueler/{schuelerId}/stammdaten")
    public SchuelerAdresseResponse getSchuelerStammdaten(@PathParam("id") UUID id, @PathParam("schuelerId") Long schuelerId) {
        try {
            SchuelerStammdaten s = service.getSchuelerStammdaten(id, schuelerId);
            return new SchuelerAdresseResponse(
                    s.getId(),
                    s.getNachname(),
                    s.getVorname(),
                    s.getGeburtsdatum(),
                    s.getStrassenname(),
                    s.getHausnummer(),
                    s.getHausnummerZusatz(),
                    s.getPlz(),
                    s.getOrt()
            );
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(ex.getMessage(), Response.Status.NOT_FOUND);
        } catch (IllegalStateException ex) {
            throw new WebApplicationException(ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("{id}/schueler/{schuelerId}/entfernung")
    public DistanceResult getDistanceToStudent(@PathParam("id") UUID id, @PathParam("schuelerId") Long schuelerId) {
        try {
            // Get school stammdaten with address
            SchuleStammdatenResult schoolResult = service.getSchuleStammdatenById(id);
            if (schoolResult == null || schoolResult.stammdaten() == null) {
                return new DistanceResult("School not found");
            }
            
            // Get student address
            SchuelerStammdaten studentData = service.getSchuelerStammdaten(id, schuelerId);
            SchuelerAdresse studentAddress = SchuelerAdresse.from(studentData);
            
            // Calculate distance
            return distanceService.calculateDistanceForStudent(
                    schoolResult.stammdaten(),
                    studentAddress
            );
        } catch (NoSuchElementException ex) {
            return new DistanceResult("Student or school not found");
        } catch (IllegalStateException ex) {
            return new DistanceResult("Error: " + ex.getMessage());
        } catch (Exception ex) {
            return new DistanceResult("Distance calculation failed: " + ex.getMessage());
        }
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

    @GET
    @Path("{id}/backup")
    @Produces("application/vnd.sqlite3")
    public Response exportBackup(@PathParam("id") UUID id) {
        try {
            Schule schule = service.getById(id);
            byte[] backup = service.exportBackup(id);
            
            String filename = "backup_" + schule.svwsSchema() + "_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sqlite";
            
            return Response.ok(backup)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/vnd.sqlite3")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Backup export failed: " + e.getMessage())
                    .build();
        }
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
                result.stammdaten() != null ? result.stammdaten().getSchulNr() : null,
                result.stammdaten() != null ? result.stammdaten().getBezeichnung1() : null,
                result.stammdaten() != null ? result.stammdaten().getSchulform() : null,
                result.error()
        );
    }
}
