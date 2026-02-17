package de.schultraeger.application;

import de.schultraeger.application.dto.SchuleStammdaten;
import de.schultraeger.application.dto.SchuleStammdatenResult;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsClientException;
import de.schultraeger.application.port.out.SchuleRepository;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SvwsServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

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
    private final PasswordCipher passwordCipher;
    private final SvwsServerRepository serverRepository;

    public SchuleService(SchuleRepository repository,
                         SvwsClient svwsClient,
                         PasswordCipher passwordCipher,
                         SvwsServerRepository serverRepository) {
        this.repository = repository;
        this.svwsClient = svwsClient;
        this.passwordCipher = passwordCipher;
        this.serverRepository = serverRepository;
    }

    public List<Schule> list() {
        return repository.findAllSchools();
    }

    @Transactional
    public Schule create(de.schultraeger.api.dto.SchuleRequest request) {
        // Encrypt the password
        String encryptedPassword = null;
        if (request.svwsPassword() != null && !request.svwsPassword().isEmpty()) {
            encryptedPassword = passwordCipher.encrypt(request.svwsPassword());
        }
        
        // Create new school
        Instant now = Instant.now();
        Schule schule = new Schule(
            UUID.randomUUID(),
            UUID.fromString(request.svwsServerId()),
            request.svwsSchema(),
            request.svwsUsername(),
            encryptedPassword,
            now,
            now
        );
        
        // Save to repository
        repository.saveSchool(schule);
        
        return schule;
    }

    public Schule getById(UUID id) {
        return repository.findSchoolById(id)
                .orElseThrow(() -> new SchuleNotFoundException(id));
    }

    @ActivateRequestContext
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int importSchoolsFromSvwsServer(SvwsServer server) {
        LOG.infov("import_schools_start server_id={0} base_url={1}", server.id(), server.baseUrl());
        String password = passwordCipher.decrypt(server.passwordEncrypted());
        
        List<SvwsSchuleInfo> schoolInfos;
        try {
            schoolInfos = svwsClient.listSchools(server.baseUrl(), server.username(), password);
            if (schoolInfos == null) {
                LOG.errorv("import_schools_failed server_id={0} reason=null_list", server.id());
                return 0;
            }
        } catch (Exception e) {
            LOG.errorv(e, "import_schools_failed server_id={0}", server.id());
            return 0;
        }

        int count = 0;
        for (SvwsSchuleInfo info : schoolInfos) {
            if (saveSchoolIfNew(server, info)) {
                count++;
            }
        }
        LOG.infov("import_schools_done server_id={0} total={1} newly_imported={2}", server.id(), schoolInfos.size(), count);
        return count;
    }

    @Transactional
    public boolean saveSchoolIfNew(SvwsServer server, SvwsSchuleInfo info) {
        if (repository.findByServerIdAndSchema(server.id(), info.schema()).isPresent()) {
            return false;
        }

        Instant now = Instant.now();
        Schule schule = new Schule(
                UUID.randomUUID(),
                server.id(),
                info.schema(),
                null,  // svwsUsername - will be set later by user
                null,  // svwsUserPasswordEncrypted - will be set later by user
                now,
                now
        );

        repository.saveSchool(schule);
        return true;
    }

    @Transactional
    public Schule update(UUID id, de.schultraeger.api.dto.SchuleRequest request) {
        // Get the existing school
        Schule existing = getById(id);
        
        // Encrypt the password if provided
        String encryptedPassword = null;
        if (request.svwsPassword() != null && !request.svwsPassword().isEmpty()) {
            encryptedPassword = passwordCipher.encrypt(request.svwsPassword());
        } else if (existing.svwsUserPasswordEncrypted() != null) {
            // Keep existing password if no new one provided
            encryptedPassword = existing.svwsUserPasswordEncrypted();
        }
        
        // Create updated school
        Schule updated = new Schule(
            id,
            existing.svwsServerId(),
            request.svwsSchema(),
            request.svwsUsername(),
            encryptedPassword,
            existing.createdAt(),
            Instant.now()
        );
        
        // Update in repository
        repository.updateSchool(updated);
        
        return updated;
    }

    @Transactional
    public void deleteByServerId(UUID svwsServerId) {
        repository.deleteByServerId(svwsServerId);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteSchool(id);
    }

    public List<SchuleStammdatenResult> listStammdaten() {
        return repository.findAllSchools().stream()
                .map(this::buildStammdatenResult)
                .toList();
    }

    private SchuleStammdatenResult buildStammdatenResult(Schule schule) {
        SvwsServer server = serverRepository.getById(schule.svwsServerId()).orElse(null);
        String serverName = server != null ? server.name() : "Unbekannter Server";

        if (server == null) {
            String message = "SVWS-Server nicht gefunden für Schema " + schule.svwsSchema();
            LOG.warn(message);
            return new SchuleStammdatenResult(schule.id(), schule.svwsSchema(), serverName, null, message);
        }

        String[] credentials;
        try {
            credentials = resolveCredentials(schule, server);
        } catch (Exception ex) {
            LOG.warnf(ex, "Fehler beim Entschlüsseln der Anmeldedaten für Schema %s", schule.svwsSchema());
            String message = ex.getMessage() != null ? ex.getMessage() : "Fehler beim Lesen der Anmeldedaten";
            return new SchuleStammdatenResult(schule.id(), schule.svwsSchema(), serverName, null, message);
        }

        if (credentials == null) {
            return new SchuleStammdatenResult(
                    schule.id(),
                    schule.svwsSchema(),
                    serverName,
                    null,
                    "Anmeldedaten fehlen"
            );
        }

        try {
            SchuleStammdaten stammdaten = svwsClient.getSchuleStammdaten(
                server.baseUrl(),
                schule.svwsSchema(),
                credentials[0],
                credentials[1]
            );
            return new SchuleStammdatenResult(
                schule.id(),
                schule.svwsSchema(),
                serverName,
                stammdaten,
                null
            );
        } catch (SvwsClientException ex) {
            LOG.warnf(ex, "Stammdaten nicht erreichbar für Schema %s (Status %d)", schule.svwsSchema(), ex.getStatusCode());
            SvwsSchuleInfo fallbackInfo = tryFallbackInfo(server, schule, credentials);
            if (fallbackInfo != null) {
                return new SchuleStammdatenResult(
                    schule.id(),
                    schule.svwsSchema(),
                    serverName,
                    new SchuleStammdaten(fallbackInfo.schulnummer(), fallbackInfo.name(), null),
                    "Eingeschränkte Ansicht: " + ex.getMessage()
                );
            }
            String message = ex.getMessage() != null ? ex.getMessage() : "Fehler beim Laden der Stammdaten";
            return new SchuleStammdatenResult(
                schule.id(),
                schule.svwsSchema(),
                serverName,
                null,
                message
            );
        } catch (Exception ex) {
            LOG.warnf(ex, "Fehler beim Laden der Stammdaten für Schema %s", schule.svwsSchema());
            String message = ex.getMessage() != null ? ex.getMessage() : "Fehler beim Laden der Stammdaten";
            return new SchuleStammdatenResult(
                schule.id(),
                schule.svwsSchema(),
                serverName,
                null,
                message
            );
        }
    }

    private String[] resolveCredentials(Schule schule, SvwsServer server) {
        String username = safeTrim(schule.svwsUsername());
        if (!username.isEmpty()) {
            return new String[]{
                    username,
                    decryptOrEmpty(schule.svwsUserPasswordEncrypted())
            };
        }

        String serverUsername = safeTrim(server.username());
        if (!serverUsername.isEmpty()) {
            return new String[]{
                    serverUsername,
                    decryptOrEmpty(server.passwordEncrypted())
            };
        }

        return null;
    }

    private SvwsSchuleInfo tryFallbackInfo(SvwsServer server, Schule schule, String[] credentials) {
        try {
            return svwsClient.getSchuleInfo(
                    server.baseUrl(),
                    schule.svwsSchema(),
                    credentials[0],
                    credentials[1]
            );
        } catch (Exception fallback) {
            LOG.debugf(fallback, "Fallback-SchuleInfo für Schema %s fehlgeschlagen", schule.svwsSchema());
            return null;
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String decryptOrEmpty(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }
        return passwordCipher.decrypt(encrypted);
    }
}
