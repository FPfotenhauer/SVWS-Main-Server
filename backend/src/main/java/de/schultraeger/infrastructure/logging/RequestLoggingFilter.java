package de.schultraeger.infrastructure.logging;

import de.schultraeger.application.security.TenantContext;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Adds request-scoped MDC fields and correlates logs via request_id.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String MDC_REQUEST_ID = "request_id";
    private static final String MDC_TENANT_ID = "tenant_id";
    private static final String MDC_USER_ID = "user_id";
    private static final String START_TIME = "requestStartTime";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = requestContext.getHeaderString(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_REQUEST_ID, requestId);

        try {
            MDC.put(MDC_TENANT_ID, tenantContext.getTenantId().toString());
        } catch (RuntimeException e) {
            // Tenant context may not be available
        }
        try {
            MDC.put(MDC_USER_ID, tenantContext.getUserId());
        } catch (RuntimeException e) {
            // User context may not be available
        }

        requestContext.setProperty(START_TIME, Instant.now().toEpochMilli());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object start = requestContext.getProperty(START_TIME);
        if (start instanceof Long startMillis) {
            long duration = Instant.now().toEpochMilli() - startMillis;
            responseContext.getHeaders().putSingle("X-Request-Duration-Ms", String.valueOf(duration));
            MDC.put("duration_ms", String.valueOf(duration));
        }
        String requestId = (String) MDC.get(MDC_REQUEST_ID);
        if (requestId != null) {
            responseContext.getHeaders().putSingle(HEADER_REQUEST_ID, requestId);
        }

        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_TENANT_ID);
        MDC.remove(MDC_USER_ID);
        MDC.remove("duration_ms");
    }
}
