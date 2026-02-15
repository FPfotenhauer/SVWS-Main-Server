package de.schultraeger.api;

import de.schultraeger.api.dto.NrwSchulkatalogeintragResponse;
import de.schultraeger.application.NrwSchulkatalogeintragService;
import de.schultraeger.application.port.out.NrwClientException;
import de.schultraeger.domain.NrwSchulkatalogeintrag;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST endpoints for NRW school catalog.
 */
@Path("/api/nrw-schulkatalog")
@Produces(MediaType.APPLICATION_JSON)
public class NrwSchulkatalogeintragResource {
    private static final Logger LOG = Logger.getLogger(NrwSchulkatalogeintragResource.class);

    private final NrwSchulkatalogeintragService service;

    public NrwSchulkatalogeintragResource(NrwSchulkatalogeintragService service) {
        this.service = service;
    }

    /**
     * Get paginated list of all schools.
     */
    @GET
    @Path("/schools")
    @PermitAll
    public Response listSchools(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("50") int pageSize,
            @QueryParam("sortBy") @DefaultValue("schulnummer") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir) {
        try {
            List<NrwSchulkatalogeintrag> schools = service.listAllSchools(page, pageSize, sortBy, sortDir);
            long total = service.getTotalCount();
            
            List<NrwSchulkatalogeintragResponse> responses = schools.stream()
                    .map(this::toResponse)
                    .toList();

            return Response.ok()
                    .entity(new SchoolListResponse(responses, total, page, pageSize))
                    .build();
        } catch (Exception e) {
            LOG.error("Failed to list schools", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to list schools: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Search schools.
     */
    @GET
    @Path("/search")
    @PermitAll
    public Response search(
            @QueryParam("q") String query,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("50") int pageSize,
            @QueryParam("sortBy") @DefaultValue("schulnummer") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return listSchools(page, pageSize, sortBy, sortDir);
            }

            List<NrwSchulkatalogeintrag> schools = service.search(query, page, pageSize, sortBy, sortDir);
            long total = service.getSearchResultCount(query);
            
            List<NrwSchulkatalogeintragResponse> responses = schools.stream()
                    .map(this::toResponse)
                    .toList();

            return Response.ok()
                    .entity(new SchoolListResponse(responses, total, page, pageSize))
                    .build();
        } catch (Exception e) {
            LOG.error("Failed to search schools", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to search schools: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Manually trigger catalog refresh (public endpoint for initial load).
     */
    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshCatalog() {
        try {
            service.refreshCatalog();
            return Response.ok()
                    .entity(new MessageResponse("Catalog refresh initiated"))
                    .build();
        } catch (NrwClientException e) {
            LOG.error("Failed to refresh catalog", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to refresh catalog: " + e.getMessage()))
                    .build();
        }
    }

    private NrwSchulkatalogeintragResponse toResponse(NrwSchulkatalogeintrag domain) {
        return new NrwSchulkatalogeintragResponse(
                domain.id().toString(),
                domain.schulnummer(),
            domain.amtsbez1(),
            domain.amtsbez2(),
            domain.amtsbez3(),
                domain.schultraegernummer(),
                domain.schultraegername(),
                domain.schulname(),
                domain.schultyp(),
                domain.strasse(),
                domain.plz(),
                domain.ort(),
                domain.kreis(),
                domain.aufloesung(),
                domain.schulamt(),
                domain.telefon(),
                domain.fax(),
                domain.email(),
                domain.homepage()
        );
    }

    /**
     * Response wrapper for school list.
     */
    public record SchoolListResponse(
            List<NrwSchulkatalogeintragResponse> schools,
            long total,
            int page,
            int pageSize
    ) {
    }

    /**
     * Generic error response.
     */
    public record ErrorResponse(String error) {
    }

    /**
     * Generic message response.
     */
    public record MessageResponse(String message) {
    }
}
