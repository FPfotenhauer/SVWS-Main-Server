# SVWS-Main-Server

## Dies ist ein Multi-Tenant-Server zur Verwaltung von mehreren SVWS-Instanzen.



<p align="center">
    <a href="https://github.com/pfotenhauer/SVWS-Main-Server/issues"><img alt="issues" src="https://img.shields.io/github/issues/pfotenhauer/SVWS-Main-Server"/></a>
	<a href="https://github.com/pfotenhauer/SVWS-Main-Server"><img alt="last-commit" src="https://img.shields.io/github/last-commit/pfotenhauer/SVWS-Main-Server"/></a>
	<a href="https://github.com/pfotenhauer/SVWS-Main-Server"><img alt="license" src="https://img.shields.io/github/license/pfotenhauer/SVWS-Main-Server"/></a>
	<img alt="java" src="https://img.shields.io/badge/Java-21-blue?logo=java"/>
</p>

---

## Kurzüberblick

Ein leichtgewichtiger Verwaltungsserver zur Registrierung, Überwachung und Verwaltung mehrerer SVWS‑Server. Optimiert für lokale Entwicklung mit Docker Compose und modularer Trennung von Backend (Quarkus) und Frontend (Vue + Vite).

**Technologien:** Java (Quarkus), Vue 3, PostgreSQL, Docker Compose, Nginx


## Inhaltsverzeichnis

- Features
- Quickstart
- Entwicklung
- Architektur & Doku
- Contributing
- Lizenz

## Highlights / Features

- Verwaltung mehrerer SVWS‑Instanzen (Name, URL, Port, Zugangsdaten)
- Verbindungstests & Statusanzeige
- Verschlüsselte Speicherung von Passwörtern (AES‑GCM)
- Unterstützung für TLS / selbstsignierte Zertifikate
- Multi‑container Entwicklung via Docker Compose

## Quickstart (lokal)

1. Repository klonen

```bash
git clone https://github.com/your-org/SVWS-Main-Server.git
cd SVWS-Main-Server
```

2. Environment kopieren und anpassen

```bash
cp .env.example .env
# Werte für DB_*, JWT_SIGN_KEY, SVWS_PASSWORD_KEY usw. anpassen
```

3. Dienste starten

```bash
docker compose up --build -d
```

Die Web‑App ist standardmäßig erreichbar unter: http://localhost:8081

Logs ansehen (Backend):

```bash
docker compose logs -f backend
```

## Entwicklung

- Backend

	```bash
	# im backend/ Verzeichnis
	./mvnw quarkus:dev
	```

- Frontend

	```bash
	# im frontend/ Verzeichnis
	npm install
	npm run dev
	```

## Architektur & Doku

Architekturentscheidungen, ADRs und weitere Doku befinden sich im `architecture`‑Ordner und in `docs`:

- [architecture/ARCHITECTURE.md](architecture/ARCHITECTURE.md)
- [docs/README.md](docs/README.md)

## Contributing

Contributions, Issues und Vorschläge willkommen. Kurzer Ablauf:

1. Fork
2. Branch erstellen
3. PR mit Beschreibung öffnen

Für komponentenspezifische Anweisungen siehe die Ordner:

- [backend](backend)
- [frontend](frontend)

## Lizenz

Siehe `LICENSE` im Projektstamm.
