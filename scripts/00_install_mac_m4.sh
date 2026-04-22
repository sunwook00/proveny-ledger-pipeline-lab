#!/usr/bin/env bash
set -euo pipefail
if ! command -v brew >/dev/null 2>&1; then
  echo "Homebrew가 없습니다. https://brew.sh/ 에서 먼저 설치하세요."
  exit 1
fi
brew install openjdk@21 gradle git gh jq || true
if ! command -v docker >/dev/null 2>&1; then brew install --cask docker || true; fi
grep -q 'openjdk@21/bin' ~/.zshrc 2>/dev/null || echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >>~/.zshrc
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
java -version || true
echo "완료. Docker Desktop 앱을 실행한 뒤 scripts/01_infra_up.sh 를 실행하세요."
