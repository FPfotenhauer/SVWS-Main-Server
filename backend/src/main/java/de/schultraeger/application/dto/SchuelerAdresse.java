package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Student address data suitable for distance calculation and geocoding.
 * Can be created from SchuelerStammdaten.
 */
public class SchuelerAdresse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("strassenname")
    private String strassenname;
    
    @JsonProperty("hausnummer")
    private String hausnummer;
    
    @JsonProperty("hausnummerZusatz")
    private String hausnummerZusatz;
    
    @JsonProperty("plz")
    private String plz;
    
    @JsonProperty("ort")
    private String ort;
    
    // Geocoding coordinates (optional, can be cached)
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    public SchuelerAdresse() {}
    
    public SchuelerAdresse(Long id, String strassenname, String hausnummer, 
                          String hausnummerZusatz, String plz, String ort) {
        this.id = id;
        this.strassenname = strassenname;
        this.hausnummer = hausnummer;
        this.hausnummerZusatz = hausnummerZusatz;
        this.plz = plz;
        this.ort = ort;
    }
    
    /**
     * Create from SchuelerStammdaten.
     */
    public static SchuelerAdresse from(SchuelerStammdaten stammdaten) {
        SchuelerAdresse adresse = new SchuelerAdresse(
                stammdaten.getId(),
                stammdaten.getStrassenname(),
                stammdaten.getHausnummer(),
                stammdaten.getHausnummerZusatz(),
                stammdaten.getPlz(),
                stammdaten.getOrt()
        );
        adresse.setLatitude(stammdaten.getLatitude());
        adresse.setLongitude(stammdaten.getLongitude());
        return adresse;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
}
