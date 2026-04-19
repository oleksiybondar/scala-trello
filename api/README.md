# API

Scala 3 backend for the monorepo. It provides REST for auth/ops endpoints and GraphQL for domain features.

## Stack

- Scala 3
- Cats / Cats Effect / FS2
- Http4s
- Sangria GraphQL
- Slick
- Flyway
- PostgreSQL
- Tapir + Swagger UI
- MUnit

## Implemented capabilities

### REST

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
- `POST /graphql`
- Swagger UI and GraphiQL routes

### GraphQL modules

- users
  - queries: `user`, `users`
  - mutations: `updateProfile`, `changeAvatar`, `changeUsername`, `changeEmail`, `changePassword`
- roles/permissions
  - queries: `role`, `roles`
- dictionaries
  - queries: `ticketSeverities`, `timeTrackingActivities`
- boards
  - queries: `board`, `myBoards(active, keyword, ownerUserId, offset, limit)`, `boardMembers` (+ legacy aliases kept)
  - mutations: create/update/deactivate board, ownership transfer, member invite/remove, member role changes
- tickets
  - queries: `ticket`, `tickets`, `myTickets(assignedOnly: Boolean, offset: Int, limit: Int)`
  - mutations: `createTicket`, `changeTicketTitle`, `changeTicketDescription`, `changeTicketAcceptanceCriteria`, `changeTicketEstimatedTime`, `changeTicketPriority`, `changeTicketSeverity`, `changeTicketStatus`, `reassignTicket`
- comments
  - queries: `comment`, `comments`, `commentsByUser`
  - mutations: `postComment`, `updateCommentMessage`, `deleteComment`
- time tracking
  - queries: `timeTrackingEntry`, `timeTrackingEntriesByUser(userId, offset, limit)`, `timeTrackingEntriesByTicket`
  - mutations: `createTimeTrackingEntry`, `updateTimeTrackingActivity`, `updateTimeTrackingDescription`, `updateTimeTrackingTime`, `deleteTimeTrackingEntry`

## Access behavior highlights

- inactive boards remain readable for board members (for example `board` and `boardMembers`)
- write operations on inactive boards remain restricted by permission rules

## Database and migrations

Flyway migrations and seeded reference data are applied for:

- users/auth sessions/password history
- boards and board memberships
- roles and permissions
- tickets and ticket states
- ticket severities
- comments
- time tracking and activities

## Local development

Prerequisites:

- JDK 17+
- `sbt`
- Docker (or local PostgreSQL)

Start DB:

```bash
docker compose up -d
```

Run migrations:

```bash
sbt migrate
```

Start API:

```bash
sbt app
```

Debug mode:

```bash
sbt dev
```

Migration + run:

```bash
sbt migrateAndRun
```

Package a standard JVM distribution:

```bash
sbt packageApp
```

Create a zipped distribution:

```bash
sbt distApp
```

The staged app is written to `target/universal/stage`, and the zip/tgz archives are written to `target/universal/`.

## Build in Docker

If you have a hardened host and do not want `sbt` installed on it, build the distribution inside a container and write the result back into the mounted working tree:

```bash
docker run --rm \
  -v "$PWD:/workspace" \
  -w /workspace \
  sbtscala/scala-sbt:latest \
  sbt clean dist
```

That command produces a standard JVM distribution under `target/universal/`. Unpack it on any machine with JRE 17+ and run:

```bash
docker run --rm \
  -v "$PWD:/workspace" \
  -w /workspace \
  sbtscala/scala-sbt:latest \
  sbt clean stage
```

That writes the runnable application layout to `target/universal/stage`, which you can start directly on any machine with JRE 17+:

```bash
target/universal/stage/bin/api
```

If you also want the dependency caches to survive between runs, mount them as volumes too:

```bash
docker run --rm \
  -v "$PWD:/workspace" \
  -v "$HOME/.ivy2:/root/.ivy2" \
  -v "$HOME/.cache/coursier:/root/.cache/coursier" \
  -w /workspace \
  sbtscala/scala-sbt:latest \
  sbt clean dist
```

## Run in Docker

Build the application image:

```bash
docker build -t api .
```

Run it against the PostgreSQL container from `docker-compose.yml`:

```bash
docker compose up -d postgres
docker run --rm \
  --name api \
  -p 8080:8080 \
  --network host \
  -e API_DB_URL=jdbc:postgresql://localhost:5432/api \
  -e API_DB_USER=api_user \
  -e API_DB_PASSWORD=api_password \
  -e API_JWT_SECRET=development-jwt-secret \
  -e API_PASSWORD_PEPPER=development-password-pepper \
  api
```

On Linux, `--network host` lets the container reach the local Postgres on `localhost`. On Docker Desktop, use `host.docker.internal` in `API_DB_URL` instead of `localhost`.

## Endpoints

- health: `http://localhost:8080/health`
- GraphQL: `http://localhost:8080/graphql`
- GraphiQL: `http://localhost:8080/docs/graphql`
- Swagger UI: `http://localhost:8080/docs`

## Quality commands

```bash
sbt fmt
sbt fmtCheck
sbt lint
sbt coverageCheck
sbt quality
```
