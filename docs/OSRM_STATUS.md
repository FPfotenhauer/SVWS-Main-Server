# OSRM Distance Calculation - Deployment Status

## ✅ Completed Tasks

### Backend Implementation
- ✅ **GraphHopperClient.java** - Updated with full OSRM support
  - Auto-detects routing engine (port 5000 = OSRM, else GraphHopper)
  - `calculateDistanceOsrm()` - OSRM routing (lon,lat order)
  - `calculateDistanceGraphHopper()` - GraphHopper routing
  - `geocodeAddress()` - Nominatim geocoding (public, free, no auth)

### Frontend Implementation  
- ✅ **EntfernungsberechnungAdresse.vue** - Vue 3 component with proper TypeScript
  - Address search with Nominatim API
  - Distance calculation via distance endpoint
  - Proper composition API imports
  - Display distance in km and duration in minutes

### Docker Configuration
- ✅ **docker-compose.osrm.yml** - OSRM service configuration
  - Mounts local `docker/osrm_data/` directory
  - Auto-extracts and contracts Germany OSM data
  - Runs OSRM server on port 5000
  - Health checks enabled

- ✅ **docker-compose.yml** - Backend updated
  - Default `GRAPHHOPPER_URL=http://osrm:5000`
  - Compatible with both OSRM and GraphHopper

- ✅ **Backend Docker** - Image built and ready
  - Compiled with OSRM/GraphHopper support
  - Ready to deploy

### Documentation
- ✅ **OSRM_SETUP.md** - Comprehensive setup guide
- ✅ **GRAPHHOPPER_SETUP.md** (existing) - Alternative GraphHopper setup

## 🔄 In Progress

### OSM Data Download
- **Status**: Download in progress (`wget` running in background)
- **Progress**: Getting Germany OSM data from Geofabrik
- **File**: `docker/osrm_data/germany-latest.osm.pbf` (~740MB)
- **Current Speed**: 200-400 KB/s (variable)
- **Estimated Time**: 30-60 minutes remaining
- **Monitor Command**: `tail -f /home/pfotenhauer/git/SVWS-Main-Server/docker/osrm_data/download.log`

## 📋 Ready for Testing (Once OSM Download Complete)

### Step 1: Verify Download Complete
```bash
ls -lh docker/osrm_data/germany-latest.osm.pbf
# Should show file size ~740MB
```

### Step 2: Start OSRM Service
```bash
cd /home/pfotenhauer/git/SVWS-Main-Server
docker compose -f docker-compose.yml -f docker-compose.osrm.yml up -d osrm
```

### Step 3: Monitor OSRM Startup
```bash
docker logs -f svws-osrm-router
# Watch for:
# - "Processing Germany OSM data..."
# - "Starting OSRM server on port 5000..."
# - "route started" (means it's ready)
# Takes 10-20 minutes for data processing
```

### Step 4: Verify OSRM is Ready
```bash
# Check health endpoint
curl http://localhost:5000/health
# Expected: HTTP 200 or {"status":"ok"}

# Test routing
curl "http://localhost:5000/route/v1/driving/8.6753,50.1109;8.6763,50.1119?overview=false"
# Cologne coordinates as example
```

### Step 5: Verify Backend Configuration
```bash
# Check backend environment
docker exec svws-backend env | grep GRAPHHOPPER
# Should see: GRAPHHOPPER_URL=http://osrm:5000
```

### Step 6: Start All Services
```bash
docker compose up -d
# Starts: database, backend, frontend, reverse-proxy
# (OSRM already running)
```

### Step 7: Test in UI
1. Open http://localhost:8081
2. Navigate to "Entfernungsberechnung" (Distance Calculation)
3. Select a school
4. Search for a student (or enter address)
5. Click "Entfernung berechnen" (Calculate Distance)
6. Should display distance in km and time in minutes

## 🔧 Current Configuration

### Environment Variables

**docker-compose.yml**:
```yaml
backend:
  environment:
    GRAPHHOPPER_URL: http://osrm:5000
```

**To Override** (before docker compose up):
```bash
export GRAPHHOPPER_URL=http://osrm:5000  # OSRM
export GRAPHHOPPER_URL=http://localhost:8989  # GraphHopper
```

### Routing Engine Detection

**Automatic in GraphHopperClient.java**:
```java
boolean isOsrm = url.contains("5000") || url.contains("osrm");
```

- Port 5000 or "osrm" in URL → Use OSRM API format
- Otherwise → Use GraphHopper API format

## 📊 System Architecture

```
┌─────────────────────────────────────────┐
│  Frontend (Vue 3 + TypeScript)          │
│  - EntfernungsberechnungAdresse.vue     │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Backend (Quarkus + Java)               │
│  - GraphHopperClient                    │
│  - Distance Calculation REST API        │
└────────────────┬────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
   ┌─────────┐      ┌──────────────┐
   │ Nominatim│     │ OSRM Server  │
   │(Geocoding)│     │(Routing)     │
   └─────────┘      └──────────────┘
        👆                  👆
    External            Docker
    (Free API)          (Port 5000)
```

## 🚀 Quick Start Checklist

Once OSM data download completes:

- [ ] Verify file exists: `ls -lh docker/osrm_data/germany-latest.osm.pbf`
- [ ] Start OSRM: `docker compose -f docker-compose.yml -f docker-compose.osrm.yml up -d osrm`
- [ ] Wait for processing (check logs): `docker logs -f svws-osrm-router`
- [ ] Test health: `curl http://localhost:5000/health`
- [ ] Start other services: `docker compose up -d`
- [ ] Test in UI: http://localhost:8081

## 📝 Files Modified in This Session

- `docker-compose.yml` - Updated GRAPHHOPPER_URL default
- `docker-compose.osrm.yml` - New OSRM service configuration
- `backend/src/main/java/.../GraphHopperClient.java` - OSRM support with auto-detection
- `frontend/src/components/EntfernungsberechnungAdresse.vue` - Fixed Vue 3 imports
- `docs/OSRM_SETUP.md` - New comprehensive documentation
- `docs/OSRM_STATUS.md` - This file

## ⚠️ Important Notes

1. **OSRM Initialization**: Takes 10-20 minutes after starting container (depends on CPU/disk)
2. **Geocoding**: Nominatim is free but has rate limits (~1 req/sec recommended)
3. **Coordinate Order**: OSRM uses **lon,lat** (opposite of GraphHopper) - auto-handled
4. **Network**: OSRM must be accessible at `http://osrm:5000` from backend container

## 🔄 Fallback Options

If OSRM setup has issues:
1. Use GraphHopper (original setup) - requires manual GraphHopper image
2. Disable distance calculation temporarily
3. Check OSRM_SETUP.md for troubleshooting

---

**Status Updated**: 2024-02-22 17:25 UTC
**Next Action**: Monitor OSM download completion, then follow Quick Start Checklist
