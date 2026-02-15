package de.schultraeger.api.dto;

/**
 * Response DTO for NRW school catalog entry.
 */
public record NrwSchulkatalogeintragResponse(
        String id,
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
        String homepage
) {
}
