<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useAuthStore } from "./stores/auth";
import LoginPanel from "./components/LoginPanel.vue";
import Dashboard from "./components/Dashboard.vue";
import SvwsServerManager from "./components/SvwsServerManager.vue";
import ChangePasswordModal from "./components/ChangePasswordModal.vue";

const auth = useAuthStore();
const showChangePasswordModal = ref(false);
const currentView = ref<'dashboard' | 'servers'>('dashboard');

onMounted(async () => {
  await auth.handleRedirect();

  // Listen for navigation events
  window.addEventListener('navigate-to-servers', () => {
    currentView.value = 'servers';
  });
});
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="eyebrow">SVWS-MAIN-SERVER</p>
        <h1>{{ currentView === 'dashboard' ? 'Dashboard' : 'SVWS Server verwalten' }}</h1>
      </div>
      <div v-if="auth.isAuthenticated" class="header-actions">
        <button v-if="currentView === 'servers'" class="ghost" type="button" @click="currentView = 'dashboard'">Dashboard</button>
        <button class="ghost" type="button" @click="showChangePasswordModal = true">Passwort ändern</button>
        <span class="pill">Angemeldet</span>
        <button class="ghost" type="button" @click="auth.logout">Abmelden</button>
      </div>
    </header>

    <main>
      <LoginPanel v-if="!auth.isAuthenticated" />
      <Dashboard v-else-if="currentView === 'dashboard'" @navigate-to-servers="currentView = 'servers'" />
      <SvwsServerManager v-else />
    </main>

    <ChangePasswordModal :visible="showChangePasswordModal" @close="showChangePasswordModal = false" />
  </div>
</template>
