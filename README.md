# Introduction Into Scala

Practice monorepo for learning Scala, functional programming, Cats, Cats Effect, and a TypeScript frontend stack.

The product is a dashboard-oriented task management application with authentication, dashboard membership, roles, ticket workflow, ticket comments, and time logging.

Core product scope:

- authenticated users can create and own dashboards
- users can participate in multiple dashboards with different roles per dashboard
- dashboard access is based on seeded roles such as `admin`, `contributor`, and `viewer`
- tickets live inside dashboards and move through seeded workflow states
- tickets can carry severity, assignment, estimation, component, scope, and acceptance criteria data
- each ticket has a comment thread
- time can be logged against tickets using seeded activity types such as development, testing, review, and planning
- authentication supports local email/password today and may expand later
- users remain local application users regardless of registration method
- backend uses REST for auth, health/docs, Swagger, and GraphiQL
- backend uses GraphQL for business/domain features
- frontend uses Vite, React, TypeScript, and MUI for fast UI bootstrap

This repository is intentionally a monorepo because it is a training project. In a production-oriented setup, the backend and frontend would likely live in separate repositories.

## Repository layout

```text
.
├── api/      Scala 3 backend
├── app/      Vite + React + TypeScript frontend
└── scripts/  shared monorepo tooling
```

Project-specific documentation:

- [`api/README.md`](./api/README.md)
- [`app/README.md`](./app/README.md)

## Running the project

Prerequisites:

- JDK 17+ and `sbt`
- Node.js and `npm`
- Docker or a local PostgreSQL instance

Install shared Git hooks from the repository root:

```bash
sh ./scripts/install-hooks.sh
```

Start PostgreSQL for the backend:

```bash
cd api
docker compose up -d
```

Run backend migration and server:

```bash
cd api
sbt migrate
sbt app
```

Run frontend checks:

```bash
cd app
npm run lint
npm run typecheck
```

## Quality gates

The monorepo uses shared root hooks that dispatch checks by changed paths:

- `api/`: `scalafmt`, `scalafix`, compile-time linting, `scapegoat`, and coverage rules
- `app/`: `eslint`, `tsc --noEmit`, production build, and unit tests
- CI only: `jscpd` duplication checks

The backend also enforces explicit public/protected result types and bans mutable or exception-style syntax such as `var`, `throw`, `return`, `while`, `asInstanceOf`, and `isInstanceOf`.

Manual runs:

```bash
sh ./scripts/run-api-checks.sh pre-commit
sh ./scripts/run-api-checks.sh build
sh ./scripts/run-api-checks.sh migrate
sh ./scripts/run-api-checks.sh coverage
sh ./scripts/run-app-checks.sh pre-commit
sh ./scripts/run-app-checks.sh build
sh ./scripts/run-app-checks.sh test
```

This is a learning-first project. Clear structure, incremental delivery, and explicit tradeoffs matter more here than shipping features quickly.
