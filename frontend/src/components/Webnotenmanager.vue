<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useWebnotenmanagerStore } from "../stores/webnotenmanager";
import { useSchulenStore } from "../stores/schulen";
import type { WebnotenmanagerWithSchuleData } from "../types/webnotenmanager";
import api from "../services/api";
import type { SchuleStammdatenResponse } from "../types/schule";

const webnotenmanagerStore = useWebnotenmanagerStore();
const schulenStore = useSchulenStore();

// Track which config is being edited or created
const editingId = ref<string | null>(null);
const creatingSchuleId = ref<string | null>(null);
const editingData = ref<{
  notenserverBaseUrl: string;
  oauthSecretEncrypted: string;
} | null>(null);

const isSaving = ref(false);
const successMessage = ref("");
const errorMessage = ref("");

// Store stammdaten for each school
const stammdatenBySchuleId = ref<Map<string, SchuleStammdatenResponse>>(new Map());
const isLoadingStammdaten = ref(false);

// Load all schools and webnotenmanager configurations on component mount
onMounted(async () => {
  await schulenStore.load();
  await webnotenmanagerStore.load();
  await loadAllStammdaten();
});

// Load stammdaten for all schools in one API call
const loadAllStammdaten = async () => {
  isLoadingStammdaten.value = true;
  try {
    const response = await api.get<SchuleStammdatenResponse[]>('/api/schulen/stammdaten');
    // Build map for quick lookup by id
    for (const stammdaten of response.data) {
      stammdatenBySchuleId.value.set(stammdaten.id, stammdaten);
    }
  } catch (err) {
    console.error('Failed to load stammdaten:', err);
  } finally {
    isLoadingStammdaten.value = false;
  }
};

// Map schools to include their webnotenmanager config if it exists
const schoolsWithConfigs = computed(() => {
  return schulenStore.items.map(school => {
    const config = webnotenmanagerStore.items.find(c => c.schuleId === school.id);
    const stammdaten = stammdatenBySchuleId.value.get(school.id);
    
    return {
      schuleId: school.id,
      svwsServerId: school.svwsServerId,
      svwsServerName: school.svwsServerName,
      svwsSchema: school.svwsSchema,
      config: config || null,
      schulnummer: stammdaten?.schulnummer || "-",
      schulname: stammdaten?.bezeichnung1 || "-"
    };
  });
});

// Start editing a configuration
const startEditing = (config: WebnotenmanagerWithSchuleData) => {
  editingId.value = config.id;
  creatingSchuleId.value = null;
  editingData.value = {
    notenserverBaseUrl: config.notenserverBaseUrl,
    oauthSecretEncrypted: config.oauthSecretEncrypted
  };
  successMessage.value = "";
  errorMessage.value = "";
};

// Start creating a configuration
const startCreating = (schuleId: string) => {
  creatingSchuleId.value = schuleId;
  editingId.value = null;
  editingData.value = {
    notenserverBaseUrl: "",
    oauthSecretEncrypted: ""
  };
  successMessage.value = "";
  errorMessage.value = "";
};

// Cancel editing/creating
const cancelEditing = () => {
  editingId.value = null;
  creatingSchuleId.value = null;
  editingData.value = null;
  successMessage.value = "";
  errorMessage.value = "";
};

// Save changes or create new
const saveConfig = async () => {
  if (!editingData.value) return;
  
  isSaving.value = true;
  errorMessage.value = "";
  successMessage.value = "";
  
  try {
    if (creatingSchuleId.value) {
      // Create new configuration
      await webnotenmanagerStore.create(creatingSchuleId.value, {
        notenserverBaseUrl: editingData.value.notenserverBaseUrl,
        oauthSecretEncrypted: editingData.value.oauthSecretEncrypted
      });
      successMessage.value = "Konfiguration erfolgreich erstellt";
    } else if (editingId.value) {
      // Update existing configuration
      await webnotenmanagerStore.update(editingId.value, {
        notenserverBaseUrl: editingData.value.notenserverBaseUrl,
        oauthSecretEncrypted: editingData.value.oauthSecretEncrypted
      });
      successMessage.value = "Konfiguration erfolgreich gespeichert";
    }
    
    setTimeout(() => {
      successMessage.value = "";
      editingId.value = null;
      creatingSchuleId.value = null;
      editingData.value = null;
    }, 2000);
  } catch (err) {
    errorMessage.value = `Fehler beim Speichern: ${err instanceof Error ? err.message : 'Unbekannter Fehler'}`;
  } finally {
    isSaving.value = false;
  }
};

