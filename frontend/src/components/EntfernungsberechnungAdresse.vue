<template>
  <section class="distance-address-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Entfernungsberechnung</p>
        <h2>Adressdaten</h2>
        <p class="subtitle">Adressdaten der ausgewählten Schüler:in.</p>
      </div>
      <button class="ghost" type="button" @click="emit('back')">Zurück zur Schülerliste</button>
    </header>

    <div class="school-info" v-if="school">
      <h3>Schule</h3>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">Schulnummer</span>
          <span class="value">{{ school.schulnummer ?? '–' }}</span>
        </div>
        <div class="info-item wide">
          <span class="label">Schulname</span>
          <span class="value">{{ school.bezeichnung1 ?? '–' }}</span>
        </div>
      </div>
    </div>

    <div class="address-card" v-if="adresse">
      <h3>Adressdaten</h3>
      <div class="info-grid">
        <div class="info-item wide">
          <span class="label">Name</span>
          <span class="value">{{ adresse.nachname ?? '–' }}, {{ adresse.vorname ?? '–' }}</span>
        </div>
        <div class="info-item">
          <span class="label">Geburtsdatum</span>
          <span class="value">{{ formatDate(adresse.geburtsdatum) }}</span>
        </div>
        <div class="info-item wide">
          <span class="label">Straße</span>
          <span class="value">
            {{ adresse.strassenname ?? '–' }}
            {{ adresse.hausnummer ?? '' }}
            {{ adresse.hausnummerZusatz ?? '' }}
          </span>
        </div>
        <div class="info-item">
          <span class="label">PLZ</span>
          <span class="value">{{ adresse.plz ?? '–' }}</span>
        </div>
        <div class="info-item">
          <span class="label">Ort</span>
          <span class="value">{{ adresse.ort ?? '–' }}</span>
        </div>
      </div>

      <div class="action-section">
        <button 
          class="primary" 
          type="button" 
          @click="calculateDistance"
          :disabled="isCalculating"
        >
          {{ isCalculating ? 'Berechne Entfernung...' : 'Entfernung berechnen' }}
        </button>
      </div>

      <div class="distance-result" v-if="distanceResult">
        <div v-if="distanceResult.error" class="error-message">
          <strong>Fehler:</strong> {{ distanceResult.error }}
        </div>
        <div v-else class="result-content">
          <div class="result-card">
            <div class="result-mode">🚗 Auto</div>
            <div class="result-item">
              <span class="label">Entfernung</span>
              <span class="value distance">{{ distanceResult.distance_km?.toFixed(1) ?? '–' }} km</span>
            </div>
            <div class="result-item">
              <span class="label">Fahrtzeit</span>
              <span class="value time">{{ formatTime(distanceResult.time_minutes) }}</span>
            </div>
          </div>
          
          <div class="result-card">
            <div class="result-mode">🚴 Fahrrad</div>
            <div class="result-item">
              <span class="label">Entfernung</span>
              <span class="value distance">{{ distanceResult.bike_distance_km?.toFixed(1) ?? '–' }} km</span>
            </div>
            <div class="result-item">
              <span class="label">Fahrtzeit</span>
              <span class="value time">{{ formatTime(distanceResult.bike_time_minutes) }}</span>
            </div>
          </div>
          
          <div class="result-card">
            <div class="result-mode">🚶 Zu Fuß</div>
            <div class="result-item">
              <span class="label">Entfernung</span>
              <span class="value distance">{{ distanceResult.foot_distance_km?.toFixed(1) ?? '–' }} km</span>
            </div>
            <div class="result-item">
              <span class="label">Gehzeit</span>
              <span class="value time">{{ formatTime(distanceResult.foot_time_minutes) }}</span>
            </div>
          </div>
        </div>

        <div v-if="hasRouteOnMap" class="map-section">
          <h4>Route auf Karte (OSM)</h4>
          <div ref="mapContainer" class="route-map"></div>
        </div>

        <div v-else-if="!distanceResult.error" class="map-hint">
          Für diese Berechnung wurden keine Routendaten für die Kartenanzeige geliefert.
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, shallowRef, watch } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { SchuleStammdatenResponse } from '../types/schule';
import type { SchuelerAdresse } from '../types/schueler';
import type { DistanceResult } from '../types/schueler';
import api from '../services/api';

interface Props {
  school: SchuleStammdatenResponse | null;
  adresse: SchuelerAdresse | null;
  schoolId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  schoolId: ''
});

const emit = defineEmits<{
  back: [];
}>();

const isCalculating = ref(false);
const distanceResult = ref<DistanceResult | null>(null);
const mapContainer = ref<HTMLDivElement | null>(null);
const mapInstance = shallowRef<L.Map | null>(null);
const routeLayer = shallowRef<L.Polyline | null>(null);
const markerLayer = shallowRef<L.LayerGroup | null>(null);

const hasRouteOnMap = computed(() => Boolean(distanceResult.value?.polyline));

const formatDate = (value?: string | null) => {
  if (!value) {
    return '–';
  }
  return value.length >= 10 ? value.slice(0, 10) : value;
};

const formatTime = (minutes?: number | null) => {
  if (!minutes) {
    return '–';
  }
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours > 0) {
    return `${hours} h ${mins} min`;
  }
  return `${mins} min`;
};

