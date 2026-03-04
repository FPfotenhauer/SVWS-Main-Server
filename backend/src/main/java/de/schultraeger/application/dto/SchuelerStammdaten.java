package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Student master data from SVWS including address and optional geocoding coordinates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchuelerStammdaten {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("nachname")
    private String nachname;
    
    @JsonProperty("vorname")
    private String vorname;
    
    @JsonProperty("geburtsdatum")
    private String geburtsdatum;
    
    @JsonProperty("strassenname")
    private String strassenname;
    
    @JsonProperty("hausnummer")
    private String hausnummer;
    
    @JsonProperty("hausnummerZusatz")
    private String hausnummerZusatz;
    
    @JsonProperty("wohnortID")
    private Long wohnortID;
    
    @JsonProperty("plz")
    private String plz;
    
    @JsonProperty("ort")
    private String ort;
    
    // Geocoding coordinates (optional, can be cached)
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("geocodedAt")
    private Long geocodedAt;  // Timestamp when geocoding was done
    
    public SchuelerStammdaten() {}
    
    public SchuelerStammdaten(Long id, String nachname, String vorname, String geburtsdatum,
                             String strassenname, String hausnummer, String hausnummerZusatz,
                             Long wohnortID, String plz, String ort) {
        this.id = id;
        this.nachname = nachname;
        this.vorname = vorname;
        this.geburtsdatum = geburtsdatum;
        this.strassenname = strassenname;
        this.hausnummer = hausnummer;
        this.hausnummerZusatz = hausnummerZusatz;
        this.wohnortID = wohnortID;
        this.plz = plz;
        this.ort = ort;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNachname() {
        return nachname;
    }
    
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }
    
    public String getVorname() {
        return vorname;
    }
    
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }
    
    public String getGeburtsdatum() {
        return geburtsdatum;
    }
    
    public void setGeburtsdatum(String geburtsdatum) {
        this.geburtsdatum = geburtsdatum;
    }
    
    public String getStrassenname() {
        return strassenname;
    }
    
    public void setStrassenname(String strassenname) {
        this.strassenname = strassenname;
    }
    
    public String getHausnummer() {
        return hausnummer;
    }
    
    public void setHausnummer(String hausnummer) {
        this.hausnummer = hausnummer;
    }
    
    public String getHausnummerZusatz() {
        return hausnummerZusatz;
    }
    
    public void setHausnummerZusatz(String hausnummerZusatz) {
        this.hausnummerZusatz = hausnummerZusatz;
    }
    
    public Long getWohnortID() {
        return wohnortID;
    }
    
    public void setWohnortID(Long wohnortID) {
        this.wohnortID = wohnortID;
    }
    
    public String getPlz() {
        return plz;
    }
    
    public void setPlz(String plz) {
        this.plz = plz;
    }
    
    public String getOrt() {
        return ort;
    }
    
    public void setOrt(String ort) {
        this.ort = ort;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Long getGeocodedAt() {
        return geocodedAt;
    }
    
    public void setGeocodedAt(Long geocodedAt) {
        this.geocodedAt = geocodedAt;
    }
}
