package de.schultraeger.api.dto;

public record SchuelerAuswahlResponse(
        Long id,
        String nachname,
        String vorname,
        String geburtsdatum,
        Integer status
) {
}
