# RecruitFlow — Applicant Tracking System (ATS) Backend

A Spring Boot backend for recruiting workflows: post jobs, manage candidate profiles, and move applications through a hiring pipeline — with **prefix/fuzzy candidate search** powered by MongoDB Atlas Search and **fine-grained, department-scoped authorization**.

**Stack:** Java 21 · Spring Boot 3 · Spring Data MongoDB (Atlas Local via Docker) · Spring Security + JWT (jjwt) · springdoc / Swagger UI · JUnit 5

## Features

- **Auth** — register / login / **refresh tokens** / logout, stateless JWT (15-min access, 7-day refresh).
- **Jobs** — create, list, view, edit, delete postings.
- **Candidate profiles** — view, update, and **autocomplete search**.
- **Applications** — candidates apply to jobs and are advanced through a **hiring pipeline**.
- **Autocomplete search** — typing `mar` returns **Mark**, **Marcus**, **Maria**… ranked by relevance, with typo tolerance (`makr` still finds `Mark`), built on **Atlas Search** `autocomplete` (edge n-gram) indexes.
- **Role-based + ownership access control** — enforced at the method level (details below).

> **Why Atlas Search, not native `$text`?** MongoDB's `$text` only matches whole words (with stemming), so `mar` matches nothing. Atlas Search's `autocomplete` field type uses edge n-grams — true prefix matching with fuzzy tolerance and relevance ranking.

## Authorization model

Security is **enforced** (stateless JWT filter + `@EnableMethodSecurity`); only `/api/auth/**` and Swagger are public. Access is expressed with custom method annotations:

| Annotation | Rule |
|---|---|
| `@IsRecruiter` | caller has role `RECRUITER` |
| `@IsProfileOwner` | `#userId` equals the authenticated user |
| `@CanViewProfile` | the profile owner **or** any `RECRUITER` (post-authorization) |
| `@CanEditJob` | the job's creating recruiter **or** an `ADMIN` (custom `PermissionEvaluator`) |
| `@IsHiringManagerForApplication` | a `HIRING_MANAGER` **whose department owns the application** (`deptSecurity.isManagerForApplication`) |

**Roles:** `ADMIN`, `RECRUITER`, `HIRING_MANAGER`, `CANDIDATE`. Hiring managers are **scoped to their department** — an Engineering manager cannot act on a Marketing application.

## API

| Method & path | Purpose | Guard |
|---|---|---|
| `POST /api/auth/register` · `/login` · `/refresh` · `/logout` | Authentication | public |
| `GET /api/candidates/search?q=` | Autocomplete candidate search | authenticated |
| `GET /api/candidates/{userId}` | View a profile | `@CanViewProfile` |
| `PUT /api/candidates/{userId}` | Update own profile | `@IsProfileOwner` |
| `POST /api/jobs` · `GET /api/jobs` · `GET /api/jobs/{id}` | Create / list / view jobs | authenticated |
| `PUT /api/jobs/{id}` · `DELETE /api/jobs/{id}` | Edit / delete a job | `@CanEditJob` |
| `POST /api/applications` | Apply to a job | authenticated |
| `PATCH /api/applications/{appId}/stage` | Advance the pipeline stage | `@IsHiringManagerForApplication` |
| `GET /api/applications/{appId}` | View an application | authenticated |

**Pipeline stages:** `APPLIED → SCREEN → INTERVIEW → OFFER → HIRED` (or `REJECTED`).

Interactive API docs: **http://localhost:8080/swagger-ui.html**

## Getting started

**Prerequisites:** Docker Desktop (for Atlas Local) · JDK 21 (to run the app directly).

### 1. Start MongoDB (Atlas Local)

```bash
docker compose up -d
```
Wait for `recruitflow-mongo` to report `healthy` (`docker compose ps`). It runs a single-node replica set on host port **27018** and connects with `directConnection=true`.

### 2. Create the search index + seed demo candidates

```bash
docker exec recruitflow-mongo mongosh --quiet --file /scripts/create-search-index.js
```
```bash
docker exec recruitflow-mongo mongosh --quiet --file /scripts/seed.js
```
The index takes ~10–30s to become queryable. Check with:
```bash
docker exec recruitflow-mongo mongosh --quiet --eval "db.getSiblingDB('recruitflow').candidate_profiles.getSearchIndexes().forEach(i => print(i.name, i.status))"
```
Wait for `candidateAutocomplete READY`.

### 3. Run the app

Build in Docker (no local Maven needed), then run with JDK 21:
```bash
docker run --rm -v "${PWD}:/app" -w /app maven:3.9-eclipse-temurin-21 mvn -q clean package -DskipTests
```
```bash
& "C:\Program Files\jdk-21.0.6\bin\java.exe" -jar target\recruit-flow-0.0.1-SNAPSHOT.jar
```
> On Windows your default `java` is Java 8, which can't run a 21 build — invoke JDK 21 explicitly as above.

On startup a `DataSeeder` inserts demo **users** (and an Engineering job + application to demo department security).

### Demo users (dev only — password = username)

| Username | Role | Department |
|---|---|---|
| `admin` | ADMIN | — |
| `recruiter`, `recruiter2` | RECRUITER | — |
| `candidate` | CANDIDATE | — |
| `eng_manager` | HIRING_MANAGER | Engineering |
| `mkt_manager` | HIRING_MANAGER | Marketing |

## Try it

```bash
# 1) Log in to get a JWT
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"recruiter","password":"recruiter"}'

# 2) Autocomplete search (use the "token" value from step 1's response)
curl "http://localhost:8080/api/candidates/search?q=mar" -H "Authorization: Bearer <token>"
```
Expected: Mark, Marcus, Maria, Marta — ranked by relevance. Try `q=marc` or a typo like `q=makr`.

## Tests

```bash
mvn clean test
```
Integration tests cover auth, jobs, applications, candidate search, and the method-security rules (`MethodSecurityTest`).

## Stopping / resetting

```bash
docker compose down       # stop, keep data
docker compose down -v     # stop AND wipe the database volume
```

## Configuration secrets

Only `${ENV_VAR}` placeholders with dev-only defaults are committed. Provide real values via environment (`MONGO_URI`, `JWT_SECRET`, …) or a git-ignored `.env` / `application-local.yml`.
