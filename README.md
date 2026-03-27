# introduction_into_scala
Scala + React full stack application to onboard to Scala and functional programming

## Quality gates

The repository uses shared Git hooks at the monorepo root. Git only supports one hook entrypoint per repository, so the root hook dispatches checks per subproject using path filters:

- `api/` blocks commits on Scala-native formatting and linting
- `app/` will use the same root hook once `app/package.json` exists, but with app-native scripts only
- duplication and coverage thresholds are enforced in CI on pull requests, not on local commits

### Tooling

- `api/`: `scalafmt`, `scalafix`, compiler warning linting, `scapegoat`, `scoverage`
- `app/`: reserved for standard TypeScript-native tools such as `eslint` and the app test runner once the app exists
- CI only: `jscpd` for duplication

### Setup

Install hooks from the repository root:

```bash
sh ./scripts/install-hooks.sh
```

Local workstation requirements stay project-native:

- `api/`: JDK + `sbt`
- `app/`: Node.js once the app is initialized

### Manual runs

```bash
sh ./scripts/run-api-checks.sh pre-commit
sh ./scripts/run-api-checks.sh build
sh ./scripts/run-api-checks.sh migrate
sh ./scripts/run-api-checks.sh coverage
```

### App integration

When `app` is bootstrapped, add standard scripts to `app/package.json` so the shared hook and CI can execute them:

```json
{
  "scripts": {
    "lint": "eslint . --max-warnings=0",
    "build": "your-app-build-command",
    "test:coverage": "your-app-coverage-command"
  }
}
```

## CI sequence

The GitHub Actions pipeline is organized around the same policy:

1. Static analysis, including duplication
2. Build
3. Database migration for `api`
4. Tests with coverage

Deploy is intentionally out of scope for this training project.
