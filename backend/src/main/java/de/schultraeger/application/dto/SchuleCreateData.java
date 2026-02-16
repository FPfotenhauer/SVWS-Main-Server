package de.schultraeger.application.dto;

import java.util.UUID;

/**
 * Input data for creating a school.
 */
public record SchuleCreateData(
        UUID svwsServerId,
        String svwsSchema,
        String svwsUsername,
        String svwsPassword
) {
}
