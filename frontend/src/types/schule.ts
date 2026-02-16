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
  // Address information
  strasse?: string | null;
  hausnummer?: string | null;
  hausnummerZusatz?: string | null;
  plz?: string | null;
  ort?: string | null;
  // Contact information
  telefon?: string | null;
  fax?: string | null;
  email?: string | null;
  homepage?: string | null;
  // Administrative information
  schulleiter?: string | null;
  schulleiterTelefon?: string | null;
  schulleiterEmail?: string | null;
  // Region information
  kreis?: string | null;
  schulamt?: string | null;
  // Additional metadata
  schulnummer2?: string | null;
  schulstatus?: string | null;
}

export interface SchuleRequest {
  name: string;
  svwsUrl: string;
  svwsSchema: string;
  svwsUsername: string;
  svwsPassword: string;
}
