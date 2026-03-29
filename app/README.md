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
npm run build
npm run test:unit
npm run test:e2e
```

Start the dev server:

```bash
npm run dev
```

## Source layout

The initial bootstrap keeps shared code organized and leaves room for future
automatic routing:

```text
src/
  app/
  components/
  contexts/
  helpers/
  hooks/
  pages/
  routes/
  theme/
  main.tsx
```

Current routing:

- `/` -> `HomePage`
- `/home` -> `HomePage`

Theming bootstrap:

- `ThemeManagerProvider` owns source, stored settings, OS detection, and localStorage sync
- `AppThemeProvider` owns MUI theme application and baseline styles
- source is explicit: `default`, `os`, or `user`
- user-controlled `mode` and `template` settings can be extended later

The shared monorepo script can also run frontend checks from the repository root:

```bash
sh ./scripts/run-app-checks.sh pre-commit
sh ./scripts/run-app-checks.sh build
sh ./scripts/run-app-checks.sh test
```

## Testing layout

Tests live in a dedicated top-level `tests/` directory:

```text
tests/
  e2e/
    pageObjects/
    support/
  setup/
  unit/
```

- `tests/unit` holds Vitest and React Testing Library tests
- `tests/e2e` holds Playwright specs and page objects
- `tests/e2e/support` is the place for browser-level helpers such as API mocking
- `tests/setup` holds shared test bootstrapping utilities
