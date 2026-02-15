package de.schultraeger.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for a managed school.
 */
public record Schule(
        UUID id,
        String name,
        Long schulnummer,
        String svwsUrl,
        String svwsSchema,
        String svwsUsername,
        String svwsPasswordEncrypted,
        SchuleStatus status,
        Instant lastSyncAt,
        SyncStatus lastSyncStatus,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {
}
