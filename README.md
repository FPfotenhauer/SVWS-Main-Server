# SVWS-Main-Server

Schultraeger-Server (MVP) für die Verwaltung mehrerer SVWS-Instanzen.

## Überblick

Dies ist ein Multi-Tenant-Server zur Verwaltung von SVWS-Instanzen mit:
- **Backend**: Quarkus 3.13.3 (Java 21)
- **Frontend**: Vue 3 + Vite
- **Datenbank**: PostgreSQL 15
- **Authentifizierung**: OIDC oder Passwort-basiert
- **Infrastruktur**: Docker Compose mit Nginx Reverse Proxy

## Voraussetzungen

- Docker und Docker Compose
- Git

## Schnelleinstieg (Docker Compose)

### 1. Repository klonen

```bash
git clone <repository-url>
cd SVWS-Main-Server
```

### 2. Umgebungsvariablen konfigurieren

```bash
cp .env.example .env
```

Bearbeite `.env` und stelle sicher, dass die folgenden Variablen gesetzt sind:

```env
# Datenbankverbindung
DB_NAME=schultraeger
DB_USER=schultraeger
DB_PASSWORD=schultraeger

# Authentifizierung
OIDC_ENABLED=false          # Auf true setzen für OIDC, false für Passwort-Auth
OIDC_URL=                   # Falls OIDC verwendet wird
OIDC_CLIENT_ID=             # Falls OIDC verwendet wird
OIDC_CLIENT_SECRET=         # Falls OIDC verwendet wird

# JWT für Passwort-Authentifizierung
JWT_SIGN_KEY=ovlIqW7FpNGuHcGKCF8jZ75GXnjhWFYmPO9DyTL4iFo=
```

### 3. Container starten

```bash
docker compose up --build
```

Die Anwendung ist dann unter **http://localhost:8081** erreichbar.

## Authentifizierung

### Passwort-basiert (Standard)

**Standardbenutzer**: `admin` / `admin`

**Login-Endpoint**:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

**Passwort ändern**:
```bash
curl -X POST http://localhost:8081/api/auth/change-password \
  -H "Authorization: Bearer <JWT-TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"admin","newPassword":"my-new-password"}'
```

### OIDC (OpenID Connect)

Um OIDC zu verwenden, setze in `.env`:
- `OIDC_ENABLED=true`
- `OIDC_URL=<your-oidc-provider-url>`
- `OIDC_CLIENT_ID=<client-id>`
- `OIDC_CLIENT_SECRET=<client-secret>`

## Entwicklung

### Backend

```bash
# Backend entwickelt automatisch mit Docker Compose
# Code-Änderungen werden bei `docker compose up --build` neu kompiliert

# Manuelle Kompilierung
cd backend
mvn clean compile
```

### Frontend

```bash
# Frontend im Dev-Modus (live reload)
cd frontend
npm install
npm run dev
```

### Logs

```bash
# Alle Container
docker compose logs -f

# Nur Backend
docker compose logs -f backend

# Nur Frontend
docker compose logs -f frontend
```

## Datenbankmigrationen

Migrationen werden automatisch beim Start mit Flyway ausgeführt.

**Migration Files**: `backend/src/main/resources/db/migration/`

Neue Migration erstellen:
```bash
# V<number>__<description>.sql
# Beispiel: V6__add_users_roles.sql
```

## API-Endpoints

| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|-------------|------|
| POST | `/api/auth/login` | Benutzer login | Nein |
| POST | `/api/auth/register` | Benutzer registrieren | Nein |
| POST | `/api/auth/change-password` | Passwort ändern | Ja |

## Hinweise

- Der Reverse Proxy ist im MVP noch HTTP-only konfiguriert. TLS-Termination kann später im Proxy ergänzt werden.
- Für produktive Nutzung müssen OIDC-Variablen gesetzt werden.
- Das `.env` File wird nicht versioniert (siehe `.gitignore`) - jeder Developer braucht eine lokale Kopie.
- `.vscode/` wird ebenfalls ignoriert - IDE-Konfigurationen sind persönlich.

## TLS lokal (optional)

Lokale TLS-Termination mit self-signed Zertifikat und lokalem Keystore:

```bash
sh docker/scripts/generate-local-keystore.sh
```

Beispiel-Passwort für das lokale Keystore: `svwskeystore`.

Start mit TLS-Override:

```bash
docker compose -f docker-compose.yml -f docker-compose.tls.yml up --build
```

Aufruf: **https://localhost:9443**

## Troubleshooting

### "Connection refused" bei localhost:8081

- Stelle sicher, dass alle Container laufen: `docker compose ps`
- Logs prüfen: `docker compose logs`
- Container rekompilieren: `docker compose up --build`

### Datenbankverbindungsfehler

- Stelle sicher, dass `DB_*` Variablen in `.env` gesetzt sind
- Überprüfe, dass die Datenbank läuft: `docker compose logs database`
- Bei Bedarf: `docker compose down -v` (löscht Daten!) und neu starten

### JWT Token abgelehnt

- Stelle sicher, dass der Token nicht abgelaufen ist
- Überprüfe, dass `JWT_SIGN_KEY` korrekt in `.env` gesetzt ist
- Bei Mismatch neu bauen: `docker compose up --build`

## Weitere Ressourcen

- [Architektur-Dokumentation](./architecture/) - Architektur-Entscheidungen und Systemübersicht
- [Detaillierte Architektur](./architecture/ARCHITECTURE.md) - High-Level Systemarchitektur
- [ADRs (Architecture Decision Records)](./architecture/#architecture-decision-records-adrs) - Detaillierte Entscheidungsdokumentation
- [System-Übersicht (Arc42)](./architecture/Arch42-Doc.md) - Umfassende Systembeschreibung
