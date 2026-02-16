package de.schultraeger.api.dto;

import java.util.UUID;

/**
 * Request payload for creating or updating a school.
 */
public record SchuleRequest(
        UUID svwsServerId,
        String svwsSchema,
        String svwsUsername,
        String svwsPassword
) {
}
