package com.example.provenylab.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class JsonCanonicalizerTest {
  @Test
  void keyOrderDoesNotChangeCanonicalForm() {
    JsonCanonicalizer c = new JsonCanonicalizer();
    Map<String, Object> a = new LinkedHashMap<>();
    a.put("b", 2);
    a.put("a", 1);
    Map<String, Object> b = new LinkedHashMap<>();
    b.put("a", 1);
    b.put("b", 2);
    assertEquals(c.canonicalize(a), c.canonicalize(b));
  }
}
