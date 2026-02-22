package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SchuelerStammdaten(
        @JsonProperty("id") Long id,
        @JsonProperty("nachname") String nachname,
        @JsonProperty("vorname") String vorname,
        @JsonProperty("geburtsdatum") String geburtsdatum,
        @JsonProperty("strassenname") String strassenname,
        @JsonProperty("hausnummer") String hausnummer,
        @JsonProperty("hausnummerZusatz") String hausnummerZusatz,
        @JsonProperty("wohnortID") Long wohnortID,
        @JsonProperty("plz") String plz,
        @JsonProperty("ort") String ort
) {
}
