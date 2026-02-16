<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useAuthStore } from "./stores/auth";
import LoginPanel from "./components/LoginPanel.vue";
import Dashboard from "./components/Dashboard.vue";
import SvwsServerManager from "./components/SvwsServerManager.vue";
import SchulkatalogeintragBrowser from "./components/SchulkatalogeintragBrowser.vue";
import ChangePasswordModal from "./components/ChangePasswordModal.vue";

const auth = useAuthStore();
const showChangePasswordModal = ref(false);
const currentView = ref<'dashboard' | 'servers' | 'schulkatalog'>('dashboard');

onMounted(async () => {
  await auth.handleRedirect();

  // Listen for navigation events
  window.addEventListener('navigate-to-servers', () => {
    currentView.value = 'servers';
  });

  window.addEventListener('navigate-to-schulkatalog', () => {
    currentView.value = 'schulkatalog';
  });
});
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="eyebrow">SVWS-MAIN-SERVER</p>
        <h1>
          {{ currentView === 'dashboard' ? 'Dashboard' : currentView === 'servers' ? 'SVWS Server verwalten' : 'Schulkatalog NRW' }}
        </h1>
        <p v-if="currentView === 'schulkatalog'" class="header-subtitle">Alle Schulen in Nordrhein-Westfalen</p>
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
      <Dashboard v-else-if="currentView === 'dashboard'" @navigate-to-servers="currentView = 'servers'" @navigate-to-schulkatalog="currentView = 'schulkatalog'" />
      <SvwsServerManager v-else-if="currentView === 'servers'" />
      <SchulkatalogeintragBrowser v-else-if="currentView === 'schulkatalog'" />
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
