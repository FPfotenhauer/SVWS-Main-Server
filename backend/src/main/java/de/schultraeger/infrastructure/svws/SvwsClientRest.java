package de.schultraeger.infrastructure.svws;

import de.schultraeger.application.dto.SchuleStammdaten;
import de.schultraeger.application.dto.SchuleStatistikenRaw;
import de.schultraeger.application.dto.OrtKatalogEintrag;
import de.schultraeger.application.dto.SchuelerAuswahl;
import de.schultraeger.application.dto.SchuelerStammdaten;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * REST client adapter for the SVWS privileged API using Java HttpClient.
 */
@ApplicationScoped
public class SvwsClientRest implements SvwsClient {
    private static final Logger log = Logger.getLogger(SvwsClientRest.class);
    
    private final boolean trustAll;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public SvwsClientRest(@ConfigProperty(name = "svws.client.trust-all", defaultValue = "false") boolean trustAll) {
        this.trustAll = trustAll;
        if (trustAll) {
            log.warn("SVWS client configured to trust all SSL certificates - this should only be used in development!");
        }
    }

    SvwsClientRest(boolean trustAll, boolean testMode) {
        this.trustAll = trustAll;
    }

    @Override
    public boolean isPrivileged(String baseUrl, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/privileged/user/isprivileged"))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            } else if (response.statusCode() == 403) {
                return false;
            }
            throw new SvwsClientException("SVWS isPrivileged failed", response.statusCode(), null);
        } catch (Exception ex) {
            throw new SvwsClientException("SVWS isPrivileged failed", -1, ex);
        }
    }

    @Override
    public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            String url = baseUrl + "/api/schema/liste/info/" + schema + "/schule";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), SvwsSchuleInfo.class);
            }
            log.warnf("SVWS getSchuleInfo failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS getSchuleInfo failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (Exception ex) {
            log.errorf(ex, "SVWS getSchuleInfo failed for schema %s", schema);
            throw new SvwsClientException("SVWS getSchuleInfo failed", -1, ex);
        }
    }

    @Override
    public SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            String url = baseUrl + "/db/" + encodedSchema + "/schule/stammdaten";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), SchuleStammdaten.class);
            }
            log.warnf("SVWS getSchuleStammdaten failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS getSchuleStammdaten failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (Exception ex) {
            log.errorf(ex, "SVWS getSchuleStammdaten failed for schema %s", schema);
            throw new SvwsClientException("SVWS getSchuleStammdaten failed", -1, ex);
        }
    }

    @Override
    public SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            String url = baseUrl + "/db/" + encodedSchema + "/statistik/gesamt";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(60))  // Statistics can be large, use longer timeout
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try {
                    return objectMapper.readValue(response.body(), SchuleStatistikenRaw.class);
                } catch (Exception jsonEx) {
                    log.warnf("SVWS getSchuleStatistiken returned 200 but JSON parsing failed for schema %s: %s", schema, jsonEx.getMessage());
                    throw new SvwsClientException("SVWS getSchuleStatistiken JSON parsing failed", 200, jsonEx);
                }
            }
            log.warnf("SVWS getSchuleStatistiken failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS getSchuleStatistiken failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (SvwsClientException ex) {
            // Re-throw SvwsClientException as-is to preserve status code
            throw ex;
        } catch (Exception ex) {
            log.errorf(ex, "SVWS getSchuleStatistiken failed for schema %s", schema);
            throw new SvwsClientException("SVWS getSchuleStatistiken failed", -1, ex);
        }
    }

    @Override
    public List<SchuelerAuswahl> getSchuelerAuswahlliste(String baseUrl, String schema, String username, String password, Integer abschnitt, List<Integer> status) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            StringBuilder urlBuilder = new StringBuilder(baseUrl)
                    .append("/db/")
                    .append(encodedSchema)
                    .append("/schueler/abschnitt/")
                    .append(abschnitt)
                    .append("/auswahlliste");

            if (status != null && !status.isEmpty()) {
                urlBuilder.append("?");
                for (int i = 0; i < status.size(); i++) {
                    if (i > 0) {
                        urlBuilder.append("&");
                    }
                    urlBuilder.append("status=").append(status.get(i));
                }
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseListResponse(response.body(), new TypeReference<List<SchuelerAuswahl>>() {});
            }
            log.warnf("SVWS getSchuelerAuswahlliste failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS getSchuelerAuswahlliste failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (SvwsClientException ex) {
            throw ex;
        } catch (Exception ex) {
            log.errorf(ex, "SVWS getSchuelerAuswahlliste failed for schema %s", schema);
            throw new SvwsClientException("SVWS getSchuelerAuswahlliste failed", -1, ex);
        }
    }

    @Override
    public List<SchuelerStammdaten> getSchuelerStammdaten(String baseUrl, String schema, String username, String password) {
        return getSchuelerStammdatenByIds(baseUrl, schema, username, password, List.of());
        }

        @Override
        public List<SchuelerStammdaten> getSchuelerStammdatenByIds(String baseUrl, String schema, String username, String password, List<Long> schuelerIds) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            String url = baseUrl + "/db/" + encodedSchema + "/schueler/stammdaten";
            String payload = objectMapper.writeValueAsString(schuelerIds == null ? List.of() : schuelerIds);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseListResponse(response.body(), new TypeReference<List<SchuelerStammdaten>>() {});
            }
            log.warnf("SVWS getSchuelerStammdatenByIds failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS getSchuelerStammdaten failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (SvwsClientException ex) {
            throw ex;
        } catch (Exception ex) {
            log.errorf(ex, "SVWS getSchuelerStammdatenByIds failed for schema %s", schema);
            throw new SvwsClientException("SVWS getSchuelerStammdaten failed", -1, ex);
        }
    }

    @Override
    public List<OrtKatalogEintrag> getOrte(String baseUrl, String schema, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            String url = baseUrl + "/db/" + encodedSchema + "/orte";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseListResponse(response.body(), new TypeReference<List<OrtKatalogEintrag>>() {});
            }
            log.warnf("SVWS getOrte failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS getOrte failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (SvwsClientException ex) {
            throw ex;
        } catch (Exception ex) {
            log.errorf(ex, "SVWS getOrte failed for schema %s", schema);
            throw new SvwsClientException("SVWS getOrte failed", -1, ex);
        }
    }

    private <T> List<T> parseListResponse(String responseBody, TypeReference<List<T>> typeReference) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        if (root.isArray()) {
            return objectMapper.convertValue(root, typeReference);
        }

        if (root.isObject()) {
            JsonNode candidates = root.get("daten");
            if (candidates == null) {
                candidates = root.get("schueler");
            }
            if (candidates == null) {
                candidates = root.get("items");
            }
            if (candidates == null) {
                var fields = root.fields();
                while (fields.hasNext()) {
                    var field = fields.next();
                    if (field.getValue() != null && field.getValue().isArray()) {
                        candidates = field.getValue();
                        break;
                    }
                }
            }
            if (candidates != null && candidates.isArray()) {
                return objectMapper.convertValue(candidates, typeReference);
            }
        }

        throw new IllegalStateException("Unerwartetes Antwortformat von SVWS");
    }

    @Override
    public java.util.List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
        System.out.println("DEBUG: SvwsClientRest.listSchools for " + baseUrl);
        HttpClient client = buildHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/schema/liste/svws"))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                System.err.println("DEBUG: listSchools failed with status " + response.statusCode());
                System.err.println("DEBUG: Response body: " + response.body());
                throw new SvwsClientException("SVWS listSchools failed with status " + response.statusCode(), response.statusCode(), null);
            }
            
            java.util.List<de.schultraeger.application.dto.SchemaListeEintrag> schemas = 
                    objectMapper.readValue(response.body(), new TypeReference<java.util.List<de.schultraeger.application.dto.SchemaListeEintrag>>() {});
            
            System.out.println("DEBUG: Found " + schemas.size() + " total schemas on SVWS server");
            
            return schemas.stream()
                .map(schema -> {
                    String schemaName = normalizeSchemaName(schema.name());
                    if (schemaName == null) {
                        log.warn("Skipping schema with missing/blank name from SVWS schema list");
                        return null;
                    }
                    try {
                        System.out.println("DEBUG: Fetching info for schema " + schemaName);
                        SvwsSchuleInfo info = getSchuleInfo(baseUrl, schemaName, username, password);
                        return withSchema(info, schemaName);
                    } catch (Exception ex) {
                        log.warnf(ex, "Failed to get detailed school info for schema %s - importing schema-only entry", schemaName);
                        return fallbackInfoForSchema(schemaName);
                    }
                })
                .filter(info -> info != null)
                .toList();
        } catch (Exception ex) {
            System.err.println("DEBUG: listSchools total failure: " + ex.getMessage());
            ex.printStackTrace();
            throw new SvwsClientException("SVWS listSchools failed", -1, ex);
        }
    }

    private String normalizeSchemaName(String schemaName) {
        if (schemaName == null) {
            return null;
        }
        String normalized = schemaName.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private SvwsSchuleInfo fallbackInfoForSchema(String schemaName) {
        return new SvwsSchuleInfo(
                null,
                schemaName,
                schemaName,
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
                null,
                null
        );
    }

    @Override
    public void destroySchema(String baseUrl, String schema, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            String url = baseUrl + "/api/schema/root/destroy/" + encodedSchema;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                log.infof("Successfully destroyed schema %s", schema);
                return;
            }
            log.warnf("SVWS destroySchema failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS destroySchema failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (SvwsClientException ex) {
            throw ex;
        } catch (Exception ex) {
            log.errorf(ex, "SVWS destroySchema failed for schema %s", schema);
            throw new SvwsClientException("SVWS destroySchema failed", -1, ex);
        }
    }

    @Override
    public byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password) {
        HttpClient client = buildHttpClient();
        try {
            String encodedSchema = schema != null ? URLEncoder.encode(schema, StandardCharsets.UTF_8) : "";
            String url = baseUrl + "/api/schema/export/" + encodedSchema + "/sqlite";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(username, password))
                    .header("Accept", "application/vnd.sqlite3")
                    .timeout(Duration.ofMinutes(10))  // Backups can take a while
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                log.infof("Successfully exported SQLite backup for schema %s (%d bytes)", schema, response.body().length);
                return response.body();
            }
            log.warnf("SVWS exportSqliteBackup failed for schema %s with status %d", schema, response.statusCode());
            throw new SvwsClientException("SVWS exportSqliteBackup failed with status " + response.statusCode(), response.statusCode(), null);
        } catch (SvwsClientException ex) {
            throw ex;
        } catch (Exception ex) {
            log.errorf(ex, "SVWS exportSqliteBackup failed for schema %s", schema);
            throw new SvwsClientException("SVWS exportSqliteBackup failed", -1, ex);
        }
    }

    /**
     * Creates a new SvwsSchuleInfo with the schema field populated.
     */
    private SvwsSchuleInfo withSchema(SvwsSchuleInfo info, String schema) {
        return new SvwsSchuleInfo(
                info.schulnummer(),
                info.name(),
                schema,
                info.plz(),
                info.ort(),
                info.strasse(),
                info.hausnummer(),
                info.hausnummerZusatz(),
                info.telefon(),
                info.fax(),
                info.email(),
                info.homepage(),
                info.schulform(),
                info.schulart(),
                info.schulgliederung(),
                info.schulleiter(),
                info.schulleiterTelefon(),
                info.schulleiterEmail(),
                info.kreis(),
                info.schulamt(),
                info.staat(),
                info.schulnummer2(),
                info.schulstatus(),
                info.kapitel(),
                info.satzungsgebendeKommune()
        );
    }

    private HttpClient buildHttpClient() {
        try {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL);
            
            if (trustAll) {
                SSLContext sslContext = trustAllSslContext();
                builder.sslContext(sslContext);
                
                // Also set system properties to disable hostname verification
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                System.setProperty("jdk.tls.client.disableHostnameVerification", "true");
            }
            
            return builder.build();
        } catch (Exception ex) {
            log.errorf(ex, "Failed to build HTTP client");
            throw new IllegalStateException("Failed to build HTTP client: " + ex.getMessage(), ex);
        }
    }

    private String basicAuth(String username, String password) {
        String token = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private SSLContext trustAllSslContext() {
        try {
            TrustManager[] trustAllManagers = new TrustManager[] { new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            } };

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllManagers, new java.security.SecureRandom());
            return context;
        } catch (Exception ex) {
            log.errorf(ex, "Failed to configure trust-all SSL");
            throw new IllegalStateException("Failed to configure trust-all SSL", ex);
        }
    }
}