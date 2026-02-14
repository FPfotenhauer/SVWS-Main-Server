<script setup lang="ts">
import { ref } from "vue";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const username = ref("");
const password = ref("");

const handlePasswordLogin = async () => {
  try {
    await auth.passwordLogin(username.value, password.value);
  } catch (err) {
    // Error is already set in the store
  }
};
</script>

<template>
  <section class="panel">
    <template v-if="auth.authMethod === 'oidc'">
      <h2>Login erforderlich</h2>
      <p class="helper">
        Die Anmeldung erfolgt ueber den konfigurierten OIDC-Provider.
      </p>
      <button type="button" @click="auth.login">Login starten</button>
    </template>
    <template v-else>
      <h2>Benutzer-Login</h2>
      <form @submit.prevent="handlePasswordLogin" class="login-form">
        <div class="form-group">
          <label for="username">Benutzername</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="Benutzername"
            required
            :disabled="auth.isLoading"
          />
        </div>
        <div class="form-group">
          <label for="password">Passwort</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="Passwort"
            required
            :disabled="auth.isLoading"
          />
        </div>
        <button type="submit" :disabled="auth.isLoading">
          {{ auth.isLoading ? "wird angemeldet..." : "Anmelden" }}
        </button>
      </form>
    </template>
    <p v-if="auth.error" class="helper" style="color: var(--danger)">
      {{ auth.error }}
    </p>
  </section>
</template>

<style scoped>
.panel {
  max-width: 400px;
  margin: 1.5rem auto;
  padding: 1.5rem;
  border: 1px solid var(--accent);
  border-radius: 8px;
  background: var(--bg);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.form-group label {
  font-weight: 500;
  font-size: 0.9rem;
}

.form-group input {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--accent);
  border-radius: 4px;
  background: var(--bg);
  color: inherit;
  font-size: 1rem;
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

button {
  padding: 0.5rem 1.5rem;
  margin-top: 0.25rem;
  background: var(--accent);
  color: var(--bg);
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

button:hover:not(:disabled) {
  opacity: 0.9;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.helper {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--accent-2);
}
</style>
