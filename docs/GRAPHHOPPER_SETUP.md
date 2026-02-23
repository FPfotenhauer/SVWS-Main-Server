# GraphHopper Integration Guide

## Overview

This project includes optional GraphHopper integration for distance calculation between schools and student addresses. The system can calculate route distance, travel time, and can optionally geocode addresses to coordinates.

## Architecture

### Distance Calculation Flow

1. **Frontend**: User clicks "Entfernung berechnen" button on student address page
2. **Backend API**: `GET /api/schulen/{schoolId}/schueler/{studentId}/entfernung`
3. **EntfernungsberechnungService**: 
   - Retrieves school and student address data
   - Geocodes addresses to coordinates (lat/lon) if not already cached
   - Calls GraphHopper Routing API
4. **GraphHopperClient**:
   - Geocoding: address string → lat/lon coordinates
   - Routing: calculates route distance and time between two coordinates
5. **Frontend**: Displays result (distance in km, travel time in minutes)

## Local Setup Options

### Option 1: Using GraphHopper (Recommended for Production)

GraphHopper is a routing engine that can be run locally. However, the public Docker image is not freely available.

**Building from Source**:
```bash
# Clone GraphHopper repository
git clone https://github.com/graphhopper/graphhopper.git
cd graphhopper

# Build Docker image with Germany OSM data
docker build -t graphhopper-de:latest .

# Or use an existing custom image if available
```

**Running Locally**:
```bash
# Use the provided docker-compose file
docker compose -f docker-compose.yml -f docker-compose.graphhopper.yml up -d

# Verify health
curl http://localhost:8989/health
```

### Option 2: Using Nominatim + OSRM (Alternative)

For development without a local GraphHopper instance:

**Nominatim** (Geocoding - address to coordinates):
```bash
docker run -d --name nominatim -p 8888:80 \
  -e PBF_URL=https://download.geofabrik.de/europe/germany-latest.osm.pbf \
  mediagis/nominatim:latest
```

**OSRM** (Routing - distance/time calculation):
```bash
docker run -d --name osrm -p 5000:5000 \
  -v /path/to/germany.osrm:/data/germany.osrm \
  osrm/osrm-backend:v5.24.0 osrm-routed /data/germany.osrm
```

### Option 3: Using Free Online APIs

For development/testing without local services:

- **Geocoding**: OpenStreetMap Nominatim API (free, rate-limited)
- **Routing**: OSRM Public API (free, may have rate limits)

Update `GRAPHHOPPER_URL` environment variable to point to public endpoints.

## Configuration

### Environment Variables

```properties
# Backend service configuration
GRAPHHOPPER_URL=http://graphhopper:8989  # Internal Docker network
# Or for local development:
GRAPHHOPPER_URL=http://localhost:8989
# Or for public API:
GRAPHHOPPER_URL=https://graphhopper.com/api/1/route  # with API key
```

### Docker Compose Setup

**Main docker-compose.yml**: Contains core services (database, backend, frontend, nginx)

**docker-compose.graphhopper.yml**: Optional override file for GraphHopper service
```bash
# Start with GraphHopper
docker compose -f docker-compose.yml -f docker-compose.graphhopper.yml up -d

# Start without GraphHopper (falls back to environment variable)
docker compose up -d
```

## API Endpoints

### Distance Calculation

**Endpoint**: `GET /api/schulen/{schoolId}/schueler/{studentId}/entfernung`

**Response**:
```json
{
  "distance_meters": 12500,
  "distance_km": 12.5,
  "time_milliseconds": 900000,
  "time_minutes": 15,
  "polyline": "encoded_polyline_string",
  "error": null
}
```

**Error Response**:
```json
{
  "error": "Could not geocode school address: ..."
}
```

## Implementation Details

### GraphHopperClient.java

Handles HTTP calls to GraphHopper service:

- `isHealthy()`: Checks if service is available
- `geocodeAddress(address)`: Converts address string to coordinates
- `calculateDistance(startLat, startLon, endLat, endLon)`: Routes between two points

### EntfernungsberechnungService.java

Business logic for distance calculation:

- Caches coordinates to avoid re-geocoding
- Formats addresses for geocoding
- Calls GraphHopperClient for routing
- Handles error cases

### Frontend Component: EntfernungsberechnungAdresse.vue

- Displays school and student address
- "Entfernung berechnen" button triggers calculation
- Shows distance result and travel time on success
- Shows error message on failure

## Testing

### Manual Testing

1. Navigate to Entfernungsberechnung (Distance Calculation)
2. Select a school
3. Search for and select a student
4. Click "Adresse anzeigen" (Show Address)
5. Click "Entfernung berechnen" (Calculate Distance)
6. Verify result displays distance in km and time in minutes

### Unit Testing

```java
// SchuleServiceTest includes test cases for distance calculation
@Test
void testDistanceCalculation() {
    // Test data with known addresses
    SchuleStammdaten school = ...
    SchuelerAdresse student = ...
    
    DistanceResult result = entfernungsberechnungService
        .calculateDistanceForStudent(school, student);
    
    assertNotNull(result.getDistanceKm());
    assertNull(result.getError());
}
```

## Troubleshooting

### GraphHopper Service Not Available

**Symptom**: Error messages in distance calculation

**Solution**:
1. Check if GraphHopper container is running: `docker ps | grep graphhopper`
2. Verify it's healthy: `curl http://localhost:8989/health`
3. Check logs: `docker logs <graphhopper_container_id>`
4. Ensure `GRAPHHOPPER_URL` environment variable is correct

### Geocoding Fails for Addresses

**Possible Causes**:
- Address format incorrect (must include street, number, postal code, city)
- Address not found in OSM database
- GraphHopper Geocoding API rate limited

**Solution**:
- Verify address data in SVWS database is complete
- Check if coordinates can be cached after first successful geocoding
- Implement fallback to public Nominatim service

### Route Not Found

**Symptom**: "No route found" error

**Possible Causes**:
- No valid road route between school and student (e.g., island/private area)
- Incorrect coordinates after geocoding
- GraphHopper data doesn't include all roads

**Solution**:
- Use routing with more flexible options (pedestrian, bicycle)
- Implement fallback to straight-line distance (haversine formula)
- Check coordinate accuracy in distance calculation logs

## Performance Optimization

### Caching Strategy

**Coordinates**:
```java
// Store in SchuelerStammdaten.latitude/longitude
// Avoid re-geocoding same addresses
```

**Distance Results**:
Consider implementing cache for school-student distance pairs:
```java
// ConcurrentHashMap<"schoolId_studentId", DistanceResult>
// With TTL to refresh periodically
```

### Batch Operations

For calculating distances for multiple students:
```java
// Not yet implemented - future enhancement
// POST /api/schulen/{schoolId}/schueler/entfernungen
// with array of student IDs to get batch distances
```

## Future Enhancements

1. **Caching Layer**: Redis/Memcached for distance results
2. **Batch Calculation**: Calculate distances for all students in a class
3. **Multiple Route Options**: Car, public transit, bicycle, pedestrian
4. **Map Visualization**: Display route on map using Leaflet/Mapbox
5. **Historical Analysis**: Track distance changes over time
6. **Export Reports**: Generate distance reports for schools

## References

- GraphHopper Documentation: https://docs.graphhopper.com/
- OpenStreetMap Nominatim: https://nominatim.org/
- OSRM (Open Source Routing Machine): http://project-osrm.org/
