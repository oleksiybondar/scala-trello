#!/usr/bin/env sh
set -eu

root_dir="$(git rev-parse --show-toplevel)"
cd "$root_dir"

staged_files="$(git diff --cached --name-only --diff-filter=ACMR)"

if printf '%s\n' "$staged_files" | grep -Eq '^api/(build\.sbt|project/|src/|\.scalafmt\.conf$|\.scalafix\.conf$)'; then
  sh "$root_dir/scripts/run-api-checks.sh" pre-commit
fi

if printf '%s\n' "$staged_files" | grep -Eq '^app/'; then
  sh "$root_dir/scripts/run-app-checks.sh" pre-commit
fi
