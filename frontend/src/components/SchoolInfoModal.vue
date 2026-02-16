<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content school-info-modal">
      <div class="modal-header">
        <h2>Schulinformationen</h2>
        <div class="header-actions">
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
      </div>

      <div class="modal-body">
        <div v-if="schule" class="school-info-content">
          <div class="info-section">
            <h3>Allgemeine Informationen</h3>
            <div class="info-grid">
              <div class="info-item">
                <label>Schulnummer:</label>
                <span>{{ schule.schulnummer }}</span>
              </div>
              <div class="info-item">
                <label>Name:</label>
                <span>{{ schule.name }}</span>
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
              <div class="info-item">
                <label>SVWS Server:</label>
                <span>{{ serverName || '-' }}</span>
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

          <div class="info-section">
            <h3>Benutzerdaten der Schule</h3>
            <div class="form-row">
              <div class="form-group">
                <label for="username">API-Benutzer:</label>
                <input type="text" id="username" placeholder="API-Benutzer" />
              </div>
              <div class="form-group">
                <label for="password">Passwort:</label>
                <input type="password" id="password" placeholder="Passwort" />
              </div>
              <button type="button" class="save-button">Speichern</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  close: [];
}>();

const props = defineProps<{
  visible: boolean;
  schule: any;
  serverName?: string;
}>();

const closeModal = () => {
  emit('close');
};

const formatted = (value?: string | null) => {
  if (!value) return "-";
  return new Date(value).toLocaleString();
};
</script>

<style scoped>
.school-info-modal {
  max-width: 800px;
  width: 90%;
  max-height: 85vh;
}

.school-info-modal .modal-header {
  padding: 12px 24px;
}

.school-info-modal .modal-header h2 {
  font-size: 1.25rem;
  margin: 0;
}

.school-info-modal .modal-body {
  padding: 12px 24px;
}

.info-section {
  margin-bottom: 1rem;
}

.info-section h3 {
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  padding-bottom: 0.25rem;
  margin-bottom: 0.75rem;
  margin-top: 0;
  color: var(--accent-2);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
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

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  flex: 1;
  min-width: 0;
}

.form-row {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
}

.form-group label {
  font-weight: 600;
  font-size: 0.8rem;
  color: var(--muted);
  margin-bottom: 0.3rem;
}

.form-group input {
  padding: 0.35rem 0.5rem;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 4px;
  background: rgba(31, 41, 55, 0.5);
  color: var(--text);
  font-family: inherit;
  font-size: 0.9rem;
}

.form-group input:focus {
  outline: none;
  border-color: var(--accent-2);
  background: rgba(31, 41, 55, 0.8);
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.1);
}

.save-button {
  padding: 0.35rem 0.75rem;
  background: var(--accent);
  color: #0b0f19;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  flex-shrink: 0;
}

.save-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.save-button:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.2);
}
</style>
