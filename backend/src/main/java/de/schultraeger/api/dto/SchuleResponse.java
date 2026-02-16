package de.schultraeger.api.dto;

import de.schultraeger.domain.SchuleStatus;
import de.schultraeger.domain.SyncStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload for school data.
 */
public record SchuleResponse(
        UUID id,
        String name,
        Long schulnummer,
        String svwsUrl,
        String svwsSchema,
        String svwsUsername,
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
