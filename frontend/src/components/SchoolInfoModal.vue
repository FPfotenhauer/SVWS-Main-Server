<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content school-info-modal">
      <div class="modal-header">
        <h2>Schulinformationen</h2>
        <div class="header-actions">
          <button class="sync-btn" @click="syncSchool" :disabled="syncing" title="Schulinformationen synchronisieren">
            <svg v-if="!syncing" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"></polyline>
              <path d="M20.49 15a9 9 0 1 1-2-8.83"></path>
            </svg>
            <svg v-else class="spinning" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="1"></circle>
              <circle cx="19" cy="5" r="1"></circle>
              <circle cx="5" cy="19" r="1"></circle>
              <circle cx="19" cy="19" r="1"></circle>
              <circle cx="5" cy="5" r="1"></circle>
            </svg>
          </button>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
      </div>

      <div class="modal-body">
        <div v-if="schule" class="school-info-content">
        <div class="info-section">
          <h3>Allgemeine Informationen</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>Name:</label>
              <span>{{ schule.name }}</span>
            </div>
            <div class="info-item">
              <label>Schulnummer:</label>
              <span>{{ schule.schulnummer || '-' }}</span>
            </div>
            <div v-if="schule.ort" class="info-item">
              <label>Ort:</label>
              <span>{{ schule.ort }}</span>
            </div>
            <div v-if="schule.plz" class="info-item">
              <label>PLZ:</label>
              <span>{{ schule.plz }}</span>
            </div>
            <div v-if="schule.svwsUrl" class="info-item">
              <label>SVWS URL:</label>
              <span>{{ schule.svwsUrl }}</span>
            </div>
            <div v-if="schule.svwsSchema" class="info-item">
              <label>Schema:</label>
              <span>{{ schule.svwsSchema }}</span>
            </div>
            <div v-if="schule.svwsUsername" class="info-item">
              <label>Benutzername:</label>
              <span>{{ schule.svwsUsername }}</span>
            </div>
            <div v-if="schule.status" class="info-item">
              <label>Status:</label>
              <span class="status" :class="statusClass(schule.status)">{{ schule.status }}</span>
            </div>
            <div v-if="schule.lastSyncAt" class="info-item">
              <label>Letzter Sync:</label>
              <span>{{ formatted(schule.lastSyncAt) }}</span>
            </div>
            <div v-if="schule.lastSyncStatus" class="info-item">
              <label>Sync Status:</label>
              <span class="status" :class="statusClass(schule.lastSyncStatus || '')">{{ schule.lastSyncStatus || '-' }}</span>
            </div>
            <div v-if="schule.createdAt" class="info-item">
              <label>Erstellt:</label>
              <span>{{ formatted(schule.createdAt) }}</span>
            </div>
            <div v-if="schule.updatedAt" class="info-item">
              <label>Aktualisiert:</label>
              <span>{{ formatted(schule.updatedAt) }}</span>
            </div>
          </div>
        </div>

        <div v-if="schule.lastError" class="info-section">
          <h3>Letzter Fehler</h3>
          <div class="error-message">{{ schule.lastError }}</div>
        </div>

        <!-- Synced School Information -->
        <div class="info-section">
          <h3>Synchronisierte Schulinformationen</h3>
          <div v-if="props.schule.lastSyncAt" class="sync-status">
            Letzter erfolgreicher Sync: {{ formatted(props.schule.lastSyncAt) }}
          </div>
          
          <!-- Basic Information -->
          <div class="info-subsection">
            <h4>Grunddaten</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>Schulnummer 1:</label>
                <span>{{ props.schule.schulnummer || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Schulnummer 2:</label>
                <span>{{ props.schule.schulnummer2 || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Schulstatus:</label>
                <span>{{ props.schule.schulstatus || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Address Information -->
          <div class="info-subsection">
            <h4>Adresse</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>Straße:</label>
                <span>{{ props.schule.strasse || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Hausnummer:</label>
                <span>{{ props.schule.hausnummer || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Hausnummer Zusatz:</label>
                <span>{{ props.schule.hausnummerZusatz || '-' }}</span>
              </div>
              <div class="info-item">
                <label>PLZ:</label>
                <span>{{ props.schule.plz || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Ort:</label>
                <span>{{ props.schule.ort || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Contact Information -->
          <div class="info-subsection">
            <h4>Kontakt</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>Telefon:</label>
                <span>{{ props.schule.telefon || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Fax:</label>
                <span>{{ props.schule.fax || '-' }}</span>
              </div>
              <div class="info-item">
                <label>E-Mail:</label>
                <span>{{ props.schule.email || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Homepage:</label>
                <span v-if="props.schule.homepage">
                  <a :href="props.schule.homepage" target="_blank" rel="noopener noreferrer">{{ props.schule.homepage }}</a>
                </span>
                <span v-else>-</span>
              </div>
            </div>
          </div>

          <!-- School Leadership -->
          <div class="info-subsection">
            <h4>Schulleitung</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>Schulleiter/in:</label>
                <span>{{ props.schule.schulleiter || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Telefon:</label>
                <span>{{ props.schule.schulleiterTelefon || '-' }}</span>
              </div>
              <div class="info-item">
                <label>E-Mail:</label>
                <span>{{ props.schule.schulleiterEmail || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Administrative Information -->
          <div class="info-subsection">
            <h4>Verwaltung & Region</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>Kreis:</label>
                <span>{{ props.schule.kreis || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Schulamt:</label>
                <span>{{ props.schule.schulamt || '-' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h3>SVWS Informationen (Live-Daten)</h3>
          <div v-if="loadingSvwsInfo" class="loading">Lade SVWS Informationen...</div>
          <div v-else-if="svwsInfoError" class="error-message">{{ svwsInfoError }}</div>
          <div v-else-if="svwsInfo" class="school-info-content">
            <!-- Basic Information -->
            <div class="info-subsection">
              <h4>Grunddaten</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Schulnummer 1:</label>
                  <span>{{ svwsInfo.schulnummer || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Name:</label>
                  <span>{{ svwsInfo.name || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Schulnummer 2:</label>
                  <span>{{ svwsInfo.schulnummer2 || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Schulstatus:</label>
                  <span>{{ svwsInfo.schulstatus || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- Address Information -->
            <div class="info-subsection">
              <h4>Adresse</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Straße:</label>
                  <span>{{ svwsInfo.strasse || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Hausnummer:</label>
                  <span>{{ svwsInfo.hausnummer || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Hausnummer Zusatz:</label>
                  <span>{{ svwsInfo.hausnummerZusatz || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>PLZ:</label>
                  <span>{{ svwsInfo.plz || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Ort:</label>
                  <span>{{ svwsInfo.ort || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- Contact Information -->
            <div class="info-subsection">
              <h4>Kontakt</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Telefon:</label>
                  <span>{{ svwsInfo.telefon || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Fax:</label>
                  <span>{{ svwsInfo.fax || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>E-Mail:</label>
                  <span>{{ svwsInfo.email || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Homepage:</label>
                  <span v-if="svwsInfo.homepage">
                    <a :href="svwsInfo.homepage" target="_blank" rel="noopener noreferrer">{{ svwsInfo.homepage }}</a>
                  </span>
                  <span v-else>-</span>
                </div>
              </div>
            </div>

            <!-- School Details -->
            <div class="info-subsection">
              <h4>Schuldetails</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Schulform:</label>
                  <span>{{ svwsInfo.schulform || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Schulart:</label>
                  <span>{{ svwsInfo.schulart || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Schulgliederung:</label>
                  <span>{{ svwsInfo.schulgliederung || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- School Leadership -->
            <div class="info-subsection">
              <h4>Schulleitung</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Schulleiter/in:</label>
                  <span>{{ svwsInfo.schulleiter || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Telefon:</label>
                  <span>{{ svwsInfo.schulleiterTelefon || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>E-Mail:</label>
                  <span>{{ svwsInfo.schulleiterEmail || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- Administrative Information -->
            <div class="info-subsection">
              <h4>Verwaltung & Region</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Kreis:</label>
                  <span>{{ svwsInfo.kreis || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Schulamt:</label>
                  <span>{{ svwsInfo.schulamt || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Staat:</label>
                  <span>{{ svwsInfo.staat || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Kapitel:</label>
                  <span>{{ svwsInfo.kapitel || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>Satzungsgebende Kommune:</label>
                  <span>{{ svwsInfo.satzungsgebendeKommune || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="helper">
            Keine SVWS Informationen verfügbar. Diese Schule muss mit einem SVWS Server verbunden und synchronisiert sein.
          </div>
        </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue";
import api from "../services/api";
import { useSchulenStore } from "../stores/schulen";

const emit = defineEmits<{
  close: [];
}>();

const props = defineProps<{
  visible: boolean;
  schule: any;
}>();

const store = useSchulenStore();
const svwsInfo = ref<any>(null);
const loadingSvwsInfo = ref(false);
const svwsInfoError = ref<string | null>(null);
const syncing = ref(false);

const hasSyncedInfo = computed(() => {
  if (!props.schule) return false;
  return !!(
    props.schule.strasse ||
    props.schule.hausnummer ||
    props.schule.hausnummerZusatz ||
    props.schule.plz ||
    props.schule.ort ||
    props.schule.telefon ||
    props.schule.fax ||
    props.schule.email ||
    props.schule.homepage ||
    props.schule.schulleiter ||
    props.schule.schulleiterTelefon ||
    props.schule.schulleiterEmail ||
    props.schule.kreis ||
    props.schule.schulamt ||
    props.schule.schulnummer2 ||
    props.schule.schulstatus
  );
});

const closeModal = () => {
  emit('close');
};

const syncSchool = async () => {
  if (!props.schule?.id || syncing.value) return;
  
  syncing.value = true;
  try {
    await store.sync(props.schule.id);
    // Get the updated school from the store
    const updated = store.items.find(s => s.id === props.schule.id);
    if (updated) {
      // Emit an event to parent to update the selected school
      Object.assign(props.schule, updated);
    }
    // Fetch fresh SVWS info after sync
    await fetchSvwsInfo();
  } catch (err) {
    console.error("Failed to sync school:", err);
    svwsInfoError.value = "Fehler beim Synchronisieren der Schule";
  } finally {
    syncing.value = false;
  }
};

const statusClass = (status?: string | null) => {
  if (!status) return "";
  if (status === "VERIFIED" || status === "SUCCESS") return "success";
  if (status === "UNVERIFIED") return "warn";
  if (status === "INVALID_CREDENTIALS" || status === "UNREACHABLE" || status === "ERROR") return "error";
  return "";
};

const formatted = (value?: string | null) => {
  if (!value) return "-";
  return new Date(value).toLocaleString();
};

const fetchSvwsInfo = async () => {
  if (!props.schule?.id) return;

  loadingSvwsInfo.value = true;
  svwsInfoError.value = null;

  try {
    const response = await api.get(`/api/schulen/${props.schule.id}/svws-info`);
    svwsInfo.value = response.data;
  } catch (error: any) {
    console.error("Failed to fetch SVWS school info:", error);
    svwsInfoError.value = error.response?.data?.message || "Fehler beim Laden der SVWS Informationen";
  } finally {
    loadingSvwsInfo.value = false;
  }
};

// Watch for modal visibility and school changes
watch(() => props.visible, (visible) => {
  if (visible && props.schule?.id) {
    fetchSvwsInfo();
  } else {
    svwsInfo.value = null;
    svwsInfoError.value = null;
  }
});

watch(() => props.schule?.id, (id) => {
  if (props.visible && id) {
    // Reset SVWS info when school changes
    svwsInfo.value = null;
    svwsInfoError.value = null;
    fetchSvwsInfo();
  }
}, { deep: true });
</script>

<style scoped>
:root,
:host {
  --border: rgba(148, 163, 184, 0.15);
  --text-secondary: #94a3b8;
  --panel-hover: rgba(31, 41, 55, 0.5);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(15, 23, 42, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: linear-gradient(135deg, rgba(17, 24, 39, 0.95) 0%, rgba(15, 23, 42, 0.95) 100%);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  max-width: 900px;
  width: 90vw;
  max-height: 85vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  flex-shrink: 0;
}

.modal-header h2 {
  margin: 0;
  color: var(--accent-2);
  font-size: 1.15rem;
  font-weight: 600;
  flex: 1;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.sync-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-2);
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.15s ease;
  padding: 0;
}

.sync-btn:hover:not(:disabled) {
  background-color: rgba(56, 189, 248, 0.2);
  color: #38bdf8;
}

.sync-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sync-btn svg {
  width: 20px;
  height: 20px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--muted);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.15s ease;
}

.close-btn:hover {
  background-color: rgba(56, 189, 248, 0.2);
  color: var(--accent-2);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* Custom scrollbar styling */
.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.modal-body::-webkit-scrollbar-thumb {
  background: var(--muted);
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

.school-info-modal {
  max-width: 900px;
  width: 90vw;
  max-height: 85vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.school-info-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
}

.info-section {
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 6px;
  padding: 0.75rem;
  background-color: rgba(17, 24, 39, 0.5);
}

.info-section h3 {
  margin: 0 0 0.75rem 0;
  color: var(--accent-2);
  font-size: 1rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  padding: 0.4rem;
}

.info-item label {
  font-weight: 600;
  color: var(--muted);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.info-item span {
  color: var(--text);
  word-break: break-word;
  font-size: 0.9rem;
}

.error-message {
  background-color: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 4px;
  padding: 0.5rem;
  color: #fca5a5;
  white-space: pre-wrap;
  font-size: 0.85rem;
}

.info-subsection {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
}

.info-subsection h4 {
  margin: 0 0 0.5rem 0;
  color: var(--accent-2);
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.sync-status {
  padding: 0.5rem;
  background-color: rgba(56, 189, 248, 0.1);
  border-radius: 4px;
  font-size: 0.85rem;
  color: var(--accent-2);
  margin-bottom: 0.75rem;
  border: 1px solid rgba(56, 189, 248, 0.2);
}
</style>