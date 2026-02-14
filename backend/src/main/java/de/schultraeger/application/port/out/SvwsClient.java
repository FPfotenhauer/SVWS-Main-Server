package de.schultraeger.application.port.out;

import de.schultraeger.application.dto.SvwsSchuleInfo;

/**
 * Port for SVWS privileged API access.
 */
public interface SvwsClient {
    boolean isPrivileged(String baseUrl, String username, String password);

    SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password);
}
