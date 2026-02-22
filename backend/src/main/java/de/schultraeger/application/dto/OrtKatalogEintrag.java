package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrtKatalogEintrag(
        @JsonProperty("id") Long id,
        @JsonProperty("plz") String plz,
        @JsonProperty("ortsname") String ortsname
) {
}
