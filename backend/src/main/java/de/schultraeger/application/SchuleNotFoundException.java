package de.schultraeger.application;

import java.util.UUID;

/**
 * Thrown when a school is not found for the current tenant.
 */
public class SchuleNotFoundException extends RuntimeException {
    public SchuleNotFoundException(UUID id) {
        super("Schule not found: " + id);
    }
}
