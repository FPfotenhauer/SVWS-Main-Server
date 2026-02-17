package de.schultraeger.application.port.out;

import de.schultraeger.application.dto.SchuleStammdaten;
import de.schultraeger.application.dto.SchuleStatistikenRaw;
import de.schultraeger.application.dto.SvwsSchuleInfo;
import java.util.List;

/**
 * Port for SVWS privileged API access.
 */
public interface SvwsClient {
    boolean isPrivileged(String baseUrl, String username, String password);

    SvwsSchuleInfo getSchuleInfo(String baseUrl, String schema, String username, String password);

    SchuleStammdaten getSchuleStammdaten(String baseUrl, String schema, String username, String password);

    SchuleStatistikenRaw getSchuleStatistiken(String baseUrl, String schema, String username, String password);

    List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password);
}
