# GraphHopper Integration & Distance Calculation - Implementation Summary

## Completion Status: ✅ Phase 1 Complete

This document summarizes the implementation of GraphHopper integration and the Entfernungsberechnung (Distance Calculation) feature for the SVWS Main Server application.

## What Was Implemented

### 1. Docker Infrastructure
- ✅ Updated `docker-compose.yml` to make GraphHopper optional
- ✅ Created `docker-compose.graphhopper.yml` for optional GraphHopper service
- ✅ Configured environment variable `GRAPHHOPPER_URL` for flexible service endpoint configuration
- ✅ All core services (backend, frontend, database, nginx) properly configured and running

### 2. Backend Java Implementation

#### DTOs (Data Transfer Objects)
- ✅ `GeocodeCoordinates.java` - Represents latitude/longitude from geocoding
- ✅ `DistanceResult.java` - Contains distance (km, meters) and time (minutes, milliseconds) results
- ✅ `SchuelerAdresse.java` - Student address data suitable for distance calculation
- ✅ Updated `SchuelerStammdaten.java` - Converted from record to class to support latitude/longitude fields
- ✅ Updated `SchuleStammdaten.java` - Converted from record to class to support latitude/longitude fields

#### Services
- ✅ `GraphHopperClient.java` - HTTP client for GraphHopper APIs
  - `isHealthy()` - Health check
  - `geocodeAddress(String)` - Address → Coordinates
  - `calculateDistance(lat, lon, lat, lon)` - Route calculation
  
- ✅ `EntfernungsberechnungService.java` - Business logic for distance calculation
  - `formatAddressForGeocoding()` - Address formatting
  - `calculateDistanceForStudent()` - Main distance calculation orchestration

- ✅ Updated `SchuleService.java`
  - `getSchuelerAuswahlliste()` - Fetch student list from SVWS with filtering
  - `getSchuelerStammdaten()` - Fetch enriched student address data
  - `getSchuleStammdatenById()` - New method for fetching school data by ID

#### REST API
- ✅ Updated `SchuleResource.java` to include distance calculation endpoint
  - `GET /api/schulen/{id}/schueler/{schuelerId}/entfernung` - Distance calculation endpoint
  - Injected `EntfernungsberechnungService` for business logic
  - Proper error handling and response serialization

### 3. Frontend Vue.js Implementation

#### TypeScript Types
- ✅ Updated `types/schueler.ts` to include `DistanceResult` interface

#### Components
- ✅ Updated `EntfernungsberechnungAdresse.vue`
  - Added "Entfernung berechnen" (Calculate Distance) button
  - Added distance calculation logic with loading state
  - Added distance result display with formatted output
  - Error handling with user-friendly messages
  - Styling for action section and result display

- ✅ Updated `App.vue`
  - Pass `schoolId` prop to address component
  - Proper routing and state management for distance calculation flow

### 4. Documentation

#### Setup Documentation
- ✅ `docs/GRAPHHOPPER_SETUP.md` - Comprehensive setup guide including:
  - Architecture overview
  - Local setup options (GraphHopper, OSRM, Nominatim)
  - Configuration details
  - API endpoint documentation
  - Troubleshooting guide
  - Performance optimization strategies
  - Future enhancement ideas

#### Feature Documentation
- ✅ `docs/ENTFERNUNGSBERECHNUNG.md` - User and technical documentation including:
  - Feature overview
  - User workflow guide
  - Technical architecture
  - Data model specification
  - Error handling strategies
  - Development guidelines
  - Testing checklist
  - Deployment instructions
  - Performance considerations

#### Setup Script
- ✅ `docker/scripts/init-graphhopper.sh` - GraphHopper initialization script

## Architecture Diagram

```
Frontend (Vue 3)
    ↓
EntfernungsberechnungAdresse.vue [Calculate Distance Button]
    ↓
axios HTTP Client
    ↓
GET /api/schulen/{id}/schueler/{studentId}/entfernung
    ↓
Backend (Quarkus)
    ↓
SchuleResource.java [REST Endpoint]
    ↓
EntfernungsberechnungService.java [Business Logic]
    ↓
SchuleService.java [Data Retrieval]
    ├─ Get School Address
    └─ Get Student Address (with PLZ/Ort enrichment)
    ↓
GraphHopperClient.java [Geocoding & Routing]
    ├─ Geocode School Address → Coordinates
    ├─ Geocode Student Address → Coordinates
    └─ Calculate Route Distance & Time
    ↓
GraphHopper Service (Docker)
    ├─ Geocoding API (address → lat/lon)
    └─ Routing API (coordinates → distance/time)
    ↓
Response: DistanceResult (distance_km, time_minutes)
    ↓
Frontend: Display Result or Error Message
```

## Key Classes & Methods

### GraphHopperClient.java
```java
public boolean isHealthy()
public Optional<GeocodeCoordinates> geocodeAddress(String address)
public DistanceResult calculateDistance(Double startLat, Double startLon, 
                                       Double endLat, Double endLon)
```

### EntfernungsberechnungService.java
```java
public String formatAddressForGeocoding(String strassenname, String hausnummer,
                                        String hausnummerZusatz, String plz, String ort)
public DistanceResult calculateDistanceForStudent(SchuleStammdaten schule, 
                                                  SchuelerAdresse schueler)
```

