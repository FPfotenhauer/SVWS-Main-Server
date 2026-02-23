package de.schultraeger.infrastructure.routing;

import de.schultraeger.application.dto.DistanceResult;
import de.schultraeger.application.dto.GeocodeCoordinates;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Client for GraphHopper Routing and Geocoding APIs.
 * Provides distance calculations and address geocoding for Germany.
 */
@ApplicationScoped
public class GraphHopperClient {
    
    private static final String ROUTING_PATH = "/route";
    private static final String HEALTH_PATH = "/health";
    
    @ConfigProperty(name = "graphhopper.url", defaultValue = "http://localhost:8989")
    String graphHopperUrl;
    
    @Inject
    ObjectMapper objectMapper;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    /**
     * Check if GraphHopper service is available.
     */
    public boolean isHealthy() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(graphHopperUrl + HEALTH_PATH))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            Log.warn("GraphHopper health check failed", e);
            return false;
        }
    }
    
    /**
     * Geocode an address string to coordinates.
     * Uses OpenStreetMap Nominatim service for geocoding.
     * @param address Full address string (e.g., "Hauptstrasse 123, 12345 Berlin")
     * @return Optional containing geocoded coordinates, or empty if geocoding failed
     */
    public Optional<GeocodeCoordinates> geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            // Use OpenStreetMap Nominatim for geocoding (free, public service)
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedAddress + 
                        "&format=jsonv2&limit=1&countrycodes=de";
            
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .header("User-Agent", "SVWS-Main-Server/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                Log.warn("Geocoding request failed with status " + response.statusCode() + " for address: " + address);
                return Optional.empty();
            }
            
            JsonNode responseBody = objectMapper.readTree(response.body());
            
            // Check if we have results in the response
            if (!responseBody.isArray() || responseBody.isEmpty()) {
                Log.debug("No geocoding results for address: " + address);
                return Optional.empty();
            }
            
            JsonNode firstResult = responseBody.get(0);
            Double lat = firstResult.get("lat").asDouble();
            Double lon = firstResult.get("lon").asDouble();
            String name = firstResult.has("display_name") ? firstResult.get("display_name").asText() : address;
            
            return Optional.of(new GeocodeCoordinates(lat, lon, name));
            
        } catch (Exception e) {
            Log.warn("Error geocoding address: " + address, e);
            return Optional.empty();
        }
    }
    
    /**
     * Calculate distance and time between two coordinates using car routing.
     * Supports both GraphHopper and OSRM routing engines.
     * @param startLat Starting latitude
     * @param startLon Starting longitude
     * @param endLat Destination latitude
     * @param endLon Destination longitude
     * @return DistanceResult containing distance and time, or error message
     */
    public DistanceResult calculateDistance(Double startLat, Double startLon, Double endLat, Double endLon) {
        if (startLat == null || startLon == null || endLat == null || endLon == null) {
            return new DistanceResult("Missing required coordinates");
        }

        try {
            // Detect if using OSRM or GraphHopper based on URL pattern
            boolean isOsrm = graphHopperUrl.contains("5000") || graphHopperUrl.contains("osrm");
            
            if (isOsrm) {
                return calculateDistanceOsrm(startLat, startLon, endLat, endLon);
            } else {
                return calculateDistanceGraphHopper(startLat, startLon, endLat, endLon);
            }
        } catch (Exception e) {
            String message = "Error calculating distance: " + e.getMessage();
            Log.warn(message, e);
            return new DistanceResult(message);
        }
    }

    private DistanceResult calculateDistanceGraphHopper(Double startLat, Double startLon, Double endLat, Double endLon) {
        try {
            // Build routing request for GraphHopper
            String params = String.format(
                    "point=%f,%f&point=%f,%f&vehicle=car&locale=de",
                    startLat, startLon, endLat, endLon
            );
            
            String url = graphHopperUrl + ROUTING_PATH + "?" + params;
            
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();
            
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = "Routing request failed with status " + response.statusCode();
                Log.warn(errorMsg);
                return new DistanceResult(errorMsg);
            }
            
            JsonNode responseBody = objectMapper.readTree(response.body());
            
            // Check for errors in response
            if (responseBody.has("message")) {
                String errorMsg = responseBody.get("message").asText();
                Log.warn("GraphHopper routing error: " + errorMsg);
                return new DistanceResult(errorMsg);
            }
            
            // Extract path information
            if (!responseBody.has("paths") || responseBody.get("paths").isEmpty()) {
                return new DistanceResult("No route found");
            }
            
            JsonNode path = responseBody.get("paths").get(0);
            
            Long distanceMeters = path.get("distance").asLong();
            Long timeMilliseconds = path.get("time").asLong();
            
            DistanceResult result = new DistanceResult(distanceMeters, timeMilliseconds);
            
            // Optional: extract polyline if available
            if (path.has("points")) {
                String polyline = path.get("points").asText();
                result.setPolyline(polyline);
            }
            
            Log.debug("Distance calculated: " + result.getDistanceKm() + " km, " + result.getTimeMinutes() + " minutes");
            return result;
            
        } catch (Exception e) {
            String message = "Error calculating distance with GraphHopper: " + e.getMessage();
            Log.warn(message, e);
            return new DistanceResult(message);
        }
    }

    private DistanceResult calculateDistanceOsrm(Double startLat, Double startLon, Double endLat, Double endLon) {
        try {
            DistanceResult result = new DistanceResult();
            
            // Calculate base car route
            boolean carRouteSuccess = calculateOsrmRoute("driving", startLat, startLon, endLat, endLon, result, "car");
            
            if (carRouteSuccess && result.getDistanceMeters() != null) {
                // Use the car route as base and estimate bike and foot times based on typical speeds
                Long distanceMeters = result.getDistanceMeters();
                
                // Bike: Average speed ~20 km/h (~5.56 m/s)
                Long bikeTimeSeconds = (distanceMeters * 1000) / 5560;
                result.setBikeDistanceMeters(distanceMeters);
                result.setBikeTimeMilliseconds(bikeTimeSeconds * 1000);
                
                // Walking: Average speed ~4-5 km/h (~1.25 m/s)
                Long footTimeSeconds = (distanceMeters * 1000) / 1250;
                result.setFootDistanceMeters(distanceMeters);
                result.setFootTimeMilliseconds(footTimeSeconds * 1000);
                
                Log.debug("OSRM Distance calculated - Car: " + result.getDistanceKm() + " km, " +
                        "Bike: " + result.getBikeDistanceKm() + " km (estimated), " +
                        "Foot: " + result.getFootDistanceKm() + " km (estimated)");
            } else {
                return result;
            }
            
            return result;
            
        } catch (Exception e) {
            String message;
            if (e instanceof java.net.ConnectException || e.getCause() instanceof java.nio.channels.UnresolvedAddressException) {
                message = "OSRM service unavailable. Ensure the osrm container is running and initialized.";
            } else {
                String details = e.getMessage();
                if (details == null || details.isBlank()) {
                    details = e.getClass().getSimpleName();
                }
                message = "Error calculating distance with OSRM: " + details;
            }
            Log.warn(message, e);
            return new DistanceResult(message);
        }
    }
    
    private boolean calculateOsrmRoute(String profile, Double startLat, Double startLon, Double endLat, Double endLon,
                                        DistanceResult result, String modeKey) {
        try {
            // Build routing request for OSRM
            // OSRM format: /route/v1/{profile}/{lon},{lat};{lon},{lat}
            String url = String.format(
                    "%s/route/v1/%s/%f,%f;%f,%f?overview=false",
                    graphHopperUrl,
                    profile,
                    startLon, startLat,  // OSRM uses lon,lat order
                    endLon, endLat
            );
            
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();
            
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = profile + " routing failed with status " + response.statusCode();
                Log.warn(errorMsg);
                result.setError(errorMsg);
                return false;
            }
            
            JsonNode responseBody = objectMapper.readTree(response.body());
            
            // Check OSRM response code
            String code = responseBody.get("code").asText();
            if (!"Ok".equals(code)) {
                String errorMsg = "OSRM error for " + profile + ": " + code;
                Log.warn(errorMsg);
                result.setError(errorMsg);
                return false;
            }
            
            // Extract route information
            if (!responseBody.has("routes") || responseBody.get("routes").isEmpty()) {
                String errorMsg = "No route found for " + profile;
                Log.warn(errorMsg);
                result.setError(errorMsg);
                return false;
            }
            
            JsonNode route = responseBody.get("routes").get(0);
            
            // OSRM returns distance in meters and duration in seconds
            Long distanceMeters = route.get("distance").asLong();
            Long timeSeconds = route.get("duration").asLong();
            Long timeMilliseconds = timeSeconds * 1000;
            
            // Set the result for this mode
            switch (modeKey) {
                case "car":
                    result.setDistanceMeters(distanceMeters);
                    result.setTimeMilliseconds(timeMilliseconds);
                    break;
                case "bike":
                    result.setBikeDistanceMeters(distanceMeters);
                    result.setBikeTimeMilliseconds(timeMilliseconds);
                    break;
                case "foot":
                    result.setFootDistanceMeters(distanceMeters);
                    result.setFootTimeMilliseconds(timeMilliseconds);
                    break;
            }
            
            return true;
            
        } catch (Exception e) {
            String errorMsg = "Error calculating " + profile + " route: " + e.getMessage();
            Log.warn(errorMsg, e);
            result.setError(errorMsg);
            return false;
        }
    }
}
