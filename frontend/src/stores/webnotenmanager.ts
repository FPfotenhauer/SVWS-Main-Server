import { defineStore } from "pinia";
import api from "../services/api";
import type { Webnotenmanager, WebnotenmanagerRequest, WebnotenmanagerWithSchuleData } from "../types/webnotenmanager";
import type { SchuleStammdatenResponse } from "../types/schule";

export const useWebnotenmanagerStore = defineStore("webnotenmanager", {
  state: () => ({
    items: [] as WebnotenmanagerWithSchuleData[],
    loading: false,
    error: ""
  }),
  actions: {
    async load() {
      this.loading = true;
      this.error = "";
      try {
        const response = await api.get<Webnotenmanager[]>("/api/webnotenmanager");
        console.log("Webnotenmanager configs loaded from API:", response.data);
        
        // Enrich each config with stammdaten from the associated schule
        this.items = await Promise.all(
          response.data.map(async (config) => {
            try {
              const stammdaten = await this.getStammdaten(config.schuleId);
              return {
                id: config.id,
                schuleId: config.schuleId,
                schulnummer: stammdaten?.schulnummer,
                schulname: stammdaten?.bezeichnung1,
                notenserverBaseUrl: config.notenserverBaseUrl,
                oauthSecretEncrypted: config.oauthSecretEncrypted,
                createdAt: config.createdAt,
                updatedAt: config.updatedAt
              };
            } catch (err) {
              console.error(`Failed to load stammdaten for schule ${config.schuleId}:`, err);
              return {
                id: config.id,
                schuleId: config.schuleId,
                schulnummer: null,
                schulname: null,
                notenserverBaseUrl: config.notenserverBaseUrl,
                oauthSecretEncrypted: config.oauthSecretEncrypted,
                createdAt: config.createdAt,
                updatedAt: config.updatedAt
              };
            }
          })
        );
        
        console.log("Store items after load:", this.items);
      } catch (err) {
        this.error = "Failed to load webnotenmanager configurations";
        console.error(this.error, err);
      } finally {
        this.loading = false;
      }
    },
    
    async getStammdaten(schuleId: string): Promise<SchuleStammdatenResponse | null> {
      try {
        const response = await api.get<SchuleStammdatenResponse>(`/api/schulen/${schuleId}/stammdaten`);
        return response.data;
      } catch (err) {
        console.error(`Failed to load stammdaten for schule ${schuleId}:`, err);
        return null;
      }
    },
    
    async create(schuleId: string, payload: WebnotenmanagerRequest) {
      this.error = "";
      try {
        const response = await api.post<Webnotenmanager>(`/api/webnotenmanager/${schuleId}`, payload);
        const stammdaten = await this.getStammdaten(schuleId);
        
        const newConfig: WebnotenmanagerWithSchuleData = {
          id: response.data.id,
          schuleId: response.data.schuleId,
          schulnummer: stammdaten?.schulnummer,
          schulname: stammdaten?.bezeichnung1,
          notenserverBaseUrl: response.data.notenserverBaseUrl,
          oauthSecretEncrypted: response.data.oauthSecretEncrypted,
          createdAt: response.data.createdAt,
          updatedAt: response.data.updatedAt
        };
        this.items = [newConfig, ...this.items];
        return newConfig;
      } catch (err) {
        this.error = `Failed to create webnotenmanager config: ${err instanceof Error ? err.message : 'Unknown error'}`;
        throw err;
      }
    },
    
    async update(id: string, payload: WebnotenmanagerRequest) {
      this.error = "";
      try {
        const response = await api.put<Webnotenmanager>(`/api/webnotenmanager/${id}`, payload);
        const index = this.items.findIndex(c => c.id === id);
        if (index >= 0) {
          const stammdaten = await this.getStammdaten(this.items[index].schuleId);
          this.items[index] = {
            id: response.data.id,
            schuleId: response.data.schuleId,
            schulnummer: stammdaten?.schulnummer,
            schulname: stammdaten?.bezeichnung1,
            notenserverBaseUrl: response.data.notenserverBaseUrl,
            oauthSecretEncrypted: response.data.oauthSecretEncrypted,
            createdAt: response.data.createdAt,
            updatedAt: response.data.updatedAt
          };
        }
        return response.data;
      } catch (err) {
        this.error = `Failed to update webnotenmanager config: ${err instanceof Error ? err.message : 'Unknown error'}`;
        throw err;
      }
    },
    
    async delete(id: string) {
      this.error = "";
      try {
        await api.delete(`/api/webnotenmanager/${id}`);
        this.items = this.items.filter(c => c.id !== id);
      } catch (err) {
        this.error = `Failed to delete webnotenmanager config: ${err instanceof Error ? err.message : 'Unknown error'}`;
        throw err;
      }
    }
  }
});
