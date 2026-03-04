export interface NrwSchulkatalogeintrag {
  id: string;
  schulnummer: string;
  oeart: string | null;
  amtsbez1: string | null;
  amtsbez2: string | null;
  amtsbez3: string | null;
  schultraegernummer: string | null;
  schultraegername: string | null;
  schulname: string;
  schultyp: string | null;
  strasse: string | null;
  plz: string | null;
  ort: string | null;
  kreis: string | null;
  aufloesung: string | null;
  schulamt: string | null;
  telefon: string | null;
  fax: string | null;
  email: string | null;
  homepage: string | null;
}

export interface SchoolListResponse {
  schools: NrwSchulkatalogeintrag[];
  total: number;
  page: number;
  pageSize: number;
}
