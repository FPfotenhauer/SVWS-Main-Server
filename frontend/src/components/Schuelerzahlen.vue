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

        <!-- Statistics Section -->
        <div v-if="statisticsLoading" class="stats-loading">
          <span class="pulse-small"></span>
          <p>Statistiken werden geladen …</p>
        </div>

        <div v-else-if="statisticsError" class="stats-error">
          <p>{{ statisticsError }}</p>
        </div>

        <div v-else-if="statistics">
          <div class="stats-section-title">Schülerstatistiken</div>
          
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-label">Gesamtschüler</div>
              <div class="stat-value">{{ statistics.totalStudents }}</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-label">Männlich / Weiblich</div>
              <div class="stat-value">{{ statistics.maleStudents }} / {{ statistics.femaleStudents }}</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-label">Mit Förderschwerpunkt</div>
              <div class="stat-value">{{ statistics.studentsWithSpecialNeeds }}</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-label">Mit Migrationshintergrund</div>
              <div class="stat-value">{{ statistics.studentsWithMigrationBackground }}</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-label">Abitur berechtigt</div>
              <div class="stat-value">{{ statistics.abiStudentsEligible }}</div>
            </div>
            
            <div class="stat-card">
              <div class="stat-label">Abitur bestanden</div>
              <div class="stat-value">{{ statistics.abiStudentsPassed }}</div>
            </div>
          </div>

          <div v-if="statistics.studentsByGrade && statistics.studentsByGrade.length" class="stats-subsection">
            <div class="subsection-title">Schüler nach Jahrgängen</div>
            <div class="grade-grid">
              <article v-for="grade in statistics.studentsByGrade" :key="grade.gradeName" class="grade-tile">
                <div class="grade-name">{{ grade.gradeName }}</div>
                <div class="grade-count-tile">{{ grade.count }}</div>

                <div v-if="confessionsForGrade(grade.gradeName).length" class="grade-confession-list">
                  <div
                    v-for="confession in confessionsForGrade(grade.gradeName)"
                    :key="`${grade.gradeName}-${confession.confessionCode}`"
                    class="grade-confession-row"
                  >
                    <span class="grade-confession-name">{{ confession.confessionName }}</span>
                    <span class="grade-confession-count">{{ confession.count }}</span>
                  </div>
                </div>
              </article>
            </div>
          </div>

          <div v-if="statistics.topLocations && statistics.topLocations.length" class="stats-subsection">
            <div class="subsection-title">Top Wohnorte</div>
            <div class="location-grid">
              <article
                v-for="loc in statistics.topLocations"
                :key="`${loc.locationName}-${loc.postalCode}`"
                class="location-tile"
              >
                <span class="location-name">{{ loc.locationName || 'Unbekannt' }}</span>
                <span class="location-code">{{ loc.postalCode ? `PLZ ${loc.postalCode}` : 'PLZ unbekannt' }}</span>
                <span class="location-count">{{ loc.count }}</span>
              </article>
            </div>
          </div>

          <div v-if="statistics.classStatistics && statistics.classStatistics.length" class="stats-subsection">
            <div class="subsection-title">Klassen</div>
            <div class="class-grid">
              <article
                v-for="classStatistic in statistics.classStatistics"
                :key="classStatistic.classId ?? classStatistic.className"
                class="class-card"
              >
                <div class="class-card-header">
                  <div>
                    <div class="class-card-title">{{ classStatistic.className }}</div>
                    <div class="class-card-subtitle">Klassen-ID: {{ classStatistic.classId ?? 'ohne ID' }}</div>
                  </div>
                </div>

                <div class="class-table-wrapper">
                  <table class="class-table class-table-summary">
                    <thead>
                      <tr>
                        <th>Gesamt</th>
                        <th>Männlich</th>
                        <th>Weiblich</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>{{ classStatistic.totalStudents }}</td>
                        <td>{{ classStatistic.maleStudents }}</td>
                        <td>{{ classStatistic.femaleStudents }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="class-card-section">
                  <div class="class-card-section-title">Jahrgänge</div>
                  <div v-if="classStatistic.grades.length" class="class-table-wrapper">
                    <table class="class-table">
                      <thead>
                        <tr>
                          <th>Jahrgang</th>
                          <th class="class-align-right">Anzahl</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="grade in classStatistic.grades" :key="`${classStatistic.classId ?? classStatistic.className}-${grade.gradeName}`">
                          <td>{{ grade.gradeName }}</td>
                          <td class="class-align-right">{{ grade.count }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div v-else class="class-empty">Keine Schüler in Jahrgängen zugeordnet</div>
                </div>

                <div class="class-card-section">
                  <div class="class-card-section-title">Förderschwerpunkte</div>
                  <div v-if="classStatistic.specialNeeds.length" class="class-table-wrapper">
                    <table class="class-table">
                      <thead>
                        <tr>
                          <th>Kürzel</th>
                          <th>Förderschwerpunkt</th>
                          <th class="class-align-right">Anzahl</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="specialNeed in classStatistic.specialNeeds"
                          :key="`${classStatistic.classId ?? classStatistic.className}-${specialNeed.specialNeedCode}`"
                        >
                          <td>{{ specialNeed.specialNeedCode }}</td>
                          <td>{{ specialNeed.specialNeedName }}</td>
                          <td class="class-align-right">{{ specialNeed.count }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div v-else class="class-empty">Keine Förderschwerpunkte vorhanden</div>
                </div>

                <div class="class-card-section">
                  <div class="class-card-section-title">Nationalitäten</div>
                  <div v-if="classStatistic.nationalities && classStatistic.nationalities.length" class="class-table-wrapper">
                    <table class="class-table">
                      <thead>
                        <tr>
                          <th>Nationalität</th>
                          <th class="class-align-right">Gesamt</th>
                          <th class="class-align-right">M</th>
                          <th class="class-align-right">W</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="nat in classStatistic.nationalities"
                          :key="`${classStatistic.classId ?? classStatistic.className}-${nat.nationalityCode}`"
                        >
                          <td>{{ nat.nationalityName }} ({{ nat.nationalityCode }})</td>
                          <td class="class-align-right">{{ nat.count }}</td>
                          <td class="class-align-right">{{ nat.maleCount }}</td>
                          <td class="class-align-right">{{ nat.femaleCount }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div v-else class="class-empty">Keine Nationalitätsdaten vorhanden</div>
                </div>
              </article>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import api from '../services/api';
import type { SchuleStammdatenResponse } from '../types/schule';

interface SchuleStatistikenGesamt {
  totalStudents: number;
  maleStudents: number;
  femaleStudents: number;
  studentsWithSpecialNeeds: number;
  studentsWithMigrationBackground: number;
  abiStudentsEligible: number;
  abiStudentsPassed: number;
  studentsByGrade: Array<{
    gradeName: string;
    count: number;
  }>;
  confessionsByGrade: Array<{
    gradeName: string;
    confessions: Array<{
      confessionCode: string;
      confessionName: string;
      count: number;
    }>;
  }>;
  topLocations: Array<{
    locationName: string;
    postalCode: string;
    count: number;
  }>;
  classStatistics: Array<{
    classId: number | null;
    className: string;
    totalStudents: number;
    maleStudents: number;
    femaleStudents: number;
    grades: Array<{
      gradeName: string;
      count: number;
    }>;
    specialNeeds: Array<{
      specialNeedCode: string;
      specialNeedName: string;
      count: number;
    }>;
    nationalities: Array<{
      nationalityCode: string;
      nationalityName: string;
      count: number;
      maleCount: number;
      femaleCount: number;
    }>;
  }>;
}

const items = ref<SchuleStammdatenResponse[]>([]);
const loading = ref(false);
const error = ref('');
const searchQuery = ref('');
const currentPage = ref(1);
const itemsPerPage = ref(10);
const showModal = ref(false);
const selectedItem = ref<SchuleStammdatenResponse | null>(null);
const statistics = ref<SchuleStatistikenGesamt | null>(null);
const statisticsLoading = ref(false);
const statisticsError = ref('');

const confessionsByGradeMap = computed(() => {
  const map = new Map<string, SchuleStatistikenGesamt['confessionsByGrade'][number]['confessions']>();
  const confessionsByGrade = statistics.value?.confessionsByGrade ?? [];

  confessionsByGrade.forEach((entry) => {
    map.set(entry.gradeName, entry.confessions ?? []);
  });

  return map;
});

const confessionsForGrade = (gradeName: string) => {
  return confessionsByGradeMap.value.get(gradeName) ?? [];
};

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

const openModal = async (item: SchuleStammdatenResponse) => {
  selectedItem.value = item;
  statistics.value = null;
  statisticsError.value = '';
  showModal.value = true;

  // Fetch statistics when modal opens
  statisticsLoading.value = true;
  try {
    const response = await api.get<SchuleStatistikenGesamt>(`/api/schulen/${item.id}/statistiken`);
    statistics.value = response.data;
  } catch (err: any) {
    statisticsError.value = err.response?.data?.message || err.message || 'Fehler beim Laden der Statistiken';
  } finally {
    statisticsLoading.value = false;
  }
};

const closeModal = () => {
  showModal.value = false;
  selectedItem.value = null;
  statistics.value = null;
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

/* Statistics Styles */
.stats-loading,
.stats-error {
  padding: 1rem;
  margin: 1rem 0;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #e2e8f0;
}

.stats-error {
  border-color: rgba(248, 113, 113, 0.3);
  background: rgba(248, 113, 113, 0.1);
  color: #fca5a5;
}

.pulse-small {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #38bdf8;
  animation: pulse 1.2s infinite;
}

.stats-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #e2e8f0;
  margin: 1.5rem 0 1rem 0;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-size: 0.9rem;
  color: rgba(226, 232, 240, 0.7);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 8px;
  padding: 0.75rem 1rem;
  text-align: center;
}

.stat-label {
  font-size: 0.75rem;
  color: rgba(226, 232, 240, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #38bdf8;
}

.stats-subsection {
  margin: 1.5rem 0;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.3);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 8px;
}

.subsection-title {
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(226, 232, 240, 0.6);
  margin-bottom: 0.75rem;
}

.grade-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 0.6rem;
  margin-bottom: 0.5rem;
}

.grade-tile {
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 8px;
  padding: 0.75rem 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.grade-tile .grade-name {
  font-size: 0.85rem;
  color: rgba(226, 232, 240, 0.7);
  font-weight: 600;
  text-align: center;
}

.grade-count-tile {
  font-size: 1.5rem;
  font-weight: 700;
  color: #38bdf8;
  text-align: center;
}

.grade-confession-list {
  margin-top: 0.2rem;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding-top: 0.4rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.grade-confession-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.78rem;
}

.grade-confession-name {
  color: rgba(226, 232, 240, 0.85);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.grade-confession-count {
  color: #38bdf8;
  font-weight: 700;
  min-width: 1.5rem;
  text-align: right;
}

.location-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 0.6rem;
}

.location-tile {
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 8px;
  padding: 0.65rem 0.7rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  min-height: 90px;
}

.location-name {
  color: #e2e8f0;
  font-weight: 600;
  line-height: 1.2;
}

.location-code {
  color: rgba(226, 232, 240, 0.5);
  font-size: 0.8rem;
}

.location-count {
  margin-top: auto;
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.85rem;
}

.class-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.class-card {
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 10px;
  padding: 1.15rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.class-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.9rem;
}

.class-card-title {
  color: #e2e8f0;
  font-size: 1.15rem;
  font-weight: 700;
}

.class-card-subtitle {
  color: rgba(226, 232, 240, 0.65);
  font-size: 0.82rem;
  margin-top: 0.15rem;
}

.class-table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.class-card-section {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.class-card-section-title {
  color: rgba(226, 232, 240, 0.75);
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.class-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 420px;
  background: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 8px;
  overflow: hidden;
}

.class-table th {
  background: rgba(30, 41, 59, 0.65);
  color: rgba(226, 232, 240, 0.75);
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 0.5rem 0.65rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  text-align: left;
}

.class-table td {
  color: #e2e8f0;
  font-size: 0.84rem;
  padding: 0.5rem 0.65rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}

.class-table tbody tr:last-child td {
  border-bottom: none;
}

.class-table-summary {
  min-width: 300px;
}

.class-table-summary th,
.class-table-summary td {
  text-align: center;
}

.class-table-summary td {
  color: #7dd3fc;
  font-size: 1rem;
  font-weight: 700;
}

.class-align-right {
  text-align: right !important;
}

.class-empty {
  color: rgba(148, 163, 184, 0.8);
  font-size: 0.8rem;
}
</style>