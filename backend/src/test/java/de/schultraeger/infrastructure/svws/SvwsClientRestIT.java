package de.schultraeger.infrastructure.svws;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvwsClientRestIT {
    @Test
    void shouldReadServerVersionFromStatusEndpoint() {
        String baseUrl = System.getenv("SVWS_TEST_URL");
        Assumptions.assumeTrue(baseUrl != null && !baseUrl.isBlank(), "SVWS_TEST_URL not set");

        SvwsClientRest client = new SvwsClientRest(true, true);
        String version = client.getServerVersion(baseUrl);

        assertNotNull(version);
        assertTrue(!version.isBlank(), "Expected non-empty server version");
    }

    @Test
    void shouldReachLocalSvwsInstance() {
        String baseUrl = System.getenv("SVWS_TEST_URL");
        String username = System.getenv("SVWS_TEST_USER");
        String password = System.getenv("SVWS_TEST_PASSWORD");
        String schema = System.getenv("SVWS_TEST_SCHEMA");

        Assumptions.assumeTrue(baseUrl != null && !baseUrl.isBlank(), "SVWS_TEST_URL not set");
        Assumptions.assumeTrue(username != null && !username.isBlank(), "SVWS_TEST_USER not set");
        Assumptions.assumeTrue(password != null && !password.isBlank(), "SVWS_TEST_PASSWORD not set");
        Assumptions.assumeTrue(schema != null && !schema.isBlank(), "SVWS_TEST_SCHEMA not set");

        SvwsClientRest client = new SvwsClientRest(true, true);
        boolean privileged = client.isPrivileged(baseUrl, username, password);
        assertTrue(privileged, "Expected privileged credentials");

        SvwsSchuleInfo info = client.getSchuleInfo(baseUrl, schema, username, password);
        assertNotNull(info);
        assertNotNull(info.name());
    }
}
