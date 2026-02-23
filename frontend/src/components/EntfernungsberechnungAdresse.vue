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
          <div class="result-item">
            <span class="label">Entfernung</span>
            <span class="value distance">{{ distanceResult.distance_km?.toFixed(1) ?? '–' }} km</span>
          </div>
          <div class="result-item">
            <span class="label">Fahrtzeit (Auto)</span>
            <span class="value time">{{ formatTime(distanceResult.time_minutes) }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
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
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.result-item .label {
  color: #94a3b8;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.result-item .value {
  color: #e2e8f0;
  font-size: 1.25rem;
  font-weight: bold;
}

.result-item .value.distance {
  color: #10b981;
}

.result-item .value.time {
  color: #f59e0b;
}

@media (max-width: 640px) {
  .info-item.wide {
    grid-column: span 1;
  }
}
</style>
