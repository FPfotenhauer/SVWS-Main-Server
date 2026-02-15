package de.schultraeger.infrastructure.persistence;

import de.schultraeger.application.NrwSchulkatalogeintragService;
import de.schultraeger.application.port.out.NrwClientException;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

/**
 * Initializer to load NRW school catalog on application startup.
 */
@ApplicationScoped
public class NrwSchulkatalogeintragInitializer {
    private static final Logger LOG = Logger.getLogger(NrwSchulkatalogeintragInitializer.class);

    private final NrwSchulkatalogeintragService service;
    private final NrwSchulkatalogeintragRepositoryPanache repository;

    public NrwSchulkatalogeintragInitializer(NrwSchulkatalogeintragService service,
                                            NrwSchulkatalogeintragRepositoryPanache repository) {
        this.service = service;
        this.repository = repository;
    }

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Checking NRW school catalog...");
        try {
            long count = repository.getTotalCount();
            if (count == 0) {
                LOG.info("NRW school catalog is empty. Loading initial data...");
                service.refreshCatalog();
                LOG.infov("Successfully loaded {0} schools", repository.getTotalCount());
            } else {
                LOG.infov("NRW school catalog already contains {0} schools", count);
            }
        } catch (NrwClientException e) {
            LOG.warn("Failed to load NRW school catalog on startup. It will be loaded on first scheduled refresh.", e);
        } catch (Exception e) {
            LOG.error("Error during NRW school catalog initialization", e);
        }
    }
}
