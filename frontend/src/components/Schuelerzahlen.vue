<template>
  <section class="student-count">
    <header class="overview-header">
      <div>
        <p class="eyebrow">Schülerzahlen</p>
        <h2>Gesamte Übersicht</h2>
        <p>Hier bauen wir eine Tabelle mit allen Schulen, ihren Schülerzahlen und weiteren Kennzahlen auf.</p>
      </div>
      <button class="ghost" type="button" disabled>
        Export vorbereiten
      </button>
    </header>

    <div class="table-placeholder">
      <p>Tabellenansicht mit Schülerzahlen, Begleitdaten und Filtern folgt in der nächsten Iteration.</p>
      <ul>
        <li>Spalten: Schule, Ort, Schulnummer, Schüler insgesamt, Schülerinnen, Schüler, Quelle</li>
        <li>Sortierbare Spalten und Filter für Region/Status</li>
        <li>Live-Aktualisierung nach Updates aus dem Datenbestand</li>
      </ul>
    </div>
  </section>
</template>

<script setup lang="ts"></script>

<style scoped>
.student-count {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  background: #111827;
  border: 1px solid #374151;
  border-radius: 16px;
  padding: 1.5rem;
}

.overview-header h2 {
  margin: 0.4rem 0 0.2rem;
}

.table-placeholder {
  background: #0f172a;
  border: 1px dashed #94a3b8;
  border-radius: 16px;
  padding: 1.5rem;
  color: #e2e8f0;
}

.table-placeholder ul {
  margin: 1rem 0 0;
  padding-left: 1.25rem;
  display: grid;
  gap: 0.35rem;
}
</style>
