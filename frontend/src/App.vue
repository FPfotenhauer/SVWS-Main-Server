<script setup lang="ts">
import { onMounted } from "vue";
import { useAuthStore } from "./stores/auth";
import LoginPanel from "./components/LoginPanel.vue";
import SchuleList from "./components/SchuleList.vue";
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
        <h1>Schulen verwalten</h1>
      </div>
      <div v-if="auth.isAuthenticated" class="header-actions">
        <span class="pill">Angemeldet</span>
        <button class="ghost" type="button" @click="auth.logout">Abmelden</button>
      </div>
    </header>

    <main>
      <LoginPanel v-if="!auth.isAuthenticated" />
      <SchuleList v-else />
    </main>

    <ChangePasswordModal />
  </div>
</template>
