package com.example.part35teammonew.domain.notification.Dto;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
public class CursorPageRequest {

  private final Instant  cursor;
  private final Instant  after;
  private final int limit;

  public CursorPageRequest(Instant  cursor, Instant after, int limit) {
    this.cursor = cursor;
    this.after = after;
    this.limit = limit;
  }


}