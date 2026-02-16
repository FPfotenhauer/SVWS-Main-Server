package de.schultraeger.application;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SchuleRepository;
import de.schultraeger.application.port.out.SvwsClient;
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

    public SchuleService(SchuleRepository repository,
                         SvwsClient svwsClient,
                         PasswordCipher passwordCipher) {
        this.repository = repository;
        this.svwsClient = svwsClient;
        this.passwordCipher = passwordCipher;
    }

    public List<Schule> list() {
        return repository.findAllSchools();
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
                now,
                now
        );

        repository.saveSchool(schule);
        return true;
    }

    @Transactional
    public void deleteByServerId(UUID svwsServerId) {
        repository.deleteByServerId(svwsServerId);
    }
}
