package de.schultraeger.application;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.PasswordCipher;
import de.schultraeger.application.port.out.SchuleRepository;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsServerRepository;
import de.schultraeger.domain.Schule;
import de.schultraeger.domain.SvwsServer;
import de.schultraeger.domain.ServerStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of(
                    createStubInfo("schema1"),
                    createStubInfo("schema2")
                );
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher());

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
        service(repo).saveSchoolIfNew(server, createStubInfo("schema1"));
        assertEquals(1, repo.findAllSchools().size());

        // Mock client returns schema1 and schema2
        SvwsClient client = new SvwsClient() {
            @Override
            public boolean isPrivileged(String baseUrl, String username, String password) { return true; }
            @Override
            public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) { return null; }
            @Override
            public List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
                return List.of(createStubInfo("schema1"), createStubInfo("schema2"));
            }
        };

        SchuleService service = new SchuleService(repo, client, new StubCipher());
        int imported = service.importSchoolsFromSvwsServer(server);

        assertEquals(1, imported); // Only schema2 is new
        assertEquals(2, repo.findAllSchools().size());
    }

    private SchuleService service(SchuleRepository repo) {
        return new SchuleService(repo, null, new StubCipher());
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
}
