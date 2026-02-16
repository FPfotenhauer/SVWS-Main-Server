export interface Schule {
  id: string;
  svwsServerId: string;
  svwsServerName: string;
  svwsSchema: string;
  createdAt: string;
  updatedAt: string;
}

export interface SchuleRequest {
  svwsServerId: string;
  svwsSchema: string;
  svwsUsername?: string;
  svwsPassword?: string;
}
