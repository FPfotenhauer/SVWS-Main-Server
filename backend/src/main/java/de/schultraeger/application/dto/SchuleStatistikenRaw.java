package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Raw statistics data from SVWS /db/{schema}/statistik/gesamt endpoint.
 * Only includes fields we care about for computing aggregates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SchuleStatistikenRaw(
        @JsonProperty("schueler")
        List<Schueler> schueler,
        
        @JsonProperty("jahrgaenge")
        List<Jahrgang> jahrgaenge,

        @JsonProperty("religionen")
        List<Religion> religionen,

        @JsonProperty("klassen")
        List<Klasse> klassen,

        @JsonProperty("foederschwerpunkte")
        List<Foerderschwerpunkt> foerderschwerpunkte,
        
        @JsonProperty("orte")
        List<Ort> orte
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Schueler(
            @JsonProperty("id")
            Long id,
            
            @JsonProperty("geschlecht")
            Integer geschlecht,  // 3=female, 4=male
            
            @JsonProperty("lernabschnitte")
            List<Lernabschnitt> lernabschnitte,
            
            @JsonProperty("wohnortID")
            Integer wohnortID,

            @JsonProperty("religionID")
            Integer idReligion,

            @JsonProperty("staatsangehoerigkeitID")
            String staatsangehoerigkeitID,
            
            @JsonProperty("idFoerderschwerpunkt1")
            Integer idFoerderschwerpunkt1,
            
            @JsonProperty("idFoerderschwerpunkt2")
            Integer idFoerderschwerpunkt2,
            
            @JsonProperty("abitur")
            Abitur abitur,
            
            @JsonProperty("hatMigrationshintergrund")
            Boolean hatMigrationshintergrund,
            
            @JsonProperty("status")
            Integer status,

            @JsonProperty("vorherigeSchuleNr")
            String vorherigeSchuleNr
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Lernabschnitt(
            @JsonProperty("idJahrgang")
            Integer idJahrgang,

            @JsonProperty("idKlasse")
            Integer idKlasse,

            @JsonProperty("idFoerderschwerpunkt1")
            Integer idFoerderschwerpunkt1,

            @JsonProperty("idFoerderschwerpunkt2")
            Integer idFoerderschwerpunkt2,
            
            @JsonProperty("Klassenart")
            String klassenart
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Abitur(
            @JsonProperty("istZugelassen")
            Boolean istZugelassen,
            
            @JsonProperty("hatBestanden")
            Boolean hatBestanden
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Jahrgang(
            @JsonProperty("id")
            Integer id,
            
            @JsonProperty("kuerzel")
            String kuerzel
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Religion(
            @JsonProperty("id")
            Integer id,

            @JsonProperty("kuerzel")
            String kuerzel
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Klasse(
            @JsonProperty("id")
            Integer id,

            @JsonProperty("kuerzel")
            String kuerzel,

            @JsonProperty("idJahrgang")
            Integer idJahrgang,

            @JsonProperty("sortierung")
            Integer sortierung
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Foerderschwerpunkt(
            @JsonProperty("id")
            Integer id,

            @JsonProperty("kuerzelStatistik")
            String kuerzelStatistik
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ort(
            @JsonProperty("id")
            Integer id,
            
            @JsonProperty("plz")
            String plz,
            
            @JsonProperty("ortsname")
            String ortsname
    ) {}
}
