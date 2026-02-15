<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useSchulenStore } from "../stores/schulen";
import type { SchuleRequest } from "../types/schule";

const store = useSchulenStore();
const form = ref<SchuleRequest>({
  name: "",
  svwsUrl: "",
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
  form.value.name &&
  form.value.svwsUrl &&
  form.value.svwsSchema &&
  form.value.svwsUsername &&
  form.value.svwsPassword
);

const create = async () => {
  await store.create(form.value);
  form.value = {
    name: "",
    svwsUrl: "",
    svwsSchema: "",
    svwsUsername: "",
    svwsPassword: ""
  };
};

onMounted(() => {
  store.load();
});
</script>

<template>
  <section class="panel">
    <h2>Schule anlegen</h2>
    <div class="form-grid">
      <input v-model="form.name" placeholder="Name" />
      <input v-model="form.svwsUrl" placeholder="SVWS URL" />
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
          <th>Name</th>
          <th>Schulnummer</th>
          <th>Status</th>
          <th>Letzter Sync</th>
          <th>Aktionen</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="schule in store.items" :key="schule.id">
          <td>
            <strong>{{ schule.name }}</strong>
            <div class="helper">{{ schule.svwsUrl }}</div>
          </td>
          <td>{{ schule.schulnummer ?? "-" }}</td>
          <td>
            <div class="status" :class="statusClass(schule.status)">
              {{ schule.status }}
            </div>
            <div class="helper" v-if="schule.lastError">{{ schule.lastError }}</div>
          </td>
          <td>
            <div>{{ formatted(schule.lastSyncAt) }}</div>
            <div class="status" :class="statusClass(schule.lastSyncStatus)">
              {{ schule.lastSyncStatus ?? "-" }}
            </div>
          </td>
          <td>
            <button class="secondary" type="button" @click="store.verify(schule.id)">
              Verbindung testen
            </button>
            <button type="button" @click="store.sync(schule.id)">Synchronisieren</button>
          </td>
        </tr>
        <tr v-if="!store.items.length">
          <td colspan="5">Keine Schulen vorhanden.</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
