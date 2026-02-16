<template>
  <div class="schulkatalog-container">
    <div class="schulkatalog-controls">
      <div class="search-bar">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Schulname, Ort, Kreis oder Schulnummer eingeben..."
          @keyup.enter="performSearch"
          class="search-input"
        />
        <button @click="performSearch" class="search-button">
          Suchen
        </button>
        <button @click="resetSearch" class="reset-button">
          Zurücksetzen
        </button>
        <button 
          @click="refreshData"
          :disabled="loading"
          class="refresh-button"
          title="Schulkatalog aktualisieren"
        >
          Aktualisieren
        </button>
        <button 
          v-if="totalSchools === 0 && !loading"
          @click="loadData"
          class="load-data-button"
          title="Laden Sie den Schulkatalog von NRW"
        >
          Katalog laden
        </button>
      </div>

      <div class="catalog-info">
        <span v-if="totalSchools">Insgesamt {{ totalSchools }} Schulen | Seite {{ currentPage + 1 }} von {{ totalPages }}</span>
        <span v-if="loading">Lädt...</span>
      </div>
    </div>

    <div class="schulkatalog-content">
      <div v-if="loading" class="loading">
        <p>Wird geladen...</p>
      </div>

      <div v-else-if="error" class="error-message">
        <strong>Fehler:</strong> {{ error }}
        <button @click="retryLoad" class="retry-button">Erneut versuchen</button>
      </div>

      <div v-else-if="schools.length === 0" class="no-results">
        <p>Keine Schulen gefunden.</p>
      </div>

      <div v-else class="schools-table-wrapper">
        <table class="schools-table">
          <thead>
            <tr>
              <th @click="handleSort('schulnummer')" class="sortable-header">Schulnr {{ sortBy === 'schulnummer' ? (sortDirection === 'asc' ? '▲' : '▼') : '' }}</th>
              <th @click="handleSort('schulname')" class="sortable-header">Bezeichnung {{ sortBy === 'schulname' ? (sortDirection === 'asc' ? '▲' : '▼') : '' }}</th>
              <th @click="handleSort('plz')" class="sortable-header">PLZ {{ sortBy === 'plz' ? (sortDirection === 'asc' ? '▲' : '▼') : '' }}</th>
              <th @click="handleSort('ort')" class="sortable-header">Ort {{ sortBy === 'ort' ? (sortDirection === 'asc' ? '▲' : '▼') : '' }}</th>
              <th @click="handleSort('schultyp')" class="sortable-header">Schultyp {{ sortBy === 'schultyp' ? (sortDirection === 'asc' ? '▲' : '▼') : '' }}</th>
              <th>Status</th>
              <th>Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="school in sortedSchools" :key="school.id" class="school-row">
              <td class="schulnummer">{{ school.schulnummer }}</td>
              <td class="schulname">{{ school.schulname }}</td>
              <td class="plz">{{ school.plz }}</td>
              <td class="ort">{{ school.ort }}</td>
              <td class="schultyp">{{ school.schultyp }}</td>
              <td class="status-cell">
                <span
                  class="status-dot"
                  :class="getStatusClass(school.aufloesung)"
                  :title="getStatusTooltip(school.aufloesung)"
                ></span>
              </td>
              <td class="aktionen">
                <button @click="showDetails(school)" class="details-button">Details</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="schools.length > 0" class="pagination">
        <button
          @click="previousPage"
          :disabled="currentPage === 0"
          class="pagination-button"
        >
          Zurück
        </button>

        <span class="page-info">
          Seite {{ currentPage + 1 }} von {{ totalPages }}
        </span>

        <button
          @click="nextPage"
          :disabled="currentPage >= totalPages - 1"
          class="pagination-button"
        >
          Weiter
        </button>
      </div>
    </div>

    <!-- Details Modal -->
    <div v-if="selectedSchool" class="modal-overlay" @click="selectedSchool = null">
      <div class="modal-content" @click.stop>
        <button class="close-button" @click="selectedSchool = null">✕</button>
        <h2>{{ selectedSchool.schulname }}</h2>

        <div class="amtsbez-block">
          <div class="amtsbez-line" v-if="selectedSchool.amtsbez1">{{ selectedSchool.amtsbez1 }}</div>
          <div class="amtsbez-line" v-if="selectedSchool.amtsbez2">{{ selectedSchool.amtsbez2 }}</div>
          <div class="amtsbez-line" v-if="selectedSchool.amtsbez3">{{ selectedSchool.amtsbez3 }}</div>
        </div>
        
        <div class="detail-grid">
          <div class="detail-item">
            <label>Schulnummer:</label>
            <span>{{ selectedSchool.schulnummer }}</span>
          </div>
          <div class="detail-item">
            <label>Schultyp:</label>
            <span>{{ selectedSchool.schultyp || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>PLZ:</label>
            <span>{{ selectedSchool.plz || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>Ort:</label>
            <span>{{ selectedSchool.ort || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>Straße:</label>
            <span>{{ selectedSchool.strasse || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>Kreis:</label>
            <span>{{ selectedSchool.kreis || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>Schultraegernr:</label>
            <span>{{ selectedSchool.schultraegernummer || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>Schultraeger:</label>
            <span>{{ selectedSchool.schultraegername || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>Telefon:</label>
            <span v-if="selectedSchool.telefon">
              <a :href="`tel:${selectedSchool.telefon}`">{{ selectedSchool.telefon }}</a>
            </span>
            <span v-else>—</span>
          </div>
          <div class="detail-item">
            <label>Fax:</label>
            <span>{{ selectedSchool.fax || '—' }}</span>
          </div>
          <div class="detail-item">
            <label>E-Mail:</label>
            <span v-if="selectedSchool.email">
              <a :href="`mailto:${selectedSchool.email}`">{{ selectedSchool.email }}</a>
            </span>
            <span v-else>—</span>
          </div>
          <div class="detail-item">
            <label>Website:</label>
            <span v-if="selectedSchool.homepage">
              <a :href="selectedSchool.homepage" target="_blank" rel="noopener noreferrer">
                {{ selectedSchool.homepage }}
              </a>
            </span>
            <span v-else>—</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { nrwKatalogApi } from '../services/nrwKatalogApi';
import type { NrwSchulkatalogeintrag } from '../types/nrwSchulkatalog';

const schools = ref<NrwSchulkatalogeintrag[]>([]);
const searchQuery = ref('');
const currentPage = ref(0);
const totalSchools = ref(0);
const pageSize = 50;
const loading = ref(false);
const error = ref('');
const selectedSchool = ref<NrwSchulkatalogeintrag | null>(null);
const sortBy = ref<string>('schulnummer');
const sortDirection = ref<'asc' | 'desc'>('asc');

const sortedSchools = computed(() => schools.value);

const totalPages = computed(() => {
  return Math.ceil(totalSchools.value / pageSize);
});

const loadSchools = async () => {
  loading.value = true;
  error.value = '';
  try {
    const result = await nrwKatalogApi.getSchools(currentPage.value, pageSize, sortBy.value, sortDirection.value);
    schools.value = result.schools;
    totalSchools.value = result.total;
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten';
    schools.value = [];
  } finally {
    loading.value = false;
  }
};

const executeSearch = async () => {
  if (searchQuery.value.trim() === '') {
    await loadSchools();
  } else {
    loading.value = true;
    error.value = '';
    try {
      const result = await nrwKatalogApi.searchSchools(searchQuery.value, currentPage.value, pageSize, sortBy.value, sortDirection.value);
      schools.value = result.schools;
      totalSchools.value = result.total;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten';
      schools.value = [];
    } finally {
      loading.value = false;
    }
  }
};

const performSearch = async () => {
  currentPage.value = 0;
  await executeSearch();
};

const resetSearch = async () => {
  searchQuery.value = '';
  currentPage.value = 0;
  await loadSchools();
};

const loadData = async () => {
  loading.value = true;
  error.value = '';
  try {
    await nrwKatalogApi.refreshCatalog();
    // Wait a moment for backend to process
    await new Promise(resolve => setTimeout(resolve, 1000));
    await loadSchools();
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten';
    schools.value = [];
  } finally {
    loading.value = false;
  }
};

const refreshData = async () => {
  loading.value = true;
  error.value = '';
  try {
    await nrwKatalogApi.refreshCatalog();
    // Wait a moment for backend to process
    await new Promise(resolve => setTimeout(resolve, 1000));
    currentPage.value = 0;
    await executeSearch();
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten';
  } finally {
    loading.value = false;
  }
};

const nextPage = async () => {
  currentPage.value++;
  await executeSearch();
};

const previousPage = async () => {
  if (currentPage.value > 0) {
    currentPage.value--;
    await executeSearch();
  }
};

const retryLoad = async () => {
  await executeSearch();
};

const showDetails = (school: NrwSchulkatalogeintrag) => {
  selectedSchool.value = school;
};

const handleSort = (column: string) => {
  if (sortBy.value === column) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = column;
    sortDirection.value = 'asc';
  }
  currentPage.value = 0;
  executeSearch();
};

const parseNswDate = (value: string | null): Date | null => {
  if (!value) return null;
  const datePart = value.split(' ')[0];
  const [day, month, year] = datePart.split('.');
  if (!day || !month || !year) return null;
  const parsed = new Date(Number(year), Number(month) - 1, Number(day));
  return isNaN(parsed.getTime()) ? null : parsed;
};

const formatDate = (date: Date) => {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}.${month}.${year}`;
};

const getStatusClass = (aufloesung: string | null) => {
  const date = parseNswDate(aufloesung);
  if (!date) return 'status-unknown';
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date < today ? 'status-inactive' : 'status-active';
};

const getStatusTooltip = (aufloesung: string | null) => {
  const date = parseNswDate(aufloesung);
  if (!date) return 'Status unbekannt';
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  if (date < today) {
    return `Aufgelöst am ${formatDate(date)}`;
  }
  return 'Aktiv';
};

// Load initial schools
loadSchools();
</script>

<style scoped>
.schulkatalog-container {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.schulkatalog-controls {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.search-bar {
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  padding: 0.5rem 0.75rem;
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 8px;
  color: #f8fafc;
  font-size: 0.9rem;
}

.search-input::placeholder {
  color: #6b7280;
}

.search-input:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.search-button, .reset-button, .retry-button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.search-button {
  background: #f97316;
  color: white;
}

.search-button:hover {
  background: #ea580c;
}

.reset-button {
  background: #6b7280;
  color: white;
}

.reset-button:hover {
  background: #4b5563;
}

.refresh-button {
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.refresh-button:hover:not(:disabled) {
  background: #2563eb;
}

.refresh-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.load-data-button {
  padding: 0.5rem 1rem;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.load-data-button:hover {
  background: #059669;
}

.retry-button {
  background: #dc2626;
  color: white;
  margin-top: 1rem;
}

.retry-button:hover {
  background: #b91c1c;
}

.catalog-info {
  text-align: center;
  color: #94a3b8;
  font-size: 0.95rem;
}

.schulkatalog-content {
  min-height: 400px;
}

.loading,
.no-results {
  text-align: center;
  padding: 3rem;
  color: #94a3b8;
  font-size: 1.1rem;
}

.error-message {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #dc2626;
  border-radius: 8px;
  padding: 1.5rem;
  color: #fca5a5;
  margin-bottom: 2rem;
}

.error-message strong {
  color: #fecaca;
}

.schools-table-wrapper {
  overflow-x: auto;
  margin-bottom: 2rem;
}

.schools-table {
  width: 100%;
  border-collapse: collapse;
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 8px;
  overflow: hidden;
}

.schools-table thead {
  background: #374151;
  font-weight: 600;
}

.schools-table th {
  padding: 0.5rem 0.75rem;
  text-align: left;
  color: #f8fafc;
  border-bottom: 1px solid #4b5563;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.sortable-header {
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
}

.sortable-header:hover {
  background: #4b5563;
  border-radius: 4px;
}

.sort-indicator {
  font-size: 0.7rem;
  font-weight: bold;
  color: #f97316;
  margin-left: 0.25rem;
}

.schools-table td {
  padding: 0.4rem 0.75rem;
  border-bottom: 1px solid #374151;
  color: #e2e8f0;
  font-size: 0.9rem;
}

.school-row:hover {
  background: #374151;
}

.school-row:last-child td {
  border-bottom: none;
}

.schulnummer {
  font-weight: 600;
  color: #f97316;
  min-width: 120px;
}

.schulname {
  color: #f8fafc;
  font-weight: 500;
}

.plz {
  color: #cbd5e1;
  min-width: 80px;
}

.ort {
  color: #cbd5e1;
  min-width: 150px;
}

.schultyp {
  color: #cbd5e1;
  min-width: 120px;
}

.kreis {
  color: #cbd5e1;
  min-width: 120px;
}

.status-cell {
  text-align: center;
  min-width: 90px;
}

.status-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #6b7280;
  box-shadow: 0 0 0 3px rgba(107, 114, 128, 0.2);
}

.status-active {
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.2);
}

.status-inactive {
  background: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
}

.status-unknown {
  background: #6b7280;
  box-shadow: 0 0 0 3px rgba(107, 114, 128, 0.2);
}

.aktionen {
  text-align: center;
}

.details-button {
  padding: 0.35rem 0.75rem;
  background: #1e40af;
  color: #bfdbfe;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.details-button:hover {
  background: #1e3a8a;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
  padding: 2rem;
  background: #1f2937;
  border-radius: 8px;
}

.pagination-button {
  padding: 0.5rem 1rem;
  background: #374151;
  border: 1px solid #4b5563;
  border-radius: 6px;
  color: #f8fafc;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.pagination-button:hover:not(:disabled) {
  background: #f97316;
  border-color: #f97316;
  color: white;
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #94a3b8;
  font-size: 0.9rem;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 1.25rem 1.5rem;
  max-width: 760px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
}

.close-button {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.2s ease;
}

.close-button:hover {
  color: #f8fafc;
}

.modal-content h2 {
  margin: 0 0 1.25rem 0;
  padding-bottom: 0.6rem;
  color: #f8fafc;
  font-size: 1.4rem;
  border-bottom: 2px solid #374151;
}

.amtsbez-block {
  margin: 0 0 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  color: #e2e8f0;
  font-size: 0.95rem;
  font-weight: 500;
}

.amtsbez-line {
  line-height: 1.2;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.9rem 1.4rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.detail-item label {
  color: #94a3b8;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.detail-item span {
  color: #e2e8f0;
  font-size: 0.95rem;
  font-weight: 500;
  line-height: 1.2;
}

.detail-item a {
  color: #f97316;
  text-decoration: none;
  transition: color 0.2s ease;
}

.detail-item a:hover {
  color: #ea580c;
}

@media (max-width: 768px) {
  .schulkatalog-container {
    padding: 1rem;
  }

  .schulkatalog-header h1 {
    font-size: 1.8rem;
  }

  .search-bar {
    flex-direction: column;
  }

  .search-input {
    min-width: unset;
  }

  .schools-table {
    font-size: 0.9rem;
  }

  .schools-table th,
  .schools-table td {
    padding: 0.5rem;
  }

  .schulnummer,
  .plz,
  .ort,
  .schultyp,
  .kreis {
    min-width: unset;
  }

  .pagination {
    flex-direction: column;
    gap: 1rem;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
  }
}
</style>
