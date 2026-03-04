package de.schultraeger.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for Webnotenmanager configuration.
 * Stores configuration for managing note servers and oauth credentials per school.
 */
public record Webnotenmanager(
        UUID id,
        UUID schuleId,
        String notenserverBaseUrl,
        String oauthSecretEncrypted,
        Instant createdAt,
        Instant updatedAt
) {
}
