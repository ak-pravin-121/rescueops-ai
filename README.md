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
