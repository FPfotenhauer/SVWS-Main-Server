<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useAuthStore } from "./stores/auth";
import LoginPanel from "./components/LoginPanel.vue";
import Dashboard from "./components/Dashboard.vue";
import SvwsServerManager from "./components/SvwsServerManager.vue";
import SchulkatalogeintragBrowser from "./components/SchulkatalogeintragBrowser.vue";
import SvwsServerSchoolList from "./components/SvwsServerSchoolList.vue";
import Schulstatistiken from "./components/Schulstatistiken.vue";
import Schuelerzahlen from "./components/Schuelerzahlen.vue";
import IDMVerwaltung from "./components/IDMVerwaltung.vue";
import BenutzerVerwaltung from "./components/BenutzerVerwaltung.vue";
import Support from "./components/Support.vue";
import Entfernungsberechnung from "./components/Entfernungsberechnung.vue";
import EntfernungsberechnungSchule from "./components/EntfernungsberechnungSchule.vue";
import EntfernungsberechnungAdresse from "./components/EntfernungsberechnungAdresse.vue";
import ChangePasswordModal from "./components/ChangePasswordModal.vue";
import type { SchuleStammdatenResponse } from "./types/schule";
import type { SchuelerAdresse } from "./types/schueler";

const auth = useAuthStore();
const showChangePasswordModal = ref(false);
const selectedDistanceSchool = ref<SchuleStammdatenResponse | null>(null);
const selectedDistanceAddress = ref<SchuelerAdresse | null>(null);
const currentView = ref<'dashboard' | 'servers' | 'schulkatalog' | 'verwaltete-schulen' | 'schulstatistiken' | 'schuelerzahlen' | 'idm-verwaltung' | 'benutzerverwaltung' | 'entfernungsberechnung' | 'entfernungsberechnung-schule' | 'entfernungsberechnung-adresse' | 'support'>('dashboard');

