package de.schultraeger.domain;

/**
 * Connection status for SVWS server.
 */
public enum ServerStatus {
    UNTESTED,
    CONNECTED,
    INVALID_CREDENTIALS,
    UNREACHABLE,
    ERROR
}
