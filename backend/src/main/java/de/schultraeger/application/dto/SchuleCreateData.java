package de.schultraeger.application.dto;

/**
 * Input data for creating a school.
 */
public record SchuleCreateData(
        String name,
        String svwsUrl,
        String svwsSchema,
        String svwsUsername,
        String svwsPassword
) {
}
