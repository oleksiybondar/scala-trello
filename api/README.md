# API

Scala 3 backend for the training project. The backend is where the Scala/FP/Cats/Cats Effect work happens, while also serving as the source of truth for authentication, persistence, and a task-board domain that is growing toward a realistic project-management application.

## Scope

Architecture target:

- REST for health, auth, Swagger, and GraphiQL
- GraphQL for application/domain features
- PostgreSQL for persistence
- functional style with Cats and Cats Effect
- dashboard-based task and collaboration domain

Current implementation:

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
- `POST /graphql`
- Swagger UI for REST endpoints
- GraphiQL page
- Flyway migrations for auth, users, dashboards, memberships, tickets, comments, time tracking, and seeded reference data
- user repository plus authenticated GraphQL user queries and mutations

Important status note:

- registration creates a user, records the initial password in password history, and immediately issues tokens
- login verifies the password hash for the matched username or email
- `POST /auth/refresh` rotates the refresh token and returns a new token pair
- `POST /auth/logout` revokes the session tied to the provided refresh token
- `GET /auth/me` returns the current authenticated user for a valid access token
- user profile, username, email, avatar, and password changes are available through authenticated GraphQL mutations
- password changes enforce the configured strength rules and password-history checks
- the database schema already defines the planned dashboard and ticketing model, but the Scala application layer for that domain is still pending
- Google OIDC is not implemented yet
- dashboard, ticket, comment, and time-tracking behavior are not implemented yet in Scala/GraphQL

## Product direction

The planned application is a dashboard-oriented task board. A user can authenticate, own one dashboard, contribute to another dashboard, and operate under a dashboard-specific role. The backend keeps ownership, membership, workflow reference data, and authorization rules in PostgreSQL so later GraphQL features can build on a stable schema.

Planned business capabilities:

- register and authenticate users
- create dashboards with a dedicated owner
- add dashboard members and assign seeded roles such as `admin`, `contributor`, and `viewer`
- control dashboard, ticket, and comment access through seeded permissions
- create and manage tickets inside dashboards
- classify tickets by state and severity
- capture free-text scoping fields such as component, scope, and acceptance criteria
- add comments to tickets
- log work time against tickets with a seeded activity type
- support ownership transfer later without collapsing owner and member-role concepts

Reference data currently modeled in the database:

- roles
- permissions
- states
- severities
- activities

Current workflow vocabulary seeded in the database:

- roles: `admin`, `contributor`, `viewer`
- states: `new`, `in_progress`, `code_review`, `in_testing`, `done`
- severities: `minor`, `normal`, `major`
- activities: `code_review`, `development`, `testing`, `planning`, `design`, `documentation`, `refinement`, `debugging`

## Data model

The schema is intentionally split between top-level entities that use `UUID` identifiers and operational/reference records that use `BIGINT` identity columns.

Main entities:

- `users`: application users with profile and credential data
- `auth_sessions`: refresh-token backed auth sessions
- `password_history`: retained password hashes used for password reuse checks
- `dashboards`: top-level workspaces with owner and audit fields
- `dashboard_members`: dashboard-to-user membership rows with a role assignment
- `tickets`: dashboard-scoped work items with assignee, state, severity, and estimation fields
- `comments`: ticket-only comments with optional self-reference for reply chains
- `time_tracking`: work log entries linked to a ticket, user, and activity

Reference entities:

- `roles`: seeded role definitions
- `permissions`: per-role permissions for `dashboard`, `ticket`, and `comment` areas
- `states`: seeded ticket workflow states
- `severities`: seeded ticket severity levels
- `activities`: seeded time logging categories

Identifier strategy:

- `UUID`: `users`, `auth_sessions`, `dashboards`
- `BIGINT`: `roles`, `permissions`, `states`, `severities`, `activities`, `tickets`, `comments`, `time_tracking`

## Stack

- Scala 3
- Cats, Cats Effect, FS2
- Http4s
- Sangria GraphQL
- Circe
- Slick
- Flyway
- Tapir + Swagger UI
- PostgreSQL
- MUnit

## Local development

Prerequisites:

- JDK 17+
- `sbt`
- Docker or a local PostgreSQL server

Start the database:

```bash
docker compose up -d
```

Run migrations:

```bash
sbt migrate
```

Start the API:

```bash
sbt app
```

Run migration and app together:

```bash
sbt migrateAndRun
```

Default config from `src/main/resources/application.conf`:

- host: `0.0.0.0`
- port: `8080`
- database: `jdbc:postgresql://localhost:5432/api`
- user: `api_user`
- password: `api_password`

## Docs and endpoints

Once the server is running:

- health: `http://localhost:8080/health`
- GraphQL: `http://localhost:8080/graphql`
- GraphiQL: `http://localhost:8080/docs/graphql`
- Swagger UI: `http://localhost:8080/docs`

GraphQL is wrapped in auth middleware, so domain queries require a valid access token.

Current auth contract:

- `POST /auth/register` accepts `{ "email": "...", "password": "...", "first_name": "...", "last_name": "...", "username": "..." }`
- `POST /auth/login` accepts `{ "login": "...", "password": "..." }`
- `login` can be either a username or an email address
- registration and login return `access_token`, `refresh_token`, `token_type`, and `expires_in`
- registration requires a valid email and a password that satisfies the configured strength rules
- login fails when the user does not exist or the password does not match
- `GET /auth/me` requires `Authorization: Bearer <access_token>` and returns the current authenticated user
- use `Authorization: Bearer <access_token>` for protected GraphQL requests
- use the returned `refresh_token` with `POST /auth/refresh` to rotate the session
- use the returned `refresh_token` with `POST /auth/logout` to revoke the session

Example login request:

```json
{
  "login": "alice@example.com",
  "password": "secret123"
}
```

Example token response:

```json
{
  "access_token": "<jwt>",
  "refresh_token": "11111111-1111-1111-1111-111111111111",
  "token_type": "Bearer",
  "expires_in": 900
}
```

## Implementation status

Implemented in Scala today:

- auth REST API
- JWT access tokens and refresh-token session rotation
- password hashing, password strength validation, and password history checks
- authenticated GraphQL user queries
- authenticated GraphQL user mutations for profile, avatar, username, email, and password changes
- Slick repositories for current auth and user storage

Prepared in the database, but not implemented in Scala yet:

- dashboard creation and ownership flows
- dashboard membership management
- role and permission enforcement for dashboards
- ticket creation and workflow updates
- ticket comments
- time logging

## Quality

Useful commands:

```bash
sbt fmt
sbt fmtCheck
sbt lint
sbt coverageCheck
sbt quality
```

Or from the monorepo root:

```bash
sh ./scripts/run-api-checks.sh pre-commit
sh ./scripts/run-api-checks.sh build
sh ./scripts/run-api-checks.sh migrate
sh ./scripts/run-api-checks.sh coverage
```
