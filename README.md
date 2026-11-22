# AnyGuessr

AnyGuessr is a full-stack geography quiz application built as a group project for CT5106 Software Engineering II at the University of Galway. Players can choose a map, start a region-identification quiz, submit answers, and view game results. The repository also contains map creation, user identity, social and statistics features.

The frontend uses React, TypeScript and Vite. The backend uses Java 25, Spring Boot 4.1.1 and Maven, with MySQL for application data. Authentication is integrated with Spring Security and AWS Cognito (OAuth2/OpenID Connect).

> **Current verification status:** The frontend builds, and the backend's 39 automated tests pass with the documented test configuration. A GitHub Actions run also successfully built and packaged the backend and built the frontend. A complete authenticated browser-to-database gameplay walkthrough has **not yet been verified**. The local Map Selector returned an authentication redirect when the backend was started with a dummy OAuth2 client; dummy credentials do not constitute a working login.

## Repository structure

```text
AnyGuessr/
├── backend/                    Spring Boot API and Maven Wrapper
│   ├── src/main/java/.../
│   │   ├── gameplay/           Quiz domain, use cases, repositories and API
│   │   ├── identity/           User identity and Cognito integration
│   │   ├── mapCreation/        Map and region API
│   │   ├── social/             Friend-related functionality
│   │   ├── stats/              Results and statistics
│   │   └── config/             Application configuration
│   └── src/test/               Automated backend tests
├── frontend/                   React and TypeScript app
│   ├── src/pages/              Screens, including Map Selector and Quiz
│   ├── src/components/         Reusable UI components
│   ├── src/api/                Map-related API calls
│   └── src/services/           Gameplay and statistics API calls
├── db/
│   ├── init/                   SQL database schema scripts
│   └── seed/demo.sql           Demo data for a new database
├── docker-compose.yml         Optional local MySQL container
├── .env.example               Example database variables (no real secrets)
└── README.md
```

The backend is organised by feature. Within the Gameplay feature, the `domain` package contains game concepts and repository interfaces, the `application` package contains use cases, `infrastructure` contains persistence adapters, and `ui` exposes REST endpoints. The frontend communicates with the Spring Boot API using HTTP. Gameplay events are also handled by the statistics integration. This describes the present structure; architectural compliance should be assessed against the code and demonstrated in the group presentation.

## Prerequisites

- Git.
- **JDK 25** for the backend.
- **Node.js and npm** for the frontend (Node.js 24 is used in the CI workflow).
- Access to **MySQL 8.4**, either locally, through Docker, or through the team's shared AWS RDS setup.
- The team's **approved Cognito OAuth2 configuration** for authenticated application features.

Maven does not need to be installed separately: the backend includes Maven Wrapper scripts.

## Get the code

```bash
git clone https://github.com/NiallMahon/AnyGuessr.git
cd AnyGuessr
```

For an existing checkout, coordinate with the team before updating your branch:

```bash
git switch main
git pull --ff-only origin main
```

## Configure the database

The backend imports optional database settings from a private `.env.aws` file at the repository root. It is excluded from Git by `.gitignore` and must **never** be committed.

Create `.env.aws` with the appropriate values supplied or approved by the database owner:

```properties
DB_HOST=127.0.0.1
MYSQL_DATABASE=anyguessr
MYSQL_USER=anyguessr_app
MYSQL_PASSWORD=YOUR_PRIVATE_DATABASE_PASSWORD
```

Replace the password placeholder locally. Do not post database credentials, SSH keys, tokens or the contents of this file in issues or pull requests. Use the application database account, not the MySQL root account, to run the backend. The database must already contain the schema expected by the application.

### Option A: Existing local MySQL server

If MySQL is installed locally, start or check its service using your operating system's service manager. Make sure the configured database, application account and tables exist. For example, on Windows the local service may be called `MySQL84`.

A local setup has been tested with MySQL 8.4.9, an existing `anyguessr` database and an `anyguessr_app` account. This verifies database access, not the complete application flow.

### Option B: Docker MySQL for a fresh environment

The repository provides `docker-compose.yml`, which starts a MySQL 8.4 database and loads scripts from `db/init/` when its data volume is **first** created. Copy `.env.example` to `.env`, replace placeholder passwords, then run:

```bash
docker compose up -d db
```

Set `.env.aws` to match the database name, application user and password created by Docker. The Compose example uses different default names from the shared AWS database, so do not assume `anyguessr_app` exists in a fresh Docker installation.

For demo maps and regions in a **new, empty** database, review `db/seed/demo.sql` and run it once after the schema is in place. **Do not rerun schema or seed scripts against an existing populated database without reviewing their effects and backing up data.**

### Option C: Team's shared AWS RDS database

The existing team setup uses a MySQL database on Amazon RDS in `eu-north-1` (Stockholm). Obtain its current connection details and access permissions privately from the teammate managing infrastructure.

On campus, the team documentation describes an SSH relay on port 443. Configure your approved SSH key and keep this tunnel open in a separate terminal:

```bash
ssh -i ~/.ssh/anyguessr-relay -p 443 -N -L 127.0.0.1:3306:YOUR_RDS_ENDPOINT:3306 ec2-user@YOUR_RELAY_HOST
```

Replace the host placeholders with the team's real private connection details. With the tunnel active, use `DB_HOST=127.0.0.1`. Off campus, the team setup may allow direct access to the RDS endpoint after the user's public IP is allowlisted; in that case use the approved RDS hostname in `DB_HOST` and do not start the tunnel. Follow the infrastructure owner's current instructions before relying on either route.

## Configure authentication

