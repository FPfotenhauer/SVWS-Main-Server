import { defineStore } from "pinia";
import api from "../services/api";
import type { SvwsServer, SvwsServerRequest, SvwsSchoolInfo } from "../types/svwsServer";

export const useSvwsServersStore = defineStore("svwsServers", {
  state: () => ({
    servers: [] as SvwsServer[],
    selectedServer: null as SvwsServer | null,
    schools: [] as SvwsSchoolInfo[],
    loading: false,
    loadingSchools: false,
    error: ""
  }),
  actions: {
    async loadServers() {
      this.loading = true;
      this.error = "";
      try {
        const response = await api.get<SvwsServer[]>("/api/svws-servers");
        this.servers = response.data;
      } catch (err) {
        this.error = "Failed to load SVWS servers";
      } finally {
        this.loading = false;
      }
    },
    async createServer(payload: SvwsServerRequest) {
      this.error = "";
      try {
        const response = await api.post<SvwsServer>("/api/svws-servers", payload);
        this.servers = [response.data, ...this.servers];
        return response.data;
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Failed to create server";
        throw err;
      }
    },
    async deleteServer(id: string) {
      this.error = "";
      try {
        await api.delete(`/api/svws-servers/${id}`);
        this.servers = this.servers.filter(s => s.id !== id);
        if (this.selectedServer?.id === id) {
          this.selectedServer = null;
          this.schools = [];
        }
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Failed to delete server";
        throw err;
      }
    },
    async loadSchoolsFromServer(serverId: string) {
      console.log("Store: loadSchoolsFromServer called with:", serverId);
      this.loadingSchools = true;
      this.error = "";
      try {
        console.log("Store: Making API call to /api/svws-servers/" + serverId + "/schools");
        const response = await api.get<SvwsSchoolInfo[]>(`/api/svws-servers/${serverId}/schools`);
        console.log("Store: API response:", response.data);
        this.schools = response.data;
        this.selectedServer = this.servers.find(s => s.id === serverId) || null;
        console.log("Store: selectedServer set to:", this.selectedServer?.name);
      } catch (err) {
        console.error("Store: Error loading schools:", err);
        this.error = err instanceof Error ? err.message : "Failed to load schools";
        this.schools = [];
        throw err;
      } finally {
        this.loadingSchools = false;
      }
    },
    clearSelection() {
      this.selectedServer = null;
      this.schools = [];
    }
  }
});
