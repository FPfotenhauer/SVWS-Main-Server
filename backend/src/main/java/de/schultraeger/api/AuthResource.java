package de.schultraeger.api;

import de.schultraeger.api.dto.ChangePasswordRequest;
import de.schultraeger.api.dto.ChangePasswordResponse;
import de.schultraeger.api.dto.ErrorResponse;
import de.schultraeger.api.dto.LoginRequest;
import de.schultraeger.api.dto.LoginResponse;
import de.schultraeger.application.AuthService;
import de.schultraeger.application.UserAlreadyExistsException;
import de.schultraeger.domain.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger logger = Logger.getLogger(AuthResource.class);

    @Inject
    AuthService authService;

    @ConfigProperty(name = "quarkus.smallrye-jwt.sign.key")
    Optional<String> jwtSignKey;

    private SecretKey getSigningKey() {
        if (jwtSignKey.isEmpty() || jwtSignKey.get().isBlank()) {
            throw new IllegalStateException("JWT_SIGN_KEY environment variable is required but not set. Generate with: openssl rand -base64 32");
        }
        String keyString = jwtSignKey.get();
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest request) {
        try {
            logger.infof("Login attempt for user: %s", request.username());
            var user = authService.authenticate(request.username(), request.password());
            
            if (user.isEmpty()) {
                logger.warnf("Authentication failed for user: %s", request.username());
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid username or password"))
                    .build();
            }

            User authenticatedUser = user.get();
            
            // Build JWT with the configured key
            SecretKey signingKey = getSigningKey();
            
            String token = Jwt.issuer("schultraeger")
                .subject(authenticatedUser.id().toString())
                .claim("preferred_username", authenticatedUser.username())
                .claim("groups", Collections.singletonList("ADMIN"))
                .expiresAt(Instant.now().plusSeconds(86400)) // 24 hours
                .sign(signingKey);

            long expiresIn = 86400;
            logger.infof("User authenticated successfully: %s", request.username());
            return Response.ok(new LoginResponse(token, "Bearer", expiresIn)).build();
        } catch (Exception e) {
            logger.errorf(e, "Error during login for user: %s", request.username());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Authentication service error: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/register")
    @PermitAll
    public Response register(LoginRequest request) {
        try {
            logger.infof("Register attempt for user: %s", request.username());
            User user = authService.createUser(request.username(), request.password());
            logger.infof("User registered successfully: %s", request.username());
            return Response.status(Response.Status.CREATED)
                .entity(new RegisterResponse(user.id(), user.username()))
                .build();
        } catch (UserAlreadyExistsException e) {
            logger.warnf("User already exists: %s", request.username());
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.errorf(e, "Error during registration for user: %s", request.username());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Registration error: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/change-password")
    public Response changePassword(
        @HeaderParam("Authorization") String authorizationHeader,
        ChangePasswordRequest request) {
        try {
            logger.infof("Change password request received. Authorization header: %s", authorizationHeader != null ? "present" : "missing");
            
            // Extract and validate JWT from Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warnf("Invalid Authorization header format");
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Missing or invalid Authorization header"))
                    .build();
            }

            String token = authorizationHeader.substring("Bearer ".length());
            logger.infof("Token extracted, length: %d", token.length());
            
            // Decode JWT payload (format: header.payload.signature)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logger.warnf("Invalid JWT format: expected 3 parts, got %d", parts.length);
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid JWT format"))
                    .build();
            }

            // Decode the payload (second part)
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            logger.infof("JWT payload decoded: %s", payloadJson);
            
            // Extract username using simple string parsing
            String username = extractJsonValue(payloadJson, "preferred_username");
            if (username == null || username.isEmpty()) {
                logger.warnf("Username not found in JWT claims");
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid JWT token - missing username"))
                    .build();
            }
            
            logger.infof("Password change attempt for user: %s", username);
            authService.changePassword(username, request.currentPassword(), request.newPassword());
            logger.infof("Password changed successfully for user: %s", username);
            return Response.ok(new ChangePasswordResponse("Password changed successfully")).build();
        } catch (IllegalArgumentException e) {
            logger.warnf(e, "Base64 decoding failed: %s", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse("Invalid JWT encoding: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.warnf(e, "Password change failed");
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse("Error: " + e.getMessage()))
                .build();
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

    public record RegisterResponse(java.util.UUID id, String username) {
    }
}

