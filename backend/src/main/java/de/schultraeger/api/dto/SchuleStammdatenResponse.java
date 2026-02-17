package de.schultraeger.api.dto;

public record SchuleStammdatenResponse(
        String id,
        String schema,
        String serverName,
        Long schulnummer,
        String bezeichnung1,
        String schulform,
        String error
) {
}