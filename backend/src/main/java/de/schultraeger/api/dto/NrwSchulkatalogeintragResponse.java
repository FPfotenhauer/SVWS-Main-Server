package de.schultraeger.api.dto;

/**
 * Response DTO for NRW school catalog entry.
 */
public record NrwSchulkatalogeintragResponse(
        String id,
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
        String homepage
) {
}
