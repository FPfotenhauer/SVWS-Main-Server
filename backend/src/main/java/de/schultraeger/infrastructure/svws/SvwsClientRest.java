package de.schultraeger.infrastructure.svws;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
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
    public java.util.List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password) {
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
                log.warnf("SVWS listSchools failed with status %d", response.statusCode());
                throw new SvwsClientException("SVWS listSchools failed with status " + response.statusCode(), response.statusCode(), null);
            }
            
            java.util.List<de.schultraeger.application.dto.SchemaListeEintrag> schemas = 
                    objectMapper.readValue(response.body(), new TypeReference<java.util.List<de.schultraeger.application.dto.SchemaListeEintrag>>() {});
            
            log.debugf("Found %d SVWS schemas, filtering and fetching school info", schemas.size());
            
            return schemas.stream()
                .filter(schema -> Boolean.TRUE.equals(schema.isSVWS()))
                .filter(schema -> !Boolean.TRUE.equals(schema.isTainted()))
                .filter(schema -> !Boolean.TRUE.equals(schema.isDeactivated()))
                .map(schema -> {
                    try {
                        return getSchuleInfo(baseUrl, schema.name(), username, password);
                    } catch (Exception ex) {
                        log.warnf("Failed to get info for schema %s: %s", schema.name(), ex.getMessage());
                        return null;
                    }
                })
                .filter(info -> info != null)
                .toList();
        } catch (Exception ex) {
            log.errorf(ex, "SVWS listSchools failed");
            throw new SvwsClientException("SVWS listSchools failed", -1, ex);
        }
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