# App

Frontend workspace for the training project. The goal is a small authenticated Trello-like UI built with React, TypeScript, Vite, and MUI on top of the Scala backend.

## Product direction

The intended UI will include:

- authenticated access to the board
- ticket listing and ticket detail views
- state transitions: `new -> in progress -> in review -> complete`
- comment thread per ticket
- login flows for local credentials and later Google OIDC

## Stack

- Vite
- React
- TypeScript
- MUI

## Local development

Install dependencies:

```bash
npm install
```

Run available checks:

```bash
npm run lint
npm run typecheck
```

The shared monorepo script can also run frontend checks from the repository root:

```bash
sh ./scripts/run-app-checks.sh pre-commit
```