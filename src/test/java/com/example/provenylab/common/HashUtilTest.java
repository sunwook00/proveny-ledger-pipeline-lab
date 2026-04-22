package com.example.provenylab.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HashUtilTest {
  @Test
  void sha256IsDeterministic() {
    String a = HashUtil.sha256("hello");
    String b = HashUtil.sha256("hello");
    assertEquals(a, b);
    assertEquals(64, a.length());
  }
}
