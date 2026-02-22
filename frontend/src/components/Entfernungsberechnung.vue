<template>
  <section class="entfernungsberechnung">
    <header class="overview-header">
      <div>
        <p class="eyebrow">Entfernungsberechnung</p>
        <h2>Schule auswählen</h2>
        <p class="subtitle">
          Bitte wählen Sie eine Schule aus. Die Liste enthält alle Schulen aller angebundenen Server.
        </p>
      </div>
      <button class="ghost" type="button" @click="loadStammdaten" :disabled="loading">
        {{ loading ? 'Aktualisiere …' : 'Aktualisieren' }}
      </button>
    </header>

    <div class="search-bar">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Nach Schulnummer, Schulform oder Name suchen …"
        class="search-input"
      />
      <span class="search-results">{{ filteredItems.length }} von {{ items.length }} Schulen</span>
    </div>

    <div v-if="loading" class="loading-state">
      <span class="pulse"></span>
      <p>Daten werden geladen …</p>
    </div>

    <div v-else>
      <div v-if="error" class="error-state">
        <strong>Fehler:</strong> {{ error }}
      </div>

      <div v-if="!paginatedItems.length && !error" class="empty-state">
        <p>
          {{ searchQuery ? 'Keine Schulen gefunden.' : 'Keine Stammdaten verfügbar. Bitte hinterlegen Sie zuerst Schuldaten im Bereich "Verwaltete Schulen".' }}
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
              <th>Aktion</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedItems" :key="item.id + '-' + item.schema">
              <td>{{ item.schulnummer ?? '–' }}</td>
              <td>{{ item.schulform ?? '–' }}</td>
              <td>
                <span v-if="item.bezeichnung1">{{ item.bezeichnung1 }}</span>
                <span v-else class="muted">Schulname fehlt</span>
              </td>
              <td>
                <span :class="statusClass(item)">
                  {{ item.error ? 'Fehler' : 'Ok' }}
                </span>
              </td>
              <td>
                <button class="select-btn" type="button" @click="selectSchool(item)">Auswählen</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="totalPages > 1" class="pagination">
          <button :disabled="currentPage === 1" @click="currentPage--" class="pagination-btn">Zurück</button>
          <span class="pagination-info">Seite {{ currentPage }} von {{ totalPages }}</span>
          <button :disabled="currentPage === totalPages" @click="currentPage++" class="pagination-btn">Weiter</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import api from '../services/api';
import type { SchuleStammdatenResponse } from '../types/schule';

const emit = defineEmits<{
  selectSchool: [school: SchuleStammdatenResponse];
}>();

const items = ref<SchuleStammdatenResponse[]>([]);
const loading = ref(false);
const error = ref('');
const searchQuery = ref('');
const currentPage = ref(1);
const itemsPerPage = ref(10);

const filteredItems = computed(() => {
  if (!searchQuery.value) return items.value;

  const query = searchQuery.value.toLowerCase();
  return items.value.filter((item) =>
    item.schulnummer?.toString().includes(query)
    || item.schulform?.toLowerCase().includes(query)
    || item.bezeichnung1?.toLowerCase().includes(query)
  );
});

const totalPages = computed(() => Math.ceil(filteredItems.value.length / itemsPerPage.value));

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  return filteredItems.value.slice(start, start + itemsPerPage.value);
});

const loadStammdaten = async () => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    const response = await api.get<SchuleStammdatenResponse[]>('/api/schulen/stammdaten');
    items.value = response.data;
    currentPage.value = 1;
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Fehler beim Laden der Stammdaten';
  } finally {
    loading.value = false;
  }
};

const statusClass = (item: SchuleStammdatenResponse) => (item.error ? 'status-badge error' : 'status-badge success');

const selectSchool = (school: SchuleStammdatenResponse) => {
  emit('selectSchool', school);
};

onMounted(loadStammdaten);
</script>

<style scoped>
.entfernungsberechnung {
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
  padding: 0.7rem 1.2rem;
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
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

.ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.search-bar {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.search-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  background: #0f172a;
  color: #e2e8f0;
  font-size: 0.9rem;
}

.search-input:focus {
  outline: none;
  border-color: #38bdf8;
}

.search-results {
  color: #94a3b8;
  font-size: 0.9rem;
  white-space: nowrap;
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

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
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
  padding: 0.9rem 1rem;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

th {
  background: #111827;
  color: #cbd5e1;
}

td {
  color: #e2e8f0;
}

.muted {
  color: #94a3b8;
}

.status-badge {
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
  padding: 0.2rem 0.6rem;
}

.status-badge.success {
  color: #34d399;
  background: rgba(52, 211, 153, 0.12);
}

.status-badge.error {
  color: #fb7185;
  background: rgba(244, 63, 94, 0.12);
}

.select-btn {
  border: none;
  background: #f97316;
  color: #111827;
  padding: 0.4rem 0.85rem;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
}

.pagination-btn {
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: #111827;
  color: #e2e8f0;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: #94a3b8;
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
