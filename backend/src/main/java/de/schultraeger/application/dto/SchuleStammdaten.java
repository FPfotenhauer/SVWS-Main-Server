package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SchuleStammdaten(
        Long schulNr,
        @JsonProperty("bezeichnung1") String bezeichnung1,
        String schulform
) {
}