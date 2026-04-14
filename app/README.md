# App

Frontend for the training monorepo, built with React, TypeScript, Vite, MUI, and TanStack Query.

## Current scope

- authentication flows: register, login, refresh, logout
- current user loading and settings updates (profile, username, email, avatar, password, UI preferences)
- boards list page with search/owner/inactive filters and create board
- board page with ticket columns, drag-and-drop state transitions, ticket filters, and priority direction toggles
- board settings pages (general, members, ownership)
- my tickets page with search, severity, priority, and assigned-only filtering
- ticket details page with:
  - header `[ticketId] name` and board link
  - card-based editable sections (title, priority+severity, description, acceptance criteria)
  - activity panel with time velocity and activity distribution charts
- my time registration page stub

## Routes

- `/`
- `/home`
- `/login`
- `/register`
- `/boards`
- `/boards/:boardId`
- `/boards/:boardId/settings`
- `/boards/:boardId/settings/general`
- `/boards/:boardId/settings/members`
- `/boards/:boardId/settings/ownership`
- `/tickets`
- `/tickets/:ticketId`
- `/time-registration`
- `/settings/profile`
- `/settings/security`
- `/settings/ui-preferences`

## Stack

- React 19
- TypeScript
- Vite
- MUI
- TanStack Query
- Vitest + Testing Library
- Playwright

## Development

Install dependencies:

```bash
npm install
```

Run checks:

```bash
npm run lint
npm run typecheck
npm run build
npm run test:unit
npm run test:e2e
```

Start dev server:

```bash
npm run dev
```

Frontend API target:

- local default: `http://127.0.0.1:8080`
- override: `API_URL=http://127.0.0.1:9000 npm run dev`
- deployed frontend env: set `VITE_API_URL` to backend origin
