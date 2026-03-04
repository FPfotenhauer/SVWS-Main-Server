package de.schultraeger.application.port.out;

import de.schultraeger.domain.Webnotenmanager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting and retrieving Webnotenmanager configurations.
 */
public interface WebnotenmanagerRepository {
    List<Webnotenmanager> findAllConfigs();

    Optional<Webnotenmanager> findConfigById(UUID id);

    Optional<Webnotenmanager> findConfigBySchuleId(UUID schuleId);

    Webnotenmanager saveConfig(Webnotenmanager webnotenmanager);

    Webnotenmanager updateConfig(Webnotenmanager webnotenmanager);

    void deleteConfigById(UUID id);

    void deleteConfigBySchuleId(UUID schuleId);
}

