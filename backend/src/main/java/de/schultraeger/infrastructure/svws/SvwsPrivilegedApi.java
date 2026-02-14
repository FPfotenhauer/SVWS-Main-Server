package de.schultraeger.infrastructure.svws;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Minimal SVWS privileged API subset used in the MVP.
 */
@Path("/api")
public interface SvwsPrivilegedApi {
    @GET
    @Path("/privileged/user/isprivileged")
    @Produces(MediaType.APPLICATION_JSON)
    boolean isPrivilegedUser();

    @GET
    @Path("/schema/liste/info/{schema}/schule")
    @Produces(MediaType.APPLICATION_JSON)
    SvwsSchuleInfo getSchuleInfo(@PathParam("schema") String schema);
}
