# SVWS-Main-Server

Schultraeger-Server (MVP) für die Verwaltung mehrerer SVWS-Instanzen.

## Überblick

Dies ist ein Multi-Tenant-Server zur Verwaltung von SVWS-Instanzen mit:
- **Backend**: Quarkus 3.13.3 (Java 21)
- **Frontend**: Vue 3 + Vite
- **Datenbank**: PostgreSQL 15
- **Authentifizierung**: OIDC oder Passwort-basiert
- **Infrastruktur**: Docker Compose mit Nginx Reverse Proxy

## Features

### SVWS Server Verwaltung
- **Server hinzufügen**: SVWS-Server mit Name, Base URL, Port und Zugangsdaten registrieren
- **Verbindungstests**: Automatisches Testen der Serververbindung mit Statusanzeige
- **Verschlüsselte Passwörter**: Passwörter werden verschlüsselt in der Datenbank gespeichert (AES-GCM)
- **SSL-Unterstützung**: Automatische Verbindung zu SVWS-Servern mit selbstsignierten Zertifikaten
- **Status-Tracking**: Visualisierung des Verbindungsstatus (UNTESTED, CONNECTED, ERROR)

### Schul-Verwaltung
- **Schulliste abrufen**: Automatischer Abruf der Schulliste von registrierten SVWS-Servern
- **Sortierung**: Sortierung nach Schulnummer oder Name (aufsteigend/absteigend)
- **Suchfunktion**: Live-Suche nach Schulnummer oder Name

### Benutzeroberfläche
- **Sortierbare Listen**: Server und Schulen können nach verschiedenen Kriterien sortiert werden
- **Suchfunktionen**: Filterung von Servern (Name, Base URL) und Schulen (Schulnummer, Name)
- **Kompaktes Design**: Optimierte Nutzung des Bildschirmplatzes
- **Icon-Buttons**: Intuitive Bedienung mit Tooltips

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
JWT_SIGN_KEY=<your-jwt-secret-key>  # Generiere einen zufälligen Base64-String

# SVWS Server Verwaltung
SVWS_PASSWORD_KEY=<your-32-char-key>  # 32-Zeichen Schlüssel für AES-GCM Verschlüsselung
SVWS_TRUST_ALL=true          # Für Entwicklung: Akzeptiert selbstsignierte SSL-Zertifikate
```

**Wichtig**: Generiere eigene sichere Schlüssel für Produktion! Verwende niemals die Beispielwerte.

### 3. Container starten

```bash
docker compose up --build
```

Die Anwendung ist dann unter **http://localhost:8081** erreichbar.

### 4. SVWS-Server verwalten

Nach dem Login kannst du SVWS-Server hinzufügen und verwalten:

1. **Server hinzufügen**: 
   - Klicke auf "Neuer SVWS-Server"
   - Gib Name, Base URL, Port und Zugangsdaten ein
   - Die Verbindung wird automatisch getestet

2. **Schulen abrufen**:
   - Klicke auf das Schulgebäude-Symbol neben einem Server
   - Die Schulliste wird automatisch vom SVWS-Server abgerufen

3. **Listen durchsuchen**:
   - Verwende die Suchfelder, um Server oder Schulen zu filtern
   - Klicke auf Spaltenüberschriften zum Sortieren

4. **Verbindung testen**:
   - Klicke auf das Uhr-Symbol, um die Verbindung zu testen
   - Status wird automatisch aktualisiert (CONNECTED, ERROR, etc.)

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

### Authentifizierung

| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|-------------|------|
| POST | `/api/auth/login` | Benutzer login | Nein |
| POST | `/api/auth/register` | Benutzer registrieren | Nein |
| POST | `/api/auth/change-password` | Passwort ändern | Ja |

### SVWS Server Verwaltung

| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|-------------|------|
| GET | `/api/svws-servers` | Alle SVWS-Server abrufen | Ja |
| POST | `/api/svws-servers` | Neuen SVWS-Server anlegen | Ja |
| GET | `/api/svws-servers/{id}` | SVWS-Server Details abrufen | Ja |
| PUT | `/api/svws-servers/{id}` | SVWS-Server aktualisieren | Ja |
| DELETE | `/api/svws-servers/{id}` | SVWS-Server löschen | Ja |
| POST | `/api/svws-servers/{id}/test-connection` | Verbindung testen und Status aktualisieren | Ja |
| GET | `/api/svws-servers/{id}/schools` | Schulliste vom SVWS-Server abrufen | Ja |

**Beispiel: SVWS-Server anlegen**
```bash
curl -X POST http://localhost:8081/api/svws-servers \
  -H "Authorization: Bearer <JWT-TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SVWS Produktiv",
    "baseUrl": "https://svws.example.com:8443",
    "username": "your-username",
    "password": "your-password"
  }'
```

**Beispiel: Schulliste abrufen**
```bash
curl -X GET http://localhost:8081/api/svws-servers/1/schools \
  -H "Authorization: Bearer <JWT-TOKEN>"
```

## Hinweise

- Der Reverse Proxy ist im MVP noch HTTP-only konfiguriert. TLS-Termination kann später im Proxy ergänzt werden.
- Für produktive Nutzung müssen OIDC-Variablen gesetzt werden.
- Das `.env` File wird nicht versioniert (siehe `.gitignore`) - jeder Developer braucht eine lokale Kopie.
- `.vscode/` wird ebenfalls ignoriert - IDE-Konfigurationen sind persönlich.

### Sicherheitshinweise

- **Passwort-Verschlüsselung**: SVWS-Server Passwörter werden mit AES-GCM verschlüsselt
- **SSL-Zertifikate**: `SVWS_TRUST_ALL=true` sollte nur in Entwicklung verwendet werden
- **Produktivbetrieb**: In Produktion sollte `SVWS_TRUST_ALL=false` gesetzt und echte SSL-Zertifikate verwendet werden
- **Umgebungsvariablen**: Speichere niemals sensible Daten im Source Code - verwende `.env`
- **JWT-Schlüssel**: Verwende einen starken, zufälligen JWT-Schlüssel für Produktion

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

### SVWS-Server Verbindungsfehler

- **SSL-Zertifikat nicht vertrauenswürdig**: Setze `SVWS_TRUST_ALL=true` in `.env` für Entwicklung
- **Connection refused**: Überprüfe, dass der SVWS-Server erreichbar ist und der Port korrekt ist
- **Authentication failed**: Überprüfe Benutzername und Passwort des SVWS-Servers
- **Docker Networking**: Bei lokalen SVWS-Servern verwende die Host-IP (z.B. 192.168.2.16) statt localhost

### Passwort-Verschlüsselung Fehler

- Stelle sicher, dass `SVWS_PASSWORD_KEY` in `.env` gesetzt ist (32 Zeichen)
- Bei Änderung des Schlüssels können alte Passwörter nicht mehr entschlüsselt werden
- Backend neu bauen nach Umgebungsänderungen: `docker compose up --build`

## Weitere Ressourcen

- [Architektur-Dokumentation](./architecture/) - Architektur-Entscheidungen und Systemübersicht
- [Detaillierte Architektur](./architecture/ARCHITECTURE.md) - High-Level Systemarchitektur
- [ADRs (Architecture Decision Records)](./architecture/#architecture-decision-records-adrs) - Detaillierte Entscheidungsdokumentation
- [System-Übersicht (Arc42)](./architecture/Arch42-Doc.md) - Umfassende Systembeschreibung
