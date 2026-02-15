package de.schultraeger.infrastructure.svws;

import de.schultraeger.application.port.out.NrwSchulkatalogeintragClient;
import de.schultraeger.application.port.out.NrwClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Implementation for fetching NRW school catalog data from Schulministerium APIs.
 */
@ApplicationScoped
public class NrwSchulkatalogeintragClientImpl implements NrwSchulkatalogeintragClient {
    private static final Logger LOG = Logger.getLogger(NrwSchulkatalogeintragClientImpl.class);

    private static final String KONTEN_URL = "https://www.schulministerium.nrw.de/BiPo/Schuldatei/SchuldateiDatenService/export/json/konten";
    private static final String KATALOG_URL = "https://www.schulministerium.nrw.de/BiPo/Schuldatei/SchuldateiDatenService/export/json/katalog";
    private static final int TIMEOUT_MS = 120000; // 2 minutes

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Map<String, Object>> fetchAllSchools() throws NrwClientException {
        LOG.info("Fetching all schools from NRW Schulministerium...");
        try {
            Map<String, Object> data = fetchJson(KONTEN_URL);
            return extractSchools(data);
        } catch (Exception e) {
            LOG.errorv("Failed to fetch schools from NRW API: {0}", e.getMessage());
            throw new NrwClientException("Failed to fetch schools from NRW API", e);
        }
    }

    @Override
    public List<Map<String, Object>> fetchKatalogEntries() throws NrwClientException {
        LOG.info("Fetching catalog entries from NRW Schulministerium...");
        try {
            return fetchJsonArray(KATALOG_URL);
        } catch (Exception e) {
            LOG.errorv("Failed to fetch catalog entries from NRW API: {0}", e.getMessage());
            throw new NrwClientException("Failed to fetch catalog entries from NRW API", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractSchools(Map<String, Object> data) {
        List<Map<String, Object>> schools = new ArrayList<>();
        
        if (data.containsKey("organisationseinheit")) {
            List<Map<String, Object>> orgs = (List<Map<String, Object>>) data.get("organisationseinheit");
            List<Map<String, Object>> addresses = (List<Map<String, Object>>) data.get("adressen");
            
            for (Map<String, Object> org : orgs) {
                try {
                    Map<String, Object> school = new HashMap<>();
                    school.put("schulnummer", org.get("schulnummer"));
                    
                    // Extract school name from grunddaten
                    if (org.containsKey("grunddaten")) {
                        Map<String, Object> grunddaten = (Map<String, Object>) org.get("grunddaten");
                        school.put("schulname", grunddaten.getOrDefault("kurzbezeichnung", ""));
                    } else {
                        school.put("schulname", "");
                    }
                    
                    // Find matching address
                    if (addresses != null) {
                        for (Map<String, Object> addr : addresses) {
                            if (org.get("schulnummer").equals(addr.get("schulnummer"))) {
                                school.put("strasse", addr.getOrDefault("strasse", null));
                                school.put("plz", addr.getOrDefault("postleitzahl", null));
                                school.put("ort", addr.getOrDefault("ort", null));
                                break;
                            }
                        }
                    }
                    
                    // Set additional fields
                    school.put("schultyp", null);
                    school.put("kreis", null);
                    school.put("schulamt", null);
                    school.put("telefon", null);
                    school.put("fax", null);
                    school.put("email", null);
                    school.put("homepage", null);
                    
                    schools.add(school);
                } catch (Exception e) {
                    LOG.warnv("Failed to process school record: {0}", e.getMessage());
                }
            }
        }
        
        LOG.infov("Extracted {0} schools", schools.size());
        return schools;
    }

    private Map<String, Object> fetchJson(String urlString) throws Exception {
        LOG.infov("Connecting to NRW API: {0}", urlString);
        URI uri = new URI(urlString);
        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
        connection.setRequestProperty("User-Agent", "SVWS-Main-Server/1.0");

        try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            LOG.info("Connected successfully, parsing JSON...");
            Map<String, Object> result = objectMapper.readValue(reader, new TypeReference<Map<String, Object>>() {});
            LOG.info("Successfully parsed JSON response");
            return result;
        } catch (Exception e) {
            LOG.errorv("Failed to fetch from NRW API: {0}", e.getMessage());
            throw e;
        }
    }

    private List<Map<String, Object>> fetchJsonArray(String urlString) throws Exception {
        LOG.infov("Connecting to NRW API: {0}", urlString);
        URI uri = new URI(urlString);
        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
        connection.setRequestProperty("User-Agent", "SVWS-Main-Server/1.0");

        try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            LOG.info("Connected successfully, parsing JSON...");
            List<Map<String, Object>> result = objectMapper.readValue(reader, new TypeReference<List<Map<String, Object>>>() {});
            LOG.infov("Successfully fetched {0} records", result.size());
            return result;
        } catch (Exception e) {
            LOG.errorv("Failed to fetch from NRW API: {0}", e.getMessage());
            throw e;
        }
    }
}
