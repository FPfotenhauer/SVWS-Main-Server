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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SchuleServiceTest {
    @Test
    void createShouldEncryptPasswordAndSetUnverified() {
        InMemoryRepo repo = new InMemoryRepo();
        SchuleService service = new SchuleService(repo, new StubSvwsClient(), new StubCipher());

        Schule created = service.create(new SchuleCreateData(
                "Schule A",
                "https://svws.local",
                "schema1",
                "user",
                "secret"
        ));

        assertNotNull(created.id());
        assertEquals(SchuleStatus.UNVERIFIED, created.status());
        assertEquals("enc(secret)", created.svwsPasswordEncrypted());
    }

    @Test
    void verifyShouldSetInvalidCredentials() {
        InMemoryRepo repo = new InMemoryRepo();
        SchuleService service = new SchuleService(repo, new StubSvwsClient(), new StubCipher());
        Schule created = service.create(new SchuleCreateData(
                "Schule A",
                "https://svws.local",
                "schema1",
                "user",
                "secret"
        ));

        Schule verified = service.verify(created.id());
        assertEquals(SchuleStatus.INVALID_CREDENTIALS, verified.status());
    }

    @Test
    void syncShouldUpdateSchulnummerAndNameOnSuccess() {
        InMemoryRepo repo = new InMemoryRepo();
        SchuleService service = new SchuleService(repo, new SuccessSvwsClient(), new StubCipher());
        Schule created = service.create(new SchuleCreateData(
                "Schule A",
                "https://svws.local",
                "schema1",
                "user",
                "secret"
        ));

        Schule synced = service.sync(created.id());
        assertEquals(SchuleStatus.VERIFIED, synced.status());
        assertEquals(SyncStatus.SUCCESS, synced.lastSyncStatus());
        assertEquals(123456L, synced.schulnummer());
        assertEquals("Staedt. Gymnasium", synced.name());
    }

    @Test
    void updateShouldResetStatus() {
        InMemoryRepo repo = new InMemoryRepo();
        SchuleService service = new SchuleService(repo, new StubSvwsClient(), new StubCipher());
        Schule created = service.create(new SchuleCreateData(
                "Schule A",
                "https://svws.local",
                "schema1",
                "user",
                "secret"
        ));

        Schule updated = service.update(created.id(), new SchuleUpdateData(
                "Schule B",
                "https://svws.local",
                "schema2",
                "user2",
                "secret2"
        ));

        assertEquals("Schule B", updated.name());
        assertEquals(SchuleStatus.UNVERIFIED, updated.status());
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
    }

    private static class StubTenant implements TenantContext {
        @Override
        public String getUserId() {
            return "user-1";
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

    private static class StubSvwsClient implements SvwsClient {
        @Override
        public boolean isPrivileged(String baseUrl, String username, String password) {
            return false;
        }

        @Override
        public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) {
            throw new SvwsClientException("Unauthorized", 403);
        }

        @Override
        public java.util.List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
            return java.util.Collections.emptyList();
        }
    }

    private static class SuccessSvwsClient implements SvwsClient {
        @Override
        public boolean isPrivileged(String baseUrl, String username, String password) {
            return true;
        }

        @Override
        public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) {
            return new SvwsSchuleInfo(
                    Long.valueOf(123456L),     // schulnummer
                    "Staedt. Gymnasium",        // name
                    schema,                     // schema
                    "12345",                    // plz
                    "Teststadt",                // ort
                    null,                       // strasse
                    null,                       // hausnummer
                    null,                       // hausnummerZusatz
                    null,                       // telefon
                    null,                       // fax
                    null,                       // email
                    null,                       // homepage
                    null,                       // schulform
                    null,                       // schulart
                    null,                       // schulgliederung
                    null,                       // schulleiter
                    null,                       // schulleiterTelefon
                    null,                       // schulleiterEmail
                    null,                       // kreis
                    null,                       // schulamt
                    null,                       // staat
                    null,                       // schulnummer2
                    null,                       // schulstatus
                    null,                       // kapitel
                    null                        // satzungsgebendeKommune
            );
        }

        @Override
        public java.util.List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
            return java.util.Collections.emptyList();
        }
    }
}
