export interface NrwSchulkatalogeintrag {
  id: string;
  schulnummer: string;
  schulname: string;
  schultyp: string | null;
  strasse: string | null;
  plz: string | null;
  ort: string | null;
  kreis: string | null;
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
