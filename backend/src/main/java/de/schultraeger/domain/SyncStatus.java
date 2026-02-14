package de.schultraeger.domain;

/**
 * Status of the last manual sync.
 */
public enum SyncStatus {
    SUCCESS,
    INVALID_CREDENTIALS,
    UNREACHABLE,
    ERROR
}
