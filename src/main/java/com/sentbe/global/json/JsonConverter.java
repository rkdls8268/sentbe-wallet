package com.sentbe.global.json;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JsonConverter {

  private final ObjectMapper objectMapper;

  public String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      throw new RuntimeException("failed serialization error", e);
    }
  }

  public <T> T fromJson(String payload, Class<T> type) {
    try {
      return objectMapper.readValue(payload, type);
    } catch (Exception e) {
      throw new RuntimeException("failed deserialization error", e);
    }
  }
}
