package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a SVWS schema list entry from the SVWS API.
 */
public record SchemaListeEintrag(
        String name,
        String username,
        @JsonProperty("isSVWS") Boolean isSVWS,
        Long revision,
        @JsonProperty("isTainted") Boolean isTainted,
        @JsonProperty("isInConfig") Boolean isInConfig,
        @JsonProperty("isDeactivated") Boolean isDeactivated
) {
}
