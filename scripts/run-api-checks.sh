#!/usr/bin/env sh
set -eu

mode="${1:-}"
root_dir="$(git rev-parse --show-toplevel)"

cd "$root_dir/api"

case "$mode" in
  pre-commit)
    exec sbt fmtCheck lint
    ;;
  static)
    exec sbt fmtCheck lint
    ;;
  build)
    exec sbt compile Test/compile
    ;;
  migrate)
    exec sbt migrate
    ;;
  coverage)
    exec sbt coverageCheck
    ;;
  *)
    echo "Usage: $0 {pre-commit|static|build|migrate|coverage}" >&2
    exit 1
    ;;
esac
