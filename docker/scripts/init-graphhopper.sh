#!/bin/bash

# Initialize GraphHopper with Germany OSM data
# This script downloads the Germany OSM file and configures GraphHopper for routing

set -e

GRAPHHOPPER_DATA_DIR="${GRAPH_DIR:-/graphhopper/data}"
GERMANY_OSM_URL="https://download.geofabrik.de/europe/germany-latest.osm.pbf"
GERMANY_OSM_FILE="$GRAPHHOPPER_DATA_DIR/germany-latest.osm.pbf"

echo "GraphHopper Initialization Script"
echo "=================================="
echo "Data directory: $GRAPHHOPPER_DATA_DIR"

# Create data directory if it doesn't exist
mkdir -p "$GRAPHHOPPER_DATA_DIR"

# Check if OSM file already exists
if [ -f "$GERMANY_OSM_FILE" ]; then
    echo "Germany OSM data already exists: $GERMANY_OSM_FILE"
else
    echo "Downloading Germany OSM data from Geofabrik..."
    wget -q --show-progress -O "$GERMANY_OSM_FILE" "$GERMANY_OSM_URL"
    echo "Download complete"
fi

# Check if routing graph already built
if [ -d "$GRAPHHOPPER_DATA_DIR/germany-latest" ]; then
    echo "Routing graph already built"
else
    echo "Building routing graph from OSM data..."
    echo "This may take several minutes..."
    
    # GraphHopper will automatically build the graph on first request
    # But we can pre-build it using the API
    # For now, let GraphHopper build it on startup
fi

echo "GraphHopper initialization complete"
