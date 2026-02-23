# Entfernungsberechnung (Distance Calculation) Feature

## Overview

The Entfernungsberechnung (Distance Calculation) feature allows users to calculate the distance and travel time between a school and a student's home address using GraphHopper routing engine.

## Features

- **School Selection**: Browse and select an available school
- **Student Search**: Find students by name, first name, birth date, or status
- **Sorting & Pagination**: Sort students by multiple columns with 20 items per page
- **Address Lookup**: View complete address details including resolved postal codes and cities
- **Distance Calculation**: One-click distance calculation with results in kilometers and minutes
- **Error Handling**: Graceful handling of missing addresses or routing failures

## User Workflow

### Step 1: Select School
1. Navigate to "Entfernungsberechnung" in the main menu
2. Click on a school to begin the distance calculation

### Step 2: Search for Student
1. View the student list with options to sort
2. Use search/filter fields to narrow results:
   - **Nachname** (Last Name)
   - **Vorname** (First Name)
   - **Geburtsdatum** (Birth Date)
   - **Status** (Student Status)
3. Sort by clicking column headers (ascending/descending)
4. Navigate between pages using "Zurück" (Previous) or "Weiter" (Next) buttons

### Step 3: View Address
1. Click "Adresse anzeigen" (Show Address) button next to a student
2. Review the student's address details:
   - Full name and birth date
   - Street, house number, postal code
   - City (resolved from postal code catalog if needed)

### Step 4: Calculate Distance
1. Click "Entfernung berechnen" (Calculate Distance) button
2. Wait for calculation (typically a few seconds)
3. View the result:
   - **Entfernung** (Distance) in kilometers
   - **Fahrtzeit** (Travel Time) in hours and minutes (by car)

## Technical Architecture

### Frontend Components

**EntfernungsberechnungSchule.vue** (Student Search Page)
- Displays school information
- Student list with search filters
- Sorting by 4 columns (Nachname, Vorname, Geburtsdatum, Status)
- Pagination (20 students per page)
- "Adresse anzeigen" button to navigate to address page

**EntfernungsberechnungAdresse.vue** (Address & Distance Page)
- Displays school and student address details
- "Entfernung berechnen" button
- Shows distance result (km) or error message
- Back button to return to student list

### Backend Services

**SchuleService.java**
- `getSchuelerAuswahlliste(UUID schuleId)`: Fetches student selection list from SVWS
- `getSchuelerStammdaten(UUID schuleId, Long schuelerId)`: Retrieves student address with PLZ/Ort enrichment
- `getSchuleStammdatenById(UUID schuleId)`: Gets school master data

**EntfernungsberechnungService.java**
- `formatAddressForGeocoding()`: Formats address strings for geocoding API
- `calculateDistanceForStudent()`: Main business logic for distance calculation
  - Geocodes school address if needed
  - Geocodes student address if needed
  - Calls GraphHopper routing API
  - Returns distance in km and time in minutes

**GraphHopperClient.java**
- `isHealthy()`: Health check for GraphHopper service
- `geocodeAddress()`: Converts address string to coordinates (lat/lon)
- `calculateDistance()`: Routes between two coordinates and returns distance/time

### REST API Endpoints

**Fetch School & Students**
```
GET /api/schulen/{id}/schueler/auswahlliste
```
Returns list of students for a school with status filtering.

**Fetch Student Address**
```
GET /api/schulen/{id}/schueler/{schuelerId}/stammdaten
```
Returns student address data with PLZ/Ort enrichment.

**Calculate Distance**
```
GET /api/schulen/{id}/schueler/{schuelerId}/entfernung
```
Returns distance in kilometers and travel time in minutes.

## Data Model

### SchuelerAdresse
```typescript
{
  id: number;
  nachname?: string;
  vorname?: string;
  geburtsdatum?: string;
  strassenname?: string;
  hausnummer?: string;
  hausnummerZusatz?: string;
  plz?: string;
  ort?: string;
}
```

### DistanceResult
```typescript
{
  distance_meters?: number;
  distance_km?: number;
  time_milliseconds?: number;
  time_minutes?: number;
  polyline?: string;  // Optional: encoded path
  error?: string;     // Error message if calculation failed
}
```

## Configuration

### Environment Variables

```properties
# GraphHopper service URL (internal Docker network)
GRAPHHOPPER_URL=http://graphhopper:8989

# For development/testing with public APIs:
GRAPHHOPPER_URL=http://localhost:8989
```

### Backend Configuration (application.properties)
No specific GraphHopper configuration needed - uses environment variable.

## Error Handling

The system implements graceful error handling:

