<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useSchulenStore } from "../stores/schulen";
import type { SchuleRequest, Schule } from "../types/schule";
import SchoolInfoModal from "./SchoolInfoModal.vue";

const store = useSchulenStore();
const showInfoModal = ref(false);
const selectedSchule = ref<Schule | null>(null);

const form = ref<SchuleRequest>({
  svwsServerId: "",
  svwsSchema: "",
  svwsUsername: "",
  svwsPassword: ""
});

const statusClass = (status?: string | null) => {
  if (!status) return "";
  if (status === "VERIFIED" || status === "SUCCESS") return "success";
  if (status === "UNVERIFIED") return "warn";
  if (status === "INVALID_CREDENTIALS" || status === "UNREACHABLE" || status === "ERROR") return "error";
  return "";
};

const formatted = (value?: string | null) => {
  if (!value) return "-";
  return new Date(value).toLocaleString();
};

const canCreate = computed(() =>
  form.value.svwsServerId &&
  form.value.svwsSchema &&
  form.value.svwsUsername &&
  form.value.svwsPassword
);

const create = async () => {
  await store.create(form.value);
  form.value = {
    svwsServerId: "",
    svwsSchema: "",
    svwsUsername: "",
    svwsPassword: ""
  };
};

const showSchoolInfo = (schule: any) => {
  selectedSchule.value = schule;
  showInfoModal.value = true;
};

onMounted(() => {
  store.load();
});
</script>

<template>
  <section class="panel">
    <h2>Schule anlegen</h2>
    <div class="form-grid">
      <input v-model="form.svwsServerId" placeholder="SVWS Server ID" />
      <input v-model="form.svwsSchema" placeholder="Schema" />
      <input v-model="form.svwsUsername" placeholder="SVWS Username" />
      <input v-model="form.svwsPassword" placeholder="SVWS Passwort" type="password" />
    </div>
    <p class="helper">Passwort wird nur fuer den Sync verschluesselt gespeichert.</p>
    <button type="button" :disabled="!canCreate" @click="create">Schule anlegen</button>
    <p v-if="store.error" class="helper">{{ store.error }}</p>
  </section>

  <section class="panel">
    <h2>Schulen</h2>
    <table class="table">
      <thead>
        <tr>
          <th>Schema</th>
          <th>Erstellt am</th>
          <th>Aktionen</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="schule in store.items" :key="schule.id">
          <td>
            <strong>{{ schule.svwsSchema }}</strong>
            <div class="helper">{{ schule.svwsServerName }}</div>
          </td>
          <td>{{ formatted(schule.createdAt) }}</td>
          <td>
            <button class="icon-button secondary" type="button" @click="showSchoolInfo(schule)" title="Informationen anzeigen">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                <path d="M12 17h.01"></path>
              </svg>
            </button>
          </td>
        </tr>
        <tr v-if="!store.items.length">
          <td colspan="3">Keine Schulen vorhanden.</td>
        </tr>
      </tbody>
    </table>
  </section>

  <!-- School Info Modal -->
  <SchoolInfoModal
    :visible="showInfoModal"
    :schule="selectedSchule"
    @close="showInfoModal = false"
  />
</template>

<style scoped>
.icon-button {
  --icon-size: 20px;
  padding: 0.5rem;
  min-width: unset;
  width: calc(var(--icon-size) + 1rem);
  height: calc(var(--icon-size) + 1rem);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
  background-color: #6c757d;
  color: white;
  border: none;
  margin-left: 0.5rem;
}

.icon-button svg {
  width: var(--icon-size);
  height: var(--icon-size);
  display: block;
}

.icon-button:hover {
  background-color: #5a6268;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(108, 117, 125, 0.3);
}

.icon-button:active {
  transform: translateY(0) scale(0.95);
  background-color: #545b62;
  box-shadow: 0 1px 4px rgba(108, 117, 125, 0.2);
}
</style>
