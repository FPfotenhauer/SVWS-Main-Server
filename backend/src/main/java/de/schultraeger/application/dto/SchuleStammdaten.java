package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * School master data from SVWS with optional geocoding coordinates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchuleStammdaten {
    
    private Long schulNr;
    
    @JsonProperty("bezeichnung1")
    private String bezeichnung1;
    
    private String schulform;
    
    @JsonProperty("idSchuljahresabschnitt")
    private Integer idSchuljahresabschnitt;
    
    // Address fields for display
    private String strassenname;
    private String hausnummer;
    private String hausnummerZusatz;
    private String plz;
    private String ort;
    
    // Geocoding coordinates (optional, can be cached)
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("geocedAt")
    private Long geocodedAt;  // Timestamp when geocoding was done
    
    public SchuleStammdaten() {}
    
    public SchuleStammdaten(Long schulNr, String bezeichnung1, String schulform, Integer idSchuljahresabschnitt) {
        this.schulNr = schulNr;
        this.bezeichnung1 = bezeichnung1;
        this.schulform = schulform;
        this.idSchuljahresabschnitt = idSchuljahresabschnitt;
    }
    
    // Getters and Setters
    public Long getSchulNr() {
        return schulNr;
    }
    
    public void setSchulNr(Long schulNr) {
        this.schulNr = schulNr;
    }
    
    public String getBezeichnung1() {
        return bezeichnung1;
    }
    
    public void setBezeichnung1(String bezeichnung1) {
        this.bezeichnung1 = bezeichnung1;
    }
    
    public String getSchulform() {
        return schulform;
    }
    
    public void setSchulform(String schulform) {
        this.schulform = schulform;
    }
    
    public Integer getIdSchuljahresabschnitt() {
        return idSchuljahresabschnitt;
    }
    
    public void setIdSchuljahresabschnitt(Integer idSchuljahresabschnitt) {
        this.idSchuljahresabschnitt = idSchuljahresabschnitt;
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