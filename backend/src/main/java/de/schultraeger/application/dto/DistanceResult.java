package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for distance calculation result between two locations.
 * Contains distance in meters, time in milliseconds, and optional polyline.
 */
public class DistanceResult {
    
    @JsonProperty("distance_meters")
    private Long distanceMeters;
    
    @JsonProperty("time_milliseconds")
    private Long timeMilliseconds;
    
    @JsonProperty("distance_km")
    private Double distanceKm;
    
    @JsonProperty("time_minutes")
    private Long timeMinutes;
    
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
