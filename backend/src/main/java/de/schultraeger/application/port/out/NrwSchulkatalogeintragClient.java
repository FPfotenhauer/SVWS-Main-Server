package de.schultraeger.application.port.out;

import java.util.List;
import java.util.Map;

/**
 * Port for accessing NRW Schulministerium APIs.
 */
public interface NrwSchulkatalogeintragClient {
    /**
     * Fetches the list of all schools in NRW.
     * Returns a list of school data as maps (keys: schulnummer, schulname, etc.)
     */
    List<Map<String, Object>> fetchAllSchools() throws NrwClientException;

    /**
     * Fetches the catalog entries (katalog).
     * Returns a list of catalog data as maps.
     */
    List<Map<String, Object>> fetchKatalogEntries() throws NrwClientException;
}
