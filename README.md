# Proveny Ledger/Event Reliability Pipeline Lab

이 저장소는 원본 Proveny 팀 프로젝트를 그대로 복제한 repo가 아닙니다. 포트폴리오에서 설명하는 **Ledger/Event Reliability Pipeline**을 면접 준비와 기술 내재화를 위해 별도로 재현한 최소 구현 repo입니다.

## 역할 경계

### 본인
- Ledger/Event Reliability Pipeline 구현·검증·재현
- Frontend AI-assisted SPA
- GitHub Webhook 연동
- Namecheap 구매 도메인의 Cloudflare DNS 연결

### 팀원
- Auth/RBAC
- Product/Workflow Core
- GitOps/EKS
- 주요 Infra 구성
- main branch merge 관리

## 빠른 실행

```bash
./scripts/00_install_mac_m4.sh
./scripts/01_infra_up.sh
./scripts/02_run_app.sh
```

새 터미널에서:

```bash
./scripts/03_demo_e2e.sh
./scripts/04_skip_locked_demo.sh
./scripts/05_idempotency_demo.sh
./scripts/06_tamper_verify_false_demo.sh
./scripts/07_recovery_demo.sh
./scripts/08_metrics_demo.sh
```

## 면접에서 보여줄 파일

| 주제 | 파일 |
|---|---|
| schema | `src/main/resources/schema.sql` |
| SKIP LOCKED claim | `OutboxClaimService.java` |
| Publisher 상태 전이 | `OutboxPublisher.java` |
| Recovery Scheduler | `OutboxRecoveryScheduler.java` |
| Kafka Consumer + idempotency | `LedgerKafkaConsumer.java`, `LedgerAppendService.java` |
| Hash chain | `HashUtil.java`, `JsonCanonicalizer.java`, `LedgerHashService.java` |
| Verify API | `LedgerVerificationService.java`, `LedgerController.java` |

## GitHub repo 생성

```bash
gh auth login
./scripts/10_create_github_repo.sh proveny-ledger-pipeline-lab
```

## 주의

이 repo를 “원본 프로젝트 당시 작성한 코드”라고 말하지 마세요. 정확한 표현은 “Proveny 팀 프로젝트에서 다룬 Ledger/Event Reliability Pipeline을 프로젝트 종료 후 직접 재현 구현한 repo”입니다.
