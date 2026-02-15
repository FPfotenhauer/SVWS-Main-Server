export type ServerStatus =
  | "UNTESTED"
  | "CONNECTED"
  | "INVALID_CREDENTIALS"
  | "UNREACHABLE"
  | "ERROR";

export interface SvwsServer {
  id: string;
  name: string;
  baseUrl: string;
  username: string;
  status: ServerStatus;
  lastError?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SvwsServerRequest {
  name: string;
  baseUrl: string;
  username: string;
  password: string;
}

export interface SvwsSchoolInfo {
  schulnummer: number;
  name: string;
  schema: string;
  ort: string;
  plz: string;
  _uid?: string; // Frontend-generated unique ID
}
