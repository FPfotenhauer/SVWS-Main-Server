<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useAuthStore } from "./stores/auth";
import LoginPanel from "./components/LoginPanel.vue";
import Dashboard from "./components/Dashboard.vue";
import SvwsServerManager from "./components/SvwsServerManager.vue";
import SchulkatalogeintragBrowser from "./components/SchulkatalogeintragBrowser.vue";
import SvwsServerSchoolList from "./components/SvwsServerSchoolList.vue";
import Schulstatistiken from "./components/Schulstatistiken.vue";
import Schuelerzahlen from "./components/Schuelerzahlen.vue";
import ChangePasswordModal from "./components/ChangePasswordModal.vue";

const auth = useAuthStore();
const showChangePasswordModal = ref(false);
const currentView = ref<'dashboard' | 'servers' | 'schulkatalog' | 'verwaltete-schulen' | 'schulstatistiken' | 'schuelerzahlen'>('dashboard');

onMounted(async () => {
  await auth.handleRedirect();

  window.addEventListener('navigate-to-servers', () => {
    currentView.value = 'servers';
  });

  window.addEventListener('navigate-to-schulkatalog', () => {
    currentView.value = 'schulkatalog';
  });

  window.addEventListener('navigate-to-verwaltete-schulen', () => {
    currentView.value = 'verwaltete-schulen';
  });

  window.addEventListener('navigate-to-schulstatistiken', () => {
    currentView.value = 'schulstatistiken';
  });

  window.addEventListener('navigate-to-schuelerzahlen', () => {
    currentView.value = 'schuelerzahlen';
  });
});
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="eyebrow">SVWS-MAIN-SERVER</p>
        <h1>
          {{
            currentView === 'dashboard'
              ? 'Dashboard'
              : currentView === 'servers'
                ? 'SVWS Server verwalten'
                : currentView === 'schulkatalog'
                  ? 'Schulkatalog NRW'
                  : currentView === 'verwaltete-schulen'
                    ? 'Verwaltete Schulen'
                    : currentView === 'schuelerzahlen'
                      ? 'Schülerzahlen'
                      : 'Schulstatistiken'
          }}
        </h1>
        <p v-if="currentView === 'schulkatalog'" class="header-subtitle">Alle Schulen in Nordrhein-Westfalen</p>
        <p v-if="currentView === 'verwaltete-schulen'" class="header-subtitle">Schulen mit Anmeldedaten</p>
        <p v-if="currentView === 'schulstatistiken'" class="header-subtitle">Erste Ideen und Kennzahlen zu Schulstatistiken</p>
        <p v-if="currentView === 'schuelerzahlen'" class="header-subtitle">Tabellarische Schülerzahlen und Begleitdaten</p>
      </div>
      <div v-if="auth.isAuthenticated" class="header-actions">
        <button v-if="currentView !== 'dashboard'" class="ghost" type="button" @click="currentView = 'dashboard'">Dashboard</button>
        <button class="ghost" type="button" @click="showChangePasswordModal = true">Passwort ändern</button>
        <span class="pill">Angemeldet</span>
        <button class="ghost" type="button" @click="auth.logout">Abmelden</button>
      </div>
    </header>

    <main>
      <LoginPanel v-if="!auth.isAuthenticated" />
      <Dashboard
        v-else-if="currentView === 'dashboard'"
        @navigate-to-servers="currentView = 'servers'"
        @navigate-to-schulkatalog="currentView = 'schulkatalog'"
        @navigate-to-schulstatistiken="currentView = 'schulstatistiken'"
      />
      <SvwsServerManager v-else-if="currentView === 'servers'" />
      <SchulkatalogeintragBrowser v-else-if="currentView === 'schulkatalog'" />
      <SvwsServerSchoolList v-else-if="currentView === 'verwaltete-schulen'" />
      <Schulstatistiken v-else-if="currentView === 'schulstatistiken'" />
      <Schuelerzahlen v-else-if="currentView === 'schuelerzahlen'" />
    </main>

    <ChangePasswordModal :visible="showChangePasswordModal" @close="showChangePasswordModal = false" />
  </div>
</template>

<style scoped>
.header-subtitle {
  color: #94a3b8;
  font-size: 1rem;
  margin: 0.25rem 0 0 0;
  font-weight: 400;
}
</style>
