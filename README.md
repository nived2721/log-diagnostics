# AI-Powered Log Diagnostics Platform

A production-grade internal developer tool that lets engineers query millions of application logs in plain English and receive ranked root-cause suggestions — built with AWS Bedrock, Claude API, LangChain, and pgvector.

---

## The Problem

At scale, production incidents are painful. An engineer gets paged at 2am, opens Grafana, and then spends 45 minutes manually grepping through millions of log lines trying to find what went wrong. Most of that time is wasted — not because the engineer isn't good, but because the tools weren't built for natural language.

This tool fixes that.

---

## What It Does

- **Natural language log search** — ask "why did the payment service fail at 3am?" and get back ranked, relevant log entries with context
- **Automated root-cause suggestions** — the LLM analyzes retrieved logs and surfaces likely causes with remediation steps
- **Semantic retrieval** — uses pgvector embeddings so you find relevant logs even when the exact keywords don't match
- **Grafana integration** — drill-down links connect suggestions directly to live dashboard panels
- **Streaming responses** — results stream token by token so engineers aren't staring at a spinner

---

## Architecture

```
Engineer Query
      │
      ▼
  Spring Boot API (REST + SSE streaming)
      │
      ├──► LangChain Retrieval Chain
      │         │
      │         ├──► pgvector (semantic search over log embeddings)
      │         └──► AWS Bedrock / Claude API (ranked root-cause generation)
      │
      └──► React 18 Dashboard
                │
                └──► Grafana drill-down links
```

**Ingestion pipeline:**
```
Raw Logs → Kafka topic → Spring consumer → chunk + embed → pgvector (PostgreSQL)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| LLM | AWS Bedrock (Claude), Anthropic SDK |
| Orchestration | LangChain, Spring AI |
| Vector Store | pgvector (PostgreSQL extension) |
| Backend | Java 21, Spring Boot, Spring WebFlux |
| Streaming | Server-Sent Events (SSE) |
| Ingestion | Apache Kafka |
| Frontend | React 18, TypeScript, Tailwind CSS |
| Observability | OpenTelemetry, Grafana, Prometheus |
| Deployment | Docker, Kubernetes (EKS) |

---

## Key Design Decisions

**Why pgvector over Pinecone?**
Keeping vectors in PostgreSQL alongside structured log metadata means a single JOIN gives you semantic similarity + exact filters (service name, time range, severity) in one query. No extra infra to manage.

**Why LangChain for orchestration?**
The retrieval chain needed reranking, prompt templating, and memory — LangChain's abstractions let us swap the underlying LLM (Bedrock ↔ OpenAI) without touching retrieval logic.

**Why streaming?**
Root-cause analysis on 2M+ logs takes 3-8 seconds. Streaming the LLM response token-by-token means engineers see output immediately rather than waiting for a full response — measurably reduces perceived latency.

---

## Results

- Incident triage time: **45 min → 15 min** (↓ 67%)
- MTTR reduction: **~50%** across on-call incidents
- Adopted by 3 engineering teams within 6 weeks of shipping

---

## Running Locally

### Prerequisites
- Java 21
- Docker + Docker Compose
- AWS credentials (Bedrock access) or Anthropic API key

### Setup

```bash
git clone https://github.com/nived2721/log-diagnostics
cd log-diagnostics
cp .env.example .env
# Add your AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, ANTHROPIC_API_KEY
```

```bash
docker-compose up -d
# Starts PostgreSQL with pgvector, Kafka, and the Spring Boot backend
```

```bash
cd frontend && npm install && npm run dev
# React dev server at http://localhost:3000
```

### Ingest sample logs

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{"source": "sample", "lines": 10000}'
```

### Query

```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"question": "why did the payment service throw NullPointerExceptions last night?"}'
```

---

## Project Structure

```
log-diagnostics/
├── backend/
│   ├── src/main/java/
│   │   ├── api/          # REST + SSE controllers
│   │   ├── ingestion/    # Kafka consumer + chunking + embedding
│   │   ├── retrieval/    # LangChain chain + pgvector queries
│   │   └── llm/          # Bedrock + Claude API clients
│   └── docker-compose.yml
├── frontend/
│   ├── src/
│   │   ├── components/   # Query input, results stream, Grafana links
│   │   └── hooks/        # useSSE, useQueryHistory
└── README.md
```

---

## What I Learned

Building this forced me to think carefully about chunking strategy — naive line-by-line chunking gave terrible retrieval because a single error spans multiple log lines. Sliding window chunking with overlap fixed recall significantly.

Reranking also mattered more than I expected. The first retrieval pass returned semantically similar logs but not always the most causally relevant ones. Adding a cross-encoder reranking step before LLM synthesis improved response quality measurably.

---

## Contact

Nived — [linkedin.com/in/sainived2721](https://linkedin.com/in/sainived2721) · nived7669@gmail.com
