package de.schultraeger.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the nrw_schulkatalog table.
 */
@Entity
@Table(name = "nrw_schulkatalog")
public class NrwSchulkatalogeintragEntity {
    @Id
    public UUID id;

    @Column(name = "schulnummer", nullable = false, unique = true, length = 50)
    public String schulnummer;

    @Column(name = "schulname", nullable = false, length = 255)
    public String schulname;

    @Column(name = "schultyp", length = 100)
    public String schultyp;

    @Column(name = "strasse", length = 255)
    public String strasse;

    @Column(name = "plz", length = 10)
    public String plz;

    @Column(name = "ort", length = 255)
    public String ort;

    @Column(name = "kreis", length = 100)
    public String kreis;

    @Column(name = "schulamt", length = 255)
    public String schulamt;

    @Column(name = "telefon", length = 255)
    public String telefon;

    @Column(name = "fax", length = 255)
    public String fax;

    @Column(name = "email", length = 255)
    public String email;

    @Column(name = "homepage", length = 512)
    public String homepage;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
