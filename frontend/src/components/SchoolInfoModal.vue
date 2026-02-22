<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content school-info-modal">
      <div class="modal-header">
        <h2>Schulinformationen & Anmeldedaten</h2>
        <div class="header-actions">
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
      </div>

      <div class="modal-body" v-if="schule">
        <!-- Schulinformationen vom Server -->
        <div class="info-section">
          <h3>Schulinformationen (vom Server)</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>Schulnummer:</label>
              <span>{{ schule.schulnummer || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Name:</label>
              <span>{{ schule.name || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Ort:</label>
              <span>{{ schule.ort || '-' }}</span>
            </div>
            <div class="info-item">
              <label>PLZ:</label>
              <span>{{ schule.plz || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Schema:</label>
              <span>{{ schule.schema || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Anmeldedaten -->
        <div class="info-section">
          <h3>Anmeldedaten</h3>
          
          <!-- Wenn keine Daten gespeichert: Formular zum Einfügen -->
          <div v-if="!hasCredentials && !isEditing" class="credentials-form">
            <p class="info-text">Noch keine Anmeldedaten gespeichert. Geben Sie Username und Passwort ein:</p>
            <div class="form-row">
              <div class="form-group">
                <label for="username">API-Benutzer:</label>
                <input 
                  type="text" 
                  id="username" 
                  v-model="formData.svwsUsername" 
                  placeholder="API-Benutzer"
                  :disabled="saving"
                />
              </div>
              <div class="form-group">
                <label for="password">Passwort:</label>
                <input 
                  type="password" 
                  id="password" 
                  v-model="formData.svwsPassword" 
                  placeholder="Passwort"
                  :disabled="saving"
                />
              </div>
              <button 
                type="button" 
                class="save-button"
                @click="saveCredentials"
                :disabled="saving || isLoadingStore || !formData.svwsUsername || (!formData.svwsPassword && !config.ALLOW_EMPTY_PASSWORD)"
              >
                {{ saving ? 'Speichern...' : 'Speichern' }}
              </button>
            </div>
          </div>

          <!-- Wenn Daten gespeichert und nicht im Editier-Modus -->
          <div v-else-if="hasCredentials && !isEditing" class="credentials-display">
            <div class="credentials-info">
              <div class="info-item">
                <label>API-Benutzer:</label>
                <span>{{ managedSchoolInfo?.svwsUsername }}</span>
              </div>
              <div v-if="managedSchoolInfo?.createdAt" class="info-item">
                <label>Gespeichert am:</label>
                <span>{{ formatted(managedSchoolInfo.createdAt) }}</span>
              </div>
            </div>
            <div class="button-group">
              <button 
                type="button" 
                class="edit-button"
                @click="startEdit"
              >
                Bearbeiten
              </button>
              <button 
                type="button" 
                class="delete-button"
                @click="deleteSchool"
                :disabled="saving || isLoadingStore"
              >
                {{ saving ? 'Schule löschen...' : 'Schule löschen' }}
              </button>
            </div>
          </div>

          <!-- Editier-Modus -->
          <div v-else-if="isEditing" class="credentials-form">
            <p class="info-text">Anmeldedaten bearbeiten:</p>
            <div class="form-row">
              <div class="form-group">
                <label for="username-edit">API-Benutzer:</label>
                <input 
                  type="text" 
                  id="username-edit" 
                  v-model="formData.svwsUsername" 
                  placeholder="API-Benutzer"
                  :disabled="saving"
                />
              </div>
              <div class="form-group">
                <label for="password-edit">Passwort:</label>
                <input 
                  type="password" 
                  id="password-edit" 
                  v-model="formData.svwsPassword" 
                  placeholder="Passwort (leer = nicht ändern)"
                  :disabled="saving"
                />
              </div>
              <button 
                type="button" 
                class="save-button"
                @click="saveCredentials"
                :disabled="saving || isLoadingStore || !formData.svwsUsername"
              >
                {{ saving ? 'Speichern...' : 'Speichern' }}
              </button>
              <button 
                type="button" 
                class="cancel-button"
                @click="cancelEdit"
                :disabled="saving || isLoadingStore"
              >
                Abbrechen
              </button>
            </div>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { useSchulenStore } from '../stores/schulen';
import { useSvwsServersStore } from '../stores/svwsServers';
import api from '../services/api';
import { config } from '../config';

const emit = defineEmits<{
  close: [];
  schoolSaved: [];
}>();

const props = defineProps<{
  visible: boolean;
  schule: any;
}>();

const schulenStore = useSchulenStore();
const svwsServersStore = useSvwsServersStore();
const isEditing = ref(false);
const saving = ref(false);
const error = ref('');
const success = ref('');
const hadCredentialsOnOpen = ref(false);
const credentialIdToUpdate = ref<string | null>(null);
const isLoadingStore = ref(false);  // WICHTIG: Track store load state!

const formData = reactive({
  svwsUsername: '',
  svwsPassword: ''
});

// Prüfe ob bereits Anmeldedaten vorhanden sind
const managedSchoolInfo = computed(() => {
  const found = schulenStore.items.find(
    s => s.svwsSchema === props.schule?.schema && 
         s.svwsServerId === svwsServersStore.selectedServer?.id &&
         s.svwsUsername !== null && s.svwsUsername !== undefined
  );
  console.log(`[SchoolInfoModal] managedSchoolInfo COMPUTED: schema=${props.schule?.schema}, serverId=${svwsServersStore.selectedServer?.id}, found=${!!found}, username=${found?.svwsUsername || 'null'}, id=${found?.id}`);
  return found;
});

const hasCredentials = computed(() => {
  // hasCredentials basiert auf managedSchoolInfo (mit dem NULL-check)
  // credentialIdToUpdate wird trotzdem für UPDATE used
  const hasId = !!managedSchoolInfo.value;
  console.log(`[SchoolInfoModal] hasCredentials COMPUTED: managedSchoolInfo=${!!managedSchoolInfo.value}, username=${managedSchoolInfo.value?.svwsUsername}, hasCredentials=${hasId}`);
  return hasId;
});

// Lade Formulardaten wenn Modal öffnet
watch(() => [props.visible, props.schule], async (newVal) => {
  if (props.visible && props.schule) {
    // WICHTIG: Store muss aktuell sein!
    // Der Parent hat bereits geladen, aber wir reloaden zur Sicherheit
    isLoadingStore.value = true;
    console.log('[SchoolInfoModal] Modal opened - reloading store');
    console.log(`  - props.schule.id from parent: ${props.schule.id}`);
    console.log(`  - props.schule.hasCredentials from parent: ${props.schule.hasCredentials}`);
    
    await schulenStore.load();
    isLoadingStore.value = false;
    
    console.log(`[SchoolInfoModal] After store reload:`);
    console.log(`  - Store contains ${schulenStore.items.length} items`);
    console.log(`  - Store items:`, schulenStore.items.map(s => ({ schema: s.svwsSchema, serverId: s.svwsServerId, id: s.id })));
    
    // Jetzt können wir sicher sein dass die ID korrekt ist
    const idFromParent = props.schule.id;
    let idToUpdate: string | null = null;
    
    if (idFromParent) {
      idToUpdate = idFromParent;
      console.log(`[SchoolInfoModal] Using ID from PARENT: ${idFromParent}`);
    } else {
      // Suche die Schule im Store - mit ODER ohne Credentials (du brauchst die ID zum UPDATE)
      const schoolInStore = schulenStore.items.find(
        s => s.svwsSchema === props.schule.schema && 
             s.svwsServerId === svwsServersStore.selectedServer?.id
      );
      idToUpdate = schoolInStore?.id || null;
      console.log(`[SchoolInfoModal] No ID from parent, searching store:`);
      console.log(`  - schema=${props.schule.schema}, serverId=${svwsServersStore.selectedServer?.id}`);
      console.log(`  - found in store: ${idToUpdate || 'NONE'}`);
    }
    
    credentialIdToUpdate.value = idToUpdate;
    hadCredentialsOnOpen.value = !!idToUpdate;
    
    console.log(`[SchoolInfoModal] Final state:`);
    console.log(`  - credentialIdToUpdate: ${idToUpdate}`);
    console.log(`  - hasCredentials: ${!!idToUpdate}`);
    
    formData.svwsUsername = managedSchoolInfo.value?.svwsUsername || props.schule.svwsUsername || '';
    formData.svwsPassword = '';
  }
  isEditing.value = false;
  error.value = '';
  success.value = '';
}, { immediate: true, deep: true });

const closeModal = () => {
  emit('close');
};

const startEdit = () => {
  isEditing.value = true;
  formData.svwsPassword = '';
  error.value = '';
};

const cancelEdit = () => {
  isEditing.value = false;
  formData.svwsUsername = managedSchoolInfo.value?.svwsUsername || '';
  formData.svwsPassword = '';
  error.value = '';
};

const saveCredentials = async () => {
  // Guard gegen doppeltes Klicken
  if (saving.value || isLoadingStore.value) {
    console.log('[SchoolInfoModal] Save already in progress, ignoring click');
    return;
  }
  
  if (!formData.svwsUsername) {
    error.value = 'Username erforderlich';
    return;
  }

  // Für neue Einträge: Passwort erforderlich (außer im Test-Modus)
  if (!hadCredentialsOnOpen.value && !formData.svwsPassword && !config.ALLOW_EMPTY_PASSWORD) {
    error.value = 'Passwort erforderlich';
    return;
  }

  error.value = '';
  success.value = '';
  saving.value = true;

  try {
    // WICHTIG: Das Modal darf NIEMALS CREATE machen!
    // Das ist nur für Editing bestehender Credentials
    // Wenn credentialIdToUpdate null ist, ist es ein Fehler!
    if (!credentialIdToUpdate.value) {
      error.value = 'FEHLER: Schule nicht gefunden in der Datenbank! Diese Schule hat noch keine Anmeldedaten.';
      console.error('[SchoolInfoModal] CRITICAL: credentialIdToUpdate is null! Cannot create from edit modal!');
      saving.value = false;
      return;
    }

    // GET svwsServerId from the store - this is GUARANTEED to be correct
    const svwsServerId = svwsServersStore.selectedServer?.id;
    
    if (!svwsServerId) {
      error.value = 'ERROR: SVWS Server ID nicht gefunden!';
      saving.value = false;
      return;
    }
    
    const payload: any = {
      svwsServerId: svwsServerId,
      svwsSchema: props.schule.schema,
      svwsUsername: formData.svwsUsername
    };

    // Nur Passwort senden wenn gesetzt (bei Edit optional)
    if (formData.svwsPassword) {
      payload.svwsPassword = formData.svwsPassword;
    }

    // ALWAYS UPDATE - this modal is only for editing!
    console.log(`[SchoolInfoModal] Executing UPDATE for ID: ${credentialIdToUpdate.value}`);
    await api.put(`/api/schulen/${credentialIdToUpdate.value}`, payload);
    success.value = 'Anmeldedaten aktualisiert!';

    isEditing.value = false;
    formData.svwsPassword = '';
    
    // Reload store
    await schulenStore.load();
    emit('schoolSaved');
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || 'Fehler beim Speichern';
  } finally {
    saving.value = false;
  }
};

const deleteSchool = async () => {
  console.log('[SchoolInfoModal] deleteSchool called');
  console.log('[SchoolInfoModal] credentialIdToUpdate:', credentialIdToUpdate.value);
  console.log('[SchoolInfoModal] props.schule:', props.schule);
  
  if (!credentialIdToUpdate.value) {
    console.error('[SchoolInfoModal] No credentialIdToUpdate - cannot delete');
    error.value = 'Keine Schule zum Löschen gefunden';
    return;
  }

  const schemaName = props.schule?.schema || 'diese Schule';
  const confirmMsg = `WARNUNG: Diese Aktion löscht die Schule "${schemaName}" PERMANENT vom SVWS-Server und aus der Datenbank!\n\n` +
    `Alle Daten dieser Schule werden unwiederbringlich gelöscht.\n\n` +
    `Möchten Sie fortfahren?`;
  
  console.log('[SchoolInfoModal] Showing confirmation dialog');
  if (!confirm(confirmMsg)) {
    console.log('[SchoolInfoModal] User cancelled deletion');
    return;
  }

  console.log('[SchoolInfoModal] User confirmed deletion');
  error.value = '';
  success.value = '';
  saving.value = true;

  try {
    console.log(`[SchoolInfoModal] Calling schulenStore.delete with ID: ${credentialIdToUpdate.value}`);
    await schulenStore.delete(credentialIdToUpdate.value);
    success.value = 'Schule erfolgreich gelöscht!';
    
    // Close modal after short delay
    setTimeout(() => {
      closeModal();
    }, 1500);
    
    emit('schoolSaved');
  } catch (e: any) {
    console.error('[SchoolInfoModal] Delete failed:', e);
    error.value = e.response?.data?.message || e.message || 'Fehler beim Löschen der Schule';
  } finally {
    saving.value = false;
  }
};

const deleteCredentials = async () => {
  if (!credentialIdToUpdate.value) {
    error.value = 'Keine Daten zum Löschen';
    return;
  }

  if (!confirm('Anmeldedaten wirklich löschen?')) {
    return;
  }

  error.value = '';
  success.value = '';
  saving.value = true;

  try {
    console.log(`[SchoolInfoModal] Deleting credential with ID: ${credentialIdToUpdate.value}`);
    await api.delete(`/api/schulen/${credentialIdToUpdate.value}`);
    success.value = 'Anmeldedaten gelöscht!';
    
    await schulenStore.load();
    emit('schoolSaved');
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || 'Fehler beim Löschen';
  } finally {
    saving.value = false;
  }
};

const formatted = (value?: string | null) => {
  if (!value) return "-";
  return new Date(value).toLocaleString();
};
</script>

<style scoped>
.school-info-modal {
  max-width: 900px;
  width: 90%;
  max-height: 85vh;
  overflow-y: auto;
}

.school-info-modal .modal-header {
  padding: 12px 24px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.school-info-modal .modal-header h2 {
  font-size: 1.25rem;
  margin: 0;
}

.school-info-modal .modal-body {
  padding: 24px;
}

.info-section {
  margin-bottom: 2rem;
}

.info-section h3 {
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
  margin-top: 0;
  color: var(--accent-2);
  font-size: 1.1rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item label {
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--muted);
  margin-bottom: 0.25rem;
}

.info-item span {
  font-size: 0.95rem;
  color: var(--text);
}

.credentials-form {
  background: rgba(31, 41, 55, 0.3);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.info-text {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: var(--muted);
}

.form-row {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 150px;
}

.form-group label {
  font-weight: 600;
  font-size: 0.8rem;
  color: var(--muted);
  margin-bottom: 0.3rem;
}

.form-group input {
  padding: 0.5rem 0.75rem;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 4px;
  background: rgba(31, 41, 55, 0.5);
  color: var(--text);
  font-family: inherit;
  font-size: 0.9rem;
  transition: all 0.15s ease;
}

.form-group input:focus {
  outline: none;
  border-color: var(--accent-2);
  background: rgba(31, 41, 55, 0.8);
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.1);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.credentials-display {
  background: rgba(31, 41, 55, 0.3);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.credentials-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.button-group {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.save-button,
.edit-button,
.delete-button,
.cancel-button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  flex-shrink: 0;
}

.save-button {
  background: var(--accent);
  color: #0b0f19;
}

.save-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.save-button:active:not(:disabled) {
  transform: translateY(0) scale(0.98);
}

.save-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.edit-button {
  background: rgba(148, 163, 184, 0.2);
  color: var(--text);
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.edit-button:hover {
  background: rgba(148, 163, 184, 0.3);
  transform: translateY(-1px);
}

.delete-button {
  background: rgba(220, 38, 38, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(220, 38, 38, 0.3);
}

.delete-button:hover:not(:disabled) {
  background: rgba(220, 38, 38, 0.3);
  transform: translateY(-1px);
}

.delete-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-button {
  background: rgba(107, 114, 128, 0.2);
  color: var(--text);
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.cancel-button:hover:not(:disabled) {
  background: rgba(107, 114, 128, 0.3);
}

.cancel-button:disabled {
  opacity: 0.6;
}

.error-message {
  background: rgba(220, 38, 38, 0.1);
  color: #fca5a5;
  border: 1px solid rgba(220, 38, 38, 0.3);
  padding: 0.75rem;
  border-radius: 4px;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.success-message {
  background: rgba(34, 197, 94, 0.1);
  color: #86efac;
  border: 1px solid rgba(34, 197, 94, 0.3);
  padding: 0.75rem;
  border-radius: 4px;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}
</style>