// Delete a configuration
const deleteConfig = async (id: string) => {
  if (!confirm("Möchten Sie diese Konfiguration wirklich löschen?")) return;
  
  try {
    await webnotenmanagerStore.delete(id);
    successMessage.value = "Konfiguration gelöscht";
    setTimeout(() => {
      successMessage.value = "";
    }, 2000);
  } catch (err) {
    errorMessage.value = `Fehler beim Löschen: ${err instanceof Error ? err.message : 'Unbekannter Fehler'}`;
  }
};
</script>

<template>
  <section class="page">
    <div class="card">
      <h2>Webnotenmanager</h2>
      <p>
        Verwalten Sie die Verbindungen zu Ihren Notenservern für jede Schule.
      </p>
    </div>

    <!-- Messages -->
    <div v-if="successMessage" class="message success">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="message error">
      {{ errorMessage }}
    </div>

    <!-- Configurations List -->
    <section class="panel">
      <h3>Schulen und Notenserver-Konfigurationen</h3>
      
      <div v-if="schulenStore.loading" class="loading">
        Schulen werden geladen...
      </div>
      
      <div v-else-if="schoolsWithConfigs.length === 0" class="info">
        Keine Schulen vorhanden
      </div>

      <div v-else class="configs-list">
        <!-- Compact Table View -->
        <table class="schools-table">
          <thead>
            <tr>
              <th>Schulname</th>
              <th>Schulnummer</th>
              <th>Server (Schema)</th>
              <th>Base URL</th>
              <th>Status</th>
              <th class="actions-col">Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="school in schoolsWithConfigs" :key="school.schuleId" class="school-row" :class="{ 'has-config': !!school.config, 'no-config': !school.config }">
              <td class="school-name" :title="school.schulname">{{ school.schulname || "-" }}</td>
              <td class="school-number">{{ school.schulnummer || "-" }}</td>
              <td class="server-info">{{ school.svwsServerName }} ({{ school.svwsSchema }})</td>
              <td class="base-url">
                <span v-if="school.config" class="url-display">{{ school.config.notenserverBaseUrl }}</span>
                <span v-else class="empty-cell">-</span>
              </td>
              <td class="status">
                <span v-if="school.config" class="status-badge configured">Konfiguriert</span>
                <span v-else class="status-badge empty">Nicht konfiguriert</span>
              </td>
              <td class="actions-cell">
                <div v-if="editingId === school.config?.id || creatingSchuleId === school.schuleId" class="edit-mode-indicator">
                  <span class="editing-text">Bearbeitung...</span>
                </div>
                <div v-else class="action-buttons">
                  <button 
                    v-if="school.config" 
                    class="btn-icon edit" 
                    @click="startEditing(school.config)" 
                    title="Bearbeiten"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                  </button>
                  <button 
                    v-if="school.config" 
                    class="btn-icon delete" 
                    @click="deleteConfig(school.config.id)" 
                    title="Löschen"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      <line x1="10" y1="11" x2="10" y2="17"></line>
                      <line x1="14" y1="11" x2="14" y2="17"></line>
                    </svg>
                  </button>
                  <button 
                    v-else 
                    class="btn-icon add" 
                    @click="startCreating(school.schuleId)" 
                    title="Hinzufügen"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="12" y1="5" x2="12" y2="19"></line>
                      <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Inline Edit Form -->
        <div v-if="editingId || creatingSchuleId" class="edit-form-container">
          <div class="edit-form-backdrop" @click="cancelEditing"></div>
          <div class="edit-form-modal">
            <div class="modal-header">
              <h3>{{ creatingSchuleId ? 'Neue Konfiguration' : 'Konfiguration bearbeiten' }}</h3>
              <button class="btn-close" @click="cancelEditing" type="button">×</button>
            </div>
            <form @submit.prevent="saveConfig" class="compact-form">
              <div class="form-group">
                <label>Notenserver Base URL:</label>
                <input
                  v-model="editingData!.notenserverBaseUrl"
                  type="url"
                  class="form-input"
                  required
                  placeholder="https://example.com"
                />
              </div>

              <div class="form-group">
                <label>OAuth-Secret:</label>
                <input
                  v-model="editingData!.oauthSecretEncrypted"
                  type="password"
                  class="form-input"
                  required
                  placeholder="OAuth-Secret"
                />
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-primary" :disabled="isSaving">
                  {{ isSaving ? 'Speichern...' : 'Speichern' }}
                </button>
                <button type="button" class="btn-secondary" @click="cancelEditing" :disabled="isSaving">
                  Abbrechen
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.page {
  padding: 2rem 0;
}

