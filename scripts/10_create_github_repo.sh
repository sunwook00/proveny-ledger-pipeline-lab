#!/usr/bin/env bash
set -euo pipefail
REPO_NAME="${1:-proveny-ledger-pipeline-lab}"
if ! command -v gh >/dev/null 2>&1; then
  echo "gh CLI가 필요합니다: brew install gh"
  exit 1
fi
gh auth status || {
  echo "먼저 gh auth login 을 실행하세요."
  exit 1
}
if [ ! -d .git ]; then git init; fi
git add .
git commit -m "docs: initialize Proveny ledger pipeline reproduction lab" || true
gh repo create "$REPO_NAME" --private --source=. --remote=origin --push
