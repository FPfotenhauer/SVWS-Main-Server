package de.schultraeger.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
    UUID id,
    String username,
    String passwordHash,
    UUID tenantId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
