export interface Webnotenmanager {
  id: string;
  schuleId: string;
  notenserverBaseUrl: string;
  oauthSecretEncrypted: string;
  createdAt: string;
  updatedAt: string;
}

export interface WebnotenmanagerRequest {
  notenserverBaseUrl: string;
  oauthSecretEncrypted: string;
}

export interface WebnotenmanagerWithSchuleData {
  id: string;
  schuleId: string;
  schulnummer?: number | null;
  schulname?: string | null;
  notenserverBaseUrl: string;
  oauthSecretEncrypted: string;
  createdAt: string;
  updatedAt: string;
}
