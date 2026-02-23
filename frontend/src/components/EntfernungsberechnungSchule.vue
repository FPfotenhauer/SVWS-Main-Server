<template>
  <section class="distance-school-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Entfernungsberechnung</p>
        <h2>Schüler:in suchen</h2>
        <p class="subtitle">
          Für die ausgewählte Schule wird hier im nächsten Schritt die Schülersuche implementiert.
        </p>
      </div>
      <button class="ghost" type="button" @click="emit('back')">Zurück zur Schulauswahl</button>
    </header>

    <div class="school-info" v-if="props.school">
      <h3>Ausgewählte Schule</h3>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">Schulnummer</span>
          <span class="value">{{ props.school.schulnummer ?? '–' }}</span>
        </div>
        <div class="info-item">
          <span class="label">Schulform</span>
          <span class="value">{{ props.school.schulform ?? '–' }}</span>
        </div>
        <div class="info-item wide">
          <span class="label">Schulname</span>
          <span class="value">{{ props.school.bezeichnung1 ?? '–' }}</span>
        </div>
      </div>
    </div>

    <div class="search-panel" v-if="props.school">
      <div class="search-grid">
        <input v-model="searchNachname" type="text" placeholder="Nachname" class="search-input" />
        <input v-model="searchVorname" type="text" placeholder="Vorname" class="search-input" />
        <input v-model="searchGeburtsdatum" type="date" class="search-input" />
      </div>
      <div class="search-footer">
        <button class="ghost" type="button" @click="loadSchueler" :disabled="loadingSchueler">
          {{ loadingSchueler ? 'Aktualisiere …' : 'Liste aktualisieren' }}
        </button>
        <span class="count">{{ sortedSchueler.length }} von {{ statusFilteredTotal }} Schüler:innen</span>
      </div>
    </div>

    <div v-if="loadingSchueler" class="state-box">Schülerliste wird geladen …</div>
    <div v-else-if="schuelerError" class="state-box error">{{ schuelerError }}</div>
    <div v-else-if="!sortedSchueler.length" class="state-box">Keine passenden Schüler:innen gefunden.</div>

    <div v-else class="table-shell">
      <table>
        <thead>
          <tr>
            <th>
              <button class="sort-btn" type="button" @click="setSorting('nachname')">
                Name
                <span class="sort-indicator" v-if="sortBy === 'nachname'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
              </button>
            </th>
            <th>
              <button class="sort-btn" type="button" @click="setSorting('vorname')">
                Vorname
                <span class="sort-indicator" v-if="sortBy === 'vorname'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
              </button>
            </th>
            <th>
              <button class="sort-btn" type="button" @click="setSorting('geburtsdatum')">
                Geburtsdatum
                <span class="sort-indicator" v-if="sortBy === 'geburtsdatum'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
              </button>
            </th>
            <th>
              <button class="sort-btn" type="button" @click="setSorting('status')">
                Status
                <span class="sort-indicator" v-if="sortBy === 'status'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
              </button>
            </th>
            <th>Aktion</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedSchueler" :key="item.id">
            <td>{{ item.nachname ?? '–' }}</td>
            <td>{{ item.vorname ?? '–' }}</td>
            <td>{{ formatDate(item.geburtsdatum) }}</td>
            <td>{{ item.status ?? '–' }}</td>
            <td>
              <button class="select-btn" type="button" @click="loadAdresse(item)">Adresse anzeigen</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="totalPages > 1">
        <button class="pagination-btn" type="button" :disabled="currentPage === 1" @click="currentPage--">
          Zurück
        </button>
        <span class="pagination-info">Seite {{ currentPage }} von {{ totalPages }}</span>
        <button class="pagination-btn" type="button" :disabled="currentPage === totalPages" @click="currentPage++">
          Weiter
        </button>
      </div>
    </div>

    <div v-if="adresseLoading" class="state-box">Adressdaten werden geladen …</div>
    <div v-else-if="adresseError" class="state-box error">{{ adresseError }}</div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import api from '../services/api';
import type { SchuleStammdatenResponse } from '../types/schule';
import type { SchuelerAdresse, SchuelerAuswahl } from '../types/schueler';

const props = defineProps<{
  school: SchuleStammdatenResponse | null;
}>();

const emit = defineEmits<{
  back: [];
  showAddress: [adresse: SchuelerAdresse];
}>();

const schueler = ref<SchuelerAuswahl[]>([]);
const loadingSchueler = ref(false);
const schuelerError = ref('');

const searchNachname = ref('');
const searchVorname = ref('');
const searchGeburtsdatum = ref('');

const sortBy = ref<'nachname' | 'vorname' | 'geburtsdatum' | 'status'>('nachname');
const sortDirection = ref<'asc' | 'desc'>('asc');
const currentPage = ref(1);
const pageSize = ref(20);

const adresseLoading = ref(false);
const adresseError = ref('');

const normalize = (value?: string | null) => (value ?? '').toLowerCase().trim();

