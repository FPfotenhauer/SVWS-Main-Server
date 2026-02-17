<template>
  <section class="schuelerzahlen">
    <header class="overview-header">
      <div>
        <p class="eyebrow">Schülerzahlen</p>
        <h2>Schulnummern &amp; Kurznamen</h2>
        <p class="subtitle">
          Die Tabelle liest die Stammdaten aus dem jeweiligen SVWS-Schema. Nur Schulen mit hinterlegten
          API-Zugangsdaten liefern eine vollständige Zeile.
        </p>
      </div>
      <button class="ghost" type="button" @click="loadStammdaten" :disabled="loading">
        {{ loading ? 'Aktualisiere …' : 'Aktualisieren' }}
      </button>
    </header>

    <div v-if="loading" class="loading-state">
      <span class="pulse"></span>
      <p>Daten werden aus den SVWS-Stammdaten geladen …</p>
    </div>

    <div v-else>
      <div v-if="error" class="error-state">
        <strong>Fehler:</strong> {{ error }}
      </div>

      <div v-if="!items.length && !error" class="empty-state">
        <p>
          Keine Stammdaten verfügbar. Bitte legen Sie die Anmeldedaten zu mindestens einer Schule über den
          Bereich "Verwaltete Schulen" an.
        </p>
      </div>

      <div v-else class="table-shell">
        <table>
          <thead>
            <tr>
              <th>Schulnummer</th>
              <th>Schulform</th>
              <th>Schulname</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.id + '-' + item.schema">
              <td>{{ item.schulnummer ?? '–' }}</td>
              <td>{{ item.schulform ?? '–' }}</td>
              <td>
                <span v-if="item.bezeichnung1">{{ item.bezeichnung1 }}</span>
                <span v-else class="muted">Schulname fehlt</span>
              </td>
              <td class="status-cell">
                <button class="details-btn" title="Details anzeigen">Details</button>
                <span :class="statusClass(item)">
                  {{ item.error ? 'Fehler' : 'Ok' }}
                </span>
                <p v-if="item.error" class="status-note">{{ item.error }}</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '../services/api';
import type { SchuleStammdatenResponse } from '../types/schule';

const items = ref<SchuleStammdatenResponse[]>([]);
const loading = ref(false);
const error = ref('');

const loadStammdaten = async () => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    const response = await api.get<SchuleStammdatenResponse[]>('/api/schulen/stammdaten');
    items.value = response.data;
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Fehler beim Laden der Stammdaten';
  } finally {
    loading.value = false;
  }
};

const statusClass = (item: SchuleStammdatenResponse) => {
  return item.error ? 'status-badge error' : 'status-badge success';
};

onMounted(loadStammdaten);
</script>

<style scoped>
.schuelerzahlen {
  width: 100%;
  padding: 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.overview-header {
  background: linear-gradient(135deg, #0f172a, #1e293b);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 18px;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.overview-header h2 {
  margin: 0.25rem 0;
  font-size: 1.9rem;
  color: #fff;
}

.overview-header .subtitle {
  margin: 0;
  color: rgba(226, 232, 240, 0.8);
  max-width: 560px;
  line-height: 1.5;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.75rem;
  margin: 0;
  color: #38bdf8;
}

.ghost {
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: rgba(15, 23, 42, 0.8);
  color: #e2e8f0;
  padding: 0.65rem 1.25rem;
  border-radius: 999px;
  font-weight: 600;
  transition: border 0.2s ease;
}

.ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-state,
.error-state,
.empty-state {
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  background: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.loading-state .pulse {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #38bdf8;
  animation: pulse 1.2s infinite;
}

.table-shell {
  overflow-x: auto;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}

th,
td {
  padding: 0.25rem 0.5rem;
  text-align: left;
}

th:nth-child(1),
td:nth-child(1) {
  width: 12%;
}

th:nth-child(2),
td:nth-child(2) {
  width: 10%;
}

th:nth-child(3),
td:nth-child(3) {
  width: 30%;
}

th:nth-child(4),
td:nth-child(4) {
  text-align: right;
}

th {
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-size: 0.75rem;
  color: rgba(226, 232, 240, 0.8);
  border-bottom: 1px solid rgba(148, 163, 184, 0.3);
}

tbody tr {
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

tbody tr:last-child {
  border-bottom: none;
}

.muted {
  color: rgba(148, 163, 184, 0.8);
}

.status-badge {
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  color: #fff;
  display: inline-flex;
  align-items: center;
}

.status-badge.success {
  background: #10b981;
}

.status-badge.error {
  background: #f87171;
}

.status-note {
  margin: 0.1rem 0 0;
  font-size: 0.75rem;
  color: #fbbf24;
}

.details-btn {
  background: rgba(59, 130, 246, 0.9);
  border: 1px solid #3b82f6;
  color: #fff;
  padding: 0.3rem 0.65rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
  margin-right: 0.5rem;
}

.details-btn:hover {
  background: #3b82f6;
}

.status-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .overview-header {
    flex-direction: column;
    align-items: flex-start;
  }

  th,
  td {
    padding: 0.2rem 0.3rem;
  }
}
</style>