<template>
  <section class="school-statistics">
    <div class="stat-card">
      <p class="eyebrow">Auswertung der Schuldaten</p>
      <h2>Schulträger Dashbord</h2>
      <p>
        Hier entsteht eine Übersicht über relevante Kennzahlen zu Schulen, inklusive Filter
        nach Regionen, Entwicklungen und Vergleichsdaten. In der nächsten Iteration liefern wir
        echte Daten und Interaktionen.
      </p>
    </div>
    <div class="stat-grid">
      <article class="stat-block">
        <header>
          <div class="stat-details">
            <h3>Anzahl SVWS-Server</h3>
            <div class="status-line">
              <span class="status-pill" :class="statusVariant"></span>
              <span>{{ statusLabel }}</span>
            </div>
          </div>
          <button type="button" class="badge badge-link" @click="navigateToSvwsManager">
            {{ serverCount }}
          </button>
        </header>
        <p>Die Anzahl kommt direkt von der SVWS-Server-API; der Status zeigt, ob alle Verbindungen grün sind.</p>
      </article>
      <article class="stat-block">
        <header>
          <h3>Anzahl der Schulen</h3>
          <span class="badge">{{ schoolCount }}</span>
        </header>
        <p>Gesamtsumme aller Schulen, die über die /api/schulen-API erfasst sind.</p>
      </article>
      <article class="stat-block">
        <header>
          <h3>Schülerzahlen</h3>
          <button type="button" class="badge badge-link" @click="navigateToSchuelerzahlen">
            Bericht öffnen
          </button>
        </header>
        <p>Demnächst eine Tabelle zu allen Schulen mit Schülerzahlen und weiteren Kennwerten.</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useSvwsServersStore } from "../stores/svwsServers";
import { useSchulenStore } from "../stores/schulen";

const store = useSvwsServersStore();
const schulenStore = useSchulenStore();

const serverCount = computed(() => store.servers.length);
const allConnected = computed(
  () => serverCount.value > 0 && store.servers.every((server) => server.status === "CONNECTED")
);

const statusVariant = computed(() => {
  if (store.loading) return "loading";
  if (!serverCount.value) return "disabled";
  return allConnected.value ? "success" : "warn";
});

const statusLabel = computed(() => {
  if (store.loading) return "Lade SVWS-Server ...";
  if (!serverCount.value) return "Keine SVWS-Server";
  return allConnected.value ? "Alle verbunden" : "Verbindungen prüfen";
});

const schoolCount = computed(() => schulenStore.items.length);

const navigateToSvwsManager = () => {
  window.dispatchEvent(new Event("navigate-to-servers"));
};

const navigateToSchuelerzahlen = () => {
  window.dispatchEvent(new Event("navigate-to-schuelerzahlen"));
};

onMounted(() => {
  if (!store.servers.length && !store.loading) {
    store.loadServers();
  }
  if (!schulenStore.items.length && !schulenStore.loading) {
    schulenStore.load();
  }
});
</script>

<style scoped>
.school-statistics {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.stat-card {
  background: linear-gradient(135deg, #111827, #1f2937);
  border: 1px solid rgba(249, 115, 22, 0.35);
  border-radius: 16px;
  padding: 1.75rem;
  color: #f8fafc;
  box-shadow: 0 15px 40px rgba(15, 23, 42, 0.5);
}

.stat-card h2 {
  margin: 0.75rem 0 0.5rem;
}

.stat-card p {
  margin: 0;
  color: #cbd5f5;
  line-height: 1.5;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.stat-block {
  background: #111827;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 1.25rem;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.stat-block header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-details {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  align-items: flex-start;
}

.status-line {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.85rem;
  color: #cbd5f5;
}

.status-pill {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 50%;
  display: inline-block;
  background: #6b7280;
  transition: transform 0.2s ease;
}

.status-pill.success {
  background: #34d399;
}

.status-pill.warn {
  background: #f97316;
}

.status-pill.disabled {
  background: #475569;
}

.status-pill.loading {
  background: #3b82f6;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.stat-block h3 {
  margin: 0;
  font-size: 1.1rem;
}

.badge {
  background: #f97316;
  color: #111827;
  padding: 0.45rem 1.1rem;
  border-radius: 999px;
  font-size: 1rem;
  font-weight: 700;
  min-width: 62px;
  text-align: center;
}

.badge-link {
  border: none;
  background: #f97316;
  cursor: pointer;
  transition: transform 0.15s ease;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.badge-link:focus-visible {
  outline: 2px solid #fbbf24;
  outline-offset: 2px;
}

.badge-link:hover {
  transform: translateY(-1px);
}

@media (max-width: 640px) {
  .school-statistics {
    padding: 1.5rem 0.75rem;
  }
}
</style>
