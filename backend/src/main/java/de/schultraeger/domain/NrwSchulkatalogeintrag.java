package de.schultraeger.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for NRW school catalog entry from Schulministerium NRW.
 */
public record NrwSchulkatalogeintrag(
        UUID id,
        String schulnummer,
        String schulname,
        String schultyp,
        String strasse,
        String plz,
        String ort,
        String kreis,
        String schulamt,
        String telefon,
        String fax,
        String email,
        String homepage,
        Instant createdAt,
        Instant updatedAt
) {
}
