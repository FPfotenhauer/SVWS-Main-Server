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
  bike_distance_meters?: number | null;
  bike_distance_km?: number | null;
  bike_time_milliseconds?: number | null;
  bike_time_minutes?: number | null;
  foot_distance_meters?: number | null;
  foot_distance_km?: number | null;
  foot_time_milliseconds?: number | null;
  foot_time_minutes?: number | null;
  polyline?: string | null;
  error?: string | null;
}
