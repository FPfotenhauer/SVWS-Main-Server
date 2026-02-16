package de.schultraeger.application.port.out;

import de.schultraeger.domain.NrwSchulkatalogeintrag;

import java.util.List;
import java.util.Optional;

/**
 * Port for NRW school catalog repository operations.
 */
public interface NrwSchulkatalogeintragRepository {
    List<NrwSchulkatalogeintrag> findAll(int offset, int limit);

    List<NrwSchulkatalogeintrag> findAll(int offset, int limit, String sortBy, String sortDir);

    List<NrwSchulkatalogeintrag> search(String query, int offset, int limit);

    List<NrwSchulkatalogeintrag> search(String query, int offset, int limit, String sortBy, String sortDir);

    Optional<NrwSchulkatalogeintrag> findBySchulnummer(String schulnummer);

    void save(NrwSchulkatalogeintrag eintrag);

    void saveAll(List<NrwSchulkatalogeintrag> eintraege);

    void clearAll();

    long getTotalCount();

    long countSearch(String query);
}
