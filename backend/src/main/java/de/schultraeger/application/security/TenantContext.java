package de.schultraeger.application.security;

import java.util.UUID;

/**
 * Access to the authenticated tenant and user context.
 */
public interface TenantContext {
    String getUserId();
}
