package de.schultraeger.api.dto;

/**
 * Request payload for creating or updating a school.
 */
public record SchuleRequest(
        String svwsServerId,
        String svwsSchema,
        String svwsUsername,
        String svwsPassword
) {
}
