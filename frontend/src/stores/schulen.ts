import { defineStore } from "pinia";
import api from "../services/api";
import type { Schule, SchuleRequest } from "../types/schule";

export const useSchulenStore = defineStore("schulen", {
  state: () => ({
    items: [] as Schule[],
    loading: false,
    error: ""
  }),
  actions: {
    async load() {
      this.loading = true;
      this.error = "";
      try {
        const response = await api.get<Schule[]>("/api/schulen");
        this.items = response.data;
      } catch (err) {
        this.error = "Failed to load schools";
      } finally {
        this.loading = false;
      }
    },
    async create(payload: SchuleRequest) {
      this.error = "";
      const response = await api.post<Schule>("/api/schulen", payload);
      this.items = [response.data, ...this.items];
    }
  }
});
