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
    </div>
  </section>
</template>

<script setup lang="ts">
import type { SchuleStammdatenResponse } from '../types/schule';
import type { SchuelerAdresse } from '../types/schueler';

defineProps<{
  school: SchuleStammdatenResponse | null;
  adresse: SchuelerAdresse | null;
}>();

const emit = defineEmits<{
  back: [];
}>();

const formatDate = (value?: string | null) => {
  if (!value) {
    return '–';
  }
  return value.length >= 10 ? value.slice(0, 10) : value;
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

@media (max-width: 640px) {
  .info-item.wide {
    grid-column: span 1;
  }
}
</style>