### SchuleResource.java
```java
@GET
@Path("{id}/schueler/{schuelerId}/entfernung")
public DistanceResult getDistanceToStudent(@PathParam("id") UUID id, 
                                           @PathParam("schuelerId") Long schuelerId)
```

## Data Flow Example

### User Action: Calculate Distance

1. **Frontend**: User clicks "Entfernung berechnen"
2. **API Call**: `GET /api/schulen/550e8400-e29b/0000/schueler/12/entfernung`
3. **Backend Processing**:
   - Fetch school: "Gymnasium Berlin, Hauptstraße 10, 10115 Berlin"
   - Fetch student: "Müller, Max, Ringstraße 5, 12345 Potsdam"
   - Geocode school → (52.5200, 13.4050)  # Berlin
   - Geocode student → (52.3907, 13.0645)  # Potsdam
   - Route: Berlin → Potsdam
   - Result: 39.5 km, 45 minutes (by car)
4. **Frontend Display**: "Entfernung: 39.5 km, Fahrtzeit: 45 min"

## Error Handling Examples

### Missing Address Data
```json
{
  "error": "Could not geocode school address: "
}
```

### GraphHopper Unavailable
```json
{
  "error": "Distance calculation failed: Connection refused"
}
```

### No Route Found
```json
{
  "error": "No route found"
}
```

## Current State & Testing

### ✅ Verified Working
- Backend compilation: `mvn clean compile` ✓
- Docker build: All services build successfully ✓
- Service startup: All containers run without errors ✓
- REST endpoint structure: Properly configured ✓
- Error handling: Graceful error responses ✓
- Frontend component: TypeScript compiles without errors ✓

### ⚠️ Requires GraphHopper Service
- Actual distance calculation requires GraphHopper running
- Frontend will show error if service unavailable
- Optional: Can use public APIs (Nominatim + OSRM)

## Files Created/Modified

### New Files
```
backend/src/main/java/de/schultraeger/application/EntfernungsberechnungService.java
backend/src/main/java/de/schultraeger/application/dto/GeocodeCoordinates.java
backend/src/main/java/de/schultraeger/application/dto/DistanceResult.java
backend/src/main/java/de/schultraeger/application/dto/SchuelerAdresse.java
backend/src/main/java/de/schultraeger/infrastructure/routing/GraphHopperClient.java
frontend/src/types/schueler.ts (updated with DistanceResult)
docker-compose.graphhopper.yml
docker/scripts/init-graphhopper.sh
docs/GRAPHHOPPER_SETUP.md
docs/ENTFERNUNGSBERECHNUNG.md
```

### Modified Files
```
docker-compose.yml (GraphHopper config & GRAPHHOPPER_URL env var)
backend/src/main/java/de/schultraeger/api/SchuleResource.java
backend/src/main/java/de/schultraeger/application/SchuleService.java
backend/src/main/java/de/schultraeger/application/dto/SchuleStammdaten.java
backend/src/main/java/de/schultraeger/application/dto/SchuelerStammdaten.java
frontend/src/components/EntfernungsberechnungAdresse.vue
frontend/src/App.vue
```

## Next Steps (Phase 2)

### High Priority
1. **Set up GraphHopper Docker container**
   - Build custom image with Germany OSM data
   - Or use OSRM as alternative
   
2. **End-to-End Testing**
   - Test with actual SVWS school/student data
   - Verify distance results are reasonable

3. **Performance Optimization**
   - Implement coordinate caching
   - Monitor geocoding/routing latency

### Medium Priority
4. **Batch Distance Calculation**
   - POST endpoint for calculating distances for multiple students
   - Useful for class reports

5. **Map Visualization**
   - Display route on interactive map
   - Show polyline/directions

6. **Database Persistence**
   - Cache coordinates in database
   - Reduce repeated geocoding

### Lower Priority
7. **Multiple Route Types**
   - Public transit, bicycle, pedestrian routes
   - Compare travel times

8. **Export Functionality**
   - Distance reports/tables
   - Integration with school reports

## Configuration Quick Start

### Local Development (Without GraphHopper)
```bash
# Start services
docker compose up -d

# Access at http://localhost:8081
# Distance calculation will fail (service unavailable)
# This is expected - GraphHopper is optional
```

### With GraphHopper (Once Available)
```bash
# Build custom GraphHopper image first
# Then start with:
docker compose -f docker-compose.yml -f docker-compose.graphhopper.yml up -d

# Set environment variable:
export GRAPHHOPPER_URL=http://graphhopper:8989
```

### With Public APIs (Alternative)
```bash
# No additional configuration needed
# Falls back to environment variable defaults
# May have rate limits
```

## Summary

The GraphHopper integration and distance calculation feature has been fully implemented in Phase 1:

✅ **Backend**: Complete Java service for distance calculation  
✅ **Frontend**: Vue component with distance calculation UI  
✅ **Architecture**: Properly structured with separation of concerns  
✅ **Error Handling**: Graceful failures with user-friendly messages  
✅ **Documentation**: Comprehensive setup and usage guides  
✅ **Docker**: Flexible configuration for optional GraphHopper service  

The system is ready for testing once GraphHopper (or alternative routing service) is properly configured. All code compiles, services run, and the feature is fully integrated with the existing student search and address lookup functionality.

---

**Implementation Date**: 2024  
**Status**: Phase 1 Complete - Ready for GraphHopper Integration  
**Next Review**: After GraphHopper service is deployed
