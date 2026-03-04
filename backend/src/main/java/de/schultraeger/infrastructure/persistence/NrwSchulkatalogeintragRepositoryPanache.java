package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.port.out.NrwSchulkatalogeintragRepository;
import de.schultraeger.domain.NrwSchulkatalogeintrag;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Panache implementation of NrwSchulkatalogeintragRepository.
 */
@ApplicationScoped
public class NrwSchulkatalogeintragRepositoryPanache implements PanacheRepository<NrwSchulkatalogeintragEntity>, NrwSchulkatalogeintragRepository {

    @Override
    public List<NrwSchulkatalogeintrag> findAll(int offset, int limit) {
        return find("oeart = :oeart ORDER BY schulname", Parameters.with("oeart", "1")).range(offset, offset + limit - 1)
                .list()
                .stream()
                .map(NrwSchulkatalogeintragMapper::toDomain)
                .toList();
    }

    @Override
    public List<NrwSchulkatalogeintrag> findAll(int offset, int limit, String sortBy, String sortDir) {
        String orderBy = buildOrderByClause(sortBy, sortDir);
        return find("oeart = :oeart ORDER BY " + orderBy, Parameters.with("oeart", "1")).range(offset, offset + limit - 1)
                .list()
                .stream()
                .map(NrwSchulkatalogeintragMapper::toDomain)
                .toList();
    }

    @Override
    public List<NrwSchulkatalogeintrag> search(String query, int offset, int limit) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        return find(
            "oeart = :oeart AND (LOWER(schulname) LIKE :query OR LOWER(ort) LIKE :query OR LOWER(kreis) LIKE :query OR schulnummer LIKE :query) " +
                        "ORDER BY schulname",
            Parameters.with("query", searchQuery).and("oeart", "1")
        ).range(offset, offset + limit - 1)
                .list()
                .stream()
                .map(NrwSchulkatalogeintragMapper::toDomain)
                .toList();
    }

    @Override
    public List<NrwSchulkatalogeintrag> search(String query, int offset, int limit, String sortBy, String sortDir) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        String orderBy = buildOrderByClause(sortBy, sortDir);
        return find(
            "oeart = :oeart AND (LOWER(schulname) LIKE :query OR LOWER(ort) LIKE :query OR LOWER(kreis) LIKE :query OR schulnummer LIKE :query) " +
                        "ORDER BY " + orderBy,
            Parameters.with("query", searchQuery).and("oeart", "1")
        ).range(offset, offset + limit - 1)
                .list()
                .stream()
                .map(NrwSchulkatalogeintragMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<NrwSchulkatalogeintrag> findBySchulnummer(String schulnummer) {
        return find("schulnummer", schulnummer)
                .firstResultOptional()
                .map(NrwSchulkatalogeintragMapper::toDomain);
    }

    @Override
    @Transactional
    public void save(NrwSchulkatalogeintrag eintrag) {
        NrwSchulkatalogeintragEntity entity = NrwSchulkatalogeintragMapper.toEntity(eintrag);
        persistAndFlush(entity);
    }

    @Override
    @Transactional
    public void saveAll(List<NrwSchulkatalogeintrag> eintraege) {
        List<NrwSchulkatalogeintragEntity> entities = eintraege.stream()
                .map(NrwSchulkatalogeintragMapper::toEntity)
                .toList();
        persist(entities);
        flush();
    }

    @Override
    @Transactional
    public void clearAll() {
        delete("1=1");
        flush();
    }

    @Override
    public long getTotalCount() {
        return count("oeart", "1");
    }

    @Override
    public long countSearch(String query) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        return count(
                "oeart = :oeart AND (LOWER(schulname) LIKE :query OR LOWER(ort) LIKE :query OR LOWER(kreis) LIKE :query OR schulnummer LIKE :query)",
                Parameters.with("query", searchQuery).and("oeart", "1")
        );
    }

    private String buildOrderByClause(String sortBy, String sortDir) {
        // Validate and sanitize column name to prevent SQL injection
        String validatedColumn = switch (sortBy) {
            case "schulname" -> "schulname";
            case "ort" -> "ort";
            case "kreis" -> "kreis";
            case "schultyp" -> "schultyp";
            case "plz" -> "plz";
            case "schulnummer" -> "CAST(schulnummer AS integer)";
            default -> "schulname";
        };

        // Validate sort direction
        String validatedDir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        return validatedColumn + " " + validatedDir;
    }
}
