package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * School info from the SVWS privileged API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SvwsSchuleInfo(
        @JsonAlias("schulNr") Long schulnummer,
        @JsonAlias("bezeichnung") String name,
        String plz,
        String ort
) {
}
