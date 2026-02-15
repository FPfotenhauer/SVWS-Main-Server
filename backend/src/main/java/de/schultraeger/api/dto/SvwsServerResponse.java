package de.schultraeger.api.dto;

import de.schultraeger.domain.ServerStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload for SVWS server data.
 */
public record SvwsServerResponse(
    UUID id,
    String name,
    String baseUrl,
    String username,
    ServerStatus status,
    String lastError,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
