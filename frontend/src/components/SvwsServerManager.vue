<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useSvwsServersStore } from "../stores/svwsServers";
import type { SvwsServerRequest } from "../types/svwsServer";

const store = useSvwsServersStore();
const showForm = ref(false);
const form = ref({
  name: "",
  url: "",
  port: "",
  username: "",
  password: ""
});

const statusClass = (status: string) => {
  if (status === "CONNECTED") return "success";
  if (status === "UNTESTED") return "warn";
  if (status === "INVALID_CREDENTIALS" || status === "UNREACHABLE" || status === "ERROR") return "error";
  return "";
};

const canCreate = computed(() =>
  form.value.name &&
  form.value.url &&
  form.value.port &&
  form.value.username &&
  form.value.password
);

const createServer = async () => {
  try {
    // Combine URL and port into baseUrl
    const baseUrl = `${form.value.url}:${form.value.port}`;
    const serverRequest: SvwsServerRequest = {
      name: form.value.name,
      baseUrl: baseUrl,
      username: form.value.username,
      password: form.value.password
    };
    await store.createServer(serverRequest);
    form.value = {
      name: "",
      url: "",
      port: "",
      username: "",
      password: ""
    };
    showForm.value = false;
  } catch (err) {
    // Error is already in store.error
  }
};

const viewSchools = async (serverId: string) => {
  console.log("viewSchools called with serverId:", serverId);
  try {
    await store.loadSchoolsFromServer(serverId);
    console.log("Schools loaded successfully");
  } catch (err) {
    console.error("Error loading schools:", err);
  }
};

const deleteServer = async (id: string) => {
  if (confirm("Are you sure you want to delete this SVWS server?")) {
    await store.deleteServer(id);
  }
};

onMounted(() => {
  store.loadServers();
});
</script>

<template>
  <div>
    <!-- Server List -->
    <section v-if="!store.selectedServer" class="panel">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <h2>SVWS Server</h2>
        <button type="button" @click="showForm = !showForm">
          {{ showForm ? "Abbrechen" : "+ Server hinzufügen" }}
        </button>
      </div>

      <!-- Add Server Form -->
      <div v-if="showForm" class="form-panel">
        <h3>Neuer SVWS Server</h3>
        <div class="form-grid">
          <input v-model="form.name" placeholder="Name (z.B. 'Hauptserver')" />
          <input v-model="form.url" placeholder="URL (z.B. 'https://svws.example.com')" />
          <input v-model="form.port" placeholder="Port (z.B. '443' oder '8443')" />
          <input v-model="form.username" placeholder="Admin Benutzername" />
          <input v-model="form.password" placeholder="Admin Passwort" type="password" />
        </div>
        <p class="helper">Das Passwort wird verschlüsselt gespeichert.</p>
        <button type="button" :disabled="!canCreate" @click="createServer">Server anlegen</button>
        <p v-if="store.error" class="error-text">{{ store.error }}</p>
      </div>

      <!-- Server Table -->
      <table class="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Base URL</th>
            <th>Benutzername</th>
            <th>Status</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="server in store.servers" :key="server.id">
            <td>
              <strong>{{ server.name }}</strong>
            </td>
            <td>{{ server.baseUrl }}</td>
            <td>{{ server.username }}</td>
            <td>
              <div class="status" :class="statusClass(server.status)">
                {{ server.status }}
              </div>
              <div class="helper" v-if="server.lastError">{{ server.lastError }}</div>
            </td>
            <td>
              <button class="secondary" type="button" @click="viewSchools(server.id)">
                Schulen anzeigen
              </button>
              <button class="danger" type="button" @click="deleteServer(server.id)">
                Löschen
              </button>
            </td>
          </tr>
          <tr v-if="!store.servers.length">
            <td colspan="5">Keine SVWS Server vorhanden.</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- Schools from Server -->
    <section v-else class="panel">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2>Schulen auf: {{ store.selectedServer.name }}</h2>
          <p class="helper">{{ store.selectedServer.baseUrl }}</p>
        </div>
        <button class="secondary" type="button" @click="store.clearSelection">
          Zurück zur Serverliste
        </button>
      </div>

      <div v-if="store.loadingSchools" class="loading">Laden...</div>
      <div v-else-if="store.error" class="error-text">{{ store.error }}</div>

      <table v-else class="table">
        <thead>
          <tr>
            <th>Schulnummer</th>
            <th>Name</th>
            <th>Ort</th>
            <th>PLZ</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="school in store.schools" :key="school.schulnummer">
            <td><strong>{{ school.schulnummer }}</strong></td>
            <td>{{ school.name }}</td>
            <td>{{ school.ort }}</td>
            <td>{{ school.plz }}</td>
          </tr>
          <tr v-if="!store.schools.length">
            <td colspan="4">Keine Schulen gefunden.</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.form-panel {
  padding: 1rem 0;
  margin: 1rem 0;
}

.form-grid {
  display: grid;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.error-text {
  color: #dc3545;
  margin-top: 0.5rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

button.danger {
  background-color: #dc3545;
  color: white;
}

button.danger:hover {
  background-color: #c82333;
}
</style>