The backend's Spring Security configuration uses OAuth2 login and requires authentication for API requests other than `/`. The frontend Login page points to the Cognito authorisation endpoint.

A valid Cognito client registration and callback setup is therefore required for actual sign-in, map loading, quiz submissions and user statistics. Obtain the correct local configuration from the teammate responsible for authentication. Keep any client secrets outside Git.

**Dummy OAuth2 client values are only for test execution or diagnosing startup.** They can allow Spring Boot to start, but they cannot sign in a real user. In a local diagnostic run, `GET /api/maps` returned HTTP `302` with a redirect to `/oauth2/authorization/test`; the Map Selector consequently could not load maps. This is a known integration blocker pending a real authenticated walkthrough.

## Run the application locally

Start the backend and frontend in **separate terminals** after configuring MySQL and authentication.

**Backend (Windows Command Prompt):**

```cmd
cd backend
mvnw.cmd spring-boot:run
```

**Backend (Linux/macOS):**

```bash
cd backend
./mvnw spring-boot:run
```

A successful backend startup reports that Tomcat has started on `http://localhost:8080`. This is the backend address, not the main React interface.

**Frontend (from the repository root, in another terminal):**

```bash
cd frontend
npm ci
npm run dev
```

Open **http://localhost:5173/** in your browser. The Vite development server proxies `/api` requests to `http://localhost:8080`. Some map-related frontend calls currently address `http://localhost:8080` directly, so frontend/backend integration should be verified with a valid authenticated session.

The homepage may render even when the backend's protected APIs cannot be accessed. Loading the homepage alone is not proof of a working full-stack application.

## Build and test

### Frontend

```bash
cd frontend
npm ci
npm run build
```

The compiled frontend assets are written to `frontend/dist/`. You can also run `npm run lint` to check the frontend; a successful lint result has not been established in this documentation.

### Backend

Backend tests use an in-memory H2 database in MySQL compatibility mode. The existing test configuration requires the `database` Spring profile and a dummy OAuth2 registration. From the `backend` directory, run:

**Windows Command Prompt:**

```cmd
mvnw.cmd clean verify "-Dspring.profiles.active=database" "-Dspring.security.oauth2.client.registration.test.provider=google" "-Dspring.security.oauth2.client.registration.test.client-id=dummy-test-client" "-Dspring.security.oauth2.client.registration.test.client-secret=dummy-test-secret"
```

**Linux/macOS:**

```bash
./mvnw clean verify \
  -Dspring.profiles.active=database \
  -Dspring.security.oauth2.client.registration.test.provider=google \
  -Dspring.security.oauth2.client.registration.test.client-id=dummy-test-client \
  -Dspring.security.oauth2.client.registration.test.client-secret=dummy-test-secret
```

Maven `verify` executes the configured automated tests and packages the backend JAR in `backend/target/`. The CI run completed **39 tests, 0 failures, 0 errors, 0 skipped** and successfully created `anyguessr-backend-0.0.1-SNAPSHOT.jar`. These tests include Gameplay unit tests and JPA integration tests; they do not exercise live Cognito sign-in or the shared RDS service.

### Continuous integration

The team has prepared a GitHub Actions workflow at `.github/workflows/ci.yml`, submitted in [PR #40](https://github.com/NiallMahon/AnyGuessr/pull/40). It configures Java 25 backend build/test/package and a Node.js 24 frontend production build. [GitHub Actions run 44845422490](https://github.com/NiallMahon/AnyGuessr/actions/runs/44845422490) passed both jobs on 21 November 2026. The workflow file must be merged into the final submitted branch to count as part of the final repository.

## Packaging and deployment

The application can be run locally using the instructions above. No publicly hosted production deployment has been verified as part of this README.

For backend packaging, run the Maven `verify` command above. The packaged Spring Boot JAR is placed in `backend/target/`. To run the packaged backend from the repository root, with database and approved Cognito settings available:

```bash
java -jar backend/target/anyguessr-backend-0.0.1-SNAPSHOT.jar
```

For the frontend, run `npm ci` and `npm run build` inside `frontend/`. Publish the contents of `frontend/dist/` to a static web host configured to route API requests to the running Spring Boot backend. Adjust frontend API URLs, CORS allowed origins, Cognito redirect URIs and server-side configuration for the chosen host before deployment. The current defaults use localhost and must not be assumed suitable for public hosting. Use HTTPS, approved secrets management and appropriate database access controls for any real deployment.

Database storage and uploaded map images also need persistent storage and backup arrangements in a hosted environment. A complete production deployment procedure remains **unverified** until an actual target environment is configured and tested.

## Known limitations and verification status

- **Verified:** React homepage renders locally; Vite frontend build passed; local MySQL application account connected and read existing map data.
- **Verified:** GitHub Actions built the frontend, executed 39 backend tests successfully, and packaged the Spring Boot JAR.
- **Blocked / not verified:** An authenticated browser-to-database quiz session, map loading and statistics updates. Dummy OAuth2 startup is not a functioning Cognito login.
- **Not verified:** Public cloud deployment, performance, browser compatibility across devices and production security readiness.

Before demonstrating the final submission, the team should complete a real authentication flow, load maps, start and finish a quiz, verify results/statistics, and record the outcome. Any unresolved issues should be documented rather than represented as tested features.

## Team development workflow

The repository is used for group development through feature branches, commits and pull requests targeting `main`. Review each teammate's changes before merging and ensure the final `main` branch contains the agreed application code, documentation and CI workflow. The team should also demonstrate its project board, story assignments and progress at the scheduled CT5106 presentation.

This README describes the repository and verification evidence available at the time of writing. Confirm the final setup, team-specific deployment details and all listed features before submission.
