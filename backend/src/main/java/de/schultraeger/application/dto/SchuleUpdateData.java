package de.schultraeger.application.dto;

/**
 * Input data for updating a school.
 */
public record SchuleUpdateData(
        String name,
        String svwsUrl,
        String svwsSchema,
        String svwsUsername,
        String svwsPassword
) {
}
