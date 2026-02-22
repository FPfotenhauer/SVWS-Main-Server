package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SchuelerAuswahl(
        @JsonProperty("id") Long id,
        @JsonProperty("nachname") String nachname,
        @JsonProperty("vorname") String vorname,
        @JsonProperty("geburtsdatum") String geburtsdatum,
        @JsonProperty("status") Integer status
) {
}
