package de.schultraeger.infrastructure.security;

import de.schultraeger.application.security.TenantContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Tenant context resolved from the JWT.
 */
@RequestScoped
public class JwtTenantContext implements TenantContext {
    private static final String CLAIM_TENANT_ID = "tenant_id";

    @Inject
    JsonWebToken token;

    @Override
    public UUID getTenantId() {
        String tenantId = token.getClaim(CLAIM_TENANT_ID);
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Missing tenant_id claim");
        }
        return UUID.fromString(tenantId);
    }

    @Override
    public String getUserId() {
        String sub = token.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("Missing sub claim");
        }
        return sub;
    }
}
