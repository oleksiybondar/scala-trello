# API

Scala 3 backend for the training project. The backend is where the Scala/FP/Cats/Cats Effect work happens, while also serving as the source of truth for authentication, persistence, and the future task-board domain.

## Scope

Architecture target:

- REST for health, auth, Swagger, and GraphiQL
- GraphQL for application/domain features
- PostgreSQL for persistence
- functional style with Cats and Cats Effect

Current implementation:

- `GET /health`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `POST /graphql`
- Swagger UI for REST endpoints
- GraphiQL page
- Flyway migrations for `users` and `auth_sessions`
- user repository and a basic authenticated GraphQL user query

Important status note:

- login currently looks up a user by username or email and issues tokens
- password validation is not implemented yet
- Google OIDC is not implemented yet
- ticket board, workflow states, and comments are not implemented yet

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
