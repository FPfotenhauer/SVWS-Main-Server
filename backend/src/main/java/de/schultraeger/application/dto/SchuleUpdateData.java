package de.schultraeger.application.dto;

import java.util.UUID;

/**
 * Input data for updating a school.
 */
public record SchuleUpdateData(
        UUID svwsServerId,
        String svwsSchema,
        String svwsUsername,
        String svwsPassword
) {
}
