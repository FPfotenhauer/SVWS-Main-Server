package de.schultraeger.application.dto;

import java.util.UUID;

public record SchuleStammdatenResult(
        UUID schuleId,
        String svwsSchema,
        String svwsServerName,
        SchuleStammdaten stammdaten,
        String error
) {
}