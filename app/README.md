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
```
