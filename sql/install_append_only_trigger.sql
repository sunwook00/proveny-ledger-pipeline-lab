CREATE OR REPLACE FUNCTION prevent_ledger_entry_mutation()
RETURNS TRIGGER AS $$
BEGIN
  RAISE EXCEPTION 'ledger_entry is immutable: update/delete not allowed';
END;
$$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_ledger_entry_no_update ON ledger_entry;
DROP TRIGGER IF EXISTS trg_ledger_entry_no_delete ON ledger_entry;
CREATE TRIGGER trg_ledger_entry_no_update BEFORE UPDATE ON ledger_entry FOR EACH ROW EXECUTE FUNCTION prevent_ledger_entry_mutation();
CREATE TRIGGER trg_ledger_entry_no_delete BEFORE DELETE ON ledger_entry FOR EACH ROW EXECUTE FUNCTION prevent_ledger_entry_mutation();
