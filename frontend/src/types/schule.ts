import type { SchuleStatus, SyncStatus } from "./status";

export interface Schule {
  id: string;
  name: string;
  schulnummer?: number | null;
  svwsUrl: string;
  svwsSchema: string;
  svwsUsername: string;
  status: SchuleStatus;
  lastSyncAt?: string | null;
  lastSyncStatus?: SyncStatus | null;
  lastError?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SchuleRequest {
  name: string;
  svwsUrl: string;
  svwsSchema: string;
  svwsUsername: string;
  svwsPassword: string;
}
