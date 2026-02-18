<template>
  <div class="benutzer-verwaltung">
    <div class="content-wrapper">
      <section class="users-section">
        <h2>Alle Benutzer</h2>
        <div class="section-content">
          <p>Benutzer Verwaltung Funktionalität kommt in Kürze.</p>
          <!-- User management functionality will be added here -->
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
// Component for BenutzerVerwaltung page
// Displays all users and allows management operations
</script>

<style scoped>
.benutzer-verwaltung {
  padding: 2rem 0;
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.users-section {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 2rem;
}

.users-section h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #f8fafc;
  border-bottom: 2px solid #f97316;
  padding-bottom: 0.75rem;
}

.section-content {
  color: #cbd5e1;
  font-size: 1rem;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .benutzer-verwaltung {
    padding: 1.5rem 1rem;
  }

  .content-wrapper {
    gap: 1.5rem;
  }

  .users-section {
    padding: 1.5rem;
  }

  .users-section h2 {
    font-size: 1.25rem;
  }
}
</style>
