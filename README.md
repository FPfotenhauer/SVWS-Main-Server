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
# SVWS-Main-Server

Kurz: Multi-tenant Verwaltungsserver (MVP) zur zentralen Verwaltung mehrerer SVWS-Instanzen.

Why it matters: Start, überwache und verwalte mehrere SVWS-Server zentral über eine Web-Oberfläche.

Requirements: Docker, Docker Compose, Git

Quickstart

1. Clone:

```bash
git clone <repository-url>
cd SVWS-Main-Server
```

2. Copy env and edit required values:

```bash
cp .env.example .env
# edit .env (DB_*, JWT_SIGN_KEY, SVWS_PASSWORD_KEY, etc.)
```

3. Start with Docker Compose:

```bash
docker compose up --build
```

App: http://localhost:8081

Where to find more

- Details, configuration examples, API docs, development and troubleshooting moved to: [docs/README.md](docs/README.md)

Contributing

- See [backend](backend) and [frontend](frontend) folders for component-specific instructions.

License: See LICENSE
