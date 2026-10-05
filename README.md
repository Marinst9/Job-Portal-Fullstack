# MarinaJobs.AI — Job Portal with Local-LLM CV Matching

A full-stack job portal with two roles. **Employers** post jobs and see who applied, ranked by an AI match score. **Candidates** upload a CV, see a match percentage for every open job, and apply.

The match score comes from a local LLM (Ollama, `llama3`) called through Spring AI. CVs never leave the machine running the backend, which matters because they contain personal data.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, Tailwind CSS (CDN), lucide-react |
| Backend | Java 21, Spring Boot 3.3 (Web, Data JPA), Spring AI (Ollama) |
| Database | PostgreSQL |
| Passwords | BCrypt (`spring-security-crypto`) |
| AI | Ollama running `llama3` locally |

## How it works

```
React (Vite, :5173)
   │  fetch / JSON
   ▼
Spring Boot REST API (:8081)
   controller → service → repository (Spring Data JPA) → PostgreSQL
                    │
                    └── AIService → Spring AI OllamaChatModel → Ollama (:11434)
```

**Data model**
- `users`: full name, unique email, BCrypt password hash, role (`CANDIDATE` or `EMPLOYER`)
- `jobs`: title, company, description, location, salary, and the employer who posted it (`employer_id`)
- `job_applications`: job, candidate, CV text, AI match score (nullable), status, timestamp

**AI match score**
1. The prompt wraps the CV and job description in `<cv>` / `<job>` tags. It tells the model that this text is data, not instructions, which is a basic prompt-injection defence.
2. The model is asked to reply with JSON only: `{"score": <0-100>}`.
3. `AIService.parseScore` takes the JSON object out of the reply. It accepts only an integer from 0 to 100. Anything else becomes `null` ("not scored"). A failed call is never stored as a fake 0%.
4. If the reply can't be parsed, the service retries once.

Employers see applicants sorted by score, with unscored applicants last.

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register (password is BCrypt-hashed, never returned) |
| POST | `/api/v1/auth/login` | Log in, returns the user without the password |
| GET | `/api/v1/jobs` | List jobs |
| POST | `/api/v1/jobs` | Create a job (employer must exist and have role `EMPLOYER`) |
| GET | `/api/v1/jobs/employer/{employerId}` | Jobs posted by one employer |
| POST | `/api/v1/jobs/match` | `{ "cvText": "..." }` → `{ jobId: score \| null }` for every job |
| POST | `/api/v1/applications` | Apply (`job.id`, `user.id`, `coverLetter`); returns 409 if already applied |
| GET | `/api/v1/applications/employer/{employerId}` | Applications for one employer's jobs, ranked by score |
| GET | `/api/v1/applications/job/{jobId}` | Applications for one job, ranked by score |
| GET | `/api/v1/applications/user/{userId}` | One candidate's applications |

Errors are returned as `{ "error": "..." }` with status 400, 404, 409 or 500 from a single `@RestControllerAdvice`.

## Running locally

**Prerequisites:** Java 21, Node.js 18+, PostgreSQL on port 5432, and [Ollama](https://ollama.com).

1. **Database**
   ```sql
   CREATE DATABASE job_portal_db;
   ```
   The connection settings come from environment variables, with local defaults: `DB_URL`, `DB_USERNAME` (`postgres`), `DB_PASSWORD` (`admin`).

2. **Ollama**
   ```bash
   ollama pull llama3
   ollama serve
   ```
   To use another model, set `OLLAMA_MODEL`.

3. **Backend**
   ```bash
   ./mvnw spring-boot:run
   ```
   On the first start with an empty database, two demo accounts and three jobs are created:
   - `candidate@marinajobs.mk` / `test`
   - `employer@marinajobs.mk` / `test`

4. **Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Open http://localhost:5173. To point the frontend at another API URL, set `VITE_API_URL`.

**Upgrading from an older version:** earlier versions stored plaintext passwords, and those accounts can no longer log in. Drop and recreate `job_portal_db` so the demo data is seeded again with hashed passwords.

## Tests

```bash
./mvnw test
```
`AIServiceParseScoreTest` is a plain unit test of score parsing and prompt construction. It needs no database and no Ollama. `JobPortalApplicationTests.contextLoads` starts the full application, so it needs PostgreSQL to be running.

## Known limitations

These are deliberate scope cuts for a portfolio project, not hidden features:

- **No session or token authentication.** Login checks the password with BCrypt and returns the user object, but later requests are not authenticated. The frontend keeps the user in `localStorage` and sends `user.id`, so the API trusts the client. The next step would be Spring Security with JWT or session cookies, `@PreAuthorize` role checks, and taking the user id from the token instead of the request body.
- **Matching is synchronous.** `/jobs/match` makes one LLM call per job inside a single HTTP request. That is fine for a handful of jobs, but with many it would need caching by (CV hash, job id) and asynchronous scoring.
- **CVs are plain `.txt` files only.** There is no PDF or DOCX parsing.
- **Application status is always `PENDING`.** There is no workflow for moving an application to interview, offer or rejected.
- The schema is managed by `ddl-auto=update` rather than Flyway or Liquibase migrations.

## Roadmap

- [ ] Spring Security + JWT, role-based endpoints
- [ ] Structured skill-gap output (`{score, matched_skills, missing_skills}`)
- [ ] Application status workflow (Applied → Interview → Offer / Rejected)
- [ ] PDF CV parsing
- [ ] Flyway migrations, Testcontainers integration tests
