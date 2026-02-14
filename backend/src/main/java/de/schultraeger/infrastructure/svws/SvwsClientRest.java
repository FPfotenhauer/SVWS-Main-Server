package de.schultraeger.infrastructure.svws;

import de.schultraeger.application.dto.SvwsSchuleInfo;
import de.schultraeger.application.port.out.SvwsClient;
import de.schultraeger.application.port.out.SvwsClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * REST client adapter for the SVWS privileged API.
 */
@ApplicationScoped
public class SvwsClientRest implements SvwsClient {
    private final boolean trustAll;

    @Inject
    public SvwsClientRest(@ConfigProperty(name = "svws.client.trust-all", defaultValue = "false") boolean trustAll) {
        this.trustAll = trustAll;
    }

    SvwsClientRest(boolean trustAll, boolean testMode) {
        this.trustAll = trustAll;
    }

    @Override
    public boolean isPrivileged(String baseUrl, String username, String password) {
        SvwsPrivilegedApi client = buildClient(baseUrl, username, password);
        try {
            return client.isPrivilegedUser();
        } catch (WebApplicationException ex) {
            int status = statusOf(ex.getResponse());
            if (status == 403) {
                return false;
            }
            throw new SvwsClientException("SVWS isPrivileged failed", status, ex);
        }
    }

    @Override
    public SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password) {
        SvwsPrivilegedApi client = buildClient(baseUrl, username, password);
        try {
            return client.getSchuleInfo(schema);
        } catch (WebApplicationException ex) {
            int status = statusOf(ex.getResponse());
            throw new SvwsClientException("SVWS getSchuleInfo failed", status, ex);
        }
    }

    private SvwsPrivilegedApi buildClient(String baseUrl, String username, String password) {
        RestClientBuilder builder = RestClientBuilder.newBuilder()
                .baseUri(URI.create(baseUrl))
                .register(new BasicAuthFilter(username, password));

        if (trustAll) {
            builder.sslContext(trustAllSslContext()).hostnameVerifier((host, session) -> true);
        }

        return builder.build(SvwsPrivilegedApi.class);
    }

    private int statusOf(Response response) {
        if (response == null) {
            return -1;
        }
        return response.getStatus();
    }

    private static class BasicAuthFilter implements jakarta.ws.rs.client.ClientRequestFilter {
        private final String headerValue;

        BasicAuthFilter(String username, String password) {
            String token = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
            this.headerValue = "Basic " + encoded;
        }

        @Override
        public void filter(jakarta.ws.rs.client.ClientRequestContext requestContext) {
            requestContext.getHeaders().putSingle("Authorization", headerValue);
        }
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
            throw new IllegalStateException("Failed to configure trust-all SSL", ex);
        }
    }
}
