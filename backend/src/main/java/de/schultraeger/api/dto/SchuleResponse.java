package de.schultraeger.api.dto;

/**
 * Response payload for school data.
 */
public record SchuleResponse(
        String id,
        String svwsServerId,
        String svwsServerName,
        String svwsSchema,
        String svwsUsername,
        String createdAt,
        String updatedAt
) {
}
