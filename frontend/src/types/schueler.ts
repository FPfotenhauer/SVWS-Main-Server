export interface SchuelerAuswahl {
  id: number;
  nachname?: string | null;
  vorname?: string | null;
  geburtsdatum?: string | null;
  status?: number | null;
}

export interface SchuelerAdresse {
  id: number;
  nachname?: string | null;
  vorname?: string | null;
  geburtsdatum?: string | null;
  strassenname?: string | null;
  hausnummer?: string | null;
  hausnummerZusatz?: string | null;
  plz?: string | null;
  ort?: string | null;
}

export interface DistanceResult {
  distance_meters?: number | null;
  distance_km?: number | null;
  time_milliseconds?: number | null;
  time_minutes?: number | null;
  polyline?: string | null;
  error?: string | null;
}
