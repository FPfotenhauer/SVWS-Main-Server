---

# 1️⃣ Systemkontext (arc42 Abschnitt 3 – Kontextabgrenzung)

## Ziel

Zeigt, wie sich der Schulträger-Server in die Umgebung einbettet.

```plantuml
@startuml
actor Admin
actor Support

rectangle "Schulträger-Server" {
  component "Backend (Quarkus)"
  component "Frontend (Vue.js)"
}

database "PostgreSQL" as DB

rectangle "SVWS-Server\n(je Schule)" {
  component "SVWS API\n(Privileged)"
}

Admin --> "Frontend (Vue.js)"
Support --> "Frontend (Vue.js)"

"Frontend (Vue.js)" --> "Backend (Quarkus)"
"Backend (Quarkus)" --> DB
"Backend (Quarkus)" --> "SVWS API\n(Privileged)"

@enduml
```

---

# 2️⃣ Bausteinsicht – Container-Level (arc42 Abschnitt 5)

## Container-Übersicht

```plantuml
@startuml
package "Schulträger-Server" {

  package "Frontend" {
    [Vue App]
    [Pinia Store]
    [API Client (Axios)]
  }

  package "Backend (Quarkus)" {

    package "API Layer" {
      [SchuleResource]
      [AuthResource]
    }

    package "Application Layer" {
      [SchuleService]
      [SyncService]
    }

    package "Domain Layer" {
      [Schule]
      [Schultraeger]
      [Status Enum]
    }

    package "Infrastructure Layer" {
      [SchuleRepository]
      [SvwsClient]
      [SvwsClientRest]
      [JWT Provider]
    }
  }

  database "PostgreSQL"
}

[Vue App] --> [API Client (Axios)]
[API Client (Axios)] --> [SchuleResource]
[SchuleResource] --> [SchuleService]
[SchuleService] --> [SchuleRepository]
[SchuleService] --> [SyncService]
[SyncService] --> [SvwsClient]
[SvwsClientRest] --> "SVWS API"
[SchuleRepository] --> "PostgreSQL"

@enduml
```

---

# 3️⃣ Verteilungssicht (arc42 Abschnitt 7)

Zeigt physische Knoten.

```plantuml
@startuml
node "Admin Browser" {
  component "Vue SPA"
}

node "Schulträger Server" {
  component "Quarkus App"
}

node "Database Server" {
  database "PostgreSQL"
}

node "Schule A" {
  component "SVWS Server"
}

node "Schule B" {
  component "SVWS Server"
}

"Vue SPA" --> "Quarkus App"
"Quarkus App" --> "PostgreSQL"
"Quarkus App" --> "SVWS Server"

@enduml
```

Später erweiterbar um:

* Keycloak Server
* Monitoring Stack
* Reverse Proxy
* Scheduler Node

---

# 4️⃣ Laufzeitsicht – Synchronisation (arc42 Abschnitt 6)

Beispiel: Sync einer Schule

```plantuml
@startuml
actor Admin

Admin -> Frontend : Klick "Synchronisieren"
Frontend -> Backend : POST /api/schulen/{id}/sync
Backend -> SvwsClient : isPrivileged()
SvwsClient -> SVWS : GET /api/privileged/user/isprivileged
SVWS --> SvwsClient : true

Backend -> SvwsClient : getSchuleInfo()
SvwsClient -> SVWS : GET /api/schema/liste/info/{schema}/schule
SVWS --> SvwsClient : SchuleInfo

Backend -> DB : Update status + lastSync
Backend --> Frontend : 200 OK

@enduml
```

---

# 5️⃣ Vorbereitung für arc42 Dokumentation

Empfohlene Struktur:

```text
1. Einführung und Ziele
2. Randbedingungen
3. Kontextabgrenzung
4. Lösungsstrategie
5. Bausteinsicht
6. Laufzeitsicht
7. Verteilungssicht
8. Querschnittliche Konzepte
9. Architekturentscheidungen (ADR)
10. Qualitätsanforderungen
11. Risiken
12. Glossar
```

---

# 6️⃣ Wichtige Architekturprinzipien für arc42 Abschnitt 4

Formuliere dort:

* Clean Architecture
* Hexagonale Architektur
* Ports & Adapters
* Mandantenfähigkeit vorbereitend
* API-first Integration mit SVWS
* RESTful Backend
* Stateless Backend
* Security-by-Design

---

# 7️⃣ Qualitätsziele (für arc42 Abschnitt 10)

Priorität:

1. Wartbarkeit
2. Erweiterbarkeit
3. Sicherheit
4. Ausfallsicherheit bei SVWS-Problemen
5. Nachvollziehbarkeit von Sync-Prozessen

---

# 🔥 Optional – C4 Modell

Wenn ihr professionell dokumentieren wollt, empfehle ich:

* C4 Level 1 = Kontext
* C4 Level 2 = Container
* C4 Level 3 = Komponenten
* C4 Level 4 = Code

arc42 + C4 kombiniert ist Best Practice.

---

