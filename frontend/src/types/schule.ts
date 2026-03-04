export interface Schule {
  id: string;
  svwsServerId: string;
  svwsServerName: string;
  svwsSchema: string;
  svwsUsername?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SchuleRequest {
  svwsServerId: string;
  svwsSchema: string;
  svwsUsername?: string;
  svwsPassword?: string;
}

export interface SchuleStammdatenResponse {
  id: string;
  schema: string;
  serverName: string;
  schulnummer?: number | null;
  bezeichnung1?: string | null;
  schulform?: string | null;
  error?: string | null;
}
