# OSRM (Open Source Routing Machine) Setup Guide

## Overview

OSRM is an open-source routing engine used for calculating distances between schools and students. It's configured as an alternative to GraphHopper.

## Current Status

### Completed:
- ✅ Backend Java service updated to support both GraphHopper and OSRM routing engines
- ✅ Auto-detection of routing engine based on URL (port 5000 = OSRM, otherwise GraphHopper)
- ✅ Geocoding switched to OpenStreetMap Nominatim (free, public, no auth required)
- ✅ Docker Compose configuration prepared (`docker-compose.osrm.yml`)
- ✅ Backend Docker image built with OSRM support
- ✅ Backend environment variable updated to use `http://osrm:5000` by default

### In Progress:
- 🔄 Germany OSM data download (~740MB, currently ~243MB downloaded at slow speed)

### Next Steps:
1. Wait for OSM data download to complete
2. Start OSRM container: `docker compose -f docker-compose.yml -f docker-compose.osrm.yml up -d osrm`
3. OSRM will extract and contract the routing database (10-20 mins)
4. Test distance calculation in UI

## Architecture

### Routing Engines

#### OSRM (Current Setup)
- **URL Format**: `http://osrm:5000/route/v1/driving/{lon},{lat};{lon},{lat}`
- **Response**: JSON with `routes[0].distance` (meters) and `routes[0].duration` (seconds)
- **Coordinate Order**: **lon,lat** (important - different from typical lat,lon)
- **Data**: Germany OSM data from Geofabrik
- **Processing**: Extract → Contract → Route
- **Port**: 5000

#### GraphHopper (Alternative)
- **URL Format**: `http://localhost:8989/route?point={lat},{lon}&point={lat},{lon}&vehicle=car`
- **Response**: JSON with `paths[0].distance` and `paths[0].time` 
- **Coordinate Order**: lat,lon
- **Port**: 8989

### Geocoding

**OpenStreetMap Nominatim** (independent of routing engine)
- **URL**: `https://nominatim.openstreetmap.org/search?q={address}&format=json&country=de`
- **Response**: Array with `lat`, `lon`, `display_name`
- **No authentication required**
- **Rate limiting**: ~1 request/second recommended

## Backend Code Changes

### GraphHopperClient.java

**Auto-detection Logic**:
```java
boolean isOsrm = graphHopperUrl.contains("5000") || graphHopperUrl.contains("osrm");
if (isOsrm) {
    return calculateDistanceOsrm(...);
} else {
    return calculateDistanceGraphHopper(...);
}
```

**Key Methods**:
- `calculateDistance(lat, lon, lat, lon)` - Public API, auto-detects engine
- `calculateDistanceOsrm()` - OSRM routing with **lon,lat coordinate order**
- `calculateDistanceGraphHopper()` - GraphHopper routing
- `geocodeAddress(String)` - Nominatim-based geocoding

## Deployment

### Option 1: Using OSRM (Current Setup)

```bash
# Ensure OSM data is downloaded
ls -lh docker/osrm_data/germany-latest.osm.pbf

# Start OSRM service
docker compose -f docker-compose.yml -f docker-compose.osrm.yml up -d osrm

# Wait for health check (120s warmup, then ~10-20 mins to process data)
docker logs -f svws-osrm-router

# Once ready, the routing service starts
# Test with: curl http://localhost:5000/health
```

OSRM Container Processing:
1. Mounts local `docker/osrm_data/` directory
2. Checks if `germany-latest.osm.pbf.osrm.mld` (processed file) exists
3. If not: Extracts → Contracts → Removes source files
4. Starts `osrm-routed` server on port 5000
5. Health check verifies readiness

### Option 2: Using GraphHopper (Fallback)

```bash
# Set backend environment variable
export GRAPHHOPPER_URL=http://localhost:8989

# Start GraphHopper service separately
docker run -p 8989:8989 -d graphhopper/graphhopper:latest

# Update backend service
docker compose build backend && docker compose up -d backend
```

## Testing

