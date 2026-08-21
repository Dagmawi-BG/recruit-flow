# RecruitFlow API Engine

A Spring Boot ATS backend (Java 21, Spring Data MongoDB, Spring Security).

This scaffold focuses on the **autocomplete candidate search** feature — typing
`mar` returns **Mark** and **Marcus** — built on **MongoDB Atlas Search** running
locally via the `mongodb/mongodb-atlas-local` Docker image.

> Why Atlas Search and not native `$text`? Native MongoDB `$text` search only
> matches whole words (with stemming), so `mar` matches nothing. Atlas Search's
> `autocomplete` field type uses edge n-grams, which is what enables true prefix
> matching with fuzzy typo tolerance and relevance ranking.

The auth (JWT/RBAC), job, and application-pipeline phases from the spec follow
via TDD; `SecurityConfig` is currently permissive so the search endpoint and
Swagger are reachable.

---

## Prerequisites

- Docker Desktop (with WSL2 integration) — for MongoDB and for building.
- JDK 21 — only needed if you run the app directly on Windows (Option B below).
  You have it at `C:\Program Files\jdk-21.0.6`.

## Project layout

```
recruit-flow/
├── docker-compose.yml          # Atlas Local (MongoDB + Atlas Search engine)
├── Dockerfile                  # Multi-stage build (no local Maven needed)
├── scripts/
│   ├── create-search-index.js  # Defines the autocomplete search index
│   └── seed.js                 # Inserts demo candidates (Mark, Marcus, ...)
├── pom.xml
└── src/main/java/com/recruitflow/
    ├── controller/CandidateController.java   # GET /api/candidates/search?q=
    ├── service/CandidateSearchService.java   # Atlas $search autocomplete
    ├── model/CandidateProfile.java
    ├── repository/CandidateRepository.java
    ├── dto/response/CandidateResponse.java
    └── config/SecurityConfig.java            # temporary permit-all
```

---

## Step 1 — Start MongoDB (Atlas Local)

From the project folder (`C:\Users\user\recruit-flow`):

```bash
docker compose up -d
```

Wait until it's healthy (about 20–30s the first time):

```bash
docker compose ps
```

You want STATUS `healthy` for `recruitflow-mongo`.

## Step 2 — Create the search index + seed demo data

The Atlas Local container ships with `mongosh`, so run the scripts inside it:

```bash
docker exec recruitflow-mongo mongosh --quiet --file /scripts/create-search-index.js
```

```bash
docker exec recruitflow-mongo mongosh --quiet --file /scripts/seed.js
```

The search index takes ~10–30s to become queryable after creation. Check status:

```bash
docker exec recruitflow-mongo mongosh --quiet --eval "db.getSiblingDB('recruitflow').candidate_profiles.getSearchIndexes().forEach(i => print(i.name, i.status))"
```

Wait until it prints `candidateAutocomplete READY`.

## Step 3 — Build and run the app

### Option A — build in Docker, run on Windows with JDK 21 (recommended)

Build the jar without needing Maven installed (run in **PowerShell** from the project folder):

```bash
docker run --rm -v "${PWD}:/app" -w /app maven:3.9-eclipse-temurin-21 mvn -q clean package -DskipTests
```

Run it with **JDK 21 explicitly** (your default `java` on PATH is Java 8, which won't run a 21 build):

```bash
& "C:\Program Files\jdk-21.0.6\bin\java.exe" -jar target\recruit-flow-0.0.1-SNAPSHOT.jar
```

### Option B — run entirely in Docker

```bash
docker build -t recruitflow-app .
```

```bash
docker run --rm -p 8080:8080 --network recruit-flow_default -e MONGODB_URI="mongodb://mongodb:27017/recruitflow?directConnection=true" recruitflow-app
```

> The `--network recruit-flow_default` value is the network Compose created for
> Step 1; confirm the exact name with `docker network ls`.

## Step 4 — Try the autocomplete search

```bash
curl "http://localhost:8080/api/candidates/search?q=mar"
```

Expected: Mark, Marcus, Maria, Marta — ranked by relevance. Try `q=mark`,
`q=marc`, or a typo like `q=makr` (fuzzy tolerance).

Swagger UI: http://localhost:8080/swagger-ui.html

---

## Stopping / resetting

```bash
docker compose down        # stop containers, keep data
docker compose down -v      # stop AND wipe the database volume
```
