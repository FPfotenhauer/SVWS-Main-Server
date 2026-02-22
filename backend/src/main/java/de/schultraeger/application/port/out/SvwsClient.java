package de.schultraeger.application.port.out;

import de.schultraeger.application.dto.SchuleStammdaten;
import de.schultraeger.application.dto.SchuleStatistikenRaw;
import de.schultraeger.application.dto.OrtKatalogEintrag;
import de.schultraeger.application.dto.SchuelerAuswahl;
import de.schultraeger.application.dto.SchuelerStammdaten;
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

    default List<SchuelerAuswahl> getSchuelerAuswahlliste(String baseUrl, String schema, String username, String password, Integer abschnitt, List<Integer> status) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default List<SchuelerStammdaten> getSchuelerStammdaten(String baseUrl, String schema, String username, String password) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default List<SchuelerStammdaten> getSchuelerStammdatenByIds(String baseUrl, String schema, String username, String password, List<Long> schuelerIds) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default List<OrtKatalogEintrag> getOrte(String baseUrl, String schema, String username, String password) {
        throw new UnsupportedOperationException("Not implemented");
    }

    List<SvwsSchuleInfo> listSchools(String baseUrl, String username, String password);

    /**
     * Destroys a schema on the SVWS server using the privileged API.
     * @param baseUrl SVWS server base URL
     * @param schema Schema name to destroy
     * @param username Privileged username
     * @param password Privileged password
     * @throws SvwsClientException if the operation fails
     */
    void destroySchema(String baseUrl, String schema, String username, String password);

    /**
     * Exports a schema as SQLite backup from the SVWS server.
     * @param baseUrl SVWS server base URL
     * @param schema Schema name to export
     * @param username Privileged username
     * @param password Privileged password
     * @return byte array containing the SQLite database file
     * @throws SvwsClientException if the operation fails
     */
    byte[] exportSqliteBackup(String baseUrl, String schema, String username, String password);
}
