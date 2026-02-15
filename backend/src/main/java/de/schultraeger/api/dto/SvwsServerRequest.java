package de.schultraeger.api.dto;

/**
 * Request payload for creating or updating an SVWS server.
 */
public record SvwsServerRequest(
    String name,
    String baseUrl,
    String username,
    String password
) {
}