1. **Missing Address Data**
   - Message: "Could not geocode school/student address"
   - Cause: Incomplete address in SVWS database
   - Solution: Verify address data in school administration

2. **Route Not Found**
   - Message: "No route found"
   - Cause: No valid driving route exists (e.g., island, restricted area)
   - Solution: Manual distance calculation or use alternative routing method

3. **GraphHopper Unavailable**
   - Message: "Distance calculation failed"
   - Cause: GraphHopper service not running or unreachable
   - Solution: Start GraphHopper service or check network configuration

4. **Geocoding Failure**
   - Message: Specific error from GraphHopper API
   - Cause: Address format not recognized or not in database
   - Solution: Verify address format matches German postal address standards

## Development

### Adding New Features

1. **Caching**: Implement coordinate caching to reduce geocoding calls
   ```java
   // Add cache entry to SchuelerStammdaten after geocoding
   student.setLatitude(coordinates.getLatitude());
   student.setLongitude(coordinates.getLongitude());
   ```

2. **Batch Calculation**: Calculate distances for all students at once
   ```java
   POST /api/schulen/{id}/schueler/entfernungen
   [studentId1, studentId2, studentId3, ...]
   ```

3. **Modal Display**: Show result in modal instead of page
   - Update EntfernungsberechnungAdresse to emit result event
   - Show result modal in parent component

4. **Map Visualization**: Display route on map
   - Add Leaflet or Mapbox integration
   - Draw polyline on map using route from GraphHopper

### Testing

**Unit Tests**:
- Test address formatting
- Test distance calculation with mock GraphHopper responses
- Test error handling

**Integration Tests**:
- Test full flow from school selection to distance result
- Test with sample SVWS data

**Manual Testing Checklist**:
- [ ] Select school
- [ ] Search students
- [ ] Sort by each column
- [ ] Navigate pagination
- [ ] View address details
- [ ] Calculate distance
- [ ] Test with incomplete address
- [ ] Test error scenarios

## Performance Considerations

### Current Implementation
- Geocoding happens on-demand when calculating distance
- No caching of coordinates or results

### Optimization Opportunities

1. **Database-Level Caching** (Recommended)
   ```sql
   ALTER TABLE schueler_stammdaten ADD COLUMN latitude DECIMAL(10,8);
   ALTER TABLE schueler_stammdaten ADD COLUMN longitude DECIMAL(10,8);
   ALTER TABLE schueler_stammdaten ADD COLUMN geocoded_at TIMESTAMP;
   ```

2. **Redis Caching** (For high volume)
   - Cache distance results with TTL (e.g., 24 hours)
   - Cache geocoding results for common addresses

3. **Batch Processing** (For reports)
   - Calculate distances for multiple students in one request
   - Use GraphHopper matrix API if available

### Current Performance

- **Geocoding**: ~500-1000ms per address (first time)
- **Routing**: ~200-500ms per route calculation
- **Total**: ~1-2 seconds per distance calculation

With caching:
- **Cached Geocoding**: ~50-100ms
- **Cached Routing**: ~100-200ms
- **Total**: ~100-300ms

## Deployment

### Docker Deployment

#### Option 1: Without GraphHopper (development)
```bash
docker compose up -d
# Falls back to GRAPHHOPPER_URL environment variable
```

#### Option 2: With GraphHopper (production)
```bash
docker compose -f docker-compose.yml -f docker-compose.graphhopper.yml up -d
```

### Health Checks

The application polls GraphHopper health endpoint:
```bash
curl http://localhost:8989/health
```

If GraphHopper is unavailable, distance calculation will fail gracefully with error message.

## Troubleshooting

### Distance Calculation Returns Error

1. **Check GraphHopper service**:
   ```bash
   docker ps | grep graphhopper
   curl http://localhost:8989/health
   ```

2. **Check address data**:
   - Verify student address has all fields (strassenname, hausnummer, plz, ort)
   - Check if address is valid German address

3. **Check logs**:
   ```bash
   docker logs svws-main-server-backend-1
   docker logs svws-main-server-graphhopper-1
   ```

4. **Check network**:
   - Verify backend can reach GraphHopper on internal network
   - If using localhost, verify port 8989 is accessible

### Slow Distance Calculation

1. **First-time geocoding**: Geocoding takes 500-1000ms per address
2. **Large student list**: Consider implementing batch processing
3. **GraphHopper performance**: May be slow with large Germany OSM dataset
   - Consider using smaller regional dataset if only one Bundesland

## Support & References

- GraphHopper API: https://docs.graphhopper.com/
- SVWS Integration: See `COPILOT_INSTRUCTION.md`
- Setup Guide: See `docs/GRAPHHOPPER_SETUP.md`
