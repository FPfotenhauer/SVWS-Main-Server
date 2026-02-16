package de.schultraeger.application.security;

/**
 * Access to the authenticated tenant and user context.
 */
public interface TenantContext {
    String getUserId();
}
