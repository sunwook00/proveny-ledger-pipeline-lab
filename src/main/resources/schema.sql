CREATE TABLE IF NOT EXISTS product_passport (
  passport_id VARCHAR(100) PRIMARY KEY,
  serial_number VARCHAR(120) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGSERIAL PRIMARY KEY,
  aggregate_id VARCHAR(100) NOT NULL,
  aggregate_type VARCHAR(50) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  payload JSONB NOT NULL,
  idempotency_key VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at TIMESTAMPTZ,
  processing_owner VARCHAR(100),
  processing_started_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at TIMESTAMPTZ,
  last_error TEXT,
  CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING','PROCESSING','PUBLISHED','FAILED'))
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_outbox_idempotency_key ON outbox_event(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_event(status, next_retry_at, created_at) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_processing_timeout ON outbox_event(status, processing_started_at) WHERE status = 'PROCESSING';
CREATE TABLE IF NOT EXISTS ledger_chain (
  passport_id VARCHAR(100) PRIMARY KEY,
  last_seq BIGINT NOT NULL DEFAULT 0,
  last_hash VARCHAR(64),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS ledger_entry (
  ledger_id VARCHAR(100) PRIMARY KEY,
  passport_id VARCHAR(100) NOT NULL,
  seq BIGINT NOT NULL,
  event_category VARCHAR(50) NOT NULL,
  event_action VARCHAR(80) NOT NULL,
  actor_role VARCHAR(50) NOT NULL,
  actor_id VARCHAR(100) NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL,
  payload_json JSONB NOT NULL,
  payload_canonical TEXT NOT NULL,
  data_hash VARCHAR(64) NOT NULL,
  prev_hash VARCHAR(64),
  entry_hash VARCHAR(64) NOT NULL,
  idempotency_key VARCHAR(255) NOT NULL,
  schema_version INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_ledger_passport_seq UNIQUE(passport_id, seq),
  CONSTRAINT uq_ledger_idempotency_key UNIQUE(idempotency_key)
);
CREATE INDEX IF NOT EXISTS idx_ledger_passport_seq ON ledger_entry(passport_id, seq);
