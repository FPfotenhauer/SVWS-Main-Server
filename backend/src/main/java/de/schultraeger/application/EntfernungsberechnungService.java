package de.schultraeger.application;

import de.schultraeger.application.dto.GeocodeCoordinates;
import de.schultraeger.application.dto.DistanceResult;
import de.schultraeger.application.dto.SchuelerAdresse;
import de.schultraeger.application.dto.SchuleStammdaten;
import de.schultraeger.infrastructure.routing.GraphHopperClient;
import io.quarkus.logging.Log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

/**
 * Service for calculating distance between school and student addresses.
 * Handles geocoding and routing via GraphHopper.
 */
@ApplicationScoped
public class EntfernungsberechnungService {
    
    @Inject
    GraphHopperClient graphHopperClient;
    
    /**
     * Format an address from school or student data for geocoding.
     * @param strassenname Street name
     * @param hausnummer House number
     * @param hausnummerZusatz House number suffix
     * @param plz Postal code
     * @param ort City/location
     * @return Formatted address string suitable for geocoding
     */
    public String formatAddressForGeocoding(String strassenname, String hausnummer, 
                                            String hausnummerZusatz, String plz, String ort) {
        StringBuilder address = new StringBuilder();
        
        if (strassenname != null && !strassenname.trim().isEmpty()) {
            address.append(strassenname);
            
            if (hausnummer != null && !hausnummer.trim().isEmpty()) {
                address.append(" ").append(hausnummer);
            }
            
            if (hausnummerZusatz != null && !hausnummerZusatz.trim().isEmpty()) {
                address.append(hausnummerZusatz);
            }
            
            address.append(", ");
        }
        
        if (plz != null && !plz.trim().isEmpty()) {
            address.append(plz).append(" ");
        }
        
        if (ort != null && !ort.trim().isEmpty()) {
            address.append(ort);
        }
        
        return address.toString().trim();
    }
    
    /**
     * Calculate distance between school and student.
     * @param schule School with location data
     * @param schueler Student with address data
     * @return DistanceResult with distance/time or error message
     */
    public DistanceResult calculateDistanceForStudent(SchuleStammdaten schule, SchuelerAdresse schueler) {
        // Check if we already have cached coordinates
        Double schoolLat = schule.getLatitude();
        Double schoolLon = schule.getLongitude();
        Double schuelerLat = schueler.getLatitude();
        Double schuelerLon = schueler.getLongitude();
        
        // If we don't have coordinates, geocode the addresses
        if (schoolLat == null || schoolLon == null) {
            String schoolAddress = formatAddressForGeocoding(
                    schule.getStrassenname(),
                    schule.getHausnummer(),
                    schule.getHausnummerZusatz(),
                    schule.getPlz(),
                    schule.getOrt()
            );
            
            Optional<GeocodeCoordinates> schoolCoords = graphHopperClient.geocodeAddress(schoolAddress);
            if (schoolCoords.isEmpty()) {
                return new DistanceResult("Could not geocode school address: " + schoolAddress);
            }
            
            schoolLat = schoolCoords.get().getLatitude();
            schoolLon = schoolCoords.get().getLongitude();
            Log.debug("School geocoded: " + schoolAddress + " -> " + schoolLat + "," + schoolLon);
        }
        
        if (schuelerLat == null || schuelerLon == null) {
            String studentAddress = formatAddressForGeocoding(
                    schueler.getStrassenname(),
                    schueler.getHausnummer(),
                    schueler.getHausnummerZusatz(),
                    schueler.getPlz(),
                    schueler.getOrt()
            );
            
            Optional<GeocodeCoordinates> studentCoords = graphHopperClient.geocodeAddress(studentAddress);
            if (studentCoords.isEmpty()) {
                return new DistanceResult("Could not geocode student address: " + studentAddress);
            }
            
            schuelerLat = studentCoords.get().getLatitude();
            schuelerLon = studentCoords.get().getLongitude();
            Log.debug("Student geocoded: " + studentAddress + " -> " + schuelerLat + "," + schuelerLon);
        }
        
        // Calculate route distance
        return graphHopperClient.calculateDistance(schoolLat, schoolLon, schuelerLat, schuelerLon);
    }
}
