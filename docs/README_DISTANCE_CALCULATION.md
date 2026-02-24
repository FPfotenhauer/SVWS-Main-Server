# Distance Calculation (Entfernungsberechnung)

The distance calculation feature allows the system to determine the driving distance and travel time between a school and a student's address. It supports two routing engines: **OSRM** and **GraphHopper**, and uses **OpenStreetMap Nominatim** for geocoding.

## Features
- **Address Geocoding**: Converts street addresses to coordinates (lat/lon) via Nominatim.
- **Routing**: Calculates distance (km) and travel time (minutes) for driving routes.
- **Engine Auto-Detection**: Backend automatically detects the engine based on the `GRAPHHOPPER_URL`.
- **UI Integration**: Searchable student list and distance calculator.

## Architecture
1. **Frontend**: Vue 3 components trigger calculations via REST API.
2. **Backend**: Quarkus service orchestrates data retrieval and external routing calls.
3. **Routing Services**:
   - **Nominatim (External)**: Public API for geocoding.
   - **OSRM / GraphHopper (Local Docker)**: Local services for routing.

## Setup & Deployment

The distance calculation requires a routing service running in Docker.

### Option 1: OSRM (Recommended, Default)
Efficient for large datasets like Germany.
```bash
# Start OSRM Service
docker compose -f docker-compose.yml -f docker-compose.osrm.yml up -d osrm
```
*Note: The first start takes 10-20 minutes to process OSM data. Monitor with `docker logs -f svws-osrm-router`.*

### Option 2: GraphHopper (Alternative)
```bash
# Start GraphHopper Service
docker compose -f docker-compose.yml -f docker-compose.graphhopper.yml up -d
```

## Configuration

Control the engine via the `GRAPHHOPPER_URL` environment variable:

| Engine | Port | Default URL |
|--------|------|-------------|
| **OSRM** | 5000 | `http://osrm:5000` |
| **GraphHopper** | 8989 | `http://graphhopper:8989` |

**Detection Logic**: If the URL contains `5000` or `osrm`, OSRM is used; otherwise, it defaults to GraphHopper.

## Troubleshooting
- **"Service Unavailable"**: Check if the container is running and healthy:
  - OSRM: `curl http://localhost:5000/health`
  - GraphHopper: `curl http://localhost:8989/health`
- **Geocoding Failures**: Ensure the address is complete (Street, Number, PLZ, City).
- **Initialization**: Routing will fail until the engine has finished processing its data maps.

## API Endpoints
- `GET /api/schulen/{id}/schueler/{studentId}/entfernung`: Calculate distance for a student.

## Usage
1. Navigate to **Entfernungsberechnung** in the main menu.
2. Select a **School**.
3. Search for a student and click **Adresse anzeigen** (Show Address).
4. Click **Entfernung berechnen** (Calculate Distance).
5. The result (km and minutes) will appear above the button.

## Requirements
- **Disk Space**: ~3GB (for OSRM data processing).
- **RAM**: ~8GB recommended for OSRM initialization.

### Data Requirement (OSRM)
The OSRM engine requires Germany OSM data in the `docker/osrm_data/` directory.
Download URL: [https://download.geofabrik.de/europe/germany-latest.osm.pbf](https://download.geofabrik.de/europe/germany-latest.osm.pbf)
