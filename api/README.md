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
  - queries: `board`, `myBoards`, `boardMembers` (+ legacy aliases kept)
  - mutations: create/update/deactivate board, ownership transfer, member invite/remove, member role changes
- tickets
  - queries: `ticket`, `tickets`, `myTickets(assignedOnly: Boolean)`
  - mutations: `createTicket`, `changeTicketTitle`, `changeTicketDescription`, `changeTicketAcceptanceCriteria`, `changeTicketEstimatedTime`, `changeTicketPriority`, `changeTicketSeverity`, `changeTicketStatus`, `reassignTicket`
- comments
  - queries: `comment`, `comments`, `commentsByUser`
  - mutations: `postComment`, `updateCommentMessage`, `deleteComment`
- time tracking
  - queries: `timeTrackingEntry`, `timeTrackingEntriesByUser`, `timeTrackingEntriesByTicket`
  - mutations: `createTimeTrackingEntry`, `updateTimeTrackingActivity`, `updateTimeTrackingDescription`, `updateTimeTrackingTime`, `deleteTimeTrackingEntry`

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
