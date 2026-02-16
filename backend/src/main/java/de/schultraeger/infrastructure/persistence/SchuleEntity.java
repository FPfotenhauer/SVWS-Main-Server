package de.schultraeger.infrastructure.persistence;

import de.schultraeger.domain.SchuleStatus;
import de.schultraeger.domain.SyncStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the schule table.
 */
@Entity
@Table(name = "schule")
public class SchuleEntity {
    @Id
    public UUID id;

    

    @Column(name = "name", nullable = false, length = 255)
    public String name;

    @Column(name = "schulnummer")
    public Long schulnummer;

    @Column(name = "svws_url", nullable = false, length = 512)
    public String svwsUrl;

    @Column(name = "svws_schema", nullable = false, length = 255)
    public String svwsSchema;

    @Column(name = "svws_username", nullable = false, length = 255)
    public String svwsUsername;

    @Column(name = "svws_password", nullable = false, length = 512)
    public String svwsPasswordEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    public SchuleStatus status;

    @Column(name = "last_sync_at")
    public Instant lastSyncAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_sync_status", length = 50)
    public SyncStatus lastSyncStatus;

    @Column(name = "last_error")
    public String lastError;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    // Address information
    @Column(name = "strasse", length = 255)
    public String strasse;

    @Column(name = "hausnummer", length = 255)
    public String hausnummer;

    @Column(name = "hausnummer_zusatz", length = 255)
    public String hausnummerZusatz;

    @Column(name = "plz", length = 10)
    public String plz;

    @Column(name = "ort", length = 255)
    public String ort;

    // Contact information
    @Column(name = "telefon", length = 255)
    public String telefon;

    @Column(name = "fax", length = 255)
    public String fax;

    @Column(name = "email", length = 255)
    public String email;

    @Column(name = "homepage", length = 512)
    public String homepage;

    // Administrative information
    @Column(name = "schulleiter", length = 255)
    public String schulleiter;

    @Column(name = "schulleiter_telefon", length = 255)
    public String schulleiterTelefon;

    @Column(name = "schulleiter_email", length = 255)
    public String schulleiterEmail;

    // Region information
    @Column(name = "kreis", length = 255)
    public String kreis;

    @Column(name = "schulamt", length = 255)
    public String schulamt;

    // Additional metadata
    @Column(name = "schulnummer2", length = 255)
    public String schulnummer2;

    @Column(name = "schulstatus", length = 255)
    public String schulstatus;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
