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

    <div class="search-bar">
      <input 
        v-model="searchQuery" 
        type="text" 
        placeholder="Nach Schulnummer, Schulform oder Name suchen …"
        class="search-input"
      />
      <span class="search-results">
        {{ filteredItems.length }} von {{ items.length }} Schulen
      </span>
    </div>

    <div v-if="loading" class="loading-state">
      <span class="pulse"></span>
      <p>Daten werden aus den SVWS-Stammdaten geladen …</p>
    </div>

    <div v-else>
      <div v-if="error" class="error-state">
        <strong>Fehler:</strong> {{ error }}
      </div>

      <div v-if="!paginatedItems.length && !error" class="empty-state">
        <p>
          {{ searchQuery ? 'Keine Schulen gefunden.' : 'Keine Stammdaten verfügbar. Bitte legen Sie die Anmeldedaten zu mindestens einer Schule über den Bereich "Verwaltete Schulen" an.' }}
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
            <tr v-for="item in paginatedItems" :key="item.id + '-' + item.schema">
              <td>{{ item.schulnummer ?? '–' }}</td>
              <td>{{ item.schulform ?? '–' }}</td>
              <td>
                <span v-if="item.bezeichnung1">{{ item.bezeichnung1 }}</span>
                <span v-else class="muted">Schulname fehlt</span>
              </td>
              <td class="status-cell">
                <button class="details-btn" title="Details anzeigen" @click="openModal(item)">Details</button>
                <span :class="statusClass(item)">
                  {{ item.error ? 'Fehler' : 'Ok' }}
                </span>
                <p v-if="item.error" class="status-note">{{ item.error }}</p>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="totalPages > 1" class="pagination">
          <button 
            :disabled="currentPage === 1" 
            @click="currentPage--"
            class="pagination-btn"
          >
            Zurück
          </button>
          
          <span class="pagination-info">
            Seite {{ currentPage }} von {{ totalPages }}
          </span>
          
          <button 
            :disabled="currentPage === totalPages" 
            @click="currentPage++"
            class="pagination-btn"
          >
            Weiter
          </button>
        </div>
      </div>
    </div>
  </section>

  <div v-if="showModal && selectedItem" class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>Schule Details</h3>
        <button class="modal-close" @click="closeModal" title="Schließen">&times;</button>
      </div>
      
      <div class="modal-body">
        <div class="field-row">
          <div class="field-group">
            <label>Schulnummer</label>
            <div class="field-value">{{ selectedItem.schulnummer ? selectedItem.schulnummer : '–' }}</div>
          </div>
          
          <div class="field-group">
            <label>Schulform</label>
            <div class="field-value">{{ selectedItem.schulform ? selectedItem.schulform : '–' }}</div>
          </div>
          
          <div class="field-group">
            <label>Schulname</label>
            <div class="field-value">{{ selectedItem.bezeichnung1 ? selectedItem.bezeichnung1 : 'nicht vorhanden' }}</div>
          </div>
        </div>
        
        <div v-if="selectedItem.error" class="field-group">
          <label>Fehler</label>
          <div class="field-value error">{{ selectedItem.error }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import api from '../services/api';
import type { SchuleStammdatenResponse } from '../types/schule';

const items = ref<SchuleStammdatenResponse[]>([]);
const loading = ref(false);
const error = ref('');
const searchQuery = ref('');
const currentPage = ref(1);
const itemsPerPage = ref(10);
const showModal = ref(false);
const selectedItem = ref<SchuleStammdatenResponse | null>(null);

const filteredItems = computed(() => {
  if (!searchQuery.value) return items.value;
  
  const query = searchQuery.value.toLowerCase();
  return items.value.filter(item => 
    (item.schulnummer?.toString().includes(query)) ||
    (item.schulform?.toLowerCase().includes(query)) ||
    (item.bezeichnung1?.toLowerCase().includes(query))
  );
});

const totalPages = computed(() => {
  return Math.ceil(filteredItems.value.length / itemsPerPage.value);
});

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredItems.value.slice(start, end);
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

const statusClass = (item: SchuleStammdatenResponse) => {
  return item.error ? 'status-badge error' : 'status-badge success';
};

const openModal = (item: SchuleStammdatenResponse) => {
  selectedItem.value = item;
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  selectedItem.value = null;
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
  padding: 0.7rem 1.2rem;
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
}

.overview-header h2 {
  margin: 0.1rem 0;
  font-size: 1.5rem;
  color: #fff;
}

.overview-header .subtitle {
  margin: 0;
  color: rgba(226, 232, 240, 0.8);
  max-width: 560px;
  line-height: 1.3;
  font-size: 0.9rem;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.65rem;
  margin: 0 0 0.05rem 0;
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

.search-bar {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 0.75rem;
}

.search-input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  color: #e2e8f0;
  font-size: 0.9rem;
  outline: none;
  transition: border 0.2s ease;
}

.search-input:focus {
  border-color: #38bdf8;
}

.search-input::placeholder {
  color: rgba(148, 163, 184, 0.6);
}

.search-results {
  white-space: nowrap;
  color: rgba(226, 232, 240, 0.8);
  font-size: 0.8rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  margin-top: 0.75rem;
}

.pagination-btn {
  background: rgba(59, 130, 246, 0.9);
  border: 1px solid #3b82f6;
  color: #fff;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: #3b82f6;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: rgba(226, 232, 240, 0.8);
  font-size: 0.85rem;
  min-width: 120px;
  text-align: center;
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: linear-gradient(135deg, #0f172a, #1e293b);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  width: 95%;
  max-width: 1200px;
  height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #fff;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.75rem;
  color: rgba(226, 232, 240, 0.8);
  cursor: pointer;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.modal-close:hover {
  color: #fff;
}

.modal-body {
  padding: 1.5rem 1.5rem;
  flex: 1;
  overflow-y: auto;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr 2fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.field-group {
  margin-bottom: 1rem;
}

.field-group:last-child {
  margin-bottom: 0;
}

.field-group label {
  display: block;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(226, 232, 240, 0.6);
  margin-bottom: 0.25rem;
}

.field-value {
  color: #e2e8f0;
  font-size: 0.95rem;
  padding: 0.5rem 0.75rem;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 6px;
  word-break: break-word;
}

.field-value.error {
  background: rgba(248, 113, 113, 0.1);
  color: #fca5a5;
  border-color: rgba(248, 113, 113, 0.3);
}

@media (max-width: 1024px) {
  .field-row {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>