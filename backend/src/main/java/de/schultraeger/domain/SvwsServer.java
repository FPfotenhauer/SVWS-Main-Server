package de.schultraeger.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a configured SVWS server instance.
 */
public record SvwsServer(
    UUID id,
    String name,
    String baseUrl,
    String username,
    String passwordEncrypted,
    ServerStatus status,
    String lastError,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
