package com.example.part35teammonew.domain.notification.Dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"content", "nextCursor", "nextAfter" ,"size",  "totalElements", "hasNext"})
public class CursorPageResponse<T> {

  private final List<T> content;
  private final String nextCursor;
  private final String nextAfter;
  private final Long size;
  private final Long totalElements;
  private final boolean hasNext;

  public CursorPageResponse(List<T> data, String nextCursor, String nextAfter, boolean hasNext,
      Long size, Long totalElement) {
    this.content = data;
    this.nextCursor = nextCursor;
    this.nextAfter = nextAfter;
    this.hasNext = hasNext;
    this.size = size;
    this.totalElements = totalElement;
  }

  public List<T> getContent() {
    return content;
  }

  public String getNextCursor() {
    return nextCursor;
  }

  public String getNextAfter() {
    return nextAfter;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public Long getSize() {
    return size;
  }

  public Long getTotalElements() {
    return totalElements;
  }
}