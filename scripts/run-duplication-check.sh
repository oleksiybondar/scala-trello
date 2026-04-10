#!/usr/bin/env sh
set -eu

root_dir="$(git rev-parse --show-toplevel)"
cd "$root_dir"

set --

if [ -d "$root_dir/api/src" ]; then
  set -- "$@" "$root_dir/api/src"
fi

if [ -d "$root_dir/app/src" ]; then
  set -- "$@" "$root_dir/app/src"
fi

if [ "$#" -eq 0 ]; then
  echo "Skipping duplication check: no source directories found."
  exit 0
fi

exec npx --yes jscpd --config "$root_dir/.jscpd.json" "$@"
