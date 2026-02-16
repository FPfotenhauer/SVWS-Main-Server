package de.schultraeger.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload for school data.
 */
public record SchuleResponse(
        UUID id,
        UUID svwsServerId,
        String svwsServerName,
        String svwsSchema,
        Instant createdAt,
        Instant updatedAt
) {
}
