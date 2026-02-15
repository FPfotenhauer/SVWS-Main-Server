package de.schultraeger.api.dto;

public record ChangePasswordRequest(
    String currentPassword,
    String newPassword
) {
}
