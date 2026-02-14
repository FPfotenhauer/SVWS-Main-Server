<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useSvwsServersStore } from "../stores/svwsServers";
import type { SvwsServerRequest } from "../types/svwsServer";

const store = useSvwsServersStore();
const showForm = ref(false);
const sortBy = ref<'name' | 'baseUrl'>('name');
const sortDirection = ref<'asc' | 'desc'>('asc');
const schoolSortBy = ref<'schulnummer' | 'name'>('schulnummer');
const schoolSortDirection = ref<'asc' | 'desc'>('asc');
const schoolSearchQuery = ref('');
const serverSearchQuery = ref('');

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

const filteredAndSortedServers = computed(() => {
  let servers = [...store.servers];
  
  // Filter by search query
  if (serverSearchQuery.value) {
    const query = serverSearchQuery.value.toLowerCase();
    servers = servers.filter(server => {
      const name = server.name?.toLowerCase() || '';
      const baseUrl = server.baseUrl?.toLowerCase() || '';
      return name.includes(query) || baseUrl.includes(query);
    });
  }
  
  // Sort
  servers.sort((a, b) => {
    let aVal = a[sortBy.value];
    let bVal = b[sortBy.value];
    
    if (typeof aVal === 'string') aVal = aVal.toLowerCase();
    if (typeof bVal === 'string') bVal = bVal.toLowerCase();
    
    if (aVal < bVal) return sortDirection.value === 'asc' ? -1 : 1;
    if (aVal > bVal) return sortDirection.value === 'asc' ? 1 : -1;
    return 0;
  });
  
  return servers;
});

const filteredAndSortedSchools = computed(() => {
  let schools = [...store.schools];
  
  // Filter by search query
  if (schoolSearchQuery.value) {
    const query = schoolSearchQuery.value.toLowerCase();
    schools = schools.filter(school => {
      const schulnummer = school.schulnummer?.toString().toLowerCase() || '';
      const name = school.name?.toLowerCase() || '';
      return schulnummer.includes(query) || name.includes(query);
    });
  }
  
  // Sort
  schools.sort((a, b) => {
    let aVal = a[schoolSortBy.value];
    let bVal = b[schoolSortBy.value];
    
    if (typeof aVal === 'string') aVal = aVal.toLowerCase();
    if (typeof bVal === 'string') bVal = bVal.toLowerCase();
    
    if (aVal < bVal) return schoolSortDirection.value === 'asc' ? -1 : 1;
    if (aVal > bVal) return schoolSortDirection.value === 'asc' ? 1 : -1;
    return 0;
  });
  
  return schools;
});

const toggleSort = (column: 'name' | 'baseUrl') => {
  if (sortBy.value === column) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = column;
    sortDirection.value = 'asc';
  }
};

const toggleSchoolSort = (column: 'schulnummer' | 'name') => {
  if (schoolSortBy.value === column) {
    schoolSortDirection.value = schoolSortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    schoolSortBy.value = column;
    schoolSortDirection.value = 'asc';
  }
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
    const server = await store.createServer(serverRequest);
    
    // Test connection after creating
    await store.testConnection(server.id);
    
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

const testConnection = async (serverId: string) => {
  try {
    await store.testConnection(serverId);
  } catch (err) {
    console.error("Error testing connection:", err);
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

      <!-- Search Box -->
      <div class="search-box">
        <input 
          v-model="serverSearchQuery" 
          type="text" 
          placeholder="Suche nach Name oder Base URL..."
          class="search-input"
        />
        <span class="search-results">{{ filteredAndSortedServers.length }} von {{ store.servers.length }} Servern</span>
      </div>

      <!-- Server Table -->
      <table class="table">
        <thead>
          <tr>
            <th class="sortable" @click="toggleSort('name')">
              Name
              <span class="sort-indicator" v-if="sortBy === 'name'">
                {{ sortDirection === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th class="sortable" @click="toggleSort('baseUrl')">
              Base URL
              <span class="sort-indicator" v-if="sortBy === 'baseUrl'">
                {{ sortDirection === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th>Benutzername</th>
            <th>Status</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="server in filteredAndSortedServers" :key="server.id">
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
              <div class="action-buttons">
                <button 
                  class="icon-button secondary" 
                  type="button" 
                  @click="testConnection(server.id)"
                  title="Verbindung testen">
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <path d="M12 6v6l4 2"></path>
                  </svg>
                </button>
                <button 
                  class="icon-button secondary" 
                  type="button" 
                  @click="viewSchools(server.id)"
                  title="Schulen anzeigen">
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                    <polyline points="9 22 9 12 15 12 15 22"></polyline>
                  </svg>
                </button>
                <button 
                  class="icon-button danger" 
                  type="button" 
                  @click="deleteServer(server.id)"
                  title="Löschen">
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 6h18"></path>
                    <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
                    <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
                    <line x1="10" y1="11" x2="10" y2="17"></line>
                    <line x1="14" y1="11" x2="14" y2="17"></line>
                  </svg>
                </button>
              </div>
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

      <div v-else>
        <!-- Search Box -->
        <div class="search-box">
          <input 
            v-model="schoolSearchQuery" 
            type="text" 
            placeholder="Suche nach Schulnummer oder Name..."
            class="search-input"
          />
          <span class="search-results">{{ filteredAndSortedSchools.length }} von {{ store.schools.length }} Schulen</span>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th class="sortable" @click="toggleSchoolSort('schulnummer')">
              Schulnummer
              <span class="sort-indicator" v-if="schoolSortBy === 'schulnummer'">
                {{ schoolSortDirection === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th class="sortable" @click="toggleSchoolSort('name')">
              Name
              <span class="sort-indicator" v-if="schoolSortBy === 'name'">
                {{ schoolSortDirection === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th>Ort</th>
            <th>PLZ</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="school in filteredAndSortedSchools" :key="school.schulnummer">
            <td><strong>{{ school.schulnummer }}</strong></td>
            <td>{{ school.name }}</td>
            <td>{{ school.ort }}</td>
            <td>{{ school.plz }}</td>
          </tr>
          <tr v-if="!filteredAndSortedSchools.length">
            <td colspan="4">Keine Schulen gefunden.</td>
          </tr>
        </tbody>
      </table>
      </div>
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

.action-buttons {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.icon-button {
  padding: 0.5rem;
  min-width: unset;
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-button svg {
  display: block;
}

.icon-button.secondary {
  background-color: #6c757d;
  color: white;
  border: none;
}

.icon-button.secondary:hover {
  background-color: #5a6268;
  transform: translateY(-1px);
}

button.danger,
.icon-button.danger {
  background-color: #dc3545;
  color: white;
  border: none;
}

button.danger:hover,
.icon-button.danger:hover {
  background-color: #c82333;
  transform: translateY(-1px);
}

.sortable {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s;
}

.sortable:hover {
  background-color: rgba(108, 117, 125, 0.1);
}

.sort-indicator {
  margin-left: 0.25rem;
  font-size: 0.9em;
  opacity: 0.8;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.search-input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.95rem;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}

.search-results {
  color: #6c757d;
  font-size: 0.9rem;
  white-space: nowrap;
}
</style>
