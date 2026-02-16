package de.schultraeger.application;

import de.schultraeger.application.dto.SchuleCreateData;
import de.schultraeger.application.dto.SchuleUpdateData;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SchuleRepository;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsClientException;
import de.schultraeger.application.security.TenantContext;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SchuleStatus;
import de.schultraeger.domain.SyncStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for school management and SVWS sync.
 */
@ApplicationScoped
public class SchuleService {
    private static final Logger LOG = Logger.getLogger(SchuleService.class);
    private final SchuleRepository repository;
    private final SvwsClient svwsClient;
    private final TenantContext tenantContext;
    private final PasswordCipher passwordCipher;

    public SchuleService(SchuleRepository repository,
                         SvwsClient svwsClient,
                         TenantContext tenantContext,
                         PasswordCipher passwordCipher) {
        this.repository = repository;
        this.svwsClient = svwsClient;
        this.tenantContext = tenantContext;
        this.passwordCipher = passwordCipher;
    }

    public List<Schule> list() {
        return repository.findAll(tenantContext.getTenantId());
    }

    public SvwsSchuleInfo getSvwsSchoolInfo(UUID schuleId) throws SchuleNotFoundException {
        Schule schule = repository.findById(tenantContext.getTenantId(), schuleId)
                .orElseThrow(() -> new SchuleNotFoundException(schuleId));

        String password = passwordCipher.decrypt(schule.svwsPasswordEncrypted());

        long startInfo = System.nanoTime();
        SvwsSchuleInfo info = svwsClient.getSchuleInfo(
                schule.svwsUrl(),
                schule.svwsSchema(),
                schule.svwsUsername(),
                password
        );
        long durationInfo = (System.nanoTime() - startInfo) / 1_000_000;
        LOG.infov("svws_getschuleinfo duration_ms={0} schule_id={1} svws_url={2} schema={3}",
            durationInfo, schule.id(), schule.svwsUrl(), schule.svwsSchema());

        return info;
    }

    @Transactional
    public Schule create(SchuleCreateData data) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        String encrypted = passwordCipher.encrypt(data.svwsPassword());

        Schule schule = new Schule(
                id,
                data.name(),
                null,                           // schulnummer
                data.svwsUrl(),
                data.svwsSchema(),
                data.svwsUsername(),
                encrypted,
                SchuleStatus.UNVERIFIED,
                null,                           // lastSyncAt
                null,                           // lastSyncStatus
                null,                           // lastError
                now,
                now,
                // Address
                null, null, null, null, null,   // strasse, hausnummer, hausnummerZusatz, plz, ort
                // Contact
                null, null, null, null,         // telefon, fax, email, homepage
                // Administrative
                null, null, null,               // schulleiter, schulleiterTelefon, schulleiterEmail
                // Region
                null, null,                     // kreis, schulamt
                // Metadata
                null, null                      // schulnummer2, schulstatus
        );

