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
        Instant updatedAt,
        // Address information
        String strasse,
        String hausnummer,
        String hausnummerZusatz,
        String plz,
        String ort,
        // Contact information
        String telefon,
        String fax,
        String email,
        String homepage,
        // Administrative information
        String schulleiter,
        String schulleiterTelefon,
        String schulleiterEmail,
        // Region information
        String kreis,
        String schulamt,
        // Additional metadata
        String schulnummer2,
        String schulstatus
) {
}
