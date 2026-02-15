package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.NrwSchulkatalogeintragService;
import de.schultraeger.application.port.out.NrwClientException;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Scheduler service for periodic NRW school catalog refresh.
 */
@ApplicationScoped
public class NrwSchulkatalogeintragScheduler {
    private static final Logger LOG = Logger.getLogger(NrwSchulkatalogeintragScheduler.class);

    private final NrwSchulkatalogeintragService service;

    public NrwSchulkatalogeintragScheduler(NrwSchulkatalogeintragService service) {
        this.service = service;
    }

    /**
     * Refresh NRW school catalog daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshCatalog() {
        LOG.info("Starting scheduled NRW school catalog refresh...");
        try {
            service.refreshCatalog();
            LOG.info("Scheduled NRW school catalog refresh completed successfully");
        } catch (NrwClientException e) {
            LOG.error("Scheduled NRW school catalog refresh failed", e);
        }
    }
}
