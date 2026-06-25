# Counter Demo – Spring Boot 3.x + React

Demo-Anwendung mit einem Button, der per API-Aufruf einen serverseitigen Counter hochzählt
und den Wert in React anzeigt.

## Struktur

```
JavaTest/
├── backend/   Spring Boot 3.x (Java 17, REST-API)
└── frontend/  React + Vite (UI mit Button)
```

## Voraussetzungen

- Java 17 (oder neuer)
- Maven
- Node.js 18+ (für das Frontend)

## Backend starten

```bash
cd backend
mvn spring-boot:run
```

Die API läuft auf http://localhost:8081

### Endpunkte

| Methode | URL                     | Beschreibung            |
|---------|-------------------------|-------------------------|
| GET     | `/api/counter`          | Aktuellen Zähler lesen  |
| POST    | `/api/counter/increment`| Zähler um 1 erhöhen     |
| POST    | `/api/counter/reset`    | Zähler auf 0 setzen     |

Alle Endpunkte liefern JSON: `{ "count": <int> }`

## Frontend starten

In einem zweiten Terminal:

```bash
cd frontend
npm install
npm run dev
```

Die UI läuft auf http://localhost:5173

Vite leitet alle `/api/*`-Anfragen an das Backend auf Port 8080 weiter
(siehe `frontend/vite.config.js`), daher sind keine CORS-Probleme im Dev-Betrieb
zu erwarten. Zusätzlich ist `@CrossOrigin` im Controller gesetzt.

## Nutzung

1. Beide Server starten (Backend + Frontend).
2. Browser öffnen: http://localhost:5173
3. Auf **Hochzählen** klicken – der Zähler wird serverseitig inkrementiert
   und der neue Wert angezeigt.
4. **Zurücksetzen** setzt den Zähler wieder auf 0.
