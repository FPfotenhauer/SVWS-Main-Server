<script setup lang="ts">
import { onMounted } from "vue";
import { useAuthStore } from "./stores/auth";
import LoginPanel from "./components/LoginPanel.vue";
import SvwsServerManager from "./components/SvwsServerManager.vue";
import ChangePasswordModal from "./components/ChangePasswordModal.vue";

const auth = useAuthStore();

onMounted(async () => {
  await auth.handleRedirect();
});
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="eyebrow">Schultraeger-Server</p>
        <h1>SVWS Server verwalten</h1>
      </div>
      <div v-if="auth.isAuthenticated" class="header-actions">
        <span class="pill">Angemeldet</span>
        <button class="ghost" type="button" @click="auth.logout">Abmelden</button>
      </div>
    </header>

    <main>
      <LoginPanel v-if="!auth.isAuthenticated" />
      <SvwsServerManager v-else />
    </main>

    <ChangePasswordModal />
  </div>
</template>
