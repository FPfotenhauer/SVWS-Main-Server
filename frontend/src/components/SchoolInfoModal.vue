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
                <label>SVWS Server:</label>
                <span>{{ schule.svwsServerName }}</span>
              </div>
              <div class="info-item">
                <label>Schema:</label>
                <span>{{ schule.svwsSchema || '-' }}</span>
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineEmits, defineProps } from "vue";

const emit = defineEmits<{
  close: [];
}>();

const props = defineProps<{
  visible: boolean;
  schule: any;
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
  max-width: 600px;
  width: 90%;
}

.info-section {
  margin-bottom: 2rem;
}

.info-section h3 {
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
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
  color: #666;
  margin-bottom: 0.25rem;
}
</style>
