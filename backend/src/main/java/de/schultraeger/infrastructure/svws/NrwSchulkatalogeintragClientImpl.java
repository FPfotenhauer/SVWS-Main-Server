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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private static final DateTimeFormatter NRW_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Map<String, Object>> fetchAllSchools() throws NrwClientException {
        LOG.info("Fetching all schools from NRW Schulministerium...");
        try {
            Map<String, Object> data = fetchJson(KONTEN_URL);
            List<Map<String, Object>> katalogEntries = fetchJsonArray(KATALOG_URL);
            Map<String, String> erreichbarkeitTypeByKey = buildErreichbarkeitTypeMap(katalogEntries);
            Map<String, String> traegerByKey = buildTraegerMap(katalogEntries);
            return extractSchools(data, erreichbarkeitTypeByKey, traegerByKey);
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
    private List<Map<String, Object>> extractSchools(
            Map<String, Object> data,
            Map<String, String> erreichbarkeitTypeByKey,
            Map<String, String> traegerByKey
    ) {
        List<Map<String, Object>> schools = new ArrayList<>();
        Map<String, Map<String, Object>> schoolMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        if (data.containsKey("organisationseinheit")) {
            List<Map<String, Object>> orgs = (List<Map<String, Object>>) data.get("organisationseinheit");
            Map<String, String> orgKurzByNummer = buildOrgKurzByNummer(orgs);
            
            for (Map<String, Object> org : orgs) {
                try {
                    String schulnummer = (String) org.get("schulnummer");
                    if (schulnummer == null || schulnummer.isEmpty()) {
                        continue;
                    }
                    
                    Map<String, Object> school = new HashMap<>();
                    school.put("schulnummer", schulnummer);
                    school.put("amtsbez1", org.getOrDefault("amtsbez1", null));
                    school.put("amtsbez2", org.getOrDefault("amtsbez2", null));
                    school.put("amtsbez3", org.getOrDefault("amtsbez3", null));
                    school.put("aufloesung", org.getOrDefault("aufloesung", null));
                    
                    // Extract school name from grunddaten
                    if (org.containsKey("grunddaten")) {
                        Map<String, Object> grunddaten = (Map<String, Object>) org.get("grunddaten");
                        school.put("schulname", grunddaten.getOrDefault("kurzbezeichnung", ""));
                        String traegerNummer = toStringOrNull(grunddaten.get("schultraegernummer"));
                        String traegerName = traegerNummer != null ? traegerByKey.get(traegerNummer) : null;
                        String traegerKurz = traegerNummer != null ? orgKurzByNummer.get(traegerNummer) : null;
                        school.put("schulamt", traegerName);
                        school.put("schultraegernummer", traegerNummer);
                        school.put("schultraegername", traegerKurz);
                    } else {
                        school.put("schulname", "");
                        school.put("schulamt", null);
                        school.put("schultraegernummer", null);
                        school.put("schultraegername", null);
                    }
                    
                    // Extract address from nested adressen array
                    if (org.containsKey("adressen")) {
                        List<Map<String, Object>> addresses = (List<Map<String, Object>>) org.get("adressen");
                        Map<String, Object> addr = selectValidAdresse(addresses, today);
                        if (addr != null) {
                            school.put("strasse", addr.getOrDefault("strasse", null));
                            school.put("plz", addr.getOrDefault("postleitzahl", null));
                            school.put("ort", addr.getOrDefault("ort", null));
                        }
                    }
                    
                    // Extract schultyp from schulform if available
                    String schultyp = extractSchultyp(org);
                    school.put("schultyp", schultyp != null ? schultyp : "");
                    
                    // Extract kreis from addresses regionalschluessel
                    String kreis = extractKreis(org);
                    school.put("kreis", kreis != null ? kreis : "");
                    
                    // Extract contact information from erreichbarkeiten
                    String preferredLiegenschaft = extractPreferredLiegenschaft(org, today);
                    extractContactInfo(org, school, erreichbarkeitTypeByKey, preferredLiegenschaft, today);
                    
                    // Keep only the latest valid record per school number
                    schoolMap.put(schulnummer, school);
                } catch (Exception e) {
                    LOG.warnv("Failed to extract school data: {0}", e.getMessage());
                }
            }
        }
        
        schools.addAll(schoolMap.values());
        LOG.infov("Extracted {0} schools", schools.size());
        return schools;
    }

    @SuppressWarnings("unchecked")
    private void extractContactInfo(
            Map<String, Object> org,
            Map<String, Object> school,
            Map<String, String> erreichbarkeitTypeByKey,
            String preferredLiegenschaft,
            LocalDate today
    ) {
        try {
            if (org.containsKey("erreichbarkeiten")) {
                List<Map<String, Object>> erreichbarkeiten = (List<Map<String, Object>>) org.get("erreichbarkeiten");
                List<Map<String, Object>> validErreichbarkeiten = filterValidEntries(erreichbarkeiten, today);
                validErreichbarkeiten.sort((a, b) -> Integer.compare(
                        erreichbarkeitScore(b, preferredLiegenschaft),
                        erreichbarkeitScore(a, preferredLiegenschaft)
                ));

                for (Map<String, Object> erreichbarkeit : validErreichbarkeiten) {
                    String codeKey = toStringOrNull(erreichbarkeit.get("codekey"));
                    String codeValue = toStringOrNull(erreichbarkeit.get("codewert"));
                    if (codeKey == null || codeValue == null || codeValue.isEmpty()) {
                        continue;
                    }

                    String type = erreichbarkeitTypeByKey.get(codeKey);
                    if (type == null) {
                        type = fallbackErreichbarkeitType(codeKey);
                    }
                    if (type == null) {
                        continue;
                    }

                    switch (type) {
                        case "telefon":
                            if (school.get("telefon") == null) {
                                school.put("telefon", codeValue);
                            }
                            break;
                        case "fax":
                            if (school.get("fax") == null) {
                                school.put("fax", codeValue);
                            }
                            break;
                        case "email":
                            if (school.get("email") == null) {
                                school.put("email", codeValue);
                            }
                            break;
                        case "homepage":
                            if (school.get("homepage") == null) {
                                school.put("homepage", codeValue);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            
            // Set defaults for any missing fields
            if (school.get("schulamt") == null) school.put("schulamt", null);
            if (school.get("telefon") == null) school.put("telefon", null);
            if (school.get("fax") == null) school.put("fax", null);
            if (school.get("email") == null) school.put("email", null);
            if (school.get("homepage") == null) school.put("homepage", null);
        } catch (Exception e) {
            LOG.warnv("Error extracting contact info: {0}", e.getMessage());
        }
    }

    private Map<String, String> buildErreichbarkeitTypeMap(List<Map<String, Object>> katalogEntries) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> entry : katalogEntries) {
            String katalog = toStringOrNull(entry.get("katalog"));
            if (!"Erreichbarkeit".equalsIgnoreCase(katalog)) {
                continue;
            }
            String code = toStringOrNull(entry.get("wert"));
            String bezeichnung = toStringOrNull(entry.get("bezeichnung"));
            if (code == null || bezeichnung == null) {
                continue;
            }
            String type = mapErreichbarkeitType(bezeichnung);
            if (type != null) {
                map.put(code, type);
            }
        }
        return map;
    }

    private Map<String, String> buildTraegerMap(List<Map<String, Object>> katalogEntries) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> entry : katalogEntries) {
            String katalog = toStringOrNull(entry.get("katalog"));
            if (!"Traeger".equalsIgnoreCase(katalog)) {
                continue;
            }
            String code = toStringOrNull(entry.get("wert"));
            String bezeichnung = toStringOrNull(entry.get("bezeichnung"));
            if (code != null && bezeichnung != null) {
                map.put(code, bezeichnung);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> buildOrgKurzByNummer(List<Map<String, Object>> orgs) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> org : orgs) {
            String nummer = toStringOrNull(org.get("schulnummer"));
            if (nummer == null) {
                continue;
            }
            if (org.containsKey("grunddaten")) {
                Map<String, Object> grunddaten = (Map<String, Object>) org.get("grunddaten");
                String kurz = toStringOrNull(grunddaten.get("kurzbezeichnung"));
                if (kurz != null) {
                    map.put(nummer, kurz);
                }
            }
        }
        return map;
    }

    private String mapErreichbarkeitType(String bezeichnung) {
        String normalized = bezeichnung.toLowerCase();
        if (normalized.contains("e-mail") || normalized.contains("email") || normalized.contains("de-mail")) {
            return "email";
        }
        if (normalized.contains("telefon")) {
            return "telefon";
        }
        if (normalized.contains("fax")) {
            return "fax";
        }
        if (normalized.contains("web")) {
            return "homepage";
        }
        return null;
    }

    private String fallbackErreichbarkeitType(String codeKey) {
        switch (codeKey) {
            case "01":
            case "0":
                return "email";
            case "02":
            case "03":
                return "telefon";
            case "04":
                return "fax";
            case "09":
                return "homepage";
            default:
                return null;
        }
    }

    private Map<String, Object> selectValidAdresse(List<Map<String, Object>> addresses, LocalDate today) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> valid = filterValidEntries(addresses, today);
        if (valid.isEmpty()) {
            return addresses.get(0);
        }
        for (Map<String, Object> addr : valid) {
            String isHauptstandort = toStringOrNull(addr.get("hauptstandortadresse"));
            if ("1".equals(isHauptstandort)) {
                return addr;
            }
        }
        return valid.get(0);
    }

    @SuppressWarnings("unchecked")
    private String extractPreferredLiegenschaft(Map<String, Object> org, LocalDate today) {
        if (!org.containsKey("adressen")) {
            return null;
        }
        List<Map<String, Object>> addresses = (List<Map<String, Object>>) org.get("adressen");
        Map<String, Object> address = selectValidAdresse(addresses, today);
        if (address == null) {
            return null;
        }
        return toStringOrNull(address.get("liegenschaft"));
    }

    private List<Map<String, Object>> filterValidEntries(List<Map<String, Object>> entries, LocalDate today) {
        List<Map<String, Object>> valid = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            if (isValidForDate(entry, today)) {
                valid.add(entry);
            }
        }
        return valid;
    }

    private boolean isValidForDate(Map<String, Object> entry, LocalDate today) {
        String gueltigab = toStringOrNull(entry.get("gueltigab"));
        String gueltigbis = toStringOrNull(entry.get("gueltigbis"));
        LocalDate validFrom = parseNswDate(gueltigab);
        LocalDate validUntil = parseNswDate(gueltigbis);

        if (validFrom != null && today.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && today.isAfter(validUntil)) {
            return false;
        }
        return true;
    }

    private LocalDate parseNswDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String datePart = value.trim();
        int spaceIndex = datePart.indexOf(' ');
        if (spaceIndex > 0) {
            datePart = datePart.substring(0, spaceIndex);
        }
        try {
            return LocalDate.parse(datePart, NRW_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private int erreichbarkeitScore(Map<String, Object> erreichbarkeit, String preferredLiegenschaft) {
        int score = 0;
        String kommgruppe = toStringOrNull(erreichbarkeit.get("kommgruppe"));
        String liegenschaft = toStringOrNull(erreichbarkeit.get("liegenschaft"));
        if ("1".equals(kommgruppe)) {
            score += 2;
        }
        if (preferredLiegenschaft != null && preferredLiegenschaft.equals(liegenschaft)) {
            score += 1;
        }
        return score;
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : String.valueOf(value).trim();
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
            Object result = objectMapper.readValue(reader, new TypeReference<Object>() {});
            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) result;
                LOG.infov("Successfully fetched {0} records", list.size());
                return list;
            }
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) result;
                Object katalog = map.get("katalog");
                if (katalog instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) katalog;
                    LOG.infov("Successfully fetched {0} records", list.size());
                    return list;
                }
            }
            throw new IllegalStateException("Unexpected katalog response format");
        } catch (Exception e) {
            LOG.errorv("Failed to fetch from NRW API: {0}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractSchultyp(Map<String, Object> org) {
        try {
            if (org.containsKey("grunddaten")) {
                Map<String, Object> grunddaten = (Map<String, Object>) org.get("grunddaten");
                if (grunddaten.containsKey("schulform")) {
                    List<Map<String, Object>> schulformen = (List<Map<String, Object>>) grunddaten.get("schulform");
                    if (!schulformen.isEmpty()) {
                        return (String) schulformen.get(0).get("schulformwert");
                    }
                }
            }
        } catch (Exception e) {
            LOG.debugv("Failed to extract schultyp: {0}", e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractKreis(Map<String, Object> org) {
        try {
            if (org.containsKey("adressen")) {
                List<Map<String, Object>> addresses = (List<Map<String, Object>>) org.get("adressen");
                if (!addresses.isEmpty()) {
                    Map<String, Object> addr = addresses.get(0);
                    String regionalschluessel = (String) addr.get("regionalschluessel");
                    if (regionalschluessel != null && regionalschluessel.length() >= 5) {
                        // Regionalschluessel format: LLLLL... (first 5 digits identify the district)
                        return regionalschluessel.substring(0, 5);
                    }
                }
            }
        } catch (Exception e) {
            LOG.debugv("Failed to extract kreis: {0}", e.getMessage());
        }
        return null;
    }
}
