package com.example.provenylab.common;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class HashUtilTest { @Test void sha256IsDeterministic(){String a=HashUtil.sha256("hello");String b=HashUtil.sha256("hello");assertEquals(a,b);assertEquals(64,a.length());} }
