package de.schultraeger.api.dto;

public record UserRequest(
    String username,
    String password
) {
}
