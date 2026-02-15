<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content school-creation-modal">
      <div class="modal-header">
        <h2>Neue Schule erstellen</h2>
        <button class="close-btn" @click="closeModal">&times;</button>
        <p class="text-muted">Wählen Sie eine Option für die Erstellung einer neuen Schule:</p>
      </div>

      <div class="creation-options">
        <button
          type="button"
          class="creation-option primary large"
          @click="handleCreateEmptySchema"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 5v14"></path>
            <path d="M5 12h14"></path>
          </svg>
          <div class="option-content">
            <h3>Leeres Schema anlegen</h3>
            <p>Erstellt eine neue Schule mit einem leeren Datenbankschema</p>
          </div>
        </button>

        <button
          type="button"
          class="creation-option secondary large"
          @click="handleImportBackup"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          <div class="option-content">
            <h3>Backup importieren</h3>
            <p>Importiert eine Schule aus einem vorhandenen Backup</p>
          </div>
        </button>

        <button
          type="button"
          class="creation-option secondary large"
          @click="handleMigrateDatabase"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
            <path d="m21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
            <path d="m3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
            <path d="m21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
          </svg>
          <div class="option-content">
            <h3>SchILD-NRW2 Datenbank migrieren</h3>
            <p>Migriert eine bestehende SchILD-NRW2 Datenbank</p>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  close: [];
  createEmptySchema: [];
  importBackup: [];
  migrateDatabase: [];
}>();

const props = defineProps<{
  visible: boolean;
}>();

const closeModal = () => {
  emit('close');
};

const handleCreateEmptySchema = () => {
  emit('createEmptySchema');
  closeModal();
};

const handleImportBackup = () => {
  emit('importBackup');
  closeModal();
};

const handleMigrateDatabase = () => {
  emit('migrateDatabase');
  closeModal();
};
</script>

<style scoped>
.school-creation-modal {
  max-width: 500px;
  width: 90vw;
}

.creation-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
}

.creation-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid #374151;
  border-radius: 8px;
  background-color: #1f2937;
  color: var(--text, #f8fafc);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
  font-family: inherit;
  font-size: inherit;
  min-height: 60px;
}

.creation-option:hover {
  border-color: #f97316;
  background-color: #374151;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
}

.creation-option.primary:hover {
  border-color: #f97316;
  background-color: rgba(249, 115, 22, 0.1);
}

.creation-option.secondary:hover {
  border-color: #6c757d;
  background-color: rgba(108, 117, 125, 0.1);
}

.creation-option svg {
  flex-shrink: 0;
  color: #f97316;
  width: 24px;
  height: 24px;
}

.creation-option.secondary svg {
  color: #94a3b8;
}

.option-content h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #f8fafc;
}

.option-content p {
  margin: 0;
  font-size: 0.85rem;
  color: #94a3b8;
  line-height: 1.4;
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
  padding: 1rem;
}

.modal-content {
  background: #111827;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.45);
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  padding: 24px;
  max-width: 500px;
  width: 90%;
}

.modal-header {
  padding: 0 0 1rem 0;
  border-bottom: 1px solid #374151;
  position: relative;
  margin-bottom: 1rem;
}

.modal-header h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f8fafc;
}

.close-btn {
  position: absolute;
  top: 0;
  right: 0;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #94a3b8;
  padding: 0.25rem;
  line-height: 1;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background-color: #374151;
  color: #f8fafc;
}

.text-muted {
  color: #94a3b8;
  margin: 0.5rem 0 0 0;
  font-size: 0.9rem;
}
</style>