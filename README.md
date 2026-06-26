# Skill Manager – Spring Boot 3.x + React

Webanwendung zur Verwaltung von Agent-Skills. Skills werden als Verzeichnisse
mit Markdown-Dateien (inkl. `skill.md`) auf dem Server abgelegt. Über die UI
lassen sich Skills auflisten, anzeigen, als ZIP herunterladen, hochladen und
archivieren.

## Struktur

```
JavaTest/
├── backend/   Spring Boot 3.5.x (Java 17, REST-API)
└── frontend/  React 18 + Vite (UI mit Routing)
```

## Voraussetzungen

- Java 17 (oder neuer)
- Maven
- Node.js 18+ (für das Frontend)

## Konfiguration

Das Backend liest seine Pfade aus `backend/src/main/resources/application.properties`:

```properties
server.port=8081
skill.path=C:\\tmp\\SkillDb          # Ablageort der Skills
skill.archive-path=C:\\tmp\\SkillDb-Archive  # Archiv für archivierte Skills
```

Jeder Skill ist ein Unterverzeichnis unter `skill.path` und muss eine
gültige `skill.md`-Datei mit Header enthalten.

## Backend starten

```bash
cd backend
mvn spring-boot:run
```

Die API läuft auf http://localhost:8081
OpenAPI/Swagger-UI: http://localhost:8081/swagger-ui.html

### Endpunkte

| Methode | URL                            | Beschreibung                                  |
|---------|--------------------------------|-----------------------------------------------|
| GET     | `/api/skill`                   | Alle Skills auflisten (Name + Header)         |
| GET     | `/api/skill/{skillName}`       | Detail eines Skills inkl. Dateiinhalte        |
| GET     | `/api/skill/{skillName}/download` | Skill als ZIP herunterladen                |
| DELETE  | `/api/skill/{skillName}`       | Skill archivieren (ZIP ins Archiv, Original wird gelöscht) |
| POST    | `/api/skill/upload`            | Skill als ZIP hochladen (muss `skill.md` enthalten) |

## Frontend starten

In einem zweiten Terminal:

```bash
cd frontend
npm install
npm run dev
```

Die UI läuft auf http://localhost:5173

Vite leitet alle `/api/*`-Anfragen an das Backend auf Port 8081 weiter
(siehe `frontend/vite.config.js`). Zusätzlich ist `@CrossOrigin` im Controller
auf `http://localhost:5173` gesetzt.

## Nutzung

1. Beide Server starten (Backend + Frontend).
2. Browser öffnen: http://localhost:5173
3. **Skill-Liste:** Übersicht aller Skills als Karten mit Name und Beschreibung.
4. **Skill-Detail:** Klick auf eine Karte zeigt Dateibaum und Dateiinhalte
   (Markdown wird gerendert).
5. **Upload:** Über den Button *Upload* eine ZIP-Datei mit einem Skill hochladen.
   Die ZIP muss eine gültige `skill.md` enthalten.
6. **Download:** In der Detailansicht als ZIP herunterladen.
7. **Archivieren:** In der Detailansicht den Skill ins Archiv verschieben
   (Original wird gelöscht).
