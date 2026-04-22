package com.example.provenylab.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

@Component
public class JsonCanonicalizer {
  private final ObjectMapper mapper;

  public JsonCanonicalizer() {
    this.mapper = new ObjectMapper();
    this.mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    this.mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
  }

  public String canonicalize(Object payload) {
    try {
      Object normalized = mapper.readValue(mapper.writeValueAsString(payload), Object.class);
      return mapper.writeValueAsString(normalized);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("canonicalization failed", e);
    }
  }

  public Object parseJson(String json) {
    try {
      return mapper.readValue(json, Object.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("invalid json", e);
    }
  }
}
