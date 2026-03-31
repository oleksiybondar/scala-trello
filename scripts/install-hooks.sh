#!/usr/bin/env sh
set -eu

root_dir="$(git rev-parse --show-toplevel)"
cd "$root_dir"

git config core.hooksPath .githooks

echo "Git hooks are installed from .githooks/"
