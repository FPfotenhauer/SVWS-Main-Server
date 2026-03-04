package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for geocoded coordinates from GraphHopper Geocoding API.
 * Represents latitude/longitude for an address.
 */
public class GeocodeCoordinates {
    
    @JsonProperty("lat")
    private Double latitude;
    
    @JsonProperty("lon")
    private Double longitude;
    
    @JsonProperty("name")
    private String name;  // Address name from geocoding response
    
    @JsonProperty("country")
    private String country;
    
    public GeocodeCoordinates() {}
    
    public GeocodeCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public GeocodeCoordinates(Double latitude, Double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Override
    public String toString() {
        return "GeocodeCoordinates{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
