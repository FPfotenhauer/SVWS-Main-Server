package de.schultraeger.api.dto;

public record SchuelerAdresseResponse(
        Long id,
        String nachname,
        String vorname,
        String geburtsdatum,
        String strassenname,
        String hausnummer,
        String hausnummerZusatz,
        String plz,
        String ort
) {
}
