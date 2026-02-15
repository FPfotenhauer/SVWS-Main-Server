package de.schultraeger.domain;

/**
 * Connection status for SVWS privileged access.
 */
public enum SchuleStatus {
    UNVERIFIED,
    VERIFIED,
    INVALID_CREDENTIALS,
    UNREACHABLE,
    ERROR
}
