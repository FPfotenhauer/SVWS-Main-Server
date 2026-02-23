package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for distance calculation result between two locations.
 * Contains distance in meters, time in milliseconds for multiple transport modes.
 */
public class DistanceResult {
    
    // Car transport mode
    @JsonProperty("distance_meters")
    private Long distanceMeters;
    
    @JsonProperty("time_milliseconds")
    private Long timeMilliseconds;
    
    @JsonProperty("distance_km")
    private Double distanceKm;
    
    @JsonProperty("time_minutes")
    private Long timeMinutes;
    
    // Bike transport mode
    @JsonProperty("bike_distance_meters")
    private Long bikeDistanceMeters;
    
    @JsonProperty("bike_time_milliseconds")
    private Long bikeTimeMilliseconds;
    
    @JsonProperty("bike_distance_km")
    private Double bikeDistanceKm;
    
    @JsonProperty("bike_time_minutes")
    private Long bikeTimeMinutes;
    
    // Foot transport mode
    @JsonProperty("foot_distance_meters")
    private Long footDistanceMeters;
    
    @JsonProperty("foot_time_milliseconds")
    private Long footTimeMilliseconds;
    
    @JsonProperty("foot_distance_km")
    private Double footDistanceKm;
    
    @JsonProperty("foot_time_minutes")
    private Long footTimeMinutes;
    
    @JsonProperty("polyline")
    private String polyline;  // Encoded polyline string (optional)
    
    @JsonProperty("error")
    private String error;  // Error message if route not found
    
    public DistanceResult() {}
    
    public DistanceResult(Long distanceMeters, Long timeMilliseconds) {
        this.distanceMeters = distanceMeters;
        this.timeMilliseconds = timeMilliseconds;
        this.distanceKm = distanceMeters / 1000.0;
        this.timeMinutes = timeMilliseconds / 60000;
    }
    
    public DistanceResult(String error) {
        this.error = error;
    }
    
    public Long getDistanceMeters() {
        return distanceMeters;
    }
    
    public void setDistanceMeters(Long distanceMeters) {
        this.distanceMeters = distanceMeters;
        if (distanceMeters != null) {
            this.distanceKm = distanceMeters / 1000.0;
        }
    }
    
    public Long getTimeMilliseconds() {
        return timeMilliseconds;
    }
    
    public void setTimeMilliseconds(Long timeMilliseconds) {
        this.timeMilliseconds = timeMilliseconds;
        if (timeMilliseconds != null) {
            this.timeMinutes = timeMilliseconds / 60000;
        }
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    public Long getTimeMinutes() {
        return timeMinutes;
    }
    
    public void setTimeMinutes(Long timeMinutes) {
        this.timeMinutes = timeMinutes;
    }
    
    public Long getBikeDistanceMeters() {
        return bikeDistanceMeters;
    }
    
    public void setBikeDistanceMeters(Long bikeDistanceMeters) {
        this.bikeDistanceMeters = bikeDistanceMeters;
        if (bikeDistanceMeters != null) {
            this.bikeDistanceKm = bikeDistanceMeters / 1000.0;
        }
    }
    
    public Long getBikeTimeMilliseconds() {
        return bikeTimeMilliseconds;
    }
    
    public void setBikeTimeMilliseconds(Long bikeTimeMilliseconds) {
        this.bikeTimeMilliseconds = bikeTimeMilliseconds;
        if (bikeTimeMilliseconds != null) {
            this.bikeTimeMinutes = bikeTimeMilliseconds / 60000;
        }
    }
    
    public Double getBikeDistanceKm() {
        return bikeDistanceKm;
    }
    
    public void setBikeDistanceKm(Double bikeDistanceKm) {
        this.bikeDistanceKm = bikeDistanceKm;
    }
    
    public Long getBikeTimeMinutes() {
        return bikeTimeMinutes;
    }
    
    public void setBikeTimeMinutes(Long bikeTimeMinutes) {
        this.bikeTimeMinutes = bikeTimeMinutes;
    }
    
    public Long getFootDistanceMeters() {
        return footDistanceMeters;
    }
    
    public void setFootDistanceMeters(Long footDistanceMeters) {
        this.footDistanceMeters = footDistanceMeters;
        if (footDistanceMeters != null) {
            this.footDistanceKm = footDistanceMeters / 1000.0;
        }
    }
    
    public Long getFootTimeMilliseconds() {
        return footTimeMilliseconds;
    }
    
    public void setFootTimeMilliseconds(Long footTimeMilliseconds) {
        this.footTimeMilliseconds = footTimeMilliseconds;
        if (footTimeMilliseconds != null) {
            this.footTimeMinutes = footTimeMilliseconds / 60000;
        }
    }
    
    public Double getFootDistanceKm() {
        return footDistanceKm;
    }
    
    public void setFootDistanceKm(Double footDistanceKm) {
        this.footDistanceKm = footDistanceKm;
    }
    
    public Long getFootTimeMinutes() {
        return footTimeMinutes;
    }
    
    public void setFootTimeMinutes(Long footTimeMinutes) {
        this.footTimeMinutes = footTimeMinutes;
    }
    
    public String getPolyline() {
        return polyline;
    }
    
    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "DistanceResult{" +
                "distanceMeters=" + distanceMeters +
                ", timeMilliseconds=" + timeMilliseconds +
                ", distanceKm=" + distanceKm +
                ", timeMinutes=" + timeMinutes +
                ", error='" + error + '\'' +
                '}';
    }
}
