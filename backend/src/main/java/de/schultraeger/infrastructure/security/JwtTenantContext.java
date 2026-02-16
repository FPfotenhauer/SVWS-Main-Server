package de.schultraeger.infrastructure.security;

import de.schultraeger.application.security.TenantContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Security context resolved from the JWT.
 */
@RequestScoped
public class JwtTenantContext implements TenantContext {

    @Inject
    JsonWebToken token;

    @Override
    public String getUserId() {
        String sub = token.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("Missing sub claim");
        }
        return sub;
    }
}
