package com.example.part35teammonew.domain.interest.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record InterestPageRequest(
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    LocalDateTime after,
    int limit,
    UUID userId
) {

  public String safeCursor() {
    return (cursor == null || cursor.isBlank()) ? null : cursor;
  }

}
