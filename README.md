# RescueOps AI
### Autonomous Incident Intelligence Platform

> Transform alerts, logs, and incidents into actionable resolutions using Gemini-powered AI agents.

Built for **Vibe2Ship 2026** (Coding Ninjas × Google for Developers).

**Problem statement mapping:** RescueOps AI is built as the *Last-Minute Life Saver* — when a
production system fails, engineers waste time, revenue is lost, and customers are impacted.
RescueOps becomes the agent that gets a team unstuck in minutes instead of hours. It doubles as a
*Community Hero* for IT teams, startups, enterprises, and open-source maintainers who need to
resolve incidents faster than they could alone.

---

## 1. Architecture

```
┌─────────────────────────┐
│      React Frontend     │  Vite + React 19 + Tailwind v4 + Recharts + STOMP/SockJS
└──────────┬──────────────┘
           │ REST (JWT) + WebSocket (STOMP)
           ▼
┌─────────────────────────┐
│ Spring Boot 3.5 API     │  Java 21
└──────────┬──────────────┘
           │
 ┌─────────┼─────────┬─────────────┐
 ▼         ▼         ▼             ▼
Incident   AI Agents  RAG          Prediction
Service   (Gemini)   (Embeddings   Engine
                       + cosine
                       similarity)
 ▼         ▼         ▼             ▼
PostgreSQL  Gemini API (generateContent / embedContent)
```

**Backend package layout** (`com.rescueops.*`): `config`, `security`, `controller`, `service`,
`repository`, `entity`, `dto`, `exception`, `websocket`, `ai`, `rag`, `prediction` — matching the
structure laid out in the project brief.

## 2. What's implemented, and how

| Module | Status | Implementation notes |
|---|---|---|
| Incident Management | ✅ Full | Create/list/filter/update incidents, attach log files (text-based: Spring Boot, Docker, Kubernetes, Nginx logs, monitoring reports) |
| AI Incident Analyzer | ✅ Full | Gemini call returns strict JSON `{severity, rootCause, confidence, fix}` exactly as specified |
| Root Cause Agent | ✅ Full | Chained agent — consumes the Analyzer's output **and** RAG context ("have we seen this before?") to go deeper |
| Runbook Agent | ✅ Full | Generates a numbered step-by-step remediation runbook, persisted per-incident |
| Postmortem Agent | ✅ Full | Generates Summary / Root Cause / Impact / Timeline / Prevention automatically |
| Security Agent | ✅ Full | Scans incident logs for SQLi, brute force, XSS, DDoS indicators |
| Multi-Agent Workflow | ✅ Full, literal | `/api/ai/full-triage` runs Analyzer → Root Cause → Runbook as a real chained pipeline, broadcasting each step to the live War Room as it happens |
| RAG Knowledge Base | ✅ Functional, simplified | See note below — no external vector DB; embeddings + cosine similarity stored in Postgres |
| Predictive Failure Engine | ✅ Functional, simplified | See note below — statistical signals + Gemini reasoning, not a trained ML model |
| Live Incident War Room | ✅ Full | Real STOMP/SockJS WebSocket broadcast; JWT-authenticated at CONNECT time |
| Real-time Dashboard | ✅ Full | Live-polling severity distribution, resolution rate, active incidents, top-risk services |

### Honest notes on the two "hard" modules

We'd rather be upfront about these than oversell them:

- **RAG / Vector DB** — Rather than wiring up an external vector database (Pinecone/Qdrant/pgvector),
  we chunk uploaded documents, embed each chunk with Gemini's `gemini-embedding-001`, and store the
  vectors directly in a Postgres column. Search embeds the query and computes cosine similarity in
  Java against all stored chunks. This is genuine semantic search and genuine RAG — it's just
  self-contained, with no extra infrastructure to deploy or that could silently be unavailable. It
  scales fine for a hackathon-scale knowledge base; a production system would swap in a dedicated
  vector store without changing the API surface.

