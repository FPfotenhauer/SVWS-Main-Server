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
        console.log("Schools loaded from API:", response.data);
        this.items = response.data;
        console.log("Store items after load:", this.items);
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
    },
    async update(id: string, payload: Partial<SchuleRequest>) {
      this.error = "";
      const response = await api.put<Schule>(`/api/schulen/${id}`, payload);
      const index = this.items.findIndex(s => s.id === id);
      if (index >= 0) {
        this.items[index] = response.data;
      }
      return response.data;
    }
  }
});
