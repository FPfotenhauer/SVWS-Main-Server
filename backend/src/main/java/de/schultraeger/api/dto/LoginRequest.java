package de.schultraeger.api.dto;

public record LoginRequest(
    String username,
    String password
) {
}
