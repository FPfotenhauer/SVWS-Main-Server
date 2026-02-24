package de.schultraeger.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
