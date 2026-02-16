package de.schultraeger.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for a managed school.
 */
public record Schule(
        UUID id,
        UUID svwsServerId,
        String svwsSchema,
        Instant createdAt,
        Instant updatedAt
) {
}
