package de.schultraeger.api;

import de.schultraeger.api.dto.ErrorResponse;
import de.schultraeger.api.dto.UserRequest;
import de.schultraeger.api.dto.UserResponse;
import de.schultraeger.application.AuthService;
import de.schultraeger.application.UserAlreadyExistsException;
import de.schultraeger.domain.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger logger = Logger.getLogger(UserResource.class);

    @Inject
    AuthService authService;

    @GET
    public Response listUsers(@HeaderParam("Authorization") String authorizationHeader) {
        if (!isValidBearerHeader(authorizationHeader)) {
            return unauthorizedResponse();
        }

        List<UserResponse> users = authService.listUsers().stream()
            .map(UserResource::toResponse)
            .toList();

        return Response.ok(users).build();
    }

    @POST
    public Response createUser(@HeaderParam("Authorization") String authorizationHeader, UserRequest request) {
        if (!isValidBearerHeader(authorizationHeader)) {
            return unauthorizedResponse();
        }

        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Username and password are required"))
                .build();
        }

        try {
            User created = authService.createUser(request.username().trim(), request.password());
            return Response.status(Response.Status.CREATED)
                .entity(toResponse(created))
                .build();
        } catch (UserAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error creating user: " + e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@HeaderParam("Authorization") String authorizationHeader,
                               @PathParam("id") UUID id,
                               UserRequest request) {
        if (!isValidBearerHeader(authorizationHeader)) {
            return unauthorizedResponse();
        }

        if (request == null || isBlank(request.username())) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Username is required"))
                .build();
        }

        try {
            User updated = authService.updateUser(id, request.username().trim(), request.password());
            return Response.ok(toResponse(updated)).build();
        } catch (UserAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("User not found:")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
            }
            logger.error("Error updating user", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error updating user: " + e.getMessage()))
                .build();
        }
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
            user.id(),
            user.username(),
            user.createdAt(),
            user.updatedAt()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Response unauthorizedResponse() {
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(new ErrorResponse("Missing or invalid Authorization header"))
            .build();
    }

    private boolean isValidBearerHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        try {
            String token = authorizationHeader.substring("Bearer ".length());
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            String username = extractJsonValue(payloadJson, "preferred_username");
            return username != null && !username.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) {
                return null;
            }
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) {
                return null;
            }
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }
}