- **Predictive Failure Engine** — We are not training or shipping an ML model (not realistic to do
  honestly in this timeframe). Instead, `PredictionService` computes real statistical signals from
  incident history (30-day frequency and critical-severity ratio per service) and asks Gemini to
  reason over those numbers to produce a risk score and explanation. It's genuinely data-driven and
  explainable, just LLM-reasoning-based rather than a trained classifier.

## 3. Tech stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Security 6 (JWT), Spring Data JPA, Spring WebSocket (STOMP/SockJS), PostgreSQL
- **AI:** Google AI Studio / Gemini (`gemini-2.5-flash` for generation, `gemini-embedding-001` for embeddings)
- **Frontend:** React 19, Vite, React Router 7, Tailwind CSS v4, Recharts, `@stomp/stompjs` + `sockjs-client`, lucide-react
- **Auth:** JWT (7-day expiry by default)
- **Deployment target:** Render / Railway (Docker)

## 4. Running it locally

### Prerequisites
- Java 21 + Maven
- Node 20+
- PostgreSQL 14+ running locally (or update `DB_HOST` etc. to point elsewhere)
- A free Gemini API key from [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)

### Backend
```bash
cd backend
cp .env.example .env        # then fill in DB + GEMINI_API_KEY
# export the vars in .env into your shell, or use a tool like direnv/dotenv-cli
mvn spring-boot:run
```
The API starts on `http://localhost:8080`. `spring.jpa.hibernate.ddl-auto=update` will create the
schema automatically on first run — no manual migrations needed.

### Frontend
```bash
cd frontend
cp .env.example .env        # defaults already point at localhost:8080
npm install
npm run dev
```
## 5. Deploying to Render

This project ships with a **Render Blueprint** (`render.yaml` at the repo root) that provisions all
three pieces — backend, frontend, and database — in one go. See **"Deployment changes" below** for
exactly what was added/changed to make this possible, and follow these steps in order:

### Step 1 — Push to GitHub
Render deploys from a Git repo. Push this whole project (including `render.yaml`) to a new GitHub repo.

### Step 2 — Create the Blueprint
In the Render Dashboard: **New → Blueprint** → connect your repo → Render detects `render.yaml` and
shows you three resources: `rescueops-db` (Postgres), `rescueops-backend` (Docker web service),
`rescueops-frontend` (static site).

### Step 3 — Fill in the prompted secrets
Render will prompt you for every `sync: false` variable before the first deploy:
- `GEMINI_API_KEY` — your real key from [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)
- `CORS_ALLOWED_ORIGINS`, `VITE_API_BASE_URL`, `VITE_WS_BASE_URL` — **leave these as a placeholder**
  (e.g. `http://localhost`) for now. Neither service has a public URL yet, so there's nothing real to
  put here — you'll fix this in Step 5. Click **Apply** to deploy.

### Step 4 — Note the two generated URLs
Once both services finish their first deploy, copy their URLs from the Render Dashboard, e.g.:
- Backend: `https://rescueops-backend-xxxx.onrender.com`
- Frontend: `https://rescueops-frontend-xxxx.onrender.com`

