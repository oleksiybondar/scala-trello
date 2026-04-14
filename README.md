# Introduction Into Scala

Monorepo for a task-board application with Scala backend and React frontend.

This project was started as a practical Scala/Cats showcase, inspired by an ongoing application process. The goal was to ramp up quickly on a new stack and deliver a working end-to-end product that demonstrates hands-on backend and frontend implementation.

## Repository layout

```text
.
├── api/      Scala 3 backend (REST + GraphQL)
├── app/      React + TypeScript frontend
└── scripts/  shared quality/check scripts
```

## Product coverage

- authentication and session lifecycle
- user profile and account management
- boards with membership and role-based access
- board settings and ownership management
- tickets with state workflow, assignment, priority, severity, estimates, and acceptance criteria
- comments on tickets
- time tracking entries with activity classification
- personal ticket views and ticket details editing

## Run locally

Prerequisites:

- JDK 17+ and `sbt`
- Node.js and `npm`
- Docker (or local PostgreSQL)

Start DB:

```bash
cd api
docker compose up -d
```

Run backend:

```bash
cd api
sbt migrate
sbt app
```

Run frontend:

```bash
cd app
npm install
npm run dev
```

## Quality commands

From repository root:

```bash
sh ./scripts/run-api-checks.sh pre-commit
sh ./scripts/run-api-checks.sh build
sh ./scripts/run-api-checks.sh migrate
sh ./scripts/run-api-checks.sh coverage

sh ./scripts/run-app-checks.sh pre-commit
sh ./scripts/run-app-checks.sh build
sh ./scripts/run-app-checks.sh test
```

Project docs:

- [`api/README.md`](./api/README.md)
- [`app/README.md`](./app/README.md)
