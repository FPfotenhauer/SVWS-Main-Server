package de.schultraeger.application;

import de.schultraeger.application.dto.SchuleStammdaten;
import de.schultraeger.application.dto.SchuleStammdatenResult;
import de.schultraeger.application.dto.SchuleStatistikenGesamt;
import de.schultraeger.application.dto.SchuleStatistikenRaw;
import de.schultraeger.application.dto.OrtKatalogEintrag;
import de.schultraeger.application.dto.SchuelerAuswahl;
import de.schultraeger.application.dto.SchuelerStammdaten;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

/**
 * Application service for school management and SVWS sync.
 */
@ApplicationScoped
public class SchuleService {
    private static final Logger LOG = Logger.getLogger(SchuleService.class);
    private static final String UNKNOWN_CONFESSION_CODE = "UNBEKANNT";
    private static final String NO_SPECIAL_NEED_CODE = "**";
    private static final String UNKNOWN_SPECIAL_NEED_CODE = "UNBEKANNT";
    private static final Map<String, String> RELIGION_LABELS = Map.ofEntries(
            Map.entry("AR", "Alevitisch"),
            Map.entry("ER", "Evangelisch"),
            Map.entry("HR", "Juedisch"),
            Map.entry("IR", "Islamisch"),
            Map.entry("KR", "Katholisch"),
            Map.entry("ME", "Mennonitische BG NRW"),
            Map.entry("OH", "Ohne Bekenntnis"),
            Map.entry("OR", "Griechisch-orthodox"),
            Map.entry("SO", "Syrisch-orthodox"),
            Map.entry("XO", "Sonstige orthodoxe"),
            Map.entry("XR", "Andere Religionen"),
            Map.entry(UNKNOWN_CONFESSION_CODE, "Unbekannt")
    );
            private static final Map<String, String> SPECIAL_NEED_LABELS = Map.ofEntries(
                Map.entry(NO_SPECIAL_NEED_CODE, "Kein Foerderschwerpunkt"),
                Map.entry("XX", "Kein Foerderschwerpunkt"),
                Map.entry("BL", "Sehen (Blinde)"),
                Map.entry("EZ", "Emotionale und soziale Entwicklung"),
                Map.entry("GB", "Geistige Entwicklung"),
                Map.entry("GH", "Hoeren und Kommunikation (Gehoerlose)"),
                Map.entry("KB", "Koerperliche und motorische Entwicklung"),
                Map.entry("KR", "Klinikschule"),
                Map.entry("LB", "Lernen"),
                Map.entry("PE", "Praeventive Foerderung im Bereich Emotionale und soziale Entwicklung"),
                Map.entry("PF", "Praeventive Foerderung"),
                Map.entry("PL", "Praeventive Foerderung im Bereich Lernen"),
                Map.entry("PS", "Praeventive Foerderung im Bereich Sprache"),
                Map.entry("SB", "Sprache"),
                Map.entry("SG", "Hoeren und Kommunikation (Schwerhoerige)"),
                Map.entry("SH", "Sehen (Sehbehinderte)"),
                Map.entry(UNKNOWN_SPECIAL_NEED_CODE, "Unbekannt")
            );
    private static final Map<String, String> NATIONALITY_LABELS = buildNationalityLabels();

