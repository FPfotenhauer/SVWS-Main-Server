package de.schultraeger.application;

import de.schultraeger.application.port.out.NrwSchulkatalogeintragClient;
import de.schultraeger.application.port.out.NrwSchulkatalogeintragRepository;
import de.schultraeger.application.port.out.NrwClientException;
import de.schultraeger.domain.NrwSchulkatalogeintrag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for NRW school catalog management.
 */
@ApplicationScoped
public class NrwSchulkatalogeintragService {
    private static final Logger LOG = Logger.getLogger(NrwSchulkatalogeintragService.class);

    private final NrwSchulkatalogeintragRepository repository;
    private final NrwSchulkatalogeintragClient client;

    public NrwSchulkatalogeintragService(NrwSchulkatalogeintragRepository repository,
                                       NrwSchulkatalogeintragClient client) {
        this.repository = repository;
        this.client = client;
    }

    /**
     * Get paginated list of all schools.
     */
    public List<NrwSchulkatalogeintrag> listAllSchools(int page, int pageSize) {
        int offset = page * pageSize;
        return repository.findAll(offset, pageSize);
    }

    /**
     * Get paginated list of all schools with sorting.
     */
    public List<NrwSchulkatalogeintrag> listAllSchools(int page, int pageSize, String sortBy, String sortDir) {
        int offset = page * pageSize;
        return repository.findAll(offset, pageSize, sortBy, sortDir);
    }

    /**
     * Search schools by query.
     */
    public List<NrwSchulkatalogeintrag> search(String query, int page, int pageSize) {
        int offset = page * pageSize;
        return repository.search(query, offset, pageSize);
    }

    /**
     * Search schools by query with sorting.
     */
    public List<NrwSchulkatalogeintrag> search(String query, int page, int pageSize, String sortBy, String sortDir) {
        int offset = page * pageSize;
        return repository.search(query, offset, pageSize, sortBy, sortDir);
    }

    /**
     * Get total count of schools.
     */
    public long getTotalCount() {
        return repository.getTotalCount();
    }

    /**
     * Get count of schools matching search query.
     */
    public long getSearchResultCount(String query) {
        return repository.countSearch(query);
    }

    /**
     * Refresh the NRW school catalog from external API.
     */
    @Transactional
    public void refreshCatalog() throws NrwClientException {
        LOG.info("Starting NRW school catalog refresh...");
        long startTime = System.currentTimeMillis();

        try {
            List<Map<String, Object>> schoolsData = client.fetchAllSchools();
            LOG.infov("Fetched {0} schools from NRW API", schoolsData.size());

            // Clear existing data
            repository.clearAll();

            // Transform and save new data
            List<NrwSchulkatalogeintrag> entries = transformSchoolsData(schoolsData);
            repository.saveAll(entries);

            long duration = System.currentTimeMillis() - startTime;
            LOG.infov("NRW school catalog refresh completed successfully. Entries: {0}, Duration: {1}ms",
                    entries.size(), duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOG.errorv("NRW school catalog refresh failed after {0}ms", duration);
            throw e;
        }
    }

    private List<NrwSchulkatalogeintrag> transformSchoolsData(List<Map<String, Object>> rawData) {
        List<NrwSchulkatalogeintrag> entries = new ArrayList<>();

        for (Map<String, Object> schoolData : rawData) {
            try {
                NrwSchulkatalogeintrag entry = new NrwSchulkatalogeintrag(
                        UUID.randomUUID(),
                        getStringValue(schoolData, "schulnummer"),
                    getStringValue(schoolData, "oeart"),
                    getStringValue(schoolData, "amtsbez1"),
                    getStringValue(schoolData, "amtsbez2"),
                    getStringValue(schoolData, "amtsbez3"),
                        getStringValue(schoolData, "schultraegernummer"),
                        getStringValue(schoolData, "schultraegername"),
                        getStringValue(schoolData, "schulname"),
                        getStringValue(schoolData, "schultyp"),
                        getStringValue(schoolData, "strasse"),
                        getStringValue(schoolData, "plz"),
                        getStringValue(schoolData, "ort"),
                        getStringValue(schoolData, "kreis"),
                        getStringValue(schoolData, "aufloesung"),
                        getStringValue(schoolData, "schulamt"),
                        getStringValue(schoolData, "telefon"),
                        getStringValue(schoolData, "fax"),
                        getStringValue(schoolData, "email"),
                        getStringValue(schoolData, "homepage"),
                        null, // createdAt
                        null  // updatedAt
                );
                entries.add(entry);
            } catch (Exception e) {
                LOG.warnv("Failed to transform school data: {0}", e.getMessage());
            }
        }

        return entries;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
}