const formatDate = (value?: string | null) => {
  if (!value) {
    return '–';
  }
  return value.length >= 10 ? value.slice(0, 10) : value;
};

const filteredSchueler = computed(() => {
  const nachname = normalize(searchNachname.value);
  const vorname = normalize(searchVorname.value);
  const geburtsdatum = searchGeburtsdatum.value;
  const allowedStatus = new Set([0, 1, 2, 3]);

  return schueler.value.filter((item) => {
    const status = Number(item.status);
    const matchStatus = allowedStatus.has(status);
    const matchNachname = !nachname || normalize(item.nachname).includes(nachname);
    const matchVorname = !vorname || normalize(item.vorname).includes(vorname);
    const matchGeburtsdatum = !geburtsdatum || (item.geburtsdatum ?? '').startsWith(geburtsdatum);
    return matchStatus && matchNachname && matchVorname && matchGeburtsdatum;
  });
});

const statusFilteredTotal = computed(() => {
  const allowedStatus = new Set([0, 1, 2, 3]);
  return schueler.value.filter((item) => allowedStatus.has(Number(item.status))).length;
});

const sortedSchueler = computed(() => {
  const items = [...filteredSchueler.value];
  const direction = sortDirection.value === 'asc' ? 1 : -1;

  return items.sort((a, b) => {
    if (sortBy.value === 'status') {
      const aVal = a.status ?? Number.MAX_SAFE_INTEGER;
      const bVal = b.status ?? Number.MAX_SAFE_INTEGER;
      return (aVal - bVal) * direction;
    }

    const aVal = normalize((a as any)[sortBy.value]);
    const bVal = normalize((b as any)[sortBy.value]);
    return aVal.localeCompare(bVal) * direction;
  });
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(sortedSchueler.value.length / pageSize.value));
});

const paginatedSchueler = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return sortedSchueler.value.slice(start, start + pageSize.value);
});

const setSorting = (column: 'nachname' | 'vorname' | 'geburtsdatum' | 'status') => {
  if (sortBy.value === column) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
    return;
  }
  sortBy.value = column;
  sortDirection.value = 'asc';
};

watch([searchNachname, searchVorname, searchGeburtsdatum, sortBy, sortDirection], () => {
  currentPage.value = 1;
});

watch(totalPages, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value;
  }
});

const loadSchueler = async () => {
  if (!props.school || loadingSchueler.value) {
    return;
  }

  loadingSchueler.value = true;
  schuelerError.value = '';
  try {
    const response = await api.get<SchuelerAuswahl[]>(`/api/schulen/${props.school.id}/schueler/auswahlliste`);
    schueler.value = response.data;
    currentPage.value = 1;
  } catch (err: any) {
    schuelerError.value = err.response?.data?.message || err.message || 'Fehler beim Laden der Schülerliste';
  } finally {
    loadingSchueler.value = false;
  }
};

const loadAdresse = async (schuelerEintrag: SchuelerAuswahl) => {
  if (!props.school) {
    return;
  }

  adresseLoading.value = true;
  adresseError.value = '';

  try {
    const response = await api.get<SchuelerAdresse>(`/api/schulen/${props.school.id}/schueler/${schuelerEintrag.id}/stammdaten`);
    emit('showAddress', response.data);
  } catch (err: any) {
    adresseError.value = err.response?.data?.message || err.message || 'Fehler beim Laden der Adressdaten';
  } finally {
    adresseLoading.value = false;
  }
};

onMounted(loadSchueler);
</script>

<style scoped>
.distance-school-page {
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

.school-info {
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  padding: 1rem 1.25rem;
}

.search-panel {
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  padding: 0.9rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.6rem;
}

.search-input {
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  background: #0f172a;
  color: #e2e8f0;
  padding: 0.6rem 0.75rem;
}

.search-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count {
  color: #94a3b8;
  font-size: 0.85rem;
}

.state-box {
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: #0f172a;
  padding: 0.8rem 1rem;
  color: #e2e8f0;
}

.state-box.error {
  border-color: rgba(244, 63, 94, 0.5);
  color: #fecdd3;
}

.table-shell {
  overflow-x: auto;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

th,
td {
  text-align: left;
  padding: 0.5rem 0.65rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  line-height: 1.2;
}

th {
  background: #111827;
  color: #cbd5e1;
}

td {
  color: #e2e8f0;
}

.select-btn {
  border: none;
  background: #3b82f6;
  color: #f8fafc;
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
}

.sort-btn {
  border: none;
  background: transparent;
  color: #cbd5e1;
  font-weight: 700;
  font-size: 0.82rem;
  padding: 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
}

.sort-indicator {
  color: #f97316;
  font-size: 0.65rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.8rem;
  padding: 0.6rem;
}

.pagination-btn {
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: #111827;
  color: #e2e8f0;
  padding: 0.3rem 0.7rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.82rem;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: #94a3b8;
  font-size: 0.82rem;
}

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
