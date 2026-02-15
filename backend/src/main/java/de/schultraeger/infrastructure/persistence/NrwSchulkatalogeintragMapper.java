package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.NrwSchulkatalogeintrag;

/**
 * Mapper between NrwSchulkatalogeintragEntity and NrwSchulkatalogeintrag domain model.
 */
public class NrwSchulkatalogeintragMapper {

    public static NrwSchulkatalogeintrag toDomain(NrwSchulkatalogeintragEntity entity) {
        return new NrwSchulkatalogeintrag(
                entity.id,
                entity.schulnummer,
                entity.schulname,
                entity.schultyp,
                entity.strasse,
                entity.plz,
                entity.ort,
                entity.kreis,
                entity.schulamt,
                entity.telefon,
                entity.fax,
                entity.email,
                entity.homepage,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public static NrwSchulkatalogeintragEntity toEntity(NrwSchulkatalogeintrag domain) {
        NrwSchulkatalogeintragEntity entity = new NrwSchulkatalogeintragEntity();
        entity.id = domain.id();
        entity.schulnummer = domain.schulnummer();
        entity.schulname = domain.schulname();
        entity.schultyp = domain.schultyp();
        entity.strasse = domain.strasse();
        entity.plz = domain.plz();
        entity.ort = domain.ort();
        entity.kreis = domain.kreis();
        entity.schulamt = domain.schulamt();
        entity.telefon = domain.telefon();
        entity.fax = domain.fax();
        entity.email = domain.email();
        entity.homepage = domain.homepage();
        entity.createdAt = domain.createdAt();
        entity.updatedAt = domain.updatedAt();
        return entity;
    }
}
