<template>
  <div class="benutzer-verwaltung">
    <div class="content-wrapper">
      <section class="users-section">
        <h2>Alle Benutzer</h2>
        <div class="section-content">
          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

          <form class="user-form" @submit.prevent="createUser">
            <div class="form-row">
              <label for="new-username">Benutzername</label>
              <input
                id="new-username"
                v-model.trim="newUser.username"
                type="text"
                autocomplete="off"
                required
              >
            </div>
            <div class="form-row">
              <label for="new-password">Passwort</label>
              <input
                id="new-password"
                v-model="newUser.password"
                type="password"
                autocomplete="new-password"
                required
              >
            </div>
            <button type="submit" :disabled="isLoading">Benutzer anlegen</button>
          </form>

          <div v-if="isLoading" class="loading-text">Lade Benutzer...</div>

          <table v-else class="users-table">
            <thead>
              <tr>
                <th>Benutzername</th>
                <th>Erstellt</th>
                <th>Aktualisiert</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="users.length === 0">
                <td colspan="4">Keine Benutzer vorhanden.</td>
              </tr>
              <template v-for="user in users" :key="user.id">
                <tr>
                  <td>{{ user.username }}</td>
                  <td>{{ formatDate(user.createdAt) }}</td>
                  <td>{{ formatDate(user.updatedAt) }}</td>
                  <td>
                    <button type="button" @click="startEdit(user)">Bearbeiten</button>
                  </td>
                </tr>
                <tr v-if="editingUserId === user.id" class="edit-row">
                  <td colspan="4">
                    <form class="user-form" @submit.prevent="updateUser(user.id)">
                      <div class="form-row">
                        <label :for="`edit-username-${user.id}`">Benutzername</label>
                        <input
                          :id="`edit-username-${user.id}`"
                          v-model.trim="editUser.username"
                          type="text"
                          autocomplete="off"
                          required
                        >
                      </div>
                      <div class="form-row">
                        <label :for="`edit-password-${user.id}`">Neues Passwort (optional)</label>
                        <input
                          :id="`edit-password-${user.id}`"
                          v-model="editUser.password"
                          type="password"
                          autocomplete="new-password"
                        >
                      </div>
                      <div class="button-row">
                        <button type="submit">Speichern</button>
                        <button type="button" @click="cancelEdit">Abbrechen</button>
                      </div>
                    </form>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import api from '../services/api';

interface ManagedUser {
  id: string;
  username: string;
  createdAt: string;
  updatedAt: string;
}

interface UserPayload {
  username: string;
  password: string;
}

const users = ref<ManagedUser[]>([]);
const isLoading = ref(false);
const errorMessage = ref('');
const editingUserId = ref<string | null>(null);

const newUser = ref<UserPayload>({
  username: '',
  password: ''
});

const editUser = ref<UserPayload>({
  username: '',
  password: ''
});

const loadUsers = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await api.get<ManagedUser[]>('/api/users');
    users.value = response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Benutzer', error);
    errorMessage.value = 'Benutzer konnten nicht geladen werden.';
  } finally {
    isLoading.value = false;
  }
};

const createUser = async () => {
  errorMessage.value = '';
  if (!newUser.value.username || !newUser.value.password) {
    errorMessage.value = 'Benutzername und Passwort sind erforderlich.';
    return;
  }

  try {
    await api.post('/api/users', newUser.value);
    newUser.value = { username: '', password: '' };
    await loadUsers();
  } catch (error) {
    console.error('Fehler beim Anlegen des Benutzers', error);
    errorMessage.value = 'Benutzer konnte nicht angelegt werden.';
  }
};

const startEdit = (user: ManagedUser) => {
  editingUserId.value = user.id;
  editUser.value = {
    username: user.username,
    password: ''
  };
};

const cancelEdit = () => {
  editingUserId.value = null;
  editUser.value = { username: '', password: '' };
};

const updateUser = async (id: string) => {
  errorMessage.value = '';
  if (!editUser.value.username) {
    errorMessage.value = 'Benutzername ist erforderlich.';
    return;
  }

  try {
    await api.put(`/api/users/${id}`, {
      username: editUser.value.username,
      password: editUser.value.password || null
    });
    cancelEdit();
    await loadUsers();
  } catch (error) {
    console.error('Fehler beim Aktualisieren des Benutzers', error);
    errorMessage.value = 'Benutzer konnte nicht aktualisiert werden.';
  }
};

const formatDate = (value: string) => {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('de-DE');
};

onMounted(async () => {
  await loadUsers();
});
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

.error-message {
  margin: 0 0 1rem 0;
}

.loading-text {
  margin: 1rem 0;
}

.user-form {
  display: grid;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  gap: 0.25rem;
}

.form-row input {
  background: #1f2937;
  border: 1px solid #374151;
  color: #f8fafc;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
}

button {
  background: #f97316;
  color: #f8fafc;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  border-bottom: 1px solid #374151;
  text-align: left;
  padding: 0.5rem;
  vertical-align: top;
}

.edit-row td {
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

.button-row {
  display: flex;
  gap: 0.5rem;
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

  .users-table {
    font-size: 0.875rem;
  }
}
</style>
