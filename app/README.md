# App

Frontend workspace for the training project. The app is built with React, TypeScript, Vite, and MUI on top of the Scala backend, and currently focuses on authenticated user flows while the dashboard and ticket domain is still being prepared on the backend.

## Product direction

Current UI capabilities:

- login with local credentials
- registration with client-side email and password validation
- authenticated session handling with access token refresh
- current-user loading from `/auth/me`
- user settings area with profile editing
- user security settings for username, email, avatar, and password changes
- UI preferences settings backed by the theme manager

Planned next-stage product capabilities:

- dashboard listing and dashboard detail views
- dashboard membership and role-aware access
- ticket listing and ticket detail views
- workflow state transitions
- ticket comments
- time logging against tickets
- later Google OIDC support

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

During local development, Vite proxies backend-bound requests to
`http://127.0.0.1:8080` by default for:

- `/auth/*`
- `/graphql`

Override the proxy target with an environment variable when needed:

```bash
API_URL=http://127.0.0.1:9000 npm run dev
```

For deployed frontend environments such as Vercel, set `VITE_API_URL` to the
public backend origin so browser requests go to the backend instead of the
frontend host:

```bash
VITE_API_URL=https://your-backend.example.com
```

## Source layout

The initial bootstrap keeps shared code organized and leaves room for future
automatic routing:

```text
src/
  app/
  components/
  contexts/
  features/
  providers/
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
- `/login` -> `LoginPage`
- `/register` -> `RegisterPage`
- `/settings` -> redirects to `/settings/profile`
- `/settings/profile` -> `UserProfileSettingsPage`
- `/settings/security` -> `UserSecuritySettingsPage`
- `/settings/ui-preferences` -> `UserUiPreferencesSettingsPage`

Current frontend/backend integration:

- REST auth endpoints are used for register, login, refresh, logout, and current-user loading
- GraphQL is already wired in the frontend helper layer and is intended for future dashboard and ticket features
- auth state is provided through `AuthProvider`
- current user loading is provided through `CurrentUserProvider`
- settings mutations are organized under `features/user`

Theming bootstrap:

- `ThemeManagerProvider` owns source, stored settings, OS detection, and localStorage sync
- `AppThemeProvider` owns MUI theme application and baseline styles
- source is explicit: `default`, `os`, or `user`
- user-controlled `mode` and `template` settings can be extended later

## Implementation status

Implemented today:

- login page
- registration page
- auth provider with token lifecycle handling
- current-user bootstrap against the backend
- settings shell and sidebar navigation
- profile editing UI
- security editing UI for username, email, avatar, and password
- UI preferences page and theme controls
- unit and e2e test scaffolding

Planned, but not implemented yet:

- dashboard pages
- dashboard member management
- dashboard role-aware UI
- ticket CRUD flows
- comment UI
- time logging UI

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