### 1. Verify OSRM is Running
```bash
curl http://localhost:5000/health
# Expected: {"status":"ok"}
```

### 2. Test Distance Calculation (Direct API)
```bash
curl "http://localhost:8081/api/schulen/{schoolUuid}/distanceToStudent?studentLat=50.0&studentLon=8.0"
# Or use Entfernungsberechnung component in UI
```

### 3. UI Testing
1. Go to "Entfernungsberechnung" (Distance Calculation) component
2. Select a school from dropdown
3. Search and select a student by address
4. Click "Entfernung anzeigen" (Show Address)
5. Click "Entfernung berechnen" (Calculate Distance)
6. Should display distance in km and time in minutes

## Configuration

### Environment Variables

**Docker Compose**:
```yaml
backend:
  environment:
    GRAPHHOPPER_URL: ${GRAPHHOPPER_URL:-http://osrm:5000}
```

**Override at runtime**:
```bash
export GRAPHHOPPER_URL=http://osrm:5000
docker compose up -d
```

## Troubleshooting

### OSM Data Download Stuck
- Speed varies based on Geofabrik CDN availability: 200KB/s - 1MB/s
- Total time: 15-60 minutes depending on bandwidth
- Monitor: `tail -f docker/osrm_data/download.log`

### OSRM Container Not Starting
```bash
# Check logs
docker logs svws-osrm-router

# Verify file exists
ls -lh docker/osrm_data/germany-latest.osm.pbf

# Check if processing is stuck
docker exec svws-osrm-router ps aux | grep osrm
```

### Distance Calculation Returns Error
1. **OSRM not ready**: Wait 10-20 minutes after container start
2. **Invalid coordinates**: Ensure lat/lon are valid Germany coordinates
3. **Address geocoding failed**: Try different address format
4. **Network unreachable**: 
   - Check: `docker network ls`
   - Ensure backend and OSRM on same network
   - Verify backend environment variable: `docker exec svws-backend env | grep GRAPHHOPPER`

### Switching Between OSRM and GraphHopper
- **OSRM**: No configuration needed (auto-detected by port 5000)
- **GraphHopper**: 
  1. Stop OSRM: `docker compose -f docker-compose.yml -f docker-compose.osrm.yml down osrm`
  2. Set: `export GRAPHHOPPER_URL=http://localhost:8989`
  3. Start GraphHopper service
  4. Rebuild backend: `docker compose build backend`

## Performance Notes

### Extraction & Contraction
- **Time**: 10-20 minutes depending on system hardware
- **RAM**: ~8GB for Germany dataset
- **Storage**: ~3GB for extracted + contracted files

### Routing Requests
- **Typical latency**: 50-200ms per request
- **Rate limit**: None (just system resources)
- **Nominatim geocoding**: ~200-500ms (add 1s delay between requests)

### Distance Calculation Flow
1. **Address Geocoding** (500ms-1s): Nominatim API → lat,lon
2. **Distance Routing** (100-200ms): OSRM API → meters & seconds
3. **Total**: ~1-2 seconds per calculation

## API Endpoints

### Backend
- `GET /api/schulen/{uuid}/distanceToStudent?studentLat={lat}&studentLon={lon}`
  - Response: `{ distance: 12500, timeMinutes: 15 }`

### OSRM (Internal)
- `GET /health` - Health check
- `GET /route/v1/driving/{lon},{lat};{lon},{lat}` - Routing

### Nominatim (External)
- `GET https://nominatim.openstreetmap.org/search?q={address}&format=json&country=de` - Geocoding

## Future Improvements

1. **Caching**: Cache geocoding and distance calculation results
2. **Batch Requests**: Support bulk distance calculations
3. **Alternative Engines**: Support Vroom (optimization) or Mapbox GL
4. **Offline Mode**: Pre-cache common routes
5. **Load Balancing**: Multiple OSRM instances for high load

## References

- [OSRM Documentation](http://project-osrm.org/)
- [OpenStreetMap Nominatim](https://nominatim.org/)
- [Geofabrik OSM Downloads](https://download.geofabrik.de/)