    private static Map<String, String> buildNationalityLabels() {
        Map<String, String> m = new java.util.HashMap<>();
        m.put("XXA", "staatenlos");
        m.put("XXB", "ungeklaert");
        m.put("XXC", "ohne Angabe");
        m.put("AFG", "Afghanistan");
        m.put("EGY", "Aegypten");
        m.put("ALB", "Albanien");
        m.put("DZA", "Algerien");
        m.put("AND", "Andorra");
        m.put("AGO", "Angola");
        m.put("ATG", "Antigua und Barbuda");
        m.put("GNQ", "Aequatorialguinea");
        m.put("ARG", "Argentinien");
        m.put("ARM", "Armenien");
        m.put("AZE", "Aserbaidschan");
        m.put("ETH", "Aethiopien");
        m.put("AUS", "Australien");
        m.put("BHS", "Bahamas");
        m.put("BHR", "Bahrain");
        m.put("BGD", "Bangladesch");
        m.put("BRB", "Barbados");
        m.put("BLR", "Belarus");
        m.put("BEL", "Belgien");
        m.put("BLZ", "Belize");
        m.put("BEN", "Benin");
        m.put("BTN", "Bhutan");
        m.put("BOL", "Bolivien");
        m.put("BIH", "Bosnien und Herzegowina");
        m.put("BWA", "Botsuana");
        m.put("BRA", "Brasilien");
        m.put("BRN", "Brunei Darussalam");
        m.put("BGR", "Bulgarien");
        m.put("BFA", "Burkina Faso");
        m.put("BDI", "Burundi");
        m.put("CPV", "Cabo Verde");
        m.put("CHL", "Chile");
        m.put("CHN", "China");
        m.put("COK", "Cookinseln");
        m.put("CRI", "Costa Rica");
        m.put("CIV", "Cote d'Ivoire");
        m.put("DNK", "Daenemark");
        m.put("DEU", "Deutschland");
        m.put("DMA", "Dominica");
        m.put("DOM", "Dominikanische Republik");
        m.put("DJI", "Dschibuti");
        m.put("ECU", "Ecuador");
        m.put("SLV", "El Salvador");
        m.put("ERI", "Eritrea");
        m.put("EST", "Estland");
        m.put("SWZ", "Eswatini");
        m.put("FJI", "Fidschi");
        m.put("FIN", "Finnland");
        m.put("FRA", "Frankreich");
        m.put("GAB", "Gabun");
        m.put("GMB", "Gambia");
        m.put("GEO", "Georgien");
        m.put("GHA", "Ghana");
        m.put("GRD", "Grenada");
        m.put("GRC", "Griechenland");
        m.put("GTM", "Guatemala");
        m.put("GIN", "Guinea");
        m.put("GNB", "Guinea-Bissau");
        m.put("GUY", "Guyana");
        m.put("HTI", "Haiti");
        m.put("HND", "Honduras");
        m.put("HKG", "Hongkong");
        m.put("IND", "Indien");
        m.put("IDN", "Indonesien");
        m.put("IRQ", "Irak");
        m.put("IRN", "Iran");
        m.put("IRL", "Irland");
        m.put("ISL", "Island");
        m.put("ISR", "Israel");
        m.put("ITA", "Italien");
        m.put("JAM", "Jamaika");
        m.put("JPN", "Japan");
        m.put("YEM", "Jemen");
        m.put("JOR", "Jordanien");
        m.put("YUG", "Jugoslawien");
        m.put("KHM", "Kambodscha");
        m.put("CMR", "Kamerun");
        m.put("CAN", "Kanada");
        m.put("KAZ", "Kasachstan");
        m.put("QAT", "Katar");
        m.put("KEN", "Kenia");
        m.put("KGZ", "Kirgisistan");
        m.put("KIR", "Kiribati");
        m.put("COL", "Kolumbien");
        m.put("COM", "Komoren");
        m.put("COG", "Kongo");
        m.put("COD", "Dem. Republik Kongo");
        m.put("PRK", "Dem. Volksrepublik Korea");
        m.put("KOR", "Republik Korea");
        m.put("XXK", "Kosovo");
        m.put("HRV", "Kroatien");
        m.put("CUB", "Kuba");
        m.put("KWT", "Kuwait");
        m.put("LAO", "Laos");
        m.put("LSO", "Lesotho");
        m.put("LVA", "Lettland");
        m.put("LBN", "Libanon");
        m.put("LBR", "Liberia");
        m.put("LBY", "Libyen");
        m.put("LIE", "Liechtenstein");
        m.put("LTU", "Litauen");
        m.put("LUX", "Luxemburg");
        m.put("MAC", "Macau");
        m.put("MDG", "Madagaskar");
        m.put("MWI", "Malawi");
        m.put("MYS", "Malaysia");
        m.put("MDV", "Malediven");
        m.put("MLI", "Mali");
        m.put("MLT", "Malta");
        m.put("MAR", "Marokko");
        m.put("MHL", "Marshallinseln");
        m.put("MRT", "Mauretanien");
        m.put("MUS", "Mauritius");
        m.put("MEX", "Mexiko");
        m.put("FSM", "Mikronesien");
        m.put("MDA", "Moldau");
        m.put("MCO", "Monaco");
        m.put("MNG", "Mongolei");
        m.put("MNE", "Montenegro");
        m.put("MOZ", "Mosambik");
        m.put("MMR", "Myanmar");
        m.put("NAM", "Namibia");
        m.put("NRU", "Nauru");
        m.put("NPL", "Nepal");
        m.put("NZL", "Neuseeland");
        m.put("NIC", "Nicaragua");
        m.put("NLD", "Niederlande");
        m.put("NER", "Niger");
        m.put("NGA", "Nigeria");
        m.put("NIU", "Niue");
        m.put("MKD", "Nordmazedonien");
        m.put("NOR", "Norwegen");
        m.put("OMN", "Oman");
        m.put("AUT", "Oesterreich");
        m.put("PAK", "Pakistan");
        m.put("PLW", "Palau");
        m.put("PAN", "Panama");
        m.put("PNG", "Papua-Neuguinea");
        m.put("PRY", "Paraguay");
        m.put("PER", "Peru");
        m.put("PHL", "Philippinen");
        m.put("POL", "Polen");
        m.put("PRT", "Portugal");
        m.put("PSE", "Palaestinensische Gebiete");
        m.put("RWA", "Ruanda");
        m.put("ROU", "Rumaenien");
        m.put("RUS", "Russische Foederation");
        m.put("SLB", "Salomonen");
        m.put("ZMB", "Sambia");
        m.put("WSM", "Samoa");
        m.put("SMR", "San Marino");
        m.put("STP", "Sao Tome und Principe");
        m.put("SAU", "Saudi-Arabien");
        m.put("SWE", "Schweden");
        m.put("CHE", "Schweiz");
        m.put("SEN", "Senegal");
        m.put("SRB", "Serbien");
        m.put("SCG", "Serbien und Montenegro");
        m.put("SYC", "Seychellen");
        m.put("SLE", "Sierra Leone");
        m.put("ZWE", "Simbabwe");
        m.put("SGP", "Singapur");
        m.put("SVK", "Slowakei");
        m.put("SVN", "Slowenien");
        m.put("SOM", "Somalia");
        m.put("SUN", "Sowjetunion");
        m.put("ESP", "Spanien");
        m.put("LKA", "Sri Lanka");
        m.put("KNA", "St. Kitts und Nevis");
        m.put("LCA", "St. Lucia");
        m.put("VCT", "St. Vincent und die Grenadinen");
        m.put("ZAF", "Suedafrika");
        m.put("SDN", "Sudan");
        m.put("SSD", "Suedsudan");
        m.put("SUR", "Suriname");
        m.put("SYR", "Syrien");
        m.put("TJK", "Tadschikistan");
        m.put("TWN", "Taiwan");
        m.put("TZA", "Tansania");
        m.put("THA", "Thailand");
        m.put("TLS", "Timor-Leste");
        m.put("TGO", "Togo");
        m.put("TON", "Tonga");
        m.put("TTO", "Trinidad und Tobago");
        m.put("TCD", "Tschad");
        m.put("CZE", "Tschechien");
        m.put("CSK", "Tschechoslowakei");
        m.put("TUN", "Tunesien");
        m.put("TUR", "Tuerkei");
        m.put("TKM", "Turkmenistan");
        m.put("TUV", "Tuvalu");
        m.put("UGA", "Uganda");
        m.put("UKR", "Ukraine");
        m.put("HUN", "Ungarn");
        m.put("URY", "Uruguay");
        m.put("UZB", "Usbekistan");
        m.put("VUT", "Vanuatu");
        m.put("VAT", "Vatikanstadt");
        m.put("VEN", "Venezuela");
        m.put("ARE", "Vereinigte Arabische Emirate");
        m.put("USA", "Vereinigte Staaten");
        m.put("GBR", "Vereinigtes Koenigreich");
        m.put("BMU", "Brit. Ueberseegebiet");
        m.put("VNM", "Vietnam");
        m.put("CAF", "Zentralafrikanische Republik");
        m.put("CYP", "Zypern");
        m.put("UNBEKANNT", "Unbekannt");
        return java.util.Collections.unmodifiableMap(m);
    }

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
            if (info == null) {
                continue;
            }
            if (saveSchoolIfNew(server, info)) {
                count++;
            }
        }
        LOG.infov("import_schools_done server_id={0} total={1} newly_imported={2}", server.id(), schoolInfos.size(), count);
        return count;
    }

    @Transactional
    public boolean saveSchoolIfNew(SvwsServer server, SvwsSchuleInfo info) {
        String schema = normalizeSchema(info.schema());
        if (schema == null) {
            LOG.warnv("Skipping school import with missing/blank schema for server_id={0}", server.id());
            return false;
        }

        if (repository.findByServerIdAndSchema(server.id(), schema).isPresent()) {
            return false;
        }

        Instant now = Instant.now();
        Schule schule = new Schule(
                UUID.randomUUID(),
                server.id(),
            schema,
                null,  // svwsUsername - will be set later by user
                null,  // svwsUserPasswordEncrypted - will be set later by user
                now,
                now
        );

        repository.saveSchool(schule);
        return true;
    }

    private String normalizeSchema(String schema) {
        if (schema == null) {
            return null;
        }
        String normalized = schema.trim();
        return normalized.isEmpty() ? null : normalized;
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
        Schule schule = repository.findSchoolById(id)
                .orElseThrow(() -> new SchuleNotFoundException(id));
        
        SvwsServer server = serverRepository.getById(schule.svwsServerId())
                .orElseThrow(() -> new IllegalStateException("SVWS-Server nicht gefunden für Schule " + id));
        
        String password = passwordCipher.decrypt(server.passwordEncrypted());
        
        try {
            // First destroy the schema on the SVWS server
            LOG.infov("Destroying schema {0} on SVWS server {1}", schule.svwsSchema(), server.name());
            svwsClient.destroySchema(server.baseUrl(), schule.svwsSchema(), server.username(), password);
            LOG.infov("Successfully destroyed schema {0}", schule.svwsSchema());
        } catch (Exception e) {
            LOG.errorv(e, "Failed to destroy schema {0} on SVWS server - will still delete from database", schule.svwsSchema());
            // Continue with database deletion even if SVWS deletion fails
            // This allows cleanup of orphaned database entries
        }
        
        // Then delete from our database
        repository.deleteSchool(id);
        LOG.infov("Deleted school {0} from database", id);
    }

    public byte[] exportBackup(UUID id) {
        Schule schule = repository.findSchoolById(id)
                .orElseThrow(() -> new SchuleNotFoundException(id));
        
        SvwsServer server = serverRepository.getById(schule.svwsServerId())
                .orElseThrow(() -> new IllegalStateException("SVWS-Server nicht gefunden für Schule " + id));
        
        String password = passwordCipher.decrypt(server.passwordEncrypted());
        
        LOG.infov("Exporting SQLite backup for schema {0} from SVWS server {1}", schule.svwsSchema(), server.name());
        return svwsClient.exportSqliteBackup(server.baseUrl(), schule.svwsSchema(), server.username(), password);
    }

    public List<SchuleStammdatenResult> listStammdaten() {
        return repository.findAllSchools().stream()
                .map(this::buildStammdatenResult)
                .toList();
    }

    public SchuleStammdatenResult getSchuleStammdatenById(UUID schuleId) {
        Schule schule = getById(schuleId);
        return buildStammdatenResult(schule);
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
                    new SchuleStammdaten(fallbackInfo.schulnummer(), fallbackInfo.name(), null, null),
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

    public SchuleStatistikenGesamt getStatistiken(UUID schuleId) {
        Schule schule = getById(schuleId);
        SvwsServer server = serverRepository.getById(schule.svwsServerId())
                .orElseThrow(() -> new IllegalStateException("SVWS-Server nicht gefunden für Schule " + schuleId));

        String[] credentials;
        try {
            credentials = resolveCredentials(schule, server);
        } catch (Exception ex) {
            LOG.warnf(ex, "Fehler beim Entschlüsseln der Anmeldedaten für Schema %s", schule.svwsSchema());
            throw new IllegalStateException("Fehler beim Lesen der Anmeldedaten", ex);
        }

        if (credentials == null) {
            throw new IllegalStateException("Anmeldedaten fehlen für Schule " + schuleId);
        }

        try {
            SchuleStatistikenRaw rawData = svwsClient.getSchuleStatistiken(
                server.baseUrl(),
                schule.svwsSchema(),
                credentials[0],
                credentials[1]
            );
            return computeAggregates(rawData);
        } catch (SvwsClientException ex) {
            // Handle 404: Statistics endpoint not available for this school - return empty statistics
            if (ex.getStatusCode() == 404) {
                LOG.warnf("Statistics endpoint not available for schema %s", schule.svwsSchema());
                return createEmptyStatistiken();
            }
            // Handle 200 with bad JSON: School database exists but has malformed statistics data
            if (ex.getStatusCode() == 200) {
                LOG.warnf("Statistics JSON parsing failed for schema %s", schule.svwsSchema());
                return createEmptyStatistiken();
            }
            LOG.warnf(ex, "Statistiken nicht erreichbar für Schema %s (Status %d)", schule.svwsSchema(), ex.getStatusCode());
            throw new IllegalStateException("Statistiken nicht erreichbar: " + ex.getMessage(), ex);
        }
    }

    public List<SchuelerAuswahl> getSchuelerAuswahlliste(UUID schuleId) {
        Schule schule = getById(schuleId);
        SvwsServer server = serverRepository.getById(schule.svwsServerId())
                .orElseThrow(() -> new IllegalStateException("SVWS-Server nicht gefunden für Schule " + schuleId));

        String[] credentials;
        try {
            credentials = resolveCredentials(schule, server);
        } catch (Exception ex) {
            LOG.warnf(ex, "Fehler beim Entschlüsseln der Anmeldedaten für Schema %s", schule.svwsSchema());
            throw new IllegalStateException("Fehler beim Lesen der Anmeldedaten", ex);
        }

        if (credentials == null) {
            throw new IllegalStateException("Anmeldedaten fehlen für Schule " + schuleId);
        }

        SchuleStammdaten stammdaten = svwsClient.getSchuleStammdaten(
                server.baseUrl(),
                schule.svwsSchema(),
                credentials[0],
                credentials[1]
        );

        Integer abschnitt = stammdaten != null ? stammdaten.getIdSchuljahresabschnitt() : null;
        if (abschnitt == null) {
            throw new IllegalStateException("idSchuljahresabschnitt konnte nicht ermittelt werden");
        }

        return svwsClient.getSchuelerAuswahlliste(
                server.baseUrl(),
                schule.svwsSchema(),
                credentials[0],
                credentials[1],
                abschnitt,
                List.of(0, 1, 2, 3)
        );
    }

    public SchuelerStammdaten getSchuelerStammdaten(UUID schuleId, Long schuelerId) {
        Schule schule = getById(schuleId);
        SvwsServer server = serverRepository.getById(schule.svwsServerId())
                .orElseThrow(() -> new IllegalStateException("SVWS-Server nicht gefunden für Schule " + schuleId));

        String[] credentials;
        try {
            credentials = resolveCredentials(schule, server);
        } catch (Exception ex) {
            LOG.warnf(ex, "Fehler beim Entschlüsseln der Anmeldedaten für Schema %s", schule.svwsSchema());
            throw new IllegalStateException("Fehler beim Lesen der Anmeldedaten", ex);
        }

        if (credentials == null) {
            throw new IllegalStateException("Anmeldedaten fehlen für Schule " + schuleId);
        }

        SchuelerStammdaten schueler = svwsClient.getSchuelerStammdatenByIds(
                        server.baseUrl(),
                        schule.svwsSchema(),
                        credentials[0],
                        credentials[1],
                        List.of(schuelerId)
                )
                .stream()
                .filter(s -> s.getId() != null && s.getId().equals(schuelerId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Schüler nicht gefunden: " + schuelerId));

        String plz = schueler.getPlz();
        String ort = schueler.getOrt();

        if (schueler.getWohnortID() != null) {
            List<OrtKatalogEintrag> orte = svwsClient.getOrte(
                    server.baseUrl(),
                    schule.svwsSchema(),
                    credentials[0],
                    credentials[1]
            );
            OrtKatalogEintrag ortEintrag = orte.stream()
                    .filter(o -> o.id() != null && o.id().equals(schueler.getWohnortID()))
                    .findFirst()
                    .orElse(null);
            if (ortEintrag != null) {
                if (ortEintrag.plz() != null && !ortEintrag.plz().isBlank()) {
                    plz = ortEintrag.plz();
                }
                if (ortEintrag.ortsname() != null && !ortEintrag.ortsname().isBlank()) {
                    ort = ortEintrag.ortsname();
                }
            }
        }

        // Update the object with enriched data
        schueler.setPlz(plz);
        schueler.setOrt(ort);
        
        return schueler;
    }

    private SchuleStatistikenGesamt computeAggregates(SchuleStatistikenRaw rawData) {
        List<SchuleStatistikenRaw.Schueler> students = rawData.schueler();
        if (students == null) {
            students = List.of();
        }

        // Basic counts
        int totalStudents = students.size();
        int maleStudents = (int) students.stream().filter(s -> s.geschlecht() != null && s.geschlecht() == 4).count();
        int femaleStudents = (int) students.stream().filter(s -> s.geschlecht() != null && s.geschlecht() == 3).count();
        int studentsWithSpecialNeeds = (int) students.stream()
                .filter(this::hasSpecialNeed)
                .count();
        int studentsWithMigrationBackground = (int) students.stream()
                .filter(s -> s.hatMigrationshintergrund() != null && s.hatMigrationshintergrund())
                .count();

        // Abitur counts
        int abiStudentsEligible = (int) students.stream()
                .filter(s -> s.abitur() != null)
                .count();
        int abiStudentsPassed = (int) students.stream()
                .filter(s -> s.abitur() != null && s.abitur().hatBestanden() != null && s.abitur().hatBestanden())
                .count();

        // Grade distribution and confessions per grade
        Map<Integer, Integer> gradeCountsById = new HashMap<>();
        Map<Integer, String> gradeNamesById = new HashMap<>();
        Map<Integer, String> confessionCodesById = new HashMap<>();
        Map<Integer, String> specialNeedCodesById = new HashMap<>();
        Map<Integer, Map<String, Integer>> confessionCountsByGrade = new HashMap<>();
        Map<Integer, Set<Long>> classStudentIds = new HashMap<>();
        Map<Integer, Set<Long>> maleStudentIdsByClass = new HashMap<>();
        Map<Integer, Set<Long>> femaleStudentIdsByClass = new HashMap<>();
        Map<Integer, Map<Integer, Set<Long>>> gradeStudentIdsByClass = new HashMap<>();
        Map<Integer, Map<String, Set<Long>>> specialNeedStudentIdsByClass = new HashMap<>();
        Map<Integer, Map<String, Set<Long>>> nationalityStudentIdsByClass = new HashMap<>();
        Map<Integer, Map<String, Set<Long>>> nationalityMaleStudentIdsByClass = new HashMap<>();
        Map<Integer, Map<String, Set<Long>>> nationalityFemaleStudentIdsByClass = new HashMap<>();

        if (rawData.jahrgaenge() != null) {
            for (SchuleStatistikenRaw.Jahrgang jg : rawData.jahrgaenge()) {
                gradeNamesById.put(jg.id(), jg.kuerzel());
            }
        }

        if (rawData.religionen() != null) {
            for (SchuleStatistikenRaw.Religion religion : rawData.religionen()) {
                if (religion.id() != null && religion.kuerzel() != null && !religion.kuerzel().isBlank()) {
                    confessionCodesById.put(religion.id(), religion.kuerzel().trim().toUpperCase());
                }
            }
        }

        if (rawData.foerderschwerpunkte() != null) {
            for (SchuleStatistikenRaw.Foerderschwerpunkt foerderschwerpunkt : rawData.foerderschwerpunkte()) {
                if (foerderschwerpunkt.id() != null
                        && foerderschwerpunkt.kuerzelStatistik() != null
                        && !foerderschwerpunkt.kuerzelStatistik().isBlank()) {
                    specialNeedCodesById.put(
                            foerderschwerpunkt.id(),
                            foerderschwerpunkt.kuerzelStatistik().trim().toUpperCase()
                    );
                }
            }
        }

        for (SchuleStatistikenRaw.Schueler student : students) {
            String confessionCode = resolveConfessionCode(student.idReligion(), confessionCodesById);
            if (student.lernabschnitte() != null) {
                for (SchuleStatistikenRaw.Lernabschnitt la : student.lernabschnitte()) {
                    gradeCountsById.merge(la.idJahrgang(), 1, Integer::sum);
                    confessionCountsByGrade
                            .computeIfAbsent(la.idJahrgang(), ignored -> new HashMap<>())
                            .merge(confessionCode, 1, Integer::sum);

                    if (student.id() != null && la.idKlasse() != null) {
                        classStudentIds
                                .computeIfAbsent(la.idKlasse(), ignored -> new HashSet<>())
                                .add(student.id());

                        if (student.geschlecht() != null && student.geschlecht() == 4) {
                            maleStudentIdsByClass
                                    .computeIfAbsent(la.idKlasse(), ignored -> new HashSet<>())
                                    .add(student.id());
                        }

                        if (student.geschlecht() != null && student.geschlecht() == 3) {
                            femaleStudentIdsByClass
                                    .computeIfAbsent(la.idKlasse(), ignored -> new HashSet<>())
                                    .add(student.id());
                        }

                        gradeStudentIdsByClass
                                .computeIfAbsent(la.idKlasse(), ignored -> new HashMap<>())
                                .computeIfAbsent(la.idJahrgang(), ignored -> new HashSet<>())
                                .add(student.id());

                        for (String specialNeedCode : resolveSpecialNeedCodes(student, la, specialNeedCodesById)) {
                            specialNeedStudentIdsByClass
                                .computeIfAbsent(la.idKlasse(), ignored -> new HashMap<>())
                                .computeIfAbsent(specialNeedCode, ignored -> new HashSet<>())
                                .add(student.id());
                        }

                        String nationalityKuerzel = resolveNationalityKuerzel(student.staatsangehoerigkeitID());
                        nationalityStudentIdsByClass
                            .computeIfAbsent(la.idKlasse(), ignored -> new HashMap<>())
                            .computeIfAbsent(nationalityKuerzel, ignored -> new HashSet<>())
                            .add(student.id());

                        if (student.geschlecht() != null && student.geschlecht() == 4) {
                            nationalityMaleStudentIdsByClass
                                .computeIfAbsent(la.idKlasse(), ignored -> new HashMap<>())
                                .computeIfAbsent(nationalityKuerzel, ignored -> new HashSet<>())
                                .add(student.id());
                        }

                        if (student.geschlecht() != null && student.geschlecht() == 3) {
                            nationalityFemaleStudentIdsByClass
                                .computeIfAbsent(la.idKlasse(), ignored -> new HashMap<>())
                                .computeIfAbsent(nationalityKuerzel, ignored -> new HashSet<>())
                                .add(student.id());
                        }
                    }
                }
            }
        }

        List<SchuleStatistikenGesamt.GradeStatistic> studentsByGrade = gradeCountsById.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(Integer::compareTo)))
                .map(e -> new SchuleStatistikenGesamt.GradeStatistic(
                        resolveGradeName(e.getKey(), gradeNamesById),
                        e.getValue()
                ))
                .toList();

        List<SchuleStatistikenGesamt.GradeConfessionStatistic> confessionsByGrade = confessionCountsByGrade.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(Integer::compareTo)))
                .map(gradeEntry -> {
                    List<SchuleStatistikenGesamt.ConfessionStatistic> confessions = gradeEntry.getValue().entrySet().stream()
                            .map(confessionEntry -> {
                                String confessionCode = confessionEntry.getKey();
                                return new SchuleStatistikenGesamt.ConfessionStatistic(
                                        confessionCode,
                                        resolveConfessionLabel(confessionCode),
                                        confessionEntry.getValue()
                                );
                            })
                            .sorted(
                                    Comparator.comparingInt(SchuleStatistikenGesamt.ConfessionStatistic::count)
                                            .reversed()
                                            .thenComparing(SchuleStatistikenGesamt.ConfessionStatistic::confessionName)
                            )
                            .toList();

                    return new SchuleStatistikenGesamt.GradeConfessionStatistic(
                            resolveGradeName(gradeEntry.getKey(), gradeNamesById),
                            confessions
                    );
                })
                .toList();

        // Top locations (top 5)
        Map<Integer, Integer> locationCounts = new HashMap<>();
        students.forEach(s -> {
            if (s.wohnortID() != null) {
                locationCounts.merge(s.wohnortID(), 1, Integer::sum);
            }
        });

        Map<Integer, SchuleStatistikenRaw.Ort> ortesById = new HashMap<>();
        if (rawData.orte() != null) {
            for (SchuleStatistikenRaw.Ort ort : rawData.orte()) {
                ortesById.put(ort.id(), ort);
            }
        }

        List<SchuleStatistikenGesamt.LocationStatistic> topLocations = locationCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    SchuleStatistikenRaw.Ort ort = ortesById.get(e.getKey());
                    String locationName = ort != null ? ort.ortsname() : "Unbekannt";
                    String postalCode = ort != null ? ort.plz() : "";
                    return new SchuleStatistikenGesamt.LocationStatistic(locationName, postalCode, e.getValue());
                })
                .toList();

            List<SchuleStatistikenGesamt.ClassStatistic> classStatistics = buildClassStatistics(
                rawData.klassen(),
                gradeNamesById,
                classStudentIds,
                maleStudentIdsByClass,
                femaleStudentIdsByClass,
                gradeStudentIdsByClass,
                specialNeedStudentIdsByClass,
                nationalityStudentIdsByClass,
                nationalityMaleStudentIdsByClass,
                nationalityFemaleStudentIdsByClass
            );

        return new SchuleStatistikenGesamt(
                totalStudents,
                maleStudents,
                femaleStudents,
                studentsWithSpecialNeeds,
                studentsWithMigrationBackground,
                abiStudentsEligible,
                abiStudentsPassed,
                studentsByGrade,
                confessionsByGrade,
                topLocations,
                classStatistics
        );
    }

    private SchuleStatistikenGesamt createEmptyStatistiken() {
        return new SchuleStatistikenGesamt(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

        private List<SchuleStatistikenGesamt.ClassStatistic> buildClassStatistics(
            List<SchuleStatistikenRaw.Klasse> classes,
            Map<Integer, String> gradeNamesById,
            Map<Integer, Set<Long>> classStudentIds,
            Map<Integer, Set<Long>> maleStudentIdsByClass,
            Map<Integer, Set<Long>> femaleStudentIdsByClass,
            Map<Integer, Map<Integer, Set<Long>>> gradeStudentIdsByClass,
            Map<Integer, Map<String, Set<Long>>> specialNeedStudentIdsByClass,
            Map<Integer, Map<String, Set<Long>>> nationalityStudentIdsByClass,
            Map<Integer, Map<String, Set<Long>>> nationalityMaleStudentIdsByClass,
            Map<Integer, Map<String, Set<Long>>> nationalityFemaleStudentIdsByClass
        ) {
        List<SchuleStatistikenGesamt.ClassStatistic> result = new ArrayList<>();
        Set<Integer> knownClassIds = new HashSet<>();
        List<SchuleStatistikenRaw.Klasse> sortedClasses = classes == null ? new ArrayList<>() : new ArrayList<>(classes);

        sortedClasses.sort(
                Comparator.comparing(
                                SchuleStatistikenRaw.Klasse::sortierung,
                                Comparator.nullsLast(Integer::compareTo)
                        )
                        .thenComparing(klasse -> klasse.kuerzel() == null ? "" : klasse.kuerzel())
        );

        for (SchuleStatistikenRaw.Klasse klasse : sortedClasses) {
            knownClassIds.add(klasse.id());
                result.add(createClassStatistic(
                    klasse.id(),
                    resolveClassName(klasse.id(), klasse.kuerzel()),
                    gradeNamesById,
                    classStudentIds.get(klasse.id()),
                    maleStudentIdsByClass.get(klasse.id()),
                    femaleStudentIdsByClass.get(klasse.id()),
                    gradeStudentIdsByClass.get(klasse.id()),
                    specialNeedStudentIdsByClass.get(klasse.id()),
                    nationalityStudentIdsByClass.get(klasse.id()),
                    nationalityMaleStudentIdsByClass.get(klasse.id()),
                    nationalityFemaleStudentIdsByClass.get(klasse.id())
                ));
        }

        classStudentIds.keySet().stream()
                .filter(classId -> !knownClassIds.contains(classId))
                .sorted(Comparator.nullsLast(Integer::compareTo))
                .forEach(classId -> result.add(createClassStatistic(
                    classId,
                    resolveClassName(classId, null),
                    gradeNamesById,
                    classStudentIds.get(classId),
                    maleStudentIdsByClass.get(classId),
                    femaleStudentIdsByClass.get(classId),
                    gradeStudentIdsByClass.get(classId),
                    specialNeedStudentIdsByClass.get(classId),
                    nationalityStudentIdsByClass.get(classId),
                    nationalityMaleStudentIdsByClass.get(classId),
                    nationalityFemaleStudentIdsByClass.get(classId)
                )));

        return result;
    }

        private SchuleStatistikenGesamt.ClassStatistic createClassStatistic(
            Integer classId,
            String className,
            Map<Integer, String> gradeNamesById,
            Set<Long> classStudents,
            Set<Long> maleStudents,
            Set<Long> femaleStudents,
            Map<Integer, Set<Long>> gradeStudentIds,
            Map<String, Set<Long>> specialNeedStudentIds,
            Map<String, Set<Long>> nationalityStudentIds,
            Map<String, Set<Long>> nationalityMaleStudentIds,
            Map<String, Set<Long>> nationalityFemaleStudentIds
        ) {
        List<SchuleStatistikenGesamt.GradeStatistic> grades = gradeStudentIds == null
                ? List.of()
                : gradeStudentIds.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(Integer::compareTo)))
                        .map(entry -> new SchuleStatistikenGesamt.GradeStatistic(
                                resolveGradeName(entry.getKey(), gradeNamesById),
                                entry.getValue().size()
                        ))
                        .toList();

        List<SchuleStatistikenGesamt.SpecialNeedStatistic> specialNeeds = specialNeedStudentIds == null
                ? List.of()
                : specialNeedStudentIds.entrySet().stream()
                        .map(entry -> new SchuleStatistikenGesamt.SpecialNeedStatistic(
                                entry.getKey(),
                                resolveSpecialNeedLabel(entry.getKey()),
                                entry.getValue().size()
                        ))
                        .sorted(
                                Comparator.comparingInt(SchuleStatistikenGesamt.SpecialNeedStatistic::count)
                                        .reversed()
                                        .thenComparing(SchuleStatistikenGesamt.SpecialNeedStatistic::specialNeedName)
                        )
                        .toList();

        List<SchuleStatistikenGesamt.NationalityStatistic> nationalities = nationalityStudentIds == null
            ? List.of()
            : nationalityStudentIds.entrySet().stream()
                .map(entry -> {
                    String kuerzel = entry.getKey();
                    int count = entry.getValue().size();
                    int maleCount = nationalityMaleStudentIds != null
                        && nationalityMaleStudentIds.containsKey(kuerzel)
                        ? nationalityMaleStudentIds.get(kuerzel).size() : 0;
                    int femaleCount = nationalityFemaleStudentIds != null
                        && nationalityFemaleStudentIds.containsKey(kuerzel)
                        ? nationalityFemaleStudentIds.get(kuerzel).size() : 0;
                    return new SchuleStatistikenGesamt.NationalityStatistic(
                        kuerzel, resolveNationalityLabel(kuerzel), count, maleCount, femaleCount);
                })
                .sorted(
                    Comparator.comparingInt(SchuleStatistikenGesamt.NationalityStatistic::count)
                        .reversed()
                        .thenComparing(SchuleStatistikenGesamt.NationalityStatistic::nationalityName)
                )
                .toList();

        return new SchuleStatistikenGesamt.ClassStatistic(
            classId,
            className,
            classStudents == null ? 0 : classStudents.size(),
            maleStudents == null ? 0 : maleStudents.size(),
            femaleStudents == null ? 0 : femaleStudents.size(),
            grades,
            specialNeeds,
            nationalities
        );
    }

    private String resolveGradeName(Integer gradeId, Map<Integer, String> gradeNamesById) {
        if (gradeId == null) {
            return "Ohne Jahrgang";
        }
        return gradeNamesById.getOrDefault(gradeId, "Jahrgang " + gradeId);
    }

    private String resolveConfessionCode(Integer religionId, Map<Integer, String> confessionCodesById) {
        if (religionId == null) {
            return "OH";
        }

        String confessionCode = confessionCodesById.get(religionId);
        if (confessionCode == null || confessionCode.isBlank()) {
            return UNKNOWN_CONFESSION_CODE;
        }

        return confessionCode;
    }

    private String resolveConfessionLabel(String confessionCode) {
        if (confessionCode == null || confessionCode.isBlank()) {
            return RELIGION_LABELS.get(UNKNOWN_CONFESSION_CODE);
        }

        return RELIGION_LABELS.getOrDefault(confessionCode, confessionCode);
    }

    private boolean hasSpecialNeed(SchuleStatistikenRaw.Schueler student) {
        if (student.idFoerderschwerpunkt1() != null || student.idFoerderschwerpunkt2() != null) {
            return true;
        }

        if (student.lernabschnitte() == null) {
            return false;
        }

        return student.lernabschnitte().stream().anyMatch(lernabschnitt ->
                lernabschnitt.idFoerderschwerpunkt1() != null || lernabschnitt.idFoerderschwerpunkt2() != null
        );
    }

    private List<String> resolveSpecialNeedCodes(
            SchuleStatistikenRaw.Schueler student,
            SchuleStatistikenRaw.Lernabschnitt lernabschnitt,
            Map<Integer, String> specialNeedCodesById
    ) {
        Integer firstId = lernabschnitt.idFoerderschwerpunkt1() != null
                ? lernabschnitt.idFoerderschwerpunkt1()
                : student.idFoerderschwerpunkt1();
        Integer secondId = lernabschnitt.idFoerderschwerpunkt2() != null
                ? lernabschnitt.idFoerderschwerpunkt2()
                : student.idFoerderschwerpunkt2();

        if (firstId == null && secondId == null) {
            return List.of(NO_SPECIAL_NEED_CODE);
        }

        List<String> specialNeedCodes = new ArrayList<>();
        String firstCode = resolveSpecialNeedCode(firstId, specialNeedCodesById);
        String secondCode = resolveSpecialNeedCode(secondId, specialNeedCodesById);

        if (firstCode != null) {
            specialNeedCodes.add(firstCode);
        }

        if (secondCode != null && !specialNeedCodes.contains(secondCode)) {
            specialNeedCodes.add(secondCode);
        }

        return specialNeedCodes.isEmpty() ? List.of(NO_SPECIAL_NEED_CODE) : specialNeedCodes;
    }

    private String resolveSpecialNeedCode(Integer specialNeedId, Map<Integer, String> specialNeedCodesById) {
        if (specialNeedId == null) {
            return null;
        }

        String specialNeedCode = specialNeedCodesById.get(specialNeedId);
        if (specialNeedCode == null || specialNeedCode.isBlank()) {
            return UNKNOWN_SPECIAL_NEED_CODE;
        }

        return specialNeedCode;
    }

    private String resolveNationalityKuerzel(String nationalityValue) {
        if (nationalityValue == null || nationalityValue.isBlank()) {
            return "UNBEKANNT";
        }

        String normalizedValue = nationalityValue.trim().toUpperCase(Locale.ROOT);
        if (normalizedValue.length() == 3
                && Character.isUpperCase(normalizedValue.charAt(0))
                && Character.isUpperCase(normalizedValue.charAt(1))
                && Character.isUpperCase(normalizedValue.charAt(2))) {
            return normalizedValue;
        }

        try {
            long nationalityId = Long.parseLong(normalizedValue);
            long baseId = nationalityId % 1_000_000_000L;
            if (baseId <= 0) {
                return "UNBEKANNT";
            }
            int c1 = (int) (baseId / 1_000_000L);
            int c2 = (int) ((baseId % 1_000_000L) / 1_000L);
            int c3 = (int) (baseId % 1_000L);
            if (c1 < 65 || c1 > 90 || c2 < 65 || c2 > 90 || c3 < 65 || c3 > 90) {
                return "UNBEKANNT";
            }
            return String.valueOf((char) c1) + (char) c2 + (char) c3;
        } catch (NumberFormatException ignored) {
            return "UNBEKANNT";
        }
    }

    private String resolveNationalityLabel(String kuerzel) {
        if (kuerzel == null || kuerzel.isBlank()) return NATIONALITY_LABELS.getOrDefault("UNBEKANNT", "Unbekannt");
        return NATIONALITY_LABELS.getOrDefault(kuerzel, kuerzel);
    }

    private String resolveSpecialNeedLabel(String specialNeedCode) {
        if (specialNeedCode == null || specialNeedCode.isBlank()) {
            return SPECIAL_NEED_LABELS.get(UNKNOWN_SPECIAL_NEED_CODE);
        }

        return SPECIAL_NEED_LABELS.getOrDefault(specialNeedCode, specialNeedCode);
    }

    private String resolveClassName(Integer classId, String className) {
        if (className != null && !className.isBlank()) {
            return className;
        }

        return classId == null ? "Ohne Klasse" : "Klasse " + classId;
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
