<template>
  <div class="dashboard">
    <div class="dashboard-grid">
      <!-- SVWS Server verwalten -->
      <div class="dashboard-tile" @click="navigateToServerManagement">
        <div class="tile-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
            <line x1="8" y1="21" x2="16" y2="21"></line>
            <line x1="12" y1="17" x2="12" y2="21"></line>
          </svg>
        </div>
        <h3>SVWS Server verwalten</h3>
        <p>Verwalten Sie SVWS Server-Instanzen und deren Konfiguration</p>
      </div>

      <!-- Schulstatistiken -->
      <div class="dashboard-tile">
        <div class="tile-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="20" x2="18" y2="10"></line>
            <line x1="12" y1="20" x2="12" y2="4"></line>
            <line x1="6" y1="20" x2="6" y2="14"></line>
          </svg>
        </div>
        <h3>Schulstatistiken</h3>
        <p>Übersicht über Schuldaten und Statistiken</p>
      </div>

      <!-- IDM Verwaltung -->
      <div class="dashboard-tile">
        <div class="tile-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M22 2l-1 1"></path>
            <path d="M22 8l-1-1"></path>
            <path d="M16 2l1 1"></path>
            <path d="M16 8l1-1"></path>
          </svg>
        </div>
        <h3>IDM Verwaltung</h3>
        <p>Identitäts- und Zugriffsverwaltung</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  navigateToServers: [];
}>();

const navigateToServerManagement = () => {
  emit('navigateToServers');
};
</script>

<style scoped>
.dashboard {
  padding: 2rem 0;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.dashboard-tile {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.dashboard-tile:hover {
  border-color: #f97316;
  background: #374151;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.15);
}

.tile-icon {
  color: #f97316;
  margin-bottom: 0.5rem;
}

.tile-icon svg {
  display: block;
  margin: 0 auto;
}

.dashboard-tile h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f8fafc;
}

.dashboard-tile p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.9rem;
  line-height: 1.5;
  max-width: 250px;
}

@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .dashboard-tile {
    padding: 1.5rem;
    min-height: 180px;
  }
}
</style>