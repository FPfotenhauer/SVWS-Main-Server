package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * School info from the SVWS privileged API.
 * This captures comprehensive school information that may be available.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SvwsSchuleInfo(
        // Basic identification
        @JsonAlias("schulNr") Long schulnummer,
        @JsonAlias("bezeichnung") String name,
        String schema,
        
        // Address information
        String plz,
        String ort,
        @JsonAlias("strasse") String strasse,
        @JsonAlias("hausnummer") String hausnummer,
        @JsonAlias("hausnummerZusatz") String hausnummerZusatz,
        
        // Contact information
        @JsonAlias("telefon") String telefon,
        @JsonAlias("fax") String fax,
        @JsonAlias("email") String email,
        @JsonAlias("homepage") String homepage,
        
        // School details
        @JsonAlias("schulform") String schulform,
        @JsonAlias("schulart") String schulart,
        @JsonAlias("schulgliederung") String schulgliederung,
        
        // Administrative information
        @JsonAlias("schulleiter") String schulleiter,
        @JsonAlias("schulleiterTelefon") String schulleiterTelefon,
        @JsonAlias("schulleiterEmail") String schulleiterEmail,
        
        // Location/region information
        @JsonAlias("kreis") String kreis,
        @JsonAlias("schulamt") String schulamt,
        @JsonAlias("staat") String staat,
        
        // Additional metadata
        @JsonAlias("schulnummer2") String schulnummer2,
        @JsonAlias("schulstatus") String schulstatus,
        @JsonAlias("kapitel") String kapitel,
        @JsonAlias("satzungsgebendeKommune") String satzungsgebendeKommune
) {
}
