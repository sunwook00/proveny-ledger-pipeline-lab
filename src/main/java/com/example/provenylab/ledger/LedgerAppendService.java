package com.example.provenylab.ledger;

import com.example.provenylab.common.JsonCanonicalizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class LedgerAppendService {
  private final JdbcTemplate jdbc; private final JsonCanonicalizer canonicalizer; private final LedgerHashService hashService; private final ObjectMapper objectMapper;
  public LedgerAppendService(JdbcTemplate jdbc, JsonCanonicalizer canonicalizer, LedgerHashService hashService, ObjectMapper objectMapper){this.jdbc=jdbc;this.canonicalizer=canonicalizer;this.hashService=hashService;this.objectMapper=objectMapper;}
  @Transactional
  public AppendResult append(LedgerEvent event){
    Integer existing=jdbc.queryForObject("SELECT count(*) FROM ledger_entry WHERE idempotency_key=?", Integer.class, event.idempotencyKey());
    if(existing!=null && existing>0) return AppendResult.duplicateIgnored(event.idempotencyKey());
    jdbc.update("INSERT INTO ledger_chain(passport_id,last_seq,last_hash) VALUES (?,0,NULL) ON CONFLICT (passport_id) DO NOTHING", event.passportId());
    Map<String,Object> chain=jdbc.queryForMap("SELECT last_seq,last_hash FROM ledger_chain WHERE passport_id=? FOR UPDATE", event.passportId());
    long nextSeq=((Number)chain.get("last_seq")).longValue()+1;
    String prevHash=(String)chain.get("last_hash");
    String canonical=canonicalizer.canonicalize(event.payload());
    String dataHash=hashService.dataHash(canonical);
    String entryHash=hashService.entryHash(prevHash,dataHash,nextSeq,event);
    String ledgerId="led-"+UUID.randomUUID();
    int inserted=jdbc.update("""
      INSERT INTO ledger_entry(ledger_id,passport_id,seq,event_category,event_action,actor_role,actor_id,occurred_at,payload_json,payload_canonical,data_hash,prev_hash,entry_hash,idempotency_key,schema_version)
      VALUES (?,?,?,?,?,?,?,?,?::jsonb,?,?,?,?,?,?) ON CONFLICT (idempotency_key) DO NOTHING
      """, ledgerId,event.passportId(),nextSeq,event.eventCategory(),event.eventAction(),event.actorRole(),event.actorId(),Timestamp.from(event.occurredAt()),toJson(event.payload()),canonical,dataHash,prevHash,entryHash,event.idempotencyKey(),event.schemaVersion());
    if(inserted==0) return AppendResult.duplicateIgnored(event.idempotencyKey());
    jdbc.update("UPDATE ledger_chain SET last_seq=?, last_hash=?, updated_at=now() WHERE passport_id=?", nextSeq,entryHash,event.passportId());
    return AppendResult.appended(ledgerId,nextSeq,event.idempotencyKey());
  }
  public LedgerEvent fromOutboxPayload(String message){
    try{
      @SuppressWarnings("unchecked") Map<String,Object> root=objectMapper.readValue(message,Map.class);
      @SuppressWarnings("unchecked") Map<String,Object> payload=(Map<String,Object>)root.get("payload");
      return new LedgerEvent((String)root.get("passportId"),(String)root.getOrDefault("eventCategory","OWNERSHIP"),(String)root.get("eventAction"),(String)root.getOrDefault("actorRole","SYSTEM"),(String)root.getOrDefault("actorId","system"),Instant.parse((String)root.get("occurredAt")),payload,(String)root.get("idempotencyKey"),((Number)root.getOrDefault("schemaVersion",1)).intValue());
    } catch(JsonProcessingException e){ throw new IllegalArgumentException("Invalid ledger event message", e); }
  }
  private String toJson(Object payload){ try{return objectMapper.writeValueAsString(payload);}catch(JsonProcessingException e){throw new IllegalArgumentException("payload json serialization failed",e);} }
}
