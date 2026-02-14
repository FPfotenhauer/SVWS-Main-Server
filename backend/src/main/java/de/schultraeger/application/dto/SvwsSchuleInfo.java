package de.schultraeger.application.dto;

/**
 * Minimal school info from the SVWS privileged API.
 */
public record SvwsSchuleInfo(
        Long schulNr,
        String bezeichnung
) {
}
