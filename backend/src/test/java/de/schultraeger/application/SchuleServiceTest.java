package de.schultraeger.application;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.dto.SchuleStatistikenGesamt;
import de.schultraeger.application.dto.SchuleStatistikenRaw;
import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.NrwSchulkatalogeintragRepository;
import de.schultraeger.application.port.out.SchuleRepository;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.NrwSchulkatalogeintrag;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SvwsServer;
import de.schultraeger.domain.ServerStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchuleServiceTest {
    private final UUID SERVER_ID = UUID.randomUUID();

    @Test
    void importSchoolsShouldCreateNewSchools() {
        InMemoryRepo repo = new InMemoryRepo();
        StubSvwsServerRepository serverRepo = new StubSvwsServerRepository();
        
        // Mock SVWS server
        SvwsServer server = serverRepo.getById(SERVER_ID).get();
        
        // Mock client returning 2 schools
        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) { return true; }
            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) { return null; }
            @Override
            public de.schultraeger.application.dto.SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public de.schultraeger.application.dto.SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of(
                    createStubInfo("schema1"),
                    createStubInfo("schema2")
                );
            }
            @Override
            public void destroySchema(String baseUrl, String schema, String username, String password) {
                // No-op for test
            }
            @Override
            public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
                return new byte[0];
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher(), serverRepo, null);

        int imported = service.importSchoolsFromSvwsServer(server);

        assertEquals(2, imported);
        assertEquals(2, repo.findAllSchools().size());
        assertTrue(repo.findByServerIdAndSchema(SERVER_ID, "schema1").isPresent());
        assertTrue(repo.findByServerIdAndSchema(SERVER_ID, "schema2").isPresent());
    }

    @Test
    void importSchoolsShouldSkipExistingSchemas() {
        InMemoryRepo repo = new InMemoryRepo();
        StubSvwsServerRepository serverRepo = new StubSvwsServerRepository();
        SvwsServer server = serverRepo.getById(SERVER_ID).get();

        // Already has schema1
        service(repo, serverRepo).saveSchoolIfNew(server, createStubInfo("schema1"));
        assertEquals(1, repo.findAllSchools().size());

        // Mock client returns schema1 and schema2
        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) { return true; }
            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) { return null; }
            @Override
            public de.schultraeger.application.dto.SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public de.schultraeger.application.dto.SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of(createStubInfo("schema1"), createStubInfo("schema2"));
            }
            @Override
            public void destroySchema(String baseUrl, String schema, String username, String password) {
                // No-op for test
            }
            @Override
            public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
                return new byte[0];
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher(), serverRepo, null);
        int imported = service.importSchoolsFromSvwsServer(server);

        assertEquals(1, imported); // Only schema2 is new
        assertEquals(2, repo.findAllSchools().size());
    }

    @Test
    void importSchoolsShouldSkipBlankOrNullSchemas() {
        InMemoryRepo repo = new InMemoryRepo();
        StubSvwsServerRepository serverRepo = new StubSvwsServerRepository();
        SvwsServer server = serverRepo.getById(SERVER_ID).get();

        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) { return true; }
            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) { return null; }
            @Override
            public de.schultraeger.application.dto.SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public de.schultraeger.application.dto.SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of(
                    createStubInfo("schema1"),
                    createStubInfo("  "),
                    createStubInfo(null)
                );
            }
            @Override
            public void destroySchema(String baseUrl, String schema, String username, String password) {
                // No-op for test
            }
            @Override
            public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
                return new byte[0];
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher(), serverRepo, null);

        int imported = service.importSchoolsFromSvwsServer(server);

        assertEquals(1, imported);
        assertEquals(1, repo.findAllSchools().size());
        assertTrue(repo.findByServerIdAndSchema(SERVER_ID, "schema1").isPresent());
    }

    @Test
    void deleteShouldDestroySchemaOnServerAndRemoveFromDatabase() {
        InMemoryRepo repo = new InMemoryRepo();
        StubSvwsServerRepository serverRepo = new StubSvwsServerRepository();
        SvwsServer server = serverRepo.getById(SERVER_ID).get();
        
        final boolean[] destroyCalled = {false};
        final String[] destroyedSchema = {null};
        
        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) { return true; }
            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) { return null; }
            @Override
            public de.schultraeger.application.dto.SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public de.schultraeger.application.dto.SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
                return null;
            }
            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of();
            }
            @Override
            public void destroySchema(String baseUrl, String schema, String username, String password) {
                destroyCalled[0] = true;
                destroyedSchema[0] = schema;
            }
            @Override
            public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
                return new byte[0];
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher(), serverRepo, null);
        
        // Create a school
        service.saveSchoolIfNew(server, createStubInfo("test-schema"));
        UUID schuleId = repo.findByServerIdAndSchema(SERVER_ID, "test-schema").get().id();
        
        assertEquals(1, repo.findAllSchools().size());
        
        // Delete it
        service.delete(schuleId);
        
        // Verify destroySchema was called
        assertTrue(destroyCalled[0], "destroySchema should have been called");
        assertEquals("test-schema", destroyedSchema[0]);
        
        // Verify removed from database
        assertEquals(0, repo.findAllSchools().size());
    }

    @Test
    void getStatistikenShouldIncludeConfessionsAndClassStatistics() {
        InMemoryRepo repo = new InMemoryRepo();
        StubSvwsServerRepository serverRepo = new StubSvwsServerRepository();
        SvwsServer server = serverRepo.getById(SERVER_ID).orElseThrow();

        SchuleService bootstrap = new SchuleService(repo, null, new StubCipher(), serverRepo, null);
        bootstrap.saveSchoolIfNew(server, createStubInfo("schema-confessions"));
        UUID schuleId = repo.findByServerIdAndSchema(SERVER_ID, "schema-confessions").orElseThrow().id();

        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) {
                return true;
            }

            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) {
                return null;
            }

            @Override
            public de.schultraeger.application.dto.SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
                return null;
            }

            @Override
            public SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
                return new SchuleStatistikenRaw(
                        List.of(
                                new SchuleStatistikenRaw.Schueler(
                                        1L,
                                        4,
                                    List.of(new SchuleStatistikenRaw.Lernabschnitt(1, 10, null, null, null)),
                                        null,
                                        5000,
                                        "DEU",
                                        null,
                                        null,
                                        null,
                                        false,
                                        2,
                                        null
                                ),
                                new SchuleStatistikenRaw.Schueler(
                                    2L,
                                    3,
                                    List.of(new SchuleStatistikenRaw.Lernabschnitt(1, 10, 2001, null, null)),
                                    null,
                                    5000,
                                    "TUR",
                                    null,
                                    null,
                                    null,
                                    false,
                                    2,
                                    null
                                ),
                                new SchuleStatistikenRaw.Schueler(
                                    3L,
                                    3,
                                    List.of(new SchuleStatistikenRaw.Lernabschnitt(2, 10, 3001, 2001, null)),
                                    null,
                                    2000,
                                    "TUR",
                                    null,
                                    null,
                                    null,
                                    false,
                                    2,
                                    null
                                ),
                                new SchuleStatistikenRaw.Schueler(
                                    4L,
                                    4,
                                    List.of(new SchuleStatistikenRaw.Lernabschnitt(2, 20, 7001, null, null)),
                                    null,
                                    7000,
                                    "DEU",
                                    null,
                                    null,
                                    null,
                                    false,
                                    2,
                                    null
                                )
                        ),
                        List.of(
                                new SchuleStatistikenRaw.Jahrgang(1, "5"),
                                new SchuleStatistikenRaw.Jahrgang(2, "6")
                        ),
                        List.of(
                                new SchuleStatistikenRaw.Religion(2000, "ER"),
                                new SchuleStatistikenRaw.Religion(5000, "KR"),
                                new SchuleStatistikenRaw.Religion(7000, "OH")
                        ),
                        List.of(
                                new SchuleStatistikenRaw.Klasse(10, "MIX", null, 1),
                                new SchuleStatistikenRaw.Klasse(20, "06B", 2, 2)
                        ),
                        List.of(
                                new SchuleStatistikenRaw.Foerderschwerpunkt(1, "**"),
                                new SchuleStatistikenRaw.Foerderschwerpunkt(2001, "EZ"),
                                new SchuleStatistikenRaw.Foerderschwerpunkt(3001, "GB"),
                                new SchuleStatistikenRaw.Foerderschwerpunkt(7001, "LB")
                        ),
                        List.of()
                );
            }

            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of();
            }

            @Override
            public void destroySchema(String baseUrl, String schema, String username, String password) {
                // No-op for test
            }

            @Override
            public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
                return new byte[0];
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher(), serverRepo, null);
        SchuleStatistikenGesamt result = service.getStatistiken(schuleId);

        assertEquals(2, result.confessionsByGrade().size());

        SchuleStatistikenGesamt.GradeConfessionStatistic grade5 = result.confessionsByGrade().stream()
                .filter(grade -> "5".equals(grade.gradeName()))
                .findFirst()
                .orElseThrow();

        SchuleStatistikenGesamt.GradeConfessionStatistic grade6 = result.confessionsByGrade().stream()
                .filter(grade -> "6".equals(grade.gradeName()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, grade5.confessions().stream()
                .filter(confession -> "KR".equals(confession.confessionCode()))
                .findFirst()
                .orElseThrow()
                .count());

        assertEquals(1, grade6.confessions().stream()
                .filter(confession -> "ER".equals(confession.confessionCode()))
                .findFirst()
                .orElseThrow()
                .count());

        assertEquals(1, grade6.confessions().stream()
                .filter(confession -> "OH".equals(confession.confessionCode()))
                .findFirst()
                .orElseThrow()
                .count());

        assertEquals(2, result.classStatistics().size());

        SchuleStatistikenGesamt.ClassStatistic mixedClass = result.classStatistics().stream()
                .filter(classStatistic -> "MIX".equals(classStatistic.className()))
                .findFirst()
                .orElseThrow();

        assertEquals(3, mixedClass.totalStudents());
        assertEquals(1, mixedClass.maleStudents());
        assertEquals(2, mixedClass.femaleStudents());
        assertEquals(2, mixedClass.grades().size());
        assertEquals(2, mixedClass.grades().stream()
                .filter(grade -> "5".equals(grade.gradeName()))
                .findFirst()
                .orElseThrow()
                .count());
        assertEquals(1, mixedClass.grades().stream()
                .filter(grade -> "6".equals(grade.gradeName()))
                .findFirst()
                .orElseThrow()
                .count());
        assertEquals(2, mixedClass.specialNeeds().stream()
            .filter(specialNeed -> "EZ".equals(specialNeed.specialNeedCode()))
            .findFirst()
            .orElseThrow()
            .count());
        assertEquals(1, mixedClass.specialNeeds().stream()
            .filter(specialNeed -> "GB".equals(specialNeed.specialNeedCode()))
            .findFirst()
            .orElseThrow()
            .count());
        assertEquals(1, mixedClass.specialNeeds().stream()
            .filter(specialNeed -> "**".equals(specialNeed.specialNeedCode()))
            .findFirst()
            .orElseThrow()
            .count());

        // Students 1 (DEU, male) and 2 (TUR, female) and 3 (TUR, female) in class MIX
        assertEquals(2, mixedClass.nationalities().size());
        SchuleStatistikenGesamt.NationalityStatistic mixDeu = mixedClass.nationalities().stream()
            .filter(n -> "DEU".equals(n.nationalityCode()))
            .findFirst()
            .orElseThrow();
        assertEquals(1, mixDeu.count());
        assertEquals(1, mixDeu.maleCount());
        assertEquals(0, mixDeu.femaleCount());
        SchuleStatistikenGesamt.NationalityStatistic mixTur = mixedClass.nationalities().stream()
            .filter(n -> "TUR".equals(n.nationalityCode()))
            .findFirst()
            .orElseThrow();
        assertEquals(2, mixTur.count());
        assertEquals(0, mixTur.maleCount());
        assertEquals(2, mixTur.femaleCount());
    }

    @Test
    void getStatistikenShouldNormalizeOriginSchoolFormKuerzel() {
        InMemoryRepo repo = new InMemoryRepo();
        StubSvwsServerRepository serverRepo = new StubSvwsServerRepository();
        SvwsServer server = serverRepo.getById(SERVER_ID).orElseThrow();

        SchuleService bootstrap = new SchuleService(repo, null, new StubCipher(), serverRepo, null);
        bootstrap.saveSchoolIfNew(server, createStubInfo("schema-origin-schoolform"));
        UUID schuleId = repo.findByServerIdAndSchema(SERVER_ID, "schema-origin-schoolform").orElseThrow().id();

        StubNrwRepository nrwRepo = new StubNrwRepository();
        nrwRepo.put(createNrwEntry("105752", "GMS", "Leverkusen, GG Am Friedenspark", "Staedt. Gemeinschaftsgrundschule"));
        nrwRepo.put(createNrwEntry("112082", "RKB", "Duesseldorf, KG Kartause-Hain-Schule", "Staedt. Kath. Grundschule"));
        nrwRepo.put(createNrwEntry("100010", "20", "Koeln, Gym Gymnasium Claudia Agrippina", "Gymnasium Claudia Agrippina"));

        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) {
                return true;
            }

            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) {
                return null;
            }

            @Override
            public de.schultraeger.application.dto.SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
                return null;
            }

            @Override
            public SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
                return new SchuleStatistikenRaw(
                        List.of(
                                new SchuleStatistikenRaw.Schueler(
                                        1L,
                                        4,
                                        List.of(new SchuleStatistikenRaw.Lernabschnitt(1, 10, null, null, null)),
                                        null,
                                        null,
                                        "DEU",
                                        null,
                                        null,
                                        null,
                                        false,
                                        2,
                                        "105752"
                                ),
                                new SchuleStatistikenRaw.Schueler(
                                        2L,
                                        3,
                                        List.of(new SchuleStatistikenRaw.Lernabschnitt(1, 10, null, null, null)),
                                        null,
                                        null,
                                        "DEU",
                                        null,
                                        null,
                                        null,
                                        false,
                                        2,
                                        "112082"
                                ),
                                new SchuleStatistikenRaw.Schueler(
                                        3L,
                                        3,
                                        List.of(new SchuleStatistikenRaw.Lernabschnitt(1, 10, null, null, null)),
                                        null,
                                        null,
                                        "DEU",
                                        null,
                                        null,
                                        null,
                                        false,
                                        2,
                                        "100010"
                                )
                        ),
                        List.of(new SchuleStatistikenRaw.Jahrgang(1, "5")),
                        List.of(),
                        List.of(new SchuleStatistikenRaw.Klasse(10, "MIX", null, 1)),
                        List.of(),
                        List.of()
                );
            }

            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of();
            }

            @Override
            public void destroySchema(String baseUrl, String schema, String username, String password) {
                // No-op for test
            }

            @Override
            public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
                return new byte[0];
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher(), serverRepo, nrwRepo);
        SchuleStatistikenGesamt result = service.getStatistiken(schuleId);

        SchuleStatistikenGesamt.ClassStatistic mixedClass = result.classStatistics().stream()
                .filter(classStatistic -> "MIX".equals(classStatistic.className()))
                .findFirst()
                .orElseThrow();

        SchuleStatistikenGesamt.OriginSchoolStatistic grundschule = mixedClass.originSchools().stream()
                .filter(origin -> "105752".equals(origin.schulnummer()))
                .findFirst()
                .orElseThrow();
        assertEquals("G", grundschule.schulformKuerzel());
        assertEquals(1, grundschule.maleCount());
        assertEquals(0, grundschule.femaleCount());

        SchuleStatistikenGesamt.OriginSchoolStatistic grundschuleAlias = mixedClass.originSchools().stream()
                .filter(origin -> "112082".equals(origin.schulnummer()))
                .findFirst()
                .orElseThrow();
        assertEquals("G", grundschuleAlias.schulformKuerzel());
        assertEquals(0, grundschuleAlias.maleCount());
        assertEquals(1, grundschuleAlias.femaleCount());

        SchuleStatistikenGesamt.OriginSchoolStatistic gymnasium = mixedClass.originSchools().stream()
                .filter(origin -> "100010".equals(origin.schulnummer()))
                .findFirst()
                .orElseThrow();
        assertEquals("GY", gymnasium.schulformKuerzel());
    }

    private SchuleService service(SchuleRepository repo, SvwsServerRepository serverRepo) {
        return new SchuleService(repo, null, new StubCipher(), serverRepo, null);
    }

    private NrwSchulkatalogeintrag createNrwEntry(String schulnummer, String schultyp, String schulname, String amtsbez1) {
        return new NrwSchulkatalogeintrag(
                UUID.randomUUID(),
                schulnummer,
                "1",
                amtsbez1,
                null,
                null,
                null,
                null,
                schulname,
                schultyp,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    private SvwsSchuleInfo createStubInfo(String schema) {
        return new SvwsSchuleInfo(null, null, schema, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static class InMemoryRepo implements SchuleRepository {
        private final List<Schule> data = new ArrayList<>();

        @Override
        public List<Schule> findAllSchools() {
            return List.copyOf(data);
        }

        @Override
        public Optional<Schule> findSchoolById(UUID id) {
            return data.stream().filter(item -> item.id().equals(id)).findFirst();
        }

        @Override
        public Optional<Schule> findByServerIdAndSchema(UUID svwsServerId, String svwsSchema) {
            return data.stream()
                .filter(item -> item.svwsServerId().equals(svwsServerId) && item.svwsSchema().equals(svwsSchema))
                .findFirst();
        }

        @Override
        public Schule saveSchool(Schule schule) {
            data.add(schule);
            return schule;
        }

        @Override
        public Schule updateSchool(Schule schule) {
            data.removeIf(item -> item.id().equals(schule.id()));
            data.add(schule);
            return schule;
        }

        @Override
        public void deleteByServerId(UUID svwsServerId) {
            data.removeIf(item -> item.svwsServerId().equals(svwsServerId));
        }

        @Override
        public void deleteSchool(UUID id) {
            data.removeIf(item -> item.id().equals(id));
        }
    }

    private class StubSvwsServerRepository implements SvwsServerRepository {
        @Override
        public List<SvwsServer> getAllServers() {
            return List.of();
        }

        @Override
        public Optional<SvwsServer> getById(UUID id) {
            return Optional.of(new SvwsServer(
                SERVER_ID,
                "Test Server",
                "https://svws.local",
                "admin",
                "enc(pass)",
                ServerStatus.CONNECTED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
            ));
        }

        @Override
        public SvwsServer save(SvwsServer server) {
            return server;
        }

        @Override
        public void delete(UUID id) {
        }
    }

    private static class StubCipher implements PasswordCipher {
        @Override
        public String encrypt(String plainText) {
            return "enc(" + plainText + ")";
        }

        @Override
        public String decrypt(String encryptedText) {
            return encryptedText.replace("enc(", "").replace(")", "");
        }
    }

    private static class StubNrwRepository implements NrwSchulkatalogeintragRepository {
        private final Map<String, NrwSchulkatalogeintrag> data = new HashMap<>();

        void put(NrwSchulkatalogeintrag entry) {
            data.put(entry.schulnummer(), entry);
        }

        @Override
        public List<NrwSchulkatalogeintrag> findAll(int offset, int limit) {
            return List.copyOf(data.values());
        }

        @Override
        public List<NrwSchulkatalogeintrag> findAll(int offset, int limit, String sortBy, String sortDir) {
            return List.copyOf(data.values());
        }

        @Override
        public List<NrwSchulkatalogeintrag> search(String query, int offset, int limit) {
            return List.of();
        }

        @Override
        public List<NrwSchulkatalogeintrag> search(String query, int offset, int limit, String sortBy, String sortDir) {
            return List.of();
        }

        @Override
        public Optional<NrwSchulkatalogeintrag> findBySchulnummer(String schulnummer) {
            return Optional.ofNullable(data.get(schulnummer));
        }

        @Override
        public void save(NrwSchulkatalogeintrag eintrag) {
            data.put(eintrag.schulnummer(), eintrag);
        }

        @Override
        public void saveAll(List<NrwSchulkatalogeintrag> eintraege) {
            for (NrwSchulkatalogeintrag entry : eintraege) {
                save(entry);
            }
        }

        @Override
        public void clearAll() {
            data.clear();
        }

        @Override
        public long getTotalCount() {
            return data.size();
        }

        @Override
        public long countSearch(String query) {
            return 0;
        }
    }
}
