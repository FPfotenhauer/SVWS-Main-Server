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
        String svwsUsername,
        String svwsUserPasswordEncrypted,
        Instant createdAt,
        Instant updatedAt
) {
}