.card {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 900px;
  margin-bottom: 2rem;
}

.card h2 {
  margin: 0 0 0.75rem 0;
  color: #f8fafc;
}

.card p {
  margin: 0;
  color: #94a3b8;
  line-height: 1.5;
}

.message {
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
  font-weight: 500;
}

.message.success {
  background: #065f46;
  color: #d1fae5;
  border: 1px solid #10b981;
}

.message.error {
  background: #7f1d1d;
  color: #fecaca;
  border: 1px solid #ef4444;
}

.panel {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 1.5rem;
}

.panel h3 {
  margin: 0 0 1.5rem 0;
  color: #f8fafc;
  font-size: 1.25rem;
}

.loading,
.info {
  color: #94a3b8;
  padding: 1rem;
  text-align: center;
}

.configs-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.schools-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.schools-table thead {
  background: #111827;
  border-bottom: 2px solid #374151;
}

.schools-table th {
  padding: 0.75rem;
  text-align: left;
  color: #e5e7eb;
  font-weight: 600;
  white-space: nowrap;
}

.schools-table th.actions-col {
  text-align: center;
  width: 120px;
}

.schools-table tbody tr {
  border-bottom: 1px solid #374151;
  transition: background-color 0.15s;
}

.schools-table tbody tr:hover {
  background: #1f2937;
}

.schools-table td {
  padding: 0.75rem;
  color: #d1d5db;
  vertical-align: middle;
}

.school-row.has-config td {
  color: #e5e7eb;
}

.school-row.no-config td {
  color: #9ca3af;
}

.school-name {
  font-weight: 500;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}

.school-number {
  text-align: center;
  min-width: 80px;
}

.server-info {
  font-size: 0.85rem;
  color: #9ca3af;
}

.base-url {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.85rem;
  font-family: monospace;
}

.url-display {
  color: #60a5fa;
}

.empty-cell {
  color: #6b7280;
}

.status {
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.configured {
  background: #065f46;
  color: #d1fae5;
}

.status-badge.empty {
  background: #374151;
  color: #9ca3af;
}

.actions-cell {
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 0.25rem;
  justify-content: center;
  flex-wrap: nowrap;
}

.btn-icon {
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.btn-icon svg {
  width: 18px;
  height: 18px;
}

.btn-icon.edit {
  background: #3b82f6;
  color: white;
}

.btn-icon.edit:hover {
  background: #2563eb;
}

.btn-icon.delete {
  background: #ef4444;
  color: white;
}

.btn-icon.delete:hover {
  background: #dc2626;
}

.btn-icon.add {
  background: #10b981;
  color: white;
}

.btn-icon.add:hover {
  background: #059669;
}

.edit-mode-indicator {
  font-size: 0.8rem;
  color: #fbbf24;
  font-weight: 500;
}

.editing-text {
  display: inline-block;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.edit-form-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.edit-form-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  cursor: pointer;
}

.edit-form-modal {
  position: relative;
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3);
  z-index: 1001;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #374151;
  padding-bottom: 1rem;
}

.modal-header h3 {
  margin: 0;
  color: #f8fafc;
  font-size: 1.1rem;
}

.btn-close {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.15s;
}

.btn-close:hover {
  color: #e5e7eb;
}

.compact-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  color: #e5e7eb;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-input {
  padding: 0.6rem;
  background: #111827;
  border: 1px solid #374151;
  border-radius: 6px;
  color: #f8fafc;
  font-size: 0.9rem;
  transition: border-color 0.15s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.form-input::placeholder {
  color: #6b7280;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.btn-primary,
.btn-secondary {
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.15s;
  flex: 1;
}

.btn-primary {
  background: #10b981;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #059669;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #4b5563;
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