        return repository.save(tenantContext.getTenantId(), schule);
    }

    @Transactional
    public Schule update(UUID id, SchuleUpdateData data) {
        Schule existing = getById(id);
        String encrypted = passwordCipher.encrypt(data.svwsPassword());
        Instant now = Instant.now();

        Schule updated = new Schule(
                existing.id(),
                data.name(),
                existing.schulnummer(),
                data.svwsUrl(),
                data.svwsSchema(),
                data.svwsUsername(),
                encrypted,
                SchuleStatus.UNVERIFIED,
                null,
                null,
                null,
                existing.createdAt(),
                now,
                existing.strasse(),
                existing.hausnummer(),
                existing.hausnummerZusatz(),
                existing.plz(),
                existing.ort(),
                existing.telefon(),
                existing.fax(),
                existing.email(),
                existing.homepage(),
                existing.schulleiter(),
                existing.schulleiterTelefon(),
                existing.schulleiterEmail(),
                existing.kreis(),
                existing.schulamt(),
                existing.schulnummer2(),
                existing.schulstatus()
        );

        return repository.update(tenantContext.getTenantId(), updated);
    }

    @Transactional
    public Schule verify(UUID id) {
        Schule existing = getById(id);
        String password = passwordCipher.decrypt(existing.svwsPasswordEncrypted());

        try {
            long start = System.nanoTime();
            boolean privileged = svwsClient.isPrivileged(
                    existing.svwsUrl(),
                    existing.svwsUsername(),
                    password
            );
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            LOG.infov("svws_isprivileged duration_ms={0} schule_id={1} svws_url={2}",
                    durationMs, existing.id(), existing.svwsUrl());
            SchuleStatus status = privileged ? SchuleStatus.VERIFIED : SchuleStatus.INVALID_CREDENTIALS;
            String error = privileged ? null : "Invalid credentials";
            Schule updated = withStatus(existing, status, null, null, error);
            return repository.update(tenantContext.getTenantId(), updated);
        } catch (SvwsClientException ex) {
            SchuleStatus status = mapStatus(ex.getStatusCode());
            String error = mapError(status);
            Schule updated = withStatus(existing, status, null, null, error);
            return repository.update(tenantContext.getTenantId(), updated);
        } catch (RuntimeException ex) {
            SchuleStatus status = mapStatus(ex);
            String error = mapError(status);
            Schule updated = withStatus(existing, status, null, null, error);
            return repository.update(tenantContext.getTenantId(), updated);
        }
    }

    @Transactional
    public Schule sync(UUID id) {
        Schule existing = getById(id);
        String password = passwordCipher.decrypt(existing.svwsPasswordEncrypted());
        Instant now = Instant.now();

        try {
            long startPriv = System.nanoTime();
            boolean privileged = svwsClient.isPrivileged(
                    existing.svwsUrl(),
                    existing.svwsUsername(),
                    password
            );
            long durationPriv = (System.nanoTime() - startPriv) / 1_000_000;
            LOG.infov("svws_isprivileged duration_ms={0} schule_id={1} svws_url={2}",
                durationPriv, existing.id(), existing.svwsUrl());
            if (!privileged) {
                Schule updated = withStatus(existing, SchuleStatus.INVALID_CREDENTIALS,
                        now, SyncStatus.INVALID_CREDENTIALS, "Invalid credentials");
                return repository.update(tenantContext.getTenantId(), updated);
            }

            long startInfo = System.nanoTime();
            SvwsSchuleInfo info = svwsClient.getSchuleInfo(
                    existing.svwsUrl(),
                    existing.svwsSchema(),
                    existing.svwsUsername(),
                    password
            );
            long durationInfo = (System.nanoTime() - startInfo) / 1_000_000;
            LOG.infov("svws_getschuleinfo duration_ms={0} schule_id={1} svws_url={2} schema={3}",
                durationInfo, existing.id(), existing.svwsUrl(), existing.svwsSchema());

            Schule updated = new Schule(
                    existing.id(),
                    info.name() != null ? info.name() : existing.name(),
                    info.schulnummer(),
                    existing.svwsUrl(),
                    existing.svwsSchema(),
                    existing.svwsUsername(),
                    existing.svwsPasswordEncrypted(),
                    SchuleStatus.VERIFIED,
                    now,
                    SyncStatus.SUCCESS,
                    null,
                    existing.createdAt(),
                    Instant.now(),
                    // Address information
                    info.strasse(),
                    info.hausnummer(),
                    info.hausnummerZusatz(),
                    info.plz(),
                    info.ort(),
                    // Contact information
                    info.telefon(),
                    info.fax(),
                    info.email(),
                    info.homepage(),
                    // Administrative information
                    info.schulleiter(),
                    info.schulleiterTelefon(),
                    info.schulleiterEmail(),
                    // Region information
                    info.kreis(),
                    info.schulamt(),
                    // Additional metadata
                    info.schulnummer2(),
                    info.schulstatus()
            );

            return repository.update(tenantContext.getTenantId(), updated);
        } catch (SvwsClientException ex) {
            SchuleStatus status = mapStatus(ex.getStatusCode());
            SyncStatus syncStatus = mapSyncStatus(ex.getStatusCode());
            String error = mapError(status);
            Schule updated = withStatus(existing, status, now, syncStatus, error);
            return repository.update(tenantContext.getTenantId(), updated);
        } catch (RuntimeException ex) {
            SchuleStatus status = mapStatus(ex);
            SyncStatus syncStatus = mapSyncStatus(ex);
            String error = mapError(status);
            Schule updated = withStatus(existing, status, now, syncStatus, error);
            return repository.update(tenantContext.getTenantId(), updated);
        }
    }

    private Schule getById(UUID id) {
        return repository.findById(tenantContext.getTenantId(), id)
                .orElseThrow(() -> new SchuleNotFoundException(id));
    }

    private Schule withStatus(Schule existing,
                              SchuleStatus status,
                              Instant lastSyncAt,
                              SyncStatus lastSyncStatus,
                              String lastError) {
        return new Schule(
                existing.id(),
                existing.name(),
                existing.schulnummer(),
                existing.svwsUrl(),
                existing.svwsSchema(),
                existing.svwsUsername(),
                existing.svwsPasswordEncrypted(),
                status,
                lastSyncAt,
                lastSyncStatus,
                lastError,
                existing.createdAt(),
                Instant.now(),
                existing.strasse(),
                existing.hausnummer(),
                existing.hausnummerZusatz(),
                existing.plz(),
                existing.ort(),
                existing.telefon(),
                existing.fax(),
                existing.email(),
                existing.homepage(),
                existing.schulleiter(),
                existing.schulleiterTelefon(),
                existing.schulleiterEmail(),
                existing.kreis(),
                existing.schulamt(),
                existing.schulnummer2(),
                existing.schulstatus()
        );
    }

    private SchuleStatus mapStatus(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return SchuleStatus.INVALID_CREDENTIALS;
        }
        if (statusCode == 408 || statusCode == 504) {
            return SchuleStatus.UNREACHABLE;
        }
        return SchuleStatus.ERROR;
    }

    private SchuleStatus mapStatus(RuntimeException ex) {
        if (isNetworkIssue(ex)) {
            return SchuleStatus.UNREACHABLE;
        }
        return SchuleStatus.ERROR;
    }

    private SyncStatus mapSyncStatus(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return SyncStatus.INVALID_CREDENTIALS;
        }
        if (statusCode == 408 || statusCode == 504) {
            return SyncStatus.UNREACHABLE;
        }
        return SyncStatus.ERROR;
    }

    private SyncStatus mapSyncStatus(RuntimeException ex) {
        if (isNetworkIssue(ex)) {
            return SyncStatus.UNREACHABLE;
        }
        return SyncStatus.ERROR;
    }

    private boolean isNetworkIssue(RuntimeException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof ConnectException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String mapError(SchuleStatus status) {
        return switch (status) {
            case INVALID_CREDENTIALS -> "Invalid credentials";
            case UNREACHABLE -> "SVWS unreachable";
            case ERROR -> "SVWS error";
            default -> null;
        };
    }
}
