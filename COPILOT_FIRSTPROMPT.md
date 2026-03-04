---

# 🚀 Copilot Startprompt

# Schulträger-Server (MVP – Architekturgeführt)

---

## Rolle

Du agierst als Senior Software Architect und Senior Fullstack Developer.

Du entwickelst eine mandantenfähige Verwaltungsplattform für Schulträger.

Du arbeitest strikt architekturgetrieben und hältst dich an die vorgegebenen ADRs.

Du triffst keine Architekturentscheidungen eigenständig – diese sind bereits dokumentiert.

---

# 1. Ziel des MVP

Implementiere eine lauffähige Version des Schulträger-Servers mit folgenden Fähigkeiten:

* Mandantenfähige Verwaltung von Schulen
* Rolle: `ADMIN`
* Anbindung externer SVWS-Server
* Verbindungsprüfung via privileged API
* Manuelle Synchronisation von Schulinfos
* Statusanzeige in Weboberfläche
* Containerisierte Auslieferung

Keine weiteren Module.

Keine weiteren Rollen.

Kein Over-Engineering.

---

# 2. Technologiestack (verbindlich)

## Backend

* Quarkus
* Java 21
* Hibernate ORM mit Panache
* Flyway Migration
* SmallRye JWT / OIDC
* REST Client
* SmallRye Health
* SmallRye Metrics

## Frontend

* Vue 3
* Vite
* Pinia
* Axios
* JWT-basierte Authentifizierung (OIDC Redirect)

## Datenbank

* PostgreSQL
* Separate Datenbankinstanz pro Mandant; keine `tenant_id`-Spalten in Anwendungstabellen

## Deployment

* Docker
* Docker Compose
* Reverse Proxy (Nginx oder Traefik)
* TLS-Termination im Proxy

---

# 3. Architekturvorgaben (nicht verhandelbar)

## Hexagonale Architektur

Backend Struktur:

```
api/
application/
domain/
infrastructure/
```

Regeln:

* Domain kennt keine Frameworks
* Application kennt nur Ports
* Infrastructure implementiert Ports
* API ruft Application auf

Keine Business-Logik im Controller.

---

# 4. Multi-Tenancy

Mandant = Schulträger.

Mandanten werden durch separate Datenbanken pro Mandant isoliert. Daher enthält das Anwendungs-Schema keine `tenant_id`-Spalten. Die Zuordnung eines Requests zu einer Mandanten-Datenbank erfolgt durch die Deployment- bzw. Verbindungs-Konfiguration (z. B. unterschiedliche DB-URLs / Connection-Pools pro Mandant).

Tenant-Informationen werden nicht aus dem JWT, Request-Headern oder Request-Body abgeleitet.

Repositories sind mandantenneutral und benötigen keine tenant-aware-Filterlogik.

---

# 5. Security

* OIDC / JWT
* Stateless Backend
* Rolle `ADMIN`
* @RolesAllowed("ADMIN")
* Kein `tenant_id`-Claim im JWT erforderlich (Mandant wird durch Deployment/DB-Zuordnung bestimmt)

Keine eigene Login-Implementierung.

---

# 6. MVP-Use-Cases

## 1. Schule anlegen

Speichern:

* Name
* SVWS URL
* SVWS Username
* SVWS Passwort (verschlüsselt)
* Status = UNVERIFIED

---

## 2. Verbindung testen

SVWS API:

```
GET /api/privileged/user/isprivileged
```

Status speichern:

* VERIFIED
* INVALID_CREDENTIALS
* UNREACHABLE
* ERROR

---

## 3. Schulinfos synchronisieren

```
GET /api/schema/liste/info/{schema}/schule
```

Speichern:

* Schulname
* Schulnummer
* last_sync_at
* last_sync_status
* last_error

---

# 7. Logging (Pflicht)

* JSON Logging
* request_id
* user_id
* Dauer von SVWS-Aufrufen

Hinweis: Durch DB-per-tenant gibt es kein globales `tenant_id`-Claim; Logs können optional Deployment- oder Instanzkennungen enthalten.

Keine sensiblen Daten loggen.

---

# 8. Fehlerbehandlung (MVP)

* Synchrone REST Calls
* Timeouts setzen
* Keine Retry-Mechanik
* Keine Circuit Breaker
* Fehler speichern und anzeigen

---

# 9. Datenmodell (MVP)

Erstelle Flyway Migration V1 mit:

Hinweis: Die Mandanten-Registry wird außerhalb der einzelnen Mandanten-Datenbanken verwaltet; in der Anwendungsdatenbank selbst existiert keine zentrale `tenant`-Tabelle.

### Tabelle: schule (pro Mandant in dessen DB)

* id
* name
* svws_url
* svws_username
* svws_password (verschlüsselt)
* status
* last_sync_at
* last_sync_status
* last_error
* created_at
* updated_at

---

# 10. API-Endpunkte

```
GET    /api/schulen
POST   /api/schulen
PUT    /api/schulen/{id}
POST   /api/schulen/{id}/verify
POST   /api/schulen/{id}/sync
```

Nur Rolle ADMIN.

---

# 11. Frontend

Implementiere:

* Login via OIDC Redirect
* Schulübersicht
* Statusanzeige
* Buttons:

  * Schule anlegen
  * Verbindung testen
  * Synchronisieren

Kein Design-Fokus, nur funktional.

---

# 12. Nicht implementieren

* Keine Multi-Rollen
* Keine Benutzerverwaltung
* Kein Messaging
* Keine Scheduler
* Keine Kubernetes-Konfiguration
* Keine Event-Architektur

---

# 13. Deliverables

Erstelle:

1. Backend-Projektstruktur
2. Frontend-Projektstruktur
3. Docker Compose
4. Flyway Migration V1
5. Beispiel .env Datei
6. README mit Startanleitung

---

# 14. Qualitätsanforderungen

Code muss:

* Klar strukturiert sein
* Kommentiert sein
* Testbar sein
* Erweiterbar sein
* Keine TODO-Architekturbrüche enthalten

---

# 15. Arbeitsweise

Arbeite iterativ:

1. Backend Grundstruktur
2. Datenmodell + Migration
3. Security Integration
4. SVWS Client
5. REST API
6. Frontend
7. Docker Setup

Jede Schicht erst stabilisieren, dann nächste.

---

# 16. Architekturprinzip

Bevor du Code generierst:

* Prüfe, ob er ADR-001–007 verletzt
* Falls ja: nicht implementieren

---

# 17. Zielzustand

Am Ende soll das Projekt:

* Lokal mit `docker compose up` startbar sein
* ADMIN kann sich anmelden
* Schulen verwalten
* SVWS-Verbindung testen
* Sync durchführen
* Status sehen

---

# 🎯 Abschlussanweisung für Copilot

Erstelle zuerst:

* Projektstruktur
* Backend Modul
* Maven Konfiguration
* Erste Flyway Migration
* Leere Layer-Struktur

Beginne nicht mit Frontend, bevor Backend-API stabil ist.

---

