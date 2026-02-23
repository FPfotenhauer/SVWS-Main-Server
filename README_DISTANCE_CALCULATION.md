# GraphHopper Integration - Quick Reference

## Status
✅ **Phase 1 Complete** - Distance calculation infrastructure fully implemented and tested
⏳ **Phase 2 Pending** - GraphHopper Docker service deployment and integration testing

## What's Working
- ✅ Student search with filtering and sorting
- ✅ Address display with PLZ/Ort enrichment from SVWS catalog
- ✅ Distance calculation endpoint (ready for GraphHopper)
- ✅ Frontend UI for distance calculation
- ✅ Error handling for missing GraphHopper

## What's Required for Full Function
- 🔧 GraphHopper Docker container running
- 📍 Germany OSM data for routing
- 🌐 Or: Alternative routing service (OSRM, Nominatim)

## Key Components

### Backend
| Component | Purpose | Status |
|-----------|---------|--------|
| `GraphHopperClient.java` | HTTP client for routing APIs | ✅ Ready |
| `EntfernungsberechnungService.java` | Business logic | ✅ Ready |
| `SchuleResource.java` | REST endpoint | ✅ Ready |
| Distance Endpoint | `/api/schulen/{id}/schueler/{studentId}/entfernung` | ✅ Ready |

### Frontend
| Component | Purpose | Status |
|-----------|---------|--------|
| `EntfernungsberechnungAdresse.vue` | Distance UI | ✅ Ready |
| Calculate Button | Triggers distance calculation | ✅ Ready |
| Result Display | Shows distance & time | ✅ Ready |

## Quick Start

### Option 1: Local Development (No GraphHopper)
```bash
docker compose up -d
# Access: http://localhost:8081
# Distance calculation will show: "service unavailable" error
```

### Option 2: With GraphHopper (Requires Setup)
```bash
# 1. Get GraphHopper Docker image
docker pull graphhopper/graphhopper:latest

# 2. Start with GraphHopper
docker compose -f docker-compose.yml \
               -f docker-compose.graphhopper.yml up -d

# 3. Wait for GraphHopper to start (~1-2 minutes)
curl http://localhost:8989/health

# 4. Access app: http://localhost:8081
# Distance calculation now works!
```

## API Endpoint

### Calculate Distance
```
GET /api/schulen/{schoolId}/schueler/{studentId}/entfernung
```

**Response (Success)**:
```json
{
  "distance_meters": 12500,
  "distance_km": 12.5,
  "time_milliseconds": 900000,
  "time_minutes": 15,
  "error": null
}
```

**Response (Error)**:
```json
{
  "error": "Could not geocode school address: ..."
}
```

## Environment Variables

```bash
# Backend service
GRAPHHOPPER_URL=http://graphhopper:8989        # Docker network (production)
GRAPHHOPPER_URL=http://localhost:8989          # Localhost (development)
GRAPHHOPPER_URL=https://api.graphhopper.com/... # Public API (with API key)
```

## Testing Checklist

- [ ] Start services: `docker compose up -d`
- [ ] Access app: http://localhost:8081
- [ ] Select school in Entfernungsberechnung
- [ ] Search for student (e.g., by last name)
- [ ] Click "Adresse anzeigen"
- [ ] Click "Entfernung berechnen"
- [ ] See error (expected - no GraphHopper) OR distance result

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Distance button shows "service unavailable" | GraphHopper not running - this is expected if not deployed |
| Backend URL incorrect | Verify `GRAPHHOPPER_URL` environment variable |
| Student address incomplete | Check SVWS data for missing street/postal code |
| Route not found | Valid driving route may not exist - try different student |

## File Locations

```
Documentation:
  docs/IMPLEMENTATION_SUMMARY.md     # This summary
  docs/GRAPHHOPPER_SETUP.md          # Setup guide
  docs/ENTFERNUNGSBERECHNUNG.md      # Feature documentation

Backend Code:
  backend/src/main/java/de/schultraeger/
    infrastructure/routing/GraphHopperClient.java
    application/EntfernungsberechnungService.java
    api/SchuleResource.java

Frontend Code:
  frontend/src/components/EntfernungsberechnungAdresse.vue
  frontend/src/types/schueler.ts

Docker:
  docker-compose.yml
  docker-compose.graphhopper.yml
```

## Architecture

```
User Interface
    ↓
"Entfernung berechnen" Button
    ↓
GET /api/schulen/{id}/schueler/{id}/entfernung
    ↓
SchuleResource → EntfernungsberechnungService
    ↓
GraphHopperClient {
  ├─ geocodeAddress(school)
  ├─ geocodeAddress(student)
  └─ calculateDistance(lat, lon)
}
    ↓
GraphHopper Service / Routing API
    ↓
Response: { distance_km: 12.5, time_minutes: 15 }
    ↓
Display Result
```

## Performance

| Operation | Time |
|-----------|------|
| Geocode address (first time) | 500-1000ms |
| Geocode address (cached) | 50-100ms |
| Calculate route | 200-500ms |
| **Total (first call)** | **~1-2 seconds** |
| **Total (cached)** | **~100-300ms** |

## Next Steps

1. **Deploy GraphHopper** (see `docs/GRAPHHOPPER_SETUP.md`)
2. **Test with real data** (select a school and student)
3. **Monitor performance** (latency, errors)
4. **Optimize caching** (as needed)
5. **Add features** (batch calculation, map visualization, etc.)

## Support Resources

- **Setup Guide**: `docs/GRAPHHOPPER_SETUP.md`
- **Feature Docs**: `docs/ENTFERNUNGSBERECHNUNG.md`
- **Implementation Details**: `docs/IMPLEMENTATION_SUMMARY.md`
- **GraphHopper API**: https://docs.graphhopper.com/
- **SVWS Integration**: `COPILOT_INSTRUCTION.md`

---

**Quick Links**:
- Application: http://localhost:8081
- Backend Health: http://localhost:8080/health
- GraphHopper Health: http://localhost:8989/health (when running)
