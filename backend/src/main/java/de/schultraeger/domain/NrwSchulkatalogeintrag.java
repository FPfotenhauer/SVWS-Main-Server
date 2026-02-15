package de.schultraeger.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for NRW school catalog entry from Schulministerium NRW.
 */
public record NrwSchulkatalogeintrag(
        UUID id,
        String schulnummer,
        String amtsbez1,
        String amtsbez2,
        String amtsbez3,
        String schultraegernummer,
        String schultraegername,
        String schulname,
        String schultyp,
        String strasse,
        String plz,
        String ort,
        String kreis,
        String aufloesung,
        String schulamt,
        String telefon,
        String fax,
        String email,
        String homepage,
        Instant createdAt,
        Instant updatedAt
) {
}