### Step 5 — Wire them together (the only manual step)
This is a one-time chicken-and-egg fix — neither service can know the other's URL until both exist:
1. Open **rescueops-backend → Environment** and set `CORS_ALLOWED_ORIGINS` to your frontend URL
   from Step 4 (e.g. `https://rescueops-frontend-xxxx.onrender.com`). Save — this only restarts the
   backend (no rebuild needed, since it's read at runtime).
2. Open **rescueops-frontend → Environment** and set:
   - `VITE_API_BASE_URL` = `https://rescueops-backend-xxxx.onrender.com/api`
   - `VITE_WS_BASE_URL` = `https://rescueops-backend-xxxx.onrender.com`
   Save — this triggers a frontend **rebuild**, because Vite bakes these values into the JS bundle
   at build time, not runtime.

Once both finish redeploying, open the frontend URL — the app is live.

### Free tier notes
- Free Postgres on Render expires **30 days after creation** (1GB storage, no backups) — fine for a
  hackathon demo, not for anything long-lived. Upgrade the database's instance type before then if
  you want to keep it.
- Free web services **spin down after 15 minutes of inactivity** and take ~30-60s to wake back up on
  the next request — the first login after idle time will feel slow. This is normal.

## 6. What changed in this version, specifically for Render deployment

None of this touches application behavior — these are deployment-readiness changes only:

| File | Change | Why |
|---|---|---|
| `render.yaml` (new) | One-click Blueprint defining the DB, backend Docker service, and frontend static site, wired together via `fromDatabase` | This is the file Render actually reads to provision everything — without it you'd have to click through three separate manual service setups |
| `backend/.../config/SecurityConfig.java` | CORS origin parsing now trims whitespace, strips trailing slashes, and drops blank entries before matching | A pasted `CORS_ALLOWED_ORIGINS` value with a stray space or trailing `/` previously caused a silent CORS rejection (exact string match) — easy to lose an hour to in production |
| `backend/.../resources/application.yml` | Added `server.forward-headers-strategy: framework` | Render terminates HTTPS at its edge proxy and forwards plain HTTP internally; without this, Spring can't correctly tell it's being served over HTTPS |
| `backend/.../resources/application.yml` | Added `spring.datasource.hikari.maximum-pool-size: ${DB_POOL_SIZE:5}` | Render's free/Basic Postgres tiers cap total connections fairly low; Spring's Hikari default pool (10) per instance can eat into that budget unnecessarily |
| `backend/Dockerfile` | Added `-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport`, switched to a non-root user | Without a RAM percentage cap, the JVM can misjudge available memory on a constrained container and get OOM-killed by Render; running as non-root is a standard container hardening practice |
| `backend/.dockerignore` (new) | Excludes `target/`, IDE files, `.env` | Keeps the Docker build context small and prevents a stray local `.env` from ever being baked into an image |
| `render.yaml` → `routes` | SPA fallback rewrite (`/* → /index.html`) on the static site | Without this, refreshing or directly opening a deep link like `/incidents/12` 404s, because Render's static file server doesn't know about client-side React Router routes |

## 7. API reference (high level)

```
POST   /api/auth/register            POST /api/auth/login

POST   /api/incidents                GET  /api/incidents?severity&status&serviceName
GET    /api/incidents/{id}            PUT  /api/incidents/{id}

GET    /api/incidents/meta/services
POST   /api/incidents/{id}/files      GET  /api/incidents/{id}/files
GET    /api/incidents/{id}/messages   POST /api/incidents/{id}/messages

POST   /api/ai/analyze | /root-cause | /runbook | /postmortem | /security-scan | /full-triage
GET    /api/ai/history/{incidentId}   GET  /api/ai/security-alerts

POST   /api/kb/upload                 GET  /api/kb/search?query=...
GET    /api/kb/documents

GET    /api/predictions               POST /api/predictions/run?serviceName=...
GET    /api/predictions/{serviceName}/latest

GET    /api/dashboard/summary
WS     /ws  (STOMP, subscribe to /topic/incidents/{id})
```

## 8. Suggested demo flow for judges

1. Register two accounts: an Engineer and (optionally) an Admin.
2. Create an incident (e.g. *"payment-service returning 500s for checkout"*), attach a sample log
   file with an obvious smell (connection pool exhaustion, OOM, etc.).
3. Click **Run Full Multi-Agent Triage** — watch the War Room fill up in real time as the Analyzer,
   Root Cause, and Runbook agents hand off to each other.
4. Open a second browser tab on the same incident to show the War Room updating live across
   sessions via WebSocket.
5. Run the Security Agent on the same logs.
6. Go to **Knowledge Base**, upload a short SOP or a past-incident write-up, then ask *"have we seen
   X before?"* on a new incident to show RAG retrieval grounding the Root Cause Agent.
7. Go to **Predictions**, run the risk engine for the service — show the statistical + LLM-reasoned
   risk score.
8. Generate a **Postmortem** once the incident is marked Resolved.