onMounted(async () => {
  await auth.handleRedirect();

  window.addEventListener('navigate-to-servers', () => {
    currentView.value = 'servers';
  });

  window.addEventListener('navigate-to-schulkatalog', () => {
    currentView.value = 'schulkatalog';
  });

  window.addEventListener('navigate-to-verwaltete-schulen', () => {
    currentView.value = 'verwaltete-schulen';
  });

  window.addEventListener('navigate-to-schulstatistiken', () => {
    currentView.value = 'schulstatistiken';
  });

  window.addEventListener('navigate-to-schuelerzahlen', () => {
    currentView.value = 'schuelerzahlen';
  });

  window.addEventListener('navigate-to-idm-verwaltung', () => {
    currentView.value = 'idm-verwaltung';
  });

  window.addEventListener('navigate-to-benutzerverwaltung', () => {
    currentView.value = 'benutzerverwaltung';
  });

  window.addEventListener('navigate-to-entfernungsberechnung', () => {
    currentView.value = 'entfernungsberechnung';
  });

  window.addEventListener('navigate-to-support', () => {
    currentView.value = 'support';
  });
});
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="eyebrow">SVWS-MAIN-SERVER</p>
        <h1>
          {{
            currentView === 'dashboard'
              ? 'Dashboard'
              : currentView === 'servers'
                ? 'SVWS Server verwalten'
                : currentView === 'schulkatalog'
                  ? 'Schulkatalog NRW'
                  : currentView === 'verwaltete-schulen'
                    ? 'Verwaltete Schulen'
                    : currentView === 'schuelerzahlen'
                      ? 'Schülerzahlen'
                      : currentView === 'idm-verwaltung'
                        ? 'IDM Verwaltung'
                        : currentView === 'benutzerverwaltung'
                          ? 'Benutzer verwalten'
                          : currentView === 'entfernungsberechnung'
                            ? 'Entfernungsberechnung'
                            : currentView === 'entfernungsberechnung-schule'
                              ? 'Entfernungsberechnung · Schüler:in suchen'
                              : currentView === 'entfernungsberechnung-adresse'
                                ? 'Entfernungsberechnung · Adressdaten'
                          : currentView === 'support'
                            ? 'Support'
                            : 'Schulstatistiken'
          }}
        </h1>
        <p v-if="currentView === 'schulkatalog'" class="header-subtitle">Alle Schulen in Nordrhein-Westfalen</p>
        <p v-if="currentView === 'verwaltete-schulen'" class="header-subtitle">Schulen mit Anmeldedaten</p>
        <p v-if="currentView === 'schulstatistiken'" class="header-subtitle">Erste Ideen und Kennzahlen zu Schulstatistiken</p>
        <p v-if="currentView === 'schuelerzahlen'" class="header-subtitle">Tabellarische Schülerzahlen und Begleitdaten</p>
      </div>
      <div v-if="auth.isAuthenticated" class="header-actions">
        <button v-if="currentView !== 'dashboard'" class="ghost" type="button" @click="currentView = 'dashboard'">Dashboard</button>
        <button class="ghost" type="button" @click="showChangePasswordModal = true">Passwort ändern</button>
        <span class="pill">Angemeldet</span>
        <button class="ghost" type="button" @click="auth.logout">Abmelden</button>
      </div>
    </header>

    <main>
      <LoginPanel v-if="!auth.isAuthenticated" />
      <Dashboard
        v-else-if="currentView === 'dashboard'"
        @navigate-to-servers="currentView = 'servers'"
        @navigate-to-schulkatalog="currentView = 'schulkatalog'"
        @navigate-to-schulstatistiken="currentView = 'schulstatistiken'"
        @navigate-to-idm-verwaltung="currentView = 'idm-verwaltung'"
        @navigate-to-benutzer-verwaltung="currentView = 'benutzerverwaltung'"
        @navigate-to-entfernungsberechnung="currentView = 'entfernungsberechnung'"
        @navigate-to-support="currentView = 'support'"
      />
      <SvwsServerManager v-else-if="currentView === 'servers'" />
      <SchulkatalogeintragBrowser v-else-if="currentView === 'schulkatalog'" />
      <SvwsServerSchoolList v-else-if="currentView === 'verwaltete-schulen'" />
      <Schulstatistiken v-else-if="currentView === 'schulstatistiken'" />
      <Schuelerzahlen v-else-if="currentView === 'schuelerzahlen'" />
      <IDMVerwaltung v-else-if="currentView === 'idm-verwaltung'" />
      <BenutzerVerwaltung v-else-if="currentView === 'benutzerverwaltung'" />
      <Entfernungsberechnung
        v-else-if="currentView === 'entfernungsberechnung'"
        @select-school="(school) => { selectedDistanceSchool = school; currentView = 'entfernungsberechnung-schule'; }"
      />
      <EntfernungsberechnungSchule
        v-else-if="currentView === 'entfernungsberechnung-schule'"
        :school="selectedDistanceSchool"
        @back="currentView = 'entfernungsberechnung'"
        @show-address="(adresse) => { selectedDistanceAddress = adresse; currentView = 'entfernungsberechnung-adresse'; }"
      />
      <EntfernungsberechnungAdresse
        v-else-if="currentView === 'entfernungsberechnung-adresse'"
        :school="selectedDistanceSchool"
        :adresse="selectedDistanceAddress"
        :schoolId="selectedDistanceSchool?.id"
        @back="currentView = 'entfernungsberechnung-schule'"
      />
      <Support v-else-if="currentView === 'support'" />
    </main>

    <ChangePasswordModal :visible="showChangePasswordModal" @close="showChangePasswordModal = false" />
  </div>
</template>

<style scoped>
.header-subtitle {
  color: #94a3b8;
  font-size: 1rem;
  margin: 0.25rem 0 0 0;
  font-weight: 400;
}
</style>