const decodePolyline = (encoded: string): L.LatLngTuple[] => {
  const points: L.LatLngTuple[] = [];
  let index = 0;
  let lat = 0;
  let lng = 0;

  while (index < encoded.length) {
    let shift = 0;
    let result = 0;
    let byte: number;

    do {
      byte = encoded.charCodeAt(index++) - 63;
      result |= (byte & 0x1f) << shift;
      shift += 5;
    } while (byte >= 0x20);

    const latitudeChange = (result & 1) ? ~(result >> 1) : (result >> 1);
    lat += latitudeChange;

    shift = 0;
    result = 0;

    do {
      byte = encoded.charCodeAt(index++) - 63;
      result |= (byte & 0x1f) << shift;
      shift += 5;
    } while (byte >= 0x20);

    const longitudeChange = (result & 1) ? ~(result >> 1) : (result >> 1);
    lng += longitudeChange;

    points.push([lat / 1e5, lng / 1e5]);
  }

  return points;
};

const ensureMap = () => {
  if (mapInstance.value || !mapContainer.value) {
    return;
  }

  mapInstance.value = L.map(mapContainer.value, {
    zoomControl: true,
    scrollWheelZoom: true
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap-Mitwirkende',
    maxZoom: 19
  }).addTo(mapInstance.value);
};

const clearRouteLayers = () => {
  if (routeLayer.value) {
    routeLayer.value.remove();
    routeLayer.value = null;
  }
  if (markerLayer.value) {
    markerLayer.value.remove();
    markerLayer.value = null;
  }
};

const renderRouteOnMap = async (polyline?: string | null) => {
  if (!polyline) {
    clearRouteLayers();
    return;
  }

  await nextTick();
  ensureMap();

  if (!mapInstance.value) {
    return;
  }

  const coordinates = decodePolyline(polyline);
  if (coordinates.length < 2) {
    clearRouteLayers();
    return;
  }

  clearRouteLayers();

  routeLayer.value = L.polyline(coordinates, {
    color: '#3b82f6',
    weight: 5,
    opacity: 0.9
  }).addTo(mapInstance.value);

  const start = coordinates[0];
  const end = coordinates[coordinates.length - 1];
  markerLayer.value = L.layerGroup([
    L.circleMarker(start, { radius: 6, color: '#10b981', fillColor: '#10b981', fillOpacity: 1 }),
    L.circleMarker(end, { radius: 6, color: '#ef4444', fillColor: '#ef4444', fillOpacity: 1 })
  ]).addTo(mapInstance.value);

  mapInstance.value.fitBounds(routeLayer.value.getBounds(), {
    padding: [20, 20]
  });
};

watch(
  () => distanceResult.value?.polyline,
  (polyline) => {
    renderRouteOnMap(polyline);
  }
);

onBeforeUnmount(() => {
  mapInstance.value?.remove();
  mapInstance.value = null;
});

const calculateDistance = async () => {
  if (!props.schoolId || !props.adresse?.id) {
    distanceResult.value = { error: 'Schule oder Schüler:in nicht ausgewählt' };
    return;
  }

  isCalculating.value = true;
  distanceResult.value = null;

  try {
    const response = await api.get<DistanceResult>(
      `/api/schulen/${props.schoolId}/schueler/${props.adresse.id}/entfernung`
    );
    distanceResult.value = response.data;
  } catch (error) {
    console.error('Distance calculation failed:', error);
    distanceResult.value = { 
      error: 'Entfernungsberechnung fehlgeschlagen. Bitte versuchen Sie es später erneut.'
    };
  } finally {
    isCalculating.value = false;
  }
};
</script>

<style scoped>
.distance-address-page {
  width: 100%;
  padding: 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.page-header {
  background: linear-gradient(135deg, #0f172a, #1e293b);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 18px;
  padding: 0.7rem 1.2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.65rem;
  margin: 0 0 0.05rem 0;
  color: #38bdf8;
}

h2 {
  margin: 0.1rem 0;
  font-size: 1.5rem;
  color: #fff;
}

.subtitle {
  margin: 0;
  color: rgba(226, 232, 240, 0.8);
  max-width: 560px;
  line-height: 1.3;
  font-size: 0.9rem;
}

.ghost {
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: rgba(15, 23, 42, 0.8);
  color: #e2e8f0;
  padding: 0.65rem 1.25rem;
  border-radius: 999px;
  font-weight: 600;
}

.school-info,
.address-card {
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  padding: 1rem 1.25rem;
}

h3 {
  margin: 0 0 0.8rem 0;
  color: #f8fafc;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
}

.info-item {
  background: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 10px;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item.wide {
  grid-column: span 2;
}

.label {
  color: #94a3b8;
  font-size: 0.8rem;
}

.value {
  color: #e2e8f0;
  font-weight: 600;
}

.action-section {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.primary {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.7rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.primary:hover:not(:disabled) {
  background: #2563eb;
}

.primary:disabled {
  background: #6b7280;
  cursor: not-allowed;
  opacity: 0.7;
}

.distance-result {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
}

.error-message {
  color: #ef4444;
  padding: 0.75rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 6px;
}

.result-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.result-card {
  background: #1e293b;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px;
  padding: 1rem;
}

.result-mode {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: #f8fafc;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item .label {
  color: #94a3b8;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.result-item .value {
  color: #e2e8f0;
  font-size: 1.1rem;
  font-weight: bold;
}

.result-item .value.distance {
  color: #10b981;
}

.result-item .value.time {
  color: #f59e0b;
}

.map-section {
  margin-top: 1rem;
}

h4 {
  margin: 0 0 0.5rem 0;
  color: #f8fafc;
}

.route-map {
  width: 100%;
  height: 320px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.map-hint {
  margin-top: 1rem;
  color: #94a3b8;
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .info-item.wide {
    grid-column: span 1;
  }
}
</style>
