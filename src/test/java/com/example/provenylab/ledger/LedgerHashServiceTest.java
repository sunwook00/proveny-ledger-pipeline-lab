package com.example.provenylab.ledger;
import org.junit.jupiter.api.Test; import java.time.Instant; import java.util.Map; import static org.junit.jupiter.api.Assertions.*;
class LedgerHashServiceTest { @Test void entryHashChangesWhenPayloadHashChanges(){LedgerHashService service=new LedgerHashService();LedgerEvent event=new LedgerEvent("psp-1","OWNERSHIP","MINT","BRAND","u1",Instant.parse("2026-01-01T00:00:00Z"),Map.of("a",1),"key",1);String h1=service.entryHash(null,"data1",1,event);String h2=service.entryHash(null,"data2",1,event);assertNotEquals(h1,h2);assertEquals(64,h1.length());} }
