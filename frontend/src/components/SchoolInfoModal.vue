<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content school-info-modal">
      <div class="modal-header">
        <h2>Schulinformationen</h2>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>

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

        <div class="info-section">
          <h3>SVWS Informationen</h3>
          <div v-if="loadingSvwsInfo" class="loading">Lade SVWS Informationen...</div>
          <div v-else-if="svwsInfoError" class="error-message">{{ svwsInfoError }}</div>
          <div v-else-if="svwsInfo" class="school-info-content">
            <!-- Basic Information -->
            <div class="info-grid">
              <div class="info-item">
                <label>Schulnummer:</label>
                <span>{{ svwsInfo.schulnummer || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Name:</label>
                <span>{{ svwsInfo.name || '-' }}</span>
              </div>
              <div v-if="svwsInfo.schulnummer2" class="info-item">
                <label>Zweite Schulnummer:</label>
                <span>{{ svwsInfo.schulnummer2 }}</span>
              </div>
              <div v-if="svwsInfo.schulstatus" class="info-item">
                <label>Status:</label>
                <span>{{ svwsInfo.schulstatus }}</span>
              </div>
            </div>

            <!-- Address Information -->
            <div v-if="svwsInfo.strasse || svwsInfo.plz || svwsInfo.ort" class="info-subsection">
              <h4>Adresse</h4>
              <div class="info-grid">
                <div v-if="svwsInfo.strasse" class="info-item">
                  <label>Straße:</label>
                  <span>{{ svwsInfo.strasse }}{{ svwsInfo.hausnummer ? ' ' + svwsInfo.hausnummer : '' }}{{ svwsInfo.hausnummerZusatz ? svwsInfo.hausnummerZusatz : '' }}</span>
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
            <div v-if="svwsInfo.telefon || svwsInfo.fax || svwsInfo.email || svwsInfo.homepage" class="info-subsection">
              <h4>Kontakt</h4>
              <div class="info-grid">
                <div v-if="svwsInfo.telefon" class="info-item">
                  <label>Telefon:</label>
                  <span>{{ svwsInfo.telefon }}</span>
                </div>
                <div v-if="svwsInfo.fax" class="info-item">
                  <label>Fax:</label>
                  <span>{{ svwsInfo.fax }}</span>
                </div>
                <div v-if="svwsInfo.email" class="info-item">
                  <label>E-Mail:</label>
                  <span>{{ svwsInfo.email }}</span>
                </div>
                <div v-if="svwsInfo.homepage" class="info-item">
                  <label>Homepage:</label>
                  <span><a :href="svwsInfo.homepage" target="_blank" rel="noopener noreferrer">{{ svwsInfo.homepage }}</a></span>
                </div>
              </div>
            </div>

            <!-- School Details -->
            <div v-if="svwsInfo.schulform || svwsInfo.schulart || svwsInfo.schulgliederung" class="info-subsection">
              <h4>Schuldetails</h4>
              <div class="info-grid">
                <div v-if="svwsInfo.schulform" class="info-item">
                  <label>Schulform:</label>
                  <span>{{ svwsInfo.schulform }}</span>
                </div>
                <div v-if="svwsInfo.schulart" class="info-item">
                  <label>Schulart:</label>
                  <span>{{ svwsInfo.schulart }}</span>
                </div>
                <div v-if="svwsInfo.schulgliederung" class="info-item">
                  <label>Schulgliederung:</label>
                  <span>{{ svwsInfo.schulgliederung }}</span>
                </div>
              </div>
            </div>

            <!-- Administration -->
            <div v-if="svwsInfo.schulleiter || svwsInfo.schulleiterTelefon || svwsInfo.schulleiterEmail" class="info-subsection">
              <h4>Schulleitung</h4>
              <div class="info-grid">
                <div v-if="svwsInfo.schulleiter" class="info-item">
                  <label>Schulleiter:</label>
                  <span>{{ svwsInfo.schulleiter }}</span>
                </div>
                <div v-if="svwsInfo.schulleiterTelefon" class="info-item">
                  <label>Telefon:</label>
                  <span>{{ svwsInfo.schulleiterTelefon }}</span>
                </div>
                <div v-if="svwsInfo.schulleiterEmail" class="info-item">
                  <label>E-Mail:</label>
                  <span>{{ svwsInfo.schulleiterEmail }}</span>
                </div>
              </div>
            </div>

            <!-- Administrative Information -->
            <div v-if="svwsInfo.kreis || svwsInfo.schulamt || svwsInfo.staat || svwsInfo.kapitel || svwsInfo.satzungsgebendeKommune" class="info-subsection">
              <h4>Verwaltung</h4>
              <div class="info-grid">
                <div v-if="svwsInfo.kreis" class="info-item">
                  <label>Kreis:</label>
                  <span>{{ svwsInfo.kreis }}</span>
                </div>
                <div v-if="svwsInfo.schulamt" class="info-item">
                  <label>Schulamt:</label>
                  <span>{{ svwsInfo.schulamt }}</span>
                </div>
                <div v-if="svwsInfo.staat" class="info-item">
                  <label>Staat:</label>
                  <span>{{ svwsInfo.staat }}</span>
                </div>
                <div v-if="svwsInfo.kapitel" class="info-item">
                  <label>Kapitel:</label>
                  <span>{{ svwsInfo.kapitel }}</span>
                </div>
                <div v-if="svwsInfo.satzungsgebendeKommune" class="info-item">
                  <label>Satzungsgebende Kommune:</label>
                  <span>{{ svwsInfo.satzungsgebendeKommune }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="helper">
            Keine SVWS Informationen verfügbar. Diese Schule muss mit einem SVWS Server verbunden sein.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import api from "../services/api";

const emit = defineEmits<{
  close: [];
}>();

const props = defineProps<{
  visible: boolean;
  schule: any;
}>();

const svwsInfo = ref<any>(null);
const loadingSvwsInfo = ref(false);
const svwsInfoError = ref<string | null>(null);

const closeModal = () => {
  emit('close');
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

watch(() => props.schule, (newSchule) => {
  if (props.visible && newSchule?.id) {
    fetchSvwsInfo();
  }
});
</script>

<style scoped>
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
  background-color: var(--panel);
  border: 2px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border);
}

.modal-header h2 {
  margin: 0;
  color: var(--text);
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.15s ease;
}

.close-btn:hover {
  background-color: var(--panel-hover);
  color: var(--text);
}

.school-info-modal {
  max-width: 700px;
  width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}
.school-info-modal {
  max-width: 700px;
  width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}

.school-info-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-section {
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 1rem;
  background-color: var(--panel);
}

.info-section h3 {
  margin: 0 0 1rem 0;
  color: var(--text);
  font-size: 1.1rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 0.75rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item label {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.info-item span {
  color: var(--text);
  word-break: break-word;
}

.error-message {
  background-color: var(--error-bg, #fee);
  border: 1px solid var(--error-border, #fcc);
  border-radius: 4px;
  padding: 0.75rem;
  color: var(--error-text, #c33);
  white-space: pre-wrap;
}

.info-subsection {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}

.info-subsection h4 {
  margin: 0 0 0.75rem 0;
  color: var(--text);
  font-size: 1rem;
  font-weight: 600;
}
</style>