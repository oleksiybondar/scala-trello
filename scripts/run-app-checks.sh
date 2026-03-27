#!/usr/bin/env sh
set -eu

mode="${1:-pre-commit}"
root_dir="$(git rev-parse --show-toplevel)"
app_dir="$root_dir/app"

if [ ! -f "$app_dir/package.json" ]; then
  echo "Skipping app checks: app/package.json does not exist yet."
  exit 0
fi

if ! command -v node >/dev/null 2>&1; then
  echo "Node.js is required to run app checks." >&2
  exit 1
fi

cd "$app_dir"

scripts_json="$(node -p 'JSON.stringify((require("./package.json").scripts) || {})')"

has_script() {
  script_name="$1"
  printf '%s' "$scripts_json" | node -e '
    const fs = require("fs");
    const scriptName = process.argv[1];
    const scripts = JSON.parse(fs.readFileSync(0, "utf8"));
    process.exit(Object.prototype.hasOwnProperty.call(scripts, scriptName) ? 0 : 1);
  ' "$script_name"
}

require_script() {
  script_name="$1"
  if ! has_script "$script_name"; then
    echo "app/package.json must define a '$script_name' script." >&2
    exit 1
  fi
}

case "$mode" in
  pre-commit|static)
    require_script lint
    require_script typecheck
    npm run lint
    exec npm run typecheck
    ;;
  build)
    if has_script build; then
      exec npm run build
    fi
    echo "Skipping app build: no build script defined yet."
    exit 0
    ;;
  coverage)
    if has_script test:coverage; then
      exec npm run test:coverage
    fi
    if has_script coverage; then
      exec npm run coverage
    fi
    echo "app/package.json must define either 'test:coverage' or 'coverage'." >&2
    exit 1
    ;;
  *)
    echo "Usage: $0 {pre-commit|static|build|coverage}" >&2
    exit 1
    ;;
esac
