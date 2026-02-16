<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useSvwsServersStore } from "../stores/svwsServers";
import { useSchulenStore } from "../stores/schulen";
import SchoolInfoModal from "./SchoolInfoModal.vue";

const svwsServersStore = useSvwsServersStore();
const schulenStore = useSchulenStore();
const showSchoolInfoModal = ref(false);
const selectedSchoolInfo = ref<any>(null);
const isLoadingForModal = ref(false);  // Prevent double-click during load

// Load schools immediately on mount
onMounted(async () => {
  console.log('[SvwsServerSchoolList] Component mounted - loading schulen');
  await schulenStore.load();
});

// Re-load when selected server changes (to get fresh data)
watch(() => svwsServersStore.selectedServer, async () => {
  console.log('[SvwsServerSchoolList] Selected server changed - reloading schulen');
  await schulenStore.load();
}, { immediate: false });

// Helper function to find managed school for a server school
const getManagedSchool = (serverSchool: any) => {
  const selectedServer = svwsServersStore.selectedServer;
  if (!selectedServer) return null;
  
  const found = schulenStore.items.find(
    m => m.svwsSchema === serverSchool.schema && 
         m.svwsServerId === selectedServer.id &&
         m.svwsUsername !== null && m.svwsUsername !== undefined
  );
  console.log(`[SvwsServerSchoolList] getManagedSchool: schema=${serverSchool.schema}, found=${!!found}, id=${found?.id}`);
  return found;
};

const showSchoolModal = async (serverSchool: any) => {
  if (isLoadingForModal.value) return;  // Prevent double-click
  
  const selectedServer = svwsServersStore.selectedServer;
  if (!selectedServer) {
    return;
  }

  // WICHTIG: Store MUSS aktuell sein before we try to find managed school!
  isLoadingForModal.value = true;
  console.log('[SvwsServerSchoolList] Before opening modal: reloading schulen to ensure fresh data');
  await schulenStore.load();
  console.log(`[SvwsServerSchoolList] Store reloaded, contains ${schulenStore.items.length} items`);
  isLoadingForModal.value = false;

  // Find if this school has managed credentials
  const managed = getManagedSchool(serverSchool);
  console.log(`[SvwsServerSchoolList] Looking for managed school:`);
  console.log(`  - schema: ${serverSchool.schema}`);
  console.log(`  - serverId: ${selectedServer.id}`);
  console.log(`  - found: ${!!managed}`);
  console.log(`  - id: ${managed?.id || 'NONE'}`);
  console.log(`[SvwsServerSchoolList] Store items:`, schulenStore.items.map(s => ({ schema: s.svwsSchema, serverId: s.svwsServerId, id: s.id })));

  const schoolToEdit = {
    id: managed?.id,  // Send the ID if credentials exist
    schulnummer: serverSchool.schulnummer,
    schema: serverSchool.schema,
    name: serverSchool.name,
    ort: serverSchool.ort,
    plz: serverSchool.plz,
    svwsServerId: selectedServer.id,
    svwsServerName: selectedServer.name,
    svwsUsername: managed?.svwsUsername,
    createdAt: managed?.createdAt,
    updatedAt: managed?.updatedAt,
    hasCredentials: !!managed
  };
  
  console.log(`[SvwsServerSchoolList] Passing to modal: id=${schoolToEdit.id}, hasCredentials=${schoolToEdit.hasCredentials}`);
  selectedSchoolInfo.value = schoolToEdit;
  showSchoolInfoModal.value = true;
};
</script>

<template>
  <div>
    <!-- Schools from Selected Server -->
    <section class="panel">
      <h3>Schulen auf {{ svwsServersStore.selectedServer?.name }}</h3>
      <div v-if="!svwsServersStore.selectedServer" class="warning">
        Kein Server ausgewählt
      </div>
      <div v-else-if="svwsServersStore.loadingSchools" class="loading">
        Schulen werden geladen...
      </div>
      <div v-else-if="svwsServersStore.schools.length === 0" class="info">
        Keine Schulen auf diesem Server gefunden
      </div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>Schulnummer</th>
            <th>Schema</th>
            <th>Name</th>
            <th>Ort</th>
            <th>API-Benutzer</th>
            <th>Erstellt</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="school in svwsServersStore.schools" :key="`${svwsServersStore.selectedServer.id}-${school.schema}`">
            <td><strong>{{ school.schulnummer }}</strong></td>
            <td>{{ school.schema }}</td>
            <td>{{ school.name }}</td>
            <td>{{ school.ort }} {{ school.plz }}</td>
            <td>
              <!-- Show username if managed credentials exist -->
              {{ getManagedSchool(school)?.svwsUsername || '-' }}
            </td>
            <td>
              <!-- Show created date if managed credentials exist -->
              {{ getManagedSchool(school)?.createdAt ? new Date(getManagedSchool(school)!.createdAt).toLocaleString() : '-' }}
            </td>
            <td>
              <button class="icon-button secondary" type="button" @click="showSchoolModal(school)" :disabled="isLoadingForModal" title="Anmeldedaten verwalten">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                  <path d="M12 17h.01"></path>
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <SchoolInfoModal
      :visible="showSchoolInfoModal"
      :schule="selectedSchoolInfo"
      @close="showSchoolInfoModal = false"
      @school-saved="schulenStore.load()"
    />
  </div>
</template>
